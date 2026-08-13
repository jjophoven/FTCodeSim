package org.firstinspires.ftc.teamcode.bocaj.opmode.base;

//
//import org.psilynx.psikit.ftc.autolog.PsiKitAutoLog;

// TODO implement gate blocking (preventing drivers from going near penalty)
// TODO zone detection/autoshooting / help align heading

public abstract class TeleOpMode extends BaseOpMode {
    protected boolean initialized = false;

    @Override
    public void init() {
        isAuto = false;
        super.init();
    }

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
}
