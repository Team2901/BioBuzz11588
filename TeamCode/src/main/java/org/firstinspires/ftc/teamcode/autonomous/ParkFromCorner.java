package org.firstinspires.ftc.teamcode.autonomous;


import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

@Autonomous(name = "ParkFromCorner")
public class ParkFromCorner extends AbstractAutonomous {
    @Override
    public void runOpMode() throws InterruptedException {
        robot.init(hardwareMap, telemetry);
        // move the robot forward 12 inches and stop
        telemetry.addLine("moves forward 12 inches and stops");
        telemetry.update();
        waitForStart();
        move(12, 0);
    }
}
