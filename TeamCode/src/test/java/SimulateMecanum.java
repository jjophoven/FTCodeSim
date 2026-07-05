import org.jjophoven.simhardware.SimHardwareMap;
import org.jjophoven.simhardware.drivetrain.SimulatedMecanum;
import org.jjophoven.simulator.SimConfig;
import org.jjophoven.simhardware.drivetrain.SimMecanumConfig;
import org.jjophoven.input.DefaultKeybinds;
import org.jjophoven.simulator.DriverStationSimulator;
import org.junit.Test;
import java.io.IOException;

public class SimulateMecanum {
    @Test
    public void test() throws IOException, InterruptedException {
        SimHardwareMap simHardwareMap = new SimHardwareMap();

        SimMecanumConfig mecanumConfig = new SimMecanumConfig();
        mecanumConfig.frontLeftMotorName = "frontLeft";
        mecanumConfig.frontRightMotorName = "frontRight";
        mecanumConfig.backLeftMotorName = "backLeft";
        mecanumConfig.backRightMotorName = "backRight";
        mecanumConfig.wheelbase = 9.5767;
        mecanumConfig.trackWidth = 8.66142;
        mecanumConfig.wheelRadius = 1.889765;
        mecanumConfig.staticVelocityRegion = 2;
        mecanumConfig.staticFriction = 45;
        mecanumConfig.maxAcceleration = 250;
        mecanumConfig.maxVelocity = 89.72;
        mecanumConfig.naturalDeceleration = 40;
        mecanumConfig.strafeEfficiency = 0.90;
        mecanumConfig.simHardwareMap = simHardwareMap;

        simHardwareMap.setDrivetrain(new SimulatedMecanum(mecanumConfig));
        simHardwareMap.pinpoint("pinpoint");

        SimConfig simConfig = new SimConfig();
        simConfig.gamepad1Keybinds = new DefaultKeybinds();
        simConfig.gamepad2Keybinds = new DefaultKeybinds();
        simConfig.simHardwareMap = simHardwareMap;
        simConfig.loopTimeMs = 20;

        DriverStationSimulator driverStation = new DriverStationSimulator(simConfig);
    }
}