package org.jjophoven.input;

import com.qualcomm.robotcore.hardware.Gamepad;

import java.util.Set;

public abstract class Keybinds {
    Set<Integer> keys;

    abstract Gamepad keybinds(Gamepad keybinds);

    public void apply(Gamepad g,  Set<Integer> keys) {
        this.keys = keys;
        Gamepad changes = new Gamepad();
        changes = keybinds(changes);
        g.fromByteArray(changes.toByteArray());
    }
    public float joystick(int positiveKeyCode, int negativeKeyCode) {
        return ((this.keys.contains(positiveKeyCode) ? 1 : 0) - (this.keys.contains(negativeKeyCode) ? 1 : 0));
    }
    public float joystick(int positiveKeyCode, int negativeKeyCode, boolean reverse) {
        int reverseMultiplier = reverse ? -1 : 1;
        return ((this.keys.contains(positiveKeyCode) ? 1 : 0) - (this.keys.contains(negativeKeyCode) ? 1 : 0)) * reverseMultiplier;
    }
    public float trigger(int triggerKeyCode) {
        return this.keys.contains(triggerKeyCode) ? 1 : 0;
    }
    public boolean button(int keyCode) {
        return this.keys.contains(keyCode);
    }
}
