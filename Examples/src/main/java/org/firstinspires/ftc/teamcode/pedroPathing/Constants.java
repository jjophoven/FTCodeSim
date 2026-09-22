package org.firstinspires.ftc.teamcode.pedroPathing;

import com.pedropathing.algorithm.ForesightConfig;
import com.pedropathing.algorithm.Foresight;
import com.pedropathing.controllers.Controller;
import com.pedropathing.follower.Follower;
import com.pedropathing.math.Matrix;
import com.pedropathing.math.Vector2D;
import com.pedropathing.revhub.drivetrains.Mecanum;
import com.pedropathing.revhub.drivetrains.MecanumConfig;
import com.pedropathing.revhub.localizers.OctoQuadConfig;
import com.pedropathing.revhub.localizers.OctoQuadLocalizer;
import com.pedropathing.revhub.localizers.PinpointConfig;
import com.qualcomm.hardware.digitalchickenlabs.OctoQuad;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

public class Constants {
    public static MecanumConfig driveConfig = new MecanumConfig(
            c -> {
                c.frontLeftName.set("frontLeft");
                c.backLeftName.set("backLeft");
                c.frontRightName.set("frontRight");
                c.backRightName.set("backRight");

                c.frontLeftDirection.set(DcMotorSimple.Direction.REVERSE);
                c.backLeftDirection.set(DcMotorSimple.Direction.REVERSE);
                c.frontRightDirection.set(DcMotorSimple.Direction.FORWARD);
                c.backRightDirection.set(DcMotorSimple.Direction.FORWARD);

                c.manualBrakeMode.set(true);
            }
    );

    public static OctoQuadConfig localizerConfig = new OctoQuadConfig(
            c -> {
                c.name.set("octoquad");
                c.encoderResolutionUnit.set(DistanceUnit.MM);
                c.offsetUnits.set(DistanceUnit.INCH);
                c.xPodDirection.set(OctoQuad.EncoderDirection.REVERSE);
                c.yPodDirection.set(OctoQuad.EncoderDirection.REVERSE);
                c.ticksPerUnit.set(37.25);
//                c.xPodOffset.set(-33.5); // ~1.3 in, 2.3 -> -58.42mm
//                c.yPodOffset.set(-63.0); // ~2.48 in, 0.3 -> -7.62mm

                // end result should be y = -2.3, x = +0.3

                // just y needs to be negative!!! for octoquad
                // and switch x and y

//                c.xPodOffset.set(-2.3875); // 2.3 -> -58.42mm
//                c.yPodOffset.set(-0.3); // 0.3 -> -7.62mm // -1.175

//                c.xPodOffset.set(-2.3875); // (inputted 2.3)
//                c.yPodOffset.set(1.175);

//                c.xPodOffset.set(-1.175);
//                c.yPodOffset.set(-2.3875);

                c.xPodOffset.set(-1.3);
                c.yPodOffset.set(-2.48);

                c.headingScalar.set(((3600 + 55.12)/3600));
            }
    );

    static PinpointConfig pinpointConfig = new PinpointConfig(
            c -> {
                c.name.set("p");

                c.xPodDirection.set(GoBildaPinpointDriver.EncoderDirection.FORWARD);
                c.yPodDirection.set(GoBildaPinpointDriver.EncoderDirection.REVERSED);

                c.xPodOffset.set(4.1871);
                c.yPodOffset.set(-6.433);

                c.podType.set(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD);
            }
    );

    public static ForesightConfig foresightConfig = new ForesightConfig(
            c -> {
                Controller primaryTranslationalForward = Controller.proportional(0.226493318780903);
                Controller secondaryTranslationalForward = Controller.proportional(0.08368322927660432);
                Controller primaryTranslationalLateral = Controller.proportional(0.5015381114306278);
                Controller secondaryTranslationalLateral = Controller.proportional(0.18530493082846347);

                c.forwardTranslational.set(Controller.piecewise(secondaryTranslationalForward).put(2.5, primaryTranslationalForward));
                c.strafeTranslational.set(Controller.piecewise(secondaryTranslationalLateral).put(2.5, primaryTranslationalLateral));

                c.coast.set(Controller.proportionalFeedforward(0.012957603483588636));
                c.brake.set(Controller.proportionalFeedforward(0.01101396296105034));

                c.headingFeedback.set(Controller.proportional(2.447421655157805));
                c.headingBrakeCoefficients.set(Vector2D.cartesian(0.04730634263294, 0.007540928288975995));
                c.headingDriveRatio.set(1.0);
                c.headingDeviationTolerance.set(0.001);

//                    c.linearBrakeCoefficients.set(Matrix.diag(0.038561008825678214, 0.06927454668889409));
//                    c.quadraticBrakeCoefficients.set(Matrix.diag(0.002985751705369463, 0.0020371201468262264));
                c.linearBrakeCoefficients.set(Matrix.diag(0.06447, 0.06447));
                c.quadraticBrakeCoefficients.set(Matrix.diag(0.0024, 0.0024));

                c.maxAchievableForwardVelocity.set(82.80318372444954);
                c.maxAchievableStrafeVelocity.set(66.35440133280454);
                c.naturalForwardDeceleration.set(42.84137414187512);
                c.naturalStrafeDeceleration.set(62.12942961650345);
            }
    );

    public static Follower create(HardwareMap h) {
        return new Follower(new OctoQuadLocalizer(h, localizerConfig), new Mecanum(h, driveConfig), new Foresight(foresightConfig));
    }
}
