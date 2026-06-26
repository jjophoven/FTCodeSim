package org.firstinspires.ftc.teamcode.fake;

import org.psilynx.psikit.core.Logger;
import org.psilynx.psikit.core.wpi.math.Pose2d;
import org.psilynx.psikit.core.wpi.math.Rotation2d;
import util.MotorModel;

public class FakeMecanum {
    private final FakeMotor[] motors;

    private static final int FL = 0;
    private static final int BL = 1;
    private static final int FR = 2;
    private static final int BR = 3;

    public FakeMecanum(FakeMotor[] motors) {
        this.motors = motors;
    }

    double fieldX = 0;
    double fieldY = 0;
    public static double heading = 0;

    double fieldXVel = 0;
    double fieldYVel = 0;
    double headingVel = 0;

    double frVel;
    double flVel;
    double brVel;
    double blVel;

public void update(double deltaTime) {
    double fr = motors[FR].getPower();
    double fl = motors[FL].getPower();
    double br = motors[BR].getPower();
    double bl = motors[BL].getPower();

    MotorModel model = MotorModel.fromString("a=Au-Bv*abs(d)-Cv-Dsgn(v)");

    double[] coefficients = new double[] {0.580678,
            1.222632,
            0.125799,
            3 //0.111082
    };

    double voltage = 13;
    double frAccel = model.predict(coefficients, frVel, fr, voltage);
    double flAccel = model.predict(coefficients, flVel, fl, voltage);
    double brAccel = model.predict(coefficients, brVel, br, voltage);
    double blAccel = model.predict(coefficients, blVel, bl, voltage);

    Logger.recordOutput("Mecanum/fr", fr);
    Logger.recordOutput("Mecanum/fl", fl);
    Logger.recordOutput("Mecanum/br", br);
    Logger.recordOutput("Mecanum/bl", bl);
    Logger.recordOutput("Mecanum/frAccel", frAccel);
    Logger.recordOutput("Mecanum/flAccel", flAccel);
    Logger.recordOutput("Mecanum/brAccel", brAccel);
    Logger.recordOutput("Mecanum/blAccel", blAccel);
    Logger.recordOutput("Mecanum/frVel", frVel);
    Logger.recordOutput("Mecanum/flVel", flVel);
    Logger.recordOutput("Mecanum/brVel", brVel);
    Logger.recordOutput("Mecanum/blVel", blVel);

//    L = half the wheelbase (center to front/back wheel)
//    W = half the track width (center to left/right wheel)
    // R = L + W
    double R = 0.2286 + 0.2286;

    double forwardAccel = (frAccel + flAccel + brAccel + blAccel) / 4;
    double strafeAccel  = (flAccel - frAccel + brAccel - blAccel) / 4;
    double headingAccel = -(brAccel - blAccel + frAccel - flAccel) / (4 * R);

    double cos = Math.cos(heading);
    double sin = Math.sin(heading);
    double xAccel = forwardAccel * cos - strafeAccel * sin;
    double yAccel  = forwardAccel * sin + strafeAccel * cos;

    Logger.recordOutput("Mecanum/fieldXAccel", xAccel);
    Logger.recordOutput("Mecanum/fieldYAccel", yAccel);

//
//    if (Math.abs(xAccel) > 0.2 || Math.abs(fieldXVel) > 0.05) {
        fieldXVel += xAccel * deltaTime;
//    }
//    else {
//        fieldXVel = 0;
//    }
//
//    if (Math.abs(yAccel) > 0.2 || Math.abs(fieldYVel) > 0.05) {
        fieldYVel += yAccel * deltaTime;
//    }
//    else {
//        fieldYVel = 0;
//    }
//
//    if (Math.abs(headingAccel) > 0.2 || Math.abs(headingVel) > 0.05) {
        headingVel += headingAccel * deltaTime;
//    }
//    else {
//        headingVel = 0;
//    }

    Logger.recordOutput("Mecanum/fieldXVel", fieldXVel);
    Logger.recordOutput("Mecanum/fieldYVel", fieldYVel);
    Logger.recordOutput("Mecanum/headingVel", headingVel);

    fieldX += fieldXVel * deltaTime;
    fieldY += fieldYVel * deltaTime;
    heading += headingVel * deltaTime;

    double cos2 = Math.cos(-heading);
    double sin2 = Math.sin(-heading);
    double forwardVel = fieldXVel * cos2 - fieldYVel * sin2;
    double strafeVel  = fieldXVel * sin2 + fieldYVel * cos2;

    flVel = forwardVel + strafeVel + headingVel * R;
    blVel = forwardVel - strafeVel + headingVel * R;
    frVel = forwardVel - strafeVel - headingVel * R;
    brVel = forwardVel + strafeVel - headingVel * R;

    Logger.recordOutput("Mecanum/pose", new Pose2d(fieldX, fieldY, new Rotation2d(heading)));
}
}