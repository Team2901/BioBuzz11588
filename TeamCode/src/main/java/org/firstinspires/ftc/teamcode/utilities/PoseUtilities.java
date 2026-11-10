package org.firstinspires.ftc.teamcode.utilities;

import android.annotation.SuppressLint;

import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;

public class PoseUtilities {

    /**
     * General purpose error function: turn how far the robot is from a target into the power
     * to apply, using a trapezoidal profile.
     * <p>
     * The result is {@code value * errorScaler}, with its magnitude clipped to between
     * {@code minThreshold} and {@code maxThreshold}. The sign of {@code value} is kept, so the
     * robot drives back toward the target if it overshoots. Far from the target the power is
     * capped at the max; close in it ramps down linearly; very close it stays at the min so the
     * motors never stall short of the target. The caller decides when it is close enough to stop.
     * <p>
     * Example #1: drive in the X or Y direction at full power until within 2 ft of the target,
     * then slow down linearly:
     * <pre>
     *     value:        distance remaining to the target (inches)
     *     errorScaler:  1.0 (max power) / 24 (inches)
     *     maxThreshold: 1.0 (max power)
     *     minThreshold: 0.01 (min power aka stall speed)
     * </pre>
     * Example #2: pivot at full power until within 30 degrees of a target heading, then slow
     * down linearly:
     * <pre>
     *     value:        angle remaining to the target heading (degrees), e.g. from {@link #angleDiffDegrees}
     *     errorScaler:  1.0 (max power) / 30 (degrees)
     *     maxThreshold: 1.0 (max power)
     *     minThreshold: 0.01 (min power aka stall speed)
     * </pre>
     *
     * @param value        signed distance or angle remaining to the target.
     * @param errorScaler  power per unit of {@code value}; usually maxThreshold divided by the
     *                     distance at which slowing down should begin.
     * @param maxThreshold largest power magnitude to return.
     * @param minThreshold smallest power magnitude to return (stall speed).
     * @return the power to apply, with the same sign as {@code value}.
     */
    public static double getError(double value, double errorScaler, double maxThreshold, double minThreshold) {
        // Step 1. Scale the input value by the errorScaler.
        double result = value * errorScaler;

        // Step 2. Clip the result to the max.
        // Step 3. Clip the result to the min.
        result = Math.signum(result) * Range.clip(Math.abs(result), minThreshold, maxThreshold);

        return result;
    }

    /**
     * Computes the shortest signed difference between two angles, in degrees.
     * <p>
     * The result is {@code degreesA - degreesB} wrapped into [-180, 180)
     *
     * @param degreesA the angle to measure to, in degrees
     * @param degreesB the angle to measure from, in degrees
     * @return the signed difference {@code degreesA - degreesB}, normalized to [-180, 180)
     */
    public static double angleDiffDegrees(double degreesA, double degreesB) {
        return AngleUnit.normalizeDegrees(degreesA - degreesB);
    }

    /**
     * Computes the shortest signed difference between two angles, in radians.
     * <p>
     * The result is {@code radiansA - radiansB} wrapped into [-π, π)
     *
     * @param radiansA the angle to measure to, in radians
     * @param radiansB the angle to measure from, in radians
     * @return the signed difference {@code radiansA - radiansB}, normalized to [-π, π)
     */
    public static double angleDiffRadians(double radiansA, double radiansB) {
        return AngleUnit.normalizeRadians(radiansA - radiansB);
    }

    /**
     * Return the pose A-B: the X, Y and heading changes needed to get from pose B to pose A.
     * <p>
     * Typically A is the target pose and B is the robot's current pose, so the result is how
     * far the robot still has to go. Both poses and the result are in field coordinates.
     *
     * @param poseA the pose to measure to (e.g. the target).
     * @param poseB the pose to measure from (e.g. the robot's current pose).
     * @return a pose holding A-B for X and Y (inches) and the shortest turn from B's heading
     * to A's heading (degrees, -180 to 180).
     */
    public static Pose2D pose2DDelta(Pose2D poseA, Pose2D poseB) {
        // Step 1. Calculate the X difference A-B.
        // Read both X values in the same unit (inches), e.g. poseA.getX(DistanceUnit.INCH).
        double xDiff = poseA.getX(DistanceUnit.INCH) - poseB.getX(DistanceUnit.INCH);

        // Step 2. Calculate the Y difference A-B the same way.
        double yDiff = poseA.getY(DistanceUnit.INCH) - poseB.getY(DistanceUnit.INCH);

        // Step 3.  Calculate the heading difference A-B
        // Use the helper method angleDiffDegrees, so the result is the shortest turn.
        // e.g. -160 - 180 -> 20 instead of -340.
        double headingDegreesDiff = PoseUtilities.angleDiffDegrees(poseA.getHeading(AngleUnit.DEGREES), poseB.getHeading(AngleUnit.DEGREES));

        // Step 4. Create the pose to return from those three values
        // Pose2D has no setters, so the values must go into the constructor (replace the zeros below).
        return new Pose2D(DistanceUnit.INCH, xDiff, yDiff, AngleUnit.DEGREES, headingDegreesDiff);
    }

    /**
     * Format a pose for telemetry as "X Y heading", to two decimal places.
     *
     * @param pose the pose to format.
     * @return X and Y in inches and the heading in degrees, separated by spaces.
     */
    @SuppressLint("DefaultLocale")
    public static String poseToString(Pose2D pose) {
        return String.format("%.2f %.2f %.2f", pose.getX(DistanceUnit.INCH), pose.getY(DistanceUnit.INCH), pose.getHeading(AngleUnit.DEGREES));
    }
}
