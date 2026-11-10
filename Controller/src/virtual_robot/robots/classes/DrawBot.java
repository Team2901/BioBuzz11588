package virtual_robot.robots.classes;

import com.qualcomm.hardware.bosch.BNO055IMUImpl;
import com.qualcomm.hardware.bosch.BNO055IMUNew;
import com.qualcomm.robotcore.hardware.AnalogInput;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorExImpl;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.ServoImpl;
import com.qualcomm.robotcore.hardware.configuration.MotorType;

import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polyline;
import javafx.scene.transform.Rotate;
import virtual_robot.config.Config;
import virtual_robot.controller.BotConfig;
import virtual_robot.controller.Filters;
import virtual_robot.controller.VirtualBot;
import virtual_robot.controller.VirtualField;

/**
 * For internal use only. Represents the DrawBot: a chassis whose two drive motors ("goldDriveMotor"
 * and "greenDriveMotor") sit together on a single pod, plus a "turnMotor" that steers which way that
 * pod faces, and a potentiometer ("pot") reading the pod's actual angle.
 *
 * DrawBot is the controller class for the "draw_bot.fxml" markup file.
 *
 * The wheels are arranged in an X. The two driven wheels sit on one diagonal - green at the front
 * right, gold at the back left - and two passive casters on the other. turnMotor steers the driven
 * pair, each pivoting in place about its own axis and always pointing the same way; the casters just
 * swivel to follow.
 *
 * The two driven wheels are the same distance from the chassis center on opposite sides of it, so
 * equal power on both is two equal parallel forces whose torques about the center cancel: a pure
 * translation in whatever direction the wheels are pointed. Unequal power also turns the chassis, as
 * much as the wheels are lined up to push around the center: fully at a pod angle of 45 degrees
 * (wheels perpendicular to the line through both of them), not at all at -45 degrees (wheels
 * pointing along that line, where any difference just scrubs). Green forward with gold backward at
 * 45 degrees spins the bot counter-clockwise in place.
 *
 * TURRET ANGLE CONVENTION: 0 is straight forward (the LIME edge of the chassis), increasing
 * counter-clockwise when viewed from above, and it wraps continuously — the pod can spin forever.
 * Positive power on turnMotor steers counter-clockwise; if the real robot goes the other way, calling
 * turnMotor.setDirection(REVERSE) in the team's hardware class flips it here too, because the turret
 * angle is read from the motor's physical shaft position.
 *
 * The pen is raised and lowered by a servo ("penServo"): position 0 is up, 1 is down. It only draws
 * while it is down, and each time it goes down it starts a new line. The pen dot on the robot is dark
 * while the pen is down and pale while it is up. The drawing sits underneath the robot, and is cleared
 * when the OpMode stops and when the robot is repositioned with the mouse.
 */
@BotConfig(name = "Draw Bot", filename = "draw_bot")
public class DrawBot extends VirtualBot {

    /*
     * Rotations of turnMotor per rotation of the pod. At the default motor type this works out to
     * roughly 167 deg/sec of pod rotation at full power. Change this to match the real gearing.
     */
    private static final double TURN_GEAR_RATIO = 10.0;

    /*
     * Potentiometer calibration. These mirror POT_MAX_VOLTAGE and POT_FORWARD_VOLTAGE in the team's DrawBot hardware class: the
     * pot is a full-turn 3.3V unit whose wiper is positioned so that a pod pointing straight forward
     * reads 3.08V. Voltage climbs with turret angle and wraps through the pot's dead zone.
     */
    private static final double POT_MAX_VOLTAGE = 3.3;
    private static final double POT_VOLTAGE_AT_TURRET_ZERO = 3.08;

    // Stop the drawing from growing without bound during a long OpMode. Two doubles per point.
    private static final int MAX_TRAIL_POINTS = 5000;

    // The pen counts as down once penServo is past this position (0 = up, 1 = down).
    private static final double PEN_DOWN_THRESHOLD = 0.5;

    // Colour of the pen dot on the robot while the pen is down and while it is up.
    private static final Color PEN_DOWN_COLOR = Color.web("#111111");
    private static final Color PEN_UP_COLOR = Color.web("#d0d0d0");

    // Only extend the trail once the bot has actually moved this far (pixels), to keep the point list sane.
    private static final double MIN_TRAIL_SEGMENT_PIXELS = 1.0;

