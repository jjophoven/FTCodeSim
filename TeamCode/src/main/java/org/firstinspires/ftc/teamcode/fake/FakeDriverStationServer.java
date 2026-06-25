package org.firstinspires.ftc.teamcode.fake;

import com.qualcomm.robotcore.hardware.Gamepad;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class FakeDriverStationServer {
    private static final int PORT = 8080;
    private static final int SOCKET_TIMEOUT_MS = 30000;

    private ServerSocket listener;
    private Socket clientSocket;
    private DataInputStream in;
    private DataOutputStream out;
    private Process process;

    public Gamepad gamepad1 = new Gamepad();

    public boolean running = false;
    public boolean clientConnected = false;

    public OpModeState state = OpModeState.WAIT_FOR_INIT;

    public FakeDriverStationServer() {}

    public void startServer() throws IOException {
        log("Starting Driver Station on port " + PORT);

        listener = new ServerSocket(PORT);
        listener.setSoTimeout(SOCKET_TIMEOUT_MS);

        process = startDriverStationProcess();

        running = true;
        clientConnected = false;
    }

    public void acceptClient()  {
        log("Connecting to Fake Driver Station on port " + PORT);

        if (!clientConnected) {
            try {
                clientSocket = listener.accept();

                in = new DataInputStream(clientSocket.getInputStream());
                out = new DataOutputStream(clientSocket.getOutputStream());

                clientConnected = true;

                log("Driver Station connected.");

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

    public void poll() {
        if (!running || !clientConnected) {
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

                        gamepad1.dpad_up    = heldKeys.contains(87);
                        gamepad1.dpad_down  = heldKeys.contains(83);
                        gamepad1.dpad_left  = heldKeys.contains(65);
                        gamepad1.dpad_right = heldKeys.contains(68);
                        gamepad1.a          = heldKeys.contains(10);

                        if (heldKeys.contains(81) && heldKeys.contains(69)) {
                            gamepad1.right_stick_x = 0; // both cancel out
                        } else if (heldKeys.contains(81)) {
                            gamepad1.right_stick_x = -1;
                        } else if (heldKeys.contains(69)) {
                            gamepad1.right_stick_x = 1;
                        } else {
                            gamepad1.right_stick_x = 0;
                        }

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
        }
    }

    public void close() {
        running = false;
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

        log("Driver Station shutdown.");
    }

    private static Process startDriverStationProcess() throws IOException {
        File projectRoot = findProjectRoot();
        File gradlew = new File(projectRoot, isWindows() ? "gradlew.bat" : "gradlew");

        Process process = new ProcessBuilder(
                gradlew.getAbsolutePath(),
                ":DriverStationClient:run"
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