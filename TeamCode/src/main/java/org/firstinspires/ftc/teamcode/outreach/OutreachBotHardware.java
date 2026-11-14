package org.firstinspires.ftc.teamcode.outreach;

import com.qualcomm.robotcore.hardware.AnalogInput;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

/// Software requirements for clawbot:
/// knowing where arm is using potentiometer
/// move arm smoothly (raise and lower)
/// move claw (open and closed)
/// move drivebase - nick's code (forward, backward, right, left) (raise claw a little so doesn't drag)
@SuppressWarnings("unused")
public class OutreachBotHardware {
    public DcMotor leftDrive = null;
    public DcMotor rightDrive = null;
    public Servo claw = null;
    public DcMotor arm = null;
    private HardwareMap hardwareMap = null;
    public enum ClawState { OPEN, CLOSED }
    public ClawState currentClawState = ClawState.CLOSED;
    public AnalogInput potentiometer = null;


    //outreach:
    public static final double TURN_WEIGHT = 1;
    public static final double STRAIGHT_POWER_WEIGHT = 1.0;
    public static final double CLAW_OPEN_POSITION = 0;
    public static final double CLAW_CLOSED_POSITION = 0.5;
    public static final double ARM_UP_POSITION = 3.0;
    public static final double ARM_DOWN_POSITION = 0.5;
    public static double ARM_POWER = 0.5;

    public void init(HardwareMap ahwMap) {
        // Save reference to Hardware map
        hardwareMap = ahwMap;

        //Refactored it to have variable syntax and called them drive instead of motor
        // Define and Initialize Motors
        leftDrive = hardwareMap.dcMotor.get("leftDrive");
        rightDrive = hardwareMap.dcMotor.get("rightDrive");
        leftDrive.setDirection(DcMotor.Direction.REVERSE); // Set to REVERSE if using AndyMark motors
        rightDrive.setDirection(DcMotor.Direction.FORWARD);

        leftDrive.setPower(0);
        rightDrive.setPower(0);

        leftDrive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        rightDrive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        claw = hardwareMap.servo.get("claw");
        claw.setPosition(0);

        arm = hardwareMap.dcMotor.get("arm");
        //potentiometer = hardwareMap.analogInput.get("potentiometer");

        arm.setDirection(DcMotor.Direction.FORWARD);
        arm.setPower(0);
        arm.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        arm.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    }

    // reading arm position
    public double getArmPosition() {
        return potentiometer.getVoltage();
    }

    /*public void moveArm(double targetPosition) {
        double currentPosition = potentiometer.getVoltage();
        double error = targetPosition - currentPosition;
        double power = error * ARM_POWER;
        power = Math.max(-0.5, Math.min(0.5, power));
        arm.setPower(power);
    } */

    //this sets the power of the arm motor, whether positive or negative
    public void setArmPower(double power) {
        arm.setPower(power);
    }


    }