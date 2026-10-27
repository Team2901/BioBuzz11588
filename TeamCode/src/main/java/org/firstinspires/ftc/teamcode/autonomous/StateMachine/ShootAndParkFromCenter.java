package org.firstinspires.ftc.teamcode.autonomous.StateMachine;


import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.autonomous.AbstractAutonomous;

import java.util.ArrayList;
import java.util.Arrays;

@Autonomous(name = "ShootAndParkFromCenter")
public class ShootAndParkFromCenter extends AbstractAutonomous {
    ShootingStateMachine shootSystem = new ShootingStateMachine(new ArrayList<>(Arrays.asList(new Idle(), new ShootingStateMachine.SpinUp(robot), new ShootingStateMachine.MoveTransfer(robot, telemetry), new ShootingStateMachine.MoveTransfer(robot, telemetry), new End())), telemetry);
    @Override
    public void runOpMode() throws InterruptedException {
        robot.init(hardwareMap, telemetry);
        // move the robot forward 18 inches and stop
        telemetry.addLine("moves forward 18 inches, turn, move forward 4 feet");
        telemetry.update();
        waitForStart();
        shootSystem.start();
        while (shootSystem.getOrderIndex() < shootSystem.order.size() - 1) {
            shootSystem.update();
        }
        shootSystem.cancel();
        /*
        move(18, 0);
        turnToAngle(-90);
        move(5 * 12, 0);
         */
    }

}