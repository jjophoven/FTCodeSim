package org.codeblooded.ftcodesim.driverstation.client.ui.components;

import javax.swing.*;
import java.awt.*;

public class RoundedButton extends JButton {

    private final int arcDiameter;

    public RoundedButton(String text, int arcDiameter) {
        super(text);
        this.arcDiameter = arcDiameter;

        super.setFocusPainted(false);
        super.setBorderPainted(false);
        super.setContentAreaFilled(false);
        super.setOpaque(false);
        super.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public RoundedButton(String text) {
        this(text, 5);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color bg = isEnabled()
                ? (getModel().isPressed() ? getBackground().darker() : getBackground())
                : new Color(0x44, 0x44, 0x44);
        g2.setColor(bg);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), this.arcDiameter, this.arcDiameter);
        g2.dispose();
        super.paintComponent(g);
    }
}
