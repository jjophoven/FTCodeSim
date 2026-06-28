import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import org.firstinspires.ftc.teamcode.opmode.FieldCentricTeleop;
import org.jjophoven.input.Keybinds;
import org.jjophoven.simulator.DriverStationSimulator;
import org.junit.Test;
import java.io.IOException;

public class SimulateOpMode {
    @Test
    public void test() throws IOException, InterruptedException {
        DriverStationSimulator driverStation = new DriverStationSimulator(new Keybinds(), new Keybinds());
    }
}

