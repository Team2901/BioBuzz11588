package org.firstinspires.ftc.teamcode.hardware;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;

public class Turret {
    Telemetry telemetry;
    Hardware robot = new Hardware();
    public DcMotorEx turret;
    public double angle;
    HardwareMap hardwareMap;
    public Turret(HardwareMap _hardwareMap, Telemetry _telemetry, double _angle){
        hardwareMap = _hardwareMap;
        telemetry = _telemetry;
        angle = _angle;
        try {
            turret = hardwareMap.get(DcMotorEx.class, "turret");
        } catch (IllegalArgumentException e) {
            turret = new MockDcMotor();
            telemetry.addLine("Can't find turret: making a mock");
        }
    }
    public double findNewAngle(int x, int y){
        Pose2D poseB = new Pose2D(DistanceUnit.INCH,x,y, AngleUnit.DEGREES,0.0);
        Pose2D newPose = robot.pose2DDelta(poseB, robot.pinpoint.getPosition());
        double difX = newPose.getX(DistanceUnit.INCH);
        double difY = newPose.getY(DistanceUnit.INCH);
        double newAngle = Math.atan2(difY,difX);
        Math.toDegrees(newAngle);
        return newAngle;
    }
}
