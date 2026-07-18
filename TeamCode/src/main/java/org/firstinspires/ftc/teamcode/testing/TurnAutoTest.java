package org.firstinspires.ftc.teamcode.testing;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.autonomous.AbstractAutonomous;

@Autonomous(name = "TurnAutoTest", group = "test")
public class TurnAutoTest extends AbstractAutonomous {
    boolean runMotor = false;
    @Override
    public void runOpMode() throws InterruptedException {
        robot.init(this.hardwareMap, telemetry);
        waitForStart();
        while(opModeIsActive() && robot.getTurnToAngleSpeed(180.0) != 0){
            if(gamepad1.xWasPressed()){
                runMotor = !runMotor;
            }
            if(runMotor){
                robot.turnToAngle(180);
            }
            telemetry.addLine("X = toggle runMotor");
            telemetry.addData("runMotor", runMotor);
            telemetry.addData("Current Angle", robot.getAngle());
            telemetry.addData("Turn Speed", robot.getTurnToAngleSpeed(180.0));
            telemetry.update();
        }
    }
}
