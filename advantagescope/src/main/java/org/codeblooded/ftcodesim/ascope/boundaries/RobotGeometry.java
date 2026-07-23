package org.codeblooded.ftcodesim.ascope.boundaries;

public class RobotGeometry {
    double width;
    double length;

    /** The x offset (forwards) from the drivetrain center to the center of the robot */
    public double centerOffsetXFromDrivetrain;

    /** The y offset (lateral) from the drivetrain center to the center of the robot */
    public double centerOffsetYFromDrivetrain;

    public RobotGeometry(double width, double length, double centerOffsetXFromDrivetrain, double centerOffsetYFromDrivetrain) {
        this.width = width;
        this.length = length;
        this.centerOffsetXFromDrivetrain = centerOffsetXFromDrivetrain;
        this.centerOffsetYFromDrivetrain = centerOffsetYFromDrivetrain;
    }

    public MotionVector[] corners(MotionVector drivetrainPose) {
        double cos = Math.cos(drivetrainPose.theta());
        double sin = Math.sin(drivetrainPose.theta());

        double fx = cos;
        double fy = sin;

        double lx = -sin;
        double ly = cos;

        double cx =
                drivetrainPose.x()
                        + fx * centerOffsetXFromDrivetrain
                        + lx * centerOffsetYFromDrivetrain;

        double cy =
                drivetrainPose.y()
                        + fy * centerOffsetXFromDrivetrain
                        + ly * centerOffsetYFromDrivetrain;

        double halfL = length / 2.0;
        double halfW = width / 2.0;

        return new MotionVector[]{
                new MotionVector(
                        cx + fx * halfL + lx * halfW,
                        cy + fy * halfL + ly * halfW
                ),
                new MotionVector(
                        cx + fx * halfL - lx * halfW,
                        cy + fy * halfL - ly * halfW
                ),
                new MotionVector(
                        cx - fx * halfL - lx * halfW,
                        cy - fy * halfL - ly * halfW
                ),
                new MotionVector(
                        cx - fx * halfL + lx * halfW,
                        cy - fy * halfL + ly * halfW
                )
        };
    }
}