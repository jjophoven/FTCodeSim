import org.jjophoven.simhardware.SimHardwareMap;
import org.jjophoven.simhardware.drivetrain.SimulatedTank;
import org.jjophoven.simhardware.drivetrain.SimTankConfig;
import org.jjophoven.simulator.RobotGeometry;
import org.jjophoven.simulator.SimConfig;
import org.jjophoven.input.DefaultKeybinds;
import org.jjophoven.simulator.DriverStationSimulator;
import org.junit.Test;
import java.io.IOException;

public class SimulateTank { // TODO create a way to tag what opmodes are using which drivetrain
    @Test
    public void test() throws IOException, InterruptedException {
        SimHardwareMap simHardwareMap = new SimHardwareMap();

        SimTankConfig config = new SimTankConfig();
        config.frontLeftMotorName = "frontLeft";
        config.frontRightMotorName = "frontRight";
        config.backLeftMotorName = "backLeft";
        config.backRightMotorName = "backRight";
        config.trackWidth = 16;
        config.wheelRadius = 1.7716535;
        config.staticVelocityRegion = 2;
        config.staticFriction = 45;
        config.maxAcceleration = 200;
        config.maxVelocity = 40;
        config.naturalDeceleration = 40;
        config.simHardwareMap = simHardwareMap;

        simHardwareMap.setDrivetrain(new SimulatedTank(config));
        simHardwareMap.pinpoint("pinpoint");

        SimConfig simConfig = new SimConfig();
        simConfig.gamepad1Keybinds = new DefaultKeybinds();
        simConfig.gamepad2Keybinds = new DefaultKeybinds();
        simConfig.simHardwareMap = simHardwareMap;
        simConfig.loopTimeMs = 20;
        simConfig.robotGeometry = new RobotGeometry(16.53543, 19.015748, 0, 0);

        DriverStationSimulator driverStation = new DriverStationSimulator(simConfig);
    }
}
