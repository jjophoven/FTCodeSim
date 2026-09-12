package org.codeblooded.ftcodesim.hardware.drivetrain;

import org.codeblooded.ftcodesim.ascope.boundaries.MotionVector;
import org.codeblooded.ftcodesim.hardware.SimHardwareMap;
import org.codeblooded.ftcodesim.hardware.devices.SimServo;

import java.util.ArrayList;
import java.util.List;

/** Four-motor drivetrain that switches kinematics from configured servo commands. */
public class SimulatedDualActuated extends SimulatedMecanum {
    protected final SimDualActuatedConfig dualConfig;
    private final List<SimServo> actuators = new ArrayList<>();
    private SimDualActuatedConfig.DriveState driveState;

    public SimulatedDualActuated(SimDualActuatedConfig config) {
        super(validated(config));
        dualConfig = config;
        driveState = config.initialState;
    }

    private static SimDualActuatedConfig validated(SimDualActuatedConfig config) {
        if (config == null) throw new IllegalArgumentException("Dual-actuated config is required");
        config.validateDualActuated();
        return config;
    }

    @Override
    public void registerDevices(SimHardwareMap hardwareMap) {
        super.registerDevices(hardwareMap);
        for (SimDualActuatedConfig.Actuator actuator : dualConfig.getActuators()) {
            SimServo servo = hardwareMap.servo(actuator.servoName);
            actuators.add(servo);
            servo.setPosition(driveState == SimDualActuatedConfig.DriveState.TANK
                    ? actuator.tankPosition : actuator.holonomicPosition);
        }
    }

    @Override
    public void update(double deltaTime) {
        updateStateFromActuators();
        super.update(deltaTime);
    }

    protected void updateStateFromActuators() {
        if (actuators.size() != dualConfig.getActuators().size()) return;
        boolean allTank = true;
        boolean allHolonomic = true;
        for (int i = 0; i < actuators.size(); i++) {
            double position = actuators.get(i).getPosition();
            SimDualActuatedConfig.Actuator config = dualConfig.getActuators().get(i);
            allTank &= near(position, config.tankPosition);
            allHolonomic &= near(position, config.holonomicPosition);
        }
        if (allTank) driveState = SimDualActuatedConfig.DriveState.TANK;
        else if (allHolonomic) driveState = SimDualActuatedConfig.DriveState.HOLONOMIC;
    }

    private boolean near(double a, double b) {
        return Math.abs(a - b) <= dualConfig.actuatorPositionTolerance;
    }

    public SimDualActuatedConfig.DriveState getDriveState() { return driveState; }

    public void setDriveState(SimDualActuatedConfig.DriveState state) {
        if (state == null) throw new IllegalArgumentException("Drive state cannot be null");
        driveState = state;
        for (int i = 0; i < actuators.size(); i++) {
            SimDualActuatedConfig.Actuator config = dualConfig.getActuators().get(i);
            actuators.get(i).setPosition(state == SimDualActuatedConfig.DriveState.TANK
                    ? config.tankPosition : config.holonomicPosition);
        }
        if (hardwareMap != null) updateWheelRollVelocities();
    }

    public void toggleDriveState() {
        setDriveState(driveState == SimDualActuatedConfig.DriveState.TANK
                ? SimDualActuatedConfig.DriveState.HOLONOMIC
                : SimDualActuatedConfig.DriveState.TANK);
    }

    protected boolean usesTankKinematics() {
        return driveState == SimDualActuatedConfig.DriveState.TANK;
    }

    public boolean isTankKinematics() { return usesTankKinematics(); }

    @Override
    protected void integratePosition(double deltaTime) {
        if (usesTankKinematics()) integrateLongitudinalPosition(deltaTime);
        else super.integratePosition(deltaTime);
    }

    @Override
    protected void constrainVelocity() {
        if (usesTankKinematics()) constrainToLongitudinalVelocity();
    }

    @Override
    MotionVector forwardKinematics(double[] motors) {
        if (!usesTankKinematics()) return super.forwardKinematics(motors);
        double radius = dualConfig.wheelRadius;
        double left = (motors[0] + motors[2]) * radius / 2.0;
        double right = (motors[1] + motors[3]) * radius / 2.0;
        return new MotionVector((left + right) / 2.0, 0.0,
                (right - left) / dualConfig.trackWidth);
    }

    @Override
    double[] inverseKinematics(MotionVector motion) {
        if (!usesTankKinematics()) return super.inverseKinematics(motion);
        double left = motion.x - motion.theta * dualConfig.trackWidth / 2.0;
        double right = motion.x + motion.theta * dualConfig.trackWidth / 2.0;
        return new double[]{left / dualConfig.wheelRadius, right / dualConfig.wheelRadius,
                left / dualConfig.wheelRadius, right / dualConfig.wheelRadius};
    }
}
