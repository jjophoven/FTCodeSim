package org.firstinspires.ftc.teamcode.bocaj.utils;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.*;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;
import org.firstinspires.ftc.teamcode.utils.Menu;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

@TeleOp
public class HardwareTestMenu extends OpMode {

    private Menu menu;

    @Override
    public void init() {
        telemetry.setDisplayFormat(Telemetry.DisplayFormat.MONOSPACE);

        menu = new Menu(gamepad1, "Select a Hardware Device:");

        // todo select multiple, reverse them etc

        register(
                DcMotorEx.class,
                 motor -> {
                    double power = -gamepad1.right_stick_y;
                    motor.setPower(power);
                    telemetry.addData("motor power", "%.2f", power);
                    telemetry.addData("ticks per second", "%.2f", motor.getVelocity());
                     telemetry.addData("current", "%.2f", motor.getCurrent(CurrentUnit.AMPS));
                },
                motor -> motor.setPower(0)
        );

        final double[] pos = {0};
        register(
                ServoImplEx.class,
                servo -> {
                    double delta = -gamepad1.right_stick_y * 0.005;
                    pos[0] = clamp(pos[0] + delta, 0.0, 1.0);

                    servo.setPosition(pos[0]);
                    telemetry.addData("servo position: ", "%.3f", pos[0]);
                },
                s -> {}
        );

        register(
                NormalizedColorSensor.class,
                sensor -> telemetry.addData("color", sensor.getNormalizedColors().toString()),
                s -> {}
        );
    }

    @Override
    public void loop() {
        menu.update();
        telemetry.addLine(menu.getDisplay());
        telemetry.update();
    }

    private <T extends HardwareDevice> void register(
            Class<T> clazz,
            Consumer<T> run,
            Consumer<T> stop
    ) {
        getSortedHardware(hardwareMap, clazz).forEach((name, device) -> menu.addOption(
                name,
                () -> run.accept(device),
                () -> stop.accept(device)
        ));
    }

    private static <T extends HardwareDevice> Map<String, T> getSortedHardware(
            HardwareMap hw, Class<T> clazz) {
        Map<String, T> map = new LinkedHashMap<>();

        hw.getAll(clazz).stream()
                .sorted(Comparator.comparing(dev ->
                        hw.getNamesOf(dev).iterator().next()))
                .forEach(dev -> {
                    String name = hw.getNamesOf(dev).iterator().next();
                    map.put(name, dev);
                });

        return map;
    }

    private double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}
