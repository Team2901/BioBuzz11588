package org.firstinspires.ftc.teamcode.teleop;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.teamcode.hardware.Hardware;

@TeleOp
public class LauncherTest extends OpMode {
    Hardware robot = new Hardware();

    // Launcher variables
    public boolean launcherOn = false;
    public double launcherVelocity = 1500;
    public static final double LAUNCHER_VELOCITY_INCREMENT = 50;

    // Transfer variables
    public int transferState = 1; // holds transfer positions 1-3, meaning the buttons (defined later) check which one is next
    public static final int TRANSFER_TOLERANCE = 10;
    public static final double TRANSFER_POWER_MULTIPLIER = 1;
    public static final int START_1 = 0;
    public static final int CLOSE_2 = 115;
    public static final int SHOOT_3 = 1050;

    // Launcher servo variables
    public double launchServoPosition = 0;
    public static final double POLLEN_POSITION = 0;
    public static final double NECTAR_POSITION = 0.84;

    public void help() {

        telemetry.addLine("----------------------");
        telemetry.addLine("Gamepad 1");
        telemetry.addData("Left Joy", "Manually move transfer");
        telemetry.addData("Dpad up/down", "adjust launch velocity by " + LAUNCHER_VELOCITY_INCREMENT);
        telemetry.addData("RT/LT", "launcher on/off");
        telemetry.addData("RB", "transfer pos 1 & 2");
        telemetry.addData("X (□)", "transfer pos 2 & 3");
        telemetry.addData("A (х)", "pollen position"); // min
        telemetry.addData("Y (△)", "nectar position"); // max
    }

    @Override
    public void init() {
        robot.init(this.hardwareMap, telemetry);

        //This helps so if the transfer is at 1501 (for example),
        // it still moves instead of shifting to 1500 first
        robot.transfer.setTargetPositionTolerance(TRANSFER_TOLERANCE);
    }

    @Override
    public void loop() {

        // LAUNCHER

        // Adjust launcher velocity
        if (gamepad1.dpadDownWasPressed()) {
            launcherVelocity = Math.max(launcherVelocity - LAUNCHER_VELOCITY_INCREMENT, 0);
        } else if (gamepad1.dpadUpWasPressed()) {
            launcherVelocity += LAUNCHER_VELOCITY_INCREMENT;
        }

        // Turn launcher on/off
        if (gamepad1.rightTriggerWasPressed()) {
            launcherOn = true;
        } else if (gamepad1.leftTriggerWasPressed()) {
            launcherOn = false;
        }

        // Send power to the launcher
        if (launcherOn) {
            robot.launcher.setVelocity(launcherVelocity);
        } else {
            robot.launcher.setVelocity(0);
        }

        // TRANSFER

        // basically says "if at 1 and button is pressed, go to 2. If not at 1 and button is pressed, go to 1"
        if (gamepad1.xWasPressed()) { // equal to the square buttons on new controller
            if (transferState == 1) {
                transferState = 2;
                moveTransfer(CLOSE_2);
            } else {
                transferState = 1;
                moveTransfer(START_1);
            }
        }
        // basically says "if at 2 and button is pressed, go to 3. If not at 2 and button is pressed, go to 2"
        else if (gamepad1.rightBumperWasPressed()) {
            if (transferState == 2) {
                transferState = 3;
                moveTransfer(SHOOT_3);
            } else if (transferState == 3) {
                transferState = 2;
                moveTransfer(CLOSE_2);
            }
        }
        // use left joystick to freely move the transfer
        else {
            double leftJoyY = -gamepad1.left_stick_y;

            if (Math.abs(leftJoyY) > 0.1) {
                robot.transfer.setPower(leftJoyY * TRANSFER_POWER_MULTIPLIER);
                robot.transfer.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            }
            // ensure power set to zero once joystick is stopped being pressed
            else if (robot.transfer.getMode() == DcMotor.RunMode.RUN_USING_ENCODER) {
                robot.transfer.setPower(0);
            }
        }

        // LAUNCHER SERVO

        // set launch to pollen vs nectar position
        if (gamepad1.aWasPressed()) {
            launchServoPosition = POLLEN_POSITION;
        } else if (gamepad1.yWasPressed()) {
            launchServoPosition = NECTAR_POSITION;
        }
        robot.launchServo.setPosition(launchServoPosition);

        // Telemetry
        telemetry.addData("Target launcher Velocity: ", launcherVelocity);
        telemetry.addData("Current launcher Velocity: ", robot.launcher.getVelocity());
        telemetry.addData("transfer current pos", robot.transfer.getCurrentPosition());
        telemetry.addData("transfer target pos", robot.transfer.getTargetPosition());
        telemetry.addData("transfer busy", robot.transfer.isBusy());
        telemetry.addData("Launch Servo Position", launchServoPosition);
        telemetry.addData("Launch Servo Mode" , launchServoPosition == POLLEN_POSITION ? "pollen" : "nectar");

        help();
        telemetry.update();
    }

    //every transfer state calls the same code to set target position,
    //then the mode, then the power
    public void moveTransfer(int target){
        robot.transfer.setTargetPosition(target);
        robot.transfer.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        robot.transfer.setPower(TRANSFER_POWER_MULTIPLIER);
    }
}