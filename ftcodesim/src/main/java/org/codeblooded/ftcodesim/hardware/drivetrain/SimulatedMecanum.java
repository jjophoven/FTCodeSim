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
        this.strafeEfficiency = config.strafeEfficiency;
    }

    @Override
    MotionVector forwardKinematics(double[] motors) {
        double fl = motors[FL] * wheelRadius;
        double fr = motors[FR] * wheelRadius;
        double bl = motors[BL] * wheelRadius;
        double br = motors[BR] * wheelRadius;

        return new MotionVector(
                (fl + fr + bl + br) / 4.0,
                (fl - fr - bl + br) / 4.0 * strafeEfficiency,
                (fl - fr + bl - br) / (4.0 * R)
        );
    }

    @Override
    double[] inverseKinematics(MotionVector motion) {
        double y = motion.y / strafeEfficiency;

        return new double[]{
                (motion.x + y + motion.theta * R) / wheelRadius, // FL
                (motion.x - y - motion.theta * R) / wheelRadius, // FR
                (motion.x - y + motion.theta * R) / wheelRadius, // BL
                (motion.x + y - motion.theta * R) / wheelRadius  // BR
        };
    }
}