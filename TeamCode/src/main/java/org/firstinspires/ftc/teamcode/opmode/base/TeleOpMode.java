package org.firstinspires.ftc.teamcode.opmode.base;

import org.firstinspires.ftc.teamcode.utils.DataSaver;
import org.psilynx.psikit.core.Logger;
import org.psilynx.psikit.core.wpi.math.*;
import org.psilynx.psikit.ftc.autolog.PsiKitAutoLog;

// TODO implement gate blocking (preventing drivers from going near penalty)
// TODO zone detection/autoshooting / help align heading

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
    }

    // allows you to hit play before beginning of teleop and only initialize and move servos after gamepad input has occurred.
    protected abstract void onFirstDriverInput();

    // does not save alliance in teleop
    @Override
    public void stop() {
        DataSaver.save(pose);
    }
}
