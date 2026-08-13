package org.firstinspires.ftc.teamcode.bocaj.pedro;

import com.pedropathing.algorithm.Algorithm;
import com.pedropathing.drivetrain.DrivePowers;
import com.pedropathing.drivetrain.Drivetrain;
import com.pedropathing.localization.MotionState;
import com.pedropathing.math.Pose;
import com.pedropathing.math.Twist;
import com.pedropathing.math.Vector2D;
import com.pedropathing.paths.PathTracker;
import com.pedropathing.utils.Control;
import com.pedropathing.utils.Timer;

import java.util.OptionalDouble;
import java.util.concurrent.TimeUnit;

import static com.pedropathing.utils.Angle.normalizeSigned;

/**
 * @author Jacob O
 */
public class Predictive2 implements Algorithm {
    public final PredictiveConfig config;
    private double closestT, curvature;
    private double curveCompletion, remainingDistance, tangentialSpeed;
    private Pose closestPose;
    private Vector2D closestTangent, closestNormal;

    private final Timer timer = new Timer();
    private boolean resetTimer = true;
    private double headingError, translationalError, targetVelocity;
    private boolean busy = false;
    private boolean isBraking = false;

    public boolean isBraking() {
        return isBraking;
    }

    public Predictive2(PredictiveConfig config) {
        this.config = config;
    }

    @Override
    public DrivePowers calculatePath(Drivetrain drivetrain, PathTracker pathTracker, MotionState state, double deltaTime) {
        closestT = pathTracker.current().curve.closestT(state.pose().toVector2D(), closestT);

        if (testParametric()) { // End Constraint
            if (pathTracker.remainingPaths() > 1) { // advance if constraints met
                pathTracker.advance();
                reset();
                return calculatePath(drivetrain, pathTracker, state, deltaTime);
            }

            pathTracker.advance();
            reset();
            return DrivePowers.zero();
        }

        closestTangent = pathTracker.current().curve.tangent(closestT);
        tangentialSpeed = closestTangent.dot(state.velocity().toVector2D());
        double tangentBrakingDisplacement = getBrakeDisplacement(state.twist(), state.pose().heading()).dot(closestTangent) * config.brakeOvershootBias.get();

        remainingDistance = pathTracker.current().curve.remainingDistance(closestT);

        isBraking = remainingDistance <= tangentBrakingDisplacement;
        boolean pathSkip = isBraking && (pathTracker.remainingPaths() > 1 || !config.brakeAtEnd.get());

        if (pathSkip) {
            pathTracker.advance();
            return calculatePath(drivetrain, pathTracker, state, deltaTime);
        }

        double targetHeading = pathTracker.current().heading(closestT);
        closestPose = pathTracker.current().curve.get(closestT).toPose(targetHeading);
        headingError = headingError(state.pose().heading(), targetHeading);
        double headingPower = headingPower(headingError, state);
        curveCompletion = 1 - remainingDistance / pathTracker.current().curve.length();

        double tangentPower = tangent(isBraking, state, tangentBrakingDisplacement, deltaTime, state.pose().heading(), tangentialSpeed, remainingDistance);

        closestNormal = pathTracker.current().curve.leftNormal(closestT);
        double normalError = closestPose.minus(state.pose()).toVector2D().dot(closestNormal);
        double normalBrakingDisplacement = getBrakeDisplacement(state.twist(), state.pose().heading()).dot(closestNormal);

        double normalPower = computeTranslationalCorrection(
                state,
                closestNormal.times(normalError),
                closestNormal.times(normalBrakingDisplacement))
                .dot(closestNormal);

        curvature = pathTracker.current().curve.curvature(closestT);
        double centripetal = centripetal(tangentialSpeed, curvature);// + config.normalFeedforward.get() * tangentialSpeed;
        normalPower += centripetal;

        double normalSquared = normalPower * normalPower;
        double remainingSquared = Math.max(0, 1 - normalSquared);

        double headingLimit = Math.sqrt(remainingSquared);
        headingPower = Math.max(-headingLimit, Math.min(headingLimit, headingPower));

        remainingSquared -= headingPower * headingPower;
        remainingSquared = Math.max(0, remainingSquared);

        double tangentLimit = Math.sqrt(remainingSquared);
        tangentPower = Math.max(-tangentLimit, Math.min(tangentLimit, tangentPower));
        Vector2D driveVector = closestNormal.times(normalPower).plus(closestTangent.times(tangentPower));
        driveVector = driveVector.times(getDriveScalar(normalError, headingError));

        return getDrivePowers(driveVector, state, headingPower);
    }

