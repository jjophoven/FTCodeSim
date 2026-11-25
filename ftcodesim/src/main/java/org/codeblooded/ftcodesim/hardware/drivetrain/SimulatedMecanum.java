package org.codeblooded.ftcodesim.hardware.drivetrain;


import com.pedropathing.math.Matrix;
import com.pedropathing.math.Vector2D;
import com.qualcomm.robotcore.hardware.DcMotor;
import org.codeblooded.ftcodesim.ascope.boundaries.FieldBoundary;
import org.codeblooded.ftcodesim.ascope.boundaries.MotionVector;
import org.codeblooded.ftcodesim.hardware.devices.SimMotor;
import org.psilynx.psikit.core.Logger;

public class SimulatedMecanum extends SimulatedDrivetrain {
    private static final int FL = 0;
    private static final int FR = 1;
    private static final int BL = 2;
    private static final int BR = 3;

    private final double R;
    private final SimMecanumConfig config;

    public SimulatedMecanum(SimMecanumConfig config) {
        super(config, config.frontLeftMotorName, config.frontRightMotorName, config.backLeftMotorName, config.backRightMotorName);

        R = config.wheelbase / 2 + config.trackWidth / 2;
        this.config = config;
    }

    public static double interpolateRadius(double xRadius, double yRadius, double theta) {
        return 1.0 / (Math.abs(Math.cos(theta)) / xRadius + Math.abs(Math.sin(theta)) / yRadius);
    }

    public static double interpolateAcceleration(double xRadius, double yRadius, double theta) {
        double cos = Math.abs(Math.cos(theta));
        double cos3 = cos * cos * cos;
        double sin = Math.abs(Math.sin(theta));
        double sin3 = sin * sin * sin;
        return 1.0 / (Math.abs(cos3) / xRadius + Math.abs(sin3) / yRadius);
    }

    @Override
    public void update(double deltaTime) {
        boolean isBrakeMode = false;
        for (int i = 0; i < motors.length; i++) {
            SimDrivetrainMotor motor = motors[i];
            motorPowers[i] = motor.getPower();

            if (motor.getZeroPowerBehavior() == DcMotor.ZeroPowerBehavior.BRAKE) {
                isBrakeMode = true;
            }

            Logger.recordOutput("Mecanum/wheel/angular vels radians per second/" + i, motor.getVelocity());
            Logger.recordOutput("Mecanum/wheel/powers/" + i, motor.getPower());
        }

        MotionVector robotVel = velocity.toRobotFrame(position.theta);
        MotionVector motorEffort = powerKinematics(motorPowers);

        robotVel.logV("Mecanum/robot/rawVelocity inches per second");

//        // TODO add lag button to distrupt path or apply forces

        double kCoulombFriction3 = config.turnNaturalDeceleration;
        double backEMF2 = config.maxTurnAccel / config.maxAngularVelocity;
        double kA2 = (backEMF2 * (config.maxAngularVelocity) + kCoulombFriction3) / config.nominalVoltage;

        // does not account for when motors are randomly opposing each-other, does not account for slippage

        double voltage = hardwareMap.getVoltage();

        double direction = Math.atan2(robotVel.y, robotVel.x);
        double maxVelocity = interpolateRadius(config.foresightConfig.maxAchievableForwardVelocity.get(), config.foresightConfig.maxAchievableStrafeVelocity.get(), direction);
        double decel = interpolateAcceleration(config.foresightConfig.naturalForwardDeceleration.get(), config.foresightConfig.naturalStrafeDeceleration.get(), direction);
        double backEMF = config.maxAcceleration / maxVelocity;
        double kA = (backEMF * (maxVelocity) + decel) / config.nominalVoltage;

        MotionVector robotAccel = new MotionVector(
                kA * voltage * motorEffort.x - (Math.signum(robotVel.x) == Math.signum(motorEffort.x) ? backEMF * Math.abs(motorEffort.x) * robotVel.x : 0),
                kA * voltage * motorEffort.y - (Math.signum(robotVel.y) == Math.signum(motorEffort.y) ? backEMF * Math.abs(motorEffort.y) * robotVel.y : 0),
                kA2 * voltage * motorEffort.theta - (Math.signum(robotVel.theta) == Math.signum(motorEffort.theta) ? backEMF2 * Math.abs(motorEffort.theta) * robotVel.theta : 0)
        );

        boolean isStationary =
                Math.abs(robotVel.x) <= config.staticVelocityRegion
                        && Math.abs(robotVel.y) <= config.staticVelocityRegion
                        && Math.abs(robotVel.theta) <= 0.05
                        && Math.abs(robotAccel.x) <= config.foresightConfig.naturalForwardDeceleration.get() * 1.1
                        && Math.abs(robotAccel.y) <= config.foresightConfig.naturalStrafeDeceleration.get() * 1.1
                        && Math.abs(robotAccel.theta) <= config.turnNaturalDeceleration * 0.1;

        if (isStationary) {
            robotVel = new MotionVector(0, 0, 0);
            robotAccel = new MotionVector(0, 0, 0);
        } else {
            Matrix quadratic = config.foresightConfig.quadraticBrakeCoefficients.get();
            Matrix linear = config.foresightConfig.linearBrakeCoefficients.get();
            Vector2D heading = config.foresightConfig.headingBrakeCoefficients.get();

            robotAccel.x -= calculateDeceleration(
                    robotVel.x, motorEffort.x, isBrakeMode,
                    quadratic.get(0, 0), linear.get(0, 0), decel
            );

            robotAccel.y -= calculateDeceleration(
                    robotVel.y, motorEffort.y, isBrakeMode,
                    quadratic.get(1, 1), linear.get(1, 1), decel
            );

            robotAccel.theta -= calculateDeceleration(
                    robotVel.theta, motorEffort.theta, isBrakeMode,
                    heading.x(), heading.y(), config.turnNaturalDeceleration
            );
        }

        robotVel.logV("Mecanum/robot/velocity inches per second");
        robotAccel.logV("Mecanum/robot/acceleration inches per second squared");
        Logger.recordOutput("Mecanum/isStationary", isStationary);

        robotVel = robotVel.step(robotAccel, deltaTime);

        velocity = robotVel.toFieldFrame(position.theta);

        MotionVector legalPosition = position;
        position = position.step(velocity, deltaTime);

        boolean isOutOfBounds = FieldBoundary.isOutOfBounds(position, config.robotGeometry, config.field.boundary);
        if (isOutOfBounds) {
            MotionVector normal = FieldBoundary.collisionNormal(
                    position,
                    config.robotGeometry,
                    config.field.boundary
            );

            double vNormal = velocity.dot(normal);

            if (vNormal > 0) {
                velocity = velocity.minus(normal.scale(vNormal));
            }

            double aNormal = acceleration.dot(normal);

            if (aNormal > 0) {
                acceleration = acceleration.minus(normal.scale(aNormal));
            }

            double correctionSign = Math.signum(legalPosition.theta - position.theta);

            if (Math.signum(velocity.theta) != correctionSign) {
                velocity.theta = 0;
            }

            if (Math.signum(acceleration.theta) != correctionSign) {
                acceleration.theta = 0;
            }

            position = legalPosition.step(velocity, deltaTime);
        }

        motorAngularVelocities = inverseKinematics(velocity.toRobotFrame(position.theta));
        for (int i = 0; i < motors.length; i++) {
            motors[i].setVelocity(motorAngularVelocities[i]);
        }

        robotAccel.toFieldFrame(position.theta).logV("Mecanum/acceleration");
        velocity.logV("Mecanum/velocity");
        position.log("Mecanum/position", config.robotModel);
        Logger.recordOutput("isInBounds", !isOutOfBounds);
    }

