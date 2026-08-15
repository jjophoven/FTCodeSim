package org.codeblooded.ftcodesim.hardware.drivetrain;

import org.codeblooded.ftcodesim.ascope.boundaries.FieldBoundary;
import org.codeblooded.ftcodesim.ascope.boundaries.MotionVector;
import org.codeblooded.ftcodesim.ascope.boundaries.RobotGeometry;
import org.codeblooded.ftcodesim.hardware.SimHardwareMap;
import org.codeblooded.ftcodesim.hardware.devices.*;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.codeblooded.fit.MotorModel;
import org.psilynx.psikit.core.Logger;

public abstract class SimulatedDrivetrain implements SimHardwareMechanism {
    private final SimMotor[] motors;

    public MotionVector position = new MotionVector(0, 0, 0);
    public MotionVector velocity = new MotionVector(0, 0, 0);

    public SimDrivetrainConfig config;
    public String[] motorNames;
    public SimHardwareMap hardwareMap;
    public SimVoltageSensor voltageSensor;

    public double regenerativeBraking;

    protected double[] motorAngularVelocities;

    public SimulatedDrivetrain(SimDrivetrainConfig config, String... motorNames) {
        this.config = config;
        this.motors = new SimMotor[motorNames.length];
        this.motorNames = motorNames;

        regenerativeBraking = findRegenerativeBrakingCoefficient(config.quadraticBraking, config.linearBraking, config.naturalDeceleration, config.maxVelocity);

        motorAngularVelocities = new double[motors.length];
    }

    public Pose2D getActualPose() {
        return new Pose2D(DistanceUnit.INCH, position.x(), position.y(), AngleUnit.RADIANS, position.theta());
    }

    public Pose2D getVelocityPose() {
        return new Pose2D(DistanceUnit.INCH, velocity.x(), velocity.y(), AngleUnit.RADIANS, velocity.theta());
    }

    public void registerDevices(SimHardwareMap hardwareMap) {
        this.hardwareMap = hardwareMap;
        this.voltageSensor = (SimVoltageSensor) hardwareMap.voltageSensor.iterator().next();
        for (int i = 0; i < motorNames.length; i++) {
            motors[i] = registerMotor(motorNames[i]);
        }
        //position.log("Drivetrain/position", config.robotModel); gives null pointer err
    }

    public static double findRegenerativeBrakingCoefficient(double A, double B, double E, double maxVelocity) {
        double bestD = 0;
        double bestError = Double.POSITIVE_INFINITY;

        // Search range. Adjust as needed.
        for (double D = 1e-6; D <= 10.0; D += 0.01) {

            double error = 0.0;

            for (double v = 0; v <= maxVelocity; v += 0.1) {
                double polynomial = A * v * v + B * v;

                double model;
                if (D < 1e-8) {
                    model = v / E;
                } else {
                    model = v / D
                            - (E / (D * D))
                            * Math.log1p(D * v / E);
                }

                double diff = polynomial - model;
                error += diff * diff;
            }

            if (error < bestError) {
                bestError = error;
                bestD = D;
            }
        }

        return bestD;
    }

    public SimMotor registerMotor(String name) {
        double kCoulombFriction = config.naturalDeceleration / config.wheelRadius;
        double backEMF = config.maxAcceleration / config.maxVelocity;
        double kA = (backEMF * (config.maxVelocity / config.wheelRadius) + kCoulombFriction) / config.nominalVoltage;

        double[] zeroPowerBrakeCoefficients = new double[]{
                kA, backEMF, regenerativeBraking, regenerativeBraking, kCoulombFriction
        };
        double[] motorCoefficients = new double[]{
                kA, backEMF, regenerativeBraking, 0, kCoulombFriction
        };

        MotorModel model = new MotorModel(
                (v,d,b) -> d*b,
                (v,d,b) -> Math.signum(v) == Math.signum(d) ? -v * Math.abs(d) : 0, // back-emf
                (v,d,b) -> Math.signum(v) != Math.signum(d) && d != 0 ? -v: 0,  // regenerative braking, not dependent on duty bc max braking is way stronger than max accel
                (v,d,b) -> d == 0 ? -v: 0,  // short circuiting brake mode
                (v,d,b) -> -Math.signum(v)
        );

        SimMotorConfig motorConfig = new SimMotorConfig(name, model, motorCoefficients, zeroPowerBrakeCoefficients, config.staticVelocityRegion/config.wheelRadius, config.staticFriction/config.wheelRadius, voltageSensor);
        return hardwareMap.motor(motorConfig);
    }

