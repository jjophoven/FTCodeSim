package org.codeblooded.ftcodesim.hardware.devices;

import com.qualcomm.robotcore.hardware.*;
import com.qualcomm.robotcore.hardware.configuration.typecontainers.MotorConfigurationType;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;

import java.util.HashSet;
import java.util.Set;

public class SimMotor implements DcMotorEx, SimHardwareDevice {

    private static final double NOMINAL_VOLTAGE = 12.0;
    private static final double STOP_RPM = 0.01;

    // Motor response time constants (seconds)
    public double accelerationTimeConstant = 0.25;
    public double coastTimeConstant = 1.25;
    public double brakeTimeConstant = 0.25;

    private final double maxRPM;
    private final VoltageSensor voltageSensor;

    private double rpm;
    private double revolutions;
    private double encoderOffset;
    private double power;

    private boolean enabled = true;

    private int targetPosition;
    private int targetPositionTolerance = 5;

    private Direction direction = Direction.FORWARD;
    private RunMode runMode = RunMode.RUN_WITHOUT_ENCODER;
    private ZeroPowerBehavior zeroPowerBehavior = ZeroPowerBehavior.FLOAT;

    private MotorConfigurationType motorType;

    private final Set<String> warnings = new HashSet<>();

    public SimMotor(double maxRPM, SimVoltageSensor voltageSensor) {
        this(maxRPM, 28, voltageSensor);
    }

    public SimMotor(
            double maxRPM,
            double ticksPerRev,
            SimVoltageSensor voltageSensor
    ) {
        this.maxRPM = maxRPM;
        this.voltageSensor = voltageSensor;

        motorType = MotorConfigurationType
                .getUnspecifiedMotorType()
                .clone();

        motorType.setMaxRPM(maxRPM);
        motorType.setTicksPerRev(ticksPerRev);
    }

    // ==================== PHYSICS ====================

    @Override
    public void update(double deltaTime) {
        if (deltaTime <= 0 || !Double.isFinite(deltaTime)) return;

        double voltage = Math.max(0, voltageSensor.getVoltage());
        double availableRPM = maxRPM * voltage / NOMINAL_VOLTAGE;

        // Substep to handle direction changes and position control.
        while (deltaTime > 0) {
            double dt = Math.min(deltaTime, 0.005);
            updatePhysics(dt, availableRPM);
            deltaTime -= dt;
        }
    }

    private void updatePhysics(double dt, double availableRPM) {
        double appliedPower = getAppliedPower(availableRPM);
        double targetRPM = appliedPower * availableRPM;

        double tau;

        if (!enabled) {
            tau = coastTimeConstant;

        } else if (appliedPower == 0) {
            tau = zeroPowerBehavior == ZeroPowerBehavior.BRAKE
                    || runMode == RunMode.RUN_TO_POSITION
                    ? brakeTimeConstant
                    : coastTimeConstant;

        } else if (appliedPower * rpm < 0) {
            // Active braking, regardless of power magnitude.
            tau = brakeTimeConstant;

        } else if (Math.abs(targetRPM) < Math.abs(rpm)) {
            // Reduced power in the same direction.
            // Near-zero power behaves similarly to coasting.
            tau = coastTimeConstant
                    + (accelerationTimeConstant - coastTimeConstant)
                    * Math.abs(appliedPower);

        } else {
            tau = accelerationTimeConstant;
        }

        // Analytical integration of:
        // dRPM/dt = (targetRPM - rpm) / tau

        double previousRPM = rpm;
        double decay = Math.exp(-dt / tau);

        rpm = targetRPM + (previousRPM - targetRPM) * decay;

        // Integrate RPM to obtain shaft revolutions.
        revolutions += (
                targetRPM * dt
                        + (previousRPM - targetRPM) * tau * (1 - decay)
        ) / 60.0;

        // Prevent an infinitely small coasting velocity.
        if (targetRPM == 0 && Math.abs(rpm) < STOP_RPM) {
            rpm = 0;
        }

        if (runMode == RunMode.STOP_AND_RESET_ENCODER) {
            encoderOffset = revolutions;
        }
    }

