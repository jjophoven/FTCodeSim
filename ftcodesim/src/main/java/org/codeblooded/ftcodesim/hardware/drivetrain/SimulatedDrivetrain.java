package org.codeblooded.ftcodesim.hardware.drivetrain;

import org.codeblooded.ftcodesim.ascope.boundaries.MotionVector;
import org.codeblooded.ftcodesim.hardware.SimHardwareMap;
import org.codeblooded.ftcodesim.hardware.devices.*;
import org.firstinspires.ftc.robotcore.external.navigation.*;

public abstract class SimulatedDrivetrain implements SimHardwareMechanism {
    protected final SimDrivetrainMotor[] motors;

    protected MotionVector position = new MotionVector(0, 0, 0);

    protected MotionVector velocity = new MotionVector(0, 0, 0);
    protected MotionVector acceleration = new MotionVector(0, 0, 0);
    protected SimDrivetrainConfig config;

    protected String[] motorNames;
    protected SimHardwareMap hardwareMap;
    protected double[] motorAngularVelocities;

    protected double[] motorAngularAccelerations;
    double[] motorPowers;

    public SimulatedDrivetrain(SimDrivetrainConfig config, String... motorNames) {
        this.config = config;
        this.motors = new SimDrivetrainMotor[motorNames.length];
        this.motorNames = motorNames;

        motorAngularVelocities = new double[motors.length];
        motorAngularAccelerations = new double[motors.length];
        motorPowers = new double[motors.length];
    }

    public Pose2D getPose() {
        return new Pose2D(DistanceUnit.INCH, position.x(), position.y(), AngleUnit.RADIANS, position.theta());
    }

    public void registerDevices(SimHardwareMap hardwareMap) {
        this.hardwareMap = hardwareMap;
        for (int i = 0; i < motorNames.length; i++) {
            motors[i] = hardwareMap.register(motorNames[i], new SimDrivetrainMotor());
        }
        //position.log("Drivetrain/position", config.robotModel); gives null pointer err
    }

    public abstract void update(double deltaTime);

    @Override
    public void reset() {
        velocity = new MotionVector(0, 0, 0);
        position = new MotionVector(0, 0, 0);
    }

    public void setPosition(MotionVector position) {
        this.position = position;
    }

    abstract MotionVector forwardKinematics(double[] motors);
    abstract double[] inverseKinematics(MotionVector motion);

    public MotionVector getPosition() {
        return position;
    }

    public MotionVector getVelocity() {
        return velocity;
    }

    public MotionVector getAcceleration() {
        return acceleration;
    }

    public double[] getMotorAngularVelocities() {
        return motorAngularVelocities;
    }

    public double[] getMotorAngularAccelerations() {
        return motorAngularAccelerations;
    }
}