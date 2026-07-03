package org.jjophoven.fakehardware.drivetrain;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.jjophoven.fakehardware.devices.FakeMotor;
import org.jjophoven.fakehardware.devices.FakeVoltageSensor;
import org.jjophoven.fakehardware.devices.FakeMotorConfig;
import org.jjophoven.fit.MotorModel;
import org.psilynx.psikit.core.Logger;

public abstract class SimulatedDrivetrain {
    private final FakeMotor[] motors;

    public MotionVector position = new MotionVector(0, 0, 0);
    public MotionVector velocity = new MotionVector(0, 0, 0);

    public DrivetrainConfig config;

    protected double[] motorAngularVelocities;

    public SimulatedDrivetrain(DrivetrainConfig config, String... motorNames) {
        this.config = config;
        this.motors = new FakeMotor[motorNames.length];
        for (int i = 0; i < motorNames.length; i++) {
            motors[i] = createMotor(motorNames[i]);
        }

        motorAngularVelocities = new double[motors.length];
    }

    public Pose2D getPose() {
        return new Pose2D(DistanceUnit.MM,
                position.x,
                position.y,
                AngleUnit.RADIANS,
                AngleUnit.normalizeRadians(position.theta));
    }

    public FakeMotor createMotor(String name) {
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

        FakeMotorConfig motorConfig = new FakeMotorConfig(name, MotorModel.fromString("a=Au-Bv*abs(d)-Cv-Dsgn(v)"), motorCoefficients, zeroPowerBrakeCoefficients, config.staticVelocityRegion/config.wheelRadius, config.staticFriction/config.wheelRadius, (FakeVoltageSensor) config.fakeHardwareMap.voltageSensor.iterator().next());
        return config.fakeHardwareMap.motor(motorConfig);
    }

    public void step(double deltaTime) {
        boolean allMotorsStationary = true;
        for (int i = 0; i < motors.length; i++) {
            //motors[i].step(deltaTime);

            FakeMotor motor = motors[i];
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