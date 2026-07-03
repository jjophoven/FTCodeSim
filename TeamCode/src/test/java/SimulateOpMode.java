import com.qualcomm.robotcore.hardware.DcMotor;
import org.jjophoven.fakehardware.FakeHardwareMap;
import org.jjophoven.fakehardware.drivetrain.FakeMecanum;
import org.jjophoven.simulator.SimulationConfig;
import org.jjophoven.fakehardware.drivetrain.MecanumConfig;
import org.jjophoven.input.Keybinds;
import org.jjophoven.simulator.DriverStationSimulator;
import org.junit.Test;
import java.io.IOException;

public class SimulateOpMode {
    @Test
    public void test() throws IOException, InterruptedException {
        SimulationConfig simulationConfig = new SimulationConfig();
        FakeHardwareMap fakeHardwareMap = new FakeHardwareMap();

        MecanumConfig mecanumConfig = new MecanumConfig();
        mecanumConfig.frontLeftMotorName = "frontLeft";
        mecanumConfig.frontRightMotorName = "frontRight";
        mecanumConfig.backLeftMotorName = "backLeft";
        mecanumConfig.backRightMotorName = "backRight";
        mecanumConfig.wheelbase = 4.68504 * 2; // distance from center of frontLeft wheel to backLeft wheel
        mecanumConfig.trackWidth = 4.56693 * 2; // distance from center of backRight wheel to backLeft wheel
        mecanumConfig.wheelRadius = 3.77953 / 2;
        mecanumConfig.staticVelocityRegion = 2;
        mecanumConfig.staticFriction = 45;
        mecanumConfig.maxAcceleration = 250;
        mecanumConfig.maxVelocity = 70;
        mecanumConfig.naturalDeceleration = 40;
        mecanumConfig.strafeEfficiency = 0.90;
        mecanumConfig.fakeHardwareMap = fakeHardwareMap;

        simulationConfig.drivetrain = new FakeMecanum(mecanumConfig);
        simulationConfig.gamepad1Keybinds = new Keybinds();
        simulationConfig.gamepad2Keybinds = new Keybinds();
        simulationConfig.fakeHardwareMap = fakeHardwareMap;

        fakeHardwareMap.pinpoint("pinpoint", simulationConfig.drivetrain);

        DriverStationSimulator driverStation = new DriverStationSimulator(simulationConfig);
    }
}