    @Override
    public DrivePowers calculateHold(Drivetrain drivetrain, Pose target, MotionState state, boolean useScaling, double deltaTime) {
        if (resetTimer) {
            timer.reset();
            resetTimer = false;
        }

        closestPose = target;
        headingError = headingError(state.pose().heading(), target.heading());
        Vector2D displacementToPath = closestPose.minus(state.pose()).toVector2D();
        translationalError = displacementToPath.magnitude();

        if (displacementToPath.isZero()) {
            tangentialSpeed = 0;
            closestTangent = Vector2D.zero();
        }
        else {
            closestTangent = displacementToPath.normalized();
            tangentialSpeed = closestTangent.dot(state.velocity().toVector2D());
        }

        if (busy && testTimeout() || (testHeading() && testTranslational() && testVelocity()))
            busy = false;

        Vector2D translational = computeTranslationalCorrection(
                state,
                displacementToPath,
                getBrakeDisplacement(state.twist(), state.pose().heading()));
        double headingPower = headingPower(headingError, state);

        if (useScaling) {
            double translationalScale = config.holdPointTranslationalScaling.get();
            double headingScale = config.holdPointHeadingScaling.get();

            translational = translational.times(translationalScale);
            headingPower *= headingScale;
        }

        return getDrivePowers(translational, state, headingPower);
    }

    @Override
    public double closestT() {
        return closestT;
    }

    @Override
    public Pose closestPose() {
        return closestPose;
    }

    @Override
    public Vector2D closestTangent() {
        return closestTangent;
    }

    @Override
    public Vector2D closestNormal() {
        return closestNormal;
    }

    @Override
    public double curvature() {
        return curvature;
    }

    @Override
    public double remainingDistance() {
        return remainingDistance;
    }

    @Override
    public double pathCompletion() {
        return curveCompletion;
    }

    @Override
    public boolean atParametricEnd(double t) {
        return testParametric();
    }

    @Override
    public void reset() {
        timer.reset();
        resetTimer = true;
        busy = true;
        closestT = 0.0;
    }

    /**
     * Compute heading correction power for the given state and target heading.
     */
    public double headingPower(double headingError, MotionState state) {
        return config.headingController.get().calculate(0, headingError, state.twist().omega());
    }

    /**
     * Gives a drive scalar to scale down the drive power based on the translational and
     * heading errors. This is to prevent aggressive drive correction when the robot
     * is deviating a lot from the path or facing the wrong direction.
     */
    public double getDriveScalar(double normalError, double headingError) {
        double trackDeviationScale = Control.cosineScale(normalError, config.translationalDeviationTolerance.get());
        double headingScale = Control.cosineScale(headingError, config.headingDeviationTolerance.get());
        return trackDeviationScale * headingScale;
    }

    public DrivePowers getDrivePowers(Vector2D fieldRelativeDrivePower, MotionState state, double headingPower) {
        Vector2D robotFrameDrivePower =
                fieldRelativeDrivePower.rotate(-state.pose().heading());
        double forward = Control.clampBrakingPower(
                robotFrameDrivePower.x(), state.twist().vx(), config.maxBrakingPower.get());
        double strafe = Control.clampBrakingPower(
                robotFrameDrivePower.y(), state.twist().vy(), config.maxBrakingPower.get());
        return new DrivePowers(forward, strafe, headingPower);
    }

    public double headingError(double current, double target) {
        return normalizeSigned(target - current);
    }

    public Vector2D computeTranslationalCorrection(MotionState state, Vector2D displacementVector, Vector2D brakingDisplacement) {
        if (displacementVector == null || displacementVector.isZero()) return Vector2D.zero();
        Vector2D adjustedError = displacementVector.minus(brakingDisplacement);
        double distance = adjustedError.magnitude();
        if (distance < config.minCorrectionDistance.get()) return Vector2D.zero();

        Vector2D bodyFrameError = adjustedError.toBodyFrame(state.pose().heading());
        return Vector2D.cartesian(
                config.forwardTranslationalController.get().calculate(0, bodyFrameError.x()),
                config.lateralTranslationalController.get().calculate(0, bodyFrameError.y())
        ).toWorldFrame(state.pose().heading());
    }

