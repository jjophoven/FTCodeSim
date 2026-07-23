package org.codeblooded.ftcodesim.simulator;

import android.os.Build;
import androidx.annotation.RequiresApi;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import org.codeblooded.ftcodesim.driverstation.OpModeState;
import org.codeblooded.ftcodesim.ascope.AdvantageScopeRunner;
import org.codeblooded.ftcodesim.driverstation.packets.InitOpModePacket;
import org.codeblooded.ftcodesim.driverstation.packets.KeyPacket;
import org.codeblooded.ftcodesim.driverstation.packets.OpModeCommandPacket;
import org.codeblooded.ftcodesim.driverstation.packets.Packet;
import org.codeblooded.ftcodesim.hardware.SimHardwareMap;
import org.codeblooded.ftcodesim.hardware.devices.SimTelemetry;
import org.codeblooded.ftcodesim.input.Keybinds;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

// TODO simulate rolling and colliding game pieces,
// TODO intake and shoot game pieces (low priority)
// TODO modify ascope state for less user setup
// add default code blooded robot models?
// automatically download and open ascope on run?
public class FTCodeSim {
    private static final int PORT = 8080;

    private ServerSocket listener;
    private Socket clientSocket;
    private DataInputStream in;
    private DataOutputStream out;
    private Process driverStationWindow;

    public boolean clientConnected = false;

    public volatile OpModeState state = OpModeState.WAIT_FOR_INIT;

    SimHardwareMap simHardwareMap;
    Keybinds gamepad1Keybinds;
    Keybinds gamepad2Keybinds;
    OpModeRegister opModeRegister = new OpModeRegister();
    SimConfig config;
    SimTelemetry telemetry;
    volatile OpModeLifecycle opModeLifecycle;

    AdvantageScopeRunner advantageScope;

