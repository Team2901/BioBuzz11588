
package org.firstinspires.ftc.teamcode.outreach;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.AnalogInput;

@TeleOp(name = "OutreachClawbot2", group = "OutreachBot")
public class OutreachClawbot2 extends OpMode {

    OutreachBotHardware robot = new OutreachBotHardware();

    public double leftPower = 0;
    public double rightPower = 0;
    public double armMovePower = 0;
    public static final double ARM_POWER = 0.3; //adjust if needed

    /*
     * TODO run TestPotentiometer and fill in correct values
     * if ARM_UP_VOLTAGE is less that ARM_DOWN_VOLTAGE, reverse the comparisons in lops
     * ie: armVoltage < OutreachBotHardware.ARM_UP_VOLTAGE -> armVoltage > OutreachBotHardware.ARM_UP_VOLTAGE
     */
    public static final double ARM_UP_VOLTAGE = 1.0;
    public static final double ARM_DOWN_VOLTAGE = 0.2;

    // TODO this belongs in the OutreachBotHardware, putting it here for now so it isn't a requirement to run the other clawbot teleop.
    public AnalogInput potentiometer = null;

    @Override
    public void init() {
        robot.init(hardwareMap);
        potentiometer = hardwareMap.analogInput.get("potentiometer");
    }

    @Override
    public void loop() {

        leftPower = -gamepad1.left_stick_y + (gamepad1.right_stick_x / OutreachBotHardware.TURN_WEIGHT);
        rightPower = -gamepad1.left_stick_y - (gamepad1.right_stick_x / OutreachBotHardware.TURN_WEIGHT);;

        leftPower = Math.max(-1, Math.min(1, leftPower));
        rightPower = Math.max(-1, Math.min(1, rightPower));

        robot.leftDrive.setPower(leftPower);
        robot.rightDrive.setPower(rightPower);

        if (gamepad1.left_bumper) {
            robot.claw.setPosition(OutreachBotHardware.CLAW_OPEN_POSITION);
        }

        if (gamepad1.right_bumper) {
            robot.claw.setPosition(OutreachBotHardware.CLAW_CLOSED_POSITION);
        }

        double armVoltage = potentiometer.getVoltage();

        //the arm only moves while a dpad button is held, and only if it has not reached that direction's limit
        if (gamepad1.dpad_up && armVoltage < ARM_UP_VOLTAGE) {
            armMovePower = ARM_POWER; //positive power moves the arm up, raising the voltage
        } else if (gamepad1.dpad_down && armVoltage > ARM_DOWN_VOLTAGE) {
            armMovePower = -ARM_POWER; //negative power moves the arm down, lowering the voltage
        } else {
            armMovePower = 0;
        }

        robot.setArmPower(armMovePower);

        telemetry.addData("Left Power", leftPower);
        telemetry.addData("Right Power", rightPower);
        telemetry.addData("Arm Voltage", armVoltage);
        telemetry.addData("Arm Power", armMovePower);
        telemetry.addData("Claw Position", robot.claw.getPosition());
        telemetry.addLine("Left stick Y = forward/backward");
        telemetry.addLine("Right stick X = turn left/right");
        telemetry.addLine("LB = open claw");
        telemetry.addLine("RB = close claw");
        telemetry.addLine("Hold dpad up/down = move arm");
        telemetry.update();
    }
}