    /*
     * Where each wheel pivots, in the 75x75 coordinate space of draw_bot.fxml. These MUST match the
     * wheel positions in that file, or a wheel will spin about the wrong point. The driven pair is on
     * one diagonal of the X, the casters on the other.
     */
    private static final double GREEN_WHEEL_X = 53.0, GREEN_WHEEL_Y = 22.0;   // front right, driven
    private static final double GOLD_WHEEL_X = 22.0, GOLD_WHEEL_Y = 53.0;     // back left, driven
    private static final double CASTER_TL_X = 22.0, CASTER_TL_Y = 22.0;
    private static final double CASTER_BR_X = 53.0, CASTER_BR_Y = 53.0;

    // Size of draw_bot.fxml's coordinate space, and its center, which is the chassis center.
    private static final double FXML_SIZE = 75.0, FXML_CENTER = 37.5;

    // Below this speed (m/s) there is nothing to align a caster with, so it keeps the angle it had.
    private static final double CASTER_ALIGN_MIN_SPEED = 0.01;

    private final MotorType MOTOR_TYPE;

    private DcMotorExImpl turnMotor = null;
    private DcMotorExImpl greenDriveMotor = null;
    private DcMotorExImpl goldDriveMotor = null;
    private AnalogInput pot = null;
    private ServoImpl penServo = null;
    private BNO055IMUImpl imu = null;
    private BNO055IMUNew imuNew = null;

    // The four wheels, instantiated during loading via their fx:id properties.
    @FXML Group greenWheel;
    @FXML Group goldWheel;
    @FXML Group casterTopLeft;
    @FXML Group casterBottomRight;

    // The pen dot on the robot, loaded via its fx:id property.
    @FXML Circle pen;

    // Each wheel turns about its own pivot rather than about the middle of the robot.
    private final Rotate greenWheelRotate = new Rotate(0, GREEN_WHEEL_X, GREEN_WHEEL_Y);
    private final Rotate goldWheelRotate = new Rotate(0, GOLD_WHEEL_X, GOLD_WHEEL_Y);
    private final Rotate casterTopLeftRotate = new Rotate(0, CASTER_TL_X, CASTER_TL_Y);
    private final Rotate casterBottomRightRotate = new Rotate(0, CASTER_BR_X, CASTER_BR_Y);

    // Current angle of the driven wheels, radians, per the convention in the class comment above.
    private volatile double turretRadians = 0;

    // Which way the casters are lying, radians, in the same robot frame as turretRadians.
    private volatile double casterRadians = 0;

    private double wheelCircumference;      // pixels
    private double maxDriveForce;           // Newtons: most the floor can push the bot with
    private double maxDriveTorque;          // Newton-meters
    private double drivenWheelRadius;       // meters, from chassis center to each driven wheel

    // Every line the pen has drawn, one Polyline per time the pen went down.
    private Group penTrail = null;

    // The line being drawn now, or null while the pen is up. Only touched on the FX thread.
    private Polyline currentStroke = null;
    private int trailPointCount = 0;

    // Updated in updateStateAndSensors(), consumed in updateDisplay().
    private volatile boolean penDown = false;

    public DrawBot() {
        super();
        MOTOR_TYPE = Config.DEFAULT_DRIVE_MOTOR_TYPE;
    }

    public void initialize() {
        super.initialize();

        hardwareMap.setActive(true);
        turnMotor = (DcMotorExImpl) hardwareMap.get(DcMotorEx.class, "turnMotor");
        greenDriveMotor = (DcMotorExImpl) hardwareMap.get(DcMotorEx.class, "greenDriveMotor");
        goldDriveMotor = (DcMotorExImpl) hardwareMap.get(DcMotorEx.class, "goldDriveMotor");
        pot = hardwareMap.get(AnalogInput.class, "pot");
        penServo = (ServoImpl) hardwareMap.servo.get("penServo");
        imu = hardwareMap.get(BNO055IMUImpl.class, "imu");
        imuNew = hardwareMap.get(BNO055IMUNew.class, "imu");
        hardwareMap.setActive(false);

        // Wheel diameter is 1/4.5 of the bot width, matching the other physics-based bots.
        wheelCircumference = Math.PI * botWidth / 4.5;

        double mass = chassisBody.getMass().getMass();
        maxDriveForce = Config.FIELD_FRICTION_COEFF * 9.8 * mass;
        maxDriveTorque = maxDriveForce * halfBotWidth / VirtualField.PIXELS_PER_METER;

        drivenWheelRadius = Math.hypot(GREEN_WHEEL_X - FXML_CENTER, GREEN_WHEEL_Y - FXML_CENTER)
                * botWidth / FXML_SIZE / VirtualField.PIXELS_PER_METER;

        greenWheel.getTransforms().add(greenWheelRotate);
        goldWheel.getTransforms().add(goldWheelRotate);
        casterTopLeft.getTransforms().add(casterTopLeftRotate);
        casterBottomRight.getTransforms().add(casterBottomRightRotate);

        /*
         * The pen trail goes into the field pane here, during FXML load, which is before
         * VirtualRobotController.getVirtualBotInstance() calls setUpDisplayGroup(). That ordering
         * puts the trail underneath the robot for free.
         */
        penTrail = new Group();
        penTrail.setMouseTransparent(true);
        fieldPane.getChildren().add(penTrail);
    }

