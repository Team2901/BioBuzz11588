package org.firstinspires.ftc.teamcode.teleop;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;
import org.firstinspires.ftc.teamcode.hardware.Hardware;

@TeleOp(name = "TeleopTest") // field oriented driving
public class TeleopTest extends OpMode {
    Hardware robot = new Hardware();
    private Limelight3A limelight3A;
    //boolean reversed = false;
    double turningPower;
    boolean launcherOn = false;
    //boolean intakeOn = false;
    //boolean servoForPollen = true;
    public int START_1 = 0;
    public int CLOSE_2 = 115; //decreased by 50
    public int SHOOT_3 = 1050;
    public int TRANSFER_TOLERANCE = 10;
    public int transferState = 1;
    public double launchServoPosition = 0;
    public double servoIncrement = 0.05;

    public double pollenPos = 0;
    public double nectarPos = 0.84;
    //public int launcherVelocityIncrement = 25;
    //transferState holds transfer positions 1-3,
    //meaning the buttons (defined later) check which one is next

    //telemetry
    public void help() {

        //telemetry.addData("position ", String.format("x=%.2f, y=%.2f, angle=%.2f", follower.getPose().getX(), follower.getPose().getY(), Math.toDegrees(follower.getPose().getHeading())));
        telemetry.addLine("----------------------");
        telemetry.addLine("Gamepad 1");
        telemetry.addData("Left Joy", "Move drive base");
        telemetry.addData("Right Joy (X only)", "Turn drive base");
        telemetry.addData("Dpad Up", "reset pinpoint");
        telemetry.addData("RT", "launcher on");
        telemetry.addData("LT", "launcher off");
        telemetry.addData("RB", "transfer pos 1 & 2");
        telemetry.addData("X", "transfer pos 2 & 3");
        telemetry.addData("a", "pollen position"); // min
        telemetry.addData("x", "nectar position"); // max
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
        //robot.update();
        Pose2D currentPose = robot.pinpoint.getPosition();

     YawPitchRollAngles orientation = robot.getOrientation();

        if(limelight3A != null) limelight3A.updateRobotOrientation(orientation.getYaw(AngleUnit.DEGREES));

        //update turning power based on right stick x
        if(gamepad1.right_stick_x != 0){
            turningPower = .75 * gamepad1.right_stick_x * robot.speedV;
        }
        else {
            turningPower = 0;
        }

        if (gamepad1.dpadUpWasPressed()) {
            robot.pinpoint.setPosition(new Pose2D(DistanceUnit.INCH, 0, 0, AngleUnit.DEGREES, 0));
        }

        //LAUNCHER
        // If the launcher is on and the button is pressed, turn launcher off.
        //if launcher isn't on, turn launcher on.

        if(gamepad1.rightTriggerWasPressed()) {
            robot.launcher.setVelocity(1500);
        }

        if (gamepad1.leftTriggerWasPressed()) {
            robot.launcher.setVelocity(0);
        }


        //TRANSFER
        //basically says "if at 1 and button is pressed, go to 2.
        // If not at 1 and button is pressed, go to 1"
        if (gamepad1.xWasPressed()) { // equal to the square buttons on new controller
            if (transferState == 1) {
                transferState = 2;
                moveTransfer(CLOSE_2);
            } else {
                transferState = 1;
                moveTransfer(START_1);
            }
        }

        //basically says "if at 2 and button is pressed, go to 3.
        // If not at 2 and button is pressed, go to 2"
        if (gamepad1.rightBumperWasPressed()) {
            if (transferState == 2) {
                transferState = 3;
                moveTransfer(SHOOT_3);
            } else if (transferState == 3) {
                transferState = 2;
                moveTransfer(CLOSE_2);
            }
        }

        if (gamepad1.aWasPressed()) {
            launchServoPosition = pollenPos;
        }

        if (gamepad1.yWasPressed()) {
            launchServoPosition = nectarPos;
        }

        robot.launchServo.setPosition(launchServoPosition);

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
        double leftJoyRadius = Math.hypot(gamepad1.left_stick_x, -gamepad1.left_stick_y);
        double leftJoyAngleDegrees = Math.toDegrees(Math.atan2(-gamepad1.left_stick_y, gamepad1.left_stick_x));
        double forwardX = Math.cos(Math.toRadians(leftJoyAngleDegrees)) * leftJoyRadius;
        double leftY = Math.sin(Math.toRadians(leftJoyAngleDegrees)) * leftJoyRadius;
        robot.movePowerRelField(forwardX, leftY, -gamepad1.right_stick_x, currentPose.getHeading(AngleUnit.DEGREES));
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
        telemetry.addData("Launch Servo Position", launchServoPosition);
        telemetry.addData("Current launcher Velocity: ", robot.launcher.getVelocity());
        telemetry.addData("imu Angle", Math.toDegrees(robot.imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.RADIANS)));
        telemetry.addData("turningPower", turningPower);
        telemetry.addData("transfer current pos", robot.transfer.getCurrentPosition());
        telemetry.addData("transfer target pos", robot.transfer.getTargetPosition());
        telemetry.addData("transfer busy", robot.transfer.isBusy());
        if (robot.pollenPosition) {
            telemetry.addLine("pollen pickup ready");
        } else {
            telemetry.addLine("nectar pickup ready");
        }
        help();
        telemetry.update();
    }

    //every transfer state calls the same code to set target position,
    //then the mode, then the power
    public void moveTransfer(int target){
        robot.transfer.setTargetPosition(target);
        robot.transfer.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        robot.transfer.setPower(1);
    }


    //only works when facing head on
    /*public double getDistanceFromTag(double ta){
        double scale = 29759.3774;
        double distance = (scale /ta);
        distance = Math.sqrt(distance);
        return distance;
        */
}