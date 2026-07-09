package org.jjophoven.simulator;

import android.os.Build;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.Gamepad;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.jjophoven.driverstation.packets.*;
import org.jjophoven.simhardware.SimHardwareMap;
import org.jjophoven.simhardware.devices.SimTelemetry;
import org.jjophoven.input.Keybinds;
import org.jjophoven.simhardware.drivetrain.MotionVector;
import org.psilynx.psikit.core.Logger;
import org.psilynx.psikit.ftc.FtcLoggingSession;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

// TODO simulate rolling and colliding game pieces,
// TODO intake and shoot game pieces (low priority)
// TODO modify ascope state for less user setup
// add default code blooded robot models?
// automatically download and open ascope on run?
public class DriverStationSimulator {
    private static final int PORT = 8080;

    private ServerSocket listener;
    private Socket clientSocket;
    private DataInputStream in;
    private DataOutputStream out;
    private Process driverStationWindow;

    public boolean clientConnected = false;

    public OpModeState state = OpModeState.WAIT_FOR_INIT;

    SimHardwareMap simHardwareMap;
    Keybinds gamepad1Keybinds;
    Keybinds gamepad2Keybinds;
    OpModeRegister opModeRegister = new OpModeRegister();
    SimConfig config;
    FtcLoggingSession psiKit;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public DriverStationSimulator(SimConfig config) throws IOException, InterruptedException {
        this.gamepad1Keybinds = config.gamepad1Keybinds;
        this.gamepad2Keybinds = config.gamepad2Keybinds;
        this.config = config;
        this.simHardwareMap = this.config.simHardwareMap;

        startServer();
        acceptClient();

        while (driverStationWindow.isAlive()) {
            OpMode selectedOpMode = waitForOpModeInit();
            if (selectedOpMode == null) {
                break;
            }
            runOpMode(selectedOpMode);
        }
    }

    public void runOpMode(OpMode opMode) throws InterruptedException {
        opMode.telemetry = new SimTelemetry(this);
        opMode.hardwareMap = simHardwareMap;
        opMode.gamepad1 = new Gamepad();
        opMode.gamepad2 = new Gamepad();

        psiKit = new FtcLoggingSession();
        psiKit.start(opMode, 5800, "", true, "sim-logs", null, opMode);

        long start = System.nanoTime();
        Logger.setTimeSource(() -> (System.nanoTime() - start) * 1e-9);

        opMode.init();

        while (state == OpModeState.INITIALIZING) {
            wrap(opMode::init_loop, opMode, psiKit);
        }

        opMode.start();

        while (state == OpModeState.RUNNING) {
            psiKit.logOncePerLoop(opMode);
            wrap(opMode::loop, opMode, psiKit);
        }

        opMode.stop();
        Logger.end();
    }

    public @Nullable OpMode waitForOpModeInit() throws IOException, InterruptedException {
        OpMode selectedOpMode = null;

        while (true) {
            while (in.available() > 0) {
                switch (in.readByte()) {
                    case Packet.STATE:
                        state = OpModeState.read(in);

                        if (state == null) {
                            close();
                            return null;
                        }

                        if (state == OpModeState.INITIALIZING) {
                            return selectedOpMode;
                        }
                        break;

                    case Packet.OPMODE:
                        OpModePacket packet = OpModePacket.read(in);
                        selectedOpMode = opModeRegister.getOpMode(packet);
                        break;
                }
            }

            if (!driverStationWindow.isAlive()) {
                return null;
            }

            Thread.sleep(config.loopTimeMs);
        }
    }

