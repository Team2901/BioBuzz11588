package org.firstinspires.ftc.teamcode.autonomous;


import com.bylazar.configurables.annotations.Configurable;
import com.bylazar.configurables.annotations.IgnoreConfigurable;
import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.Path;
import com.pedropathing.paths.PathChain;
import com.pedropathing.paths.PathConstraints;
import com.pedropathing.util.PoseHistory;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.hardware.Hardware;
import org.firstinspires.ftc.teamcode.pedroPathing.Constants;
import org.firstinspires.ftc.teamcode.pedroPathing.MyDrawing;
//import org.firstinspires.ftc.teamcode.pedroPathing.Drawing;

import java.util.function.Supplier;

//Starting Order: GPP
//ID 21: GPP; 0 Turns
//ID 22: PGP; 2 Turns
//ID 23: PPG; 1 Turns
@Disabled
@Configurable
@Autonomous(name = "PedropathingAuto")
public class PedropathingAuto extends OpMode {
    private Follower follower;
    public static Pose startingPose = new Pose(72, 72, Math.toRadians(90)); //start
    public Integer obeliskID = null;
    private Supplier<PathChain> pathChain;
    private TelemetryManager telemetryM;
    public Hardware robot = new Hardware();
    private boolean automatedDrive;


    //public static PathConstraints defaultConstraints = new PathConstraints(0.995, 0.1, 0.1, 0.007, 100, 1, 10, 1);

    public static double tValueConstraint = 0.995;
    public static double velocityConstraint = 0.1;
    public static double translationalConstraint = 0.1;
    public static double headingConstraint = 0.007;
    public static double timeoutConstraint = 200;

    @IgnoreConfigurable
    static PoseHistory poseHistory;

    Limelight3A limelight3A;
    @Override
    public void init() {
        follower = Constants.createFollower(hardwareMap);
        follower.setStartingPose(startingPose == null ? new Pose() : startingPose);
        follower.update();
        telemetryM = PanelsTelemetry.INSTANCE.getTelemetry();
        robot.init(this.hardwareMap, telemetry);
        MyDrawing.init();
        robot.imu.resetYaw();
        limelight3A = hardwareMap.get(Limelight3A.class, "limelight");
        limelight3A.pipelineSwitch(0);
        limelight3A.start();

    }

    @Override
    public void start() {
        //The parameter controls whether the Follower should use break mode on the motors (using it is recommended).
        //In order to use float mode, add .useBrakeModeInTeleOp(true); to your Drivetrain Constants in Constant.java (for Mecanum)
        //If you don't pass anything in, it uses the default (false)
    }

    @Override
    public void loop() {


        //Stop automated following if the follower is done
        if (automatedDrive && !follower.isBusy()) {
            follower.startTeleopDrive();
            automatedDrive = false;
        }

            if (gamepad1.startWasPressed()) {
                pathChain = () -> follower.pathBuilder() //120, 120, 45
                        .addPath(new Path(new BezierLine(follower::getPose, startingPose)))
                        .setLinearHeadingInterpolation(follower.getHeading(), startingPose.getHeading())
                        .setTranslationalConstraint(0.1)
                        .build();
                follower.followPath(pathChain.get(), false);
                follower.setMaxPower(0.75);
                automatedDrive = true;
            }
            follower.update();


        LLResult llResult = limelight3A.getLatestResult();
        //Integer ID;
        if(llResult != null && llResult.isValid()){
            obeliskID = llResult.getFiducialResults().get(0).getFiducialId();
        }
        telemetryM.addData("automatedDrive ", automatedDrive);
        telemetryM.addData("isBusy ", follower.isBusy());
        telemetryM.addData("getDistanceRemaining ", follower.getDistanceRemaining());
        telemetryM.addData("position ", String.format("x=%.2f, y=%.2f, angle=%.2f", follower.getPose().getX(), follower.getPose().getY(), Math.toDegrees(follower.getPose().getHeading())));
        telemetryM.addData("error ", Math.toDegrees(follower.getHeadingError()));
        telemetryM.addData("Tag ID ", obeliskID == null?"not found":obeliskID);
        if(follower.getCurrentPath() != null) {
            telemetryM.addData("headingConstraint ", follower.getCurrentPath().getPathEndHeadingConstraint());
        }
        telemetryM.update(telemetry);


        poseHistory = follower.getPoseHistory();

        try {
            MyDrawing.drawRobot(follower.getPose());
            MyDrawing.drawPoseHistory(poseHistory);
            MyDrawing.sendPacket();
        } catch (Exception e) {
            throw new RuntimeException("Drawing failed ", e);
        }



    }
}


// Just a note: this goes to the spot where the robot would intake the balls, will Calvin's state
// machine tell the robot to move forward and then go back to the launchPose or no?
