package org.firstinspires.ftc.teamcode.testing;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.autonomous.AbstractAutonomous;

@Autonomous (name = "testMotorDriveAuto", group = "test")
public class testMotorDriveAuto extends AbstractAutonomous {
    ElapsedTime timer = new ElapsedTime();

    @Override
    public void runOpMode() throws InterruptedException {
        robot.init(this.hardwareMap, telemetry);
        waitForStart();
        timer.reset();

    /*
        while ( timer.seconds() <= 5){

            robot.speed = 0.5;

            telemetry.addData("backLeft", robot.backLeft.getCurrentPosition());
            telemetry.addData("backRight", robot.backRight.getCurrentPosition());
            telemetry.addData("frontLeft", robot.frontLeft.getCurrentPosition());
            telemetry.addData("fontRight", robot.frontRight.getCurrentPosition());
            telemetry.update();
        }

        robot.backLeft.setPower(0);
        robot.backRight.setPower(0);
        robot.frontLeft.setPower(0);
        robot.frontRight.setPower(0);

     */

        while (timer.seconds() < 10) {
            robot.backLeft.setPower(1);
            robot.backRight.setPower(1);
            robot.frontLeft.setPower(1);
            robot.frontRight.setPower(1);
        }

        robot.backLeft.setPower(0);
        robot.backRight.setPower(0);
        robot.frontLeft.setPower(0);
        robot.frontRight.setPower(0);

        telemetry.addData("backLeft", robot.backLeft.getCurrentPosition());
        telemetry.addData("backRight", robot.backRight.getCurrentPosition());
        telemetry.addData("frontLeft", robot.frontLeft.getCurrentPosition());
        telemetry.addData("fontRight", robot.frontRight.getCurrentPosition());
        telemetry.update();

        while (opModeIsActive()) {
            idle();
        }
    }
}
