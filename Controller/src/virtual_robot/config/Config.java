package virtual_robot.config;

import com.qualcomm.robotcore.hardware.configuration.MotorType;
import javafx.scene.image.Image;
import virtual_robot.controller.Game;
import virtual_robot.controller.VirtualBot;
import virtual_robot.robots.classes.BioBuzzV1;
import virtual_robot.games.NoGame;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Class for configuring the field (width and image) and the game.
 */
public class Config {
    /**
     * FRACTION OF TOTAL FIELD TO CONSTRAIN ROBOT MOTION
     */
    public static final double X_MIN_FRACTION = 0; //0 for WHOLE FIELD OR BLUE REMOTE, 0.3333 for RED REMOTE FIELD
    public static final double X_MAX_FRACTION = 1; //1 for WHOLE FIELD OR RED REMOTE, 0.6667 for BLUE REMOTE FIELD
    public static final double Y_MIN_FRACTION = 0;  //Leave this alone for Ultimate Goal
    public static final double Y_MAX_FRACTION = 1;  //Leave this alone for Ultimate Goal

    /**
     *  Width of the field, in pixels
     */
    public static final double FIELD_WIDTH = 648;

    /**
     * The image object for the field.
     */
    public static final Image BACKGROUND = new Image("/virtual_robot/assets/biobuzzFieldCoords648.bmp");

    /**
     * If true, Virtual Gamepad joysticks and triggers will stay in the position where they were
     * released by default. If false, the default behavior will be to "snap back" to zero.
     *
     * But if the SHIFT or ALT key is down when the control is released, the behavior will be the opposite of the
     * default behavior.
     */
    public static final boolean HOLD_CONTROLS_BY_DEFAULT = false;

    /**
     * Define the game.  This must match the forGame attribute of the GameElementConfig annotation
     * on the VirtualGameElement implementations.
     */
    public static final Game GAME = new NoGame();

    /**
     * Friction coefficient between field and robot wheels. A very high value will minimize the effect
     * of collisions on robot kinetics, but will allow robot to push game elements substantially into
     * the wall.
     */
    public static final double FIELD_FRICTION_COEFF = 10;

    /**
     *  Default type for robot drive motors. Change this if you want something else.
     */
    public static final MotorType DEFAULT_DRIVE_MOTOR_TYPE = MotorType.Gobilda192;

    /**
     *  Default Robot Configuration
     */
    public static final Class<? extends VirtualBot> DEFAULT_BOT = BioBuzzV1.class;

    /**
     * Which of the PRESET_POSES below every robot configuration starts at when it is loaded.
     * Must be one of their names exactly, e.g. "Origin" or "Red Audience".
     */
    public static final String DEFAULT_PRESET_POSE = "Origin";

    /**
     * Poses offered by the simulator's Move Robot dropdown, in the order listed. They are in the
     * same frame as the Robot Pose readout: origin at field centre, X right (toward the audience),
     * Y up the screen (toward the BLUE side), heading 0 facing +X, counter-clockwise positive.
     * The four starting poses are the Decode alliance starting spots; the simulator does not read
     * team code, so keep them in step with any starting poses the team's autonomous uses.
     */
    public static final Map<String, Pose2D> PRESET_POSES;
    static {
        Map<String, Pose2D> poses = new LinkedHashMap<>();
        poses.put("Origin", pose(0, 0, 0));
        poses.put("Red Audience", pose(72 - 9, -12, 180));
        poses.put("Red Far", pose(-72 + 9, -12, 0));
        poses.put("Blue Audience", pose(72 - 9, 12, 180));
        poses.put("Blue Far", pose(-72 + 9, 12, 0));
        PRESET_POSES = Collections.unmodifiableMap(poses);
    }

    /**
     * The pose named by DEFAULT_PRESET_POSE. The robot is placed there exactly as a drag would
     * place it, so attached arms follow and the odometry reports this pose.
     */
    public static final Pose2D STARTING_POSE = PRESET_POSES.get(DEFAULT_PRESET_POSE);
    static {
        if (STARTING_POSE == null) {
            throw new IllegalStateException("Config.DEFAULT_PRESET_POSE is \"" + DEFAULT_PRESET_POSE
                    + "\", which is not one of the PRESET_POSES: " + PRESET_POSES.keySet());
        }
    }

    private static Pose2D pose(double xInches, double yInches, double headingDegrees) {
        return new Pose2D(DistanceUnit.INCH, xInches, yInches, AngleUnit.DEGREES, headingDegrees);
    }
}
