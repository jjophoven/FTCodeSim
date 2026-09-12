package org.firstinspires.ftc.teamcode.opmode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;

import org.codeblooded.ftcodesim.hardware.drivetrain.SimulatedSelectableDrivetrain;

import java.util.Arrays;

/** Hands-on comparison of mecanum, tank, and dual-actuated simulator physics. */
@TeleOp(name = "Interactive Drivetrain Test")
public class InteractiveDrivetrainTest extends OpMode {
    public static final String SELECTOR_NAME = "selectableDrivetrain";

    private SimulatedSelectableDrivetrain drivetrain;
    private DcMotorEx[] motors;
    private double speedScalar = 1.0;

    @Override
    public void init() {
        drivetrain = hardwareMap.get(SimulatedSelectableDrivetrain.class, SELECTOR_NAME);
        motors = new DcMotorEx[]{
                hardwareMap.get(DcMotorEx.class, "frontLeft"),
                hardwareMap.get(DcMotorEx.class, "frontRight"),
                hardwareMap.get(DcMotorEx.class, "backLeft"),
                hardwareMap.get(DcMotorEx.class, "backRight")
        };
        for (DcMotorEx motor : motors) motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        showTelemetry();
    }

    @Override
    public void init_loop() {
        updateSpeedScalar();
        showTelemetry();
    }

    @Override
    public void loop() {
        updateSpeedScalar();
        if (gamepad1.xWasPressed()) drivetrain.cycleDriveType();
        if (gamepad1.aWasPressed() && drivetrain.getDriveType()
                == SimulatedSelectableDrivetrain.DriveType.DUAL_ACTUATED) {
            drivetrain.toggleDriveState();
        }

        double forward = speedScalar * -gamepad1.left_stick_y;
        double strafe = speedScalar * gamepad1.left_stick_x;
        double turn = speedScalar * gamepad1.right_stick_x;
        if (drivetrain.isTankKinematics()) driveTank(forward, turn);
        else driveMecanum(forward, strafe, turn);
        showTelemetry();
    }

    @Override
    public void stop() { setPowers(0, 0, 0, 0); }

    private void updateSpeedScalar() {
        if (gamepad1.dpadDownWasPressed()) speedScalar -= 0.05;
        if (gamepad1.dpadUpWasPressed()) speedScalar += 0.05;
        speedScalar = Math.max(0, Math.min(1, speedScalar));
    }

    private void driveTank(double forward, double turn) {
        double left = forward + turn;
        double right = forward - turn;
        double scale = Math.max(1.0, Math.max(Math.abs(left), Math.abs(right)));
        setPowers(left / scale, right / scale, left / scale, right / scale);
    }

    private void driveMecanum(double forward, double strafe, double turn) {
        double fl = forward + strafe + turn;
        double fr = forward - strafe - turn;
        double bl = forward - strafe + turn;
        double br = forward + strafe - turn;
        double scale = Math.max(1.0, Math.max(Math.abs(fl),
                Math.max(Math.abs(fr), Math.max(Math.abs(bl), Math.abs(br)))));
        setPowers(fl / scale, fr / scale, bl / scale, br / scale);
    }

    private void setPowers(double fl, double fr, double bl, double br) {
        double[] powers = {fl, fr, bl, br};
        for (int i = 0; i < motors.length; i++) motors[i].setPower(powers[i]);
    }

    private void showTelemetry() {
        telemetry.addData("Selected drivetrain", drivetrain.getDriveType());
        telemetry.addData("Dual-actuated state", drivetrain.getDriveState());
        telemetry.addData("Active kinematics", drivetrain.isTankKinematics() ? "TANK" : "HOLONOMIC");
        telemetry.addData("Speed scalar", "%.0f%%", speedScalar * 100.0);
        telemetry.addData("Pose", "x=%.2f  y=%.2f  heading=%.1f deg",
                drivetrain.position.x, drivetrain.position.y,
                Math.toDegrees(drivetrain.position.theta));
        telemetry.addData("Motor powers", Arrays.toString(drivetrain.getMotorPowers()));
        telemetry.addLine("Drive: left stick XY + right stick X");
        telemetry.addLine("X: next drivetrain   A: toggle dual mode");
        telemetry.addLine("D-pad Up/Down: adjust speed");
        telemetry.update();
    }
}
