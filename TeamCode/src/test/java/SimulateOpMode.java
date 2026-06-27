import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import org.firstinspires.ftc.teamcode.opmode.FieldCentricTeleop;
import org.jjophoven.simulator.OpModeSimulator;
import org.jjophoven.simulator.OpModeRegister;
import org.junit.Test;
import java.io.IOException;

public class SimulateOpMode {
    @Test
    public void test() throws IOException, InterruptedException {
         OpModeRegister register = new OpModeRegister();
         for (OpMode opMode : register.getTeleOpModes()) {

             //OpModeSimulator.simulate(opMode);
             System.out.println(opMode.getClass().getSimpleName());
         }

         OpModeSimulator.simulate(new FieldCentricTeleop());
    }
}

