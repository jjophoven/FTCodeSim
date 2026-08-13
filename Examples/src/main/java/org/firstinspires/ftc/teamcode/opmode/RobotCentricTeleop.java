//package org.firstinspires.ftc.teamcode.opmode;
//
//import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
//import org.firstinspires.ftc.teamcode.opmode.base.TeleOpMode;
//import org.psilynx.psikit.core.Logger;
//
//public class RobotCentricTeleop extends TeleOpMode {
//    double lockedHeading = 0;
//    @Override
//    public void loop() {
//        super.loop();
//
//        double heading = localizer.getPose().getHeading();
//
//        Logger.recordOutput("heading", heading);
//        Logger.recordOutput("headingVelocity", localizer.getVelocity().getHeading());
//
//        double turn;
//        if (Math.abs(gamepad1.right_stick_x) > 0.05) {
//            lockedHeading = heading;
//            turn = -gamepad1.right_stick_x;
//        } else if (localizer.getVelocity().getHeading() > 0.1) {
//            lockedHeading = heading;
//            turn = 0;
//        } else {
//            turn = angleWrap(lockedHeading - heading) * 1 - localizer.getVelocity().getHeading() * 0.1;
//        }
//
//        Logger.recordOutput("lockedHeading", lockedHeading);
//        Logger.recordOutput("turn", turn);
//
//        drivetrain.drive(
//                -gamepad1.left_stick_y,
//                -gamepad1.left_stick_x,
//                turn
//        );
//    }
//
//    private static double angleWrap(double angle) {
//        while (angle <= -Math.PI) angle += 2 * Math.PI;
//        while (angle > Math.PI) angle -= 2 * Math.PI;
//        return angle;
//    }
//
//    @Override
//    protected void onFirstDriverInput() {
//
//    }
//}
