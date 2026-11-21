package org.firstinspires.ftc.teamcode.teleop;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;
import org.firstinspires.ftc.teamcode.hardware.Hardware;

public class QualTeleop extends OpMode {
    public Hardware robot = new Hardware();
    private Limelight3A limelight3A;
    boolean reversed = false;
    double turningPower;
    boolean launcherOn = false;
    boolean intakeOn = false;
    boolean servoForPollen = true;
    public int TRANSFER_POS_1 = 0;
    public int TRANSFER_POS_2 = 250;
    public int TRANSFER_POS_3 = 1500;
    public int TRANSFER_TOLERANCE = 10;
    public int transferState = 1;
    public int launcherVelocityIncrement = 25;
    //transferState holds transfer positions 1-3,
    //meaning the buttons (defined later) check which one is next

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
        Pose2D currentPose = robot.pinpoint.getPosition();

        double leftJoyY = -gamepad1.left_stick_y;
        double leftJoyX = gamepad1.left_stick_x;
        double rightJoyX = gamepad1.right_stick_x;

        double leftJoyRadius = Math.hypot(leftJoyX, leftJoyY);
        double leftJoyAngleDegrees = Math.toDegrees(Math.atan2(leftJoyY, leftJoyX));

        YawPitchRollAngles orientation = robot.getOrientation();

        if(limelight3A != null) limelight3A.updateRobotOrientation(orientation.getYaw(AngleUnit.DEGREES));

        if (gamepad1.a) {
            robot.moveTowardsRelField(new Pose2D(DistanceUnit.INCH, 0, 0, AngleUnit.DEGREES, 0), currentPose);
        } else {
            double forwardX = Math.cos(Math.toRadians(leftJoyAngleDegrees)) * leftJoyRadius;
            double leftY = Math.sin(Math.toRadians(leftJoyAngleDegrees)) * leftJoyRadius;
            robot.movePowerRelField(forwardX, leftY, -rightJoyX, currentPose.getHeading(AngleUnit.DEGREES));
        }

        if (gamepad1.dpadUpWasPressed()) {
            robot.imu.resetYaw();
        }

        if (gamepad1.dpadLeftWasPressed()) {
            robot.launchServo.setPosition(Hardware.POLLEN_POSITION);
            robot.pollenPosition = true;
        }

        if (gamepad1.dpadRightWasPressed()) {
            robot.launchServo.setPosition(Hardware.NECTAR_POSITION);
            robot.pollenPosition = false;
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
        if (gamepad1.xWasPressed()) { // equal to the square buttons on new controller
            if (transferState == 1) {
                transferState = 2;
                robot.moveTransfer(TRANSFER_POS_2);
            } else {
                transferState = 1;
                robot.moveTransfer(TRANSFER_POS_1);
            }
        }
        if(servoForPollen){
            robot.launchServo.setPosition(0.04);
        }else{
            robot.launchServo.setPosition(0.27);
        }
        if(gamepad1.dpadDownWasPressed()){
            servoForPollen = !servoForPollen;
        }
        //basically says "if at 2 and button is pressed, go to 3.
        // If not at 2 and button is pressed, go to 2"
        if (gamepad1.rightBumperWasPressed()) {
            if (transferState == 2) {
                transferState = 3;
                robot.moveTransfer(TRANSFER_POS_3);
            } else if (transferState == 3) {
                transferState = 2;
                robot.moveTransfer(TRANSFER_POS_2);
            }
        }


        // if gamepad2 right stick x is pressed, turn the turret continually in that direction (counter clockwise or clockwise)
        // else if gamepad2 left stick is pressed, turn the turret to face that direction (relative to the field)
        // else turn the turret to face the origin of the field (0,0,0)
        LLResult llResult = limelight3A!=null? limelight3A.getLatestResult():null;
        double joystick2Angle = Math.toDegrees(Math.atan2(gamepad2.right_stick_y, gamepad2.right_stick_x));
        telemetry.addData("JoystickAngle", "%.2f", joystick2Angle);
        //turret.turretMotor.setPower(gamepad2.right_stick_x);
        telemetry.addData("Right stick X", gamepad2.right_stick_x);
        telemetry.addData("current pose", "(%.2f, %.2f, %.2f)",
                currentPose.getX(DistanceUnit.INCH),
                currentPose.getY(DistanceUnit.INCH),
                currentPose.getHeading(AngleUnit.DEGREES));
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
}
