import org.jjophoven.fakehardware.FakeHardwareMap;
import org.jjophoven.fakehardware.drivetrain.FakeTank;
import org.jjophoven.fakehardware.drivetrain.TankConfig;
import org.jjophoven.simulator.SimulationConfig;
import org.jjophoven.input.DefaultKeybinds;
import org.jjophoven.simulator.DriverStationSimulator;
import org.junit.Test;
import java.io.IOException;

public class SimulateTank { // TODO create a way to tag what opmodes are using which drivetrain
    @Test
    public void test() throws IOException, InterruptedException {
        SimulationConfig simulationConfig = new SimulationConfig();

        FakeHardwareMap fakeHardwareMap = new FakeHardwareMap();

        TankConfig config = new TankConfig();
        config.frontLeftMotorName = "frontLeft";
        config.frontRightMotorName = "frontRight";
        config.backLeftMotorName = "backLeft";
        config.backRightMotorName = "backRight";
        config.trackWidth = 12; // distance from center of backRight wheel to backLeft wheel
        config.wheelRadius = 3.77953 / 2;
        config.staticVelocityRegion = 2;
        config.staticFriction = 45;
        config.maxAcceleration = 250;
        config.maxVelocity = 70;
        config.naturalDeceleration = 40;
        config.fakeHardwareMap = fakeHardwareMap;

        fakeHardwareMap.setDrivetrain(new FakeTank(config));

        simulationConfig.gamepad1Keybinds = new DefaultKeybinds();
        simulationConfig.gamepad2Keybinds = new DefaultKeybinds();
        simulationConfig.fakeHardwareMap = fakeHardwareMap;

        fakeHardwareMap.pinpoint("pinpoint");

        DriverStationSimulator driverStation = new DriverStationSimulator(simulationConfig);
    }
}
