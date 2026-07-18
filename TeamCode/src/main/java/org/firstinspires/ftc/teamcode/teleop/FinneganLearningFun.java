package org.firstinspires.ftc.teamcode.teleop;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.robotcore.internal.collections.EvictingBlockingQueue;

@TeleOp()
public class FinneganLearningFun extends OpMode {
    @Override
    public void init(){
        telemetry.addData("Hello", "World");
    }
    @Override
    public void loop(){
        if(gamepad1.a) {
            gamepad1.rumble(20);
        }
    }
}
