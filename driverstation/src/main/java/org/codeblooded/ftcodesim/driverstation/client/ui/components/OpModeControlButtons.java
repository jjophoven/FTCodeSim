package org.codeblooded.ftcodesim.driverstation.client.ui.components;


import org.codeblooded.ftcodesim.driverstation.OpModeState;
import org.codeblooded.ftcodesim.driverstation.client.DSClientModel;

import javax.swing.*;
import java.awt.*;

public class OpModeControlButtons extends JPanel {

    private final RoundedButton mainButton;
    private final RoundedButton stopButton;

    private Color mainButtonColor = new Color(0x4C, 0xAF, 0x50);
    private Color mainButtonTextColor = Color.WHITE;
    private Color stopButtonColor = new Color(0xF4, 0x43, 0x36);
    private Color stopButtonTextColor = Color.WHITE;

    public OpModeControlButtons() {
        super(new GridLayout(1, 2, 5, 5));

        this.mainButton = new RoundedButton("INIT");
        this.mainButton.setBackground(this.mainButtonColor);
        this.mainButton.setForeground(this.mainButtonTextColor);
        this.mainButton.setPreferredSize(new Dimension(100, 20));
        this.mainButton.setMinimumSize(this.mainButton.getPreferredSize());

        this.stopButton = new RoundedButton("STOP");
        this.stopButton.setBackground(this.stopButtonColor);
        this.stopButton.setForeground(this.stopButtonTextColor);
        this.stopButton.setPreferredSize(new Dimension(100, 20));
        this.stopButton.setMinimumSize(this.stopButton.getPreferredSize());

        this.onOpModeStateChanged(OpModeState.WAIT_FOR_INIT);

        super.add(this.mainButton);
        super.add(this.stopButton);
    }

    public void acceptClient(DSClientModel client) {
        this.mainButton.addActionListener(evt -> {
            switch (client.getOpModeState()){
                case WAIT_FOR_INIT:
                    client.transitionOpModeState(OpModeState.INITIALIZING);
                    break;
                case INITIALIZING:
                    client.transitionOpModeState(OpModeState.RUNNING);
                    break;
            }
        });
        this.stopButton.addActionListener(evt -> client.transitionOpModeState(OpModeState.WAIT_FOR_INIT));
    }

    public void onOpModeStateChanged(OpModeState state) {
        switch (state) {
            case WAIT_FOR_INIT:
                this.mainButton.setText("INIT");
                this.mainButton.setEnabled(true);

                this.stopButton.setEnabled(false);
                break;
            case INITIALIZING:
                this.mainButton.setText("START");
                this.mainButton.setEnabled(true);

                this.stopButton.setEnabled(true);
                break;
            case RUNNING:
                this.mainButton.setText("RUNNING");
                this.mainButton.setEnabled(false);

                this.stopButton.setEnabled(true);
                break;
        }

        this.mainButton.repaint();
        this.stopButton.repaint();
    }

    public RoundedButton getMainButton() {
        return mainButton;
    }

    public RoundedButton getStopButton() {
        return stopButton;
    }

    public Color getMainButtonColor() {
        return mainButtonColor;
    }

    public void setMainButtonColor(Color mainButtonColor) {
        this.mainButtonColor = mainButtonColor;
        this.mainButton.setBackground(this.mainButtonColor);
    }

    public Color getMainButtonTextColor() {
        return mainButtonTextColor;
    }

    public void setMainButtonTextColor(Color mainButtonTextColor) {
        this.mainButtonTextColor = mainButtonTextColor;
        this.mainButton.setForeground(this.mainButtonTextColor);
    }

    public Color getStopButtonColor() {
        return stopButtonColor;
    }

    public void setStopButtonColor(Color stopButtonColor) {
        this.stopButtonColor = stopButtonColor;
        this.stopButton.setBackground(this.stopButtonColor);
    }

    public Color getStopButtonTextColor() {
        return stopButtonTextColor;
    }

    public void setStopButtonTextColor(Color stopButtonTextColor) {
        this.stopButtonTextColor = stopButtonTextColor;
        this.stopButton.setForeground(this.stopButtonTextColor);
    }
}
