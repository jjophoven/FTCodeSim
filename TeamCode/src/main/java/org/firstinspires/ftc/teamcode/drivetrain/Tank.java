package org.firstinspires.ftc.teamcode.drivetrain;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class Tank {
    private final CachingMotor[] motors;
    private final double[] wheelPowers = new double[4];

    private static final int FL = 0;
    private static final int FR = 1;
    private static final int BL = 2;
    private static final int BR = 3;

    public Tank(HardwareMap map) {
        double powerDeadband = 0.01;

        motors = new CachingMotor[]{
                new CachingMotor(map.get(DcMotorEx.class, "frontLeft"), powerDeadband),
                new CachingMotor(map.get(DcMotorEx.class, "frontRight"), powerDeadband),
                new CachingMotor(map.get(DcMotorEx.class, "backLeft"), powerDeadband),
                new CachingMotor(map.get(DcMotorEx.class, "backRight"), powerDeadband)
        };

        for (CachingMotor motor : motors) {
            motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        }
    }

    public void driveFieldCentric(double heading, double x, double turn) {
        double forward = x * Math.cos(-heading);

        drive(forward, turn);
    }

    public void drive(double forward, double turn) {
        double left = forward + turn;
        double right = forward - turn;

        double max = Math.max(1.0, Math.max(Math.abs(left), Math.abs(right)));

        left /= max;
        right /= max;

        wheelPowers[FL] = left;
        wheelPowers[BL] = left;
        wheelPowers[FR] = right;
        wheelPowers[BR] = right;

        for (int i = 0; i < wheelPowers.length; i++) {
            motors[i].setPower(wheelPowers[i]);
        }
    }
}