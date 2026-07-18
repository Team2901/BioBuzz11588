package org.firstinspires.ftc.teamcode.teleop;
import android.annotation.SuppressLint;

import com.bylazar.configurables.annotations.Configurable;
import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.HeadingInterpolator;
import com.pedropathing.paths.Path;
import com.pedropathing.paths.PathChain;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.hardware.Hardware;
import org.firstinspires.ftc.teamcode.pedroPathing.Constants;

import java.util.function.Supplier;
@Configurable
@TeleOp(name="TestTeleopPedropathing")
public class TestTeleopPedropathing extends OpMode {
    private Follower follower;
    public static Pose startingPose = new Pose(72,72, Math.toRadians(90)); //START
    public static Pose testPos = new Pose(72, 96, Math.toRadians(90)); //24in FORWARD
    private boolean automatedDrive;
    private Supplier<PathChain> pathChain;
    private TelemetryManager telemetryM;
    private double slowModeMultiplier = 1;
    Hardware robot = new Hardware();
    @Override
    public void init() { //TODO starts at (72, 72, 90)
        follower = Constants.createFollower(hardwareMap);
        follower.setStartingPose(startingPose == null ? new Pose() : startingPose);
        follower.update();
        telemetryM = PanelsTelemetry.INSTANCE.getTelemetry();
        robot.init(this.hardwareMap, telemetry);
        //pathChain = () -> follower.pathBuilder() //Lazy Curve Generation
                //.addPath(new Path(new BezierLine(follower::getPose, new Pose(72, 72))))
                //.setHeadingInterpolation(HeadingInterpolator.linearFromPoint(follower::getHeading, Math.toRadians(45), 0.8))
                //.build();
    }
    @Override
    public void start() {
        //The parameter controls whether the Follower should use break mode on the motors (using it is recommended).
        //In order to use float mode, add .useBrakeModeInTeleOp(true); to your Drivetrain Constants in Constant.java (for Mecanum)
        //If you don't pass anything in, it uses the default (false)
        follower.startTeleopDrive();
    }
    @SuppressLint("DefaultLocale")
    @Override
    public void loop() {
        //Call this once per loop
        follower.update();
        telemetryM.update();
        if (!automatedDrive) {
             follower.setTeleOpDrive(
                    -gamepad1.left_stick_y * slowModeMultiplier,
                    -gamepad1.left_stick_x * slowModeMultiplier,
                    -gamepad1.right_stick_x * slowModeMultiplier,
                    true // Robot Centric
            );
        }
        //Automated PathFollowing
        //Stop automated following if the follower is done
        if (automatedDrive && (gamepad1.bWasPressed() || !follower.isBusy())) {
            follower.startTeleopDrive();
            automatedDrive = false;
        }

        //Optional way to change slow mode strength
        if (gamepad2.xWasPressed()) {
            slowModeMultiplier += 0.1;
        }
        //Optional way to change slow mode strength
        if (gamepad2.yWasPressed()) {
            slowModeMultiplier -= 0.1;
        }
        //TODO building a path to point testPos (72, 96, 90)
        if (gamepad1.aWasPressed()) { //72, 96, 90
            pathChain = () -> follower.pathBuilder() //Lazy Curve Generation
                    .addPath(new Path(new BezierLine(follower::getPose, testPos)))
                    .setHeadingInterpolation(HeadingInterpolator.linearFromPoint(follower::getHeading, Math.toRadians(90), 1.0))
                    .setTranslationalConstraint(0.1)
                    .build();
            follower.followPath(pathChain.get(), true);
            follower.setMaxPower(0.75);
            follower.getTranslationalError().getMagnitude();
            automatedDrive = true;
        }
        //TODO starting position, centre of field
        if (gamepad1.aWasPressed()) { //72, 72, 90
            pathChain = () -> follower.pathBuilder() //Lazy Curve Generation
                    .addPath(new Path(new BezierLine(follower::getPose, startingPose)))
                    .setHeadingInterpolation(HeadingInterpolator.linearFromPoint(follower::getHeading, Math.toRadians(90), 1.0))
                    .build();
            follower.followPath(pathChain.get());
            automatedDrive = true;
        }

        //TODO telemetry
        telemetryM.debug("position", follower.getPose());
        telemetryM.debug("velocity", follower.getVelocity());
        telemetryM.debug("automatedDrive", automatedDrive);
        telemetryM.addLine(" ");
        telemetry.addData("position ", String.format("x=%.2f, y=%.2f, angle=%.2f", follower.getPose().getX(), follower.getPose().getY(), Math.toDegrees(follower.getPose().getHeading())));
        telemetry.addData("X error", follower.getTranslationalError().getXComponent());
        telemetry.addData("Y error", follower.getTranslationalError().getYComponent());
        telemetry.addData("velocity ", follower.getVelocity());
        telemetry.addData("automatedDrive ", automatedDrive);
        telemetry.addData("heading ", Math.toDegrees(follower.getHeading()));
        telemetry.addData("targetHeading ", Math.toDegrees(follower.getHeadingError()));
        telemetry.addData("imu ", robot.getAngle());
        telemetry.addData("multiplier: ", slowModeMultiplier);
    }
}