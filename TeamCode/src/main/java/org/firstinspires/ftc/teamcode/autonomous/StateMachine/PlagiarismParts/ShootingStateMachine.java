package org.firstinspires.ftc.teamcode.autonomous.StateMachine.PlagiarismParts;

import com.bylazar.configurables.annotations.Configurable;

@Configurable
public class ShootingStateMachine {
    /*
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
            robot.launcher.setVelocity(robot.defaultVelocity);
        }

        @Override
        boolean isEnded() {
            return (Math.abs(robot.launcher.getVelocity()-robot.defaultVelocity)<=100);
        }
    }
    public static class Roll extends MyState {
        ElapsedTime timer;
        @Override
        void enter() {
            timer = new ElapsedTime();
            timer.reset();
        }

        @Override
        boolean isEnded() {
            return timer.seconds() > 1;
        }
    }
    public static class EndRoll extends MyState {

        ElapsedTime timer;
        Hardware robot;
        public EndRoll(Hardware robot){
            this.robot = robot;
        }
        @Override
        void enter() {
            timer = new ElapsedTime();
            timer.reset();
            robot.closeGate();
            robot.launcher.setVelocity(0);
        }

        @Override
        boolean isEnded() {
            return timer.seconds() > 1;
        }
    }
    public static class OpenGate extends MyState {
        ElapsedTime timer;
        Hardware robot;
        public OpenGate(Hardware robot){
            this.robot = robot;
        }
        @Override
        void enter() {
            timer = new ElapsedTime();
            timer.reset();
            robot.openGate();
        }

        @Override
        boolean isEnded() {
            return timer.seconds() > 1;
        }
    }
    public static class CloseGate extends MyState {
        ElapsedTime timer;
        Hardware robot;
        public CloseGate(Hardware robot){
            this.robot = robot;
        }
        @Override
        void enter() {
            timer = new ElapsedTime();
            timer.reset();
            robot.closeGate();
        }

        @Override
        boolean isEnded() {
            return timer.seconds() > 1;
        }
    }
    public static class WaitToOpen extends MyState {
        CanOpen canOpen;
        Telemetry telemetry;
        public WaitToOpen(CanOpen _canOpen, Telemetry _telemetry){
            this.canOpen = _canOpen;
            telemetry = _telemetry;
        }
        @Override
        void enter() {

        }

        @Override
        boolean isEnded() {
            return canOpen.value;
        }

        @Override
        void leaving() {
            super.leaving();
            canOpen.value = false;
        }
    }
    public static class CarouselReady extends MyState {
        Hardware robot;
        public CarouselReady(Hardware robot){
            this.robot = robot;
        }
        @Override
        void enter() {
            robot.carousel.advanceBySlots(1);
        }

        @Override
        boolean isEnded() {
            return !robot.carousel.motor.isBusy();
        }
    }
    public static class CarouselMove extends MyState {
        Hardware robot;
        int num;
        int i = 0;
        public CarouselMove(Hardware robot, int _num){
            this.robot = robot;
            num = _num;
        }
        @Override
        void enter() {
            i = 0;
        }

        @Override
        boolean isEnded() {
            return !robot.carousel.motor.isBusy() && i==num;
        }
        void update() {
            super.update();
            if(Math.abs(robot.launcher.getVelocity()-robot.defaultVelocity)<=100 && i<num){
                robot.carousel.advanceBySlots(1);
                i++;
            }
        }
    }

 */
}
