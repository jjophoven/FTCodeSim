package org.firstinspires.ftc.teamcode.opmode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.teamcode.Alliance;
import org.firstinspires.ftc.teamcode.opmode.base.TeleOpMode;
import org.psilynx.psikit.core.Logger;

@TeleOp
public class FieldCentricTeleop extends TeleOpMode {
    @Override
    public void loop() {
        super.loop();

        Logger.recordOutput("heading", localizer.getPose().getHeading());

        double heading = localizer.getPose().getHeading();
        if (alliance == Alliance.BLUE) {
            heading += Math.PI;
        }

        drivetrain.driveFieldCentric(
                heading,
                -gamepad1.left_stick_y,
                gamepad1.left_stick_x,
                gamepad1.right_stick_x
        );
    }

    @Override
    protected void onFirstDriverInput() {

    }
}