    public void wrap(Runnable runnable, OpMode opMode, FtcLoggingSession loggingSession) throws InterruptedException {
        Gamepads gamepads = updateDSInput();
        if (gamepads != null) {
            opMode.gamepad1.fromByteArray(gamepads.gamepad1);
            opMode.gamepad2.fromByteArray(gamepads.gamepad2);
        }
        updateHardware();

        Logger.periodicBeforeUser();
        loggingSession.logOncePerLoop(opMode);

        runnable.run();

        Logger.periodicAfterUser(0,0);
        Thread.sleep(config.loopTimeMs);
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

    private long previousTime = 0;
    public MotionVector previousLegalPose = new MotionVector(141.5/2, 141.5/2, 0);

    public void updateHardware() {
        long currentTime = System.nanoTime();
        double deltaTime = (currentTime - previousTime) * 1e-9;
        simHardwareMap.update(deltaTime);
        previousTime = currentTime;

        Pose2D pose = simHardwareMap.getDrivetrain().getActualPose();
        RobotGeometry robot = config.robotGeometry;
        MotionVector currentPose = MotionVector.fromPose2D(pose);
        boolean isOutOfBounds = FieldBoundary.isOutOfBounds(currentPose, robot);

        if (isOutOfBounds) {
            MotionVector closest = FieldBoundary.closestInBoundsPosition(previousLegalPose, currentPose, robot);

            MotionVector correctionDir = currentPose.minus(closest);

            if (correctionDir.magnitude() > 1e-6) {

                MotionVector normal = correctionDir.unitVector();

                MotionVector velocity = simHardwareMap.getDrivetrain().velocity;

                double vOut = velocity.dot(normal);

                MotionVector correctedVelocity = velocity.minus(normal.scale(vOut));

                simHardwareMap.getDrivetrain().setPosition(closest);
                simHardwareMap.getDrivetrain().setLinearVel(correctedVelocity);
            }
        }
        else {
            previousLegalPose = currentPose;
        }

        Logger.recordOutput("isInBounds", !isOutOfBounds);
        previousLegalPose.log("previousLegalPose");

    }

    static public class Gamepads {
        byte[] gamepad1;
        byte[] gamepad2;
    }

    public Gamepads updateDSInput() {
        if (!driverStationWindow.isAlive()) {
            close();
            return null;
        }

        Gamepads gamepads = null;

        try {
            while (in.available() > 0) {
                switch (in.readByte()) {
                    case Packet.KEY:
                        KeyPacket keyPacket = KeyPacket.read(in);

                        System.out.println("KEY: " + keyPacket.keyCode + ", " + (keyPacket.down ? "pressed" : "released"));

                        if (keyPacket.down) heldKeys.add(keyPacket.keyCode);
                        else heldKeys.remove(keyPacket.keyCode);

                        // Note: edge detection does not work over multiple keys
                        gamepads = new Gamepads();
                        gamepads.gamepad1 = gamepad1Keybinds.getByteArray(heldKeys);
                        gamepads.gamepad2 = gamepad2Keybinds.getByteArray(heldKeys);

                        break;
                    case Packet.CONTROLLER:
                        ByteArrayOutputStream gPadData = new ByteArrayOutputStream();
                        gamepads = new Gamepads();
                        if (in.readByte() == 0) {
                            for (int i = 0; i < 65; i++) {
                                gPadData.write(in.readByte());
                            }
                            gamepads.gamepad1 = gPadData.toByteArray();
                            if (gamepads.gamepad2 == null) {
                                gamepads.gamepad2 = new Gamepad().toByteArray();
                            }
                        } else if (in.readByte() == 1) {
                            for (int i = 0; i < 65; i++) {
                                gPadData.write(in.readByte());
                            }
                            gamepads.gamepad2 = gPadData.toByteArray();
                            if (gamepads.gamepad1 == null) {
                                gamepads.gamepad1 = new Gamepad().toByteArray();
                            }
                        }
                    case Packet.STATE:
                        this.state = OpModeState.read(in);
                        break;
                    case Packet.OPMODE:
                        System.out.println("RECEIVED OPMODE");
                        OpModePacket packet = OpModePacket.read(in);
//                        opMode = opModeRegister.getOpMode(packet);
//
//                        opMode.telemetry = new SimTelemetry(this);
//
//                        opMode.hardwareMap = simHardwareMap;
//                        opMode.gamepad1 = new Gamepad();
//                        opMode.gamepad2 = new Gamepad();

                        break;
                }
            }


        } catch (IOException e) {
            log("Error updating Fake DS" + e);
            close();
        }

        return gamepads;
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
//        if (opMode != null) {
//            opMode.stop();
//        }

        log("Driver Station shutdown.");
    }

    private static Process startDriverStationProcess() throws IOException {
        File projectRoot = findProjectRoot();
        File driverStationJar = new File(projectRoot,
                "DriverStationWindow/build/libs/DriverStationWindow.jar");

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
     * Build the DriverStationWindow JAR using Gradle (one-time operation).
     * This is only called if the JAR doesn't already exist.
     */
    private static void buildDriverStationJar(File projectRoot) throws IOException {
        System.out.println("[DriverStation] Building DriverStationWindow JAR (this happens once)...");

        File gradlew = new File(projectRoot, isWindows() ? "gradlew.bat" : "gradlew");
        Process buildProcess = new ProcessBuilder(
                gradlew.getAbsolutePath(),
                ":DriverStationWindow:build"
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