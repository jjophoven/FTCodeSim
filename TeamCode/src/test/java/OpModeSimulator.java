import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.Gamepad;
import org.firstinspires.ftc.teamcode.fake.FakeHardwareMap;
import org.firstinspires.ftc.teamcode.fake.FakeTelemetry;
import org.firstinspires.ftc.teamcode.fake.FakeDriverStationServer;
import org.firstinspires.ftc.teamcode.fake.OpModeState;

import java.io.IOException;

public class OpModeSimulator {
    public static void simulate(OpMode opMode) throws InterruptedException, IOException {
        FakeDriverStationServer driverStation = new FakeDriverStationServer();

        driverStation.startServer();
        driverStation.acceptClient();

        FakeHardwareMap fakeHardwareMap = new FakeHardwareMap(null, null);
        opMode.hardwareMap = fakeHardwareMap;
        opMode.telemetry = new FakeTelemetry(driverStation);
        opMode.gamepad1 = new Gamepad();

        System.out.println(driverStation.state);

        while (driverStation.state == OpModeState.WAIT_FOR_INIT) {
            driverStation.poll();

            Thread.sleep(20);
        }

        opMode.init();

        while (driverStation.state == OpModeState.INITIALIZING) {
            driverStation.poll();

            opMode.gamepad1.fromByteArray(driverStation.gamepad1.toByteArray());

            opMode.init_loop();

            Thread.sleep(20);
        }

        opMode.start();

        while (driverStation.state == OpModeState.RUNNING) {
            driverStation.poll();

            opMode.gamepad1.fromByteArray(driverStation.gamepad1.toByteArray());

            fakeHardwareMap.updateDrivetrain();

            opMode.loop();

            Thread.sleep(20);
        }
    }
}