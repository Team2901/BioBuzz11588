package org.firstinspires.ftc.teamcode.hardware;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.bylazar.telemetry.PanelsTelemetry;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.Limelight3A;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;
import org.firstinspires.ftc.teamcode.utilities.ConfigUtilities;
@Configurable
public class Hardware {

    public DcMotorEx frontLeft;
    public DcMotorEx backLeft;
    public DcMotorEx frontRight;
    public DcMotorEx backRight;
    public DcMotorEx intake;
    Limelight3A limelight3A;
    //RevHub orientation necessary for an usage of IMU
    RevHubOrientationOnRobot.UsbFacingDirection usbFacingDirection;
    RevHubOrientationOnRobot.LogoFacingDirection logoDirection;

    public IMU imu;
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

        limelight3A = hardwareMap.get(Limelight3A.class, "limelight");
        limelight3A.pipelineSwitch(0);
        limelight3A.start();
        //initializes necessary motors
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

        frontLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        frontRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        backLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        backRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        intake.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        frontLeft.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
        frontRight.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
        backLeft.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
        backRight.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
        intake.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);

        frontLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        intake.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
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

        frontLeft.setPower(0);
        frontRight.setPower(0);
        backLeft.setPower(0);
        backRight.setPower(0);
        intake.setPower(0);

        /*Sets orientation: If something like field oriented driving or anything IMU
        * dependent isn't working check if this matches positions on the robot IRL */
        logoDirection = RevHubOrientationOnRobot.LogoFacingDirection.RIGHT;
        usbFacingDirection = RevHubOrientationOnRobot.UsbFacingDirection.UP;

         if (configurationName.equals("coachbot 2901 24-25")) {
            // Use the new RevHubOrientationOnRobot classes to describe how the control hub is mounted on the robot.
            // For the coach bot its mounted Backward / usb cable on the right (as seen from back of robot)
            // Doc: https://github.com/FIRST-Tech-Challenge/FtcRobotController/wiki/Universal-IMU-Interface
            logoDirection = RevHubOrientationOnRobot.LogoFacingDirection.FORWARD;
            usbFacingDirection  = RevHubOrientationOnRobot.UsbFacingDirection.RIGHT;
        }

        // Our Control Hub has the new IMU chip (BHI260AP). Use the new generic IMU class when
        // requesting a reference to the IMU hardware. What chip you have can be determined by
        // using "program and manage" tab on dr iver station, then "manage" on the hamburger menu.
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

    public void stop() {
        frontLeft.setPower(0);
        frontRight.setPower(0);
        backLeft.setPower(0);
        backRight.setPower(0);
    }
}
