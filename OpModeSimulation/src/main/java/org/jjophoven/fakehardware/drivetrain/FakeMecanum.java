package org.jjophoven.fakehardware.drivetrain;

import org.jjophoven.fakehardware.FakeMotor;
import org.jjophoven.fit.MotorModel;

public class FakeMecanum extends SimulatedDrivetrain {
    private static final int FL = 0;
    private static final int BL = 1;
    private static final int FR = 2;
    private static final int BR = 3;

    private final double R;
    private final double wheelRadius;

    public FakeMecanum(FakeMotor[] motors, double[] coefficients, double wheelbase, double trackWidth, double wheelDiameter) {
        super(motors, MotorModel.fromString("a=Au-Bv*abs(d)-Cv-Dsgn(v)"), coefficients);

        R = wheelbase + trackWidth;
        wheelRadius = wheelDiameter / 2;
    }

    MotionVector forwardKinematics(double[] motors) {
        double fl = motors[FL] * wheelRadius;
        double fr = motors[FR] * wheelRadius;
        double bl = motors[BL] * wheelRadius;
        double br = motors[BR] * wheelRadius;

        return new MotionVector(
            (fl + fr + bl + br) / 4,
                (fl - fr + br - bl) / 4,
                -(br - bl + fr - fl) / (4 * R)
        );
    }

    double[] inverseKinematics(MotionVector motion) {
        return new double[]{
                (motion.x + motion.y + motion.theta * R) / wheelRadius,
                (motion.x - motion.y + motion.theta * R) / wheelRadius,
                (motion.x - motion.y - motion.theta * R) / wheelRadius,
                (motion.x + motion.y - motion.theta * R) / wheelRadius
        };
    };
}