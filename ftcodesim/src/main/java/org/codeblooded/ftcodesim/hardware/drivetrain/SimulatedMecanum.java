package org.codeblooded.ftcodesim.hardware.drivetrain;


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
    private final double wheelRadius;
    private final SimMecanumConfig config;

    public SimulatedMecanum(SimMecanumConfig config) {
        super(config, config.frontLeftMotorName, config.frontRightMotorName, config.backLeftMotorName, config.backRightMotorName);

        R = config.wheelbase / 2 + config.trackWidth / 2;
        wheelRadius = config.wheelRadius;
        this.config = config;
    }

    public static double interpolateRadius(double xRadius, double yRadius, double theta) {
        return 1.0 / (Math.abs(Math.cos(theta)) / xRadius + Math.abs(Math.sin(theta)) / yRadius);
    }

    @Override
    public void update(double deltaTime) {
        for (int i = 0; i < motors.length; i++) {
            SimMotor motor = motors[i];
            motorAngularAccelerations[i] = motor.getAcceleration();

            Logger.recordOutput("Mecanum/angular vels radians per second/" + motor.deviceName, motor.getVelocity());
            Logger.recordOutput("Mecanum/powers/" + motor.deviceName, motor.getPower());
            Logger.recordOutput("Mecanum/angular accelerations radians per second per second/" + motor.deviceName, motor.getAcceleration());
        }

        acceleration = forwardKinematics(motorAngularAccelerations);


        MotionVector robotVel = velocity.toRobotFrame(position.theta);
        double naturalDeceleration = interpolateRadius(config.forwardNaturalDeceleration, config.strafeNaturalDeceleration, Math.atan2(robotVel.y, robotVel.x));

        boolean isStationary = acceleration.magnitude() < naturalDeceleration && velocity.magnitude() < config.staticVelocityRegion && Math.abs(acceleration.theta) < config.turnNaturalDeceleration && Math.abs(velocity.theta) < config.staticVelocityRegion;

        if (isStationary) {
            velocity = new MotionVector(0, 0, 0);
            acceleration = new MotionVector(0, 0, 0);
        }
        else {
            acceleration = acceleration.minus(robotVel.unitVector().scale(naturalDeceleration));
            acceleration.theta -= config.turnNaturalDeceleration * Math.signum(velocity.theta);
        }

        acceleration = acceleration.toFieldFrame(position.theta);

        velocity = velocity.step(acceleration, deltaTime);

        MotionVector legalPosition = position;
        position = position.step(velocity, deltaTime);

        boolean isOutOfBounds = FieldBoundary.isOutOfBounds(position, config.robotGeometry);
        if (isOutOfBounds) {
            MotionVector normal = FieldBoundary.collisionNormal(
                    position,
                    config.robotGeometry
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
            motors[i].velocity = motorAngularVelocities[i];
        }

        acceleration.log("Mecanum/acceleration");
        velocity.log("Mecanum/velocity");
        position.log("Mecanum/position", config.robotModel);
        Logger.recordOutput("isInBounds", !isOutOfBounds);
    }


    @Override
    MotionVector forwardKinematics(double[] motors) {
        double fl = motors[FL] * wheelRadius;
        double fr = motors[FR] * wheelRadius;
        double bl = motors[BL] * wheelRadius;
        double br = motors[BR] * wheelRadius;

        double x = (fl + fr + bl + br) / 4.0;
        double y = (-fl + fr + bl - br) / 4.0;
        double theta = (-fl + fr - bl + br) / (4.0 * R);

        return new MotionVector(x, y, theta);
    }

    @Override
    double[] inverseKinematics(MotionVector motion) {
        double x = motion.x;
        double y = motion.y;

        return new double[]{
                (x - y - motion.theta * R) / wheelRadius, // FL
                (x + y + motion.theta * R) / wheelRadius, // FR
                (x + y - motion.theta * R) / wheelRadius, // BL
                (x - y + motion.theta * R) / wheelRadius  // BR
        };
    }
}