    // TODO create a way to select from multiple "simulated" robots
    @RequiresApi(api = Build.VERSION_CODES.O)
    public FTCodeSim(SimConfig config) throws IOException {
        this.gamepad1Keybinds = config.gamepad1Keybinds;
        this.gamepad2Keybinds = config.gamepad2Keybinds;
        this.config = config;
        this.simHardwareMap = this.config.simHardwareMap;
        this.telemetry = new SimTelemetry(this);

        startServer();
        acceptClient();

        advantageScope = new AdvantageScopeRunner(config.field);
        config.simHardwareMap.update();
        advantageScope.saveConfig();

        new Thread(() -> {
            while (windowIsRunning()) {
                readPackets();
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }, "DS INPUT").start();
    }

    public void run() throws InterruptedException {
        while (windowIsRunning()) {
            if (opModeLifecycle == null) {
                Thread.sleep(config.loopTimeMs);
                continue;
            }
            opModeLifecycle.runOpMode();
            opModeLifecycle = null;
        }
        close();
    }

    public boolean windowIsRunning() {
        return driverStationWindow.isAlive() && advantageScope.isOpen();
    }

    public void startServer() throws IOException {
        log("Connecting to Fake Driver Station on port " + PORT);

        listener = new ServerSocket(PORT);

        driverStationWindow = startDriverStationProcess();

        clientConnected = false;
    }

    public void acceptClient()  {
        if (!clientConnected) {
            try {
                clientSocket = listener.accept();

                in = new DataInputStream(clientSocket.getInputStream());
                out = new DataOutputStream(clientSocket.getOutputStream());

                opModeRegister.writeOpmodes(out);

                clientConnected = true;

                log("Fake Driver Station connected on port " + PORT);

            } catch (IOException e) {
                log("Error connecting to Fake DS" + e);
                close();
            }
        }
    }

    private final Set<Integer> heldKeys = new HashSet<>();

    public void readPackets() {
        try {
            while (in.available() > 0) {
                byte type = in.readByte();
                switch (type) {
                    case Packet.KEY:
                        KeyPacket keyPacket = KeyPacket.read(in);

                        System.out.println("KEY: " + keyPacket.keyCode + ", " + (keyPacket.down ? "pressed" : "released"));

                        if (keyPacket.down) heldKeys.add(keyPacket.keyCode);
                        else heldKeys.remove(keyPacket.keyCode);

                        // Note: edge detection does not work over multiple keys
                        opModeLifecycle.latestGamepad1Data = gamepad1Keybinds.getByteArray(heldKeys);

                        break;
                    case Packet.CONTROLLER:
                        int index = in.readByte();
                        byte[] gamepad = new byte[65];
                        in.readFully(gamepad);

                        if (opModeLifecycle != null) {
                            if (index == 0) {
                                opModeLifecycle.latestGamepad1Data = gamepad;
                            } else if (index == 1) {
                                opModeLifecycle.latestGamepad2Data = gamepad;
                            }
                        }

                        break;
                    case Packet.OPMODE_COMMAND:
                        OpModeCommandPacket command = OpModeCommandPacket.read(in);
                        System.out.println("RECEIVED COMMAND: " + command);

                        if (command.equals(OpModeCommandPacket.START)) {
                            opModeLifecycle.isStarted = true;
                        }
                        if (command.equals(OpModeCommandPacket.STOP)) {
                            opModeLifecycle.isStopped = true;
                        }
                        break;
                    case Packet.INIT_OPMODE:
                        InitOpModePacket packet = InitOpModePacket.read(in);
                        OpMode selectedOpMode = opModeRegister.getOpMode(packet);
                        System.out.println("RECEIVED OPMODE " + selectedOpMode.getClass().getSimpleName());
                        opModeLifecycle = new OpModeLifecycle(selectedOpMode, telemetry, simHardwareMap, config.loopTimeMs);
                        break;
                    default:
                        System.out.println("UNKNOWN PACKET TYPE: " + type);
                        break;
                }
            }
        } catch (IOException e) {
            log("Error updating Fake DS" + e);
            close();
        }
    }

    public void sendTelemetry(String data) {
        if (out == null) {
            return;
        }

        try {
            out.writeByte(Packet.TELEMETRY);
            out.writeUTF(data);
            out.flush();
        } catch (IOException ignored) {
            close();
        }
    }

    public void close() {
        clientConnected = false;

        try {
            if (clientSocket != null) clientSocket.close();
        } catch (Exception ignored) {
        }

        try {
            if (listener != null) listener.close();
        } catch (Exception ignored) {
        }

        if (driverStationWindow != null) {
            driverStationWindow.destroy();
        }

        state = null;

        log("Driver Station shutdown.");
    }

//    private static Process startDriverStationProcess() throws IOException {
//        File projectRoot = findProjectRoot();
//        File driverStationJar = new File(projectRoot,
//                "DriverStationWindow/build/libs/DriverStationWindow.jar");
//
//        if (!driverStationJar.exists()) {
//            buildDriverStationJar(projectRoot);
//        }
//
//        String javaExe = findJavaExecutable();
//        Process process = new ProcessBuilder(
//                javaExe,
//                "-jar",
//                driverStationJar.getAbsolutePath()
//        )
//                .directory(projectRoot)
//                .redirectErrorStream(true)
//                .start();
//
//        new Thread(() -> {
//            try (BufferedReader reader = new BufferedReader(
//                    new InputStreamReader(process.getInputStream()))) {
//
//                String line;
//                while ((line = reader.readLine()) != null) {
//                    System.out.println("[DriverStation CLI] " + line);
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }).start();
//        return process;
//    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static Process startDriverStationProcess() throws IOException {
        File projectRoot = findProjectRoot();

        URL resource = Objects.requireNonNull(
                FTCodeSim.class.getClassLoader()
                        .getResource("assets/DriverStationWindow-all.jar"),
                "Could not find DriverStationWindow-all.jar in resources"
        );

        Path tempJar = Files.createTempFile(
                "DriverStationWindow-all",
                ".jar"
        );

        try (InputStream input = resource.openStream()) {
            Files.copy(
                    input,
                    tempJar,
                    StandardCopyOption.REPLACE_EXISTING
            );
        }

        String javaExe = findJavaExecutable();

        Process process = new ProcessBuilder(
                javaExe,
                "-jar",
                tempJar.toAbsolutePath().toString()
        )
                .directory(projectRoot)
                .redirectErrorStream(true)
                .start();

        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("[DriverStation CLI] " + line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        return process;
    }

    /**
     * Build the DriverStationWindow JAR using Gradle (one-time operation).
     * This is only called if the JAR doesn't already exist.
     */
    private static void buildDriverStationJar(File projectRoot) throws IOException {
        System.out.println("[DriverStation] Building DriverStationWindow JAR (this happens once)...");

        File gradlew = new File(projectRoot, isWindows() ? "gradlew.bat" : "gradlew");
        Process buildProcess = new ProcessBuilder(
                gradlew.getAbsolutePath(),
//                ":DriverStationWindow:build"
                ":DriverStationWindow:shadowJar"
        )
                .directory(projectRoot)
                .redirectErrorStream(true)
                .start();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(buildProcess.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[DriverStation Build] " + line);
            }
        }

        int exitCode;
        try {
            exitCode = buildProcess.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Build interrupted", e);
        }

        if (exitCode != 0) {
            throw new IOException("Failed to build DriverStationWindow JAR (exit code: " + exitCode + ")");
        }

        System.out.println("[DriverStation] JAR build complete!");
    }

    /**
     * Find the Java executable to use.
     * Prefers JAVA_HOME environment variable, falls back to 'java' in PATH.
     */
    private static String findJavaExecutable() {
        String javaHome = System.getenv("JAVA_HOME");
        if (javaHome != null && !javaHome.isEmpty()) {
            File javaExe = new File(javaHome, "bin" + File.separator + (isWindows() ? "java.exe" : "java"));
            if (javaExe.exists()) {
                return javaExe.getAbsolutePath();
            }
        }
        // Fall back to 'java' in PATH
        return isWindows() ? "java.exe" : "java";
    }

    private static File findProjectRoot() {
        return new File(Objects.requireNonNull(System.getProperty("user.dir"))).getParentFile();
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    private static void log(String msg) {
        System.out.println("[DriverStation] " + msg);
    }
}