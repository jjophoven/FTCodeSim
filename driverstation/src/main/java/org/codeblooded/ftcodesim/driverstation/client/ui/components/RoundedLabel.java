package org.codeblooded.ftcodesim.driverstation.client.ui.components;

import javax.swing.*;
import java.awt.*;

public class RoundedLabel extends JLabel {

    private final int arcDiameter;

    public RoundedLabel(String text, int arcDiameter) {
        super(text);
        this.arcDiameter = arcDiameter;
    }

    public RoundedLabel(String text) {
        this(text, 5);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color bg = super.getBackground();
        g2.setColor(bg);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), this.arcDiameter, this.arcDiameter);
        g2.dispose();
        super.paintComponent(g);
    }
}
