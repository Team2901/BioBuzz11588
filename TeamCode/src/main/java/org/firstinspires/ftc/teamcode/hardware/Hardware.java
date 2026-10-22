package org.firstinspires.ftc.teamcode.hardware;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcontroller.external.samples.SensorGoBildaPinpoint;
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
    public static final double SLOWDOWN_DISTANCE = 2 *12; // start slowing down when 2ft from target position


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
    public double getAngle(){
        YawPitchRollAngles angles = imu.getRobotYawPitchRollAngles();
        return AngleUnit.normalizeDegrees(angles.getYaw(AngleUnit.DEGREES));
    }
    public void init(HardwareMap hardwareMap, Telemetry _telemetry){
        telemetry = _telemetry;

        //initializes necessary motors
        try{
            limelight3A = hardwareMap.get(Limelight3A.class, "limelight");
            limelight3A.pipelineSwitch(0);
            limelight3A.start();
        }catch (IllegalArgumentException e){
            limelight3A = null;
            telemetry.addLine("Can't find limelight3A: making a mock");
        }
        try{
            frontLeft = hardwareMap.get(DcMotorEx.class, "frontLeft");
        }catch(IllegalArgumentException e){
            frontLeft = new MockDcMotor();
            telemetry.addLine("Can't find frontLeft: making a mock");
        }
        try{
            frontRight = hardwareMap.get(DcMotorEx.class, "frontRight");
        }catch(IllegalArgumentException e){
            frontRight = new MockDcMotor();
            telemetry.addLine("Can't find frontRight: making a mock");
        }
        try{
            backLeft = hardwareMap.get(DcMotorEx.class, "backLeft");
        }catch(IllegalArgumentException e){
            backLeft = new MockDcMotor();
            telemetry.addLine("Can't find backLeft: making a mock");
        }
        try{
            backRight = hardwareMap.get(DcMotorEx.class, "backRight");
        }catch(IllegalArgumentException e){
            backRight = new MockDcMotor();
            telemetry.addLine("Can't find backRight: making a mock");
        }

        try{
            intake = hardwareMap.get(DcMotorEx.class, "intake");
        }catch(IllegalArgumentException e){
            intake = new MockDcMotor();
            telemetry.addLine("Can't find intake: making a mock");
        }

        try{
            launcher = hardwareMap.get(DcMotorEx.class, "launcher");
        }catch(IllegalArgumentException e){
            launcher = new MockDcMotor();
            telemetry.addLine("Can't find launcher: making a mock");
        }

        try{
            transfer = hardwareMap.get(DcMotorEx.class, "transfer");
        }catch(IllegalArgumentException e){
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
            usbFacingDirection  = RevHubOrientationOnRobot.UsbFacingDirection.RIGHT;
        }

        initImu(hardwareMap);
        initPinpoint(hardwareMap);
    }

    private void initImu(HardwareMap hardwareMap){
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

        if(success && (telemetry != null)){
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
        pinpoint.setEncoderDirections(GoBildaPinpointDriver.EncoderDirection.FORWARD,
                GoBildaPinpointDriver.EncoderDirection.FORWARD);

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
     * TODO: Implement a field relative (non pending) move
     *
     * moveTowardsRelField - determine a power that will move the robot from where it currently
     * is to the target position.
     *
     * 1. Determine the how far the robot currently is (B) from the taget position yielding (A-B)
     *
     * 2. Take the value (A-B) which is in field relative coordinates and
     *    determine a power level that will move the robot in that direction.
     *
     * 3. Apply a max threshold so the power never exceeds the motor max power of 1.0
     * 4. Apply a min threshold so the power is never below the stall power.
     *
     * 5. When the robot is within 2 ft of the target, start scaling the power down
     *    from the max to the min threshold.
     *
     */
    public void moveTowardsRelField(Pose2D targetPoseRelField) {
        // 1. Get the position (aka pose) of the robot using the pinpoint object
        // 2. Determine the difference between where the robot is on the field and the target position
        // 3. Calculate the power in the field X and Y direction need to get from here to there.
        // 4. Calculate the power needed to change the heading
        // 5. Convert the field relative values to robot relative values.
        Pose2D currentPoseRelField = pinpoint.getPosition();
        Pose2D poseDeltaRelField = pose2DDelta(targetPoseRelField, currentPoseRelField);
        double xPowerRelField = getError(poseDeltaRelField.getX(DistanceUnit.INCH), MAX_POWER/SLOWDOWN_DISTANCE, MAX_POWER, MIN_STALL_POWER);
        double yPowerRelField = getError(poseDeltaRelField.getY(DistanceUnit.INCH), MAX_POWER/SLOWDOWN_DISTANCE, MAX_POWER, MIN_STALL_POWER);
        double counterClockwisePower = getError(poseDeltaRelField.getHeading(AngleUnit.DEGREES), 1.0/30, 1, 0.01);
        movePowerRelField(xPowerRelField, yPowerRelField, counterClockwisePower, currentPoseRelField.getHeading(AngleUnit.DEGREES));
    }

    /**
     * TODO: implement helper method
     *
     * pose2DDelta - Returns the pose A-B.
     *
     * Given a pose A what x,y, and heading changes are needed to reach pos B.
     */
    public Pose2D pose2DDelta(Pose2D poseA, Pose2D poseB) {
        // 1. Create a new pose object to return
        Pose2D delta = new Pose2D(DistanceUnit.INCH,0,0,AngleUnit.DEGREES, 0);
        // 3. Set the X value of the new pose object to A-B X value.
        // 4. Set the Y value of the new pose object to A-B Y value.
        // 5. Set the Heading to new heading of A-B values. use helper method angleDiff
        return delta;
    }

    /**
     * TODO: implement helper method
     *
     * getError - general purpose error function using a trapezoidal algorithm.
     *
     *  Example  #1: Control the robot speed in the X or Y direction as the robot approaches a target position.
     *  For example if the robot should go full power until it is within 2ft of the target and then
     *  slow down in a linear fashion as it approaches the target.
     *  call this function with parameters
     *     value: distance to target (in inches)
     *     errorScale: 1.0 (max power) / 24 (inches)
     *     maxThreshold: 1.0 (max power)
     *     minThreshold: 0.01 (min power aka stall speed)
     *
     *  Example #2: control the power used to change the robot heading as it pivots to a target angle.
     *  For example given a target angle the robot should turn using full power until it is within
     *  30 degrees of the target angle and then slow down in a linear fashion as it approaches the
     *  target angle. Call this function with parameters
     *     value: taget angle (in degrees)
     *     errorScale: 1.0 (max power) / 30 (degrees)
     *     maxThreshold: 1.0 (max power)
     *     minThreshold: 0.01 (mon pwer aka stall speed)
     *
     */
    public double getError(double value, double errorScaler, double maxThreshold, double minThreshold) {
        // 1. scale the input value by the errorScaler
        // 2. clip the result to the max
        // 3. clip the result to the min
        double result = 0;
        return result;
    }

    /**
     * Given a target position on the field return the robot X,Y power needed to move toward the target position.
     *
     * @param xPowerRelField
     * @param yPowerRelField
     * @param counterClockwisePower
     * @param robotHeadingRelField
     */
    public void movePowerRelField(double xPowerRelField, double yPowerRelField, double counterClockwisePower, double robotHeadingRelField) {

        // 1. Convert from Field coordinate system to robot coordinate system.
        double[] xyPowerRelRobot = translatePowerToRelRobot(xPowerRelField, yPowerRelField, robotHeadingRelField);

        // 2. Move that robot in that direction.
        movePowerRelRobot(xyPowerRelRobot[0], xyPowerRelRobot[1], counterClockwisePower);
    }

    /**
     * Return Robot power in the X and Y direction given as input Field relative power in the X and Y direction
     * and heading
     *
     * Given as input the X and Y power in terms of the field (aka I want to move full speed in the field's X direction)
     * Field positive X direction is always away from the audience.
     * Field Positive Y direction is always toward the BLUE side.
     *
     * Transform this into the power to apply in the robots X and Y direction.
     * If the robot is facing the audience the positive X is
     * If the robot is facing a
     *
     * @param xDiffRelField    power to apply in the Field relative X direction.
     * @param yDiffRelField    power to apply in the Field relative Y direction.
     * @param robotHeadingRelField robot heading in Field relative coordinate system
     * @return
     */
    public double[] translatePowerToRelRobot(double xDiffRelField, double yDiffRelField, double robotHeadingRelField) {
        // Step 1
        // We currently have a power to apply in the X direction and a power to apply in the Y direction.
        // Get the power to apply along the hypotenuse of the right triangle formed by X and Y values.
        // Hypotenuse equation. H = squareRoot(x**2 + y**2) which Math.hypot does for you.
        double xyMag = 0; // TODO: update

        // Step 2
        // The angle we have is the Target heading in the field coordinate system.
        // However the robot my already be at some angle already. May even be at the
        // target heading already..
        //
        // First, determine the target angle in field coordinate system using the X and Y values passed in
        double xyHeadingRelField = 0; // TODO: update

        // Step 3
        // Next, calculate the difference between the target angle and the robot's current heading in field coordindate system.
        double xyHeadingRelRobot = 0; // TODO: udpate

        // Step 4
        // Next, Determine how much of the power should be attributed to the Robot's X and Y directions
        // To move forward on the field 2 ft, the robot may need to
        //    move 2ft forward if it is facing forward
        //    move -2ft forward if it is facing backward
        //    strafe +/- 2ft if it is turned 90 degrees from forward
        double xDiffRelRobot = 0; // update
        double yDiffRelRobot = 0; // update

        return new double[]{xDiffRelRobot, yDiffRelRobot};
    }

    public void movePowerRelRobot(double xPowerRelRobot, double yPowerRelRobot, double counterClockwisePower) {
        // xPowerRelRobot = "forward", yPowerRelRobot = "left"
        // Step 1. calculate the power needed for each motor

        // Step 2.
        // If any power is outside -1 to 1, scale all four down by the same amount
        // so the robot still moves in the same direction.

        // Step 3.
        // Set power on all 4 motors.

    }

    public double angleDiff(double targetHeadingRelField, double robotHeadingRelField) {
        return AngleUnit.normalizeDegrees(targetHeadingRelField - robotHeadingRelField);
    }


    //Calculates velocity motors should be set to based on distance from desired angle. This should be
    //inside a loop so it is called constantly updating velocity as it approaches the desired angle.
    // TODO: make work for both power and velocity
    public Double getTurnToAngleSpeed(Double turnAngle) {

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
    public YawPitchRollAngles getOrientation(){
        return imu.getRobotYawPitchRollAngles();
    }
    // Checks robot is currently moving
    public boolean isDriveBusy(){
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
        double tps= LAUNCHER_POWER_FIT_2 * Math.pow(distance, 2) + LAUNCHER_POWER_FIT_1 * distance + LAUNCHER_POWER_FIT_0;
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
