package org.firstinspires.ftc.teamcode.drivetrain;

import android.annotation.SuppressLint;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class Mecanum {
    private final CachingMotor[] motors;
    private final double[] wheelPowers = new double[4];

    private static final int FL = 0;
    private static final int BL = 1;
    private static final int FR = 2;
    private static final int BR = 3;
    private boolean manual;

    public Mecanum(HardwareMap map) {
        double powerDeadband = 0.01;

        motors = new CachingMotor[]{
                new CachingMotor(map.get(DcMotorEx.class, "frontLeft"), powerDeadband),

                new CachingMotor(map.get(DcMotorEx.class, "frontRight"), powerDeadband),
                new CachingMotor(map.get(DcMotorEx.class, "backLeft"), powerDeadband),
                new CachingMotor(map.get(DcMotorEx.class, "backRight"), powerDeadband)
        };
    }

    public void driveFieldCentric(double heading, double x, double y, double turn) {
        double cos = Math.cos(-heading);
        double sin = Math.sin(-heading);
        double forward = y * cos - x * sin;
        double strafe  = y * sin + x * cos;

        drive(forward, strafe, turn);
    }

    public void drive(double forward, double strafe, double turn) {
        double fl = forward + strafe + turn;
        double bl = forward - strafe + turn;
        double fr = forward - strafe - turn;
        double br = forward + strafe - turn;

        double max = Math.max(1.0, Math.max(Math.abs(fl), Math.max(Math.abs(bl), Math.max(Math.abs(fr), Math.abs(br)))));

        wheelPowers[FL] = fl / max;
        wheelPowers[BL] = bl / max;
        wheelPowers[FR] = fr / max;
        wheelPowers[BR] = br / max;

        for (int i = 0; i < wheelPowers.length; i++) {
            motors[i].setPower(wheelPowers[i]);
        }
    }
}