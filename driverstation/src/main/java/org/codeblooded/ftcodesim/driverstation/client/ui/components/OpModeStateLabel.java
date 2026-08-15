package org.codeblooded.ftcodesim.driverstation.client.ui.components;

import org.codeblooded.ftcodesim.driverstation.OpModeState;

import javax.swing.*;
import java.awt.*;

public class OpModeStateLabel extends JPanel {

    private Color initBackgroundColor = new Color(0x21, 0x96, 0xF3);
    private Color initTextColor = new Color(0x2A, 0x2A, 0x2A);
    private Color runningBackgroundColor = new Color(0x4C, 0xAF, 0x50);
    private Color runningTextColor = new Color(0x2A, 0x2A, 0x2A);
    private Color stoppedBackgroundColor = new Color(0xF4, 0x43, 0x36);
    private Color stoppedTextColor = new Color(0x2A, 0x2A, 0x2A);

    private final JLabel label;
    public OpModeStateLabel() {
        super();

        this.label = new RoundedLabel("STOPPED");
        this.label.setMinimumSize(new Dimension(75, 20));
        this.label.setPreferredSize(this.label.getMinimumSize());
        this.label.setOpaque(false);
        this.label.setHorizontalAlignment(SwingConstants.CENTER);
        this.changeIndicatorState(OpModeState.WAIT_FOR_INIT);

        super.setOpaque(true);
        super.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        super.add(Box.createHorizontalGlue());
        super.add(this.label);
        super.add(Box.createHorizontalGlue());
    }

    public void changeIndicatorState(OpModeState opModeState) {
        switch (opModeState) {
            case WAIT_FOR_INIT:
                super.setBackground(this.stoppedBackgroundColor);
                this.label.setForeground(this.stoppedTextColor);
                this.label.setText("STOPPED");
                break;
            case INITIALIZING:
                super.setBackground(this.initBackgroundColor);
                this.label.setForeground(this.initTextColor);
                this.label.setText("INIT");
                break;
            case RUNNING:
                super.setBackground(this.runningBackgroundColor);
                this.label.setForeground(this.runningTextColor);
                this.label.setText("RUNNING");
                break;
        }
    }

    public JLabel getLabel() {
        return label;
    }

    public Color getInitBackgroundColor() {
        return initBackgroundColor;
    }

    public void setInitBackgroundColor(Color initBackgroundColor) {
        this.initBackgroundColor = initBackgroundColor;
    }

    public Color getInitTextColor() {
        return initTextColor;
    }

    public void setInitTextColor(Color initTextColor) {
        this.initTextColor = initTextColor;
    }

    public Color getRunningBackgroundColor() {
        return runningBackgroundColor;
    }

    public void setRunningBackgroundColor(Color runningBackgroundColor) {
        this.runningBackgroundColor = runningBackgroundColor;
    }

    public Color getRunningTextColor() {
        return runningTextColor;
    }

    public void setRunningTextColor(Color runningTextColor) {
        this.runningTextColor = runningTextColor;
    }

    public Color getStoppedBackgroundColor() {
        return stoppedBackgroundColor;
    }

    public void setStoppedBackgroundColor(Color stoppedBackgroundColor) {
        this.stoppedBackgroundColor = stoppedBackgroundColor;
    }

    public Color getStoppedTextColor() {
        return stoppedTextColor;
    }

    public void setStoppedTextColor(Color stoppedTextColor) {
        this.stoppedTextColor = stoppedTextColor;
    }
}
