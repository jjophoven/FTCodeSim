package org.jjophoven.simhardware.devices;

import com.qualcomm.robotcore.hardware.VoltageSensor;

import java.util.Random;

public class SimVoltageSensor implements VoltageSensor, SimHardwareDevice {
    private static final double MAX_VOLTAGE = 14.2;
    private static final double MIN_VOLTAGE = 9.0;

    private static final double BASE_DISCHARGE = 0;  // 0.00045

    private final Random random = new Random();

    private double batteryVoltage;
    private double reportedVoltage;

    public SimVoltageSensor() {
        //batteryVoltage = 13.9 + random.nextDouble() * 0.3;
        batteryVoltage = 13.1;
        reportedVoltage = batteryVoltage;
    }

    @Override
    public double getVoltage() {
        return reportedVoltage;
    }

    @Override
    public void update(double deltaTime) {
        double dischargeMultiplier = 1.0 + (MAX_VOLTAGE - batteryVoltage) * 0.18;

        batteryVoltage -= BASE_DISCHARGE * dischargeMultiplier * deltaTime;
        batteryVoltage = Math.max(MIN_VOLTAGE, batteryVoltage);

        double noise = (random.nextDouble() - 0.5) * 0.010;

        reportedVoltage = batteryVoltage + noise;
    }

    @Override
    public Manufacturer getManufacturer() {
        return Manufacturer.Other;
    }

    @Override
    public String getDeviceName() {
        return "Simulated Voltage Sensor";
    }

    @Override
    public String getConnectionInfo() {
        return "Virtual";
    }

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public void resetDeviceConfigurationForOpMode() {
    }

    @Override
    public void close() {
    }
}