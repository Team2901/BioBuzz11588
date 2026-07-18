package org.firstinspires.ftc.teamcode.autonomous.StateMachine;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.autonomous.AbstractAutonomous;

import java.util.ArrayList;
import java.util.Arrays;

@Autonomous (name = "StateMachineRun")
public class StateMachineRun extends AbstractAutonomous {
    ElapsedTime timer = new ElapsedTime();
    int k;
    @Override
    public void runOpMode() throws InterruptedException {
        robot.init(this.hardwareMap, telemetry);
        Alliance alliance = askAlliance();
        if(alliance == Alliance.RED){
            k = 1;
        }else if(alliance == Alliance.BLUE){
            k = -1;
        }
        StateMachine moveSystem = new StateMachine(new ArrayList<>(Arrays.asList(new Idle(), new End())));
        waitForStart();
        timer.reset();
        moveSystem.start();
        while(timer.seconds() < 30 && opModeIsActive()){
            moveSystem.update();
            telemetry.addData("Current State Move", moveSystem.getState());
            telemetry.addData("IMU", robot.imu.getRobotYawPitchRollAngles());
            telemetry.addData("Turn To Angle Speed Degree 0", robot.getTurnToAngleSpeed(0.0));
            telemetry.update();
        }
    }
}
