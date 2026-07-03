package org.jjophoven.fakehardware.devices;

import com.qualcomm.robotcore.hardware.VoltageSensor;

public class FakeVoltageSensor implements VoltageSensor, FakeHardwareDevice {
    @Override
    public double getVoltage() {
        return 13;
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

    @Override
    public void update() {

    }
}
