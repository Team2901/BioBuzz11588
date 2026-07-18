package org.firstinspires.ftc.teamcode.testing;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;
import org.firstinspires.ftc.teamcode.hardware.Hardware;
import org.firstinspires.ftc.teamcode.utilities.WriteObservations;

@TeleOp(name = "LimelightTestVision.V2", group = "test")
public class LimelightTestVision extends OpMode{
    private Limelight3A limelight3A;
    Hardware bench = new Hardware();
    private double distance;
    @Override
    public void init() {
        bench.init(hardwareMap, telemetry);
        limelight3A = hardwareMap.get(Limelight3A.class, "limelight");
        limelight3A.pipelineSwitch(0);
        WriteObservations.writeHeader();
    }
    @Override
    public void start(){
        limelight3A.start();
    }
    @Override
    public void loop() {
        // get yaw from control hub IMU
        YawPitchRollAngles orientation = bench.getOrientation();
        limelight3A.updateRobotOrientation(orientation.getYaw(AngleUnit.DEGREES));

        // get latest limelight result, pipeline 8 for April tag 0
        LLResult llResult = limelight3A.getLatestResult();
        if (llResult != null && llResult.isValid()){
            Pose3D botpose = llResult.getBotpose_MT2();
            distance = getDistanceFromTag(llResult.getTa());
            telemetry.addData("Calculated Distance", distance);
            telemetry.addData("Target X", llResult.getTx());
            telemetry.addData("Target Y", llResult.getTy());
            telemetry.addData("YawPitchRollAngles", orientation);
            telemetry.addData("Target Area", llResult.getTa());
            telemetry.addData("Botpose", botpose.toString());
            telemetry.addData("Fiducial Results", llResult.getFiducialResults().size());

            if (!llResult.getFiducialResults().isEmpty()) {
                LLResultTypes.FiducialResult fid = llResult.getFiducialResults().get(0);
                telemetry.addData("Fiducial ID", fid.getFiducialId());
                telemetry.addData("Fiducial Tx", fid.getTargetXDegrees());
                telemetry.addData("Fiducial Target Pose", fid.getTargetPoseRobotSpace());
                WriteObservations wO = new WriteObservations(distance, llResult.getTx(), llResult.getTy(), orientation, llResult.getTa(), llResult.getFiducialResults().size(), fid.getFiducialId(), fid.getTargetXDegrees(), fid.getTargetPoseRobotSpace());
                if(gamepad1.aWasPressed()){
                    wO.writeToFile(WriteObservations.getfilePath(), wO.getData());
                }
            }
        }else{
            telemetry.addLine("Can't see AprilTag");
        }
    }

    public double getDistanceFromTag(double ta){
        double scale = 29759.3774;
        double distance = (scale /ta);
        distance = Math.sqrt(distance);
        return distance;
    }
}
