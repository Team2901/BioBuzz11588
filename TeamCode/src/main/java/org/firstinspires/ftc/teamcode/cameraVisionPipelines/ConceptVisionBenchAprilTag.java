package org.firstinspires.ftc.teamcode.cameraVisionPipelines;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;
import java.util.List;
@TeleOp(name = "AprilTag Test", group = "Vision")
public class ConceptVisionBenchAprilTag extends LinearOpMode {
    private AprilTagProcessor aprilTag;
    private VisionPortal visionPortal;
    @Override
    public void runOpMode() {
        // Create AprilTag processor
        aprilTag = new AprilTagProcessor.Builder()
                .build();
        // Create VisionPortal
        visionPortal = new VisionPortal.Builder()
                .setCamera(
                        hardwareMap.get(
                                WebcamName.class,
                                "Webcam 1"
                        )
                )
                .addProcessor(aprilTag)
                .build();
        telemetry.addLine("AprilTag Vision Ready");
        telemetry.addLine("Press START");
        telemetry.update();
        waitForStart();
        while (opModeIsActive()) {
            List<AprilTagDetection> detections =
                    aprilTag.getDetections();
            telemetry.addData(
                    "Tags Detected",
                    detections.size()
            );
            for (AprilTagDetection detection : detections) {

                telemetry.addData(
                        "Tag ID",
                        detection.id
                );
                if (detection.metadata != null) {
                    telemetry.addData(
                            "X",
                            "%.1f",
                            detection.ftcPose.x
                    );
                    telemetry.addData(
                            "Y",
                            "%.1f",
                            detection.ftcPose.y
                    );
                    telemetry.addData(
                            "Z",
                            "%.1f",
                            detection.ftcPose.z
                    );
                    telemetry.addData(
                            "Yaw",
                            "%.1f",
                            detection.ftcPose.yaw
                    );
                    telemetry.addData(
                            "Range",
                            "%.1f",
                            detection.ftcPose.range
                    );
                }
            }
            telemetry.update();
            sleep(20);
        }
        visionPortal.close();
    }
}
