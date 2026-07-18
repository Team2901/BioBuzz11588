package org.firstinspires.ftc.teamcode.autonomous;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;

import org.firstinspires.ftc.teamcode.hardware.Hardware;
public abstract class AbstractAutonomous extends LinearOpMode{
    public Hardware robot = new Hardware();

    // setup RED and BLUE alliances
    public enum Alliance {
        RED,
        BLUE
    }
    Alliance alliance = Alliance.RED;

    // method to get alliance from the gamepad and return it
    public Alliance askAlliance(){
        while(!isStarted()&&!isStopRequested()){
            if (gamepad1.x) {
                alliance = Alliance.BLUE;
            }
            if (gamepad1.b) {
                alliance = Alliance.RED;
            }
            telemetry.addLine("Select Alliance:");
            telemetry.addLine("X = BLUE");
            telemetry.addLine("B = RED");
            telemetry.addData("Current Alliance", alliance);
            telemetry.update();
        }
        return alliance;
    }

    // move() method will not return until it reaches its destination
    public void move(double yInches, double xInches) {

        // Calculate the target ticks needed to reach the destination and set the motors to
        // run using encoders and set the power to 0.5 so the robot moves to the target.
        // Cautionary note: this call will reset the encoders to 0.
        robot.move(yInches,xInches);

        // although the motor controller will stop the robot, we want to see the progress
        // so we loop until it reaches its destination
        while (opModeIsActive() && (robot.isDriveBusy())) {
            telemetryLog(robot.frontLeft);
            telemetryLog(robot.frontRight);
            telemetryLog(robot.backLeft);
            telemetryLog(robot.backRight);
        }

        // Turn off the motors after the robot reaches its destination so it
        // doesn't try maintain its position.
        robot.stop();

        // Return the robot to RUN_USING_ENCODER
        robot.frontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        robot.frontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        robot.backLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        robot.backRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }


    // telemetry
    private void telemetryLog(DcMotorEx dcMotorEx) {
        telemetry.addData("angle", robot.getAngle());
        telemetry.addData("PIDFCoefficients", dcMotorEx.getPIDFCoefficients(DcMotor.RunMode.RUN_TO_POSITION));
        telemetry.addData("Target Position", dcMotorEx.getTargetPosition());
        telemetry.addData("Current Position", dcMotorEx.getCurrentPosition());
        telemetry.addData("Target Position - fL", robot.frontLeft.getTargetPosition());
        telemetry.addData("Target Position - fR", robot.frontRight.getTargetPosition());
        telemetry.addData("Target Position - bL", robot.backLeft.getTargetPosition());
        telemetry.addData("Target Position - bR", robot.backRight.getTargetPosition());
        telemetry.addData("Current Position - fL", robot.frontLeft.getCurrentPosition());
        telemetry.addData("Current Position - fR", robot.frontRight.getCurrentPosition());
        telemetry.addData("Current Position - bL", robot.backLeft.getCurrentPosition());
        telemetry.addData("Current Position - bR", robot.backRight.getCurrentPosition());
        telemetry.update();
    }

    public void turnToAngle(double turnAngle) {

        double turnPower = robot.getTurnToAngleSpeed(turnAngle);

        // turn the robot until it reaches the desired angle
        while (opModeIsActive() && turnPower != 0) {
            robot.frontLeft.setPower(-turnPower);
            robot.frontRight.setPower(turnPower);
            robot.backLeft.setPower(-turnPower);
            robot.backRight.setPower(turnPower);

            turnPower = robot.getTurnToAngleSpeed(turnAngle);
            telemetryLog(robot.frontLeft);
        }
        robot.frontLeft.setPower(0);
        robot.frontRight.setPower(0);
        robot.backRight.setPower(0);
        robot.backLeft.setPower(0);
    }

    // turn the robot by an angle
    public void turnRelative(double relativeAngle) {
        double targetAngle = (robot.getAngle() + relativeAngle);
        turnToAngle(targetAngle);
    }
}