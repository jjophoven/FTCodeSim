package org.jjophoven.fakehardware;

import androidx.annotation.Nullable;
import com.qualcomm.robotcore.hardware.*;
import org.jjophoven.fakehardware.devices.*;
import org.jjophoven.fakehardware.drivetrain.SimulatedDrivetrain;

import java.util.List;

public class FakeHardwareMap extends HardwareMap {
    private SimulatedDrivetrain drivetrain;

    public SimulatedDrivetrain getDrivetrain() {
        return drivetrain;
    }

    public void setDrivetrain(SimulatedDrivetrain drivetrain) {
        this.drivetrain = drivetrain;
    }

    public FakeHardwareMap() {
        super(null, null);
        FakeVoltageSensor voltageSensor = new FakeVoltageSensor();

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
            return result;
        }
    }

    public FakeMotor motor(FakeMotorConfig config) {
        return register(config.name, new FakeMotor(config));
    }

    public FakeGobildaPinpoint pinpoint(String name) {
        if (drivetrain == null) {
            throw new RuntimeException("Drivetrain not set");
        }
        return register(name, new FakeGobildaPinpoint(drivetrain));
    }

    public <T extends HardwareDevice> T register(String name, T device) {
        put(name, device);
        return device;
    }

    public void update() {
        // TODO add deltaTime here to step and update
        if (drivetrain != null) {
            drivetrain.step(0.02);
        }
        for (List<HardwareDevice> device : allDevicesMap.values()) {
            for (HardwareDevice d : device) {
                try {
                    ((FakeHardwareDevice) d).update();
                } catch (ClassCastException ignored) {
                }
            }
        }
    }
}