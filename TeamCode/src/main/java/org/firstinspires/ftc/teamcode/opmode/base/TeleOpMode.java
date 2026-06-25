package org.firstinspires.ftc.teamcode.opmode.base;

import org.firstinspires.ftc.teamcode.utils.DataSaver;
import org.psilynx.psikit.core.Logger;
import org.psilynx.psikit.core.wpi.math.Pose2d;
import org.psilynx.psikit.core.wpi.math.Rotation2d;
import org.psilynx.psikit.core.wpi.math.Translation2d;
import org.psilynx.psikit.ftc.autolog.PsiKitAutoLog;

@PsiKitAutoLog
public abstract class TeleOpMode extends RobotOpMode {
    protected boolean initialized = false;

    double x = 0;
    double y = 0;
    double theta = 0;

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

        double delta = 0.05;

        x += (gamepad1.dpad_left ? 1 : 0) * delta - (gamepad1.dpad_right ? 1 : 0) * delta;
        y += (gamepad1.dpad_up ? 1 : 0) * delta - (gamepad1.dpad_down ? 1 : 0) * delta;
        theta += gamepad1.right_stick_x * delta;

        Logger.recordOutput("pose", new Pose2d(new Translation2d(x,y), new Rotation2d(theta)));
    }

    // allows you to hit play before beginning of teleop and only initialize and move servos after gamepad input has occurred.
    protected abstract void onFirstDriverInput();

    // does not save alliance in teleop
    @Override
    public void stop() {
        DataSaver.save(pose);
    }
}
