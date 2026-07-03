package org.jjophoven.simulator;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.Gamepad;
import org.jjophoven.driverstation.packets.*;
import org.jjophoven.fakehardware.FakeHardwareMap;
import org.jjophoven.fakehardware.devices.FakeTelemetry;
import org.jjophoven.input.Keybinds;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

// TODO simulate boundary collisions
// TODO simulate rolling and colliding game pieces,
// TODO intake and shoot game pieces (low priority)
// TODO optimize loop times and make it configurable + fix ascope time lapsed
// TODO add ascope and default robot models in github releases
public class DriverStationSimulator {
    private static final int PORT = 8080;
    private static final int SOCKET_TIMEOUT_MS = 30000;

    private ServerSocket listener;
    private Socket clientSocket;
    private DataInputStream in;
    private DataOutputStream out;
    private Process driverStationWindow;

    public boolean clientConnected = false;

    public OpModeState state = OpModeState.WAIT_FOR_INIT;

    OpMode opMode;
    FakeHardwareMap fakeHardwareMap;
    Keybinds gamepad1Keybinds;
    Keybinds gamepad2Keybinds;
    OpModeRegister opModeRegister = new OpModeRegister();
    SimulationConfig simulationConfig;

    public DriverStationSimulator(SimulationConfig config) throws IOException, InterruptedException {
        this.gamepad1Keybinds = config.gamepad1Keybinds;
        this.gamepad2Keybinds = config.gamepad2Keybinds;
        this.simulationConfig = config;
        this.fakeHardwareMap = simulationConfig.fakeHardwareMap;

        startServer();
        acceptClient();

        while (driverStationWindow.isAlive()) {
            System.out.println("Waiting for next opmode...");

            while (state == OpModeState.WAIT_FOR_INIT) {
                poll();
                Thread.sleep(20);
            }

            if (opMode == null || state == null) {
                close();
                return;
            }

            opMode.init();

            while (state == OpModeState.INITIALIZING) {
                update();

                opMode.init_loop();

                Thread.sleep(20);
            }

            opMode.start();

            while (state == OpModeState.RUNNING) {
                update();

                opMode.loop();

                Thread.sleep(20);
            }

            opMode.stop();
        }
    }

    public void startServer() throws IOException {
        log("Connecting to Fake Driver Station on port " + PORT);

        listener = new ServerSocket(PORT);
        listener.setSoTimeout(SOCKET_TIMEOUT_MS);

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

    public void update() {
        poll();
        fakeHardwareMap.update();
    }

    public void poll() {
        if (!driverStationWindow.isAlive()) {
            close();
            return;
        }

        try {
            while (in.available() > 0) {
                switch (in.readByte()) {
                    case Packet.KEY:
                        if (opMode == null) return;

                        KeyPacket keyPacket = KeyPacket.read(in);

                        System.out.println("KEY: " + keyPacket.keyCode + ", " + (keyPacket.down ? "pressed" : "released"));

                        if (keyPacket.down) heldKeys.add(keyPacket.keyCode);
                        else heldKeys.remove(keyPacket.keyCode);

                        gamepad1Keybinds.apply(opMode.gamepad1, heldKeys);
                        gamepad2Keybinds.apply(opMode.gamepad2, heldKeys);

                        break;
                    case Packet.STATE:
                        this.state = OpModeState.read(in);
                        break;
                    case Packet.OPMODE:
                        System.out.println("RECEIVED OPMODES");
                        OpModePacket packet = OpModePacket.read(in);
                        opMode = opModeRegister.getOpMode(packet);

                        opMode.telemetry = new FakeTelemetry(this);

                        opMode.hardwareMap = fakeHardwareMap;
                        opMode.gamepad1 = new Gamepad();
                        opMode.gamepad2 = new Gamepad();

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
        if (opMode != null) {
            opMode.stop();
        }

        log("Driver Station shutdown.");
    }

    private static Process startDriverStationProcess() throws IOException {
        File projectRoot = findProjectRoot();
        File driverStationJar = new File(projectRoot,
                "DriverStationClient/build/libs/DriverStationWindow.jar");

        if (!driverStationJar.exists()) {
            buildDriverStationJar(projectRoot);
        }

        String javaExe = findJavaExecutable();
        Process process = new ProcessBuilder(
                javaExe,
                "-jar",
                driverStationJar.getAbsolutePath()
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
     * Build the DriverStationWindow fat JAR using Gradle (one-time operation).
     * This is only called if the JAR doesn't already exist.
     */
    private static void buildDriverStationJar(File projectRoot) throws IOException {
        System.out.println("[DriverStation] Building DriverStationWindow fat JAR (this happens once)...");

        File gradlew = new File(projectRoot, isWindows() ? "gradlew.bat" : "gradlew");
        Process buildProcess = new ProcessBuilder(
                gradlew.getAbsolutePath(),
                ":DriverStationClient:shadowJar"
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

        int exitCode = 0;
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

    private static File findProjectRoot() throws IOException {
        File dir = new File(System.getProperty("user.dir")).getAbsoluteFile();

        while (dir != null) {

            File gradlew = new File(dir, isWindows() ? "gradlew.bat" : "gradlew");
            File settings = new File(dir, "settings.gradle");

            if (gradlew.exists() && settings.exists()) {
                return dir;
            }

            dir = dir.getParentFile();
        }

        throw new IOException("Could not locate project root.");
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    private static void log(String msg) {
        System.out.println("[DriverStation] " + msg);
    }
}