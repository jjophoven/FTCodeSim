package org.firstinspires.ftc.teamcode.motorphysics;

import com.acmerobotics.dashboard.FtcDashboard;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.VoltageSensor;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.utils.Menu;

public abstract class MeasureKinematicsOpMode extends OpMode {
    VoltageSensor voltageSensor;
    DcMotor.ZeroPowerBehavior zeroPowerBehavior;
    Telemetry dashboardTelemetry;

    double dutyCycle;
    double deltaTime;
    double timestamp;
    double batteryVoltage;
    double appliedVoltage;

    private long lastTime = System.nanoTime();
    private long startLoopTime;
    private Menu controlMenu;

    @Override
    public void init() {
        initHardware();

        dashboardTelemetry = FtcDashboard.getInstance().getTelemetry();

        controlMenu = new Menu(gamepad1,"Select Power");
        controlMenu.addOption("Full Power", () -> setDutyCycle(1));
        controlMenu.addOption("Full Neg Power", () -> setDutyCycle(-1));
        controlMenu.addOption("0.2 Power", () -> setDutyCycle(0.2));
        controlMenu.addOption("0.01 Power", () -> setDutyCycle(0.01));
        controlMenu.addOption("0.0001 Power", () -> setDutyCycle(0.0001));
        controlMenu.addOption("-0.0001 Power", () -> setDutyCycle(-0.0001));
        controlMenu.addOption("-0.2 Power", () -> setDutyCycle(-0.2));
        controlMenu.addOption("-0.01 Power", () -> setDutyCycle(-0.01));
        controlMenu.addOption("0 Power", () -> setDutyCycle(0));
        controlMenu.addOption("Set to Zero Power Brake Mode", () -> setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE));
        controlMenu.addOption("Set to Zero Power Float Mode", () -> setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT));

        telemetry.addLine("Ready. Press START.");
        telemetry.update();
    }

    @Override
    public void loop() {
        long now = System.nanoTime();
        timestamp = (now - startLoopTime) * 1e-9;
        deltaTime = (now - lastTime) * 1e-9;
        lastTime = now;

        batteryVoltage = voltageSensor.getVoltage();
        appliedVoltage = batteryVoltage * dutyCycle;

        controlMenu.update();

        telemetry.addLine(controlMenu.getDisplay());
        telemetry.update();

        dashboardTelemetry.addData("applied voltage", appliedVoltage);
        dashboardTelemetry.addData("duty cycle", dutyCycle);
        dashboardTelemetry.addData("zero power behavior", zeroPowerBehavior);
        dashboardTelemetry.addData("battery voltage", batteryVoltage);
        dashboardTelemetry.addData("loop time ms", deltaTime * 1000);
        dashboardTelemetry.addData("timestamp seconds", timestamp);
    }

    public void initHardware() {
        voltageSensor = hardwareMap.voltageSensor.iterator().next();

        for (LynxModule hub : hardwareMap.getAll(LynxModule.class)) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.AUTO);
        }
    }

    @Override
    public void start() {
        startLoopTime = System.nanoTime();
    }

    abstract void setDutyCycle(double dutyCycle);
    abstract void setZeroPowerBehavior(DcMotor.ZeroPowerBehavior behavior);
}
