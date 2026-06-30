import org.jjophoven.simulator.SimulationConfig;
import org.jjophoven.fakehardware.drivetrain.MecanumConfig;
import org.jjophoven.input.Keybinds;
import org.jjophoven.simulator.DriverStationSimulator;
import org.junit.Test;
import java.io.IOException;

public class SimulateOpMode {
    @Test
    public void test() throws IOException, InterruptedException {
        // TUNABLE MECANUM CONSTANTS
        double maxVelocity = 70;
        double maxAcceleration = 250;
        double naturalDeceleration = 100;


        // Ill clean the rest up later
        SimulationConfig simulationConfig = new SimulationConfig();

        MecanumConfig mecanumConfig = new MecanumConfig();
        mecanumConfig.frontLeftMotorName = "frontLeft";
        mecanumConfig.frontRightMotorName = "frontRight";
        mecanumConfig.backLeftMotorName = "backLeft";
        mecanumConfig.backRightMotorName = "backRight";
        mecanumConfig.wheelbase = 4.68504; // half distance from frontLeft wheel to backLeft wheel
        mecanumConfig.trackWidth = 4.56693; // half distance from backRight wheel to backLeft wheel
        mecanumConfig.wheelDiameter = 3.77953;
        double radius = mecanumConfig.wheelDiameter / 2;

        double maxOmega = maxVelocity / radius;
        double maxAlpha = maxAcceleration / radius;
        double naturalAlpha = naturalDeceleration / radius;

        double kA = (maxAlpha + naturalAlpha) / 13;
        double kBackEMF = maxAlpha / maxOmega;

        mecanumConfig.coefficients =  new double[]{
                kA, kBackEMF, 0, naturalDeceleration / radius
        };

        // angular units
        mecanumConfig.staticVelocityRegion = 2 / radius;
        mecanumConfig.staticFriction = 45 / radius; // minimum accel to move

        simulationConfig.drivetrain = mecanumConfig;
        simulationConfig.gamepad1Keybinds = new Keybinds();
        simulationConfig.gamepad2Keybinds = new Keybinds();

        DriverStationSimulator driverStation = new DriverStationSimulator(simulationConfig);
    }
}
