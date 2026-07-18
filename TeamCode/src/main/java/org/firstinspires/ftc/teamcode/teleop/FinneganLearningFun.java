package org.firstinspires.ftc.teamcode.teleop;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp()
public class FinneganLearningFun extends OpMode {
    @Override
    public void init(){
        telemetry.addData("Hello", "World");
    }
    @Override
    public void loop(){
        telemetry.addData("Left Stick X: ", gamepad1.left_stick_x);
        telemetry.addData("Left Stick Y: ", gamepad1.left_stick_y);
        telemetry.addData("A: ", gamepad1.a);
    }
}