    public double centripetal(double speed, double curvature) {
        return speed * speed * curvature * config.centripetalScaling.get();
    }

    public double tangent(boolean isBraking, MotionState state, double tangentBrakingDisplacement, double deltaTime, double heading, double tangentialVel, double remainingDistance) {
        if (isBraking) {
            return computeTranslationalCorrection(
                    state,
                    closestTangent.times(remainingDistance),
                    closestTangent.times(tangentBrakingDisplacement))
                    .dot(closestTangent);
        }

        OptionalDouble maxAccelerationConstraint = config.maxAccelerationConstraint.get();
        OptionalDouble maxVelocityConstraint = config.maxVelocityConstraint.get();
        OptionalDouble maxDecelerationConstraint = config.maxCoastDecelerationConstraint.get();

        double headingFromTangent = closestTangent.angleTo(Vector2D.unit(heading));
        double maxAchievableVelocity = Diamond.interpolateRadius(config.maxAchievableForwardVelocity.get(), config.maxAchievableStrafeVelocity.get(), headingFromTangent);

        double constrainedVelocity = maxAchievableVelocity;
        if (maxVelocityConstraint.isPresent()) {
            constrainedVelocity = Math.min(constrainedVelocity, maxVelocityConstraint.getAsDouble());
        }
        if (maxAccelerationConstraint.isPresent()) {
            constrainedVelocity = Math.min(constrainedVelocity, tangentialVel + maxAccelerationConstraint.getAsDouble() * deltaTime);
        }

        double targetVel = constrainedVelocity;
        double feedforwardVelocity = targetVel;

        if (maxDecelerationConstraint.isPresent()) {
            double naturalDeceleration = Diamond.interpolateRadius(config.naturalForwardDeceleration.get(), config.naturalStrafeDeceleration.get(), headingFromTangent);
            double velocityNeededToCoastInTime = Math.sqrt(config.coastDownToVelocity.get() * config.coastDownToVelocity.get() - 2 * -maxDecelerationConstraint.getAsDouble() * remainingDistance);
            double zeroPowerCoastFinalVelSquared = tangentialVel * tangentialVel - 2 * naturalDeceleration * remainingDistance;
            double zeroPowerCoastFinalVel = Math.signum(zeroPowerCoastFinalVelSquared) * Math.sqrt(Math.abs(zeroPowerCoastFinalVelSquared));
            targetVel = Math.min(targetVel, velocityNeededToCoastInTime);
            double velocityMomentumCannotProvide = Math.max(0, (config.coastDownToVelocity.get() - zeroPowerCoastFinalVel));
            feedforwardVelocity = Math.min(feedforwardVelocity, velocityMomentumCannotProvide);
        }

        if (targetVel >= maxAchievableVelocity) {
            return 1;
        }

        double error = Math.max(0.00001, targetVel - tangentialVel);

        return config.coastController.get().calculate(feedforwardVelocity, error);
    }

    public Vector2D getBrakeDisplacement(Twist twist, double heading) {
        Vector2D linearTwist = twist.toVector2D();

        Vector2D quadratic =
                linearTwist.hadamardProduct(linearTwist.abs()).transform(config.quadraticBrakeCoefficients.get());
        Vector2D linear = linearTwist.transform(config.linearBrakeCoefficients.get());

        return quadratic.plus(linear).rotate(heading);
    }

    public double getHeadingError() {
        return headingError;
    }

    public double getTranslationalError() {
        return translationalError;
    }

    public boolean testVelocity() {
        return tangentialSpeed < config.velocityConstraint.get();
    }

    public boolean testTranslational() {
        return Math.abs(translationalError) < config.translationalConstraint.get();
    }

    public boolean testHeading() {
        return Math.abs(headingError) < config.headingConstraint.get();
    }

    public boolean testParametric() {
        return closestT >= (1 - config.parametricTConstraint.get());
    }

    public boolean testTimeout() {
        return !resetTimer && timer.get(TimeUnit.MILLISECONDS) > config.timeoutConstraint.get();
    }

    public double getTargetVelocity() {
        return targetVelocity;
    }

    @Override
    public boolean isBusy() {
        return busy;
    }
}
