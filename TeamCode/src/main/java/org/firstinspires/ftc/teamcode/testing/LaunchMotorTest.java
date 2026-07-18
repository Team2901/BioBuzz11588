/*package org.firstinspires.ftc.teamcode.testing;

import com.bylazar.configurables.annotations.Configurable;
import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;

import org.firstinspires.ftc.teamcode.hardware.Hardware;

@TeleOp(name = "LaunchMotorTestV1", group = "test")
@Configurable
public class LaunchMotorTest extends OpMode {
    static TelemetryManager panelsTelemetry;
    static double power = 0;
    static int velocity = 0;

    static double p;
    static double i;
    static double d;
    static double f;

    Hardware robot = new Hardware();
    int ticksPerSecond;
    enum placeValue {
        ONES,
        TENS,
        HUNDREDS,
        THOUSANDS,
    }
    int valuesIndex = 0;

    placeValue currentValue;
    placeValue[] values = placeValue.values();
    enum Modes { POWER, VELOCITY, DISTANCE}
    Modes currentMode = Modes.POWER;
    public void help(){
        telemetry.addLine("Use Dpad up/down to change velocity " +
                "in specific place value or power");
        telemetry.addLine("Use Dpad right/left to change velocity " +
                "place value being updated.");
        telemetry.addLine("            up - increase");
        telemetry.addLine("            dn - decrease");
        telemetry.addLine("            right - go one place value to the right");
        telemetry.addLine("            left - go one place value to the left");
        telemetry.addLine("Press B to switch between power increment mode and " +
                "velocity increment mode");
        telemetry.addLine("");
    }

    public void telemetry() {
        telemetry.addData("Pid coeff", robot.launcher.getPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER));
        telemetry.addData("current velocity", robot.launcher.getVelocity());
        telemetry.addData("currentmode", currentMode);
        telemetry.addData("velocity setting ", velocity);
        telemetry.addData("power setting", power);
        telemetry.addData("getCurrentPosition", robot.launcher.getCurrentPosition());
        telemetry.addData("panelsTelemetry", panelsTelemetry.toString());

        panelsTelemetry.addData("current velocity", robot.launcher.getVelocity());
        panelsTelemetry.addData("current mode: ", currentMode);
        panelsTelemetry.addData("velocity setting: ", velocity);
        panelsTelemetry.addData("power setting: ", power);
        panelsTelemetry.addData("getCurrentPosition: ", robot.launcher.getCurrentPosition());
        panelsTelemetry.update();

        switch (currentValue) {
            case ONES:
                telemetry.addLine("THOUSANDS HUNDREDS TENS ONES");
                telemetry.addLine("                                                            ^");
                telemetry.addLine("                                                            |");
                break;
            case TENS:
                telemetry.addLine("THOUSANDS HUNDREDS TENS ONES");
                telemetry.addLine("                                                 ^");
                telemetry.addLine("                                                 |");
                break;
            case HUNDREDS:
                telemetry.addLine("THOUSANDS HUNDREDS TENS ONES");
                telemetry.addLine("                          ^");
                telemetry.addLine("                          |");
                break;
            case THOUSANDS:
                telemetry.addLine("THOUSANDS HUNDREDS TENS ONES");
                telemetry.addLine("^");
                telemetry.addLine("|");
                break;
        }
    }

    @Override
    public void init() {
        panelsTelemetry = PanelsTelemetry.INSTANCE.getTelemetry();
        robot.init(this.hardwareMap, telemetry);
        currentValue = values[0];

       PIDFCoefficients defaultCoefficients = robot.launcher.getPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER);
        p = defaultCoefficients.p;
        i = defaultCoefficients.i;
        d = defaultCoefficients.d;
        f = defaultCoefficients.f;

       help();
    }

    @Override
    public void loop() {
        if (gamepad1.bWasPressed()) {
            int currentModeIndex = currentMode.ordinal();
            int nextModeIndex = currentModeIndex + 1;
            if (nextModeIndex >= Modes.values().length) {
                nextModeIndex = 0;
            }
            currentMode = Modes.values()[nextModeIndex];
            velocity = 0;
            power = 0;
            robot.launcher.setVelocity(0);
            robot.launcher.setPower(0);
            currentValue = values[0];
        }
        if (currentMode ==Modes.POWER) {
            if (gamepad1.dpadUpWasPressed() && power <= 1) {
                power += 0.05;
            } else if (gamepad1.dpadDownWasPressed() && power >= -1) {
                power -= 0.05;
            }
            robot.launcher.setPower(power);
        } else {
            if (gamepad1.dpadLeftWasPressed() && valuesIndex < 3) {
                valuesIndex += 1;
                currentValue = values[valuesIndex];
            } else if (gamepad1.dpadRightWasPressed() && valuesIndex > 0) {
                valuesIndex -= 1;
                currentValue = values[valuesIndex];
            }
            switch (currentValue) {
                case ONES:
                    if (gamepad1.dpadUpWasPressed()) {
                        velocity += 1;
                    } else if (gamepad1.dpadDownWasPressed()) {
                        velocity -= 1;
                    }
                    break;
                case TENS:
                    if (gamepad1.dpadUpWasPressed()) {
                        velocity += 10;
                    } else if (gamepad1.dpadDownWasPressed()) {
                        velocity -= 10;
                    }
                    break;
                case HUNDREDS:
                    if (gamepad1.dpadUpWasPressed()) {
                        velocity += 100;
                    } else if (gamepad1.dpadDownWasPressed()) {
                        velocity -= 100;
                    }
                    break;
                case THOUSANDS:
                    if (gamepad1.dpadUpWasPressed()) {
                        velocity += 1000;
                    } else if (gamepad1.dpadDownWasPressed()) {
                        velocity -= 1000;
                    }
                    break;
            }

            if (currentMode == Modes.VELOCITY) {
                robot.launcher.setVelocityPIDFCoefficients(p, i, d, f);
                robot.launcher.setVelocity(velocity);
            } else if (currentMode == Modes.DISTANCE) {
                robot.setMotorSpeedFromDistance(velocity);
            }
        }

        telemetry();
    }
}
*/