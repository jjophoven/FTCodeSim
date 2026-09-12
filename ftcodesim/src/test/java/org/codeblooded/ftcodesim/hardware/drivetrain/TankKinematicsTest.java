package org.codeblooded.ftcodesim.hardware.drivetrain;

import org.codeblooded.ftcodesim.ascope.boundaries.MotionVector;
import org.junit.Test;
import static org.junit.Assert.*;

public class TankKinematicsTest {
    private SimulatedTank tank(boolean four) {
        SimTankConfig c = new SimTankConfig();
        c.frontLeftMotorName = "fl";
        c.frontRightMotorName = "fr";
        if (four) { c.backLeftMotorName = "bl"; c.backRightMotorName = "br"; }
        c.wheelRadius = 2;
        c.trackWidth = 10;
        return new SimulatedTank(c);
    }

    private void vector(MotionVector actual, double x, double y, double theta) {
        assertEquals(x, actual.x, 1e-9);
        assertEquals(y, actual.y, 1e-9);
        assertEquals(theta, actual.theta, 1e-9);
    }

    @Test public void wheelSpeedsUseCounterclockwiseTurnsAndAverageEachSide() {
        vector(tank(false).forwardKinematics(new double[]{1, 3}), 4, 0, 0.4);
        vector(tank(true).forwardKinematics(new double[]{0, 2, 2, 4}), 4, 0, 0.4);
        vector(tank(false).forwardKinematics(new double[]{-1, 1}), 0, 0, 0.4);
    }

    @Test public void inverseRoundTripsLongitudinalMotion() {
        for (boolean four : new boolean[]{false, true}) {
            SimulatedTank t = tank(four);
            for (double speed : new double[]{-10, 0, 10}) {
                for (double turn : new double[]{-2, 0, 2}) {
                    double[] wheels = t.inverseKinematics(new MotionVector(speed, 17, turn));
                    assertEquals(four ? 4 : 2, wheels.length);
                    vector(t.forwardKinematics(wheels), speed, 0, turn);
                }
            }
        }
    }

    @Test public void quarterCirclePreservesSpeedAndClosesOverFullTurn() {
        SimulatedTank t = tank(false);
        t.velocity = new MotionVector(10, 0, 1);
        t.integratePosition(Math.PI / 2);
        vector(t.position, 10, 10, Math.PI / 2);
        vector(t.velocity, 0, 10, 1);
        for (int i = 0; i < 300; i++) t.integratePosition(Math.PI / 200);
        vector(t.position, 0, 0, 2 * Math.PI);
        vector(t.velocity, 10, 0, 1);
    }

    @Test public void straightReverseAndNearZeroTurnRemainFinite() {
        SimulatedTank t = tank(true);
        t.position = new MotionVector(3, 4, Math.PI / 2);
        t.velocity = new MotionVector(-5, 0, 1e-12).toFieldFrame(t.position.theta);
        t.integratePosition(2);
        vector(t.position, 3, -6, Math.PI / 2 + 2e-12);
    }

    @Test public void constraintRemovesOnlyLateralMotion() {
        SimulatedTank t = tank(false);
        t.position.theta = Math.PI / 2;
        t.velocity = new MotionVector(8, 3, -2).toFieldFrame(t.position.theta);
        t.constrainVelocity();
        vector(t.velocity.toRobotFrame(t.position.theta), 8, 0, -2);
    }

    @Test public void selectableTankUsesArcWhileMecanumRetainsFieldVelocity() {
        SimDualActuatedConfig c = new SimDualActuatedConfig();
        c.frontLeftMotorName = "fl"; c.frontRightMotorName = "fr";
        c.backLeftMotorName = "bl"; c.backRightMotorName = "br";
        c.trackWidth = 10; c.wheelbase = 10; c.wheelRadius = 2;
        c.initialState = SimDualActuatedConfig.DriveState.TANK;
        c.addActuator("mode", 1, 0);
        SimulatedDualActuated dual = new SimulatedDualActuated(c);
        dual.velocity = new MotionVector(10, 0, 1);
        dual.integratePosition(Math.PI / 2);
        vector(dual.position, 10, 10, Math.PI / 2);
        vector(dual.forwardKinematics(new double[]{0, 2, 2, 4}), 4, 0, 0.4);
        SimulatedSelectableDrivetrain selectable = new SimulatedSelectableDrivetrain(c);
        selectable.velocity = new MotionVector(10, 0, 1);
        selectable.integratePosition(Math.PI / 2);
        vector(selectable.position, 5 * Math.PI, 0, Math.PI / 2);
        vector(selectable.velocity, 10, 0, 1);
        selectable.setDriveType(SimulatedSelectableDrivetrain.DriveType.TANK);
        selectable.position = new MotionVector(0, 0, 0);
        selectable.velocity = new MotionVector(10, 0, 1);
        selectable.integratePosition(Math.PI / 2);
        vector(selectable.position, 10, 10, Math.PI / 2);
        selectable.setDriveType(SimulatedSelectableDrivetrain.DriveType.DUAL_ACTUATED);
        selectable.setDriveState(SimDualActuatedConfig.DriveState.HOLONOMIC);
        assertFalse(selectable.isTankKinematics());
    }

    @Test public void rejectsIncompleteRearPairAndInvalidGeometry() {
        SimTankConfig c = new SimTankConfig();
        c.frontLeftMotorName = "fl"; c.frontRightMotorName = "fr";
        c.wheelRadius = 2; c.trackWidth = 10; c.backLeftMotorName = "bl";
        assertThrows(IllegalArgumentException.class, () -> new SimulatedTank(c));
        c.backLeftMotorName = null;
        c.trackWidth = Double.NaN;
        assertThrows(IllegalArgumentException.class, () -> new SimulatedTank(c));
        c.trackWidth = 10; c.wheelRadius = 0;
        assertThrows(IllegalArgumentException.class, () -> new SimulatedTank(c));
    }
}
