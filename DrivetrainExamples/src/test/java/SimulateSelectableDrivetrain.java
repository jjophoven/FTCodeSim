import org.codeblooded.ftcodesim.ascope.SeasonField;
import org.codeblooded.ftcodesim.ascope.SourceType;
import org.codeblooded.ftcodesim.ascope.boundaries.MotionVector;
import org.codeblooded.ftcodesim.ascope.boundaries.RobotGeometry;
import org.codeblooded.ftcodesim.hardware.SimHardwareMap;
import org.codeblooded.ftcodesim.hardware.devices.SimPinpoint;
import org.codeblooded.ftcodesim.hardware.drivetrain.SimDualActuatedConfig;
import org.codeblooded.ftcodesim.hardware.drivetrain.SimulatedSelectableDrivetrain;
import org.codeblooded.ftcodesim.input.DefaultKeybinds;
import org.codeblooded.ftcodesim.simulator.FTCodeSim;
import org.codeblooded.ftcodesim.simulator.SimConfig;
import org.firstinspires.ftc.teamcode.opmode.InteractiveDrivetrainTest;
import org.junit.Test;

import java.io.IOException;

/** Launches one robot whose drivetrain physics can be changed from the test TeleOp. */
public class SimulateSelectableDrivetrain {
    @Test
    public void runInteractiveDrivetrainTest() throws IOException, InterruptedException {
        SimDualActuatedConfig drivetrainConfig = new SimDualActuatedConfig();
        drivetrainConfig.frontLeftMotorName = "frontLeft";
        drivetrainConfig.frontRightMotorName = "frontRight";
        drivetrainConfig.backLeftMotorName = "backLeft";
        drivetrainConfig.backRightMotorName = "backRight";
        drivetrainConfig.wheelbase = 9.37008;
        drivetrainConfig.trackWidth = 9.13386;
        drivetrainConfig.wheelRadius = 1.889765;
        drivetrainConfig.staticVelocityRegion = 1e-3;
        drivetrainConfig.staticFriction = 55;
        drivetrainConfig.maxAcceleration = 150;
        drivetrainConfig.maxVelocity = 65;
        drivetrainConfig.forwardNaturalDeceleration = 49;
        drivetrainConfig.strafeNaturalDeceleration = 85;
        drivetrainConfig.turnNaturalDeceleration = 1;
        drivetrainConfig.naturalDeceleration = 49;
        drivetrainConfig.quadraticBraking = 0.0021;
        drivetrainConfig.linearBraking = 0.0644;
        drivetrainConfig.robotGeometry = new RobotGeometry(12, 18, 2, 0);
        drivetrainConfig.robotModel = SourceType.ROBOT_MECANUM_BASE;
        drivetrainConfig.initialState = SimDualActuatedConfig.DriveState.HOLONOMIC;
        drivetrainConfig.addActuator("driveModeServo", 1.0, 0.0);

        SimulatedSelectableDrivetrain drivetrain =
                new SimulatedSelectableDrivetrain(drivetrainConfig);
        // FTCodeSim uses a bottom-left corner origin. Start at field center so the robot's
        // footprint is fully legal before collision handling runs its first update.
        drivetrain.setPosition(new MotionVector(141.5 / 2.0, 141.5 / 2.0, 0.0));
        SimHardwareMap hardwareMap = new SimHardwareMap();
        hardwareMap.register(drivetrain);
        hardwareMap.register(InteractiveDrivetrainTest.SELECTOR_NAME, drivetrain);
        hardwareMap.register("pinpoint", new SimPinpoint(drivetrain));

        SimConfig simConfig = new SimConfig();
        simConfig.gamepad1Keybinds = new DefaultKeybinds();
        simConfig.gamepad2Keybinds = new DefaultKeybinds();
        simConfig.simHardwareMap = hardwareMap;
        simConfig.loopTimeMs = 20;
        simConfig.field = SeasonField.DECODE;
        simConfig.autoConfigureAscope = true;

        new FTCodeSim(simConfig).run();
    }
}
