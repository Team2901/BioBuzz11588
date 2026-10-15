package org.firstinspires.ftc.teamcode.autonomous;


import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

@Autonomous(name = "ParkFromCenter")
public class ParkFromCenter extends AbstractAutonomous {
    @Override
    public void runOpMode() throws InterruptedException {
        robot.init(hardwareMap, telemetry);
        // move the robot forward 18 inches and stop
        telemetry.addLine("moves forward 18 inches, turn, move forward 4 feet");
        telemetry.update();
        waitForStart();
        move(18, 0);
        turnToAngle(-90);
        move(4 * 12,0);
    }

}