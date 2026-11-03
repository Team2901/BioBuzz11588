package org.firstinspires.ftc.teamcode.testing;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.teamcode.hardware.Hardware;

@TeleOp(group = "test")
public class TestMoveDriveBaseTeleop extends OpMode {
    public Hardware robot = new Hardware();

    @Override
    public void init() {
        robot.init(hardwareMap, telemetry);
    }

    @Override
    public void loop() {
        robot.update();

        double leftJoyY = -gamepad1.left_stick_y;
        double leftJoyX = gamepad1.left_stick_x;
        double rightJoyX = gamepad1.right_stick_x;
        Pose2D currentPose = robot.pinpoint.getPosition();

        double robotAngle  = robot.getAngle() + 90; // TODO - robot angle should match how robot is initially on field
        if(gamepad1.a){
            robot.moveTowardsRelField(new Pose2D(DistanceUnit.INCH, 0, 0, AngleUnit.DEGREES, 0), currentPose);
        }else{
            robot.movePowerRelField(leftJoyX, leftJoyY, -rightJoyX, robotAngle);
        }

        telemetry.addData("current robot X", currentPose.getX(DistanceUnit.INCH));
        telemetry.addData("current robot Y", currentPose.getY(DistanceUnit.INCH));
        telemetry.addData("current robot Ang", currentPose.getHeading(AngleUnit.DEGREES));
        telemetry.update();

    }
}