    @Override
    MotionVector forwardKinematics(double[] motors) {
        double fl = motors[FL] * config.wheelRadius;
        double fr = motors[FR] * config.wheelRadius;
        double bl = motors[BL] * config.wheelRadius;
        double br = motors[BR] * config.wheelRadius;

        double x = (fl + fr + bl + br) / 4.0;
        double y = (-fl + fr + bl - br) / 4.0;
        double theta = (-fl + fr - bl + br) / (4.0 * R);

        return new MotionVector(x, y, theta);
    }

    MotionVector powerKinematics(double[] motors) {
        double fl = motors[FL];
        double fr = motors[FR];
        double bl = motors[BL];
        double br = motors[BR];

        double x = (fl + fr + bl + br) / 4.0;
        double y = (-fl + fr + bl - br) / 4.0;
        double theta = (-fl + fr - bl + br) / 4.0; // no R bc this is effort

        return new MotionVector(x, y, theta);
    }

    @Override
    double[] inverseKinematics(MotionVector motion) {
        double x = motion.x;
        double y = motion.y;

        return new double[]{
                (x - y - motion.theta * R) / config.wheelRadius, // FL
                (x + y + motion.theta * R) / config.wheelRadius, // FR
                (x + y - motion.theta * R) / config.wheelRadius, // BL
                (x - y + motion.theta * R) / config.wheelRadius  // BR
        };
    }

    private double calculateDeceleration(
            double velocity,
            double effort,
            boolean isBrakeMode,
            double quadratic,
            double linear,
            double naturalDeceleration
    ) {
        if (velocity == 0) return 0;

        boolean braking = effort == 0
                ? isBrakeMode
                : velocity * effort < 0;

        if (braking) {
            return velocity / (2.0 * quadratic * Math.abs(velocity) + linear);
        }

        return naturalDeceleration * Math.signum(velocity);
    }
}