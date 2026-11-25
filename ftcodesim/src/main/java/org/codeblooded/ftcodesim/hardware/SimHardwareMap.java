package org.codeblooded.ftcodesim.hardware;

import androidx.annotation.Nullable;
import com.qualcomm.hardware.digitalchickenlabs.OctoQuad;
import com.qualcomm.robotcore.hardware.*;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;

import org.codeblooded.ftcodesim.hardware.devices.*;
import org.codeblooded.ftcodesim.hardware.drivetrain.SimulatedDrivetrain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

public class SimHardwareMap extends HardwareMap {

    private long previousTime;
    private double deltaTime;

    private final List<SimHardwareMechanism> mechanisms = new ArrayList<>();

    private final SimVoltageSensor simVoltageSensor;
    private SimulatedDrivetrain drivetrain;

    public boolean autoCreateDevices = true;

    public SimHardwareMap() {
        super(null, null);

        dcMotor = replaceMapping(dcMotor, DcMotor.class);
        servo = replaceMapping(servo, Servo.class);
        touchSensor = replaceMapping(touchSensor, TouchSensor.class);

        simVoltageSensor = new SimVoltageSensor();
        register("voltageSensor", simVoltageSensor);
    }

    public double getVoltage() {
        return simVoltageSensor.getVoltage();
    }

    private <T extends HardwareDevice> DeviceMapping<T> replaceMapping(
            DeviceMapping<T> original,
            Class<T> type
    ) {
        DeviceMapping<T> replacement = new DeviceMapping<T>(type) {
            @Override
            public T get(String name) {
                return SimHardwareMap.this.get(type, name);
            }
        };

        int index = allDeviceMappings.indexOf(original);

        if (index >= 0) {
            allDeviceMappings.set(index, replacement);
        } else {
            allDeviceMappings.add(replacement);
        }

        return replacement;
    }

    @Override
    public <T> T get(Class<? extends T> type, String name) {
        T device = tryGet(type, name);

        if (device == null) {
            throw new IllegalArgumentException(
                    "Could not find device \"" + name
                            + "\" of type " + type.getSimpleName()
            );
        }

        return device;
    }

    @Override
    public @Nullable <T> T tryGet(
            Class<? extends T> type,
            String name
    ) {
        synchronized (lock) {
            name = name.trim();

            T device = find(type, name);

            if (device != null) {
                return device;
            }

            if (autoCreateDevices && !allDevicesMap.containsKey(name)) {
                HardwareDevice simulated = createDefaultDevice(type);

                if (simulated != null) {
                    System.err.println(
                            "[FTCodeSim] WARNING: Device \"" + name
                                    + "\" was not registered. Using default "
                                    + simulated.getClass().getSimpleName()
                    );

                    register(name, simulated);

                    return type.cast(simulated);
                }
            }

            System.err.println(
                    "[FTCodeSim] Could not find device \"" + name
                            + "\" of type " + type.getSimpleName()
            );

            return null;
        }
    }

    /**
     * Retrieves a device without automatically creating one.
     */
    public @Nullable <T> T find(
            Class<? extends T> type,
            String name
    ) {
        synchronized (lock) {
            List<HardwareDevice> devices =
                    allDevicesMap.get(name.trim());

            if (devices != null) {
                for (HardwareDevice device : devices) {
                    if (type.isInstance(device)) {
                        rebuildDeviceNamesIfNecessary();
                        return type.cast(device);
                    }
                }
            }

            return null;
        }
    }

    // ==================== AUTOMATIC CREATION ====================

    private @Nullable HardwareDevice createDefaultDevice(Class<?> type) {

        if (DcMotorSimple.class.isAssignableFrom(type)
                && type.isAssignableFrom(SimMotor.class)) {

            return gobildaYellowJacket6000();
        }

        if (Servo.class.isAssignableFrom(type)
                && type.isAssignableFrom(SimServo.class)) {

            return new SimServo();
        }

        if (TouchSensor.class.isAssignableFrom(type)
                && type.isAssignableFrom(SimTouchSensor.class)) {

            return new SimTouchSensor();
        }

        if (GoBildaPinpointDriver.class.isAssignableFrom(type)
                && type.isAssignableFrom(SimPinpoint.class)) {

            if (drivetrain == null) {
                System.err.println(
                        "[FTCodeSim] Cannot create SimPinpoint "
                                + "without a registered drivetrain."
                );

                return null;
            }

            return new SimPinpoint(drivetrain);
        }

        if (OctoQuad.class.isAssignableFrom(type)
                && type.isAssignableFrom(SimOctoquad.class)) {

            if (drivetrain == null) {
                System.err.println(
                        "[FTCodeSim] Cannot create SimOctoquad "
                                + "without a registered drivetrain."
                );

                return null;
            }

            return new SimOctoquad(drivetrain);
        }

        return null;
    }