    private double getAppliedPower(double availableRPM) {
        if (!enabled || runMode == RunMode.STOP_AND_RESET_ENCODER) {
            return 0;
        }

        if (runMode == RunMode.RUN_TO_POSITION) {
            if (availableRPM <= 0) return 0;

            double error = targetPosition - getEncoderPosition();

            if (Math.abs(error) <= targetPositionTolerance) {
                return 0;
            }

            double errorRevolutions = error / motorType.getTicksPerRev();

            // Approach the target gradually to reduce overshoot.
            double desiredRPM =
                    errorRevolutions * 60 / (4 * brakeTimeConstant);

            return clamp(
                    desiredRPM / availableRPM,
                    -Math.abs(power),
                    Math.abs(power)
            ) * directionSign();
        }

        return power * directionSign();
    }

    // Physical shaft RPM, unaffected by SDK direction convention.
    public double getRawRPM() {
        return rpm;
    }

    public double getRawRevolutions() {
        return revolutions;
    }

    public double getRPM() {
        return rpm * directionSign();
    }

    public double getMaxRPM() {
        return maxRPM;
    }

    private double directionSign() {
        return direction == Direction.FORWARD ? 1 : -1;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    // ==================== POWER ====================

    @Override
    public void setPower(double power) {
        this.power = clamp(power, -1, 1);
    }

    @Override
    public double getPower() {
        return power;
    }

    @Override
    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    @Override
    public Direction getDirection() {
        return direction;
    }

    @Override
    public void setZeroPowerBehavior(ZeroPowerBehavior behavior) {
        if (behavior == ZeroPowerBehavior.UNKNOWN) {
            unsupported("ZeroPowerBehavior.UNKNOWN");
            return;
        }

        zeroPowerBehavior = behavior;
    }

    @Override
    public ZeroPowerBehavior getZeroPowerBehavior() {
        return zeroPowerBehavior;
    }

    @Override
    public void setPowerFloat() {
        zeroPowerBehavior = ZeroPowerBehavior.FLOAT;
        power = 0;
    }

    @Override
    public boolean getPowerFloat() {
        return power == 0
                && zeroPowerBehavior == ZeroPowerBehavior.FLOAT;
    }

    private double getEncoderPosition() {
        return (revolutions - encoderOffset)
                * motorType.getTicksPerRev()
                * directionSign();
    }

    @Override
    public int getCurrentPosition() {
        return (int) Math.round(getEncoderPosition());
    }

    @Override
    public double getVelocity() {
        return getRPM() / 60.0 * motorType.getTicksPerRev();
    }

    @Override
    public double getVelocity(AngleUnit unit) {
        return unit == AngleUnit.DEGREES
                ? getRPM() * 6
                : getRPM() * Math.PI / 30;
    }

    @Override
    public void setMode(RunMode mode) {
        switch (mode) {
            case RUN_USING_ENCODER:
            case RUN_USING_ENCODERS:
                unsupported("RUN_USING_ENCODER");
                runMode = RunMode.RUN_WITHOUT_ENCODER;
                break;

            case STOP_AND_RESET_ENCODER:
            case RESET_ENCODERS:
                power = 0;
                encoderOffset = revolutions;
                runMode = RunMode.STOP_AND_RESET_ENCODER;
                break;

            case RUN_WITHOUT_ENCODERS:
                runMode = RunMode.RUN_WITHOUT_ENCODER;
                break;

            default:
                runMode = mode;
                break;
        }
    }

    @Override
    public RunMode getMode() {
        return runMode;
    }

    @Override
    public void setTargetPosition(int position) {
        targetPosition = position;
    }

    @Override
    public int getTargetPosition() {
        return targetPosition;
    }

    @Override
    public void setTargetPositionTolerance(int tolerance) {
        targetPositionTolerance = Math.max(0, tolerance);
    }

    @Override
    public int getTargetPositionTolerance() {
        return targetPositionTolerance;
    }

    @Override
    public boolean isBusy() {
        return runMode == RunMode.RUN_TO_POSITION
                && Math.abs(targetPosition - getEncoderPosition())
                > targetPositionTolerance;
    }

    @Override
    public MotorConfigurationType getMotorType() {
        return motorType;
    }

    @Override
    public void setMotorType(MotorConfigurationType motorType) {
        this.motorType = motorType.clone();
        this.motorType.setMaxRPM(maxRPM);
    }

    @Override
    public void setMotorEnable() {
        enabled = true;
    }

    @Override
    public void setMotorDisable() {
        enabled = false;
    }

    @Override
    public boolean isMotorEnabled() {
        return enabled;
    }

    // ==================== HARDWARE DEVICE ====================

    @Override
    public void reset() {
        power = 0;
        rpm = 0;
        revolutions = 0;
        encoderOffset = 0;
        targetPosition = 0;
        targetPositionTolerance = 5;

        enabled = true;
        direction = Direction.FORWARD;
        runMode = RunMode.RUN_WITHOUT_ENCODER;
        zeroPowerBehavior = ZeroPowerBehavior.FLOAT;
    }

    @Override
    public void resetDeviceConfigurationForOpMode() {
        reset();
    }

    @Override
    public void close() {
        power = 0;
        enabled = false;
    }

    @Override
    public Manufacturer getManufacturer() {
        return Manufacturer.Other;
    }

    @Override
    public String getDeviceName() {
        return "SimMotor (" + maxRPM + " RPM)";
    }

    @Override
    public String getConnectionInfo() {
        return "Simulated hardware";
    }

    @Override
    public int getVersion() {
        return 1;
    }

    // ==================== UNSUPPORTED ====================

    private void unsupported(String operation) {
        if (warnings.add(operation)) {
            System.err.println(
                    "[FTCodeSim] SimMotor: " + operation + " is unsupported."
            );
        }
    }

    @Override
    public DcMotorController getController() {
        unsupported("getController()");
        return null;
    }

    @Override
    public int getPortNumber() {
        unsupported("getPortNumber()");
        return -1;
    }

    @Override
    public void setVelocity(double angularRate) {
        unsupported("setVelocity()");
    }

    @Override
    public void setVelocity(double angularRate, AngleUnit unit) {
        unsupported("setVelocity()");
    }

    @Override
    public void setPIDCoefficients(RunMode mode, PIDCoefficients coefficients) {
        unsupported("setPIDCoefficients()");
    }

    @Override
    public PIDCoefficients getPIDCoefficients(RunMode mode) {
        unsupported("getPIDCoefficients()");
        return new PIDCoefficients(0, 0, 0);
    }

    @Override
    public void setPIDFCoefficients(RunMode mode, PIDFCoefficients coefficients) {
        unsupported("setPIDFCoefficients()");
    }

    @Override
    public PIDFCoefficients getPIDFCoefficients(RunMode mode) {
        unsupported("getPIDFCoefficients()");
        return new PIDFCoefficients(0, 0, 0, 0);
    }

    @Override
    public void setVelocityPIDFCoefficients(
            double p, double i, double d, double f
    ) {
        unsupported("setVelocityPIDFCoefficients()");
    }

    @Override
    public void setPositionPIDFCoefficients(double p) {
        unsupported("setPositionPIDFCoefficients()");
    }

    @Override
    public double getCurrent(CurrentUnit unit) {
        unsupported("getCurrent()");
        return 0;
    }

    @Override
    public double getCurrentAlert(CurrentUnit unit) {
        unsupported("getCurrentAlert()");
        return 0;
    }

    @Override
    public void setCurrentAlert(double current, CurrentUnit unit) {
        unsupported("setCurrentAlert()");
    }

    @Override
    public boolean isOverCurrent() {
        unsupported("isOverCurrent()");
        return false;
    }
}