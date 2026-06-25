import org.firstinspires.ftc.teamcode.opmode.base.TeleOpMode;
import org.junit.Test;

import java.io.IOException;

public class SimulateOpMode {
    @Test
    public void test() throws InterruptedException, IOException {
        OpModeSimulator.simulate(new TeleOpMode() {
            @Override
            protected void onFirstDriverInput() {}
        });
    }
}
