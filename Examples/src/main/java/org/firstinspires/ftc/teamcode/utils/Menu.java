package org.firstinspires.ftc.teamcode.utils;

import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.util.ElapsedTime;

import java.util.ArrayList;
import java.util.List;

public class Menu {
    public interface OptionAction {
        void run();
    }

    static class Haptics {
        public static int CONFIRM = 100;
        public static int LIGHT_TICK = 30;
    }

    public static class Option {
        private final String name;
        private final OptionAction confirm;
        private final OptionAction unconfirm;
        
        public Option(String name, OptionAction action, OptionAction onExit) {
            this.name = name;
            this.confirm = action;
            this.unconfirm = onExit;
        }
        
        public String getName() {
            return name;
        }
        
        public void run() {
            confirm.run();
        }
        
        public void exit() {
            unconfirm.run();
        }
    }
    
    private final List<Option> options = new ArrayList<>();
    private int selectedIndex = 0;
    private int confirmedIndex = -1;
    private boolean confirmed = false;
    
    private final Gamepad gamepad;
    private final ElapsedTime debounceTimer = new ElapsedTime();
    
    private Menu currentSubmenu = null;
    private final String title;
    
    public Menu(Gamepad gamepad, String title) {
        this.gamepad = gamepad;
        this.title = title;
    }

    public void addOption(String name, OptionAction confirm) {
        options.add(new Option(name, confirm, () -> {}));
    }

    public void addOption(String name, OptionAction confirm, OptionAction unconfirm) {
        options.add(new Option(name, confirm, unconfirm));
    }
    
    public void confirmOption(int index) {
        confirmedIndex = index;
        selectedIndex = index;
        confirmed = true;
    }
    
    public void addSubmenu(String name, Menu submenu) {
        addOption(name, () -> {
                      this.currentSubmenu = submenu;
                      unconfirmOption();
                  }, () -> {});
    }
    
    public void unconfirmOption() {
        confirmed = false;
        confirmedIndex = -1;
    }
    
    public void update() {
        if (currentSubmenu != null) {
            currentSubmenu.update();
            if (gamepad.b && debounceTimer.milliseconds() > 300) {
                currentSubmenu = null;
                if (confirmed) {
                    getSelectedOption().exit();
                }
                gamepad.rumble(Haptics.CONFIRM);
                debounceTimer.reset();
            }
            return;
        }
        
        if (confirmed) {
            getSelectedOption().run();
        }
        
        if (debounceTimer.milliseconds() > 300) {
            if (gamepad.dpad_down || -gamepad.left_stick_y < -0.2) {
                selectedIndex = (selectedIndex + 1) % options.size();
                gamepad.rumble(Haptics.LIGHT_TICK);
                debounceTimer.reset();
            } else if (gamepad.dpad_up || -gamepad.left_stick_y > 0.2) {
                selectedIndex = (selectedIndex - 1 + options.size()) % options.size();
                gamepad.rumble(Haptics.LIGHT_TICK);
                debounceTimer.reset();
            }
        }
        
        if (gamepad.aWasPressed() && !gamepad.options) {
            if (confirmed) {
                getSelectedOption().exit();
            }
            confirmed = true;
            confirmedIndex = selectedIndex;
            gamepad.rumble(Haptics.CONFIRM);
            debounceTimer.reset();
        }
    }
    
    public Option getSelectedOption() {
        return options.get(confirmedIndex);
    }

    public String getDisplay() {
        if (currentSubmenu != null) {
            return currentSubmenu.getDisplay() + "\n(B to go back)";
        }

        StringBuilder display = new StringBuilder(title + "\n");
        for (int i = 0; i < options.size(); i++) {
            if (i == confirmedIndex && i == selectedIndex) {
                display.append("> [x] ");
            } else if (i == confirmedIndex) {
                display.append("  [x] ");
            } else if (i == selectedIndex) {
                display.append("> [ ] ");
            } else {
                display.append("  [ ] ");
            }
            display.append(options.get(i).getName()).append("\n");
        }
        return display.toString();
    }
}
