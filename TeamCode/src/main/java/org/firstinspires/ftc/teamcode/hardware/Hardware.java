package org.firstinspires.ftc.teamcode.hardware;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;
import org.firstinspires.ftc.teamcode.utilities.ConfigUtilities;

@Configurable
public class Hardware {

    //Config comments updated 10/12/26
    public DcMotorEx frontLeft; // EH - 0
    public DcMotorEx backLeft; // EH - 1
    public DcMotorEx frontRight; // CH - 0
    public DcMotorEx backRight; // CH - 1
    public DcMotorEx launcher; // CH - 2
    public DcMotorEx intake; // EH - 2
    public DcMotorEx transfer; // EH - 3
    public Servo intakeServo; // CH - 0
    public Servo launchServo; // CH - 1

    //public CRServo intakeServoR;
    //public CRServo intakeServoL;
    Limelight3A limelight3A;
    //RevHub orientation necessary for an usage of IMU
    RevHubOrientationOnRobot.UsbFacingDirection usbFacingDirection;
    RevHubOrientationOnRobot.LogoFacingDirection logoDirection;
    public static double defaultVelocity = 1500;
    public double overrideVelocity = defaultVelocity;

    private static final double pinpointXOffsetMM = 0;
    private static final double pinpointYOffsetMM = 0;

    public IMU imu;
    public GoBildaPinpointDriver pinpoint;

    public static final double MAX_POWER = 1.0;
    public static final double MIN_STALL_POWER = 0.01;
    public static final double SLOWDOWN_DISTANCE = 2 * 12; // start slowing down when 2ft from target position


    public Telemetry telemetry;
    /*Constants dependent on wheels, motors, and drive-base construction necessary for autonomous
     * methods in which we set distance we want to move in inches.
     * */
    public static final double TICKS_PER_MOTOR_REV = 537.7;
    public static final double DRIVE_GEAR_RATIO = 1.0/1.0;
    public static final double TICKS_PER_DRIVE_REV = TICKS_PER_MOTOR_REV * DRIVE_GEAR_RATIO;
    public static final double WHEEL_DIAMETER = 3.78;
    public static final double WHEEL_CIRCUMFERENCE = Math.PI * WHEEL_DIAMETER;
    public static final double TICKS_PER_INCH = TICKS_PER_DRIVE_REV / WHEEL_CIRCUMFERENCE;
    public static final int POLLEN_POSITION = 0;
    public static final int NECTAR_POSITION = 0;
    public boolean pollenPosition = true; // false means nectar position
    //Turn error tolerance
    public double TOLERANCE = 1; // degrees
    public double robotTargetAngle;
    public double robotTurnError;
    public double robotTurnPower;
    //Scalar for calculating robot velocity
    public double speed = 3000;

    public double getAngle() {
        YawPitchRollAngles angles = imu.getRobotYawPitchRollAngles();
        return AngleUnit.normalizeDegrees(angles.getYaw(AngleUnit.DEGREES));
    }

