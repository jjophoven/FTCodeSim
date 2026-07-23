package org.codeblooded.ftcodesim.hardware.drivetrain;

import org.codeblooded.ftcodesim.ascope.boundaries.MotionVector;

public class SimulatedTank extends SimulatedDrivetrain {
    private static final int FL = 0;
    private static final int FR = 1;
    private static final int BL = 2;
    private static final int BR = 3;

    private final SimTankConfig config;

    public SimulatedTank(SimTankConfig config) {
        super(config, config.frontLeftMotorName, config.frontRightMotorName, config.backLeftMotorName, config.backRightMotorName);
        this.config = config;
    }

    @Override
    MotionVector forwardKinematics(double[] motors) {
        double fl = motors[FL] * config.wheelRadius;
        double fr = motors[FR] * config.wheelRadius;
        double bl = motors[BL] * config.wheelRadius;
        double br = motors[BR] * config.wheelRadius;

        double left = (fl + bl) / 2.0;
        double right = (fr + br) / 2.0;

        return new MotionVector(
                (left + right) / 2.0,
                0.0,
                (left - right) / config.trackWidth
        );
    }

    @Override
    double[] inverseKinematics(MotionVector motion) {
        double left = motion.x + motion.theta * config.trackWidth / 2.0;
        double right = motion.x - motion.theta * config.trackWidth / 2.0;

        return new double[]{
                left / config.wheelRadius,
                right / config.wheelRadius,
                left / config.wheelRadius,
                right / config.wheelRadius
        };
    }
}