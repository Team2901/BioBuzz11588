package org.firstinspires.ftc.teamcode.testing;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.teamcode.hardware.ExampleTurret;
import org.firstinspires.ftc.teamcode.hardware.Hardware;

@TeleOp(group = "test")
public class TestMoveDriveBaseTeleop extends OpMode {
    public Hardware robot = new Hardware();
    public ExampleTurret turret;

    @Override
    public void init() {
        robot.init(hardwareMap, telemetry);
        turret = new ExampleTurret(hardwareMap, telemetry);
    }

    @Override
    public void loop() {
        //robot.update();
        robot.pinpoint.update();
        Pose2D currentPose = robot.pinpoint.getPosition();

        double leftJoyY = -gamepad1.left_stick_y;
        double leftJoyX = gamepad1.left_stick_x;
        double rightJoyX = gamepad1.right_stick_x;

        double leftJoyRadius = Math.hypot(leftJoyX, leftJoyY);
        double leftJoyAngleDegrees = Math.toDegrees(Math.atan2(leftJoyY, leftJoyX));

        if (gamepad1.a) {
            robot.moveTowardsRelField(new Pose2D(DistanceUnit.INCH, 0, 0, AngleUnit.DEGREES, 0), currentPose);
        } else {
            double forwardX = Math.cos(Math.toRadians(leftJoyAngleDegrees)) * leftJoyRadius;
            double leftY = Math.sin(Math.toRadians(leftJoyAngleDegrees)) * leftJoyRadius;
            robot.movePowerRelField(forwardX, leftY, -rightJoyX, currentPose.getHeading(AngleUnit.DEGREES));
        }

        // if gamepad2 right stick x is pressed, turn the turret continually in that direction (counter clockwise or clockwise)
        // else if gamepad2 left stick is pressed, turn the turret to face that direction (relative to the field)
        // else turn the turret to face the origin of the field (0,0,0)

        double joystick2Angle = Math.toDegrees(Math.atan2(gamepad2.right_stick_y, gamepad2.right_stick_x));
        telemetry.addData("JoystickAngle", "%.2f", joystick2Angle);
        //turret.turretMotor.setPower(gamepad2.right_stick_x);
        turret.turnTowardsRelRobot(joystick2Angle);
        telemetry.addData("Turret Power", turret.turretMotor.getPower());
        telemetry.addData("Right stick X", gamepad2.right_stick_x);

        telemetry.addData("current pose", "(%.2f, %.2f, %.2f)",
            currentPose.getX(DistanceUnit.INCH),
            currentPose.getY(DistanceUnit.INCH),
            currentPose.getHeading(AngleUnit.DEGREES));

        telemetry.addData("current robot heading", turret.getHeadingRelRobot());
        telemetry.addData("current field heading", turret.getHeadingRelField(currentPose));
    }
}

