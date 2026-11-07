package org.firstinspires.ftc.teamcode.autonomous;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.autonomous.StateMachine.MyState;
import org.firstinspires.ftc.teamcode.autonomous.StateMachine.StateMachine;

import java.util.ArrayList;

@Autonomous(name = "StateMachineExample")
public class StateMachineExample extends OpMode {

    CleanCounterState cleanCounters = new CleanCounterState(gamepad1,telemetry);
    TakeOutTrashState takeOutTrash = new TakeOutTrashState(gamepad1,telemetry);
    DoDishesState doDishes = new DoDishesState(gamepad1,telemetry);
    MopFloor mopFloor = new MopFloor(gamepad1,telemetry);
    ArrayList<MyState> listOfStates = new ArrayList<MyState>();
    StateMachine stateMachine;

    @Override
    public void init() {
        listOfStates.add(takeOutTrash);
        listOfStates.add(cleanCounters);
        listOfStates.add(doDishes);
        listOfStates.add(mopFloor);
        stateMachine = new StateMachine(listOfStates);
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

    public BaseState(Gamepad gamepad1, Telemetry telemetry) {
        this.gamepad1 = gamepad1;
        this.telemetry = telemetry;
    }

    public BaseState() {

    }

    public abstract void enter();

    public abstract boolean isEnded();

}


class TakeOutTrashState extends BaseState {

    public TakeOutTrashState(Gamepad gamepad1, Telemetry telemetry) {
        super(gamepad1, telemetry);
    }

    @NonNull
    @Override
    public String toString() {
        return "TakeOutTrash";
    }

    @Override
    public void enter() {
        done = false;
    }

    public boolean update() {
        if (gamepad1.aWasPressed()) {
            telemetry.addLine("A button was pressed.");
            done = true;
        } else {
            telemetry.addLine("Press A when done Taking out the trash.");
        }
        return false;
    }

    @Override
    public boolean isEnded() {
        return done;
    }

}

// TODO : advance to next state when B button pressed
class CleanCounterState extends BaseState {

    public CleanCounterState(Gamepad gamepad1, Telemetry telemetry) {
        super(gamepad1, telemetry);
    }

    public CleanCounterState() {
        super();
    }

    @NonNull
    @Override
    public String toString() {
        return "CleanCounter";
    }

    @Override
    public boolean update() {
        if (gamepad1.bWasPressed()) {
            telemetry.addLine("B button was pressed.");
            done = true;
        } else {
            telemetry.addLine("Press B when done cleaning the counter.");

        }
        return false;
    }

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
    ElapsedTime timer = new ElapsedTime(0);

    public DoDishesState(Gamepad gamepad1, Telemetry telemetry) {
        super(gamepad1, telemetry);
    }

    @NonNull
    @Override
    public String toString() {
        return "DoDishes";
    }

    @SuppressLint("DefaultLocale")
    @Override
    public boolean update() {
        if (timer.seconds() > 5) {
            telemetry.addLine("Timer's up, dishes are done.");
            done = true;
        } else {
            double countdown = 5 - timer.seconds();
            telemetry.addLine(String.format("Dishes will be done in %.3f seconds.", countdown));

        }
        return false;
    }

    @Override
    public void enter() {
        timer.reset();
    }

    @Override
    public boolean isEnded() {
        return done;
    }
}

// TODO: advance to next state when X button pressed
class MopFloor extends BaseState {


    public MopFloor(Gamepad gamepad1, Telemetry telemetry) {
        super(gamepad1, telemetry);
    }

    @NonNull
    @Override
    public String toString() {
        return "MopFloor";
    }

    @Override
    public boolean update() {
        if (gamepad1.xWasPressed()) {
            telemetry.addLine("X button was pressed.");
            done = true;
        } else {
            telemetry.addLine("Press X when done mopping the floor.");

        }
        return false;
    }

    @Override
    public void enter() {

    }

    @Override
    public boolean isEnded() {
        return done;
    }
}