    public void update(double deltaTime) {
        boolean allMotorsStationary = true;
        for (int i = 0; i < motors.length; i++) {
            SimMotor motor = motors[i];
            motorAngularVelocities[i] = motor.getVelocity();
            // TODO CHANGE COEFFICIENTS / FRICTION WHEN LATERAL TO MAKE IT GO THE RIGHT MAX SPEED

//            double rhombusScale = Math.max(
//                    Math.abs(x) / maxForwardSpeed + Math.abs(y) / maxStrafeSpeed,
//                    1.0
//            );
//            x *= rhombusScale;
//            y *= rhombusScale;

            Logger.recordOutput("Drivetrain/angular vels radians per second/" + motor.deviceName, motor.getVelocity());
            Logger.recordOutput("Drivetrain/powers/" + motor.deviceName, motor.getPower());
            Logger.recordOutput("Drivetrain/angular accelerations radians per second per second/" + motor.deviceName, motor.getAcceleration());

            if (!motor.isStationary()) {
                allMotorsStationary = false;
            }
        }

        velocity = forwardKinematics(motorAngularVelocities).toFieldFrame(position.theta);

        if (allMotorsStationary) {
            velocity = new MotionVector(0, 0, 0);
        }

        position = position.step(velocity, deltaTime);

        velocity.log("Drivetrain/velocity");
        updateWheelRollVelocities();

        // TODO maybe make it more accurate by calculating rolling accel?

        position.log("Drivetrain/position", config.robotModel);

        collisionCheck();
    }

    MotionVector previousLegalPose = new MotionVector(0, 0, 0);

    public void collisionCheck() {
        Pose2D pose = getActualPose();
        RobotGeometry robot = config.robotGeometry;
        MotionVector currentPose = new MotionVector(pose.getX(DistanceUnit.INCH), pose.getY(DistanceUnit.INCH), pose.getHeading(AngleUnit.RADIANS));
        boolean isOutOfBounds = FieldBoundary.isOutOfBounds(currentPose, robot);

        if (isOutOfBounds) {
            MotionVector closest = FieldBoundary.closestInBoundsPosition(previousLegalPose, currentPose, robot);

            MotionVector correctionDir = currentPose.minus(closest);

            if (correctionDir.magnitude() > 1e-6) {

                MotionVector normal = correctionDir.unitVector();

                double vOut = velocity.dot(normal);

                MotionVector correctedVelocity = velocity.minus(normal.scale(vOut));

                setPosition(closest);
                setLinearVel(correctedVelocity);
            }
        }
        else {
            previousLegalPose = currentPose;
        }

        Logger.recordOutput("isInBounds", !isOutOfBounds);
        previousLegalPose.log("previousLegalPose");
    }

    public void setPosition(MotionVector position) {
        this.position = position;
    }

    public void setLinearVel(MotionVector velocity) {
        this.velocity = new MotionVector(velocity.x, velocity.y, this.velocity.theta);
        updateWheelRollVelocities();
    }

    public void updateWheelRollVelocities() {
        // Accounts for wheels moving from whole robot moving
        motorAngularVelocities = inverseKinematics(velocity.toRobotFrame(position.theta));
        for (int i = 0; i < motors.length; i++) {
            motors[i].setRollVelocity(motorAngularVelocities[i]);
        }
    }

    abstract MotionVector forwardKinematics(double[] motors);
    abstract double[] inverseKinematics(MotionVector motion);
}