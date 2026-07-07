package org.firstinspires.ftc.teamcode.x;

import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.BezierCurve;
import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.PathChain;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import org.psilynx.psikit.core.Logger;
import org.psilynx.psikit.ftc.FtcLoggingSession;

@Autonomous(name = "auto1", group = "Autonomous")
public class Auto extends OpMode {
    //private TelemetryManager panelsTelemetry; // Panels Telemetry instance
    public Follower follower; // Pedro Pathing follower instance
    private int pathState; // Current autonomous path state (state machine)
    private Paths paths; // Paths defined in the Paths class

    private final FtcLoggingSession psiKit = new FtcLoggingSession();


    @Override
    public void init() {
        psiKit.start(this, 5800, "", true, "TeamCode/logs");

//        panelsTelemetry = PanelsTelemetry.INSTANCE.getTelemetry();

        follower = Constants.createFollower(hardwareMap);
        follower.setStartingPose(new Pose(72, 8, Math.toRadians(90)));

        paths = new Paths(follower); // Build paths
//
//        panelsTelemetry.debug("Status", "Initialized");
//        panelsTelemetry.update(telemetry);
        //follower.setStartingPose(new Pose(124.547, 116.437, 0));
    }

    @Override
    public void start() {
        follower.followPath(paths.MainChain);
    }

    @Override
    public void loop() {
        Logger.periodicBeforeUser();
        follower.update(); // Update Pedro Pathing
        pathState = autonomousPathUpdate(); // Update autonomous state machine

        Logger.recordOutput("Path State", pathState);
        Logger.recordOutput("pose", String.valueOf(follower.getPose()));

        Logger.recordOutput("closestPose", follower.getClosestPose().getPose().toString());
        Logger.recordOutput("driveVector", follower.getDriveVector().toString());

        Logger.periodicAfterUser(0,0);

        // Log values to Panels and Driver Station
//        panelsTelemetry.debug("Path State", pathState);
//        panelsTelemetry.debug("X", follower.getPose().getX());
//        panelsTelemetry.debug("Y", follower.getPose().getY());
//        panelsTelemetry.debug("Heading", follower.getPose().getHeading());
//        panelsTelemetry.update(telemetry);
    }

    public static class Paths {
        public PathChain MainChain;

        public Paths(Follower follower) {
            MainChain = follower.pathBuilder()
                    .addPath(
                            new BezierLine(
                                    new Pose(124.547, 116.437),
                                    new Pose(82.000, 82.500)
                            )
                    )
                    .setTangentHeadingInterpolation()
                    .setReversed()
                    .addPath(
                            new BezierLine(
                                    new Pose(82.000, 82.500),
                                    new Pose(118.500, 82.500)
                            )
                    )
                    .setTangentHeadingInterpolation()
                    .addPath(
                            new BezierLine(
                                    new Pose(118.500, 82.500),
                                    new Pose(82.000, 71.000)
                            )
                    )
                    .setTangentHeadingInterpolation()
                    .setReversed()
                    .addPath(
                            new BezierCurve(
                                    new Pose(82.000, 71.000),
                                    new Pose(92.000, 58.500),
                                    new Pose(125.000, 59.500)
                            )
                    )
                    .setConstantHeadingInterpolation(Math.toRadians(0))
                    .addPath(
                            new BezierLine(
                                    new Pose(125.000, 59.500),
                                    new Pose(82.000, 70.000)
                            )
                    )
                    .setTangentHeadingInterpolation()
                    .setReversed()
                    .addPath(
                            new BezierLine(
                                    new Pose(82.000, 70.000),
                                    new Pose(123.000, 59.000)
                            )
                    )
                    .setTangentHeadingInterpolation()
                    .addPath(
                            new BezierLine(
                                    new Pose(123.000, 59.000),
                                    new Pose(129.500, 57.500)
                            )
                    )
                    .setLinearHeadingInterpolation(Math.toRadians(0), Math.toRadians(22))
                    .addPath(
                            new BezierLine(
                                    new Pose(129.500, 57.500),
                                    new Pose(82.000, 70.000)
                            )
                    )
                    .setTangentHeadingInterpolation()
                    .setReversed()
                    .build();
        }
    }

    public int autonomousPathUpdate() {
        // Add your state machine Here
        // Access paths with paths.pathName
        // Refer to the Pedro Pathing Docs (Auto Example) for an example state machine

        return 0;
    }
}