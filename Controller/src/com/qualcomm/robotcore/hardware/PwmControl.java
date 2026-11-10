package com.qualcomm.robotcore.hardware;

/**
 * Virtual Robot's approximation of the FTC SDK's PwmControl: control over the pulse
 * width range a servo is driven with. On the robot, hardwareMap servos are ServoImplEx,
 * which implements this, so team code commonly casts {@code (PwmControl) servo}.
 *
 * The simulator records the range and enable state but does not model their effect on
 * the servo's travel.
 */
public interface PwmControl {

    void setPwmRange(PwmRange range);

    PwmRange getPwmRange();

    void setPwmEnable();

    void setPwmDisable();

    boolean isPwmEnabled();

    /** A PWM timing range, in microseconds. Mirrors the SDK's PwmControl.PwmRange. */
    class PwmRange {
        public static final double usFrameDefault = 20000;
        public static final double usPulseUpperDefault = 2400;
        public static final double usPulseLowerDefault = 600;
        public static final PwmRange defaultRange = new PwmRange(usPulseLowerDefault, usPulseUpperDefault);

        public final double usPulseLower;
        public final double usPulseUpper;
        public final double usFrame;

        public PwmRange(double usPulseLower, double usPulseUpper) {
            this(usPulseLower, usPulseUpper, usFrameDefault);
        }

        public PwmRange(double usPulseLower, double usPulseUpper, double usFrame) {
            this.usPulseLower = usPulseLower;
            this.usPulseUpper = usPulseUpper;
            this.usFrame = usFrame;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof PwmRange)) return false;
            PwmRange him = (PwmRange) o;
            return this.usPulseLower == him.usPulseLower
                    && this.usPulseUpper == him.usPulseUpper
                    && this.usFrame == him.usFrame;
        }

        @Override
        public int hashCode() {
            return Double.valueOf(usPulseLower).hashCode()
                    ^ Double.valueOf(usPulseUpper).hashCode()
                    ^ Double.valueOf(usFrame).hashCode();
        }

        @Override
        public String toString() {
            return String.format("[%.0f, %.0f] frame %.0f us", usPulseLower, usPulseUpper, usFrame);
        }
    }
}
