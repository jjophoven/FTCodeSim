package org.jjophoven.input;

import com.qualcomm.robotcore.hardware.Gamepad;

public class ExampleCustomKeyBinds extends DefaultKeybinds {
    @Override
    public Gamepad keybinds(Gamepad keybinds) {
        keybinds.right_stick_x = joystick(Keys.RIGHT, Keys.LEFT);
        keybinds.right_stick_y = joystick(Keys.UP, Keys.DOWN, true);
        keybinds.right_stick_button = button(Keys.SHIFT);

        keybinds.right_bumper = button(Keys.CONTROL);
        keybinds.right_trigger = trigger(Keys.NUMPAD_0);

        keybinds.left_stick_x = joystick(Keys.D, Keys.A);
        keybinds.left_stick_y = joystick(Keys.W, Keys.S, true);
        keybinds.left_stick_button = button(Keys.DIGIT_2);

        keybinds.left_bumper = button(Keys.E);
        keybinds.left_trigger = trigger(Keys.Q);

        keybinds.dpad_up = button(Keys.I);
        keybinds.dpad_down = button(Keys.K);
        keybinds.dpad_left = button(Keys.J);
        keybinds.dpad_right = button(Keys.L);

        return keybinds;
    }
}
