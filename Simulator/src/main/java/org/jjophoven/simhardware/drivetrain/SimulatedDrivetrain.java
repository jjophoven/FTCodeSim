package org.jjophoven.simhardware.drivetrain;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.jjophoven.simhardware.devices.SimMotor;
import org.jjophoven.simhardware.devices.SimVoltageSensor;
import org.jjophoven.simhardware.devices.SimMotorConfig;
import org.jjophoven.fit.MotorModel;
import org.psilynx.psikit.core.Logger;

public abstract class SimulatedDrivetrain {
    private final SimMotor[] motors;

    public MotionVector position = new MotionVector(0, 0, 0);
    public MotionVector velocity = new MotionVector(0, 0, 0);

    public SimDrivetrainConfig config;

    protected double[] motorAngularVelocities;

    public SimulatedDrivetrain(SimDrivetrainConfig config, String... motorNames) {
        this.config = config;
        this.motors = new SimMotor[motorNames.length];
        for (int i = 0; i < motorNames.length; i++) {
            motors[i] = createMotor(motorNames[i]);
        }

        motorAngularVelocities = new double[motors.length];
    }

    public Pose2D getActualPose() {
        return position.toPose2D();
    }

    public SimMotor createMotor(String name) {
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

        SimMotorConfig motorConfig = new SimMotorConfig(name, MotorModel.fromString("a=Au-Bv*abs(d)-Cv-Dsgn(v)"), motorCoefficients, zeroPowerBrakeCoefficients, config.staticVelocityRegion/config.wheelRadius, config.staticFriction/config.wheelRadius, (SimVoltageSensor) config.simHardwareMap.voltageSensor.iterator().next());
        return config.simHardwareMap.motor(motorConfig);
    }

    // TODO setVelocity for collisions

    public void step(double deltaTime) {
        boolean allMotorsStationary = true;
        for (int i = 0; i < motors.length; i++) {
            //motors[i].step(deltaTime);

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

        // Accounts for wheels moving from whole robot moving
        motorAngularVelocities = inverseKinematics(velocity.toRobotFrame(position.theta));
        for (int i = 0; i < motors.length; i++) {
            motors[i].setRollVelocity(motorAngularVelocities[i]);
        }

        // TODO maybe make it more accurate by calculating rolling accel?

        position.log("Mecanum/position");
    }

    abstract MotionVector forwardKinematics(double[] motors);
    abstract double[] inverseKinematics(MotionVector motion);
}