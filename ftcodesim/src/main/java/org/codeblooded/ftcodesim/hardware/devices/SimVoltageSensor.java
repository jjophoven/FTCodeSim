package org.codeblooded.ftcodesim.hardware.devices;

import com.qualcomm.robotcore.hardware.VoltageSensor;

public class SimVoltageSensor implements VoltageSensor, SimHardwareDevice {
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
    public void update(double deltaTime) {
        // add noise if you want that instability
    }
}
