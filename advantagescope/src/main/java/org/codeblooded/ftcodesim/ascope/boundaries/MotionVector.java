package org.codeblooded.ftcodesim.ascope.boundaries;

import org.codeblooded.ftcodesim.ascope.AdvantageScopeRunner;
import org.codeblooded.ftcodesim.ascope.SourceType;
import org.psilynx.psikit.core.Logger;
import org.psilynx.psikit.core.wpi.math.Pose2d;
import org.psilynx.psikit.core.wpi.math.Rotation2d;
import org.psilynx.psikit.core.wpi.math.Translation3d;

public class MotionVector {
    public double x;
    public double y;
    public double theta;

    public MotionVector(double x, double y, double theta) {
        this.x = x;
        this.y = y;
        this.theta = theta;
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public double theta() {
        return theta;
    }

    public MotionVector(double x, double y) {
        this(x, y, 0);
    }

    public MotionVector subtractX(double x) {
        return new MotionVector(this.x - x, this.y, this.theta);
    }

    public MotionVector addY(double y) {
        return new MotionVector(this.x, this.y + y, this.theta);
    }

    public double dot(MotionVector other) {
        return this.x * other.x + this.y * other.y;
    }

    public MotionVector scale(double scalar) {
        return new MotionVector(x * scalar, y * scalar, theta);
    }

    public double magnitude() {
        return Math.sqrt(x * x + y * y);
    }

    public MotionVector unitVector() {
        double mag = magnitude();
        return new MotionVector(x / mag, y / mag, 0);
    }

    public MotionVector projectOnto(MotionVector direction) {
        double magSq = direction.x * direction.x + direction.y * direction.y;

        if (magSq < 1e-9) {
            return new MotionVector(0, 0, 0);
        }

        double dot = this.x * direction.x + this.y * direction.y;
        double scale = dot / magSq;

        return new MotionVector(
                direction.x * scale,
                direction.y * scale,
                0
        );
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

    public void log(String key, SourceType sourceType) {
        log(key);
        AdvantageScopeRunner.INSTANCE.addSource("RealOutputs/" + key + " ftc coords (m)", sourceType);
    }


    public void log3d(String key, double z) {
        Logger.recordOutput(key + " Pedro coords (inches)", toWPITranslation(z));
        Logger.recordOutput(key + " ftc coords (m)", toFtcCoords().toWPITranslation(z / 39.37));
    }

    public void log3d(String key, double z, SourceType sourceType) {
        log3d(key, z);
        AdvantageScopeRunner.INSTANCE.addSource("RealOutputs/" + key + " ftc coords (m)", sourceType);
    }

    public MotionVector toFtcCoords() {
        double inchesPerMeter = 39.37;
        double halfField = 141.5 / 2;
        return new MotionVector(-(y - halfField) / inchesPerMeter, (x - halfField) / inchesPerMeter, theta + Math.PI / 2);
    }

    public Pose2d toWPIPose() {
        return new Pose2d(x, y, new Rotation2d(theta));
    }

    public Translation3d toWPITranslation(double z) {
        return new Translation3d(x, y, z);
    }

    public MotionVector plus(MotionVector other) {
        return new MotionVector(this.x + other.x, this.y + other.y, this.theta + other.theta);
    }

    public MotionVector minus(MotionVector other) {
        return new MotionVector(this.x - other.x, this.y - other.y, this.theta - other.theta);
    }

    public MotionVector zeroHeading() {
        return new MotionVector(this.x, this.y, 0);
    }

    public MotionVector withHeading(double heading) {
        return new MotionVector(this.x, this.y, heading);
    }
}