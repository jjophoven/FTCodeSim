package org.firstinspires.ftc.teamcode.bocaj.opmode;

import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.teamcode.bocaj.opmode.base.TeleOpMode;

@TeleOp
public class RobotCentricTeleop extends TeleOpMode {
    double speedScalar = 1;

    @Override
    public void init() {
        super.init();
        follower.setPose(new Pose(72,72,0));
    }

    @Override
    public void init_loop() {
        if (gamepad1.dpadDownWasPressed()) {
            speedScalar -= 0.05;
        }
        if (gamepad1.dpadUpWasPressed()) {
            speedScalar += 0.05;
        }
        speedScalar = Math.max(0, Math.min(1, speedScalar));
        telemetry.addData("Speed Scalar %.0f%%", speedScalar * 100);
        telemetry.update();
    }

    @Override
    public void loop() {
        super.loop();

        follower.manual(
                speedScalar * -gamepad1.left_stick_y,
                speedScalar * -gamepad1.left_stick_x,
                speedScalar * -gamepad1.right_stick_x
        );

        telemetry.addData("Pose", follower.pose());
        telemetry.update();
    }

    @Override
    protected void onFirstDriverInput() {

    }
}
