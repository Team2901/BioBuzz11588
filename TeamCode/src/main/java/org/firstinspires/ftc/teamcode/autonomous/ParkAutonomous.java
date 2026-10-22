package org.firstinspires.ftc.teamcode.autonomous;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;


@Autonomous(name = "Giancarlo")
public class ParkAutonomous extends AbstractAutonomous {
    @Override
    public void runOpMode() throws InterruptedException {
        robot.init(hardwareMap, telemetry);
        // moves the robot forward 18 inches and stops
        telemetry.addLine("Moves forward 18 inches and stops");
        telemetry.update();
        waitForStart();
        move(18, 0);
        turnToAngle(-90);
        move(4 * 12, 0);
    }
}