    public void init(HardwareMap hardwareMap, Telemetry _telemetry) {
        telemetry = _telemetry;

        //initializes necessary motors
        try {
            limelight3A = hardwareMap.get(Limelight3A.class, "limelight");
            limelight3A.pipelineSwitch(0);
            limelight3A.start();
        } catch (IllegalArgumentException e) {
            limelight3A = null;
            telemetry.addLine("Can't find limelight3A: making a mock");
        }
        try {
            frontLeft = hardwareMap.get(DcMotorEx.class, "frontLeft");
        } catch (IllegalArgumentException e) {
            frontLeft = new MockDcMotor();
            telemetry.addLine("Can't find frontLeft: making a mock");
        }
        try {
            frontRight = hardwareMap.get(DcMotorEx.class, "frontRight");
        } catch (IllegalArgumentException e) {
            frontRight = new MockDcMotor();
            telemetry.addLine("Can't find frontRight: making a mock");
        }
        try {
            backLeft = hardwareMap.get(DcMotorEx.class, "backLeft");
        } catch (IllegalArgumentException e) {
            backLeft = new MockDcMotor();
            telemetry.addLine("Can't find backLeft: making a mock");
        }
        try {
            backRight = hardwareMap.get(DcMotorEx.class, "backRight");
        } catch (IllegalArgumentException e) {
            backRight = new MockDcMotor();
            telemetry.addLine("Can't find backRight: making a mock");
        }

        try {
            intake = hardwareMap.get(DcMotorEx.class, "intake");
        } catch (IllegalArgumentException e) {
            intake = new MockDcMotor();
            telemetry.addLine("Can't find intake: making a mock");
        }

        try {
            launcher = hardwareMap.get(DcMotorEx.class, "launcher");
        } catch (IllegalArgumentException e) {
            launcher = new MockDcMotor();
            telemetry.addLine("Can't find launcher: making a mock");
        }

        try {
            transfer = hardwareMap.get(DcMotorEx.class, "transfer");
        } catch (IllegalArgumentException e) {
            transfer = new MockDcMotor();
            telemetry.addLine("Can't find transfer: making a mock");
        }

        try {
            intakeServo = hardwareMap.get(Servo.class, "intakeServo");
        } catch (IllegalArgumentException e) {
            intakeServo = new MockServo();
            telemetry.addLine("Can't find intakeServo: making a mock");
        }

        try {
            launchServo = hardwareMap.get(Servo.class, "launchServo");
        } catch (IllegalArgumentException e) {
            launchServo = new MockServo();
            telemetry.addLine("Can't find launchServo: making a mock");
        }

        //intakeServoR = hardwareMap.get(CRServo.class, "intakeServoR");
        //intakeServoL = hardwareMap.get(CRServo.class, "intakeServoL");

        frontLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        frontRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        backLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        backRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        intake.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        launcher.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        transfer.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        frontLeft.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
        frontRight.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
        backLeft.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
        backRight.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
        intake.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
        launcher.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
        transfer.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);

        frontLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        intake.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        launcher.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        transfer.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        /* This may not be necessary, but exists so that when using coachbot the wheels go
         * in the correct direction. The reveres wheels may need to change as the drive base
         * changes.
         */
        String configurationName = ConfigUtilities.getRobotConfigurationName();
        if (configurationName.equals("coachbot 2901 24-25")) {
            frontRight.setDirection(DcMotorSimple.Direction.REVERSE);
            backRight.setDirection(DcMotorSimple.Direction.REVERSE);
        } else {
            frontLeft.setDirection(DcMotorSimple.Direction.REVERSE);
            backLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        }

        intake.setDirection(DcMotorSimple.Direction.REVERSE);
        launcher.setDirection(DcMotorSimple.Direction.REVERSE);

        frontLeft.setPower(0);
        frontRight.setPower(0);
        backLeft.setPower(0);
        backRight.setPower(0);
        intake.setPower(0);
        launcher.setPower(0);

        if (configurationName.equals("coachbot 2901 24-25")) {
            // Use the new RevHubOrientationOnRobot classes to describe how the control hub is mounted on the robot.
            // For the coach bot its mounted Backward / usb cable on the right (as seen from back of robot)
            // Doc: https://github.com/FIRST-Tech-Challenge/FtcRobotController/wiki/Universal-IMU-Interface
            logoDirection = RevHubOrientationOnRobot.LogoFacingDirection.FORWARD;
            usbFacingDirection = RevHubOrientationOnRobot.UsbFacingDirection.RIGHT;
        }

