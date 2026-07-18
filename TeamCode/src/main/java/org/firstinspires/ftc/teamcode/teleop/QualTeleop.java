package org.firstinspires.ftc.teamcode.teleop;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;
import org.firstinspires.ftc.teamcode.hardware.Hardware;

import java.util.ArrayList;
import java.util.Arrays;

@TeleOp(name = "QualTeleopV4")
public class QualTeleop extends OpMode {
    Hardware robot = new Hardware();
    private Limelight3A limelight3A;

    double turningPower;

    //telemetry
    public void help() {

        //telemetry.addData("position ", String.format("x=%.2f, y=%.2f, angle=%.2f", follower.getPose().getX(), follower.getPose().getY(), Math.toDegrees(follower.getPose().getHeading())));
        telemetry.addLine("----------------------");
        telemetry.addLine("Gamepad 1");
        telemetry.addData("Left Joy", "Move drive base");
        telemetry.addData("Right Joy (X only)", "Turn drive base");
    }

    @Override
    public void init() {
        robot.init(this.hardwareMap, telemetry);
        //initialize camera or throw an error if it fails.
        try{
            limelight3A = hardwareMap.get(Limelight3A.class, "limelight");
            limelight3A.pipelineSwitch(0);
        } catch (IllegalArgumentException e){
            System.out.println("Warning: No Limelight found.");
        }
    }

    //start limelight camera if it is initialized
    @Override
    public void start(){
        if(limelight3A != null) limelight3A.start();
    }
    @Override
    public void loop() {
        //update joysticks and robot.speed
        double joyStickAngle = Math.atan2(-gamepad1.left_stick_y, gamepad1.left_stick_x) - Math.toRadians(90);
        double joyStickMagnitude = Math.hypot(-gamepad1.left_stick_y, gamepad1.left_stick_x);
        double moveAngle = joyStickAngle-robot.imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.RADIANS);
        double y = robot.speed * joyStickMagnitude*Math.sin(moveAngle);
        double x = robot.speed * joyStickMagnitude*Math.cos(moveAngle);

        YawPitchRollAngles orientation = robot.getOrientation();

        if(limelight3A != null) limelight3A.updateRobotOrientation(orientation.getYaw(AngleUnit.DEGREES));

        //update turning power based on right stick x
        if(gamepad1.right_stick_x != 0){
            turningPower = .75 * gamepad1.right_stick_x * robot.speed ;
        }
        if (gamepad1.left_bumper) {
            robot.imu.resetYaw();
        }
        //update the wheels' velocities
        robot.frontLeft.setVelocity(x - y + turningPower);
        robot.frontRight.setVelocity(x + y - turningPower);
        robot.backLeft.setVelocity(x + y + turningPower);
        robot.backRight.setVelocity(x - y - turningPower);
        LLResult llResult = limelight3A!=null? limelight3A.getLatestResult():null;
        //telemetry
        if (llResult != null && llResult.isValid()){
            Pose3D botPose = llResult.getBotpose_MT2();
            double distance = getDistanceFromTag(llResult.getTa());
            telemetry.addData("Calculated Distance", distance);
            telemetry.addData("Target X", llResult.getTx());
            telemetry.addData("Target Area", llResult.getTa());
            telemetry.addData("BotPose", botPose.toString());
        }
        telemetry.addData("imu Angle", Math.toDegrees(robot.imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.RADIANS)));
        telemetry.addData("Joystick Angle", Math.toDegrees(joyStickAngle));
        telemetry.addData("Move Angle", Math.toDegrees(moveAngle));
        telemetry.addData("turningPower", turningPower);
        help();
        telemetry.update();
    }
    //update distance based on ta

    //only works when facing head on
    public double getDistanceFromTag(double ta){
        double scale = 29759.3774;
        double distance = (scale /ta);
        distance = Math.sqrt(distance);
        return distance;
    }
}