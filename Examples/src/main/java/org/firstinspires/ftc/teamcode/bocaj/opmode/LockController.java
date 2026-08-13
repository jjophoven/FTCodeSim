package org.firstinspires.ftc.teamcode.bocaj.opmode;

public class LockController {
    double lockedTarget = 0;
    boolean decelerating = false;
    double lockingInputThreshold;
    double maximumLockingVelocity;
    double kP;
    double kD;
    double kQ;
    boolean headingLock;

    public LockController(double lockingInputThreshold, double maximumLockingVelocity, double kP, double kD, double kQ, boolean headingLock) {
        this.lockingInputThreshold = lockingInputThreshold;
        this.maximumLockingVelocity = maximumLockingVelocity;
        this.kP = kP;
        this.kD = kD;
        this.kQ = kQ;
        this.headingLock = headingLock;
    }

    public double getOutput(double input, double current, double velocity) {
        if (Math.abs(input) > lockingInputThreshold) {
            decelerating = true;
            return input;
        } else if (Math.abs(velocity) < maximumLockingVelocity && decelerating) {
            lockedTarget = current;
            decelerating = false;
            return 0;
        } else if (decelerating) {
            return 0;
        } else { // heading lock
            double error = lockedTarget - current;
            if (headingLock) {
                error = angleWrap(error);
            }
           // double kS = (Math.signum(error) * 0.625) / voltageSensor.getVoltage();
            return kP * (error - velocity * kD - velocity * velocity * kQ);
//            if (Math.abs(turn) <= Math.abs(kS) + 0.01) {
//                turn = 0;
 //           }
        }
    }

    public double getLockedTarget() {
        return lockedTarget;
    }

    private static double angleWrap(double angle) {
        while (angle <= -Math.PI) angle += 2 * Math.PI;
        while (angle > Math.PI) angle -= 2 * Math.PI;
        return angle;
    }
}
