package org.codeblooded.ftcodesim.hardware.drivetrain;

import org.codeblooded.ftcodesim.hardware.SimHardwareMap;
import org.codeblooded.ftcodesim.hardware.devices.*;
import org.codeblooded.ftcodesim.physics.FieldBoundary;
import org.codeblooded.ftcodesim.physics.MotionVector;
import org.codeblooded.ftcodesim.physics.RobotGeometry;
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

    protected double[] motorAngularVelocities;

    public SimulatedDrivetrain(SimDrivetrainConfig config, String... motorNames) {
        this.config = config;
        this.motors = new SimMotor[motorNames.length];
        this.motorNames = motorNames;

        motorAngularVelocities = new double[motors.length];
    }

    public Pose2D getActualPose() {
        return position.toPose2D();
    }

    public void registerDevices(SimHardwareMap hardwareMap) {
        this.hardwareMap = hardwareMap;
        this.voltageSensor = (SimVoltageSensor) hardwareMap.voltageSensor.iterator().next();
        for (int i = 0; i < motorNames.length; i++) {
            motors[i] = registerMotor(motorNames[i]);
        }
    }

    public SimMotor registerMotor(String name) {
        double maxOmega = config.maxVelocity / config.wheelRadius;
        double maxAlpha = config.maxAcceleration / config.wheelRadius;
        double naturalAlpha = config.naturalDeceleration / config.wheelRadius;

        double kA = (maxAlpha + naturalAlpha) / config.nominalVoltage;
        double kBackEMF = maxAlpha / maxOmega;
        double kCoulombFriction = config.naturalDeceleration / config.wheelRadius;

        double[] zeroPowerBrakeCoefficients  = new double[]{
                    kA, 0, kBackEMF, kCoulombFriction
        };
        double[] motorCoefficients = new double[]{
                kA, kBackEMF, 0, kCoulombFriction
        };

        SimMotorConfig motorConfig = new SimMotorConfig(name, MotorModel.fromString("a=Au-Bv*abs(d)-Cv-Dsgn(v)"), motorCoefficients, zeroPowerBrakeCoefficients, config.staticVelocityRegion/config.wheelRadius, config.staticFriction/config.wheelRadius, voltageSensor);
        return hardwareMap.motor(motorConfig);
    }

    public void update(double deltaTime) {
        boolean allMotorsStationary = true;
        for (int i = 0; i < motors.length; i++) {
            SimMotor motor = motors[i];
            motorAngularVelocities[i] = motor.getVelocity();

            Logger.recordOutput("Mecanum/angular vels radians per second/" + motor.deviceName, motor.getVelocity());
            Logger.recordOutput("Mecanum/powers/" + motor.deviceName, motor.getPower());
            Logger.recordOutput("Mecanum/angular accelerations radians per second per second/" + motor.deviceName, motor.getAcceleration());

            if (!motor.isStationary()) {
                allMotorsStationary = false;
            }
        }

        velocity = forwardKinematics(motorAngularVelocities).toFieldFrame(position.theta);

        if (allMotorsStationary) {
            velocity = new MotionVector(0, 0, 0);
        }

        position = position.step(velocity, deltaTime);

        velocity.log("Mecanum/velocity");
        updateWheelRollVelocities();

        // TODO maybe make it more accurate by calculating rolling accel?

        position.log("Mecanum/position");

        collisionCheck();
    }

    MotionVector previousLegalPose = new MotionVector(0, 0, 0);

    public void collisionCheck() {
        Pose2D pose = getActualPose();
        RobotGeometry robot = config.robotGeometry;
        MotionVector currentPose = MotionVector.fromPose2D(pose);
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