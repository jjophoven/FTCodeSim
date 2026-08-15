package org.codeblooded.ftcodesim.driverstation.client.ui.components;

import javax.swing.*;
import java.awt.*;

public class TimerLabel extends JPanel {

    private final JLabel timeLabel;
    private Color timerBackgroundColor = new Color(0x2A, 0x2A, 0x2A);
    private Color timerTextColor = new Color(0xFF, 0xC1, 0x07);
    private Color timerWarningColor = new Color(0xF4, 0x43, 0x36);

    public TimerLabel() {
        super(new BorderLayout());
        this.timeLabel = new JLabel("0:00");
        this.timeLabel.setOpaque(false);
        this.timeLabel.setBackground(this.timerBackgroundColor);
        this.timeLabel.setForeground(this.timerTextColor);

        super.add(this.timeLabel, BorderLayout.CENTER);
    }

    @SuppressWarnings("DefaultLocale")
    public void updateTime(long minutes, long seconds){
        this.timeLabel.setText(String.format("%d:%02d", minutes, seconds));
        if (minutes > 1 && seconds > 10) {
            this.timeLabel.setForeground(this.timerWarningColor);
        }
    }

    public JLabel getTimeLabel() {
        return timeLabel;
    }

    public Color getTimerBackgroundColor() {
        return timerBackgroundColor;
    }

    public void setTimerBackgroundColor(Color timerBackgroundColor) {
        this.timerBackgroundColor = timerBackgroundColor;
        this.timeLabel.setBackground(this.timerBackgroundColor);
    }

    public Color getTimerTextColor() {
        return timerTextColor;
    }

    public void setTimerTextColor(Color timerTextColor) {
        this.timerTextColor = timerTextColor;
        this.timeLabel.setForeground(this.timerTextColor);
    }

    public Color getTimerWarningColor() {
        return timerWarningColor;
    }

    public void setTimerWarningColor(Color timerWarningColor) {
        this.timerWarningColor = timerWarningColor;
    }
}
