package org.firstinspires.ftc.teamcode.autonomous.StateMachine;

public abstract class MyState {
    abstract void enter();

    void update() {

    }
    abstract boolean isEnded();
    void leaving(){

    }
}
