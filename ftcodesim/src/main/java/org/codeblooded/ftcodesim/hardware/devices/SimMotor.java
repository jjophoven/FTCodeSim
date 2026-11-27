package org.codeblooded.ftcodesim.hardware.devices;

import com.qualcomm.robotcore.hardware.*;
import com.qualcomm.robotcore.hardware.configuration.typecontainers.MotorConfigurationType;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;

import java.util.HashSet;
import java.util.Set;

public class SimMotor implements DcMotorEx, SimHardwareDevice {

    private static final double NOMINAL_VOLTAGE = 12.0;
    private static final double RPM_TO_RAD = Math.PI / 30.0;
    private static final double BASE_RESPONSE_TIME = 0.25;
    private static final double STATIC_LOAD_FRACTION = 0.05;
    private static final double KINETIC_LOAD_FRACTION = 0.04;
    // Additional empirical reverse-power damping, relative to electrical damping.
    private static final double REGEN_BRAKING = 4.0;

    // Motor constants referred to the simulated output shaft (SI units).
    private double stallTorque;
    private double resistance;
    private double torqueConstant;
    private double backEmfConstant;
    private double viscousFriction;
    private double baseInertia;

    // Scales both mechanism inertia and Coulomb friction.
    private double load = 1.0;
    private double acceleration;
    private double currentAlertAmps = Double.POSITIVE_INFINITY;

    public void setLoad(double load) {
        if (!Double.isFinite(load) || load <= 0) {
            throw new IllegalArgumentException("Motor load must be finite and positive");
        }
        this.load = load;
    }

    public double getLoad() {
        return load;
    }

    // Baseline angular acceleration (rad/s^2) per Nm at load = 1.
    public void setTorqueToAcceleration(double coefficient) {
        if (!Double.isFinite(coefficient) || coefficient <= 0) {
            throw new IllegalArgumentException("Torque-to-acceleration must be finite and positive");
        }
        baseInertia = 1.0 / coefficient;
    }

    public double getTorqueToAcceleration() {
        return 1.0 / (baseInertia * load);
    }

    public double getInertia() {
        return baseInertia * load;
    }

    // Nominal 12 V specifications; torque is measured at the output shaft.
    // Call before setTorqueToAcceleration(), as this resets the default inertia.
    public void configureMotor(double stallTorqueNm, double stallCurrentAmps,
                               double noLoadCurrentAmps) {
        if (!Double.isFinite(stallTorqueNm) || stallTorqueNm <= 0
                || !Double.isFinite(stallCurrentAmps) || stallCurrentAmps <= 0
                || !Double.isFinite(noLoadCurrentAmps) || noLoadCurrentAmps < 0
                || noLoadCurrentAmps >= stallCurrentAmps) {
            throw new IllegalArgumentException("Invalid motor specifications");
        }
        stallTorque = stallTorqueNm;
        resistance = NOMINAL_VOLTAGE / stallCurrentAmps;
        torqueConstant = stallTorqueNm / stallCurrentAmps;
        double freeSpeed = maxRPM * RPM_TO_RAD;
        backEmfConstant = (NOMINAL_VOLTAGE - resistance * noLoadCurrentAmps) / freeSpeed;
        viscousFriction = torqueConstant * noLoadCurrentAmps / freeSpeed;
        baseInertia = BASE_RESPONSE_TIME * (getElectricalDamping() + viscousFriction);
    }

    private double getElectricalDamping() {
        return torqueConstant * backEmfConstant / resistance;
    }

    private double getResponseTime() {
        return getInertia() / (getElectricalDamping() + viscousFriction);
    }

    public double getStaticFrictionTorque() {
        return STATIC_LOAD_FRACTION * stallTorque * load;
    }

    public double getKineticFrictionTorque() {
        return KINETIC_LOAD_FRACTION * stallTorque * load;
    }

    // Electromagnetic shaft torque (Nm), excluding empirical extra braking.
    public double getMotorTorque() {
        return torqueConstant * getSignedCurrent();
    }

    // Net angular acceleration, including friction and braking (rad/s^2).
    public double getAcceleration() {
        return acceleration;
    }

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
    // True for standard FTC motor encoder semantics. False for an
    // independent mechanism encoder whose polarity is fixed to the shaft.
    private boolean encoderFollowsDirection = true;

    public void setEncoderFollowsDirection(boolean followsDirection) {
        this.encoderFollowsDirection = followsDirection;
    }

    private double encoderDirectionSign() {
        return encoderFollowsDirection ? directionSign() : 1.0;
    }
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
        if (!Double.isFinite(maxRPM) || maxRPM <= 0) {
            throw new IllegalArgumentException("Max RPM must be finite and positive");
        }
        this.maxRPM = maxRPM;
        this.voltageSensor = voltageSensor;

        motorType = new MotorConfigurationType();

        motorType.setMaxRPM(maxRPM);
        motorType.setTicksPerRev(ticksPerRev);

