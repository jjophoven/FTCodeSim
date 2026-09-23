package org.firstinspires.ftc.teamcode.pedroPathing.tests;

import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.pedropathing.paths.curves.Curve;
import com.pedropathing.paths.interpolator.Interpolator;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.teamcode.pedroPathing.Constants;
import org.psilynx.psikit.core.Logger;

import static com.pedropathing.api.Paths.curve;
import static com.pedropathing.api.Paths.line;

@TeleOp(group = "4")
public class HeadingTest extends OpMode {
    public static double DISTANCE = 36;
    public double loops = 0, lastLoop = 0, loopTime = 0;
    private Path forwards, backwards;
    private boolean forward;
    private Follower follower;

    @Override
    public void init() {
        follower = Constants.create(hardwareMap);
        follower.setPose(new Pose(72, 72, 0));
    }

    @Override
    public void start() {
       // follower.follow(line(new Pose(72,72), new Pose(72,72, Math.toRadians(180))));
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

        follower.hold(new Pose(72,72, Math.toRadians(-180)));

        Logger.recordOutput("heading vel", follower.velocity().omega);

        follower.update();

        telemetry.addData("Calculation Nano Time", System.nanoTime() - nanoBefore);
        telemetry.addData("Calculation Ms", 1e-6 * (System.nanoTime() - nanoBefore));

        telemetry.addData("Loop Time Hz", 1000/loopTime);
        telemetry.addData("Mode", follower.mode());
        telemetry.addData("Following?", follower.following());
        telemetry.addData("Pose", follower.pose());
        telemetry.update();
    }
}
