package org.codeblooded.ftcodesim.driverstation.client;

import org.codeblooded.ftcodesim.driverstation.DriverStationConnection;
import org.codeblooded.ftcodesim.driverstation.OpModeState;
import org.codeblooded.ftcodesim.driverstation.packets.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Vector;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class DSClientModel implements KeyEventDispatcher{

    private final DriverStationConnection dsConnection;
    private boolean connected;
    private final Vector<InitOpModePacket> availableOpModes;
    private int selectedOpMode;
    private volatile OpModeState opModeState;
    private String telemetry;
    private final Timer timer;

    // external listeners
    private final Consumer<Boolean> connectionListener;
    private final Consumer<Vector<InitOpModePacket>> opModeListListener;
    private final Consumer<OpModeState> opModeStateListener;
    private final Consumer<String> telemetryListener;
    private final BiConsumer<Long, Long> timerListener;
    private final Consumer<Integer> keyPressedListener;
    private final Consumer<Integer> keyReleasedListener;

    public DSClientModel(int port, Consumer<Boolean> connectionListener, Consumer<Vector<InitOpModePacket>> opModeListListener, Consumer<OpModeState> opModeStateListener, Consumer<String> telemetryListener, BiConsumer<Long, Long> timerListener, Consumer<Integer> keyPressedListener, Consumer<Integer> keyReleasedListener) {
        this.connectionListener = connectionListener;
        this.opModeListListener = opModeListListener;
        this.opModeStateListener = opModeStateListener;
        this.telemetryListener = telemetryListener;
        this.timerListener = timerListener;
        this.keyPressedListener = keyPressedListener;
        this.keyReleasedListener = keyReleasedListener;

        this.connected = false;
        this.availableOpModes = new Vector<>();
        this.selectedOpMode = -1;
        this.opModeState = OpModeState.WAIT_FOR_INIT;
        this.timer = new Timer(500, this::updateTimer);

        this.dsConnection = new DriverStationConnection(port,
                this::onTelemetryPacketReceive,
                this::onOpModeListReceive,
                this::onConnection,
                this::onDisconnect);
    }

    private void onConnection() {
        this.connected = true;
        this.connectionListener.accept(true);
    }

    private void onDisconnect() {
        this.connected = false;
        this.connectionListener.accept(false);
    }

    private void onTelemetryPacketReceive(TelemetryPacket telemetryPacket) {
        this.telemetry = telemetryPacket.telemetry;
        this.telemetryListener.accept(this.telemetry);
    }

    private void onOpModeListReceive(OpModesPacket opModesPacket) {
        this.availableOpModes.addAll(opModesPacket.opmodes);
        this.opModeListListener.accept(this.availableOpModes);
    }

    private long timerStartMs;
    public void startTimer(){
        this.timerStartMs = System.currentTimeMillis();
        this.timer.restart();
    }

    private void updateTimer(ActionEvent evt) {
        long elapsed = (System.currentTimeMillis() - timerStartMs) / 1000;
        long mins = elapsed / 60;
        long secs = elapsed % 60;

        this.timerListener.accept(mins, secs);
    }

    private void stopTimer(){
        this.timer.stop();
        this.timerListener.accept(0L, 0L);
    }

    private final HashSet<Integer> pressedKeys = new HashSet<>();

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        if (e.isConsumed()) return false;

        int keycode = e.getKeyCode();
        int keyActionType = e.getID();

        if (keyActionType == KeyEvent.KEY_PRESSED){
            if (this.pressedKeys.add(keycode)){
                this.dsConnection.send(new KeyPacket(keycode, true));
                this.keyPressedListener.accept(keycode);
            }
        } else if (keyActionType == KeyEvent.KEY_RELEASED){
            if (this.pressedKeys.remove(keycode)){
                this.dsConnection.send(new KeyPacket(keycode, false));
                this.keyPressedListener.accept(keycode);
            }
        }

        return false;
    }

    public void transitionOpModeState(OpModeState opModeState) {
        this.opModeState = opModeState;
        switch (opModeState) {
            case INITIALIZING:
                if (!this.availableOpModes.isEmpty() && this.selectedOpMode > -1){
                    this.dsConnection.send(this.availableOpModes.get(this.selectedOpMode));
                }
                break;
            case WAIT_FOR_INIT:
                this.stopTimer();
                this.dsConnection.send(OpModeCommandPacket.STOP);
                break;
            case RUNNING:
                this.startTimer();
                this.dsConnection.send(OpModeCommandPacket.START);
                break;
        }


        this.opModeStateListener.accept(this.opModeState);
    }

    public void dispose(){
        this.dsConnection.close();
    }

    public DriverStationConnection getDsConnection() {
        return dsConnection;
    }

    public boolean isConnected() {
        return connected;
    }

    public Vector<InitOpModePacket> getAvailableOpModes() {
        return availableOpModes;
    }

    public int getSelectedOpMode() {
        return selectedOpMode;
    }

    public void setSelectedOpMode(int selectedOpMode) {
        this.selectedOpMode = selectedOpMode;
    }

    public OpModeState getOpModeState() {
        return opModeState;
    }

    public String getTelemetry() {
        return telemetry;
    }

    public Timer getTimer() {
        return timer;
    }

    public Consumer<Boolean> getConnectionListener() {
        return connectionListener;
    }

    public Consumer<Vector<InitOpModePacket>> getOpModeListListener() {
        return opModeListListener;
    }

    public Consumer<OpModeState> getOpModeStateListener() {
        return opModeStateListener;
    }

    public Consumer<String> getTelemetryListener() {
        return telemetryListener;
    }

    public BiConsumer<Long, Long> getTimerListener() {
        return timerListener;
    }

    public Consumer<Integer> getKeyPressedListener() {
        return keyPressedListener;
    }

    public Consumer<Integer> getKeyReleasedListener() {
        return keyReleasedListener;
    }

    public long getTimerStartMs() {
        return timerStartMs;
    }

    public HashSet<Integer> getPressedKeys() {
        return pressedKeys;
    }
}
