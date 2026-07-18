package org.firstinspires.ftc.teamcode.pedroPathing;

import com.pedropathing.control.PIDFCoefficients;
import com.pedropathing.follower.Follower;
import com.pedropathing.follower.FollowerConstants;
import com.pedropathing.ftc.FollowerBuilder;
import com.pedropathing.ftc.drivetrains.MecanumConstants;
import com.pedropathing.ftc.localization.constants.DriveEncoderConstants;
import com.pedropathing.ftc.localization.constants.PinpointConstants;
import com.pedropathing.paths.PathConstraints;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

public class Constants { //TODO These constants were updated on 2/21/26 for the 25/26 FTC Season DECODE
    public static FollowerConstants followerConstants = new FollowerConstants().mass(14.7) // must be in kg
            .forwardZeroPowerAcceleration(-39.5731743) //-33.93568
            .lateralZeroPowerAcceleration(-79.0157080) //-70.53691
            .translationalPIDFCoefficients(new PIDFCoefficients(0.11, 0, 0.015, 0.025)) //0.1, 0, 0.015, 0.033
            .headingPIDFCoefficients(new PIDFCoefficients(1.2, 0, 0.06, 0.03)) //1.2, 0, 0.025, 0.03
            .centripetalScaling(0.00065) //0.00069
            ;

    public static PathConstraints pathConstraints = new PathConstraints(0.99, 100, 1, 1);

    public static Follower createFollower(HardwareMap hardwareMap) {
        return new FollowerBuilder(followerConstants, hardwareMap)
                .pathConstraints(pathConstraints)
                .mecanumDrivetrain(driveConstants)
                .pinpointLocalizer(localizerConstants)
                //.driveEncoderLocalizer(encoderConstants)
                .build();
    }
    public static MecanumConstants driveConstants = new MecanumConstants()
            .maxPower(1)
            .rightFrontMotorName("frontRight")
            .rightRearMotorName("backRight")
            .leftRearMotorName("backLeft")
            .leftFrontMotorName("frontLeft")
            .leftFrontMotorDirection(DcMotorSimple.Direction.REVERSE)
            .leftRearMotorDirection(DcMotorSimple.Direction.REVERSE)
            .rightFrontMotorDirection(DcMotorSimple.Direction.FORWARD)
            .rightRearMotorDirection(DcMotorSimple.Direction.FORWARD)
            .xVelocity(54.9306774) //55.32534
            .yVelocity(39.9604042); //40.60084

    // B = 6 + 10/16 (support bar to back bar)
    // A = 8/16 (support bar to pinpoint)
    // C = 29/32 (pod to back bar)
    public static PinpointConstants localizerConstants = new PinpointConstants()
            .forwardPodY(6.1417325) //good
            .strafePodX(2.3622045) //good
            .distanceUnit(DistanceUnit.INCH)
            .hardwareMapName("odo")
            .encoderResolution(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD)
            .forwardEncoderDirection(GoBildaPinpointDriver.EncoderDirection.REVERSED)
            .strafeEncoderDirection(GoBildaPinpointDriver.EncoderDirection.FORWARD);


    //public static DriveEncoderConstants encoderConstants = new DriveEncoderConstants()
    //        .turnTicksToInches(1.1);
}
