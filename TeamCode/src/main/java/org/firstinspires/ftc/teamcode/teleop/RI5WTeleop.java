package org.firstinspires.ftc.teamcode.teleop;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;
import org.firstinspires.ftc.teamcode.hardware.Hardware;

//// Test Autonomous
/// reverse launchServo

@TeleOp(name = "RI5WTeleop") // field oriented driving
public class RI5WTeleop extends OpMode {
    Hardware robot = new Hardware();
    private Limelight3A limelight3A;
    boolean reversed = false;
    double turningPower;
    boolean launcherOn = false;
    boolean intakeOn = false;
    public int TRANSFER_POS_1 = 0;
    public int TRANSFER_POS_2 = 250;
    public int TRANSFER_POS_3 = 1500;
    public int TRANSFER_TOLERANCE = 10;
    public int transferState = 1;
    public int launcherVelocityIncrement = 25;
    public double transferSpeed = 0.85;
    //transferState holds transfer positions 1-3,
    //meaning the buttons (defined later) check which one is next

    //telemetry
    public void help() {

        //telemetry.addData("position ", String.format("x=%.2f, y=%.2f, angle=%.2f", follower.getPose().getX(), follower.getPose().getY(), Math.toDegrees(follower.getPose().getHeading())));
        telemetry.addLine("----------------------");
        telemetry.addLine("Gamepad 1");
        telemetry.addData("Left Joy", "Move drive base");
        telemetry.addData("Right Joy (X only)", "Turn drive base");
        telemetry.addData("RT", "launcher");
        telemetry.addData("LT", "intake");
        telemetry.addData("B", "reverse intake direction");
        telemetry.addData("RB", "transfer pos 1 & 2");
        telemetry.addData("X", "transfer pos 2 & 3");
        telemetry.addData("Y", "Increases Velocity");
        telemetry.addData("A", "Decreases Velocity");
    }

    @Override
    public void init() {
        robot.init(this.hardwareMap, telemetry);

        //This helps so if the transfer is at 1501 (for example),
        // it still moves instead of shifting to 1500 first
        robot.transfer.setTargetPositionTolerance(TRANSFER_TOLERANCE);

        //initialize camera or throw an error if it fails.
        try{
            limelight3A = hardwareMap.get(Limelight3A.class, "limelight");
            limelight3A.pipelineSwitch(0);
        } catch (IllegalArgumentException e){
            System.out.println("Warning: No Limelight found.");
        }
    }

    //start limelight camera if it is initialized
    @Override
    public void start(){
        if(limelight3A != null) limelight3A.start();
    }

        @Override
    public void loop() {
        //DRIVEBASE
        //update joysticks and robot.speed
        double joyStickAngle = Math.atan2(-gamepad1.left_stick_y, gamepad1.left_stick_x) - Math.toRadians(90);
        double joyStickMagnitude = Math.hypot(-gamepad1.left_stick_y, gamepad1.left_stick_x);
        double moveAngle = joyStickAngle-robot.imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.RADIANS);
        double y = robot.speed * joyStickMagnitude*Math.sin(moveAngle);
        double x = robot.speed * joyStickMagnitude*Math.cos(moveAngle);

        YawPitchRollAngles orientation = robot.getOrientation();

        if(limelight3A != null) limelight3A.updateRobotOrientation(orientation.getYaw(AngleUnit.DEGREES));

        //update turning power based on right stick x
        if(gamepad1.right_stick_x != 0){
            turningPower = .75 * gamepad1.right_stick_x * robot.speed;
        }
        else {
            turningPower = 0;
        }

       if (gamepad1.dpadUpWasPressed()) {
            robot.imu.resetYaw();
        }

        //LAUNCHER
        // If the launcher is on and the button is pressed, turn launcher off.
        //if launcher isn't on, turn launcher on.
        if (gamepad1.rightTriggerWasPressed()) {
            launcherOn = !launcherOn;
        }
        if (launcherOn) {
            robot.launcher.setVelocity(robot.overrideVelocity);
        }
        else {
            robot.launcher.setVelocity(0);
        }
        if(gamepad1.yWasPressed()){
            robot.overrideVelocity+=launcherVelocityIncrement;
        }
        if(gamepad1.aWasPressed()){
            robot.overrideVelocity-=launcherVelocityIncrement;
        }


