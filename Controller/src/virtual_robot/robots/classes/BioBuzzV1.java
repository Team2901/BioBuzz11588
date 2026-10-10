package virtual_robot.robots.classes;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorExImpl;
import com.qualcomm.robotcore.hardware.configuration.MotorType;

import javafx.fxml.FXML;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import virtual_robot.controller.BotConfig;

/**
 * Team 2901's BioBuzz robot, version 1.
 *
 * A mecanum drive like Mecanum Bot - the same four wheel motors (frontLeft, frontRight,
 * backLeft, backRight), "imu" and "pinpoint" (plus sensor_otos, octoquad and limelight) -
 * but without Mecanum Bot's back servo, dead-wheel encoders, color sensor or distance
 * sensors. In their place it has three game motors:
 *
 *   "intake"    roller, a 312 RPM goBILDA motor
 *   "transfer"  carries game pieces from the intake to the launcher, a 312 RPM goBILDA motor
 *   "launcher"  flywheel, a 6000 RPM 1:1 motor (28 ticks/rev, as in Constants.Launcher)
 *
 * On screen the intake roller (front) and transfer belt (middle) turn green when running
 * forward and red when reversed; the launcher wheel (back) brightens with flywheel speed.
 *
 * BioBuzzV1 is the controller class for the "biobuzz_v1.fxml" markup file.
 */
@BotConfig(name = "Biobuzz V1", filename = "biobuzz_v1")
public class BioBuzzV1 extends MecanumPhysicsBase {

    public static final MotorType LAUNCHER_MOTOR_TYPE = MotorType.RevUltraPlanetaryOneToOne;
    public static final MotorType INTAKE_MOTOR_TYPE = MotorType.Gobilda192;
    public static final MotorType TRANSFER_MOTOR_TYPE = MotorType.Gobilda192;

    private static final Color LAUNCHER_IDLE = Color.DIMGRAY;
    private static final Color LAUNCHER_FULL = Color.ORANGERED;
    private static final Color ROLLER_IDLE = Color.DARKGRAY;
    private static final Color ROLLER_FORWARD = Color.LIMEGREEN;
    private static final Color ROLLER_REVERSE = Color.CRIMSON;

    private DcMotorExImpl launcher = null;
    private DcMotorExImpl intake = null;
    private DcMotorExImpl transfer = null;

    // Instantiated during loading via fx:id properties.
    @FXML Circle launcherWheel;
    @FXML Rectangle intakeRoller;
    @FXML Rectangle transferBelt;

    public BioBuzzV1() {
        super();
    }

    public void initialize() {
        super.initialize();
        hardwareMap.setActive(true);
        launcher = (DcMotorExImpl) hardwareMap.get(DcMotorEx.class, "launcher");
        intake = (DcMotorExImpl) hardwareMap.get(DcMotorEx.class, "intake");
        transfer = (DcMotorExImpl) hardwareMap.get(DcMotorEx.class, "transfer");
        hardwareMap.setActive(false);
    }

    protected void createHardwareMap() {
        super.createHardwareMap();
        // The drive motors use ports 0-3 of motorController0; these use motorController1.
        hardwareMap.put("launcher", new DcMotorExImpl(LAUNCHER_MOTOR_TYPE, motorController1, 0));
        hardwareMap.put("intake", new DcMotorExImpl(INTAKE_MOTOR_TYPE, motorController1, 1));
        hardwareMap.put("transfer", new DcMotorExImpl(TRANSFER_MOTOR_TYPE, motorController1, 2));
    }

    public synchronized void updateStateAndSensors(double millis) {
        super.updateStateAndSensors(millis);
        // Advance the game motors so their encoders and velocity track what the OpMode commands.
        launcher.update(millis);
        intake.update(millis);
        transfer.update(millis);
    }

    public synchronized void updateDisplay() {
        super.updateDisplay();

        double launcherFraction = Math.min(1.0,
                Math.abs(launcher.getVelocity()) / LAUNCHER_MOTOR_TYPE.MAX_TICKS_PER_SECOND);
        launcherWheel.setFill(LAUNCHER_IDLE.interpolate(LAUNCHER_FULL, launcherFraction));

        intakeRoller.setFill(rollerColor(intake.getPower()));
        transferBelt.setFill(rollerColor(transfer.getPower()));
    }

    /// Gray when stopped, shading to green running forward or red running in reverse.
    private static Color rollerColor(double power) {
        if (power > 0) return ROLLER_IDLE.interpolate(ROLLER_FORWARD, Math.min(1.0, power));
        if (power < 0) return ROLLER_IDLE.interpolate(ROLLER_REVERSE, Math.min(1.0, -power));
        return ROLLER_IDLE;
    }

    public void powerDownAndReset() {
        super.powerDownAndReset();
        launcher.stopAndReset();
        intake.stopAndReset();
        transfer.stopAndReset();
    }
}
