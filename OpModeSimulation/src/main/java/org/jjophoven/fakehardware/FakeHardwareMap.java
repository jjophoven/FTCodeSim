package org.jjophoven.fakehardware;

import androidx.annotation.Nullable;
import com.qualcomm.robotcore.hardware.*;
import org.jjophoven.fakehardware.devices.*;
import org.jjophoven.fakehardware.drivetrain.SimulatedDrivetrain;

import java.util.List;

public class FakeHardwareMap extends HardwareMap {
    public FakeHardwareMap() {
        super(null, null);
        FakeVoltageSensor voltageSensor2 = new FakeVoltageSensor();

        voltageSensor.put("voltageSensor", voltageSensor2);

        put("voltageSensor", voltageSensor2);
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

    public FakeGobildaPinpoint pinpoint(String name, SimulatedDrivetrain drivetrain) {
        return register(name, new FakeGobildaPinpoint(drivetrain));
    }

    public <T extends HardwareDevice> T register(String name, T device) {
        put(name, device);
        return device;
    }

    public void update() {
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