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
            ArrayList<String> patterns = new ArrayList<String>();
            patterns.add("GPP");
            patterns.add("PGP");
            patterns.add("PPG");
            int ID = llResult.getFiducialResults().get(0).getFiducialId();
            Pose3D botPose = llResult.getBotpose_MT2();
            Tx = llResult.getTx();
            Ty = llResult.getTy();
            Ta = llResult.getTa();

            telemetry.addData("Tag ID: ", ID);
            telemetry.addData("Target X Offset (degree): ", Tx);
            telemetry.addData("Target Y Offset (degree): ", Ty);
            telemetry.addData("Target Area Offset (degree): ", Ta);

            Pose3D limelightPose = llResult.getBotpose();
            Position llPosition = limelightPose.getPosition();

            telemetry.addData("Pose unit", llPosition.unit);
            telemetry.addData("Pose X", llPosition.x);
            telemetry.addData("Pose X", llPosition.y);
            telemetry.addData("Distance (in meters): ", getDistanceFromTag(Ta));

            if (ID >= 21 && ID <= 23){
                if (ID == 21){
                    telemetry.addData("Pattern: ", patterns.get(0));
                }
                if (ID == 22){
                    telemetry.addData("Pattern: ", patterns.get(1));
                }
                if (ID == 23){
                    telemetry.addData("Pattern: ", patterns.get(2));
                }
            }

            if (ID >= 20 && ID <= 24){
                if (ID == 20){
                    telemetry.addLine("Facing Blue");
                }
                if (ID == 24){
                    telemetry.addLine("Facing Red");
                }
            }
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


