package org.firstinspires.ftc.teamcode.testing;


import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@TeleOp(name = "MotorRunToPositionTest.V1", group = "test")
public class MotorRunToPositionTest extends OpMode {
    List<Map.Entry<String, DcMotor>> dcMotorList = new ArrayList<>();
    Integer activeIndex = (0);
    double y;

    int targetPosition = 0;

    public void help() {
        telemetry.addLine("Use Dpad up/down to choose motor");
        telemetry.addLine("Use right bumper to run");
        telemetry.addLine("Left stick Y for power");
        telemetry.addData("a/y", "+/- 10");
        telemetry.addData("b/x", "+/- 100");
        telemetry.addLine("");
    }

    public void telemetry(){
        telemetry.addData("current motor", dcMotorList.get(activeIndex).getKey());
        telemetry.addData("motor power", dcMotorList.get(activeIndex).getValue().getPower());
        telemetry.addData("mode", dcMotorList.get(activeIndex).getValue().getMode());
        telemetry.addData("encoder current (ticks)", dcMotorList.get(activeIndex).getValue().getCurrentPosition());
        telemetry.addData("encoder target (ticks)", dcMotorList.get(activeIndex).getValue().getTargetPosition());
        telemetry.addData("is busy", dcMotorList.get(activeIndex).getValue().isBusy());
        telemetry.addData("velocity", ((DcMotorEx) dcMotorList.get(activeIndex).getValue()).getVelocity());
        telemetry.addData("y stick", y);
    }

    @Override
    public void init() {
        Set<Map.Entry<String, DcMotor>> dcMotorSet = this.hardwareMap.dcMotor.entrySet();
        dcMotorList.addAll(dcMotorSet);
        for (DcMotor dcMotor : this.hardwareMap.dcMotor) {
            dcMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        }
        help();
    }

    @Override
    public void loop() {

        if(gamepad1.dpadUpWasPressed()){
            dcMotorList.get(activeIndex).getValue().setPower(0);
            dcMotorList.get(activeIndex).getValue().setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            activeIndex++;
        }
        if(gamepad1.dpadDownWasPressed()){
            dcMotorList.get(activeIndex).getValue().setPower(0);
            dcMotorList.get(activeIndex).getValue().setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            activeIndex--;
        }
        if(activeIndex == dcMotorList.size()){
            activeIndex = 0;
        }
        if(activeIndex == -1){
            activeIndex = dcMotorList.size() - 1;
        }

        if (gamepad1.yWasPressed()){
            targetPosition += 10;
        }
        if (gamepad1.aWasPressed()){
            targetPosition -= 10;
        }

        if (gamepad1.bWasPressed()){
            targetPosition += 100;
        }

        if (gamepad1.xWasPressed()){
            targetPosition -= 100;
        }

        y = -gamepad1.left_stick_y;

        if (gamepad1.rightBumperWasPressed()) {
            dcMotorList.get(activeIndex).getValue().setPower(y);
            dcMotorList.get(activeIndex).getValue().setTargetPosition(targetPosition);
            dcMotorList.get(activeIndex).getValue().setMode(DcMotor.RunMode.RUN_TO_POSITION);
        }

        help();
        telemetry();
    }
}