        // Generic defaults. Configure actual motor specifications when known.
        configureMotor(1.0, 10.0, 0.2);
    }

    // ==================== PHYSICS ====================

    @Override
    public void update(double deltaTime) {
        if (deltaTime <= 0 || !Double.isFinite(deltaTime)) return;

        double availableRPM = getAvailableRPM();

        // Substep to handle direction changes and position control.
        while (deltaTime > 0) {
            double dt = Math.min(deltaTime, 0.005);
            updatePhysics(dt, availableRPM);
            deltaTime -= dt;
        }
    }

    private boolean isFloating(double appliedPower) {
        return !enabled || (appliedPower == 0
                && zeroPowerBehavior != ZeroPowerBehavior.BRAKE
                && runMode != RunMode.RUN_TO_POSITION);
    }

    private double getBatteryVoltage() {
        double voltage = voltageSensor.getVoltage();
        return Double.isFinite(voltage) ? Math.max(0, voltage) : 0;
    }

    // Signed equivalent armature current (A), neglecting inductance and PWM ripple.
    private double getSignedCurrent() {
        double appliedPower = getAppliedPower();
        if (isFloating(appliedPower)) return 0;
        return (appliedPower * getBatteryVoltage()
                - backEmfConstant * rpm * RPM_TO_RAD) / resistance;
    }

    private void updatePhysics(double dt, double availableRPM) {
        double appliedPower = getAppliedPower(availableRPM);
        boolean floating = isFloating(appliedPower);
        double voltage = appliedPower * getBatteryVoltage();
        double driveTorque = floating ? 0 : torqueConstant * voltage / resistance;
        double damping = viscousFriction + (floating ? 0 : getElectricalDamping());

        // Split at zero velocity: friction and extra braking can change there.
        integrateMotor(dt, appliedPower, driveTorque, damping);

        if (runMode == RunMode.STOP_AND_RESET_ENCODER) {
            encoderOffset = revolutions;
        }
    }

    private void integrateMotor(double dt, double appliedPower,
                                double driveTorque, double baseDamping) {
        if (dt <= 0) return;
        double omega = rpm * RPM_TO_RAD;
        double motionSign = Math.signum(omega);

        if (motionSign == 0) {
            // Static friction balances the applied torque until breakaway.
            if (Math.abs(driveTorque) <= getStaticFrictionTorque()) {
                rpm = 0;
                acceleration = 0;
                return;
            }
            motionSign = Math.signum(driveTorque);
        }

        double damping = baseDamping;
        if (appliedPower * motionSign < 0) {
            // Additional empirical braking torque = -K * angular velocity.
            damping += REGEN_BRAKING * getElectricalDamping();
        }

        double constantTorque = driveTorque - motionSign * getKineticFrictionTorque();
        double inertia = getInertia();

        // If the solution crosses zero, integrate only to that instant,
        // then reevaluate static friction and reverse-power braking.
        if (omega * constantTorque < 0) {
            double timeToZero = damping == 0
                    ? -omega * inertia / constantTorque
                    : inertia / damping * Math.log1p(-damping * omega / constantTorque);
            if (timeToZero <= dt) {
                integrateSegment(timeToZero, omega, constantTorque, damping, inertia);
                rpm = 0;
                acceleration = 0;
                integrateMotor(dt - timeToZero, appliedPower, driveTorque, baseDamping);
                return;
            }
        }

        integrateSegment(dt, omega, constantTorque, damping, inertia);
    }

    private void integrateSegment(double dt, double omega, double constantTorque,
                                  double damping, double inertia) {
        // Exact solution of J * d(omega)/dt = constantTorque - damping * omega.
        double nextOmega;
        double angle;
        if (damping == 0) {
            double alpha = constantTorque / inertia;
            nextOmega = omega + alpha * dt;
            angle = omega * dt + 0.5 * alpha * dt * dt;
        } else {
            double timeConstant = inertia / damping;
            double equilibrium = constantTorque / damping;
            double response = -Math.expm1(-dt / timeConstant);
            nextOmega = omega + (equilibrium - omega) * response;
            angle = omega * dt + (equilibrium - omega) * (dt - timeConstant * response);
        }
        rpm = nextOmega / RPM_TO_RAD;
        revolutions += angle / (2 * Math.PI);
        acceleration = (constantTorque - damping * nextOmega) / inertia;
    }

    private double getAvailableRPM() {
        return maxRPM * getBatteryVoltage() / NOMINAL_VOLTAGE;
    }

    // Physical power, including motor direction and position control.
    public double getAppliedPower() {
        return getAppliedPower(getAvailableRPM());
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
                    errorRevolutions * 60 / (4 * getResponseTime());

            return clamp(
                    desiredRPM / availableRPM,
                    -Math.abs(power),
                    Math.abs(power)
            ) * encoderDirectionSign();
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

    // Physical shaft RPM, suitable for simulator physics.
    public double getRPM() {
        return rpm;
    }

    // Direction-adjusted RPM, suitable for FTC SDK feedback.
    public double getLogicalRPM() {
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
        this.power = Double.isFinite(power) ? clamp(power, -1, 1) : 0;
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
                * encoderDirectionSign();
    }

    @Override
    public int getCurrentPosition() {
        return (int) Math.round(getEncoderPosition());
    }

    @Override
    public double getVelocity() {
        return rpm * encoderDirectionSign() / 60.0 * motorType.getTicksPerRev();
    }

    @Override
    public double getVelocity(AngleUnit unit) {
        double encoderRPM = rpm * encoderDirectionSign();
        return unit == AngleUnit.DEGREES
                ? encoderRPM * 6
                : encoderRPM * Math.PI / 30;
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
        acceleration = 0;
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
        double amps = Math.abs(getSignedCurrent());
        return unit == CurrentUnit.AMPS ? amps : amps * 1000;
    }

    @Override
    public double getCurrentAlert(CurrentUnit unit) {
        return unit == CurrentUnit.AMPS ? currentAlertAmps : currentAlertAmps * 1000;
    }

    @Override
    public void setCurrentAlert(double current, CurrentUnit unit) {
        if (Double.isNaN(current) || current < 0) {
            throw new IllegalArgumentException("Invalid current alert");
        }
        currentAlertAmps = unit == CurrentUnit.AMPS ? current : current / 1000;
    }

    @Override
    public boolean isOverCurrent() {
        return Math.abs(getSignedCurrent()) > currentAlertAmps;
    }
}
