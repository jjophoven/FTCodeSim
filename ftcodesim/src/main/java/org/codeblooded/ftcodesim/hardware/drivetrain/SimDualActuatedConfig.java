package org.codeblooded.ftcodesim.hardware.drivetrain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Configuration for a drivetrain that actuates between mecanum and tank kinematics. */
public class SimDualActuatedConfig extends SimMecanumConfig {
    public enum DriveState { TANK, HOLONOMIC }

    public static final class Actuator {
        public final String servoName;
        public final double tankPosition;
        public final double holonomicPosition;

        private Actuator(String servoName, double tankPosition, double holonomicPosition) {
            this.servoName = servoName;
            this.tankPosition = tankPosition;
            this.holonomicPosition = holonomicPosition;
        }
    }

    public DriveState initialState = DriveState.HOLONOMIC;
    public double actuatorPositionTolerance = 1e-6;
    private final List<Actuator> actuators = new ArrayList<>();

    public SimDualActuatedConfig addActuator(String servoName, double tankPosition,
                                             double holonomicPosition) {
        if (servoName == null || servoName.trim().isEmpty()) {
            throw new IllegalArgumentException("Actuator servo name is required");
        }
        validatePosition(tankPosition, "tank");
        validatePosition(holonomicPosition, "holonomic");
        if (tankPosition == holonomicPosition) {
            throw new IllegalArgumentException("Actuator endpoints must be distinct");
        }
        for (Actuator actuator : actuators) {
            if (actuator.servoName.equals(servoName)) {
                throw new IllegalArgumentException("Duplicate actuator servo name: " + servoName);
            }
        }
        actuators.add(new Actuator(servoName, tankPosition, holonomicPosition));
        return this;
    }

    public List<Actuator> getActuators() { return Collections.unmodifiableList(actuators); }

    void validateDualActuated() {
        if (actuators.isEmpty()) throw new IllegalArgumentException("At least one actuator is required");
        if (initialState == null) throw new IllegalArgumentException("Initial drive state is required");
        if (!Double.isFinite(actuatorPositionTolerance) || actuatorPositionTolerance < 0) {
            throw new IllegalArgumentException("Actuator tolerance must be finite and nonnegative");
        }
        if (frontLeftMotorName == null || frontRightMotorName == null ||
                backLeftMotorName == null || backRightMotorName == null) {
            throw new IllegalArgumentException("Dual-actuated drive requires four motor names");
        }
        if (!Double.isFinite(trackWidth) || trackWidth <= 0 ||
                !Double.isFinite(wheelbase) || wheelbase <= 0 ||
                !Double.isFinite(wheelRadius) || wheelRadius <= 0) {
            throw new IllegalArgumentException("Dual-actuated geometry must be positive and finite");
        }
    }

    private static void validatePosition(double value, String label) {
        if (!Double.isFinite(value) || value < 0 || value > 1) {
            throw new IllegalArgumentException(label + " actuator position must be within [0, 1]");
        }
    }
}
