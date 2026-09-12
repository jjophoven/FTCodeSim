package org.codeblooded.ftcodesim.hardware.devices;

import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.ServoController;

/** Ideal positional servo whose commanded position changes instantaneously. */
public class SimServo implements Servo, SimHardwareDevice {
    private Direction direction = Direction.FORWARD;
    private double position;
    private double scaleMin;
    private double scaleMax = 1.0;

    @Override
    public void setDirection(Direction direction) {
        if (direction == null) throw new IllegalArgumentException("Servo direction cannot be null");
        this.direction = direction;
    }

    @Override
    public Direction getDirection() { return direction; }

    @Override
    public void setPosition(double position) {
        this.position = clip(position);
    }

    @Override
    public double getPosition() { return position; }

    @Override public ServoController getController() { return null; }
    @Override public int getPortNumber() { return 0; }

    /** Returns the scaled, direction-adjusted physical output in the range [0, 1]. */
    public double getPhysicalPosition() {
        double directed = direction == Direction.REVERSE ? 1.0 - position : position;
        return scaleMin + directed * (scaleMax - scaleMin);
    }

    @Override
    public void scaleRange(double min, double max) {
        if (!Double.isFinite(min) || !Double.isFinite(max) || min < 0 || max > 1 || min >= max) {
            throw new IllegalArgumentException("Servo scale range must satisfy 0 <= min < max <= 1");
        }
        scaleMin = min;
        scaleMax = max;
    }

    private static double clip(double value) {
        if (Double.isNaN(value)) throw new IllegalArgumentException("Servo position cannot be NaN");
        return Math.max(0.0, Math.min(1.0, value));
    }

    @Override public Manufacturer getManufacturer() { return Manufacturer.Other; }
    @Override public String getDeviceName() { return "FTCodeSim Servo"; }
    @Override public String getConnectionInfo() { return "simulated"; }
    @Override public int getVersion() { return 1; }
    @Override public void resetDeviceConfigurationForOpMode() {
        direction = Direction.FORWARD;
        scaleMin = 0.0;
        scaleMax = 1.0;
    }
    @Override public void close() { }
    @Override public void update(double deltaTime) { }
}
