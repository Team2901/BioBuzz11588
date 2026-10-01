package org.firstinspires.ftc.teamcode.teleop;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.teamcode.hardware.Hardware;

import java.util.ArrayList;

@TeleOp(name = "LimelightTeleop")
public class LimelightTeleop extends OpMode {
    Hardware robot = new Hardware();
    Limelight3A limelight3A;
    Double targetTurnAngle;
    double turningPower = 0;
    double Tx;
    double Ty;
    double Ta;

    public void init() {
        robot.init(hardwareMap, telemetry);
        IMU imu;
        limelight3A = hardwareMap.get(Limelight3A.class, "limelight");
        limelight3A.pipelineSwitch(0);
        limelight3A.start();
        imu = hardwareMap.get(IMU.class, "imu");
        RevHubOrientationOnRobot revOrientation = new RevHubOrientationOnRobot(
                RevHubOrientationOnRobot.LogoFacingDirection.UP,
                RevHubOrientationOnRobot.UsbFacingDirection.FORWARD);
        imu.initialize(new IMU.Parameters(revOrientation));
    }
    public double getDistanceFromTag(double ta){
        double scale = 29759.3774;
        double distance = (scale /ta);
        distance = Math.sqrt(distance);
        return distance;
    }
    public void loop() {
        double y = -robot.speed * gamepad1.left_stick_y;
        double x = robot.speed * gamepad1.left_stick_x;

        // Detect yellows
        if(gamepad1.a) {
            limelight3A.pipelineSwitch(0);
        }

        // Detect red
        if(gamepad1.b) {
            limelight3A.pipelineSwitch(1);
        }

        // Detect blue
        if(gamepad1.x) {
            limelight3A.pipelineSwitch(2);
        }

        Double turnToAngleSpeed = robot.getTurnToAngleSpeed(targetTurnAngle);

        if (turnToAngleSpeed != null && turnToAngleSpeed == 0) {
            targetTurnAngle = null;
            turnToAngleSpeed = null;
        }
        if (gamepad1.right_stick_x != 0) {
            turningPower = .75 * gamepad1.right_stick_x;
            targetTurnAngle = null;
        } else {
            if (turnToAngleSpeed != null) {
                turningPower = -turnToAngleSpeed;
            } else {
                turningPower = 0;
            }
        }

        robot.frontLeft.setPower(y + x + turningPower);
        robot.frontRight.setPower(y - x - turningPower);
        robot.backLeft.setPower(y - x + turningPower);
        robot.backRight.setPower(y + x - turningPower);

        LLResult llResult = limelight3A.getLatestResult();
        if (llResult != null && llResult.isValid()) {
            // int ID = llResult.getFiducialResults().get(0).getFiducialId();
            Pose3D botPose = llResult.getBotpose_MT2();
            Tx = llResult.getTx();
            Ty = llResult.getTy();
            Ta = llResult.getTa();

            //telemetry.addData("Tag ID: ", ID);
            telemetry.addData("Ball X Offset (degree): ", Tx); // How far left or right the target is (degrees)
            telemetry.addData("Ball Y Offset (degree): ", Ty); // How far up or down the target is (degrees)
            telemetry.addData("Ball Area (degree): ", Ta); // How big the target looks (0%-100% of the image)

            Pose3D limelightPose = llResult.getBotpose();
            Position llPosition = limelightPose.getPosition();

            telemetry.addData("Pose unit", llPosition.unit);
            telemetry.addData("Pose X", llPosition.x);
            telemetry.addData("Pose X", llPosition.y);
            telemetry.addData("Distance (in meters): ", getDistanceFromTag(Ta));
        }

        telemetry.addData("left stick y", gamepad1.left_stick_y);
        telemetry.addData("left stick x", gamepad1.left_stick_x);
        telemetry.addData("right stick y", gamepad1.right_stick_y);
        telemetry.addData("right stick x", gamepad1.right_stick_x);
        telemetry.addData("y", y);
        telemetry.addData("x", x);
        telemetry.addData("turningPower", turningPower);
    }
}


