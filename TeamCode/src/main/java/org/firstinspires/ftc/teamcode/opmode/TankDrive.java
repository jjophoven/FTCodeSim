package org.firstinspires.ftc.teamcode.opmode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.teamcode.drivetrain.Tank;
import org.firstinspires.ftc.teamcode.opmode.base.TeleOpMode;
import org.psilynx.psikit.core.Logger;

@TeleOp
public class TankDrive extends TeleOpMode {
    private Tank tank;

    @Override
    public void init() {
        super.init();
        tank = new Tank(this.hardwareMap);
    }
    @Override
    public void loop() {
        super.loop();

        Logger.recordOutput("heading", localizer.getPose().getHeading());

        tank.drive(
                -gamepad1.left_stick_y,
                -gamepad1.right_stick_x
        );
    }

    @Override
    protected void onFirstDriverInput() {

    }
}