    protected void createHardwareMap() {
        hardwareMap = new HardwareMap();
        hardwareMap.put("turnMotor", new DcMotorExImpl(MOTOR_TYPE, motorController0, 0));
        hardwareMap.put("greenDriveMotor", new DcMotorExImpl(MOTOR_TYPE, motorController0, 1));
        hardwareMap.put("goldDriveMotor", new DcMotorExImpl(MOTOR_TYPE, motorController0, 2));
        hardwareMap.put("pot", new AnalogInput(POT_MAX_VOLTAGE));
        hardwareMap.put("penServo", new ServoImpl());
        hardwareMap.put("imu", new BNO055IMUImpl(this, 10));
        hardwareMap.put("imu", new BNO055IMUNew(this, 10));
    }

    public synchronized void updateStateAndSensors(double millis) {

        /*
         * Get updated position and heading from the dyn4j body (chassisBody)
         */
        x = chassisBody.getTransform().getTranslationX() * VirtualField.PIXELS_PER_METER;
        y = chassisBody.getTransform().getTranslationY() * VirtualField.PIXELS_PER_METER;
        headingRadians = chassisBody.getTransform().getRotationAngle();

        turnMotor.update(millis);
        greenDriveMotor.update(millis);
        goldDriveMotor.update(millis);

        penDown = penServo.getInternalPosition() > PEN_DOWN_THRESHOLD;

        /*
         * Pod angle follows the turn motor's physical shaft position, which already accounts for
         * setDirection() and MotorType.REVERSED. getActualPosition() grows with positive power, and
         * turret angle is counter-clockwise positive, so positive power steers counter-clockwise and
         * drives the pot voltage UP. Negating this would invert the feedback loop of any OpMode
         * steering on (target - current): the pod would run away from the target and park half a
         * turn away, where the wrapped error flips sign.
         */
        turretRadians = 2.0 * Math.PI * turnMotor.getActualPosition()
                / (MOTOR_TYPE.TICKS_PER_ROTATION * TURN_GEAR_RATIO);
        pot.update(potVoltageForTurretAngle(turretRadians));

        /*
         * Signed wheel speeds, in pixels/sec. The two driven wheels sit on opposite sides of the
         * chassis center facing opposite ways, so one motor has to be commanded REVERSE for both to
         * roll the same way. That is the same mirroring BasicTwoWheelPhysicsBase does for a left/right
         * pair, and with the team's hardware class (green REVERSE, gold FORWARD) both wheels read
         * positive here for positive power.
         */
        double greenWheelSpeed = greenDriveMotor.getVelocity(AngleUnit.RADIANS) * wheelCircumference / (2.0 * Math.PI);
        double goldWheelSpeed = goldDriveMotor.getVelocity(AngleUnit.RADIANS) * wheelCircumference / (2.0 * Math.PI);
        boolean greenRev = greenDriveMotor.getDirection() == DcMotorSimple.Direction.REVERSE;
        boolean goldRev = goldDriveMotor.getDirection() == DcMotorSimple.Direction.REVERSE;
        if (MOTOR_TYPE.REVERSED && greenRev || !MOTOR_TYPE.REVERSED && !greenRev) greenWheelSpeed = -greenWheelSpeed;
        if (MOTOR_TYPE.REVERSED && !goldRev || !MOTOR_TYPE.REVERSED && goldRev) goldWheelSpeed = -goldWheelSpeed;

        /*
         * Both driven wheels always point the same way, so their average drives the bot along the pod
         * direction.
         */
        double podSpeed = 0.5 * (greenWheelSpeed + goldWheelSpeed) / VirtualField.PIXELS_PER_METER;

        /*
         * Half the difference between them is how fast each wheel rolls around the center, in
         * opposite directions. Only the part of that roll perpendicular to the wheel's radius turns the
         * chassis; the rest scrubs. Green sits front right, at -45 degrees in the pod-angle frame, so
         * the perpendicular is 45 degrees and cos(turret - 45 degrees) is how much of the roll counts.
         * Green rolling forward then turns the bot counter-clockwise. This is the least-squares fit of
         * the wheels' rolling speeds with no sideways slip.
         */
        double spinSpeed = 0.5 * (greenWheelSpeed - goldWheelSpeed) / VirtualField.PIXELS_PER_METER;
        double targetAngularSpeed = spinSpeed * Math.cos(turretRadians - Math.PI / 4.0) / drivenWheelRadius;

        /*
         * Target velocity is podSpeed along the pod's world direction. Robot forward is +Y here, so a
         * pod angle of zero points along (-sin(heading), cos(heading)).
         */
        double podWorldAngle = headingRadians + turretRadians;
        Vector2 targetVelocity = new Vector2(-podSpeed * Math.sin(podWorldAngle), podSpeed * Math.cos(podWorldAngle));

        double t = millis / 1000.0;

        /*
         * Force and torque needed to reach those targets by the next physics update (F = ma, tau = I*alpha),
         * capped at what friction with the floor could actually deliver. With the wheels turning together
         * the target spin is zero, so the torque term also damps out spin picked up from collisions with
         * walls and game elements.
         */
        Vector2 force = targetVelocity.difference(chassisBody.getLinearVelocity())
                .product(chassisBody.getMass().getMass() / t);
        if (force.getMagnitude() > maxDriveForce) force.setMagnitude(maxDriveForce);

        double torque = (targetAngularSpeed - chassisBody.getAngularVelocity())
                * chassisBody.getMass().getInertia() / t;
        if (Math.abs(torque) > maxDriveTorque) torque = maxDriveTorque * Math.signum(torque);

        chassisBody.applyForce(force);
        chassisBody.applyTorque(torque);

        updateCasterAngle();

        imu.updateHeadingRadians(headingRadians);
        imuNew.updateHeadingRadians(headingRadians);
    }

