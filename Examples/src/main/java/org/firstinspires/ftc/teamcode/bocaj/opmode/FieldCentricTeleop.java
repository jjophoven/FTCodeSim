package org.firstinspires.ftc.teamcode.bocaj.opmode;

import com.pedropathing.math.Pose;
import com.pedropathing.revhub.ManualDrive;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.teamcode.bocaj.opmode.base.TeleOpMode;
import org.firstinspires.ftc.teamcode.bocaj.utils.Alliance;

@TeleOp
public class FieldCentricTeleop extends TeleOpMode {
    LockController headingLock = new LockController(0.05, 0.05, 2, 0.1876, 0.0, true); // 2.77, 0.177
    LockController xLock = new LockController(0.05, 0.05, 0.05, 0.0644, 0.0021, false);
    LockController yLock = new LockController(0.05, 0.05, 0.05, 0.0644, 0.0021, false);

    @Override
    public void init() {
        super.init();
        follower.setPose(new Pose(72,72,Math.PI/4));
    }

    @Override
    public void loop() {
        super.loop();

        telemetry.addData("xLock", xLock.getLockedTarget());
        telemetry.addData("yLock", yLock.getLockedTarget());
        telemetry.addData("headingLock", headingLock.getLockedTarget());
        telemetry.addData("pose", follower.pose());
        telemetry.addData("velocity", follower.velocity().toVector2D().magnitude());
        //telemetry.update();

        follower.manual(ManualDrive.fieldCentric(
                gamepad1.left_stick_x,
                -gamepad1.left_stick_y,
                -gamepad1.right_stick_x,
                -follower.pose().heading(),
                alliance == Alliance.RED ? Math.PI/2 : -Math.PI/2
        ));
//
//        if (gamepad1.right_bumper) {
//            follower.manual(ManualDrive.fieldCentric(
//                    xLock.getOutput(gamepad1.left_stick_x/3, follower.pose().x(), follower.velocity().vx),
//                    yLock.getOutput(-gamepad1.left_stick_y/3, follower.pose().y(), follower.velocity().vy),
//                    headingLock.getOutput(-gamepad1.right_stick_x/3, follower.pose().heading(), follower.velocity().omega),
//                    follower.pose().heading(),
//                    alliance == Alliance.RED ? Math.PI : 0
//            ));
//        }
//        else {
//            follower.manual(ManualDrive.fieldCentric(
//                    xLock.getOutput(gamepad1.left_stick_x, follower.pose().x(), follower.velocity().vx),
//                    yLock.getOutput(-gamepad1.left_stick_y, follower.pose().y(), follower.velocity().vy),
//                    headingLock.getOutput(-gamepad1.right_stick_x, follower.pose().heading(), follower.velocity().omega),
//                    follower.pose().heading(),
//                    alliance == Alliance.RED ? Math.PI : 0
//            ));
//        }
    }

    private static double angleWrap(double angle) {
        while (angle <= -Math.PI) angle += 2 * Math.PI;
        while (angle > Math.PI) angle -= 2 * Math.PI;
        return angle;
    }

    @Override
    protected void onFirstDriverInput() {

    }
}
