package org.codeblooded.ftcodesim.hardware.devices;

import com.qualcomm.hardware.rev.RevTouchSensor;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.DigitalChannelController;
import com.qualcomm.robotcore.util.SerialNumber;

public class SimTouchSensor extends RevTouchSensor {
    public SimTouchSensor() {
        super(new DigitalChannelController() {
            @Override
            public SerialNumber getSerialNumber() {
                return null;
            }

            @Override
            public DigitalChannel.Mode getDigitalChannelMode(int channel) {
                return null;
            }

            @Override
            public void setDigitalChannelMode(int channel, DigitalChannel.Mode mode) {

            }

            @Override
            public void setDigitalChannelMode(int channel, Mode mode) {

            }

            @Override
            public boolean getDigitalChannelState(int channel) {
                return false;
            }

            @Override
            public void setDigitalChannelState(int channel, boolean state) {

            }

            @Override
            public Manufacturer getManufacturer() {
                return null;
            }

            @Override
            public String getDeviceName() {
                return "";
            }

            @Override
            public String getConnectionInfo() {
                return "";
            }

            @Override
            public int getVersion() {
                return 0;
            }

            @Override
            public void resetDeviceConfigurationForOpMode() {

            }

            @Override
            public void close() {

            }
        }, 0);
    }

    @Override
    public boolean isPressed() {
        return false;
    }
}