    /**
     * Point the casters wherever the bot is actually travelling. They carry no load in the model and
     * steer nothing; they are here so the drawing tells the truth about which wheels push and which
     * merely follow. Below a crawl there is no travel direction to align with, so they stay put.
     */
    private void updateCasterAngle() {
        Vector2 velocity = chassisBody.getLinearVelocity();
        if (velocity.getMagnitude() < CASTER_ALIGN_MIN_SPEED) return;

        /*
         * Robot forward is +Y, so the chassis's forward direction sits at headingRadians + PI/2 in
         * the world frame. Subtracting that turns a world travel direction into the same
         * robot-relative frame turretRadians uses: 0 is straight ahead, counter-clockwise positive.
         */
        casterRadians = Math.atan2(velocity.y, velocity.x) - headingRadians - Math.PI / 2.0;
    }

    /**
     * Voltage the potentiometer reports for a given pod angle. This is the inverse of
     * potVoltageToDegrees() in the team's DrawBot hardware class: forward reads POT_VOLTAGE_AT_TURRET_ZERO, and voltage
     * climbs with angle, wrapping back to zero through the pot's dead zone.
     */
    private static double potVoltageForTurretAngle(double radians) {
        double degrees = Math.toDegrees(radians) % 360.0;
        if (degrees < 0) degrees += 360.0;
        return (POT_VOLTAGE_AT_TURRET_ZERO + degrees / 360.0 * POT_MAX_VOLTAGE) % POT_MAX_VOLTAGE;
    }

    public synchronized void updateDisplay() {
        super.updateDisplay();

        // Negated because screen Y is flipped relative to field Y, as in DiffSwerveBot.
        double drivenWheelAngle = -Math.toDegrees(turretRadians);
        greenWheelRotate.setAngle(drivenWheelAngle);
        goldWheelRotate.setAngle(drivenWheelAngle);

        double casterAngle = -Math.toDegrees(casterRadians);
        casterTopLeftRotate.setAngle(casterAngle);
        casterBottomRightRotate.setAngle(casterAngle);

        pen.setFill(penDown ? PEN_DOWN_COLOR : PEN_UP_COLOR);

        extendPenTrail();
    }