    @Override
    public void put(String name, HardwareDevice device) {
        synchronized (lock) {
            name = name.trim();

            List<HardwareDevice> existing = allDevicesMap.get(name);

            if (existing != null) {
                List<HardwareDevice> oldDevices =
                        new ArrayList<>(existing);

                for (DeviceMapping<? extends HardwareDevice> mapping
                        : allDeviceMappings) {

                    mapping.remove(name);
                }

                for (HardwareDevice oldDevice : oldDevices) {
                    super.remove(name, oldDevice);
                }
            }

            super.put(name, device);

            for (DeviceMapping<? extends HardwareDevice> mapping
                    : allDeviceMappings) {

                registerMapping(mapping, name, device);
            }
        }
    }

    private <T extends HardwareDevice> void registerMapping(
            DeviceMapping<T> mapping,
            String name,
            HardwareDevice device
    ) {
        Class<T> type = mapping.getDeviceTypeClass();

        if (type.isInstance(device)) {
            mapping.putLocal(name, type.cast(device));
        }
    }

    public <T extends HardwareDevice> T register(
            String name,
            T device
    ) {
        System.out.println(
                "[FTCodeSim] Registering " + name
                        + " as " + device.getClass().getSimpleName()
        );

        put(name, device);

        return device;
    }

    public <T extends SimHardwareMechanism> T register(T mechanism) {
        synchronized (lock) {
            System.out.println(
                    "[FTCodeSim] Registering "
                            + mechanism.getClass().getSimpleName()
            );

            if (mechanism instanceof SimulatedDrivetrain) {
                drivetrain = (SimulatedDrivetrain) mechanism;
            }

            mechanism.registerDevices(this);
            mechanisms.add(mechanism);

            return mechanism;
        }
    }

    public void setDrivetrain(SimulatedDrivetrain drivetrain) {
        synchronized (lock) {
            this.drivetrain = drivetrain;
        }
    }

    public @Nullable SimulatedDrivetrain getDrivetrain() {
        return drivetrain;
    }

    public SimVoltageSensor getVoltageSensor() {
        return simVoltageSensor;
    }

    public SimMotor motor(double maxRPM) {
        return new SimMotor(maxRPM, simVoltageSensor);
    }

    public SimMotor motor(double maxRPM, double ticksPerRev) {
        return new SimMotor(maxRPM, ticksPerRev, simVoltageSensor);
    }

    public SimMotor gobildaYellowJacket30() {
        return motor(30, 5281.1);
    }

    public SimMotor gobildaYellowJacket43() {
        return motor(43, 3895.9);
    }

    public SimMotor gobildaYellowJacket60() {
        return motor(60, 2786.2);
    }

    public SimMotor gobildaYellowJacket84() {
        return motor(84, 1993.6);
    }

    public SimMotor gobildaYellowJacket117() {
        return motor(117, 1425.1);
    }

    public SimMotor gobildaYellowJacket223() {
        return motor(223, 751.8);
    }

    public SimMotor gobildaYellowJacket312() {
        return motor(312, 537.7);
    }

    public SimMotor gobildaYellowJacket435() {
        return motor(435, 384.5);
    }

    public SimMotor gobildaYellowJacket1150() {
        return motor(1150, 145.1);
    }

    public SimMotor gobildaYellowJacket1620() {
        return motor(1620, 103.8);
    }

    public SimMotor gobildaYellowJacket6000() {
        return motor(6000, 28);
    }

    private List<SimHardwareDevice> getSimDevices() {
        synchronized (lock) {
            List<SimHardwareDevice> devices = new ArrayList<>();

            Set<HardwareDevice> visited = Collections.newSetFromMap(
                    new IdentityHashMap<>()
            );

            for (List<HardwareDevice> list : allDevicesMap.values()) {
                for (HardwareDevice device : list) {
                    if (device instanceof SimHardwareDevice
                            && visited.add(device)) {

                        devices.add((SimHardwareDevice) device);
                    }
                }
            }

            return devices;
        }
    }

    private List<SimHardwareMechanism> getMechanisms() {
        synchronized (lock) {
            return new ArrayList<>(mechanisms);
        }
    }

    public void update() {
        updateDeltaTime();

        List<SimHardwareDevice> devices = getSimDevices();

        for (SimHardwareDevice device : devices) {
            if (!isOdometryDevice(device)) {
                device.update(deltaTime);
            }
        }

        for (SimHardwareMechanism mechanism : getMechanisms()) {
            mechanism.update(deltaTime);
        }

        for (SimHardwareDevice device : devices) {
            if (isOdometryDevice(device)) {
                device.update(deltaTime);
            }
        }
    }

    private boolean isOdometryDevice(SimHardwareDevice device) {
        return device instanceof SimPinpoint
                || device instanceof SimOctoquad;
    }

    public void reset() {
        for (SimHardwareMechanism mechanism : getMechanisms()) {
            mechanism.reset();
        }

        for (SimHardwareDevice device : getSimDevices()) {
            device.reset();
        }

        previousTime = 0;
        deltaTime = 0;
    }

    private void updateDeltaTime() {
        long currentTime = System.nanoTime();

        deltaTime = previousTime == 0
                ? 0
                : (currentTime - previousTime) * 1e-9;

        previousTime = currentTime;
    }

    public double getDeltaTime() {
        return deltaTime;
    }
}