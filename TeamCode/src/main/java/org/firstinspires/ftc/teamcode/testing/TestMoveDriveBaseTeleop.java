package org.firstinspires.ftc.teamcode.testing;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

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

        double leftJoyY = -gamepad1.left_stick_y;
        double leftJoyX = gamepad1.left_stick_x;
        double rightJoyX = gamepad1.right_stick_x;

        double robotAngle  = robot.getAngle() + 90; // TODO - robot angle should match how robot is initially on field
        robot.movePowerRelField(leftJoyX, leftJoyY, -rightJoyX, robotAngle);
    }
}