        //INTAKE
        // If the intake is on and the button is pressed, turn intake off.
        //if intake isn't on, turn intake on.
        if (gamepad1.leftTriggerWasPressed()) {
            intakeOn = !intakeOn;
        }

        if (intakeOn) {
            robot.intake.setPower(0.65);
        } else {
            robot.intake.setPower(0);
        }

        //changing direction the intake spins,
        // to either collect pollen or release pollen
        if (gamepad1.bWasPressed()){
            if (reversed){
                robot.intake.setDirection(DcMotorSimple.Direction.FORWARD);
            } else {
                robot.intake.setDirection(DcMotorSimple.Direction.REVERSE);
            }
            reversed = !reversed;
        }


        //TRANSFER
        //basically says "if at 1 and button is pressed, go to 2.
        // If not at 1 and button is pressed, go to 1"
        if (gamepad1.xWasPressed()) {
            if (transferState == 1) {
                transferState = 2;
                moveTransfer(TRANSFER_POS_2);
            } else {
                transferState = 1;
                moveTransfer(TRANSFER_POS_1);
            }
        }


        //basically says "if at 2 and button is pressed, go to 3.
        // If not at 2 and button is pressed, go to 2"
        if (gamepad1.rightBumperWasPressed()) {
            if (transferState == 2) {
                transferState = 3;
                moveTransfer(TRANSFER_POS_3);
            } else if (transferState == 3) {
                transferState = 2;
                moveTransfer(TRANSFER_POS_2);
            }
        }

            /* if(intakeServoOn) {
                robot.intakeServoR.setPower(1);
                robot.intakeServoL.setPower(-1);
            } else {
                robot.intakeServoR.setPower(0);
                robot.intakeServoL.setPower(0);
            }

            if (gamepad1.xWasPressed())
                limelight3A.start();
         */

        //update the wheels' velocities
        robot.frontLeft.setVelocity(x - y + turningPower);
        robot.frontRight.setVelocity(x + y - turningPower);
        robot.backLeft.setVelocity(x + y + turningPower);
        robot.backRight.setVelocity(x - y - turningPower);
        LLResult llResult = limelight3A!=null? limelight3A.getLatestResult():null;

        //telemetry
        if (llResult != null && llResult.isValid()){
            Pose3D botPose = llResult.getBotpose_MT2();
            //double distance = getDistanceFromTag(llResult.getTa());
            //telemetry.addData("Calculated Distance", distance);
            telemetry.addData("Target X", llResult.getTx());
            telemetry.addData("Target Area", llResult.getTa());
            telemetry.addData("BotPose", botPose.toString());
        }
        telemetry.addData("Current Velocity: ", robot.overrideVelocity);
        telemetry.addData("imu Angle", Math.toDegrees(robot.imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.RADIANS)));
        telemetry.addData("Joystick Angle", Math.toDegrees(joyStickAngle));
        telemetry.addData("Move Angle", Math.toDegrees(moveAngle));
        telemetry.addData("turningPower", turningPower);
        telemetry.addData("transfer current pos", robot.transfer.getCurrentPosition());
        telemetry.addData("transfer target pos", robot.transfer.getTargetPosition());
        telemetry.addData("transfer busy", robot.transfer.isBusy());
        help();
        telemetry.update();
    }

    //every transfer state calls the same code to set target position,
    //then the mode, then the power
    public void moveTransfer(int target){
        robot.transfer.setTargetPosition(target);
        robot.transfer.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        robot.transfer.setPower(transferSpeed); //get up to transferSpeed
    }


    //only works when facing head on
    /*public double getDistanceFromTag(double ta){
        double scale = 29759.3774;
        double distance = (scale /ta);
        distance = Math.sqrt(distance);
        return distance;
        */
}