        initImu(hardwareMap);
        //initPinpoint(hardwareMap);
    }

    private void initImu(HardwareMap hardwareMap) {
        /*Sets orientation: If something like field oriented driving or anything IMU
         * dependent isn't working check if this matches positions on the robot IRL */
        logoDirection = RevHubOrientationOnRobot.LogoFacingDirection.RIGHT;
        usbFacingDirection = RevHubOrientationOnRobot.UsbFacingDirection.UP;

        // Our Control Hub has the new IMU chip (BHI260AP). Use the new generic IMU class when
        // requesting a reference to the IMU hardware. What chip you have can be determined by
        // using "program and manage" tab on driver station, then "manage" on the hamburger menu.
        imu = hardwareMap.get(IMU.class, "imu");

        IMU.Parameters parameters;
        RevHubOrientationOnRobot orientationOnRobot = new RevHubOrientationOnRobot(logoDirection, usbFacingDirection);
        parameters = new IMU.Parameters(orientationOnRobot);
        boolean success = imu.initialize(parameters);

        if (success && (telemetry != null)) {
            telemetry.addLine("IMU initialized");
            telemetry.update();
        }
    }

    private void initPinpoint(HardwareMap hardwareMap) {

        pinpoint = hardwareMap.get(GoBildaPinpointDriver.class, "pinpoint");

        /*
         *  Set the odometry pod positions relative to the point that you want the position to be measured from.
         *
         *  The X pod offset refers to how far sideways from the tracking point the X (forward) odometry pod is.
         *  Left of the center is a positive number, right of center is a negative number.
         *
         *  The Y pod offset refers to how far forwards from the tracking point the Y (strafe) odometry pod is.
         *  Forward of center is a positive number, backwards is a negative number.
         */
        pinpoint.setOffsets(pinpointXOffsetMM, pinpointYOffsetMM, DistanceUnit.MM); //these are tuned for 3110-0002-0001 Product Insight #1

        /*
         * Set the kind of pods used by your robot. If you're using goBILDA odometry pods, select either
         * the goBILDA_SWINGARM_POD, or the goBILDA_4_BAR_POD.
         * If you're using another kind of odometry pod, uncomment setEncoderResolution and input the
         * number of ticks per unit of your odometry pod.  For example:
         *     pinpoint.setEncoderResolution(13.26291192, DistanceUnit.MM);
         */
        pinpoint.setEncoderResolution(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD);

        /*
         * Set the direction that each of the two odometry pods count. The X (forward) pod should
         * increase when you move the robot forward. And the Y (strafe) pod should increase when
         * you move the robot to the left.
         */
        pinpoint.setEncoderDirections(GoBildaPinpointDriver.EncoderDirection.FORWARD, GoBildaPinpointDriver.EncoderDirection.FORWARD);

        /*
         * Before running the robot, recalibrate the IMU. This needs to happen when the robot is stationary
         * The IMU will automatically calibrate when first powered on, but recalibrating before running
         * the robot is a good idea to ensure that the calibration is "good".
         * resetPosAndIMU will reset the position to 0,0,0 and also recalibrate the IMU.
         * This is recommended before you run your autonomous, as a bad initial calibration can cause
         * an incorrect starting value for x, y, and heading.
         */
        pinpoint.resetPosAndIMU();

        // Set the location of the robot - this should be the place you are starting the robot from
        pinpoint.setPosition(new Pose2D(DistanceUnit.INCH, 0, 0, AngleUnit.DEGREES, 0));
    }

    /**
     * Set the drive motor powers for one step of a move toward a target pose on the field.
     * <p>
     * This does not wait for the robot to arrive. It looks at where the robot is now, sets the
     * motor powers once, and returns. Call it repeatedly in a loop; each call adjusts the power
     * as the robot gets closer, and the caller decides when the robot is close enough to stop.
     * <p>
     * The power for X, Y and heading each follows the {@link #getError} profile: full power far
     * from the target, ramping down linearly once within 2 ft (or 30 degrees for heading), and
     * never below stall power.
     *
     * @param targetPoseRelField the pose to move toward, in field coordinates: X and Y position
     *                           plus the heading the robot should end up facing.
     */
    public void moveTowardsRelField(Pose2D targetPoseRelField) {
        // Step 1. Get the robot's current pose from the pinpoint.
        // Call pinpoint.update() first so the position is fresh, then pinpoint.getPosition().
        // TODO implement step 1

        // Step 2. Get the difference between the target pose and the current pose
        // Use pose2DDelta(targetPoseRelField, currentPose).
        // This is how far the robot still has to go in field X, field Y and heading.
        // TODO implement step 2

        // Step 3. Turn the X and Y differences into field relative X and Y powers
        // Call getError,
        // e.g. errorScaler 1.0 / 24 so the robot slows down within 2 ft of the target.
        // TODO implement step 3

        // Step 4. Turn the heading difference into a counter-clockwise power
        // Call getError,
        // e.g. errorScaler 1.0 / 30 so the robot slows down within 30 degrees.
        // TODO implement step 4

        // Step 5. Drive by calling movePowerRelField
        // Pass the powers from steps 3 and 4 and the robot's current heading.
        // It converts the field relative powers to robot relative ones and sets the motors.
        // TODO implement step 5
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
    public Pose2D pose2DDelta(Pose2D poseA, Pose2D poseB) {
        // Step 1. Calculate the X difference A-B.
        // Read both X values in the same unit (inches), e.g. poseA.getX(DistanceUnit.INCH).
        // TODO implement step 1

        // Step 2. Calculate the Y difference A-B the same way.
        // TODO implement step 2

        // Step 3.  Calculate the heading difference A-B
        // Use the helper method angleDiffDegrees, so the result is the shortest turn.
        // e.g. -160 - 180 -> 20 instead of -340.
        // TODO implement step 3

        // Step 4. Create the pose to return from those three values
        // Pose2D has no setters, so the values must go into the constructor (replace the zeros below).
        // TODO implement step 4
        Pose2D delta = new Pose2D(DistanceUnit.INCH, 0, 0, AngleUnit.DEGREES, 0);

        return delta;
    }

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
    public double getError(double value, double errorScaler, double maxThreshold, double minThreshold) {
        // Step 1. Scale the input value by the errorScaler.
        // TODO implement step 1
        double result = 0;

        // Step 2. Clip the result to the max.
        // TODO implement step 2

        // Step 3. Clip the result to the min.
        // TODO implement step 3

        return result;
    }

    /**
     * Drive the robot using powers given in the field's coordinate system (field-centric drive).
     * <p>
     * Field positive X is always away from the audience; field positive Y is always toward
     * the BLUE side. The robot moves in the requested field direction no matter which way it
     * is facing: the powers are converted to the robot's coordinate system with
     * {@link #translatePowerToRelRobot} and then applied with {@link #movePowerRelRobot}.
     *
     * @param xPowerRelField              power to apply in the field relative X direction, -1 to 1.
     * @param yPowerRelField              power to apply in the field relative Y direction, -1 to 1.
     * @param counterClockwisePower       power to apply to rotation, -1 to 1; positive turns counter-clockwise.
     * @param robotHeadingDegreesRelField robot heading in the field coordinate system, in degrees;
     *                                    0 faces field +X and positive is counter-clockwise.
     */
    public void movePowerRelField(double xPowerRelField, double yPowerRelField, double counterClockwisePower, double robotHeadingDegreesRelField) {
        // Step 1. Convert from Field coordinate system to robot coordinate system.
        double[] xyPowerRelRobot = translatePowerToRelRobot(xPowerRelField, yPowerRelField, robotHeadingDegreesRelField);

        // Step 2. Move that robot in that direction.
        movePowerRelRobot(xyPowerRelRobot[0], xyPowerRelRobot[1], counterClockwisePower);
    }

    /**
     * Convert a power given in the field's coordinate system into the power to apply in the
     * robot's own coordinate system, given the robot's current heading on the field.
     * <p>
     * Field positive X is always away from the audience; field positive Y is always toward
     * the BLUE side.
     *
     * @param xDiffRelField               power to apply in the field relative X direction.
     * @param yDiffRelField               power to apply in the field relative Y direction.
     * @param robotHeadingDegreesRelField robot heading in the field coordinate system, in degrees.
     * @return a two-element array {xPowerRelRobot, yPowerRelRobot}: the forward and left power
     * to pass to {@link #movePowerRelRobot}.
     */
    public double[] translatePowerToRelRobot(double xDiffRelField, double yDiffRelField, double robotHeadingDegreesRelField) {
        // Step 1. Calculate the magnitude of the xy values.
        // We currently have a power to apply in the X direction and a power to apply in the Y direction.
        // Get the power to apply along the hypotenuse of the right triangle formed by X and Y values.
        // Hypotenuse equation. H = squareRoot(x**2 + y**2) which Math.hypot does for you.
        double xyMag = 0; // TODO implement step 1

        // Step 2. Determine the target angle in field coordinate system using the X and Y values passed in.
        // The angle we have is the Target heading in the field coordinate system.
        // However the robot my already be at some angle already. May even be at the
        // target heading already..
        double xyHeadingRelField = 0; // TODO implement step 2

        // Step 3. Calculate the difference between the target angle and the robot's current heading in field coordinate system.
        double xyHeadingRelRobot = 0; // TODO implement step 3

        // Step 4. Determine how much of the power should be attributed to the Robot's X and Y directions.
        // To move forward on the field 2 ft, the robot may need to
        //    move 2ft forward if it is facing forward
        //    move -2ft forward if it is facing backward
        //    strafe +/- 2ft if it is turned 90 degrees from forward
        // TODO implement step 4
        double xDiffRelRobot = 0;
        double yDiffRelRobot = 0;

        return new double[]{xDiffRelRobot, yDiffRelRobot};
    }

    /**
     * Drive the robot using powers given in the robot's own coordinate system.
     * <p>
     * Robot X is forward and robot Y is left, regardless of which way
     * the robot faces on the field. The three inputs are combined into a power for each of
     * the 4 drive motors. If any motor power falls outside -1 to 1, all four are scaled down
     * by the same amount so the robot still moves in the requested direction.
     *
     * @param xPowerRelRobot        power to apply in the robot relative X (forward) direction, -1 to 1.
     * @param yPowerRelRobot        power to apply in the robot relative Y (left) direction, -1 to 1.
     * @param counterClockwisePower power to apply to rotation, -1 to 1; positive turns counter-clockwise.
     */
    public void movePowerRelRobot(double xPowerRelRobot, double yPowerRelRobot, double counterClockwisePower) {
        // Step 1. Calculate the power needed for each motor
        // TODO implement step 1

        // Step 2. Scale the power to remaining in [-1, 1].
        // If any power is outside -1 to 1, scale all four down by the same amount
        // so the robot still moves in the same direction.
        // TODO implement step 2

        // Step 3. Set power on all 4 motors.
        // TODO implement step 3
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
    public double angleDiffDegrees(double degreesA, double degreesB) {
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
    public double angleDiffRadians(double radiansA, double radiansB) {
        return AngleUnit.normalizeRadians(radiansA - radiansB);
    }

    /**
     * Calculates velocity motors should be set to based on distance from desired angle
     * <p>
     *  This should be inside a loop so it is called constantly updating velocity as it approaches the desired angle.
     * @param turnAngle angle to turn to
     * @return turn power
     */
    public Double getTurnToAngleSpeed(Double turnAngle) {
        // TODO: make work for both power and velocity

        if (turnAngle == null) {
            return null;
        }

        //robot.getAngle is between -180 and 180, starting at 0
        double turnPower = 0;
        double targetAngle = turnAngle;
        double startAngle = getAngle();
        double turnError = AngleUnit.normalizeDegrees(targetAngle - startAngle);
        // If within TOLERANCE (1 degree) stop movement
        if (Math.abs(turnError) <= TOLERANCE) {
            return 0.0;
        }
        //calculate turn power based on turnError divided by the scalar 90 and multiplied by the speed
        if (turnError >= 0) {
            turnPower = speed * (turnError / 90);
            if (turnPower > speed) {
                turnPower = speed;
            }
        } else if (turnError < 0) {
            turnPower = speed * (turnError / 90);
            if (turnPower < -speed) {
                turnPower = -speed;
            }
        }
        telemetry.addData("Error:", turnError);
        return turnPower;
    }

    public YawPitchRollAngles getOrientation() {
        return imu.getRobotYawPitchRollAngles();
    }

    // Checks robot is currently moving
    public boolean isDriveBusy() {
        return frontLeft.isBusy() && frontRight.isBusy() && backLeft.isBusy() && backRight.isBusy();
    }

    // Autonomously moves robot by calculating necessary encoder ticks to move to input position.
    public void move(double yInches, double xInches) {
        // Conversion of inches to ticks
        int ticksY = (int) (yInches * Hardware.TICKS_PER_INCH);
        int ticksX = (int) (xInches * (Hardware.TICKS_PER_INCH / 0.9));

        frontLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        frontRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        backLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        backRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        frontLeft.setTargetPosition(ticksY + ticksX);
        frontRight.setTargetPosition(ticksY - ticksX);
        backLeft.setTargetPosition(ticksY - ticksX);
        backRight.setTargetPosition(ticksY + ticksX);

        frontLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        frontRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        backLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        backRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        frontLeft.setPower(0.5);
        frontRight.setPower(0.5);
        backLeft.setPower(0.5);
        backRight.setPower(0.5);
    }

    // Autonomously turns to set angle
    public void turnToAngle(double turnAngle) {
        stop(); // Make sure no power is sent to out motors before changing modes
        frontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        frontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        backLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        backRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        double turnPower = getTurnToAngleSpeed(turnAngle);
        frontLeft.setVelocity(-turnPower);
        frontRight.setVelocity(turnPower);
        backLeft.setVelocity(-turnPower);
        backRight.setVelocity(turnPower);
    }

    public void setMotorSpeedFromDistance(double distance) {
        //
        double tps = LAUNCHER_POWER_FIT_2 * Math.pow(distance, 2) + LAUNCHER_POWER_FIT_1 * distance + LAUNCHER_POWER_FIT_0;
        launcher.setVelocity(tps);
    }

    double LAUNCHER_POWER_FIT_2 = 0;
    double LAUNCHER_POWER_FIT_1 = 3;
    double LAUNCHER_POWER_FIT_0 = 2;

    public void stop() {
        frontLeft.setPower(0);
        frontRight.setPower(0);
        backLeft.setPower(0);
        backRight.setPower(0);
    }
}
