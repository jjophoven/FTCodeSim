package org.jjophoven.simhardware.devices;

import androidx.annotation.Nullable;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.I2cAddr;
import com.qualcomm.robotcore.hardware.I2cDeviceSynchSimple;
import com.qualcomm.robotcore.hardware.I2cWaitControl;
import com.qualcomm.robotcore.hardware.TimestampedData;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.jjophoven.simhardware.drivetrain.MotionVector;
import org.jjophoven.simhardware.drivetrain.SimulatedDrivetrain;

public class SimGobildaPinpoint extends GoBildaPinpointDriver implements SimHardwareDevice {
    private final SimulatedDrivetrain drivetrain;

    public SimGobildaPinpoint(SimulatedDrivetrain drivetrain) {
        super(new FakeI2C(), false);
        this.drivetrain = drivetrain;
    }

    @Override
    public void update(double deltaTime) {
        pose2D = drivetrain.getActualPose(); // TODO add optional noise
    }

    @Override
    public void setPosition(Pose2D pos) {
        pose2D = pos;
        drivetrain.position = new MotionVector(pos.getX(DistanceUnit.INCH), pos.getY(DistanceUnit.INCH), pos.getHeading(AngleUnit.RADIANS));
    }

    @Override
    protected synchronized boolean doInitialize() {
        return true;
    }

    public Pose2D pose2D;

    public Pose2D getPosition() {
        return pose2D;
    }
}

class FakeI2C implements I2cDeviceSynchSimple {

    @Override
    public byte read8() {
        return 0;
    }

    @Override
    public byte read8(int ireg) {
        return 0;
    }

    @Override
    public byte[] read(int creg) {
        return new byte[0];
    }

    @Override
    public byte[] read(int ireg, int creg) {
        return new byte[0];
    }

    @Override
    public TimestampedData readTimeStamped(int creg) {
        return null;
    }

    @Override
    public TimestampedData readTimeStamped(int ireg, int creg) {
        return null;
    }

    @Override
    public void write8(int bVal) {

    }

    @Override
    public void write8(int ireg, int bVal) {

    }

    @Override
    public void write(byte[] data) {

    }

    @Override
    public void write(int ireg, byte[] data) {

    }

    @Override
    public void write8(int bVal, I2cWaitControl waitControl) {

    }

    @Override
    public void write8(int ireg, int bVal, I2cWaitControl waitControl) {

    }

    @Override
    public void write(byte[] data, I2cWaitControl waitControl) {

    }

    @Override
    public void write(int ireg, byte[] data, I2cWaitControl waitControl) {

    }

    @Override
    public void waitForWriteCompletions(I2cWaitControl waitControl) {

    }

    @Override
    public void enableWriteCoalescing(boolean enable) {

    }

    @Override
    public boolean isWriteCoalescingEnabled() {
        return false;
    }

    @Override
    public boolean isArmed() {
        return false;
    }

    @Override
    public void setI2cAddr(I2cAddr i2cAddr) {

    }

    @Override
    public I2cAddr getI2cAddr() {
        return null;
    }

    @Override
    public void setLogging(boolean enabled) {

    }

    @Override
    public boolean getLogging() {
        return false;
    }

    @Override
    public void setLoggingTag(String loggingTag) {

    }

    @Override
    public String getLoggingTag() {
        return "";
    }

    @Override
    public void setUserConfiguredName(@Nullable String name) {

    }

    @Nullable
    @Override
    public String getUserConfiguredName() {
        return "";
    }

    @Override
    public Manufacturer getManufacturer() {
        return null;
    }

    @Override
    public String getDeviceName() {
        return "";
    }

    @Override
    public String getConnectionInfo() {
        return "";
    }

    @Override
    public int getVersion() {
        return 0;
    }

    @Override
    public void resetDeviceConfigurationForOpMode() {

    }

    @Override
    public void close() {

    }

    @Override
    public void setHealthStatus(HealthStatus status) {

    }

    @Override
    public HealthStatus getHealthStatus() {
        return null;
    }

    @Override
    public void setI2cAddress(I2cAddr newAddress) {

    }

    @Override
    public I2cAddr getI2cAddress() {
        return null;
    }
}