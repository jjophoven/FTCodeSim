package org.firstinspires.ftc.teamcode.bocaj.pedro;


import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ElapsedTime;
import org.firstinspires.ftc.teamcode.pedroPathing.Constants;

import static com.pedropathing.api.Paths.line;

@Config
@TeleOp
public class BackAndForthStop extends OpMode {
    public static double DISTANCE = 48+24;
    public double loops = 0, lastLoop = 0, loopTime = 0;
    private Path line1, line2;
    private boolean forward;
    private Follower follower;
    private MultipleTelemetry multipleTelemetry;
    ElapsedTime timer = new ElapsedTime();

    @Override
    public void init() {
        follower = Constants.create(hardwareMap);
        follower.setPose(new Pose(72, 72, 0));

        multipleTelemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());
    }

    @Override
    public void start() {
        line1 = line(new Pose(72,72, 0), new Pose(DISTANCE + 72,72, 0)).constant(0);
        line2 = line(new Pose(DISTANCE + 72,72, 0), new Pose(72,72, 0)).constant(0);

        timer.reset();
        follower.follow(line1);
    }

    @Override
    public void loop() {
        loops++;

        if (loops > 10) {
            double now = System.currentTimeMillis();
            loopTime = (now - lastLoop) / loops;
            lastLoop = now;
            loops = 0;
        }

        double nanoBefore = System.nanoTime();

        follower.update();

        multipleTelemetry.addData("Calculation Nano Time", System.nanoTime() - nanoBefore);
        multipleTelemetry.addData("Calculation Ms", 1e-6 * (System.nanoTime() - nanoBefore));

        Foresight algo = (Foresight) follower.getAlgorithm();
        if ((algo.testParametric() && algo.testHeading() && algo.testTranslational() && algo.testHeading()) || algo.testTimeout()) {
            timer.reset();
            if (forward) {
                follower.follow(line2);
            } else {
                follower.follow(line1);
            }
            forward = !forward;
        }

        multipleTelemetry.addData("Loop Time Hz", 1000/loopTime);
        multipleTelemetry.addData("Mode", follower.mode());
        multipleTelemetry.addData("Following?", follower.following());
        multipleTelemetry.addData("Pose", follower.pose());
        multipleTelemetry.update();
    }
}
