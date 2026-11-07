package org.firstinspires.ftc.teamcode.autonomous.StateMachine;

public abstract class MyState {
    public abstract void enter();

    public void update() {

    }
    public abstract boolean isEnded();
    public void leaving(){

    }
}
