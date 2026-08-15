package org.codeblooded.ftcodesim.driverstation.client.ui.components;

import javax.swing.*;
import java.awt.*;

public class ConnectionLabel extends JPanel {

    private final JLabel label;

    private Color connectedColor = new Color(0x4C, 0xAF, 0x50);
    private Color disconnectedColor = new Color(0xF4, 0x43, 0x36);

    public ConnectionLabel() {
        super(new BorderLayout());


        this.label = new RoundedLabel("● DISCONNECTED");
        this.label.setMinimumSize(new Dimension(100, 20));

        this.onConnectionStateChange(false);

        super.add(this.label, BorderLayout.CENTER);
    }

    public void onConnectionStateChange(boolean isConnected) {
        if (isConnected) {
            this.label.setText("● CONNECTED");
            this.label.setForeground(this.connectedColor);

        } else {
            this.label.setText("● DISCONNECTED");
            this.label.setForeground(this.disconnectedColor);

        }
    }

    public JLabel getLabel() {
        return label;
    }

    public Color getConnectedColor() {
        return connectedColor;
    }

    public void setConnectedColor(Color connectedColor) {
        this.connectedColor = connectedColor;
    }

    public Color getDisconnectedColor() {
        return disconnectedColor;
    }

    public void setDisconnectedColor(Color disconnectedColor) {
        this.disconnectedColor = disconnectedColor;
    }
}
