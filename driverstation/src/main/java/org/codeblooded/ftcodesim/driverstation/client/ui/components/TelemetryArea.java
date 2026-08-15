package org.codeblooded.ftcodesim.driverstation.client.ui.components;

import javax.swing.*;
import java.awt.*;

public class TelemetryArea extends JPanel {

    private final TextArea telemetryArea;
    private final JScrollPane scrollPane;
    private Color telemetryBackgroundColor = new Color(0x12, 0x12, 0x12);
    private Color telemetryTextColor = new Color(0x4C, 0xAF, 0x50);
    public TelemetryArea() {
        super(new BorderLayout(5, 5));

        this.telemetryArea = new TextArea();
        this.telemetryArea.setEditable(false);
        this.telemetryArea.setForeground(this.telemetryTextColor);
        this.telemetryArea.setBackground(this.telemetryBackgroundColor);

        this.scrollPane = new JScrollPane(this.telemetryArea);
        this.scrollPane.getViewport().setBackground(this.telemetryBackgroundColor);

        super.add(scrollPane, BorderLayout.CENTER);
    }

    public void updateTelemetry(String telemetry) {
        this.telemetryArea.setText(telemetry);
    }

    public TextArea getTelemetryOutput() {
        return telemetryArea;
    }

    public Color getTelemetryBackgroundColor() {
        return telemetryBackgroundColor;
    }

    public void setTelemetryBackgroundColor(Color telemetryBackgroundColor) {
        this.telemetryBackgroundColor = telemetryBackgroundColor;
        this.scrollPane.setBackground(this.telemetryBackgroundColor);
        this.telemetryArea.setBackground(this.telemetryBackgroundColor);
    }

    public Color getTelemetryTextColor() {
        return telemetryTextColor;
    }

    public void setTelemetryTextColor(Color telemetryTextColor) {
        this.telemetryTextColor = telemetryTextColor;
        this.telemetryArea.setForeground(this.telemetryTextColor);
    }
}
