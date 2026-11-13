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
    public DcMotorEx turretMotor;
    HardwareMap hardwareMap;
    private final static double TURRET_GEAR_RATIO = 2.75/1;
    private final static double TICKS_PER_TURRET_REVOLUTION = Hardware.TICKS_PER_MOTOR_REV*TURRET_GEAR_RATIO;
    public Turret(HardwareMap _hardwareMap, Telemetry _telemetry, Hardware _robot){
        hardwareMap = _hardwareMap;
        telemetry = _telemetry;
        robot = _robot;
        try {
            turretMotor = hardwareMap.get(DcMotorEx.class, "turret");
        } catch (IllegalArgumentException e) {
            turretMotor = new MockDcMotor();
            telemetry.addLine("Can't find turret: making a mock");
        }
    }
    public double findNewAngle(int x, int y){
        Pose2D poseB = new Pose2D(DistanceUnit.INCH,x,y, AngleUnit.DEGREES,0.0);
        Pose2D currentPose = robot.pinpoint.getPosition();
        Pose2D newPose = robot.pose2DDelta(poseB, currentPose);
        double difX = newPose.getX(DistanceUnit.INCH);
        double difY = newPose.getY(DistanceUnit.INCH);
        double fieldAngle = Math.atan2(difY,difX);
        fieldAngle = Math.toDegrees(fieldAngle);
        double robotHeading = currentPose.getHeading(AngleUnit.DEGREES);
        return robot.angleDiffDegrees(fieldAngle, robotHeading);
    }
    public double getAngle(){
        return 360*(turretMotor.getCurrentPosition()%TICKS_PER_TURRET_REVOLUTION)/(TICKS_PER_TURRET_REVOLUTION);
        // It is a 312 RPM Motor so it uses the same TICKS_PER_MOTOR_REV as drivebase motors
    }
    public void resetEncoder(){
        turretMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
    }
    private void setAngle(){
        double newAngle;
        if(robot.pinpoint.getPosition().getX(DistanceUnit.INCH) >= 0){
            newAngle = findNewAngle(12,-12);
        }else{
            newAngle = findNewAngle(-12,-12);
        }
        double ticks = (newAngle/360)*TICKS_PER_TURRET_REVOLUTION;
        double posDifference = ticks- turretMotor.getCurrentPosition();
        double difWrapped = posDifference%TICKS_PER_TURRET_REVOLUTION;
        if(difWrapped > TICKS_PER_TURRET_REVOLUTION/2){
            difWrapped -= TICKS_PER_TURRET_REVOLUTION;
        }else if(difWrapped < -TICKS_PER_TURRET_REVOLUTION/2){
            difWrapped += TICKS_PER_TURRET_REVOLUTION;
        }
        turretMotor.setTargetPosition(turretMotor.getCurrentPosition() + (int) difWrapped);
        turretMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        turretMotor.setVelocity(robot.getTurnToAngleSpeedTurret(newAngle, false));
    }
    public void update(){
        double desiredAngle;
        if(robot.pinpoint.getPosition().getX(DistanceUnit.INCH) > 0){
            desiredAngle = findNewAngle(12,-12);
        }else{
            desiredAngle = findNewAngle(-12,-12);
        }
        setAngle();
        telemetry.addData("Current turret angle", robot.turret.getAngle());
        telemetry.addData("Desired angle", desiredAngle);
        telemetry.addData("Turret ticks", turretMotor.getCurrentPosition());
    }
}
