package org.firstinspires.ftc.teamcode.hardware;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;

public class ExampleTurret {

    public Telemetry telemetry;

    private final static double turretGearRatio = 5.0;

    /// Encoder ticks per turret rotation: the motor turns 5 times for each turret rotation.
    public static final double TICKS_PER_MOTOR_REV = 537.7;
    public static final double turretTicksPerRotation = TICKS_PER_MOTOR_REV * turretGearRatio;

    /// The turret's hard stops, in degrees from facing forward, positive counter-clockwise.
    /// min must be below max, and the turret must face forward (0) when INIT is pressed, so
    /// min must be at most 0 and max at least 0.
    public static final double minTurretHeading = -360.0;
    public static final double maxTurretHeading = 360.0;

    /// Where the turret's pivot is on the robot, in inches from the robot's center (the point its
    /// pose is measured at), in robot coordinates: X is forward and Y is left.
    /// -3.72, 0 matches the simulator's Biobuzz V2, whose turret sits 3.72 inches behind center;
    /// measure the real robot and change these to match.
    public static final double turretOffsetXInches = -3.72;
    public static final double turretOffsetYInches = 0.0;

    public DcMotorEx turretMotor;

    /**
     * Find the turret motor named "turret" and start counting its encoder from here, so the turret
     * must face forward when INIT is pressed. If the robot has no turret, a {@link MockDcMotor}
     * stands in so the rest of the code still runs. Call once from the OpMode's init.
     *
     * @param hardwareMap the OpMode's hardware map.
     * @param telemetry   the OpMode's telemetry, kept for reporting status.
     */
    public ExampleTurret(HardwareMap hardwareMap, Telemetry telemetry) {
        this.telemetry = telemetry;

        try {
            turretMotor = hardwareMap.get(DcMotorEx.class, "turret");
        } catch (IllegalArgumentException e) {
            turretMotor = new MockDcMotor();
            telemetry.addLine("Can't find turret: making a mock");
        }

        // Encoder 0 is the turret facing forward, so it must be facing forward when INIT is pressed.
        turretMotor.setDirection(DcMotorSimple.Direction.FORWARD);
        turretMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    /**
     * Per-loop update for the turret. Call once every loop.
     *
     * @param robotPoseRelField     the robot's pose in field coordinates; only its heading is used.
     */
    public void update(Pose2D robotPoseRelField) {
        // Step 1: choose target pose for turret to aim at
        // TODO implement step H
        Pose2D targetPoseRelField = null;

        // Step 2: aim at target
        aimTurretAtRelField(targetPoseRelField, robotPoseRelField);
    }

    /**
     * Set the turret power for one step of turning the turret to face a heading on the field.
     * <p>
     * This does not wait for the turret to arrive. It looks at where the turret is now, sets the
     * power once, and returns. Call it repeatedly in a loop; the caller decides when the turret
     * is close enough. The heading stays fixed on the field, so the turret keeps facing it while
     * the robot turns underneath.
     *
     * @param targetHeadingRelField the heading to face, in field coordinates, in degrees: 0 faces
     *                              field +X and positive is counter-clockwise.
     * @param robotPoseRelField     the robot's pose in field coordinates; only its heading is used.
     */
    public void turnTowardsRelField(double targetHeadingRelField, Pose2D robotPoseRelField) {
        // Step 1. Convert the field heading into a heading relative to the robot, -180 to 180.
        // TODO implement step D
        double targetHeadingRelRobot = 0;

        // Step 2. Pick the turret position that faces it: the shorter of turning counter-clockwise
        // or clockwise that stays inside the hard stops.
        double closestTargetHeadingRelRobot = chooseReachableHeading(targetHeadingRelRobot);

        // Step 3. Turn the turret toward that position.
        turnTowardsRelRobot(closestTargetHeadingRelRobot);
    }
    
    /**
     * Choose the turret position that faces a heading on the robot, without passing a hard stop.
     * <p>
     * From where it is now, the turret can face the heading two ways: by turning counter-clockwise
     * or by turning clockwise. Together the two turns add up to one full rotation. This picks the
     * shorter one that stays between {@link #minTurretHeading} and {@link #maxTurretHeading}. If
     * the range is less than a full rotation and neither fits, it picks whichever hard stop is
     * closer to facing the heading.
     *
     * @param targetHeadingRelRobot the heading to face relative to the robot, in degrees.
     * @return the turret position to turn to, in degrees, minTurretHeading to maxTurretHeading:
     * 0 faces the robot's front and positive is counter-clockwise.
     */
    public double chooseReachableHeading(double targetHeadingRelRobot) {
        // Step 1. Calculate the shortest turn from the turret's current heading to the target (-180 to 180).
        // TODO implement step F-1
        double currentHeadingRelRobot = getHeadingRelRobot();
        double targetHeadingRelTurret = 0;

        // Step 2. From that, calculate the turn in each direction: counter-clockwise is positive,
        // clockwise is negative, and together they make one full rotation.
        // e.g. if targetHeadingRelTurret is 30, the counter-clockwise turn is 30 and the clockwise
        // turn is 30 - 360 = -330.
        // TODO implement step F-2
        double ccwTargetHeadingRelTurret;
        double cwTargetHeadingRelTurret;

        // Step 3. Add each turn to the current heading to get the two candidate turret positions.
        // TODO implement step F-3
        double ccwTargetHeadingRelRobot = 0;
        double cwTargetHeadingRelRobot = 0;

        // Step 4. Check whether each candidate is inside the turret's range
        // (see minTurretHeading and maxTurretHeading).
        // TODO implement step F-4
        boolean canTurnCounterClockwise = false;
        boolean canTurnClockwise = false; 

        // Step 5. Return the candidate to turn to.
        // If both are in range: use the shorter turn.
        // If only one is in range: use it.
        // If neither is (the range is under one rotation): use the stop that faces nearest the heading.
        // TODO implement step F-5
        return targetHeadingRelRobot;
    }

    /**
     * Set the turret power for one step of turning the turret to an angle on the robot.
     * <p>
     * This does not wait; call it every loop. The power follows the PoseUtilities.getError
     * profile: full power far from the target, ramping down linearly once within 30 degrees.
     * <p>
     * The turret can only turn between {@link #minTurretHeading} and {@link #maxTurretHeading},
     * so the error is taken without wrapping around: the turret always turns the way that stays
     * inside its range rather than into its hard stop.
     *
     * @param targetHeadingRelRobot the turret angle to turn to, relative to the robot, in degrees:
     *                              0 faces the robot's front and positive is counter-clockwise.
     *                              Not wrapped: 270 and -90 are different turret positions.
     *                              Clamped to minTurretHeading to maxTurretHeading.
     */
    public void turnTowardsRelRobot(double targetHeadingRelRobot) {
        // Step 1. Keep the target inside the turret's range.
        // TODO implement step C-1
        double clippedTargetHeadingRelRobot = targetHeadingRelRobot;

        // Step 2. Get the error without wrapping, so the turret never turns through its hard stop.
        double errorDegrees = clippedTargetHeadingRelRobot - getHeadingRelRobot();

        // Step 3. Turn the error into a power, slowing down within 30 degrees.
        // TODO implement step C-2
        double counterClockwisePower = 1;

        // Step 4. Turn the turret.
        // TODO implement step C-3
    }

    /**
     * Read the turret's angle from the motor encoder.
     *
     * @return the turret angle relative to the robot, in degrees: 0 faces the robot's front and
     * positive is counter-clockwise.
     */
    public double getHeadingRelRobot() {
        // TODO implement step A
        return 0;
    }

    /**
     * Get the direction the turret is facing on the field: the robot's heading plus the turret's
     * angle on the robot.
     *
     * @param robotPoseRelField the robot's pose in field coordinates; only its heading is used.
     * @return the turret heading relative to the field, in degrees, -180 to 180, positive
     * counter-clockwise, in the same frame as the robot's heading.
     */
    public double getHeadingRelField(Pose2D robotPoseRelField) {
        // TODO implement step B
        return 0;
    }

    /**
     * Convert the robot's pose into the turret's pose: the same heading, moved from the robot's
     * center to the turret's pivot (see {@link #turretOffsetXInches} and {@link #turretOffsetYInches}).
     *
     * @param robotPoseRelField the robot's pose in field coordinates.
     * @return the turret pivot's pose in field coordinates, with the robot's heading.
     */
    public Pose2D robotPoseToTurretPose(Pose2D robotPoseRelField) {
        // Step 1. Get the robot's heading on the field.
        // TODO implement step G-1
        double heading = 0;

        // Step 2. Rotate the offset from robot coordinates into field coordinates.
        // The offset is measured on the robot (X forward, Y left), so which way it points on the
        // field depends on which way the robot is facing.
        // TODO implement step G-2
        double offsetXRelField = 0;
        double offsetYRelField = 0;

        // Step 3. Add the field offset to the robot's position to get the turret's position.
        // TODO implement step G-3
        double turretXRelField = 0;
        double turretYRelField = 0;

        // Step 4. Build the turret's pose, keeping the robot's heading.
        // TODO implement step G-4
        return robotPoseRelField;
    }

    /**
     * Set the turret power for one step of turning the turret to face a point on the field.
     * <p>
     * This does not wait; call it every loop. Does nothing if the robot has no turret.
     * Works out the field heading from the turret's pivot (not the robot's center) to the point,
     * then calls {@link #turnTowardsRelField}.
     *
     * @param targetPoseRelField the point to face, in field coordinates; only its X and Y are
     *                           used, its heading is ignored.
     * @param robotPoseRelField  the robot's pose in field coordinates.
     */
    public void aimTurretAtRelField(Pose2D targetPoseRelField, Pose2D robotPoseRelField) {
        // Step 1. Get where the turret's pivot is on the field.
        Pose2D turretPoseRelField = robotPoseToTurretPose(robotPoseRelField);

        // Step 2. Get the field heading from the turret's pivot to the target point.
        // pose2DDelta gives the X and Y from the turret to the target; atan2 turns them into a heading.
        // TODO implement step E
        double targetHeadingRelField = 0;

        // Step 3. Turn the turret to face that heading.
        turnTowardsRelField(targetHeadingRelField, robotPoseRelField);
    }
}