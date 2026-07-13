package org.codeblooded.ftcodesim.hardware.drivetrain;

import org.codeblooded.ftcodesim.hardware.SimHardwareMap;
import org.codeblooded.ftcodesim.physics.RobotGeometry;

public abstract class SimDrivetrainConfig {
    public double maxVelocity;
    public double maxAcceleration;
    public double naturalDeceleration;
    public double wheelRadius;
    public double staticVelocityRegion;
    public double staticFriction;
    public double nominalVoltage = 13;
    public RobotGeometry robotGeometry;
}
