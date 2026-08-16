import org.codeblooded.ftcodesim.ascope.SeasonField;
import org.codeblooded.ftcodesim.ascope.SourceType;
import org.codeblooded.ftcodesim.ascope.boundaries.RobotGeometry;
import org.codeblooded.ftcodesim.hardware.devices.SimOctoquad;
import org.codeblooded.ftcodesim.hardware.devices.SimPinpoint;
import org.codeblooded.ftcodesim.hardware.drivetrain.SimulatedDrivetrain;
import org.codeblooded.ftcodesim.input.DefaultKeybinds;
import org.codeblooded.ftcodesim.hardware.SimHardwareMap;
import org.codeblooded.ftcodesim.hardware.drivetrain.SimMecanumConfig;
import org.codeblooded.ftcodesim.hardware.drivetrain.SimulatedMecanum;
import org.codeblooded.ftcodesim.simulator.FTCodeSim;
import org.codeblooded.ftcodesim.simulator.SimConfig;
import org.junit.Test;
import java.io.IOException;


public class SimulateCodeBloodedDecode {
    @Test
    public void test() throws IOException, InterruptedException {
        SimHardwareMap simHardwareMap = new SimHardwareMap();

        SimMecanumConfig config = new SimMecanumConfig();
        config.frontLeftMotorName = "frontLeft";
        config.frontRightMotorName = "frontRight";
        config.backLeftMotorName = "backLeft";
        config.backRightMotorName = "backRight";
        config.wheelbase = 9.37008;
        config.trackWidth = 9.13386;
        config.wheelRadius = 1.889765;
        config.staticVelocityRegion = 2;
        config.staticFriction = 45;
        config.maxAcceleration = 150;
        config.maxVelocity = 65;
        config.staticVelocityRegion = 1e-3;
        config.staticFriction = 55;
        config.forwardNaturalDeceleration = 49;
        config.strafeNaturalDeceleration = 85;
        config.turnNaturalDeceleration = 1;
        config.naturalDeceleration = 33;
        config.quadraticBraking = 0.0014846306;
        config.linearBraking = 0.09533276;
        config.robotGeometry = new RobotGeometry(12, 18, 2, 0);
        config.robotModel = SourceType.ROBOT_CODE_BLOODED_DECODE;

        SimulatedDrivetrain drivetrain = new SimulatedMecanum(config);

        simHardwareMap.register(drivetrain);
        simHardwareMap.register("pinpoint", new SimPinpoint(drivetrain));

        SimConfig simConfig = new SimConfig();
        simConfig.gamepad1Keybinds = new DefaultKeybinds();
        simConfig.gamepad2Keybinds = new DefaultKeybinds();
        simConfig.simHardwareMap = simHardwareMap;
        simConfig.loopTimeMs = 20;
        simConfig.field = SeasonField.DECODE;
        simConfig.autoConfigureAscope = true;

        FTCodeSim sim = new FTCodeSim(simConfig);

        sim.run();
    }
}