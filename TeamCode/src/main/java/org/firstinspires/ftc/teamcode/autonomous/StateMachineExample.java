package org.firstinspires.ftc.teamcode.autonomous;

import androidx.annotation.NonNull;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.autonomous.StateMachine.MyState;
import org.firstinspires.ftc.teamcode.autonomous.StateMachine.StateMachine;

import java.util.ArrayList;

@Autonomous(name = "StateMachineExample")
public class StateMachineExample extends OpMode {

    CleanCounterState cleanCounters = new CleanCounterState();
    TakeOutTrashState takeOutTrash = new TakeOutTrashState();
    DoDishesState doDishes = new DoDishesState();
    MopFloor mopFloor = new MopFloor();
    ArrayList<MyState> listOfStates = new ArrayList<MyState>();
    StateMachine stateMachine;

    @Override
    public void init() {
        takeOutTrash.gamepad1= gamepad1;
        takeOutTrash.telemetry= telemetry;

        listOfStates.add(takeOutTrash);
        listOfStates.add(cleanCounters);
        listOfStates.add(doDishes);
        listOfStates.add(mopFloor);
        stateMachine  = new StateMachine(listOfStates);
    }

    @Override
    public void start() {
        stateMachine.start();
    }

    @Override
    public void loop() {
        telemetry.addData("Current State", stateMachine.getState());
        stateMachine.update();
    }
}

abstract class BaseState extends MyState {
    boolean done;
    Gamepad gamepad1;
    Telemetry telemetry;

    public abstract void enter();
    public abstract boolean isEnded();

}


class TakeOutTrashState extends BaseState {

    @NonNull
    public String toString() {
        return "TakeOutTrash";
    }

    @Override
    public void enter() {
        done = false;
    }

    public void update() {
        if (gamepad1.aWasPressed()) {
            telemetry.addLine("A button was pressed.");
            done = true;
        } else {
            telemetry.addLine("Press A when done Taking out the trash.");
        }
    }

    @Override
    public boolean isEnded() {
        return done;
    }

}

// TODO : advance to next state when B button pressed
class CleanCounterState extends BaseState {

    @Override
    public void enter() {

    }

    @Override
    public boolean isEnded() {
        return done;
    }
}

// TODO: advance to next state when Y button pressed
class DoDishesState extends BaseState {

    @Override
    public void enter() {

    }

    @Override
    public boolean isEnded() {
        return done;
    }
}

// TODO: advance to next state when X button pressed
class MopFloor extends BaseState {


    @Override
    public void enter() {

    }

    @Override
    public boolean isEnded() {
        return done;
    }
}
