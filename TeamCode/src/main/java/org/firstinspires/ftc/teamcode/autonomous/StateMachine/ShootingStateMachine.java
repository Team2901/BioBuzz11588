package org.firstinspires.ftc.teamcode.autonomous.StateMachine;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.hardware.Hardware;

import java.util.ArrayList;

@Configurable
public class ShootingStateMachine {

    ArrayList<MyState> order;
    Telemetry telemetry;
    int orderIndex;
    public ShootingStateMachine(ArrayList<MyState> _order, Telemetry _telemetry) {
        telemetry = _telemetry;
        order = _order;
        cancel();
    }
    public boolean isRunning(){
        return isInBounds(orderIndex);
    }
    public MyState getState() {
        if(isInBounds(orderIndex)){
            return order.get(orderIndex);
        }
        return null;
    }
    public int getOrderIndex(){
        return orderIndex;
    }
    public void cancel(){
        orderIndex = -1;
    }
    public void start(){
        orderIndex = 0;
        order.get(orderIndex).enter();
    }
    public boolean isInBounds(int num){
        return num>-1 && num<order.size();
    }
    public void update() {
        if(orderIndex < 0){
            return;
        }
        MyState nextState = null;
        if(order.get(orderIndex).isEnded()){
            order.get(orderIndex).leaving();
            if(isInBounds(orderIndex+1)){
                orderIndex++;
                nextState = order.get(orderIndex);
            }else{ // This is so that if not entering END state; state will be null
                orderIndex = -1;
            }
        }
        if (nextState != null) {
            order.get(orderIndex).enter();
        }
        if(isInBounds(orderIndex)){
            order.get(orderIndex).update();
        }

    }
    public static class SpinUp extends MyState {
        Hardware robot;
        public SpinUp(Hardware robot){
            this.robot = robot;
        }
        @Override
        void enter() {
            robot.launcher.setVelocity(Hardware.defaultVelocity);
        }

        @Override
        boolean isEnded() {
            return (Math.abs(robot.launcher.getVelocity()- Hardware.defaultVelocity)<=10);
        }
    }
    public static class MoveTransfer extends MyState{
        public int TRANSFER_POS_1 = 0;
        public int TRANSFER_POS_2 = 1500;
        Hardware robot;
        Telemetry telemetry;
        boolean transferAtPos2 = false;
        public MoveTransfer(Hardware _robot, Telemetry _telemetry){
            robot = _robot;
            telemetry = _telemetry;
        }
        @Override
        void enter() {
            if(robot.transfer.getTargetPosition() != TRANSFER_POS_2) {
                transferAtPos2 = true;
            }else{
                transferAtPos2 = false;
            }

            if (transferAtPos2) {
                robot.transfer.setTargetPosition(TRANSFER_POS_2);
            } else {
                robot.transfer.setTargetPosition(TRANSFER_POS_1);
            }
            robot.transfer.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            robot.transfer.setPower(1);
        }

        @Override
        void update() {
            telemetry.addData("currentPosition: ", robot.transfer.getCurrentPosition());
            telemetry.addData("transferAtPos2: ", transferAtPos2);
            super.update();
        }

        @Override
        boolean isEnded() {
            if(transferAtPos2){
                return robot.transfer.getCurrentPosition()<TRANSFER_POS_2+10 && robot.transfer.getCurrentPosition()>TRANSFER_POS_2-10;
            }else{
                return robot.transfer.getCurrentPosition()<TRANSFER_POS_1+10 && robot.transfer.getCurrentPosition()>TRANSFER_POS_1-10;
            }
        }
    }
}