    /**
     * Add the bot's current position to the line being drawn, in field pane coordinates, if the pen is
     * down. Lifting the pen ends the line; lowering it starts a new one. Must run on the FX thread;
     * updateDisplay() is the only caller.
     */
    private void extendPenTrail() {
        if (penTrail == null) return;

        if (!penDown) {
            currentStroke = null;
            return;
        }

        if (currentStroke == null) {
            currentStroke = new Polyline();
            currentStroke.setStroke(Color.CRIMSON);
            currentStroke.setStrokeWidth(1.5);
            penTrail.getChildren().add(currentStroke);
        }

        double penX = x + VirtualField.HALF_FIELD_WIDTH;
        double penY = VirtualField.HALF_FIELD_WIDTH - y;

        ObservableList<Double> points = currentStroke.getPoints();
        int n = points.size();
        if (n >= 2) {
            double dx = penX - points.get(n - 2);
            double dy = penY - points.get(n - 1);
            if (dx * dx + dy * dy < MIN_TRAIL_SEGMENT_PIXELS * MIN_TRAIL_SEGMENT_PIXELS) return;
        }

        points.addAll(penX, penY);
        trailPointCount++;
        if (trailPointCount > MAX_TRAIL_POINTS) removeOldestTrailPoint();
    }

    /**
     * Drop the first point of the oldest line, and the line itself once it is empty.
     */
    private void removeOldestTrailPoint() {
        Polyline oldest = (Polyline) penTrail.getChildren().get(0);
        oldest.getPoints().remove(0, 2);
        trailPointCount--;
        if (oldest.getPoints().isEmpty() && oldest != currentStroke) penTrail.getChildren().remove(0);
    }

    /**
     * Erase the drawing. Safe to call from the OpMode thread.
     */
    private void clearPenTrail() {
        if (penTrail == null) return;
        if (Platform.isFxApplicationThread()) clearPenTrailNow();
        else Platform.runLater(this::clearPenTrailNow);
    }

    private void clearPenTrailNow() {
        penTrail.getChildren().clear();
        currentStroke = null;
        trailPointCount = 0;
    }

    public void powerDownAndReset() {
        turnMotor.stopAndReset();
        greenDriveMotor.stopAndReset();
        goldDriveMotor.stopAndReset();
        imu.close();
        chassisBody.setLinearVelocity(0, 0);
        chassisBody.setAngularVelocity(0);
        clearPenTrail();
    }

    /**
     * Repositioning the bot by hand would otherwise draw a straight line across the field from
     * wherever it used to be, so start a fresh drawing instead.
     */
    @Override
    public synchronized void positionWithMouseClick(MouseEvent arg) {
        super.positionWithMouseClick(arg);
        clearPenTrail();
    }

    /**
     * The pen trail is a child of the field pane in its own right, so it has to be taken out along
     * with the display group when the configuration changes.
     */
    @Override
    public void removeFromDisplay(Pane fieldPane) {
        super.removeFromDisplay(fieldPane);
        if (penTrail != null) fieldPane.getChildren().remove(penTrail);
    }

    /**
     *  Set up the chassisBody and add it to the dyn4j world. This method creates a Body and adds a
     *  BodyFixture containing a Rectangle.
     *     Large mass: collisions with lightweight game elements will have negligible effect on bot motion
     *     The "friction" of 0 refers to robot-game element and robot-wall friction (NOT robot-floor)
     *     The "restitution" of 0 refers to "bounce" when robot collides with wall and game elements
     *
     *  The filter set on the chassisFixture indicates what other things the robot is capable of colliding with
     */
    public void setUpChassisBody() {
        chassisBody = new Body();
        chassisBody.setUserData(this);
        double botWidthMeters = botWidth / VirtualField.PIXELS_PER_METER;
        chassisFixture = chassisBody.addFixture(
                new org.dyn4j.geometry.Rectangle(botWidthMeters, botWidthMeters), 71.76, 0, 0);
        chassisRectangle = (org.dyn4j.geometry.Rectangle) chassisFixture.getShape();
        chassisFixture.setFilter(Filters.CHASSIS_FILTER);
        chassisBody.setMass(MassType.NORMAL);
        world.addBody(chassisBody);
    }

}
