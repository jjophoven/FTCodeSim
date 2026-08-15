package org.codeblooded.ftcodesim.driverstation.client;

import com.studiohartman.jamepad.ControllerManager;
import com.studiohartman.jamepad.ControllerState;
import org.codeblooded.ftcodesim.driverstation.OpModeState;
import org.codeblooded.ftcodesim.driverstation.packets.ControllerPacket;
import org.codeblooded.ftcodesim.driverstation.packets.KeyPacket;
import org.codeblooded.ftcodesim.driverstation.packets.Packet;

import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.function.Consumer;

public class DSInputManager {

    private final HashSet<Integer> pressedKeys;
    private Consumer<Integer> keyPressListener;
    private Consumer<Integer> keyReleaseListener;

    private final ControllerManager controllerManager;
    private final boolean[] gamepadsConnected;
    private boolean swapGamepads;
    private final BooleanConsumer[] gamepadConnectionListener;

    private final DSClientModel client;
    private Consumer<Packet> packetConsumer;

    public DSInputManager(DSClientModel client, Consumer<Packet> connection) {
        this.pressedKeys = new HashSet<>();
        this.keyPressListener = key -> {};
        this.keyReleaseListener = key -> {};

        this.controllerManager = new ControllerManager();
        this.controllerManager.initSDLGamepad();
        this.gamepadsConnected = new boolean[2];
        this.swapGamepads = false;
        this.gamepadConnectionListener = new BooleanConsumer[2];

        this.client = client;
        this.packetConsumer = connection;
    }

    public DSInputManager(DSClientModel client) {
        this(client, packet -> {});
    }

    public boolean areInputsEnabled(){
        return !this.areInputsDisabled();
    }

    public boolean areInputsDisabled(){
        return this.client.getOpModeState() == null ||  this.client.getOpModeState() == OpModeState.WAIT_FOR_INIT;
    }

    public boolean onKeyEvent(KeyEvent e) {

        if (this.areInputsDisabled()) {
            return false;
        }

        if (e.isConsumed()) return false;

        int keycode = e.getKeyCode();
        int keyActionType = e.getID();

        if (keyActionType == KeyEvent.KEY_PRESSED){
            if (this.pressedKeys.add(keycode)){
                this.packetConsumer.accept(new KeyPacket(keycode, true));
                this.keyPressListener.accept(keycode);
            }
        } else if (keyActionType == KeyEvent.KEY_RELEASED){
            if (this.pressedKeys.remove(keycode)){
                this.packetConsumer.accept(new KeyPacket(keycode, false));
                this.keyReleaseListener.accept(keycode);
            }
        }

        return false;
    }

    public void pollGamepads(){
        if (areInputsEnabled()){
            // poll gamepad states
            ControllerState physicalGamepad1 = this.controllerManager.getState(0);
            ControllerState physicalGamepad2 = this.controllerManager.getState(1);

            byte g1ID = (byte) (this.swapGamepads ? 0b1 : 0b0);
            byte g2ID = (byte) (this.swapGamepads ? 0b0 : 0b1);

            this.updateGamepad(g1ID, physicalGamepad1);
            this.updateGamepad(g2ID, physicalGamepad2);
        }
    }

    private void updateGamepad(byte id, ControllerState gamepad){
        // check if gamepad is connected
        if (gamepad.isConnected){
            if (!this.gamepadsConnected[id]) {
                // update gamepad connection state and report it to the console
                System.out.println("GAMEPAD " + id + " CONNECTED: " +  gamepad.controllerType);
                this.gamepadsConnected[id] = true;
                this.gamepadConnectionListener[id].accept(true);
            }

            // send a controller packet to packet consumer (ideally the DS connection)
            this.packetConsumer.accept(new ControllerPacket(id, gamepad));
        } else {
            // update gamepad connection state
            if (this.gamepadsConnected[id]) {
                this.gamepadsConnected[id] = false;
                this.gamepadConnectionListener[id].accept(false);
            }
        }
    }

    public DSClientModel getClient() {
        return client;
    }

    public Consumer<Packet> getPacketConsumer() {
        return packetConsumer;
    }

    public void setPacketConsumer(Consumer<Packet> packetConsumer) {
        this.packetConsumer = packetConsumer;
    }

    public HashSet<Integer> getPressedKeys() {
        return pressedKeys;
    }

    public Consumer<Integer> getKeyPressListener() {
        return keyPressListener;
    }

    public void setKeyPressListener(Consumer<Integer> keyPressListener) {
        this.keyPressListener = keyPressListener;
    }

    public Consumer<Integer> getKeyReleaseListener() {
        return keyReleaseListener;
    }

    public void setKeyReleaseListener(Consumer<Integer> keyReleaseListener) {
        this.keyReleaseListener = keyReleaseListener;
    }

    public boolean areGamepadsSwapped() {
        return swapGamepads;
    }

    public void setSwapGamepads(boolean swapGamepads) {
        this.swapGamepads = swapGamepads;

        // flash gamepad connection status to false
        Arrays.fill(this.gamepadsConnected, false);
    }

    public boolean isGamepad1Connected() {
        return this.gamepadsConnected[0];
    }

    public boolean isGamepad2Connected() {
        return this.gamepadsConnected[1];
    }

    public void attachGamepad1ConnectionListener(BooleanConsumer listener) {
        this.gamepadConnectionListener[0] = listener;
    }

    public void attachGamepad2ConnectionListener(BooleanConsumer listener) {
        this.gamepadConnectionListener[1] = listener;
    }
}
