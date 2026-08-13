package org.codeblooded.ftcodesim.hardware.devices;

import com.qualcomm.hardware.digitalchickenlabs.OctoQuad;
import org.codeblooded.ftcodesim.ascope.boundaries.MotionVector;
import org.codeblooded.ftcodesim.hardware.drivetrain.SimulatedDrivetrain;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;

public class SimOctoquad implements OctoQuad {
    private final SimulatedDrivetrain drivetrain;

    public SimOctoquad(SimulatedDrivetrain drivetrain) {
        this.drivetrain = drivetrain;
    }

    @Override
    public byte getChipId() {
        return 0;
    }

    @Override
    public FirmwareVersion getFirmwareVersion() {
        return null;
    }

    @Override
    public String getFirmwareVersionString() {
        return "";
    }

    @Override
    public void setSingleEncoderDirection(int idx, EncoderDirection dir) {

    }

    @Override
    public EncoderDirection getSingleEncoderDirection(int idx) {
        return null;
    }

    @Override
    public void setAllEncoderDirections(boolean[] reverse) {

    }

    @Override
    public void setChannelBankConfig(ChannelBankConfig config) {

    }

    @Override
    public ChannelBankConfig getChannelBankConfig() {
        return null;
    }

    @Override
    public void readAllEncoderData(EncoderDataBlock out) {
        }

    @Override
    public EncoderDataBlock readAllEncoderData() {
        return null;
    }

    @Override
    public void setCachingMode(CachingMode mode) {

    }

    @Override
    public void refreshCache() {

    }

    @Override
    public int readSinglePosition_Caching(int idx) {
        return 0;
    }

    @Override
    public short readSingleVelocity_Caching(int idx) {
        return 0;
    }

    @Override
    public short readSingleVelocity(int idx) {
        return 0;
    }

    @Override
    public int readSinglePosition(int idx) {
        return 0;
    }

    @Override
    public void resetSinglePosition(int idx) {

    }

    @Override
    public void resetAllPositions() {

    }

    @Override
    public void resetMultiplePositions(boolean[] resets) {

    }

    @Override
    public void resetMultiplePositions(int... indices) {

    }

    @Override
    public void setSingleVelocitySampleInterval(int idx, int intvlms) {

    }

    @Override
    public int getSingleVelocitySampleInterval(int idx) {
        return 0;
    }

    @Override
    public void setAllVelocitySampleIntervals(int intvlms) {

    }

    @Override
    public void setSingleChannelPulseWidthParams(int idx, int min_length_us, int max_length_us) {

    }

    @Override
    public void setSingleChannelPulseWidthParams(int idx, ChannelPulseWidthParams params) {

    }

    @Override
    public ChannelPulseWidthParams getSingleChannelPulseWidthParams(int idx) {
        return null;
    }

    @Override
    public void setSingleChannelPulseWidthTracksWrap(int idx, boolean trackWrap) {

    }

    @Override
    public boolean getSingleChannelPulseWidthTracksWrap(int idx) {
        return false;
    }

    @Override
    public void setAllChannelsPulseWidthTracksWrap(boolean[] trackWrap) {

    }

    @Override
    public void setLocalizerPortX(int port) {

    }

    @Override
    public void setLocalizerPortY(int port) {

    }

    @Override
    public void setLocalizerCountsPerMM_X(float ticksPerMM_x) {

    }

    @Override
    public void setLocalizerCountsPerMM_Y(float ticksPerMM_y) {

    }

    @Override
    public void setLocalizerTcpOffsetMM_X(float tcpOffsetMM_X) {

    }

    @Override
    public void setLocalizerTcpOffsetMM_Y(float tcpOffsetMM_Y) {

    }

    @Override
    public void setLocalizerImuHeadingScalar(float headingScalar) {

    }

    @Override
    public void setLocalizerVelocityIntervalMS(int ms) {

    }

    @Override
    public void setAllLocalizerParameters(int portX, int portY, float ticksPerMM_x, float ticksPerMM_y, float tcpOffsetMM_X, float tcpOffsetMM_Y, float headingScalar, int velocityIntervalMs) {

    }

    @Override
    public void readLocalizerData(LocalizerDataBlock out) {
        out.crcOk = true;
        Pose2D pose = drivetrain.getActualPose();
        out.heading_rad = (float) pose.getHeading(AngleUnit.RADIANS);
        out.posX_mm = (short) pose.getX(DistanceUnit.MM);
        out.posY_mm = (short) pose.getY(DistanceUnit.MM);
        Pose2D vel = drivetrain.getVelocityPose();
        out.velX_mmS = (short) vel.getX(DistanceUnit.MM);
        out.velY_mmS = (short) vel.getY(DistanceUnit.MM);
        out.velHeading_radS = (float) vel.getHeading(AngleUnit.RADIANS);
        out.localizerStatus = LocalizerStatus.RUNNING;
    }

    @Override
    public LocalizerDataBlock readLocalizerData() {
        return null;
    }

    @Override
    public void readLocalizerDataAndAllEncoderData(LocalizerDataBlock localizerOut, EncoderDataBlock encoderOut) {

    }

    @Override
    public void setLocalizerPose(int posX_mm, int posY_mm, float heading_rad) {
        drivetrain.setPosition(new MotionVector(posX_mm / 25.4, posY_mm / 25.4, heading_rad));
    }

    @Override
    public void setLocalizerHeading(float heading_rad) {

    }

    @Override
    public LocalizerStatus getLocalizerStatus() {
        return LocalizerStatus.RUNNING;
    }

    @Override
    public LocalizerYawAxis getLocalizerHeadingAxisChoice() {
        return null;
    }

    @Override
    public void resetLocalizerAndCalibrateIMU() {

    }

    @Override
    public void setI2cRecoveryMode(I2cRecoveryMode mode) {

    }

    @Override
    public I2cRecoveryMode getI2cRecoveryMode() {
        return null;
    }

    @Override
    public void saveParametersToFlash() {

    }

    @Override
    public void resetEverything() {

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
}
