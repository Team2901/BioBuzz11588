package org.firstinspires.ftc.teamcode.autonomous.StateMachine;

import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.Path;
import com.pedropathing.paths.PathBuilder;
import com.pedropathing.paths.PathChain;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.hardware.Hardware;

import java.util.ArrayList;
import java.util.function.Supplier;

class StateMachine {
    Follower follower;
    int orderIndex;
    ArrayList<MyState> order;
    public StateMachine(ArrayList<MyState> _order) {
        order = _order;
        cancel();
    }
    public StateMachine(ArrayList<MyState> _order, Follower _follower) {
        order = _order;
        follower = _follower;
        cancel();
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

    static class Move extends MyState {
        Hardware robot;
        int k;
        int x;
        int y;
        public Move(Hardware robot, int _k, int _x, int _y){
            k = _k;
            x = _x;
            y = _y;
            this.robot = robot;
        }
        @Override
        void enter() {
            robot.move(y, k*x);
        }

        @Override
        boolean isEnded() {
            return !robot.isDriveBusy();
        }
    }

    static class PedroPathingMove extends MyState {
        Follower follower;
        Pose targetPose;
        Double timeOutConstraint;
        Double translationalConstraint;
        Double velocityConstraint;
        Double headingConstraint;
        Double tValueConstraint;
        static PedroPathingMove createWithTimeOutConstrain(Follower _follower, Pose _targetPose, double _timeOutConstraint){
            PedroPathingMove path = new PedroPathingMove(_follower, _targetPose);
            path.timeOutConstraint = _timeOutConstraint;
            return path;
        }
        public PedroPathingMove(Follower _follower, Pose _targetPose){
            follower = _follower;
            targetPose = _targetPose;
            tValueConstraint = 0.995;
            velocityConstraint = 0.1;
            translationalConstraint = 0.1;
            headingConstraint = 0.007;
            timeOutConstraint = 200.0;
        }
        @Override
        void enter() {
            PathBuilder builder;
            Supplier<PathChain> pathChain;
            builder =follower.pathBuilder() //120, 120, 45
                    .addPath(new Path(new BezierLine(follower::getPose, targetPose)))
                    .setLinearHeadingInterpolation(follower.getHeading(), targetPose.getHeading());
            if(timeOutConstraint != null){
                builder.setTimeoutConstraint(timeOutConstraint);
            }
            if(translationalConstraint != null){
                builder.setTranslationalConstraint(translationalConstraint);
            }
            if(velocityConstraint != null){
                builder.setVelocityConstraint(velocityConstraint);
            }
            if(headingConstraint != null){
                builder.setHeadingConstraint(headingConstraint);
            }
            if(tValueConstraint != null){
                builder.setTValueConstraint(tValueConstraint);
            }
            pathChain = builder::build;
            follower.followPath(pathChain.get(), false);
            follower.setMaxPower(0.75);

        }

        @Override
        boolean isEnded() {
            return !follower.isBusy();
        }

        @Override
        void update() {
            super.update();
            follower.update();
        }
    }
}
