package org.jjophoven.simulator;

import org.jjophoven.simhardware.drivetrain.MotionVector;

public class FieldBoundary {
    public static final MotionVector[] FIELD = {
            new MotionVector(0, 0, 0),
            new MotionVector(0, 68.8, 0),
            new MotionVector(7.6, 68.8, 0),
            new MotionVector(7.6, 118.8, 0),
            new MotionVector(23.4, 141.3, 0),
            new MotionVector(117.9, 141.3, 0),
            new MotionVector(133.8, 118.8, 0),
            new MotionVector(133.8, 68.8, 0),
            new MotionVector(141.3, 68.8, 0),
            new MotionVector(141.3, 0, 0)
    };

    public static boolean isOutOfBounds(
            MotionVector pose,
            RobotGeometry robot
    ) {
        MotionVector[] corners = robot.corners(pose);

        for (MotionVector p : corners) {
            if (!pointInsidePolygon(p, FIELD)) {
                return true;
            }
        }


        for (int i = 0; i < 4; i++) {
            MotionVector a1 = corners[i];
            MotionVector a2 = corners[(i + 1) % 4];

            for (int j = 0; j < FIELD.length; j++) {
                MotionVector b1 = FIELD[j];
                MotionVector b2 = FIELD[(j + 1) % FIELD.length];

                if (segmentsIntersect(a1, a2, b1, b2)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static MotionVector closestInBoundsPosition(
            MotionVector previousLegalPose,
            MotionVector desiredPose,
            RobotGeometry robot
    ) {
        // Already legal.
        if (!isOutOfBounds(desiredPose, robot)) {
            return desiredPose;
        }

        MotionVector closest = closestInBoundsPosition(desiredPose, robot); // i'm sorry i have two funcs
        if (!isOutOfBounds(closest, robot)) {
            return closest;
        }

        double low = 0.0;
        double high = 1.0;

        // Binary search for the last legal pose.
        for (int i = 0; i < 40; i++) {
            double t = (low + high) * 0.5;

            MotionVector test = interpolate(previousLegalPose, desiredPose, t);

            if (isOutOfBounds(test, robot)) {
                high = t;
            } else {
                low = t;
            }
        }

        MotionVector result = interpolate(previousLegalPose, desiredPose, low);

        // Move back a tiny amount for floating-point safety.
        double dx = desiredPose.x - previousLegalPose.x;
        double dy = desiredPose.y - previousLegalPose.y;

        double len = Math.hypot(dx, dy);

        if (len > 1e-9) {
            final double EPS = 1e-4;

            result = new MotionVector(
                    result.x - dx / len * EPS,
                    result.y - dy / len * EPS,
                    result.theta
            );
        }

        return result;
    }
//
//    public static MotionVector closestInBoundsPosition(
//            MotionVector previousLegalPose,
//            MotionVector desiredPose,
//            RobotGeometry robot
//    ) {
//        // Desired translation + heading is already legal.
//        if (!isOutOfBounds(desiredPose, robot)) {
//            return desiredPose;
//        }
//
//        double low = 0.0;
//        double high = 1.0;
//
//        // Search only the translation path. Ignore heading while finding bounds.
//        for (int i = 0; i < 40; i++) {
//            double t = (low + high) * 0.5;
//
//            MotionVector test = new MotionVector(
//                    previousLegalPose.x + (desiredPose.x - previousLegalPose.x) * t,
//                    previousLegalPose.y + (desiredPose.y - previousLegalPose.y) * t,
//                    previousLegalPose.theta // Always favor the requested heading
//            );
//
//            if (isOutOfBounds(test, robot)) {
//                high = t;
//            } else {
//                low = t;
//            }
//        }
//
//        MotionVector result = new MotionVector(
//                previousLegalPose.x + (desiredPose.x - previousLegalPose.x) * low,
//                previousLegalPose.y + (desiredPose.y - previousLegalPose.y) * low,
//                desiredPose.theta
//        );
//
//        // Move slightly back along translation direction for floating point safety.
//        double dx = desiredPose.x - previousLegalPose.x;
//        double dy = desiredPose.y - previousLegalPose.y;
//
//        double len = Math.hypot(dx, dy);
//
//        if (len > 1e-9) {
//            final double EPS = 1e-4;
//
//            result = new MotionVector(
//                    result.x - dx / len * EPS,
//                    result.y - dy / len * EPS,
//                    desiredPose.theta
//            );
//        }
//
//        return result;
//    }

//    public static MotionVector closestInBoundsPosition(
//            MotionVector previousLegalPose,
//            MotionVector desiredPose,
//            RobotGeometry robot
//    ) {
//        // Desired pose is already legal.
//        if (!isOutOfBounds(desiredPose, robot)) {
//            return desiredPose;
//        }
//
//        // Direction of attempted movement.
//        double dx = desiredPose.x - previousLegalPose.x;
//        double dy = desiredPose.y - previousLegalPose.y;
//        double len = Math.hypot(dx, dy);
//
//        // If only heading is the problem, back up until it fits.
//        if (len < 1e-9 || !isOutOfBounds(new MotionVector(
//                previousLegalPose.x,
//                previousLegalPose.y,
//                desiredPose.theta
//        ), robot)) {
//            return new MotionVector(
//                    previousLegalPose.x,
//                    previousLegalPose.y,
//                    desiredPose.theta
//            );
//        }
//
//        double dirX = dx / len;
//        double dirY = dy / len;
//
//        double x = previousLegalPose.x;
//        double y = previousLegalPose.y;
//
//        // Move backwards until desired heading fits.
//        double backupStep = 0.01;
//
//        while (isOutOfBounds(new MotionVector(x, y, desiredPose.theta), robot)) {
//            x -= dirX * backupStep;
//            y -= dirY * backupStep;
//        }
//
//        MotionVector backupPose = new MotionVector(x, y, desiredPose.theta);
//
//        // Now search forward from the backed-up legal position.
//        double low = 0.0;
//        double high = 1.0;
//
//        for (int i = 0; i < 40; i++) {
//            double t = (low + high) * 0.5;
//
//            MotionVector test = new MotionVector(
//                    backupPose.x + (desiredPose.x - backupPose.x) * t,
//                    backupPose.y + (desiredPose.y - backupPose.y) * t,
//                    desiredPose.theta
//            );
//
//            if (isOutOfBounds(test, robot)) {
//                high = t;
//            } else {
//                low = t;
//            }
//        }
//
//        return new MotionVector(
//                backupPose.x + (desiredPose.x - backupPose.x) * low,
//                backupPose.y + (desiredPose.y - backupPose.y) * low,
//                desiredPose.theta
//        );
//    }


    private static MotionVector interpolate(
            MotionVector a,
            MotionVector b,
            double t
    ) {
        return new MotionVector(
                a.x + (b.x - a.x) * t,
                a.y + (b.y - a.y) * t,
                a.theta + (b.theta - a.theta) * t
        );
    }

    public static MotionVector closestInBoundsPosition2(
            MotionVector pose,
            RobotGeometry robot
    ) {
        double x = pose.x;
        double y = pose.y;

        final double EPS = 1e-4;

        for (int iter = 0; iter < 100; iter++) {

            MotionVector robotPose = new MotionVector(x, y, pose.theta);

            MotionVector[] robotPoly = robot.corners(robotPose);

            MotionVector mtv = getMTVInside(robotPoly, FIELD);

            if (mtv == null) {
                return robotPose;
            }

            double len = Math.sqrt(mtv.x * mtv.x + mtv.y * mtv.y);

            if (len < EPS) {
                break;
            }

            x += mtv.x + (mtv.x / len) * EPS;
            y += mtv.y + (mtv.y / len) * EPS;
        }

        return new MotionVector(x, y, pose.theta);
    }


    private static MotionVector getMTVInside(
            MotionVector[] object,
            MotionVector[] boundary
    ) {
        double smallestDepth = Double.POSITIVE_INFINITY;
        MotionVector smallestAxis = null;

        // Test all boundary edges
        for (int i = 0; i < boundary.length; i++) {

            MotionVector a = boundary[i];
            MotionVector b = boundary[(i + 1) % boundary.length];

            double edgeX = b.x - a.x;
            double edgeY = b.y - a.y;

            // outward normal
            double nx = edgeY;
            double ny = -edgeX;

            double length = Math.sqrt(nx * nx + ny * ny);
            nx /= length;
            ny /= length;

            double maxObject = projectMax(object, nx, ny);
            double boundaryProjection = projectMin(boundary, nx, ny);

            // object is outside this edge
            if (maxObject > boundaryProjection) {

                double depth = maxObject - boundaryProjection;

                if (depth < smallestDepth) {
                    smallestDepth = depth;

                    // Push inward
                    smallestAxis = new MotionVector(
                            -nx * depth,
                            -ny * depth,
                            0
                    );
                }
            }
        }

        return smallestAxis;
    }


    private static double projectMax(
            MotionVector[] polygon,
            double nx,
            double ny
    ) {
        double max = -Double.MAX_VALUE;

        for (MotionVector p : polygon) {
            double projection = p.x * nx + p.y * ny;
            max = Math.max(max, projection);
        }

        return max;
    }


    private static double projectMin(
            MotionVector[] polygon,
            double nx,
            double ny
    ) {
        double min = Double.MAX_VALUE;

        for (MotionVector p : polygon) {
            double projection = p.x * nx + p.y * ny;
            min = Math.min(min, projection);
        }

        return min;
    }

    public static MotionVector closestInBoundsPosition(
            MotionVector pose,
            RobotGeometry robot
    ) {
        double x = pose.x;
        double y = pose.y;

        final double EPS = 1e-4;

        for (int iter = 0; iter < 100; iter++) {

            MotionVector robotPose = new MotionVector(x, y, pose.theta);

            if (!isOutOfBounds(robotPose, robot)) {
                return robotPose;
            }

            MotionVector[] corners = robot.corners(robotPose);

            double bestPushX = 0;
            double bestPushY = 0;
            double largestPenetration = 0;

            for (MotionVector corner : corners) {

                if (pointInsidePolygon(corner, FIELD)) {
                    continue;
                }

                double closestDistSq = Double.POSITIVE_INFINITY;
                MotionVector closest = null;

                for (int i = 0; i < FIELD.length; i++) {

                    MotionVector a = FIELD[i];
                    MotionVector b = FIELD[(i + 1) % FIELD.length];

                    MotionVector p = closestPointOnSegment(corner, a, b);

                    double dx = p.x - corner.x;
                    double dy = p.y - corner.y;

                    double distSq = dx * dx + dy * dy;

                    if (distSq < closestDistSq) {
                        closestDistSq = distSq;
                        closest = p;
                    }
                }

                if (closest != null) {

                    double dx = closest.x - corner.x;
                    double dy = closest.y - corner.y;

                    double dist = Math.sqrt(dx * dx + dy * dy);

                    if (dist > largestPenetration) {
                        largestPenetration = dist;
                        bestPushX = dx;
                        bestPushY = dy;
                    }
                }
            }

            if (largestPenetration < EPS) {
                break;
            }

            double len = Math.sqrt(bestPushX * bestPushX + bestPushY * bestPushY);

            x += bestPushX + (bestPushX / len) * EPS;
            y += bestPushY + (bestPushY / len) * EPS;
        }

        return new MotionVector(x, y, pose.theta);
    }

//    public static MotionVector closestInBoundsPosition(
//            MotionVector pose,
//            RobotGeometry robot
//    ) {
//        double x = pose.x;
//        double y = pose.y;
//
//        final double EPS = 1e-6;
//
//        for (int iter = 0; iter < 20; iter++) {
//
//            MotionVector[] corners =
//                    robot.corners(new MotionVector(x, y, pose.theta));
//
//            boolean changed = false;
//
//            for (MotionVector corner : corners) {
//
//                if (pointInsidePolygon(corner, FIELD))
//                    continue;
//
//                double bestDist = Double.POSITIVE_INFINITY;
//                double pushX = 0;
//                double pushY = 0;
//
//                for (int i = 0; i < FIELD.length; i++) {
//
//                    MotionVector a = FIELD[i];
//                    MotionVector b = FIELD[(i + 1) % FIELD.length];
//
//                    MotionVector closest =
//                            closestPointOnSegment(corner, a, b);
//
//                    double dx = closest.x - corner.x;
//                    double dy = closest.y - corner.y;
//                    double distSq = dx * dx + dy * dy;
//
//                    if (distSq < bestDist) {
//                        bestDist = distSq;
//                        pushX = dx;
//                        pushY = dy;
//                    }
//                }
//
//                x += pushX + Math.signum(pushX) * EPS;
//                y += pushY + Math.signum(pushY) * EPS;
//                changed = true;
//            }
//
//            if (!changed &&
//                    !isOutOfBounds(new MotionVector(x, y, pose.theta), robot)) {
//                break;
//            }
//        }
//
//        return new MotionVector(x, y, pose.theta);
//    }

    private static boolean pointInsidePolygon(MotionVector p, MotionVector[] polygon) {
        boolean inside = false;

        for (int i = 0, j = polygon.length - 1; i < polygon.length; j = i++) {

            if ((polygon[i].y > p.y) != (polygon[j].y > p.y)
                    && p.x < (polygon[j].x - polygon[i].x)
                    * (p.y - polygon[i].y)
                    / (polygon[j].y - polygon[i].y)
                    + polygon[i].x) {

                inside = !inside;
            }
        }

        return inside;
    }

    private static MotionVector closestPointOnSegment(
            MotionVector p,
            MotionVector a,
            MotionVector b
    ) {
        double dx = b.x - a.x;
        double dy = b.y - a.y;

        double lenSq = dx * dx + dy * dy;

        if (lenSq == 0) {
            return new MotionVector(a.x, a.y, 0);
        }

        double t = ((p.x - a.x) * dx + (p.y - a.y) * dy) / lenSq;
        t = Math.max(0, Math.min(1, t));

        return new MotionVector(
                a.x + t * dx,
                a.y + t * dy,
                0
        );
    }

    // TODO use this for the robot
    private static boolean segmentsIntersect(MotionVector p1, MotionVector p2,
                                             MotionVector q1, MotionVector q2) {

        double o1 = orientation(p1, p2, q1);
        double o2 = orientation(p1, p2, q2);
        double o3 = orientation(q1, q2, p1);
        double o4 = orientation(q1, q2, p2);

        if (o1 * o2 < 0 && o3 * o4 < 0) {
            return true;
        }

        if (o1 == 0 && onSegment(p1, q1, p2)) return true;
        if (o2 == 0 && onSegment(p1, q2, p2)) return true;
        if (o3 == 0 && onSegment(q1, p1, q2)) return true;
        if (o4 == 0 && onSegment(q1, p2, q2)) return true;

        return false;
    }

    private static double orientation(MotionVector a, MotionVector b, MotionVector c) {
        return (b.x - a.x) * (c.y - a.y)
                - (b.y - a.y) * (c.x - a.x);
    }

    private static boolean onSegment(MotionVector a, MotionVector b, MotionVector c) {
        final double EPS = 1e-9;

        return b.x >= Math.min(a.x, c.x) - EPS
                && b.x <= Math.max(a.x, c.x) + EPS
                && b.y >= Math.min(a.y, c.y) - EPS
                && b.y <= Math.max(a.y, c.y) + EPS;
    }
}