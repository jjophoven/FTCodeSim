package org.jjophoven.simulator;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.Gamepad;
import org.jjophoven.driverstation.OpModeState;
import org.jjophoven.fakehardware.FakeHardwareMap;
import org.jjophoven.fakehardware.FakeTelemetry;
import org.jjophoven.input.Keybinds;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class DriverStation {
    private static final int PORT = 8080;
    private static final int SOCKET_TIMEOUT_MS = 30000;

    private ServerSocket listener;
    private Socket clientSocket;
    private DataInputStream in;
    private DataOutputStream out;
    private Process process;

    public boolean clientConnected = false;

    public OpModeState state = OpModeState.WAIT_FOR_INIT;

    OpMode opMode;
    FakeHardwareMap fakeHardwareMap;
    Keybinds gamepad1Keybinds;
    Keybinds gamepad2Keybinds;

    public DriverStation(OpMode opMode, Keybinds gamepad1, Keybinds gamepad2) {
        opMode.telemetry = new FakeTelemetry(this);

        this.opMode = opMode;

        fakeHardwareMap = new FakeHardwareMap();
        opMode.hardwareMap = fakeHardwareMap;
        this.gamepad1Keybinds = gamepad1;
        this.gamepad2Keybinds = gamepad2;
        opMode.gamepad1 = new Gamepad();
        opMode.gamepad2 = new Gamepad();
    }

    public void startServer() throws IOException {
        log("Connecting to Fake Driver Station on port " + PORT);

        listener = new ServerSocket(PORT);
        listener.setSoTimeout(SOCKET_TIMEOUT_MS);

        process = startDriverStationProcess();

        clientConnected = false;
    }

    public void acceptClient()  {
        if (!clientConnected) {
            try {
                clientSocket = listener.accept();

                in = new DataInputStream(clientSocket.getInputStream());
                out = new DataOutputStream(clientSocket.getOutputStream());

                clientConnected = true;

                log("Fake Driver Station connected on port " + PORT);

            } catch (IOException e) {
                log("Error connecting to Fake DS" + e);
                close();
            }
        }
    }

    public static final byte INPUT_TELEMETRY = 1;
    public static final byte KEY_PACKET = 1;
    public static final byte STATE_PACKET = 2;

    private final Set<Integer> heldKeys = new HashSet<>();

    public void update() {
        poll();
        fakeHardwareMap.updateHardware();
    }

    public void poll() {
        if (!process.isAlive()) {
            close();
            return;
        }

        try {
            while (in.available() > 0) {
                switch (in.readByte()) {
                    case KEY_PACKET:
                        int keyCode = in.readInt();
                        boolean pressed = in.readBoolean();

                        System.out.println("KEY: " + keyCode + ", " + (pressed ? "pressed" : "released"));

                        if (pressed) heldKeys.add(keyCode);
                        else heldKeys.remove(keyCode);

                        gamepad1Keybinds.apply(opMode.gamepad1, heldKeys);
                        gamepad2Keybinds.apply(opMode.gamepad2, heldKeys);

                        break;

                    case STATE_PACKET:
                        this.state = OpModeState.values()[in.readByte()];
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
            out.writeByte(INPUT_TELEMETRY);
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

        if (process != null) {
            process.destroy();
        }

        state = OpModeState.STOPPED;

        log("Driver Station shutdown.");
    }

    private static Process startDriverStationProcess() throws IOException {
        File projectRoot = findProjectRoot();
        File gradlew = new File(projectRoot, isWindows() ? "gradlew.bat" : "gradlew");

        Process process = new ProcessBuilder(
                gradlew.getAbsolutePath(),
                "runSimulator"
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