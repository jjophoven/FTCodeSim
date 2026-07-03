package org.firstinspires.ftc.teamcode.opmode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.teamcode.opmode.base.TeleOpMode;
import org.psilynx.psikit.core.Logger;

@TeleOp
public class RobotCentricTeleop extends TeleOpMode {
    @Override
    public void loop() {
        super.loop();

        Logger.recordOutput("heading", localizer.getPose().getHeading());

        drivetrain.drive(
                -gamepad1.left_stick_y,
                -gamepad1.left_stick_x,
                -gamepad1.right_stick_x
        );
    }

    @Override
    protected void onFirstDriverInput() {

    }
}
