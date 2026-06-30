package org.jjophoven.fakehardware.drivetrain;

import org.psilynx.psikit.core.Logger;
import org.psilynx.psikit.core.wpi.math.Pose2d;
import org.psilynx.psikit.core.wpi.math.Rotation2d;

public class MotionVector {
    public final double x;
    public final double y;
    public final double theta;

    public MotionVector(double x, double y, double heading) {
        this.x = x;
        this.y = y;
        this.theta = heading;
    }

    public MotionVector toFieldFrame(double heading) {
        return rotate(-heading);
    }

    public MotionVector toRobotFrame(double heading) {
        return rotate(heading);
    }

    public MotionVector rotate(double heading) {
        double cos = Math.cos(heading);
        double sin = Math.sin(heading);
        double xAccel = x * cos - y * sin;
        double yAccel = x * sin + y * cos;
        return new MotionVector(xAccel, yAccel, this.theta);
    }

    public MotionVector step(MotionVector motion, double deltaTime) {
        return new MotionVector(this.x + motion.x * deltaTime, this.y + motion.y * deltaTime, this.theta + motion.theta * deltaTime);
    }

    public void log(String key) {
        Logger.recordOutput(key + " Pedro coords (inches)", toPose2d());
        Logger.recordOutput(key + " ftc coords (m)", toFtcCoords().toPose2d());
    }

    public MotionVector toFtcCoords() {
        double inchesPerMeter = 39.37;
        return new MotionVector(-y / inchesPerMeter, x / inchesPerMeter, theta + Math.PI/2);
    }

    public Pose2d toPose2d() {
        return new Pose2d(x, y, new Rotation2d(theta));
    }

    public MotionVector plus(MotionVector other) {
        return new MotionVector(this.x + other.x, this.y + other.y, this.theta + other.theta);
    }
}