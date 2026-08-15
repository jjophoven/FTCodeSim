package org.codeblooded.ftcodesim.driverstation.client.ui;

import org.codeblooded.ftcodesim.driverstation.client.ui.components.*;
import org.codeblooded.ftcodesim.driverstation.client.ui.components.*;

import javax.swing.*;
import java.awt.*;

public class DSComponentStyle {

    public void stylePanel(JPanel panel){
        panel.setBackground(new Color(0x2A, 0x2A, 0x2A));
    }

    public void styleWindow(JPanel windowContentPanel){
        windowContentPanel.setBackground(new Color(0x1A, 0x1A, 0x1A));
    }

    public void styleConnectionLabel(ConnectionLabel connectionLabel){
        connectionLabel.getLabel().setFont(new Font("Dialog", Font.BOLD, 12));
        connectionLabel.getLabel().setOpaque(false);
    }

    public void styleOpModeSelector(OpModeSelector opModeSelector){
        this.stylePanel(opModeSelector);
        opModeSelector.getOpModeComboBox().setFont(new Font("Dialog", Font.BOLD, 18));
    }

    public void styleOpModeControlButtons(OpModeControlButtons opModeControlButtons){
        this.stylePanel(opModeControlButtons);
    }

    public void styleOpModeStateLabel(OpModeStateLabel opModeStateLabel){
        opModeStateLabel.getLabel().setFont(new Font("Dialog", Font.BOLD, 11));
    }

    public void styleTimerLabel(TimerLabel timerLabel){
        timerLabel.getTimeLabel().setFont(new Font("Monospaced", Font.BOLD, 28));
        timerLabel.getTimeLabel().setHorizontalAlignment(SwingConstants.RIGHT);
    }

    public void styleTelemetryArea(TelemetryArea telemetryArea){
        telemetryArea.getTelemetryOutput().setFont(new Font("Monospaced", Font.PLAIN, 12));
    }

}
