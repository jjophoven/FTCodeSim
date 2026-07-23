package org.codeblooded.ftcodesim.hardware;

import androidx.annotation.Nullable;
import com.qualcomm.robotcore.hardware.*;
import org.codeblooded.ftcodesim.hardware.devices.*;
import org.codeblooded.ftcodesim.hardware.devices.*;
import org.codeblooded.ftcodesim.hardware.drivetrain.SimulatedDrivetrain;

import java.util.ArrayList;
import java.util.List;

public class SimHardwareMap extends HardwareMap {
    private long previousTime = System.nanoTime();
    double deltaTime;
    List<SimHardwareMechanism> mechanisms = new ArrayList<>();

    public SimHardwareMap() {
        super(null, null);

        SimVoltageSensor voltageSensor = new SimVoltageSensor();

        // TODO automatically do this for every device
        this.voltageSensor.put("voltageSensor", voltageSensor);

        put("voltageSensor", voltageSensor);
    }

    public @Nullable <T> T tryGet(Class<? extends T> classOrInterface, String deviceName) {
        synchronized (lock) {
            deviceName = deviceName.trim();
            List<HardwareDevice> list = allDevicesMap.get(deviceName);
            @Nullable T result = null;

            if (list != null) {
                for (HardwareDevice device : list) {
                    if (classOrInterface.isInstance(device)) {
                        rebuildDeviceNamesIfNecessary();
                        result = classOrInterface.cast(device);
                        break;
                    }
                }
            }
            if (result == null) {
                System.out.println("Could not find device " + deviceName + " of type " + classOrInterface.getSimpleName());
            }

            return result;
        }
    }

    public SimMotor motor(SimMotorConfig config) {
        return register(config.name, new SimMotor(config));
    }

    public <T extends HardwareDevice> T register(String name, T device) {
        System.out.println("Registering " + name + " as " + device.getClass().getSimpleName());
        put(name, device);
        return device;
    }

    public SimHardwareMechanism register(SimHardwareMechanism mechanism) {
        System.out.println("Registering " + mechanism.getClass().getSimpleName());
        mechanism.registerDevices(this);
        mechanisms.add(mechanism);
        return mechanism;
    }

    public void update() {
        updateDeltaTime();

        for (List<HardwareDevice> device : allDevicesMap.values()) {
            for (HardwareDevice d : device) {
                if (d instanceof SimHardwareDevice) {
                    ((SimHardwareDevice) d).update(deltaTime);
                }
            }
        }

        for (SimHardwareMechanism mechanism : mechanisms) {
            mechanism.update(deltaTime);
        }
    }

    private void updateDeltaTime() {
        long currentTime = System.nanoTime();
        deltaTime = (currentTime - previousTime) * 1e-9;
        previousTime = currentTime;
    }
}