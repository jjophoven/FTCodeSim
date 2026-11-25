package org.codeblooded.ftcodesim.hardware.drivetrain;

import com.qualcomm.robotcore.hardware.*;
import com.qualcomm.robotcore.hardware.configuration.typecontainers.MotorConfigurationType;
import org.codeblooded.ftcodesim.hardware.devices.SimHardwareDevice;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;

public class SimDrivetrainMotor implements DcMotorEx, SimHardwareDevice {

    private double power;
    private double velocity;
    private ZeroPowerBehavior zeroPowerBehavior = ZeroPowerBehavior.FLOAT;

    @Override
    public void setPower(double power) {
        this.power = Math.max(-1, Math.min(1, power));
    }

    @Override
    public double getPower() {
        return power;
    }

    @Override
    public void setMotorEnable() {

    }

    @Override
    public void setMotorDisable() {

    }

    @Override
    public boolean isMotorEnabled() {
        return false;
    }

    public void setVelocity(double velocity) {
        this.velocity = velocity;
    }

    @Override
    public void setVelocity(double angularRate, AngleUnit unit) {

    }

    public double getVelocity() {
        return velocity;
    }

    @Override
    public double getVelocity(AngleUnit unit) {
        return 0;
    }

    @Override
    public void setPIDCoefficients(RunMode mode, PIDCoefficients pidCoefficients) {

    }

    @Override
    public void setPIDFCoefficients(RunMode mode, PIDFCoefficients pidfCoefficients) throws UnsupportedOperationException {

    }

    @Override
    public void setVelocityPIDFCoefficients(double p, double i, double d, double f) {

    }

    @Override
    public void setPositionPIDFCoefficients(double p) {

    }

    @Override
    public PIDCoefficients getPIDCoefficients(RunMode mode) {
        return null;
    }

    @Override
    public PIDFCoefficients getPIDFCoefficients(RunMode mode) {
        return null;
    }

    @Override
    public void setTargetPositionTolerance(int tolerance) {

    }

    @Override
    public int getTargetPositionTolerance() {
        return 0;
    }

    @Override
    public double getCurrent(CurrentUnit unit) {
        return 0;
    }

    @Override
    public double getCurrentAlert(CurrentUnit unit) {
        return 0;
    }

    @Override
    public void setCurrentAlert(double current, CurrentUnit unit) {

    }

    @Override
    public boolean isOverCurrent() {
        return false;
    }

    @Override
    public void setZeroPowerBehavior(ZeroPowerBehavior behavior) {
        this.zeroPowerBehavior = behavior;
    }

    @Override
    public ZeroPowerBehavior getZeroPowerBehavior() {
        return zeroPowerBehavior;
    }

    @Override
    public void setPowerFloat() {
        zeroPowerBehavior = ZeroPowerBehavior.FLOAT;
        setPower(0);
    }

    @Override
    public boolean getPowerFloat() {
        return power == 0 && zeroPowerBehavior == ZeroPowerBehavior.FLOAT;
    }

    public boolean isBraking() {
        return (power == 0 && zeroPowerBehavior == ZeroPowerBehavior.BRAKE)
                || power * velocity < 0;
    }

    @Override
    public void update(double deltaTime) {}

    @Override
    public void reset() {
        power = 0;
        velocity = 0;
        zeroPowerBehavior = ZeroPowerBehavior.FLOAT;
    }

    @Override
    public void setDirection(Direction direction) {}

    @Override
    public Direction getDirection() {
        return Direction.FORWARD;
    }

    @Override
    public void setMode(RunMode mode) {}

    @Override
    public RunMode getMode() {
        return RunMode.RUN_WITHOUT_ENCODER;
    }

    @Override
    public void setTargetPosition(int position) {}

    @Override
    public int getTargetPosition() {
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        return 0;
    }

    @Override
    public boolean isBusy() {
        return false;
    }

    @Override
    public MotorConfigurationType getMotorType() {
        return MotorConfigurationType.getUnspecifiedMotorType();
    }

    @Override
    public void setMotorType(MotorConfigurationType motorType) {}

    @Override
    public DcMotorController getController() {
        return null;
    }

    @Override
    public int getPortNumber() {
        return -1;
    }

    @Override
    public Manufacturer getManufacturer() {
        return Manufacturer.Other;
    }

    @Override
    public String getDeviceName() {
        return "SimDrivetrainMotor";
    }

    @Override
    public String getConnectionInfo() {
        return "Simulated drivetrain";
    }

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public void resetDeviceConfigurationForOpMode() {
        reset();
    }

    @Override
    public void close() {}
}