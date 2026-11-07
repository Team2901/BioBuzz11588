package org.firstinspires.ftc.teamcode.hardware;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;

public class Turret {
    Telemetry telemetry;
    Hardware robot;
    public DcMotorEx turret;
    HardwareMap hardwareMap;
    private final static double TURRET_GEAR_RATIO = 2.75/1;
    private final static double TICKS_PER_TURRET_REVOLUTION = Hardware.TICKS_PER_MOTOR_REV*TURRET_GEAR_RATIO;
    public Turret(HardwareMap _hardwareMap, Telemetry _telemetry, Hardware _robot){
        hardwareMap = _hardwareMap;
        telemetry = _telemetry;
        robot = _robot;
        try {
            turret = hardwareMap.get(DcMotorEx.class, "turret");
        } catch (IllegalArgumentException e) {
            turret = new MockDcMotor();
            telemetry.addLine("Can't find turret: making a mock");
        }
        turret.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }
    public double findNewAngle(int x, int y){
        Pose2D poseB = new Pose2D(DistanceUnit.INCH,x,y, AngleUnit.DEGREES,0.0);
        Pose2D newPose = robot.pose2DDelta(poseB, robot.pinpoint.getPosition());
        double difX = newPose.getX(DistanceUnit.INCH);
        double difY = newPose.getY(DistanceUnit.INCH);
        double newAngle = Math.atan2(difY,difX);
        newAngle = Math.toDegrees(newAngle);
        return newAngle;
    }
    public double getAngle(){
        return 360*(turret.getCurrentPosition()%TICKS_PER_TURRET_REVOLUTION)/(TICKS_PER_TURRET_REVOLUTION);
        // It is a 312 RPM Motor so it uses the same TICKS_PER_MOTOR_REV as drivebase motors
    }
    public void resetEncoder(){
        turret.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        turret.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }
    private void setAngle(){
        double newAngle;
        if(robot.pinpoint.getPosition().getY(DistanceUnit.INCH) > 0){
            newAngle = findNewAngle(-12,12);
        }else{
            newAngle = findNewAngle(-12,-12);
        }
        double ticks = (newAngle/360)*TICKS_PER_TURRET_REVOLUTION;
        double posDifference = ticks-turret.getCurrentPosition();
        double difWrapped = posDifference%TICKS_PER_TURRET_REVOLUTION;
        if(difWrapped>TICKS_PER_TURRET_REVOLUTION/2){
            turret.setTargetPosition((int) (difWrapped-TICKS_PER_TURRET_REVOLUTION));
        }else if(difWrapped<TICKS_PER_TURRET_REVOLUTION/2){
            turret.setTargetPosition((int) (difWrapped+TICKS_PER_TURRET_REVOLUTION));
        }else{
            turret.setTargetPosition((int) difWrapped);
        }
        turret.setVelocity(robot.getTurnToAngleSpeed(newAngle));
    }
    public void update(){
        setAngle();
        telemetry.addData("Current turret angle", robot.turret.getAngle());
        telemetry.addData("Desired angle", findNewAngle(-12, -12));
    }
}
