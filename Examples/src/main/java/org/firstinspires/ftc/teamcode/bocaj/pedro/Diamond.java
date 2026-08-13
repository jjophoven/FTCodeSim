package org.firstinspires.ftc.teamcode.bocaj.pedro;

public class Diamond {
    public static double interpolateRadius(double halfWidth, double halfHeight, double theta) {
        return 1.0 / (Math.abs(Math.cos(theta)) / halfWidth + Math.abs(Math.sin(theta)) / halfHeight);
    }
}