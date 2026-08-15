package org.codeblooded.ftcodesim.hardware.drivetrain;


import org.codeblooded.ftcodesim.ascope.boundaries.MotionVector;

public class SimulatedMecanum extends SimulatedDrivetrain {
    private static final int FL = 0;
    private static final int FR = 1;
    private static final int BL = 2;
    private static final int BR = 3;

    private final double R;
    private final double wheelRadius;
    private final double strafeEfficiency;

    public SimulatedMecanum(SimMecanumConfig config) {
        super(config, config.frontLeftMotorName, config.frontRightMotorName, config.backLeftMotorName, config.backRightMotorName);

        R = config.wheelbase / 2 + config.trackWidth / 2;
        wheelRadius = config.wheelRadius;
        this.strafeEfficiency = config.maxStrafeSpeed / config.maxForwardSpeed; // strafeEfficiency produces ellipse instead of rhombus
    }

    @Override
    MotionVector forwardKinematics(double[] motors) {
        double fl = motors[FL] * wheelRadius;
        double fr = motors[FR] * wheelRadius;
        double bl = motors[BL] * wheelRadius;
        double br = motors[BR] * wheelRadius;

        double x = (fl + fr + bl + br) / 4.0;
        double y = (-fl + fr + bl - br) / 4.0 * strafeEfficiency;
        double theta = (-fl + fr - bl + br) / (4.0 * R);

        return new MotionVector(x, y, theta);
    }

    @Override
    double[] inverseKinematics(MotionVector motion) {
        double x = motion.x;
        double y = motion.y / strafeEfficiency;

        return new double[]{
                (x - y - motion.theta * R) / wheelRadius, // FL
                (x + y + motion.theta * R) / wheelRadius, // FR
                (x + y - motion.theta * R) / wheelRadius, // BL
                (x - y + motion.theta * R) / wheelRadius  // BR
        };
    }
}