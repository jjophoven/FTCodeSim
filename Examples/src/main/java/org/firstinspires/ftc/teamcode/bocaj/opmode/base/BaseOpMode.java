package org.firstinspires.ftc.teamcode.bocaj.opmode.base;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.widget.TextView;
import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.pedropathing.follower.Follower;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.math.Pose;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.VoltageSensor;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.R;
import org.firstinspires.ftc.teamcode.bocaj.opmode.RobotDataStore;
import org.firstinspires.ftc.teamcode.bocaj.utils.Alliance;
import org.firstinspires.ftc.teamcode.utils.Menu;
import org.firstinspires.ftc.teamcode.pedroPathing.Constants;
import java.util.List;

public abstract class BaseOpMode extends OpMode {
    private Menu initMenu;

    protected VoltageSensor voltageSensor;
    public Follower follower;
    private List<LynxModule> hubs;
    public Alliance alliance;

    boolean isAuto = false;

    @Override
    public void init() {
        initOpMode();
        initHardware();
        initMenus();
        loadRobotData();
        Scheduler.reset();
    }

    public void initOpMode() {
        isAuto = getClass().isAnnotationPresent(Autonomous.class);
        telemetry = new MultipleTelemetry(
                telemetry,
                FtcDashboard.getInstance().getTelemetry(),
                new TelemetryLogger()
        );
        telemetry.setDisplayFormat(Telemetry.DisplayFormat.MONOSPACE);
    }

    public void initHardware() {
        follower = Constants.create(hardwareMap);
        follower.setPose(new Pose(141.5/2, 141.5/2, 0));

        hubs = hardwareMap.getAll(LynxModule.class);
        hubs.forEach(hub -> hub.setBulkCachingMode(LynxModule.BulkCachingMode.MANUAL));

        voltageSensor = hardwareMap.voltageSensor.iterator().next();
    }

    public void initMenus() {
        initMenu = new Menu(gamepad1, "Select Alliance: ");
        initMenu.addOption("Red Alliance", () -> alliance = Alliance.RED);
        initMenu.addOption("Blue Alliance", () -> alliance = Alliance.BLUE);
    }

    public void loadRobotData() {
        RobotDataStore.loadData();
        String allianceName = RobotDataStore.get("alliance");
        if (allianceName != null) {
            alliance = Alliance.valueOf(allianceName);
            initMenu.confirmOption(alliance.ordinal());
        }
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void init_loop() {
        hubs.forEach(LynxModule::clearBulkCache);

        initMenu.update();

        double voltage = voltageSensor.getVoltage();

        telemetry.addLine(initMenu.getDisplay()); // TODO offload this to Menu class
        telemetry.addLine(String.format(
                "Battery: %s %.1f%% (%.2fV)",
                getBatteryStatus(voltage),
                voltage / 14.5 * 100,
                voltage
        ));
        logLoopTime();

        Scheduler.execute();

        telemetry.update();
    }

    @SuppressLint("DefaultLocale")
    public void logLoopTime() {
//        telemetry.addLine("Loop Time: " + String.format("%.3f ms", follower.getDeltaTime() * 1000));
//        telemetry.addLine("Loop Frequency: " + String.format("%.2f hz", 1.0 / follower.getDeltaTime()));
    }

    @Override
    public void start() {
        if (isAuto) {
            RobotDataStore.put("alliance", alliance.name());
        }
    }

    @Override
    public void loop() {
        follower.update();

        hubs.forEach(LynxModule::clearBulkCache);

        logLoopTime();
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

    private Activity activity;
    private TextView activeConfTxtView;
    private volatile String configFile;

    private String getActiveConf() {
        activity = (Activity) hardwareMap.appContext;
        activity.runOnUiThread(() -> {
            activeConfTxtView = activity.findViewById(R.id.idActiveConfigName);
            configFile = activeConfTxtView.getText().toString();
        });
        while (configFile == null) {
            try {
                Thread.sleep(15);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            // Wait for the configuration file to be set
        }
        return configFile;
    }
}