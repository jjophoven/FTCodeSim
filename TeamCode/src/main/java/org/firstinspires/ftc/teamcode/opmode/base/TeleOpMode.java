package org.firstinspires.ftc.teamcode.opmode.base;

import org.firstinspires.ftc.teamcode.utils.DataSaver;
import org.psilynx.psikit.core.Logger;
import org.psilynx.psikit.core.wpi.math.*;
import org.psilynx.psikit.ftc.autolog.PsiKitAutoLog;

@PsiKitAutoLog
public abstract class TeleOpMode extends RobotOpMode {
    protected boolean initialized = false;

    @Override
    public void loop() {
        super.loop();

        double moveDelta = 0.15;
        if (!initialized &&
                (Math.abs(gamepad1.right_stick_x) > moveDelta
                        || Math.abs(gamepad1.left_stick_y) > moveDelta
                        || Math.abs(gamepad1.right_stick_x) > moveDelta)) {
            onFirstDriverInput();
            initialized = true;
        }

        localizer.update();

        Logger.recordOutput("heading", localizer.getPose().getHeading());

        drivetrain.driveFieldCentric(localizer.getPose().getHeading(), (gamepad1.dpad_left ? 1 : 0) - (gamepad1.dpad_right ? 1 : 0), (gamepad1.dpad_up ? 1 : 0) - (gamepad1.dpad_down ? 1 : 0), -gamepad1.right_stick_x);
    }

    // allows you to hit play before beginning of teleop and only initialize and move servos after gamepad input has occurred.
    protected abstract void onFirstDriverInput();

    // does not save alliance in teleop
    @Override
    public void stop() {
        DataSaver.save(pose);
    }
}
