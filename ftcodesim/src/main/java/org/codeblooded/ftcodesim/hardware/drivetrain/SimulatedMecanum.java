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

    private final double strafeEfficiency;

    BrakingModel forward;
    BrakingModel strafe;
    BrakingModel heading;

    public SimulatedMecanum(SimMecanumConfig config) {
        super(config, config.frontLeftMotorName, config.frontRightMotorName, config.backLeftMotorName, config.backRightMotorName);

        R = config.wheelbase / 2 + config.trackWidth / 2;
        wheelRadius = config.wheelRadius;
        this.config = config;

        strafeEfficiency = config.foresightConfig.maxAchievableStrafeVelocity.get() / config.foresightConfig.maxAchievableForwardVelocity.get();

        forward = fitBrakingModel(config.foresightConfig.quadraticBrakeCoefficients.get().get(0,0), config.foresightConfig.linearBrakeCoefficients.get().get(0,0), config.foresightConfig.maxAchievableForwardVelocity.get());

        strafe = fitBrakingModel(config.foresightConfig.quadraticBrakeCoefficients.get().get(1,1), config.foresightConfig.linearBrakeCoefficients.get().get(1,1), config.foresightConfig.maxAchievableStrafeVelocity.get());

        heading = fitBrakingModel(config.foresightConfig.headingBrakeCoefficients.get().x(), config.foresightConfig.headingBrakeCoefficients.get().y(), config.maxAngularVelocity);

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

    double stoppingFriction(double v) {
        if (Math.abs(v) < 1e-6)
            return 0;

        double d = config.quadraticBrakeHeading * v * Math.abs(v) + config.linearBrakeHeading * v;

        // Acceleration opposing wheel velocity
        return -v * Math.abs(v) / (2.0 * d);
    }

    double regenerativeAcceleration(double v) {
        return -regenerativeBraking * v;
    }

//    @Override
//    public void update(double deltaTime) {
//        for (int i = 0; i < motors.length; i++) {
//            SimMotor motor = motors[i];
//            motorAngularAccelerations[i] = motor.getAcceleration();
//
//            Logger.recordOutput("Mecanum/angular vels radians per second/" + motor.deviceName, motor.getVelocity());
//            Logger.recordOutput("Mecanum/powers/" + motor.deviceName, motor.getPower());
//            Logger.recordOutput("Mecanum/angular accelerations radians per second per second/" + motor.deviceName, motor.getAcceleration());
//        }
//
//        MotionVector robotVel = velocity.toRobotFrame(position.theta);
//        //acceleration = forwardAccKinematics(motorAngularAccelerations, Math.atan2(robotVel.y, robotVel.x));
//        acceleration = forwardKinematics(motorAngularAccelerations);
//        //acceleration = forwardAccKinematics2(motorAngularAccelerations, robotVel);
//
//        double naturalDeceleration = interpolateAcceleration(config.forwardNaturalDeceleration, config.strafeNaturalDeceleration, Math.atan2(robotVel.y, robotVel.x));
//        naturalDeceleration = config.forwardNaturalDeceleration;
//
//        boolean isStationary = acceleration.magnitude() < naturalDeceleration && velocity.magnitude() < config.staticVelocityRegion && Math.abs(acceleration.theta) < config.turnNaturalDeceleration && Math.abs(velocity.theta) < config.staticVelocityRegion;
//
//        if (isStationary) {
//            velocity = new MotionVector(0, 0, 0);
//            acceleration = new MotionVector(0, 0, 0);
//        }
//        else {
//            acceleration = acceleration.minus(robotVel.unitVector().scale(naturalDeceleration));
////            acceleration.theta -= config.turnNaturalDeceleration * Math.signum(velocity.theta);
//
//            // accel = Au - Bv
//
//            (-fl + fr - bl + br) / (4.0 * R)
//
//            // theta accel = (-fl + fr - bl + br) / (4.0 * R)
//            //acceleration.theta -= -velocity.theta / (2 * config.quadraticBrakeHeading * velocity.theta + config.linearBrakeHeading);
//        }
//
//        acceleration = acceleration.toFieldFrame(position.theta);
//
//        velocity = velocity.step(acceleration, deltaTime);
//
//        MotionVector legalPosition = position;
//        position = position.step(velocity, deltaTime);
//
//        boolean isOutOfBounds = FieldBoundary.isOutOfBounds(position, config.robotGeometry);
//        if (isOutOfBounds) {
//            MotionVector normal = FieldBoundary.collisionNormal(
//                    position,
//                    config.robotGeometry
//            );
//
//            double vNormal = velocity.dot(normal);
//
//            if (vNormal > 0) {
//                velocity = velocity.minus(normal.scale(vNormal));
//            }
//
//            double aNormal = acceleration.dot(normal);
//
//            if (aNormal > 0) {
//                acceleration = acceleration.minus(normal.scale(aNormal));
//            }
//
//            double correctionSign = Math.signum(legalPosition.theta - position.theta);
//
//            if (Math.signum(velocity.theta) != correctionSign) {
//                velocity.theta = 0;
//            }
//
//            if (Math.signum(acceleration.theta) != correctionSign) {
//                acceleration.theta = 0;
//            }
//
//            position = legalPosition.step(velocity, deltaTime);
//        }
//
//        motorAngularVelocities = inverseKinematics(velocity.toRobotFrame(position.theta));
//        for (int i = 0; i < motors.length; i++) {
//            motors[i].velocity = motorAngularVelocities[i];
//        }
//
//        acceleration.log("Mecanum/acceleration");
//        velocity.log("Mecanum/velocity");
//        position.log("Mecanum/position", config.robotModel);
//        Logger.recordOutput("isInBounds", !isOutOfBounds);
//    }

    public static class BrakingModel {
        public final double A2;
        public final double B2;
        public final double rSquared;

        public BrakingModel(double A2, double B2, double rSquared) {
            this.A2 = A2;
            this.B2 = B2;
            this.rSquared = rSquared;
        }
    }

    /**
     * Fits:
     *
     *     D(v) = A * v^2 + B * v
     *
     * to the integrated acceleration model:
     *
     *     a(v) = A2 * v + B2
     *
     * giving:
     *
     *     D(v) = v/A2
     *          - B2/A2^2 * ln(1 + A2*v/B2)
     *
     * The fit is performed from v = 0 to maxVelocity.
     */
    public static BrakingModel fitBrakingModel(
            double A,
            double B,
            double maxVelocity
    ) {
        final int samples = 200;
        final int maxIterations = 100;

        double B2 = 1.0 / (2.0 * Math.max(A, 1e-12));
        double A2 = 0.001;

        double previousError = totalSquaredError(
                A, B, maxVelocity, A2, B2, samples
        );

        for (int iteration = 0; iteration < maxIterations; iteration++) {
            double j11 = 0.0;
            double j12 = 0.0;
            double j22 = 0.0;

            double g1 = 0.0;
            double g2 = 0.0;

            for (int i = 1; i <= samples; i++) {
                double v = maxVelocity * i / samples;

                double target = A * v * v + B * v;
                double predicted = stoppingDistance(v, A2, B2);
                double error = predicted - target;

                double da = Math.max(Math.abs(A2) * 1e-5, 1e-7);
                double db = Math.max(Math.abs(B2) * 1e-5, 1e-7);

                // Don't allow the lower numerical-derivative sample
                // to cross zero.
                double lowerA = Math.max(A2 - da, A2 * 0.5);
                double upperA = A2 + da;

                double lowerB = Math.max(B2 - db, B2 * 0.5);
                double upperB = B2 + db;

                double dDa =
                        (stoppingDistance(v, upperA, B2)
                                - stoppingDistance(v, lowerA, B2))
                                / (upperA - lowerA);

                double dDb =
                        (stoppingDistance(v, A2, upperB)
                                - stoppingDistance(v, A2, lowerB))
                                / (upperB - lowerB);

                j11 += dDa * dDa;
                j12 += dDa * dDb;
                j22 += dDb * dDb;

                g1 += dDa * error;
                g2 += dDb * error;
            }

            // Levenberg-style damping.
            //
            // Scale it relative to the diagonal rather than adding the
            // same tiny absolute number every time.
            double lambda = 1e-6;

            j11 += lambda * Math.max(j11, 1.0);
            j22 += lambda * Math.max(j22, 1.0);

            double determinant = j11 * j22 - j12 * j12;

            if (!Double.isFinite(determinant) ||
                    Math.abs(determinant) < 1e-20) {
                break;
            }

            double deltaA =
                    (-g1 * j22 + g2 * j12) / determinant;

            double deltaB =
                    (-j11 * g2 + j12 * g1) / determinant;

            if (!Double.isFinite(deltaA) ||
                    !Double.isFinite(deltaB)) {
                break;
            }

            /*
             * Raw Gauss-Newton can take a gigantic step.
             * Reduce the step until it actually improves the fit.
             */
            double scale = 1.0;

            double newA2 = A2;
            double newB2 = B2;
            double newError = previousError;

            boolean accepted = false;

            for (int attempt = 0; attempt < 20; attempt++) {
                double candidateA =
                        Math.max(A2 + deltaA * scale, 1e-10);

                double candidateB =
                        Math.max(B2 + deltaB * scale, 1e-10);

                double candidateError = totalSquaredError(
                        A,
                        B,
                        maxVelocity,
                        candidateA,
                        candidateB,
                        samples
                );

                if (Double.isFinite(candidateError) &&
                        candidateError < previousError) {
                    newA2 = candidateA;
                    newB2 = candidateB;
                    newError = candidateError;
                    accepted = true;
                    break;
                }

                scale *= 0.5;
            }

            if (!accepted) {
                break;
            }

            double actualDeltaA = newA2 - A2;
            double actualDeltaB = newB2 - B2;

            A2 = newA2;
            B2 = newB2;
            previousError = newError;

            if (Math.abs(actualDeltaA) < 1e-10 &&
                    Math.abs(actualDeltaB) < 1e-8) {
                break;
            }
        }

        // R²
        double mean = 0.0;

        for (int i = 1; i <= samples; i++) {
            double v = maxVelocity * i / samples;
            mean += A * v * v + B * v;
        }

        mean /= samples;

        double ssTot = 0.0;
        double ssRes = 0.0;

        for (int i = 1; i <= samples; i++) {
            double v = maxVelocity * i / samples;

            double target = A * v * v + B * v;
            double predicted = stoppingDistance(v, A2, B2);

            double targetError = target - mean;
            double fitError = target - predicted;

            ssTot += targetError * targetError;
            ssRes += fitError * fitError;
        }

        double rSquared = 1.0 - ssRes / ssTot;

        System.out.println(
                "A: " + A +
                        " B: " + B +
                        " maxVel: " + maxVelocity
        );

        System.out.println(
                "A2: " + A2 +
                        " B2: " + B2 +
                        " rSquared: " + rSquared
        );

        return new BrakingModel(A2, B2, rSquared);
    }

    private static double totalSquaredError(
            double A,
            double B,
            double maxVelocity,
            double A2,
            double B2,
            int samples
    ) {
        double error = 0.0;

        for (int i = 1; i <= samples; i++) {
            double v = maxVelocity * i / samples;

            double target = A * v * v + B * v;
            double predicted = stoppingDistance(v, A2, B2);

            double e = predicted - target;
            error += e * e;
        }

        return error;
    }

    /**
     * Stopping distance resulting from:
     *
     *     a(v) = A2*v + B2
     */
    public static double stoppingDistance(
            double v,
            double A2,
            double B2
    ) {
        if (v <= 0) {
            return 0;
        }

        // Handle the A2 -> 0 case separately.
        if (Math.abs(A2) < 1e-12) {
            return v * v / (2.0 * B2);
        }

        double x = A2 * v / B2;

        return v / A2
                - B2 / (A2 * A2)
                * Math.log1p(x);
    }

    @Override
    public void update(double deltaTime) {
        for (int i = 0; i < motors.length; i++) {
            SimMotor motor = motors[i];
            motorAngularAccelerations[i] = motor.getAcceleration();

            Logger.recordOutput("Mecanum/wheel/angular vels radians per second/" + motor.deviceName, motor.getVelocity());
            Logger.recordOutput("Mecanum/wheel/powers/" + motor.deviceName, motor.getPower());
            Logger.recordOutput("Mecanum/wheel/angular accelerations radians per second per second/" + motor.deviceName, motor.getAcceleration());
        }

        MotionVector robotVel = velocity.toRobotFrame(position.theta);
        MotionVector robotAccel = forwardKinematics(motorAngularAccelerations);

        robotVel.logV("Mecanum/robot/rawVelocity inches per second");
        robotAccel.logV("Mecanum/robot/rawAcceleration inches per second squared");

        double vx = robotVel.x;
        double vy = robotVel.y;

        double xDecel = 0;
        double yDecel = 0;

        double speed = Math.hypot(vx, vy);

        double velocityDotAcceleration =
                robotVel.x * robotAccel.x
                        + robotVel.y * robotAccel.y;

        if (speed > 1e-6 && velocityDotAcceleration >= 0) {
            double cos = Math.abs(vx) / speed;
            double sin = Math.abs(vy) / speed;

            double forwardDecel =
                    config.foresightConfig.naturalForwardDeceleration.get();

            double strafeDecel =
                    config.foresightConfig.naturalStrafeDeceleration.get();

            // Radius of the mecanum deceleration rhombus in this direction
            double decel =
                    1.0 / (cos / forwardDecel + sin / strafeDecel);

            // 2 left, moving the right two wheels should slow down -> +x, 0 = y, -theta

            // Apply that magnitude opposite the current velocity
            xDecel = decel * vx / speed;
            yDecel = decel * vy / speed;
        }

        if (Math.signum(robotVel.x) != Math.signum(robotAccel.x) || robotAccel.x == 0)
            robotAccel.x -= robotVel.x * forward.A2 + forward.B2 * Math.signum(robotVel.x) + xDecel;

        if (Math.signum(robotVel.y) != Math.signum(robotAccel.y) || robotAccel.y == 0)
            robotAccel.y -= robotVel.y * strafe.A2 + strafe.B2 * Math.signum(robotVel.y) - yDecel;

        if (Math.signum(robotVel.theta) != Math.signum(robotAccel.theta) || robotAccel.theta == 0)
            robotAccel.theta -= robotVel.theta * heading.A2 * 7 + heading.B2 * Math.signum(robotVel.theta) - config.turnNaturalDeceleration * Math.signum(robotVel.theta);

        boolean isStationary =
                Math.abs(robotVel.x) < config.staticVelocityRegion
                        && Math.abs(robotVel.y) < config.staticVelocityRegion
                        && Math.abs(robotVel.theta) < config.staticVelocityRegion
                        && Math.abs(robotAccel.x) < config.foresightConfig.naturalForwardDeceleration.get()
                        && Math.abs(robotAccel.y) < config.foresightConfig.naturalStrafeDeceleration.get()
                        && Math.abs(robotAccel.theta) < config.turnNaturalDeceleration;

        if (isStationary) {
            robotVel = new MotionVector(0, 0, 0);
            acceleration = new MotionVector(0, 0, 0);
        } else {
//            rawAcceleration.x -= xDecel;
//            rawAcceleration.y -= yDecel;
//            rawAcceleration.theta -=
//                        config.turnNaturalDeceleration * Math.signum(robotVel.theta);
        }

        robotVel.logV("Mecanum/robot/velocity inches per second");
        robotAccel.logV("Mecanum/robot/acceleration inches per second squared");

        acceleration = robotAccel.toFieldFrame(position.theta);
        velocity = robotVel.toFieldFrame(position.theta).step(acceleration, deltaTime);

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

        acceleration.logV("Mecanum/acceleration");
        velocity.logV("Mecanum/velocity");
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

//        if (Math.signum(robotVel.x) != Math.signum(x) || x == 0)
//            x -= robotVel.x * forward.A2 + forward.B2 * Math.signum(robotVel.x) + xDecel;
//
//        if (Math.signum(robotVel.y) != Math.signum(y) || y == 0)
//            y -= robotVel.y * strafe.A2 + strafe.B2 * Math.signum(robotVel.y) - yDecel;
//
//        if (Math.signum(robotVel.theta) != Math.signum(theta) || rawAcceleration.theta == 0)
//            theta -= robotVel.theta * heading.A2 + heading.B2 * Math.signum(robotVel.theta) - config.turnNaturalDeceleration * Math.signum(robotVel.theta);

        return new MotionVector(x, y, theta);
    }

    MotionVector forwardKinematics(double[] motors, MotionVector robotVel, double xDecel, double yDecel) {
        double fl = motors[FL] * wheelRadius;
        double fr = motors[FR] * wheelRadius;
        double bl = motors[BL] * wheelRadius;
        double br = motors[BR] * wheelRadius;

        double x = (fl + fr + bl + br) / 4.0;
        double y = (-fl + fr + bl - br) / 4.0;
        double theta = (-fl + fr - bl + br) / (4.0 * R);

        // Convert current chassis velocity back into wheel linear velocities.
        double flVel = robotVel.x - robotVel.y - robotVel.theta * R;
        double frVel = robotVel.x + robotVel.y + robotVel.theta * R;
        double blVel = robotVel.x + robotVel.y - robotVel.theta * R;
        double brVel = robotVel.x - robotVel.y + robotVel.theta * R;

        // Full braking magnitude for each chassis axis.
        double xBrake =
                robotVel.x * forward.A2
                        + forward.B2 * Math.signum(robotVel.x)
                        + xDecel;

        double yBrake =
                robotVel.y * strafe.A2
                        + strafe.B2 * Math.signum(robotVel.y)
                        - yDecel;

        double thetaBrake =
                robotVel.theta * heading.A2
                        + heading.B2 * Math.signum(robotVel.theta)
                        - config.turnNaturalDeceleration * Math.signum(robotVel.theta);

        // Each wheel contributes 1/4 of x/y and 1/(4R) of theta.
        if (against(flVel, fl)) {
            x     -= xBrake / 4.0;
            y     += yBrake / 4.0;
            theta += thetaBrake / (4.0 * R);
        }

        if (against(frVel, fr)) {
            x     -= xBrake / 4.0;
            y     -= yBrake / 4.0;
            theta -= thetaBrake / (4.0 * R);
        }

        if (against(blVel, bl)) {
            x     -= xBrake / 4.0;
            y     -= yBrake / 4.0;
            theta += thetaBrake / (4.0 * R);
        }

        if (against(brVel, br)) {
            x     -= xBrake / 4.0;
            y     += yBrake / 4.0;
            theta -= thetaBrake / (4.0 * R);
        }

        return new MotionVector(x, y, theta);
    }

    private boolean against(double wheelVelocity, double wheelPower) {
        return wheelVelocity != 0
                && (wheelPower == 0
                || Math.signum(wheelVelocity) != Math.signum(wheelPower));
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

    double[] inverseKinematics(MotionVector motion, MotionVector robotVel, double xDecel, double yDecel) {
        double x = motion.x;
        double y = motion.y;
        double theta = motion.theta;

        double flVel = robotVel.x - robotVel.y - robotVel.theta * R;
        double frVel = robotVel.x + robotVel.y + robotVel.theta * R;
        double blVel = robotVel.x + robotVel.y - robotVel.theta * R;
        double brVel = robotVel.x - robotVel.y + robotVel.theta * R;

        double xBrake =
                robotVel.x * forward.A2
                        + forward.B2 * Math.signum(robotVel.x)
                        + xDecel;

        double yBrake =
                robotVel.y * strafe.A2
                        + strafe.B2 * Math.signum(robotVel.y)
                        - yDecel;

        double thetaBrake =
                robotVel.theta * heading.A2
                        + heading.B2 * Math.signum(robotVel.theta)
                        - config.turnNaturalDeceleration * Math.signum(robotVel.theta);

        if (against(flVel, x - y - theta * R)) {
            x += xBrake / 4.0;
            y -= yBrake / 4.0;
            theta -= thetaBrake / 4.0;
        }

        if (against(frVel, x + y + theta * R)) {
            x += xBrake / 4.0;
            y += yBrake / 4.0;
            theta += thetaBrake / 4.0;
        }

        if (against(blVel, x + y - theta * R)) {
            x += xBrake / 4.0;
            y += yBrake / 4.0;
            theta -= thetaBrake / 4.0;
        }

        if (against(brVel, x - y + theta * R)) {
            x += xBrake / 4.0;
            y -= yBrake / 4.0;
            theta += thetaBrake / 4.0;
        }

        return new double[]{
                (x - y - theta * R) / wheelRadius, // FL
                (x + y + theta * R) / wheelRadius, // FR
                (x + y - theta * R) / wheelRadius, // BL
                (x - y + theta * R) / wheelRadius  // BR
        };
    }
}