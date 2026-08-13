//package org.firstinspires.ftc.teamcode.auto;
//
//import com.pedropathing.follower.Follower;
//import com.pedropathing.geometry.BezierLine;
//import com.pedropathing.geometry.Pose;
//import com.pedropathing.paths.PathChain;
//import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
//import com.qualcomm.robotcore.eventloop.opmode.OpMode;
//import org.psilynx.psikit.core.Logger;
//
//@Autonomous(name = "auto1", group = "Autonomous")
//public class Auto extends OpMode {
//    public Follower follower; // Pedro Pathing follower instance
//    private int pathState; // Current autonomous path state (state machine)
//    private Paths paths; // Paths defined in the Paths class
//
//    @Override
//    public void init() {
//        follower = Constants.createFollower(hardwareMap);
////        follower.setStartingPose(new Pose(124.547, 116.437, Math.toRadians(36.5)));
//        follower.setStartingPose(new Pose(141.5/2, 141.5/2, Math.toRadians(0)));
//
//        paths = new Paths(follower);
//    }
//
//    @Override
//    public void start() {
//        follower.followPath(paths.MainChain);
//    }
//
//    @Override
//    public void loop() {
//        follower.update(); // Update Pedro Pathing
//
//        Logger.recordOutput("Path State", pathState);
//        Logger.recordOutput("pose", String.valueOf(follower.getPose()));
//
//        Logger.recordOutput("closestPose", follower.getClosestPose().getPose().toString());
//        Logger.recordOutput("driveVector", follower.getDriveVector().toString());
//    }
//
//    public static class Paths {
//        public PathChain MainChain;
//
//        public Paths(Follower follower) {
//            MainChain = follower.pathBuilder()
//                    .addPath(
//                            new BezierLine(
//                                    new Pose(141.5/2, 141.5/2),
//                                    new Pose(141.5/2 + 20, 141.5/2)
//                            )
//                    )
//                    .setConstantHeadingInterpolation(0)
//                    .build();
//        }
//    }
//}