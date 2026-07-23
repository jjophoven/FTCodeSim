package org.codeblooded.models;

import org.codeblooded.fit.MotorModel;

import java.io.IOException;

import static org.codeblooded.fit.ModelFitter.fitAndReportToCSV;

public class BeltMecanumFit {
    public static void main(String[] args) throws IOException {
        //String[] paths = (args.length > 0) ? args : new String[]{"MotorModeling/ftcBeltMecanumMotorData.csv"};
        String[] paths = (args.length > 0) ? args : new String[]{"MotorModeling/singleMotorPlusAndMinus0.001.csv"};

        // ignore vel less than 0.5

        int windowSize = 21;
        int polyDegree = 2;

        // kF and kB should technically be divided by voltage

        // Motor Model:
        // a = kU * u - kV * v * u * max(sgn(uv),0) - kS * sgn(v) - kB * max(-sgn(uv), 0) * v

        // if u*v > 0 (Not Opposing Motion)
        // a = kU * u - kV * v * u * max(sgn(uv),0) - kS * sgn(v)
        // -kU*u + kV * v * u)+  = -a - kS * sgn(v)
        // u(-kU + kV * v) = -a - kS * sgn(v)
        // u = (a + kS * sgn(v)) / (kU - kV*v)

        // if u*v < 0 (Opposing Motion)
        // a = kU * u - kS * sgn(v) - kB * v
        // -kU * u = - kS * sgn(v) - kB * v - a
        // u = (kS * sgn(v) + kB * v + a) / kU

        // piecewise
        // u(a,v) = (a + kS*sgn(v)) / (kU - kV*v), if u*v > 0
        //          (a + kS*sgn(v) + kB*v) / kU,   if u*v < 0

        // R^2 = 0.838686 fit over 10+ trials,
        // very accurate when looked at manually
        // model is slightly ahead of real acceleration due to jerk limitations (but this is good for prediction)

        // Explanation (same as FRC model but with a modified kV term and a extra kB term)
        // When accelerating, the velocity curve is bends a lot and the robot takes longer to speed up the faster it is due to backEMF. However, when the power is cut or duty is low, deceleration becomes effectively constant meaning there is not a persisting backEMF velocity-proportional term. The backEMF is only actively used for the on PWM cycles, thus the kV * -v * u term.
        // When opposing motion (e.g. +1 to -0.0001) there is very harsh velocity-proportional decel due to MOSFET doing regenerative braking stuff. This is accounted for in the kB * -v.

        MotorModel model = new MotorModel(
                (v,d,b) -> d*b,
                (v,d,b) -> Math.signum(v) == Math.signum(d) ? -v * d : 0,
                (v,d,b) -> Math.signum(v) != Math.signum(d) ? -v : 0,
                (v,d,b) -> -Math.signum(v)
        );

        // increasing motion
        MotorModel model3 = new MotorModel(
                (v,d,b) -> d*b,
                (v,d,b) -> -v * d,
                (v,d,b) -> -Math.signum(v)
        );

        // opposing motion
        MotorModel model2 = new MotorModel(
                (v,d,b) -> d*b,
                (v,d,b) -> -v,
                (v,d,b) -> -Math.signum(v)
        );

        fitAndReportToCSV(paths, windowSize, polyDegree, "fitBelt.csv", model);
    }
}
