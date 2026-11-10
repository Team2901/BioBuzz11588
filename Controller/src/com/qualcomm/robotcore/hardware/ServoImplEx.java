package com.qualcomm.robotcore.hardware;

/**
 * Virtual Robot's approximation of the FTC SDK's ServoImplEx: a ServoImpl that also
 * offers PwmControl. On the robot, every servo from hardwareMap is one of these, so the
 * simulator's robot configurations create ServoImplEx too, letting team code cast a
 * servo to PwmControl exactly as it does on a Control Hub.
 */
public class ServoImplEx extends ServoImpl implements PwmControl {

    protected PwmRange pwmRange = PwmRange.defaultRange;
    protected boolean pwmEnabled = true;

    @Override
    public synchronized void setPwmRange(PwmRange range) {
        if (range == null) throw new NullPointerException("range");
        this.pwmRange = range;
    }

    @Override
    public synchronized PwmRange getPwmRange() {
        return pwmRange;
    }

    @Override
    public synchronized void setPwmEnable() {
        pwmEnabled = true;
    }

    @Override
    public synchronized void setPwmDisable() {
        pwmEnabled = false;
    }

    @Override
    public synchronized boolean isPwmEnabled() {
        return pwmEnabled;
    }
}
