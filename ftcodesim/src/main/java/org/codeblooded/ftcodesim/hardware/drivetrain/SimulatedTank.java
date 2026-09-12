package org.codeblooded.ftcodesim.hardware.drivetrain;

import org.codeblooded.ftcodesim.ascope.boundaries.MotionVector;

public class SimulatedTank extends SimulatedDrivetrain {
    private static final int FL = 0;
    private static final int FR = 1;
    private final SimTankConfig config;
    private final boolean fourMotor;

    public SimulatedTank(SimTankConfig config) {
        super(config, config.motorNames());
        this.config = config;
        this.fourMotor = motorNames.length == 4;
    }

    @Override
    protected void integratePosition(double deltaTime) {
        integrateLongitudinalPosition(deltaTime);
    }

    @Override
    protected void constrainVelocity() {
        constrainToLongitudinalVelocity();
    }

    @Override
    MotionVector forwardKinematics(double[] motors) {
        double fl = motors[FL] * config.wheelRadius;
        double fr = motors[FR] * config.wheelRadius;
        double left = fourMotor ? (fl + motors[2] * config.wheelRadius) / 2.0 : fl;
        double right = fourMotor ? (fr + motors[3] * config.wheelRadius) / 2.0 : fr;

        return new MotionVector(
                (left + right) / 2.0,
                0.0,
                (right - left) / config.trackWidth
        );
    }

    @Override
    double[] inverseKinematics(MotionVector motion) {
        double left = motion.x - motion.theta * config.trackWidth / 2.0;
        double right = motion.x + motion.theta * config.trackWidth / 2.0;

        if (!fourMotor) {
            return new double[]{left / config.wheelRadius, right / config.wheelRadius};
        }
        return new double[]{left / config.wheelRadius, right / config.wheelRadius,
                left / config.wheelRadius, right / config.wheelRadius};
    }
}
