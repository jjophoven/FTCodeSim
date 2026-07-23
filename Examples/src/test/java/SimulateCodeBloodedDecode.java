import org.codeblooded.ftcodesim.ascope.SeasonField;
import org.codeblooded.ftcodesim.ascope.SourceType;
import org.codeblooded.ftcodesim.ascope.boundaries.RobotGeometry;
import org.codeblooded.ftcodesim.hardware.devices.SimGobildaPinpoint;
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

        SimMecanumConfig mecanumConfig = new SimMecanumConfig();
        mecanumConfig.frontLeftMotorName = "frontLeft";
        mecanumConfig.frontRightMotorName = "frontRight";
        mecanumConfig.backLeftMotorName = "backLeft";
        mecanumConfig.backRightMotorName = "backRight";
        mecanumConfig.wheelbase = 9.37008;
        mecanumConfig.trackWidth = 9.13386;
        mecanumConfig.wheelRadius = 1.889765;
        mecanumConfig.staticVelocityRegion = 2;
        mecanumConfig.staticFriction = 45;
        mecanumConfig.maxAcceleration = 150;
        mecanumConfig.maxVelocity = 75;
        mecanumConfig.naturalDeceleration = 40;
        mecanumConfig.strafeEfficiency = 0.80;
        mecanumConfig.robotGeometry = new RobotGeometry(12, 18, 2, 0);
        mecanumConfig.robotModel = SourceType.ROBOT_CODE_BLOODED_DECODE;

        SimulatedDrivetrain drivetrain = new SimulatedMecanum(mecanumConfig);

        simHardwareMap.register(drivetrain);
        simHardwareMap.register("pinpoint", new SimGobildaPinpoint(drivetrain));

        SimConfig simConfig = new SimConfig();
        simConfig.gamepad1Keybinds = new DefaultKeybinds();
        simConfig.gamepad2Keybinds = new DefaultKeybinds();
        simConfig.simHardwareMap = simHardwareMap;
        simConfig.loopTimeMs = 20;
        simConfig.field = SeasonField.DECODE;

        FTCodeSim sim = new FTCodeSim(simConfig);
        sim.run();
    }
}