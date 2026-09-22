import com.pedropathing.algorithm.ForesightConfig;
import com.pedropathing.controllers.Controller;
import com.pedropathing.math.Matrix;
import com.pedropathing.math.Vector2D;
import org.codeblooded.ftcodesim.ascope.SeasonField;
import org.codeblooded.ftcodesim.ascope.SourceType;
import org.codeblooded.ftcodesim.ascope.boundaries.RobotGeometry;
import org.codeblooded.ftcodesim.hardware.SimHardwareMap;
import org.codeblooded.ftcodesim.hardware.devices.SimOctoquad;
import org.codeblooded.ftcodesim.hardware.drivetrain.SimulatedDrivetrain;
import org.codeblooded.ftcodesim.hardware.drivetrain.SimulatedMecanum;
import org.codeblooded.ftcodesim.simulator.SimConfig;
import org.codeblooded.ftcodesim.hardware.drivetrain.SimMecanumConfig;
import org.codeblooded.ftcodesim.input.DefaultKeybinds;
import org.codeblooded.ftcodesim.simulator.FTCodeSim;
import org.junit.Test;
import java.io.IOException;

public class SimulateMecanumBase {
    @Test
    public void test() throws IOException, InterruptedException {
        SimMecanumConfig config = new SimMecanumConfig();
        config.frontLeftMotorName = "frontLeft";
        config.frontRightMotorName = "frontRight";
        config.backLeftMotorName = "backLeft";
        config.backRightMotorName = "backRight";
        config.wheelbase = 16;
        config.trackWidth = 16;
        config.wheelRadius = 1.889765;
        config.staticVelocityRegion = 1e-3;
        config.staticFriction = 55;
        config.maxAcceleration = 150;
        config.turnNaturalDeceleration = 3;
        config.quadraticBraking = 0.0024;
        config.linearBraking = 0.06447;
        config.naturalDeceleration = 49;
        config.robotGeometry = new RobotGeometry(18, 18, 0, 0);
        config.robotModel = SourceType.ROBOT_MECANUM_BASE;
        config.maxAngularVelocity = 5.175;

        config.foresightConfig = new ForesightConfig(
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

//        c.linearBrakeCoefficients.set(Matrix.diag(0.06447, 0.06447));
//        c.quadraticBrakeCoefficients.set(Matrix.diag(0.0024, 0.0024));
//
//        c.maxAchievableForwardVelocity.set(85.17);
//        c.maxAchievableStrafeVelocity.set(66.8431);
//
//        c.naturalForwardDeceleration.set(49.09);
//        c.naturalStrafeDeceleration.set(49.09);
//
//        c.maxDecelerationConstraint.set(49.09);
//        c.coast.set(Controller.proportional(0).plus(Controller.proportionalFeedforward(0.015)));
//
//        c.forwardTranslational.set(Controller.proportional(0.2));
//        c.strafeTranslational.set(Controller.proportional(0.2));
//
//        c.brake.set(Controller.proportionalFeedforward(0.02));
//
//        c.maxBrakingPower.set(0.2);
//
//        c.headingFeedback.set(Controller.proportional(2));
//
//        c.headingBrakeCoefficients.set(Vector2D.cartesian(0.042, 0.0101));

        SimulatedDrivetrain drivetrain = new SimulatedMecanum(config);

        SimHardwareMap simHardwareMap = new SimHardwareMap();
        simHardwareMap.register(drivetrain);
        simHardwareMap.register("octoquad", new SimOctoquad(drivetrain));

        SimConfig simConfig = new SimConfig();
        simConfig.gamepad1Keybinds = new DefaultKeybinds();
        simConfig.gamepad2Keybinds = new DefaultKeybinds();
        simConfig.simHardwareMap = simHardwareMap;
        simConfig.loopTimeMs = 10;
        simConfig.field = SeasonField.DECODE;
        simConfig.autoConfigureAscope = true;

        FTCodeSim sim = new FTCodeSim(simConfig);
        sim.run();
    }
}