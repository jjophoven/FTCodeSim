/*
 * Copyright (c) 2026 Pedro Pathing
 * SPDX-License-Identifier: BSD-3-Clause
 */
package org.firstinspires.ftc.teamcode.bocaj.pedro;

import com.pedropathing.config.ConfigVar;
import com.pedropathing.config.Configuration;
import com.pedropathing.controllers.Controller;
import com.pedropathing.math.Matrix;

import java.util.OptionalDouble;

import static com.pedropathing.config.Validator.nonnegative;
import static com.pedropathing.config.Validator.positive;

// TODO test iZone, decay, and maxI to prevent integral wind-up and have zero-steady state error
// TODO unify pid deltaTimes

public final class PredictiveConfig {
    public final ConfigVar<Controller> headingController = ConfigVar.of(Controller.pid(1.5, 0, 0.1));

    public final ConfigVar<Controller> forwardTranslationalController = ConfigVar.of(Controller.pid(0.3, 0, 0)
            .plus(Controller.staticFeedforward(0.015)));
    public final ConfigVar<Controller> lateralTranslationalController = ConfigVar.of(Controller.pid(0.3, 0, 0)
            .plus(Controller.staticFeedforward(0.015)));

    public final ConfigVar<Controller> coastController = ConfigVar.of(Controller.pid(0.025, 0, 0)
            .plus(Controller.dynamicFeedforward(0.015))
            .plus(Controller.staticFeedforward(0.05))); // TODO change to sgn(t)

    /**
     * This scales the translational error correction power when holding.
     */
    public final ConfigVar<Double> holdPointTranslationalScaling = ConfigVar.of(0.45, nonnegative());

    /**
     * This scales the heading error correction power when holding.
     */
    public final ConfigVar<Double> holdPointHeadingScaling = ConfigVar.of(0.35, nonnegative());

    /**
     * Centripetal force to power scaling.
     */
    public final ConfigVar<Double> centripetalScaling = ConfigVar.of(0.005 * 12, nonnegative());

    /**
     * The maximum amount of power the robot can apply in the opposite direction of momentum. Default is 0.2. Too high of a value might burn out the control hub and too low of a value might not be able to stop quickly after back-emf is overcome.
     */
    public final ConfigVar<Double> maxBrakingPower = ConfigVar.of(0.2, positive());

    public final ConfigVar<OptionalDouble> maxAccelerationConstraint = ConfigVar.of(OptionalDouble.empty());
    public final ConfigVar<OptionalDouble> maxVelocityConstraint = ConfigVar.of(OptionalDouble.empty());
    public final ConfigVar<OptionalDouble> maxCoastDecelerationConstraint = ConfigVar.of(OptionalDouble.empty());

    /**
     * How much overshooting is allowed when braking. A value of 1 means no bias, while a value greater than 1 means the controller will overshoot the target, and a value lower than 1 means the controller will undershoot the target.
     * <p>
     * Useful if you do not need to fully brake due to an obstacle that can slow you down.
     * Lower number is helpful if you need to ensure you do not overshoot the target.
     */
    public final ConfigVar<Double> brakeOvershootBias = ConfigVar.of(1.0, positive());

    /**
     * The velocity the robot coasts down to before it starts braking. Does nothing if the coastingConstraintScale is infinity.
     */
    public final ConfigVar<Double> coastDownToVelocity = ConfigVar.of(0.0, nonnegative());

    public final ConfigVar<Double> headingDeviationTolerance = ConfigVar.of(Math.toRadians(45), positive());
    public final ConfigVar<Double> translationalDeviationTolerance = ConfigVar.of(2.5, positive());
    public final ConfigVar<Boolean> brakeAtEnd = ConfigVar.of(true);

    public final ConfigVar<Matrix> linearBrakeCoefficients = ConfigVar.required();
    public final ConfigVar<Matrix> quadraticBrakeCoefficients = ConfigVar.required();

    /**
     * Maximum achievable speed that the robot can move forward/backward at, in units per second.
     */
    public final ConfigVar<Double> maxAchievableForwardVelocity = ConfigVar.required(positive());

    /**
     * Maximum achievable speed that the robot can move laterally, in units per second.
     */
    public final ConfigVar<Double> maxAchievableStrafeVelocity = ConfigVar.required(positive());

    public final ConfigVar<Double> naturalForwardDeceleration = ConfigVar.required(positive());
    public final ConfigVar<Double> naturalStrafeDeceleration = ConfigVar.required(positive());

    /**
     * The distance the controller will stop commanding power to correct for path deviations.
     */
    public final ConfigVar<Double> minCorrectionDistance = ConfigVar.of(1e-3);

    public final ConfigVar<Double> parametricTConstraint = ConfigVar.of(0.025, positive());

    public final ConfigVar<Double> headingConstraint = ConfigVar.of(0.007, positive());
    public final ConfigVar<Double> translationalConstraint = ConfigVar.of(0.1, positive());
    public final ConfigVar<Double> velocityConstraint = ConfigVar.of(0.1, positive());
    public final ConfigVar<Double> timeoutConstraint = ConfigVar.of(100.0, nonnegative());

    public PredictiveConfig(Configuration<PredictiveConfig> config) {
        config.configure(this);
    }
}
