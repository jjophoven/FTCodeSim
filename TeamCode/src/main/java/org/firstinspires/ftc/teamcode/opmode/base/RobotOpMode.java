package org.firstinspires.ftc.teamcode.opmode.base;

import android.annotation.SuppressLint;
import com.pedropathing.ftc.localization.constants.PinpointConstants;
import com.pedropathing.ftc.localization.localizers.PinpointLocalizer;
import com.pedropathing.geometry.Pose;
import com.pedropathing.ivy.Scheduler;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.VoltageSensor;
import org.firstinspires.ftc.teamcode.*;
import org.firstinspires.ftc.teamcode.drivetrain.Mecanum;
import org.firstinspires.ftc.teamcode.utils.DataSaver;
import org.firstinspires.ftc.teamcode.utils.Menu;
import org.firstinspires.ftc.teamcode.utils.OpModeTimer;
import org.psilynx.psikit.core.Logger;
import org.psilynx.psikit.ftc.FtcLoggingSession;
import org.psilynx.psikit.ftc.autolog.PsiKitAutoLog;

import java.util.List;

@PsiKitAutoLog
public abstract class RobotOpMode extends OpMode {
    private Menu initMenu;

    protected Context context;
    protected Alliance alliance;
    protected VoltageSensor voltageSensor;
    protected Pose pose;
    protected Mecanum drivetrain;
    protected PinpointLocalizer localizer;

    private List<LynxModule> hubs;

    OpModeTimer timer = new OpModeTimer();

    @Override
    public void init() {
        DataSaver.SavedData savedData = DataSaver.load();

        pose = savedData == null || savedData.pose == null ? new Pose() : savedData.pose;

        initMenu = new Menu(gamepad1, "Select Alliance: ");
        initMenu.addOption("Red Alliance", () -> alliance = Alliance.RED);
        initMenu.addOption("Blue Alliance", () -> alliance = Alliance.BLUE);

        boolean allianceSaved = savedData != null && savedData.alliance != null;
        if (allianceSaved) {
            initMenu.confirmOption(savedData.alliance.ordinal());
            alliance = savedData.alliance;
        }

        Scheduler.reset();
        context = new Context(this);

        hubs = hardwareMap.getAll(LynxModule.class);
        hubs.forEach(hub -> hub.setBulkCachingMode(LynxModule.BulkCachingMode.MANUAL));
        voltageSensor = hardwareMap.voltageSensor.iterator().next();

        localizer = new PinpointLocalizer(hardwareMap, new PinpointConstants());
        localizer.setPose(new Pose(141.5/2, 141.5/2, 0));

        drivetrain = new Mecanum(hardwareMap);

        Logger.recordMetadata("Init/Teleop", "UtilOpMode");
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void init_loop() {
        timer.updateDt();

        initMenu.update();

        double voltage = voltageSensor.getVoltage();

        context.telemetry.addLine(initMenu.getDisplay());
        context.telemetry.addLine(String.format(
                "Battery: %s %.1f%% (%.2fV)",
                getBatteryStatus(voltage),
                voltage / 14.5 * 100,
                voltage
        ));
        context.telemetry.addLine("Loop Time: " + String.format("%.3f ms", timer.getSmoothedDtMs()));
        context.telemetry.addLine("Start Pose: " + pose);

        Logger.recordOutput("InitLoop/raw loop time (ms)", timer.getDt());
        Logger.recordOutput("InitLoop/init time secs", timer.getInitTime());

        Scheduler.execute();
        context.telemetry.update();
    }

    @Override
    public void start() {
        timer.start();
        Logger.recordMetadata("Start/Alliance", String.valueOf(alliance));
        Logger.recordMetadata("Start/Pose", String.valueOf(pose));

    }

    @Override
    public void loop() {
        timer.updateDt();

        hubs.forEach(LynxModule::clearBulkCache);

        localizer.update();

        Logger.recordOutput("Loop/raw loop time (ms)", timer.getDtMs());
        Logger.recordOutput("Loop/runtime secs", timer.getRuntime());
        Logger.recordOutput("Robot/battery voltage", voltageSensor.getVoltage());
    }

    @Override
    public void stop() {
        DataSaver.save(pose, alliance);
    }

    public String getBatteryStatus(double voltage) {
        if (voltage < 10.0) {
            return "CRITICAL";
        } else if (voltage < 12.0) {
            return "LOW";
        } else if (voltage < 13.0) {
            return "OK";
        } else if (voltage < 14.0) {
            return "HIGH";
        } else {
            return "SUPERCHARGED";
        }
    }
}