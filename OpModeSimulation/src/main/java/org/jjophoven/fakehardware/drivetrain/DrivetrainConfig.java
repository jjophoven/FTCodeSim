package org.jjophoven.fakehardware.drivetrain;

import org.jjophoven.fakehardware.FakeHardwareMap;

public abstract class DrivetrainConfig {
    public FakeHardwareMap fakeHardwareMap;
    public double maxVelocity;
    public double maxAcceleration;
    public double naturalDeceleration;
    public double wheelRadius;
    public double staticVelocityRegion;
    public double staticFriction;
    public double nominalVoltage = 13;
}
