package org.codeblooded.ftcodesim.hardware.drivetrain;

public class SimTankConfig extends SimDrivetrainConfig {
    public String frontLeftMotorName;
    public String frontRightMotorName;
    public String backLeftMotorName;
    public String backRightMotorName;
    public double trackWidth;

    String[] motorNames() {
        if (isBlank(frontLeftMotorName) || isBlank(frontRightMotorName)) {
            throw new IllegalArgumentException("Tank drive requires both front motor names");
        }
        boolean hasBackLeft = !isBlank(backLeftMotorName);
        boolean hasBackRight = !isBlank(backRightMotorName);
        if (hasBackLeft != hasBackRight) {
            throw new IllegalArgumentException("Tank drive rear motors must be configured as a pair");
        }
        if (!Double.isFinite(trackWidth) || trackWidth <= 0) {
            throw new IllegalArgumentException("Tank track width must be positive and finite");
        }
        if (!Double.isFinite(wheelRadius) || wheelRadius <= 0) {
            throw new IllegalArgumentException("Tank wheel radius must be positive and finite");
        }
        return hasBackLeft
                ? new String[]{frontLeftMotorName, frontRightMotorName,
                    backLeftMotorName, backRightMotorName}
                : new String[]{frontLeftMotorName, frontRightMotorName};
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
