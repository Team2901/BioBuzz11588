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
/*
This is a sequential state machine meaning it moves through states in the order given at the
making of an object of this class. Each state is a class in itself but every state will share 2
methods since they are the children of the abstract class MyState. These methods are enter() and
isEnded().,

enter() is what needs to be called upon entering that state
isEnded() checks if the end conditions of the state have been meet.

This class makes order an ArrayList of states which
is populated with objects of the chosen states when an object of this class is constructed.
Since it is a list of states and we want to move through those states we need a way to do that.
We use orderIndex to do this by making a method which retrieves the state storied in order at
orderIndex and then once that state's isEnded() is true we increment orderIndex moving to the state
after the current state within order. Whenever we do this we must call enter() or else nothing will
happen because this class in itself doesn't have the code we want to run on the robot instead it
acts as a container for the order we want that code to run in. The states are what actually contain
the code we want to the robot to enact.
 */
class StateMachine {
    Follower follower;
    // The index for the state in order which the system is currently in
    int orderIndex;
    // Will store states when StateMachineRun class passes them into the constructor
    ArrayList<MyState> order;
    // Non pedropathing constructor
    public StateMachine(ArrayList<MyState> _order) {
        order = _order;
        cancel();
    }
    // Pedropathing constructor
    public StateMachine(ArrayList<MyState> _order, Follower _follower) {
        order = _order;
        follower = _follower;
        cancel();
    }
    // Returns the state that the orderIndex currently points to in order
    // In simple language which state the robot is currently "in"
    public MyState getState() {
        // Checks bounds to avoid indexOutOfBoundsError
        // Note this returns an object of MyState not the states name
        if(isInBounds(orderIndex)){
            return order.get(orderIndex);
        }
        // For if called when order has not been populated or orderIndex is out of bounds
        return null;
    }
    // Immediately stops movement between states as orderIndex = -1 is outside bounds
    public void cancel(){
        orderIndex = -1;
    }
    // Sets orderIndex to zero causing update to begin moving through states within order
    public void start(){
        orderIndex = 0;
        order.get(orderIndex).enter();
    }
    // Checks if a given number is between 0 and the number of states within order - 1
    public boolean isInBounds(int num){
        return num>-1 && num<order.size();
    }

    public void update() {
        // Stops update from running if orderIndex is less than 1
        if(orderIndex < 0){
            return;
        }
        // Declares a temporary holding State and initializes it as null
        MyState nextState = null;
        // isEnded() returns a boolean which represents if the state has met the conditions to move
        // into the next state
        if(order.get(orderIndex).isEnded()){
            // leaving() is called for states which need something done before the next state
            // which can not be done within the state itself
            order.get(orderIndex).leaving();
            // Increments orderIndex and sets nextState equal to the state at that index
            if(isInBounds(orderIndex+1)){
                orderIndex++;
                nextState = order.get(orderIndex);
            }else{ // Stops update() incrementing once orderIndex will no longer go to next state
                orderIndex = -1;
            }
        }
        // Ensures we don't call enter on a null or already called state
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
