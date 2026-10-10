package virtual_robot.robots.classes;

import com.qualcomm.hardware.CommonOdometry;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriverInternal;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorExImpl;
import com.qualcomm.robotcore.hardware.ServoImpl;
import com.qualcomm.robotcore.hardware.configuration.MotorType;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import org.dyn4j.geometry.Vector2;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import virtual_robot.controller.BotConfig;
import virtual_robot.controller.VirtualField;

/**
 * For internal use only. Represents a robot with two drive wheels (differential drive),
 * a motor-driven arm, a servo-driven claw on the end of the arm, and a goBILDA Pinpoint
 * odometry computer.
 *
 * ClawBot extends BasicTwoWheelPhysicsBase, which manages the two-wheel drive train and its
 * physics. In addition to the drive motors, ClawBot therefore also inherits a BNO055 IMU
 * from that base class.
 *
 * Hardware map names:
 *   leftDrive, rightDrive - drive motors (provided by BasicTwoWheelPhysicsBase)
 *   imu                   - BNO055 IMU (provided by BasicTwoWheelPhysicsBase)
 *   arm                   - raises / lowers the arm
 *   claw                  - 0.0 = claw fully open, 1.0 = claw fully closed
 *   pinpoint              - goBILDA Pinpoint odometry computer (GoBildaPinpointDriver)
 *
 * The Pinpoint reports the robot's pose, velocity and simulated encoder counts exactly as it
 * does on the mecanum bots. As on a real robot, its values only change when the OpMode calls
 * pinpoint.update() each loop. Note that a differential drive cannot strafe, so the Y (strafe)
 * pod will only see movement caused by turning or collisions.
 *
 * The arm and claw are visual only: they are JavaFX transforms driven by the arm
 * encoder position and the servo position, and do not physically interact with game
 * elements. See ArmBot for an example that adds dyn4j Bodys so that accessories can
 * grab game elements.
 *
 * The @BotConfig annotation is required. "name" is what the user sees in the
 * Configuration combo box; "filename" is the fxml file (in virtual_robot/robots/fxml)
 * that holds the graphical representation, with ClawBot as its fx:controller.
 */
@BotConfig(name = "Claw Bot", filename = "claw_bot")
public class ClawBot extends BasicTwoWheelPhysicsBase {

    // Arm encoder travel, in ticks, that corresponds to a full arm sweep (Neverest40 = 1120 ticks/rev).
    private static final double ARM_TICKS_FULL_TRAVEL = 1120.0;
    // Angle, in degrees, that the arm sweeps through over ARM_TICKS_FULL_TRAVEL.
    private static final double ARM_SWEEP_DEGREES = 135.0;
    // Maximum travel of each claw finger, in fxml pixels, from fully open to fully closed.
    private static final double CLAW_FINGER_TRAVEL = 6.0;

    private DcMotorExImpl armMotor = null;
    private ServoImpl clawServo = null;

    // Shared odometry model that feeds the simulated Pinpoint. CommonOdometry is reset by the
    // controller before each new robot configuration is built, so this is a fresh instance.
    private final CommonOdometry odo = CommonOdometry.getInstance();
    private GoBildaPinpointDriverInternal pinpoint = null;

    // Previous world-frame velocity, used to estimate acceleration for the odometry model.
    private Vector2 prevLinearVelocity = new Vector2(0, 0);
    private double prevAngularVelocity = 0;

    // Instantiated automatically during loading of claw_bot.fxml via their fx:id attributes.
    @FXML private Group armGroup;
    @FXML private Rectangle leftClaw;
    @FXML private Rectangle rightClaw;

    // Transforms created in initialize() and manipulated in updateDisplay().
    private Rotate armRotate;
    private Translate leftClawTranslate;
    private Translate rightClawTranslate;

    // Updated in updateStateAndSensors(), consumed in updateDisplay().
    private double armAngleDegrees = 0;
    private double clawClosedFraction = 0;

    public ClawBot() {
        super();
    }

    public void initialize() {
        // Handles createHardwareMap(), the chassis body, and all two-wheel drive-base setup.
        super.initialize();

        // Temporarily activate the hardware map so we can "get" the accessory hardware.
        hardwareMap.setActive(true);

        armMotor = (DcMotorExImpl) hardwareMap.get(DcMotorEx.class, "arm");
        armMotor.setActualPositionLimits(0, ARM_TICKS_FULL_TRAVEL);
        armMotor.setPositionLimitsEnabled(true);

        clawServo = (ServoImpl) hardwareMap.servo.get("claw");

        pinpoint = hardwareMap.get(GoBildaPinpointDriverInternal.class, "pinpoint");

        hardwareMap.setActive(false);

        // Rotates the whole arm/claw group about the center of the robot (37.5, 37.5).
        armRotate = new Rotate(0, 37.5, 37.5);
        armGroup.getTransforms().add(armRotate);

        // Translate each claw finger along X to open / close the claw.
        leftClawTranslate = new Translate(0, 0);
        leftClaw.getTransforms().add(leftClawTranslate);
        rightClawTranslate = new Translate(0, 0);
        rightClaw.getTransforms().add(rightClawTranslate);
    }

