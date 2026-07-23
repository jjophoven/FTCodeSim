import org.codeblooded.ftcodesim.ascope.SeasonField;
import org.codeblooded.ftcodesim.ascope.SourceType;
import org.codeblooded.ftcodesim.ascope.boundaries.RobotGeometry;
import org.codeblooded.ftcodesim.hardware.SimHardwareMap;
import org.codeblooded.ftcodesim.hardware.devices.SimGobildaPinpoint;
import org.codeblooded.ftcodesim.hardware.drivetrain.SimulatedDrivetrain;
import org.codeblooded.ftcodesim.hardware.drivetrain.SimulatedTank;
import org.codeblooded.ftcodesim.hardware.drivetrain.SimTankConfig;
import org.codeblooded.ftcodesim.simulator.SimConfig;
import org.codeblooded.ftcodesim.input.DefaultKeybinds;
import org.codeblooded.ftcodesim.simulator.FTCodeSim;
import org.junit.Test;
import java.io.IOException;

public class SimulateTank {
    @Test
    public void test() throws IOException, InterruptedException {
        SimTankConfig config = new SimTankConfig();
        config.frontLeftMotorName = "frontLeft";
        config.frontRightMotorName = "frontRight";
        config.backLeftMotorName = "backLeft";
        config.backRightMotorName = "backRight";
        config.trackWidth = 16;
        config.wheelRadius = 1.889765;
        config.staticVelocityRegion = 2;
        config.staticFriction = 45;
        config.maxAcceleration = 200;
        config.maxVelocity = 70;
        config.naturalDeceleration = 40;
        config.robotGeometry = new RobotGeometry(18, 18, 0, 0);
        config.robotModel = SourceType.ROBOT_TANK;

        SimulatedDrivetrain tank = new SimulatedTank(config);

        SimHardwareMap simHardwareMap = new SimHardwareMap();
        simHardwareMap.register(tank);
        simHardwareMap.register("pinpoint", new SimGobildaPinpoint(tank));

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
