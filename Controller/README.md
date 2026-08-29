# Virtual Robot simulator (`:Controller`)

A 2D simulator that runs the team's OpModes on a PC, forked from
[`Beta8397/virtual_robot`](https://github.com/Beta8397/virtual_robot). It compiles
the *same* files as the Android `:TeamCode` module, so an OpMode written for the
robot runs here without modification — and vice versa.

Run it with the **`Run Simulator`** configuration in Android Studio, or:

```
./gradlew :Controller:run
```

Gradle needs a JDK 17+ to run, which Android Studio's bundled JBR already satisfies.
There is no separate JDK to install and no setup script.

## What it can do

**Run your OpModes unchanged.** Write them in `org.firstinspires.ftc.teamcode`,
annotate `@TeleOp` / `@Autonomous`, extend `OpMode` / `LinearOpMode`. They appear in
the simulator's Op Mode dropdown and on the Driver Station, from one copy of the
source. Nothing in `TeamCode` is excluded from the simulator build.

**Drive a robot with real physics.** Motor powers turn into motion through a rigid
body physics engine (dyn4j), including momentum, wheel slip and collisions with
field elements. Encoders, the IMU and the odometry computer report values derived
from that simulated motion, not from dead reckoning.

**Follow PedroPathing paths for real.** The simulator uses the *same* PedroPathing
artifacts the robot does, so autonomous paths genuinely run: the follower computes
corrections, the drive moves the robot, and the simulated Pinpoint feeds the pose
back. `Tuning` and its selector menu work here too. This is the main reason to
simulate — you can see whether a path clips an obstacle or ends facing the wrong
way before trusting it on the field. Keep `pedroPathingVersion` and
`pedroPathingTelemetryVersion` in `build.gradle` in sync with
`build.dependencies.gradle`, so the simulator and the robot stay on one version.

**Report the active robot configuration.** `ConfigUtilities.getRobotConfigurationName()`
returns whatever is selected in the **Configuration** dropdown (`"Mecanum Bot"`,
`"Claw Bot"`, …), so code that branches on the configuration name behaves sensibly
in both places.

**Fail like a real robot when hardware is missing.** Each configuration declares its
own devices. Asking for one it does not have raises the same error the Robot
Controller raises when the Control Hub is configured without it.

## What it cannot do

**See anything.** There is no camera. The Limelight is a stand-in that reports "not
connected" and never returns a result, so vision OpModes always take their no-target
path. AprilTag detection, colour blob detection and Limelight pipelines cannot be
tested here.

**Show a Panels dashboard.** Panels (`com.bylazar`) is a stand-in: `@Configurable`
fields keep their source values (you cannot tune them live), field drawing is
discarded, and Panels telemetry is forwarded to the simulator's own telemetry
window. FTC Dashboard is likewise a stand-in.

**Substitute for tuning on the real robot.** The simulated robot is not your robot.
`pedroPathing/Constants.java` describes the physical machine — its mass, PIDF gains
and track width — so path *tracking* here will not match the field. Check path
geometry and autonomous logic in the simulator; tune on the robot. Numbers produced
by the tuners here describe the simulated robot and should not be copied into
`Constants.java`.

**Model your robot's mechanisms.** The simulated robots are generic chassis. Your
intake, launcher, lift or claw does not exist, and motors configured for them fall
back to mock devices that accept commands and do nothing.

**Track the FTC SDK exactly.** `Controller/src` holds a hand-written approximation of
the SDK, not the real thing. It covers what the team's code uses; an SDK class or
method that has never been needed here may simply be absent. Adding it is usually a
small edit — see the existing classes under `com/qualcomm/` for the pattern.

## Excluding an OpMode from the simulator

Nothing in `TeamCode` is excluded today — every OpMode builds for both targets from a
single shared copy of the source, with no duplicated or simulator-specific team files.

If you write an OpMode the simulator cannot compile — usually because it uses a
library or an FTC SDK class the approximation above does not provide — add its path
to the **`simIncompatibleOpModes`** list in `build.gradle`:

```groovy
def simIncompatibleOpModes = [
        'org/firstinspires/ftc/teamcode/teleop/MyNewTeleop.java',
]
```

Paths are relative to `TeamCode/src/main/java`, and `**` wildcards work for whole
folders. Excluding a file only keeps it out of the **simulator** build; it still
builds and deploys to the robot exactly as before. Say why in a comment, so the entry
can be dropped once the gap is filled.

## How the build works, and how it differs from the robot's

Both targets are Gradle subprojects of the same project, listed in `settings.gradle`,
but they are built in completely different ways.

| | `:FtcRobotController` + `:TeamCode` | `:Controller` |
|---|---|---|
| Plugin | `com.android.application` | `application` + `org.openjfx.javafxplugin` |
| Output | an Android APK, installed on the Control Hub | a desktop JavaFX app, run on your PC |
| FTC SDK | real AARs, `org.firstinspires.ftc:*:11.2.1` | the source approximation in `src/` |
| Java level | 8 (`build.common.gradle`) | whatever JVM runs Gradle (JBR 21 in Android Studio) |
| Shared config | `build.common.gradle`, `build.dependencies.gradle` | none - this module stands alone |

The two never share a classpath. `:Controller` deliberately does **not** depend on
`:FtcRobotController` and never sees the real FTC artifacts, so its `com.qualcomm.*`
symbols resolve only to the approximation in `src/`. That is what keeps two
definitions of the same class from colliding: each target compiles the same OpMode
source against a different definition of the SDK. It is also why the FTC artifacts
could not simply be reused here - they are Android AARs, which a plain-Java module
cannot consume at all.

### The shared OpModes are copied, not referenced

`:TeamCode` compiles `TeamCode/src/main/java` directly. `:Controller` cannot also
point a source root at that folder: Android Studio requires every source root to
belong to exactly one module, and sharing it makes the IDE report *"Duplicate content
roots detected"* and pull the folder out of `:TeamCode`.

Instead a `Sync` task copies the sim-compatible OpModes into `build/shared-opmodes`,
a generated folder the IDE ignores, and that is the simulator's source root. Keep
editing OpModes in `TeamCode/src/main/java` as normal - the copy refreshes on every
`:Controller` build. If you ever go looking, compile errors from the simulator name
files under `build/shared-opmodes/...`; the file to fix is the original in `TeamCode`.

### PedroPathing is unpacked from its AAR

`:TeamCode` just declares `com.pedropathing:ftc` and Android handles the AAR.
`:Controller` cannot, so `extractPedroPathingFtc` and `extractPedroPathingTelemetry`
pull `classes.jar` out of each AAR and put that on the classpath; `com.pedropathing:core`
is a plain jar and is used as-is. The versions are declared at the top of
`build.gradle` and must match `build.dependencies.gradle`.

### Building it

`./gradlew :Controller:run` runs, in order:

```
extractPedroPathingFtc  ->  syncSharedOpModes  ->  compileJava  ->  processResources  ->  run
extractPedroPathingTelemetry
```

`processResources` matters more than it looks: the FXML layouts, the field `.bmp`
images and the robot geometry live next to the source under `src/`, and are packaged
from there.

Building or deploying the robot app is untouched by any of this - `:TeamCode` has no
dependency on `:Controller`, so `assembleDebug` behaves exactly as it did before the
simulator existed.

## Robot configurations

Chosen from the **Configuration** dropdown before pressing INIT. Position the robot
by left-clicking the field (position) and right-clicking (heading).

Mecanum Bot, MecDynamic Bot, Arm Bot, Freight Bot, QQ Bot and Ulti Bot are mecanum
drives and the closest match to the team's competition robot. They carry
`frontLeft` / `frontRight` / `backLeft` / `backRight`, an `imu`, an `odo` Pinpoint,
`sensor_otos`, `octoquad`, a `color_sensor`, four distance sensors and a `limelight`.

Also available: Claw Bot, Two Wheel Bot, XDrive Bot, Kiwi Bot, Swerve Bot,
Differential Swerve Bot, Square Omni Bot, Turret Bot, and a Programming Board that
does not drive but carries a motor, servo, potentiometer, touch sensor and
colour-distance sensor.

## Settings

`Controller/src/virtual_robot/config/Config.java` holds the run-time options —
virtual versus real gamepads (`USE_VIRTUAL_GAMEPAD`), field size and image, and the
game (currently Decode; `new NoGame()` removes the field obstacles).

The sliders beside the field inject random and systematic motor error and motor
inertia, which is useful for checking that an autonomous routine is not relying on
perfectly accurate motors.

## Layout

| Path | Contents |
|---|---|
| `src/virtual_robot/` | the simulator: JavaFX UI, physics, robot configurations, game elements |
| `src/com/qualcomm/`, `src/org/firstinspires/` | the FTC SDK approximation |
| `src/com/qualcomm/hardware/limelightvision/`, `src/com/bylazar/` | Limelight and Panels stand-ins |
| `src/android/`, `src/androidx/`, `src/org/json/` | just enough Android to compile SDK-style code off-device |
| `libs/` | dyn4j, Reflections, Jamepad, Guava, Javassist |
| `LEGAL/` | third-party licences — see `LEGAL/README.md` |

OpModes are discovered by scanning the classpath at run time for `@TeleOp` and
`@Autonomous`, the same annotations the Robot Controller uses. There is no
registration file.
