package org.firstinspires.ftc.teamcode.opmode.base;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import dev.frozenmilk.dairy.cachinghardware.CachingDcMotorEx;
import dev.frozenmilk.dairy.cachinghardware.CachingServo;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.psilynx.psikit.core.Logger;

public final class Context {
    public final HardwareMap hardwareMap;
    public final Telemetry telemetry;

    public Context(OpMode opMode) {
        hardwareMap = opMode.hardwareMap;
        Telemetry telemetry1 = FtcDashboard.getInstance().getTelemetry();
        telemetry = new MultipleTelemetry(
                opMode.telemetry,
                telemetry1
        );
    }

    public void log(String name, double value) {
        Logger.recordOutput(name, value);
        telemetry.addData(name, value);
    }

    public void log(String name, int value) {
        Logger.recordOutput(name, value);
        telemetry.addData(name, value);
    }

    public void log(String name, Object value) {
        Logger.recordOutput(name, value.toString());
        telemetry.addData(name, value);
    }

    public Servo servo(String name) {
        return new CachingServo(hardwareMap.get(Servo.class, name));
    }

    public DcMotorEx motor(String name) {
        return new CachingDcMotorEx(hardwareMap.get(DcMotorEx.class, name));
    }
}