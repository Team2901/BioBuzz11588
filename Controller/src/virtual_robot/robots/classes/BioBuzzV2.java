package virtual_robot.robots.classes;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorExImpl;
import com.qualcomm.robotcore.hardware.configuration.MotorType;

import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.transform.Rotate;
import virtual_robot.controller.BotConfig;

/**
 * Team 2901's BioBuzz robot, version 2.
 *
 * Biobuzz V1 - the same drive, "intake", "transfer" and "launcher" - with the launcher mounted
 * on a turret. One more motor turns it:
 *
 *   "turret"    a 312 RPM goBILDA motor through a 5:1 reduction to the turret ring
 *               (2688 encoder ticks per turret revolution, about 62 RPM at full power)
 *
 * The turret can turn one full revolution: from 180 degrees clockwise to 180 degrees
 * counter-clockwise of facing forward, where it stops as if against a hard stop (a real turret
 * would be limited this way to keep from winding up its wiring). Positive power turns it
 * counter-clockwise. It starts facing forward, at encoder position 0, when the configuration
 * is loaded, and keeps its angle between OpMode runs as a real turret would.
 *
 * BioBuzzV2 is the controller class for the "biobuzz_v2.fxml" markup file.
 */
@BotConfig(name = "Biobuzz V2", filename = "biobuzz_v2")
public class BioBuzzV2 extends BioBuzzV1 {

    public static final MotorType TURRET_MOTOR_TYPE = MotorType.Gobilda192;
    public static final double TURRET_GEAR_RATIO = 5.0;  // motor revolutions per turret revolution
    public static final double TURRET_TICKS_PER_REV = TURRET_MOTOR_TYPE.TICKS_PER_ROTATION * TURRET_GEAR_RATIO;

    // Pivot of the turret in the 75x75 fxml coordinates; the center of the launcher wheel.
    private static final double TURRET_PIVOT_X = 37.5;
    private static final double TURRET_PIVOT_Y = 53.0;

    private DcMotorExImpl turretMotor = null;

    private final Rotate turretRotate = new Rotate(0, TURRET_PIVOT_X, TURRET_PIVOT_Y);

    // Instantiated during loading via fx:id properties.
    @FXML Group turret;

    public BioBuzzV2() {
        super();
    }

    public void initialize() {
        super.initialize();
        hardwareMap.setActive(true);
        turretMotor = (DcMotorExImpl) hardwareMap.get(DcMotorEx.class, "turret");
        hardwareMap.setActive(false);

        // One full revolution, half each way from facing forward.
        turretMotor.setActualPositionLimits(-TURRET_TICKS_PER_REV / 2, TURRET_TICKS_PER_REV / 2);
        turretMotor.setPositionLimitsEnabled(true);

        turret.getTransforms().add(turretRotate);
    }

    protected void createHardwareMap() {
        super.createHardwareMap();
        // Ports 0-2 of motorController1 are the launcher, intake and transfer.
        hardwareMap.put("turret", new DcMotorExImpl(TURRET_MOTOR_TYPE, motorController1, 3));
    }

    public synchronized void updateStateAndSensors(double millis) {
        super.updateStateAndSensors(millis);
        turretMotor.update(millis);
    }

    /// Turret angle in degrees, counter-clockwise from facing forward.
    public double getTurretDegrees() {
        return 360.0 * turretMotor.getActualPosition() / TURRET_TICKS_PER_REV;
    }

    public synchronized void updateDisplay() {
        super.updateDisplay();
        // JavaFX rotates clockwise for positive angles; the turret's positive is counter-clockwise.
        turretRotate.setAngle(-getTurretDegrees());
    }

    public void powerDownAndReset() {
        super.powerDownAndReset();
        turretMotor.stopAndReset();
    }
}
