package org.jjophoven.fakehardware.drivetrain;

import org.jjophoven.fakehardware.FakeMotor;
import org.jjophoven.fit.MotorModel;
import org.psilynx.psikit.core.Logger;

public abstract class SimulatedDrivetrain {
    private final FakeMotor[] motors;

    public MotionVector position = new MotionVector(0, 0, 0); // model start heading
    public MotionVector velocity = new MotionVector(0, 0, 0);

    protected double[] motorAngularVelocities;
    protected MotorModel model;
    protected double[] coefficients;

    public SimulatedDrivetrain(FakeMotor[] motors, MotorModel model, double[] coefficients) {
        this.motors = motors;

        motorAngularVelocities = new double[motors.length];
        this.model = model;
        this.coefficients = coefficients;
    }

    public void step(double deltaTime) {
        boolean allMotorsStationary = true;
        for (int i = 0; i < motors.length; i++) {
            motors[i].step(deltaTime);

            FakeMotor motor = motors[i];
            motorAngularVelocities[i] = motor.getVelocity();

            Logger.recordOutput("Mecanum/angular vels/" + motor.deviceName, motor.getVelocity());
            Logger.recordOutput("Mecanum/powers/" + motor.deviceName, motor.getPower());
            Logger.recordOutput("Mecanum/angular accelerations/" + motor.deviceName, motor.getAcceleration());

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

       //  Accounts for wheels moving from whole robot moving
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