    protected void createHardwareMap() {
        // Adds leftDrive, rightDrive and the IMU.
        super.createHardwareMap();

        // Drive motors occupy ports 0 and 1 of motorController0, so put the arm motor on motorController1.
        hardwareMap.put("arm", new DcMotorExImpl(MotorType.Neverest40, motorController1, 0));
        hardwareMap.put("claw", new ServoImpl());
        hardwareMap.put("pinpoint", new GoBildaPinpointDriverInternal());
    }

    public synchronized void updateStateAndSensors(double millis) {
        // Handles the drive train and the standard sensors.
        super.updateStateAndSensors(millis);

        armMotor.update(millis);
        armAngleDegrees = armMotor.getActualPosition() / ARM_TICKS_FULL_TRAVEL * ARM_SWEEP_DEGREES;

        // Servo position maps directly to how far the claw is closed (0 = open, 1 = closed).
        clawClosedFraction = clawServo.getInternalPosition();

        updateOdometry(millis);
    }

    /**
     * Feed the current chassis pose, velocity and acceleration (world frame, meters / radians)
     * into CommonOdometry, which the Pinpoint reads from when the OpMode calls update().
     * It isn't necessary to update the Pinpoint itself here; that is the user's responsibility.
     */
    private void updateOdometry(double millis) {
        double xMeters = chassisBody.getTransform().getTranslationX();
        double yMeters = chassisBody.getTransform().getTranslationY();
        double heading = chassisBody.getTransform().getRotationAngle();

        Vector2 vel = chassisBody.getLinearVelocity().copy();
        double angVel = chassisBody.getAngularVelocity();

        // The two-wheel base doesn't expose its applied force, so estimate acceleration
        // from the change in velocity since the previous update.
        double t = millis / 1000.0;
        Vector2 accel = t > 0 ? vel.difference(prevLinearVelocity).quotient(t) : new Vector2(0, 0);
        double angAccel = t > 0 ? (angVel - prevAngularVelocity) / t : 0;
        prevLinearVelocity = vel;
        prevAngularVelocity = angVel;

        odo.update(
                new Pose2D(DistanceUnit.METER, xMeters, yMeters, AngleUnit.RADIANS, heading),
                new Pose2D(DistanceUnit.METER, vel.x, vel.y, AngleUnit.RADIANS, angVel),
                new Pose2D(DistanceUnit.METER, accel.x, accel.y, AngleUnit.RADIANS, angAccel)
        );
    }

    /**
     * Push the robot's current (stationary) pose into the odometry model.
     */
    private void updateOdometryAtRest() {
        prevLinearVelocity = new Vector2(0, 0);
        prevAngularVelocity = 0;
        odo.update(
                new Pose2D(DistanceUnit.METER, x / VirtualField.PIXELS_PER_METER, y / VirtualField.PIXELS_PER_METER,
                        AngleUnit.RADIANS, headingRadians),
                new Pose2D(DistanceUnit.METER, 0, 0, AngleUnit.RADIANS, 0),
                new Pose2D(DistanceUnit.METER, 0, 0, AngleUnit.RADIANS, 0)
        );
    }

    public synchronized void updateDisplay() {
        // Positions and orients the robot on the field.
        super.updateDisplay();

        armRotate.setAngle(armAngleDegrees);
        leftClawTranslate.setX(CLAW_FINGER_TRAVEL * clawClosedFraction);
        rightClawTranslate.setX(-CLAW_FINGER_TRAVEL * clawClosedFraction);
    }

    public void powerDownAndReset() {
        super.powerDownAndReset();
        armMotor.stopAndReset();
        updateOdometryAtRest();
        pinpoint.update();
    }

    /**
     * When the robot is placed by clicking the field, make that spot the Pinpoint's new
     * origin (0, 0, heading 0) and zero its encoders, matching the mecanum bots.
     */
    @Override
    public synchronized void positionWithMouseClick(MouseEvent arg) {
        super.positionWithMouseClick(arg);
        updateOdometryAtRest();
        odo.setPosition(new Pose2D(DistanceUnit.METER, 0, 0, AngleUnit.RADIANS, 0));
        pinpoint.internalUpdate(false, false);
        pinpoint.resetEncoders();
    }

}
