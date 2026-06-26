package org.firstinspires.ftc.teamcode.fake;

import android.content.Context;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.eventloop.opmode.OpModeManagerNotifier;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.VoltageSensor;

import java.util.ArrayList;
import java.util.List;

public class FakeHardwareMap extends HardwareMap {
    public FakeHardwareMap(Context appContext, OpModeManagerNotifier notifier) {
        super(appContext, notifier);
        voltageSensor.put("voltageSensor", new FakeVoltageSensor());
    }

    List<FakeMotor> motors = new ArrayList<>();
    FakeMecanum drivetrain;

    @Override
    public <T> T get(Class<? extends T> classOrInterface, String deviceName) {
        synchronized (lock) {
            if (classOrInterface.equals(DcMotor.class) || classOrInterface.equals(DcMotorEx.class)) {
                FakeMotor motor = new FakeMotor();
                if (deviceName.equals("frontLeft")) {
                    motors.add(motor);
                }
                if (deviceName.equals("frontRight")) {
                    motors.add(motor);
                }
                if (deviceName.equals("backLeft")) {
                    motors.add(motor);
                }
                if (deviceName.equals("backRight")) {
                    motors.add(motor);
                    drivetrain = new FakeMecanum(motors.toArray(new FakeMotor[4]));
                }

                return (T) motor;
            } else if (classOrInterface.equals(VoltageSensor.class)) {
                return (T) new FakeVoltageSensor();
            } else if (classOrInterface.equals(GoBildaPinpointDriver.class)) {
                return (T) new FakePinpoint();
            }
            System.out.println("Unable to find a hardware device with name \"" + deviceName + "\" and type " + classOrInterface.getSimpleName());
            return null;
        }
    }

    public void updateDrivetrain() {
        drivetrain.update(0.02);
    }
}
