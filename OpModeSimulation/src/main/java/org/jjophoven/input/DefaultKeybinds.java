package org.jjophoven.input;

import com.qualcomm.robotcore.hardware.Gamepad;

import java.util.Set;

// TODO make a keyboard to controller image map
public class DefaultKeybinds extends Keybinds {
    @Override
    public Gamepad keybinds(Gamepad keybinds) {
        keybinds.dpad_up = button(Keys.UP);
        keybinds.dpad_down = button(Keys.DOWN);
        keybinds.dpad_left = button(Keys.LEFT);
        keybinds.dpad_right = button(Keys.RIGHT);

        keybinds.right_stick_x = joystick(Keys.L, Keys.J);
        keybinds.right_stick_y = joystick(Keys.I, Keys.K, true);

        keybinds.left_stick_x = joystick(Keys.D, Keys.A);
        keybinds.left_stick_y = joystick(Keys.W, Keys.S, true);

        keybinds.back = button(Keys.TAB);
        keybinds.start = button(Keys.ENTER);

        keybinds.right_bumper = button(Keys.Q);
        keybinds.left_bumper = button(Keys.E);
        keybinds.right_trigger = trigger(Keys.U);
        keybinds.left_trigger = trigger(Keys.O);

        keybinds.square = button(Keys.P);
        keybinds.circle = button(Keys.OPEN_BRACKET);
        keybinds.triangle = button(Keys.MINUS);
        keybinds.cross = button(Keys.SEMICOLON);

        keybinds.x = button(Keys.P);
        keybinds.y = button(Keys.MINUS);
        keybinds.b = button(Keys.OPEN_BRACKET);
        keybinds.a = button(Keys.SEMICOLON);

        keybinds.left_stick_button = button(Keys.Z);
        keybinds.right_stick_button = button(Keys.COMMA);

        return keybinds;
    }
}
