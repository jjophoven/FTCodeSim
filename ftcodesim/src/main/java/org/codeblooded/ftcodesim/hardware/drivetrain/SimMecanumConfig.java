package org.codeblooded.ftcodesim.hardware.drivetrain;

public class SimMecanumConfig extends SimDrivetrainConfig {
    public String frontLeftMotorName;
    public String frontRightMotorName;
    public String backLeftMotorName;
    public String backRightMotorName;

    /** distance from center of frontLeft wheel to backLeft wheel */
    public double wheelbase;

    /** distance from center of backRight wheel to backLeft wheel */
    public double trackWidth;

    public double strafeEfficiency;
}
