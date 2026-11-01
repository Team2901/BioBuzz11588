
package org.firstinspires.ftc.teamcode.outreach;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ElapsedTime;

@TeleOp(name = "OutreachClawbot", group = "OutreachBot")
public class OutreachClawbot extends OpMode {

//// arm does not work

    OutreachBotHardware robot = new OutreachBotHardware();

    public double leftPower = 0;
    public double rightPower = 0;
    public ElapsedTime armTimer = new ElapsedTime();
    public boolean armMoving = false;
    public double armMovePower = 0;
    public double armMoveTime = 0;
    public static final double ARM_POWER = 0.3; //adjust if needed
    public static final double ARM_UP_TIME = 3.0; //adjust if needed
    public static final double ARM_DOWN_TIME = 1.0; //adjust if needed

    @Override
    public void init() {
        robot.init(hardwareMap);
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

        // check the button press here so the "WasPressed" check clears out while the arm is moving
        boolean upPressed = gamepad1.dpadUpWasPressed();
        boolean downPressed = gamepad1.dpadDownWasPressed();
        if (!armMoving) {
            if (upPressed) {
                armMovePower = ARM_POWER; //setting positive power to motor
                armMoveTime = ARM_UP_TIME; //setting the amount of time the arm should be moving
                armMoving = true;
                armTimer.reset(); //reset timer to start counting at 0
            } else if (downPressed){
                armMovePower = -ARM_POWER; //setting negative power to motor
                armMoveTime = ARM_DOWN_TIME;
                armMoving = true;
                armTimer.reset();
            }
        }

        //in charge of checking if the arm is supposed to be moving
        if (armMoving) {
            if (armTimer.seconds() < armMoveTime) { //check whether the armMoveTime has finished
                robot.setArmPower(armMovePower); //continues running the motor
            } else {
                robot.setArmPower(0);
                armMoving = false;
            }
        }

        //an emergency stop
        if (gamepad1.a) {
            robot.setArmPower(0);
            armMoving = false;
        }

        //robot.moveArm(OutreachBotHardware.ARM_UP_POSITION);

        telemetry.addData("Left Power", leftPower);
        telemetry.addData("Right Power", rightPower);
        telemetry.addLine("Left stick Y = forward/backward");
        telemetry.addLine("Right stick X = turn left/right");
        telemetry.addLine("LB = open claw");
        telemetry.addLine("RB = close claw");
        telemetry.addData("Arm Moving", armMoving);
        telemetry.addData("Arm Timer", armTimer.seconds());
        telemetry.update();
    }
    double armTargetPosition = 10.0;
}
