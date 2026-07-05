package org.jjophoven.simhardware.drivetrain;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
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
        return rotate(heading);
    }

    public MotionVector toRobotFrame(double heading) {
        return rotate(-heading);
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
        Logger.recordOutput(key + " Pedro coords (inches)", toWPIPose());
        Logger.recordOutput(key + " ftc coords (m)", toFtcCoords().toWPIPose());
    }

    public MotionVector toFtcCoords() {
        double inchesPerMeter = 39.37;
        double halfField = 141.5/2;
        return new MotionVector(-(y - halfField) / inchesPerMeter, (x - halfField) / inchesPerMeter, theta + Math.PI/2);
    }

    public Pose2d toWPIPose() {
        return new Pose2d(x, y, new Rotation2d(theta));
    }

    public Pose2D toPose2D() {
        return new Pose2D(DistanceUnit.INCH,
                x,
                y,
                AngleUnit.RADIANS,
                AngleUnit.normalizeRadians(theta));
    }

    public MotionVector plus(MotionVector other) {
        return new MotionVector(this.x + other.x, this.y + other.y, this.theta + other.theta);
    }
}