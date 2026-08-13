package org.firstinspires.ftc.teamcode.bocaj.pedro;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ElapsedTime;
import org.firstinspires.ftc.teamcode.pedroPathing.Constants;

@Config
@TeleOp
public class HoldPoints extends OpMode {
    public static double DISTANCE = 48;
    public double loops = 0, lastLoop = 0, loopTime = 0;
    private boolean forward;
    private Follower follower;
    private MultipleTelemetry multipleTelemetry;
    ElapsedTime timer = new ElapsedTime();
    Pose target;

    @Override
    public void init() {
        follower = Constants.create(hardwareMap);
        follower.setPose(new Pose(0, 0, 0));

        multipleTelemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());
        target = new Pose(48,0,0);
        forward = true;
    }

    @Override
    public void start() {
        timer.reset();
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

        if (follower.velocity().toVector2D().magnitude() < 0.1 && follower.pose().distance(target) < 0.5) {
            timer.reset();
            if (forward) {
                target = new Pose(48,0,0);
            } else {
                target = new Pose(0,0,0);
            }
            forward = !forward;
        }

        follower.hold(target);

        multipleTelemetry.addData("Loop Time Hz", 1000/loopTime);
        multipleTelemetry.addData("Mode", follower.mode());
        multipleTelemetry.addData("Following?", follower.following());
        multipleTelemetry.addData("Pose", follower.pose());
        multipleTelemetry.update();
    }
}
