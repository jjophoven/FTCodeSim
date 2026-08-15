package org.codeblooded.ftcodesim.driverstation.client.ui.components;

import org.codeblooded.ftcodesim.driverstation.client.DSClientModel;
import org.codeblooded.ftcodesim.driverstation.packets.InitOpModePacket;

import javax.swing.*;
import java.awt.*;
import java.util.Vector;

public class OpModeSelector extends JPanel {

    private final DefaultComboBoxModel<InitOpModePacket> comboBoxModel;
    private final JComboBox<InitOpModePacket> opModeComboBox;

    public OpModeSelector() {
        super(new BorderLayout());

        this.comboBoxModel = new DefaultComboBoxModel<>();
        this.opModeComboBox = new JComboBox<>(comboBoxModel);

        super.setMinimumSize(new Dimension(100, 20));

        super.add(this.opModeComboBox, BorderLayout.CENTER);
    }

    public void acceptClient(DSClientModel client) {
        this.opModeComboBox.addActionListener(evt -> client.setSelectedOpMode(OpModeSelector.this.opModeComboBox.getSelectedIndex()));
    }

    public void updateAvailableOpModes(Vector<InitOpModePacket> availableOpModes){
        this.comboBoxModel.removeAllElements();
        availableOpModes.forEach(this.comboBoxModel::addElement);
        this.comboBoxModel.setSelectedItem(this.comboBoxModel.getElementAt(0));
    }

    public JComboBox<InitOpModePacket> getOpModeComboBox() {
        return opModeComboBox;
    }
}
