package org.codeblooded.ftcodesim.hardware.drivetrain;

import com.qualcomm.robotcore.hardware.HardwareDevice;

/** Interactive harness that selects mecanum, tank, or servo-controlled dual physics at runtime. */
public class SimulatedSelectableDrivetrain extends SimulatedDualActuated implements HardwareDevice {
    public enum DriveType { MECANUM, TANK, DUAL_ACTUATED }

    private DriveType driveType = DriveType.MECANUM;

    public SimulatedSelectableDrivetrain(SimDualActuatedConfig config) { super(config); }

    public DriveType getDriveType() { return driveType; }

    public void setDriveType(DriveType driveType) {
        if (driveType == null) throw new IllegalArgumentException("Drive type cannot be null");
        this.driveType = driveType;
        if (hardwareMap != null) updateWheelRollVelocities();
    }

    public void cycleDriveType() {
        DriveType[] values = DriveType.values();
        setDriveType(values[(driveType.ordinal() + 1) % values.length]);
    }

    @Override
    protected boolean usesTankKinematics() {
        if (driveType == DriveType.TANK) return true;
        if (driveType == DriveType.MECANUM) return false;
        return super.usesTankKinematics();
    }

    @Override
    protected void constrainVelocity() {
        if (driveType == DriveType.TANK) {
            constrainToLongitudinalVelocity();
        } else if (driveType == DriveType.DUAL_ACTUATED) {
            super.constrainVelocity();
        }
    }

    public double[] getMotorPowers() {
        double[] powers = new double[motors.length];
        for (int i = 0; i < motors.length; i++) powers[i] = motors[i].getPower();
        return powers;
    }

    @Override public Manufacturer getManufacturer() { return Manufacturer.Other; }
    @Override public String getDeviceName() { return "FTCodeSim Selectable Drivetrain"; }
    @Override public String getConnectionInfo() { return "simulated"; }
    @Override public int getVersion() { return 1; }
    @Override public void resetDeviceConfigurationForOpMode() { }
    @Override public void close() { }
}
