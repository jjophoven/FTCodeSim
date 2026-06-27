package org.jjophoven.driverstation;

import javax.swing.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class DriverStationConnection {
    public static final byte INPUT_TELEMETRY = 1;
    public static final byte KEY_PACKET = 1;
    public static final byte STATE_PACKET = 2;

    private final Consumer<String> telemetryConsumer;
    private final Runnable connectedCallback;
    private final Runnable disconnectedCallback;

    private DataInputStream input;
    private DataOutputStream output;
    private Socket socket;
    private final AtomicBoolean running = new AtomicBoolean(true);

    public DriverStationConnection(
            int port,
            Consumer<String> telemetryConsumer,
            Runnable connectedCallback,
            Runnable disconnectedCallback
    ) {
        this.telemetryConsumer = telemetryConsumer;
        this.connectedCallback = connectedCallback;
        this.disconnectedCallback = disconnectedCallback;

        new Thread(() -> connect(port)).start();
    }

    private void connect(int port) {
        try {
            socket = new Socket("127.0.0.1", port);
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());

            SwingUtilities.invokeLater(connectedCallback);

            readLoop();
        } catch (IOException e) {
            close();
        }
    }

    private void readLoop() {
        try {
            while (running.get()) {
                byte type = input.readByte();
                if (type == INPUT_TELEMETRY) {
                    String telemetry = input.readUTF();
                    SwingUtilities.invokeLater(() -> telemetryConsumer.accept(telemetry));
                }
            }
        } catch (IOException ignored) {
            close();
        }
    }

    public void sendKey(int keyCode, boolean down) {
        safe(() -> {
            output.writeByte(KEY_PACKET);
            output.writeInt(keyCode);
            output.writeBoolean(down);
            output.flush();
        });
    }

    public void sendState(OpModeState state) {
        safe(() -> {
            output.writeByte(STATE_PACKET);
            output.writeByte(state.ordinal());
            output.flush();
        });
    }

    @FunctionalInterface
    public interface IORunnable {
        void run() throws IOException;
    }

    public void safe(IORunnable run) {
        if (output == null || input == null) {
            System.out.println("ERR Uninitialized client");
            return;
        }

        try {
            run.run();
        } catch (IOException e) {
            System.out.println("Send error: " + e.getMessage());
            close();
        }
    }


    public void close() {
        running.set(false);

        try { if (input != null) input.close(); } catch (IOException ignored) {}
        try { if (output != null) output.close(); } catch (IOException ignored) {}
        try { if (socket != null) socket.close(); } catch (IOException ignored) {}

        SwingUtilities.invokeLater(disconnectedCallback);
    }
}