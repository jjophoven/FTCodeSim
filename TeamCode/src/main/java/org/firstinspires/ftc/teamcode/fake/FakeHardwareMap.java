package org.firstinspires.ftc.teamcode.fake;

import android.content.Context;
import com.qualcomm.robotcore.eventloop.opmode.OpModeManagerNotifier;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.VoltageSensor;

public class FakeHardwareMap extends HardwareMap {
    public FakeHardwareMap(Context appContext, OpModeManagerNotifier notifier) {
        super(appContext, notifier);
        voltageSensor.put("voltageSensor", new FakeVoltageSensor());
    }

    @Override
    public <T> T get(Class<? extends T> classOrInterface, String deviceName) {
        synchronized (lock) {
            if (classOrInterface.equals(DcMotor.class)) {
                return (T) new FakeMotor();
            } else if (classOrInterface.equals(VoltageSensor.class)) {
                return (T) new FakeVoltageSensor();
            }
            System.out.println("Unable to find a hardware device with name \"" + deviceName + "\" and type " + classOrInterface.getSimpleName());
            return null;
        }
    }
}
