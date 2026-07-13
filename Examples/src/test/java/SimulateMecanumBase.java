import org.codeblooded.ftcodesim.hardware.SimHardwareMap;
import org.codeblooded.ftcodesim.hardware.devices.SimGobildaPinpoint;
import org.codeblooded.ftcodesim.hardware.drivetrain.SimulatedDrivetrain;
import org.codeblooded.ftcodesim.hardware.drivetrain.SimulatedMecanum;
import org.codeblooded.ftcodesim.physics.RobotGeometry;
import org.codeblooded.ftcodesim.simulator.SimConfig;
import org.codeblooded.ftcodesim.hardware.drivetrain.SimMecanumConfig;
import org.codeblooded.ftcodesim.input.DefaultKeybinds;
import org.codeblooded.ftcodesim.simulator.FTCodeSim;
import org.junit.Test;
import java.io.IOException;

public class SimulateMecanumBase {
    @Test
    public void test() throws IOException, InterruptedException {
        SimMecanumConfig mecanumConfig = new SimMecanumConfig();
        mecanumConfig.frontLeftMotorName = "frontLeft";
        mecanumConfig.frontRightMotorName = "frontRight";
        mecanumConfig.backLeftMotorName = "backLeft";
        mecanumConfig.backRightMotorName = "backRight";
        mecanumConfig.wheelbase = 16;
        mecanumConfig.trackWidth = 16;
        mecanumConfig.wheelRadius = 1.889765;
        mecanumConfig.staticVelocityRegion = 2;
        mecanumConfig.staticFriction = 45;
        mecanumConfig.maxAcceleration = 150;
        mecanumConfig.maxVelocity = 70;
        mecanumConfig.naturalDeceleration = 40;
        mecanumConfig.strafeEfficiency = 0.90;
        mecanumConfig.robotGeometry = new RobotGeometry(18, 18, 0, 0);

        SimulatedDrivetrain drivetrain = new SimulatedMecanum(mecanumConfig);

        SimHardwareMap simHardwareMap = new SimHardwareMap();
        simHardwareMap.register(drivetrain);
        simHardwareMap.register("pinpoint", new SimGobildaPinpoint(drivetrain));

        SimConfig simConfig = new SimConfig();
        simConfig.gamepad1Keybinds = new DefaultKeybinds();
        simConfig.gamepad2Keybinds = new DefaultKeybinds();
        simConfig.simHardwareMap = simHardwareMap;
        simConfig.loopTimeMs = 20;

        FTCodeSim sim = new FTCodeSim(simConfig);
        sim.run();
    }
}