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
        config.maxVelocity = 65;
        config.forwardNaturalDeceleration = 49;
        config.strafeNaturalDeceleration = 85;
        config.turnNaturalDeceleration = 1;
        config.quadraticBraking = 0.0021;
        config.linearBraking = 0.0644;
        config.naturalDeceleration = 49;
        config.robotGeometry = new RobotGeometry(18, 18, 0, 0);
        config.robotModel = SourceType.ROBOT_MECANUM_BASE;

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