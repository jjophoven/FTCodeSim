package org.firstinspires.ftc.teamcode.pedroPathing;

import com.pedropathing.algorithm.Foresight;
import com.pedropathing.algorithm.ForesightConfig;
import com.pedropathing.controllers.Controller;
import com.pedropathing.follower.Follower;
import com.pedropathing.math.Matrix;
import com.pedropathing.revhub.drivetrains.Mecanum;
import com.pedropathing.revhub.drivetrains.MecanumConfig;
import com.pedropathing.revhub.localizers.OctoQuadConfig;
import com.pedropathing.revhub.localizers.PinpointConfig;
import com.qualcomm.hardware.digitalchickenlabs.OctoQuad;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.bocaj.pedro.PredictiveConfig;

import java.util.OptionalDouble;

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
                c.offsetUnits.set(DistanceUnit.MM);
                c.xPodDirection.set(OctoQuad.EncoderDirection.REVERSE);
                c.yPodDirection.set(OctoQuad.EncoderDirection.REVERSE);
                c.ticksPerUnit.set(37.25);
                c.xPodOffset.set(-33.5);
                c.yPodOffset.set(-63.0);
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
                //c.normalFeedforward.set(-0.0645);
//                c.headingFeedforward.set(
//                        Controller.staticFromError(0.14).plus(Controller.staticBias(0.14)));
                c.forwardTranslationalController.set(Controller.pid(0.2 , 0, 0));
                //.plus(Controller.staticFeedforward(0.015)));
                c.lateralTranslationalController.set(Controller.pid(0.2 * 85.17/66.8431, 0, 0));
                //c.headingDeviationTolerance.set(Math.toRadians(45.0));
//                        .plus(Controller.staticFeedforward(-0.0645)));
                //   .plus(Controller.staticFromError(0.0645)));
                // 2.7 * e - 0.18v = x * (e - 0.18v)
//                c.brakeController.set(Controller.pid(0.0196, 0, 0) // 0.0196
//                        .plus(Controller.dynamicFeedforward(0.0129)));
                c.brakeController.set(Controller.pid(0, 0, 0) // 0.0196
                        .plus(Controller.dynamicFeedforward(0.0129))); //1/85
                //.plus(Controller.staticFeedforward(0.05)));
                //c.turnBeforeDriving.set(true);
//                c.brakeAccelFeedforward.set(Controller.dynamicFeedforward(0.0021)); // 0.0021
//                c.brakeAccelFeedforward.set(Controller.dynamicFeedforward(0.042)); // 0.0021
                c.brakeAccelFeedforward.set(Controller.dynamicFeedforward(0.021)); // 0.0021
                c.headingDriveRatio.set(1.0);
                c.timeoutConstraint.set(1000.0);
//                c.maxAccelerationConstraint.set(70.0);
//                c.maxVelocityConstraint.set(40.0);
                c.headingController.set(Controller.pid(2.77, 0, 0.177));
                c.linearBrakeCoefficients.set(Matrix.diag(.0644, .0644));
                c.quadraticBrakeCoefficients.set(Matrix.diag(.0021, .0021));

                c.maxAchievableForwardVelocity.set(85.17);
                c.maxAchievableStrafeVelocity.set(66.8431);

                c.naturalForwardDeceleration.set(49.09);
                c.naturalStrafeDeceleration.set(49.09);
            }
    );

    // RobotConstant.getVoltage()

    static PredictiveConfig predictiveConfig = new PredictiveConfig(
            c -> {
                c.forwardTranslationalController.set(Controller.pid(0.2 , 0, 0));
                c.lateralTranslationalController.set(Controller.pid(0.2 * 85.17/66.8431, 0, 0));
                c.coastController.set(Controller.pid(0.015, 0, 0)
                        .plus(Controller.dynamicFeedforward(0.0129))
                        .plus(Controller.staticFeedforward(0.05))); // TODO make depend on voltage
                c.timeoutConstraint.set(1000.0);
                //c.maxAccelerationConstraint.set(OptionalDouble.of(70.0));
                //c.maxVelocityConstraint.set(OptionalDouble.of(40.0));
                c.headingController.set(Controller.pid(2.77, 0, 0.177));

                c.linearBrakeCoefficients.set(Matrix.diag(.0644, .0644));
                c.quadraticBrakeCoefficients.set(Matrix.diag(.0021, .0021));

                c.maxAchievableForwardVelocity.set(85.17);
                c.maxAchievableStrafeVelocity.set(66.8431);

                c.naturalForwardDeceleration.set(49.09);
                c.naturalStrafeDeceleration.set(49.09); // put here

                c.brakeOvershootBias.set(1.0); // undershoot bias
                //c.brakeAtEnd
                c.maxCoastDecelerationConstraint.set(OptionalDouble.of(49.09));
                c.coastDownToVelocity.set(20.0);
                c.brakeAtEnd.set(true);
            }
    );

    public static Follower create(HardwareMap h) {
        return new Follower(new com.pedropathing.revhub.localizers.OctoQuad(h, localizerConfig), new Mecanum(h, driveConfig), new Foresight(foresightConfig));
    }
}
