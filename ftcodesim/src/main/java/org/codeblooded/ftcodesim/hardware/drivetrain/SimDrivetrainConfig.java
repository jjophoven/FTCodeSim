package org.codeblooded.ftcodesim.hardware.drivetrain;

import com.pedropathing.algorithm.ForesightConfig;
import org.codeblooded.ftcodesim.ascope.SourceType;
import org.codeblooded.ftcodesim.ascope.boundaries.RobotGeometry;

public abstract class SimDrivetrainConfig {
    public double maxVelocity;
    public double maxAngularVelocity;

    public double maxAcceleration;
    public ForesightConfig foresightConfig;

    public double naturalDeceleration;
    public double wheelRadius;
    public double staticVelocityRegion;
    public double staticFriction;
    public double nominalVoltage = 13;
    public double quadraticBraking;
    public double linearBraking;

    public double quadraticBrakeHeading;
    public double linearBrakeHeading;

    public double forwardBrakingQuadratic;
    public double forwardBrakingLinear;

    public RobotGeometry robotGeometry;
    public SourceType robotModel;
}
