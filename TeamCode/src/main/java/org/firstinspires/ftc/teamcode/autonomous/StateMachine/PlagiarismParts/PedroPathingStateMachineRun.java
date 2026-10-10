package org.firstinspires.ftc.teamcode.autonomous.StateMachine.PlagiarismParts;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.autonomous.AbstractAutonomous;

@Autonomous // Change from abstract when plagiarizing this is just so it compiles
public abstract class PedroPathingStateMachineRun extends AbstractAutonomous {
    /*
    ElapsedTime timer = new ElapsedTime();
    CanOpen canOpen = new CanOpen();
    Pose startingPose = new Pose(121, 120.5, Math.toRadians(45));
    Pose farStartingPose = new Pose(96, 12, Math.toRadians(90));
    Pose farStartingPoseShoot = new Pose(84, 12, Math.toRadians(68.19859));
    Pose farEndPose = new Pose(96, 36, Math.toRadians(180));
    Pose obeliskView = new Pose(96, 96, Math.toRadians(120));
    Pose shootPose = new Pose(96, 96, Math.toRadians(45));
    Pose firstPickUpStart = new Pose(96, 82, Math.toRadians(180));
    Pose firstPickUpEnd = new Pose(122.5, 82, Math.toRadians(180));
    Pose secondPickUpStart = new Pose(96, 59, Math.toRadians(180));
    Pose secondPickUpEnd = new Pose(122.5, 59, Math.toRadians(180));
    Pose park = new Pose(115, 72, Math.toRadians(0));
    Pose nearEndPose = new Pose(84, 132, Math.toRadians(270));
    Pose moveBasic = new Pose(96, 12, Math.toRadians(180));

    enum Alliance {
        RED,
        BLUE
    }

    enum StartPos {
        NEAR,
        FAR,
        SAFETY_FAR,
        EMERGENCY_FAR,
        FAR_SHOOT
    }

    Alliance alliance = Alliance.RED;
    StartPos startPos = StartPos.NEAR;
    int targetTime = 10;
    @IgnoreConfigurable
    static PoseHistory poseHistory;

    @Override
    public void runOpMode() throws InterruptedException {
        robot.init(this.hardwareMap, telemetry);

        while (!isStarted() && !isStopRequested()) {
            if (gamepad1.x) {
                alliance = Alliance.BLUE;
            }
            if (gamepad1.b) {
                alliance = Alliance.RED;
            }
            if (gamepad1.y) {
                startPos = StartPos.FAR;
            }
            if (gamepad1.a) {
                startPos = StartPos.NEAR;
            }
            if (gamepad1.dpad_down) {
                startPos = StartPos.EMERGENCY_FAR;
            }
            if (gamepad1.dpad_up) {
                startPos = StartPos.SAFETY_FAR;
            }
            if (gamepad1.dpad_right) {
                startPos = StartPos.FAR_SHOOT;
            }
            if (gamepad1.rightBumperWasPressed()){
                targetTime += 5;
            }
            if (gamepad1.leftBumperWasPressed()){
                targetTime -= 5;
            }
            telemetry.addLine("Remember load balls PGP");
            telemetry.addLine("Select Alliance:");
            telemetry.addLine("X = BLUE");
            telemetry.addLine("B = RED");
            telemetry.addLine("Select Starting Pos:");
            telemetry.addLine("Y = FAR");
            telemetry.addLine("A = NEAR");
            telemetry.addLine("dpad Up = SAFETY FAR");
            telemetry.addLine("dpad Down = EMERGENCY FAR");
            telemetry.addLine("dpad Right = Far Shoot");

            telemetry.addData("Current Alliance", alliance);
            telemetry.addData("Current Start Pos:", startPos);
            if (startPos == StartPos.FAR){
                telemetry.addLine("Edit Wait Time:");
                telemetry.addLine("RB: +5 sec");
                telemetry.addLine("LB: -5 sec");
                telemetry.addData("Wait Amount:", targetTime);
            }
            telemetry.update();
        }

        Follower follower = Constants.createFollower(hardwareMap);
        setPose();
        if (startPos == StartPos.FAR || startPos == StartPos.SAFETY_FAR) {
            startingPose = farStartingPose;
        }
        if(startPos == StartPos.FAR_SHOOT){
            startingPose = farStartingPoseShoot;
        }
        follower.setStartingPose(startingPose);
        follower.update();
        ArrayList<MyState> shootingStates = new ArrayList<>(Arrays.asList(
                new Idle(),
                new SpinUp(robot),
                new WaitToOpen(canOpen, telemetry),
                new OpenGate(robot),
                new CarouselMove(robot, 3),
                new Roll(),
                new EndRoll(robot)));
        ArrayList<MyState> movingStates;
        ShootingStateMachine shootSystem = new ShootingStateMachine(shootingStates, telemetry);
        if (startPos == StartPos.FAR) { // Goes to shoot preload after waiting, then goes back
            movingStates = new ArrayList<>(Arrays.asList(
                    new waitUntil(targetTime),
                    new PedroPathingMove(follower, shootPose),
                    new Shoot(shootSystem, canOpen),
                    new PedroPathingMove(follower, farEndPose)));
        } else if (startPos == StartPos.SAFETY_FAR){ // Goes to shoot preload, then goes towards near wall
            movingStates = new ArrayList<>(Arrays.asList(
                    new PedroPathingMove(follower, shootPose),
                    new Shoot(shootSystem, canOpen),
                    new PedroPathingMove(follower, nearEndPose)));
        } else if (startPos == StartPos.EMERGENCY_FAR) { // moves to the right a bit
            movingStates = new ArrayList<>(Arrays.asList(
                    new PedroPathingMove(follower, farEndPose)));
        }else if(startPos == StartPos.FAR_SHOOT){
            canOpen.value = true;
            robot.defaultVelocity = 2200;
            movingStates = new ArrayList<>(Arrays.asList(
                    new Shoot(shootSystem, canOpen),
                    new PedroPathingMove(follower, farEndPose)));
        } else {
            movingStates = new ArrayList<>(Arrays.asList(
                    //PedroPathingMove.createWithTimeOutConstrain(follower, obeliskView, 200),
                    new PedroPathingMove(follower, shootPose),
                    new Shoot(shootSystem, canOpen),
                    new PedroPathingMove(follower, firstPickUpStart),
                    new PickUpBalls(robot, follower, firstPickUpEnd),
                    new PedroPathingMove(follower, shootPose),
                    new Shoot(shootSystem, canOpen),
                    new PedroPathingMove(follower, secondPickUpStart),
                    new PickUpBalls(robot, follower, secondPickUpEnd),
                    new PedroPathingMove(follower, secondPickUpStart),
                    new PedroPathingMove(follower, shootPose),
                    new Shoot(shootSystem, canOpen),
                    new PedroPathingMove(follower, park)));
        }
            MovingStateMachine moveSystem = new MovingStateMachine(movingStates, follower);
            waitForStart();
            robot.gate.close();
            robot.intake.setPower(1);
            timer.reset();
            moveSystem.start();
            shootSystem.start();
            while (opModeIsActive()) {//timer.seconds() < 30 && | put back in after testing.
                if (!shootSystem.isRunning() && startPos != StartPos.EMERGENCY_FAR) {
                    shootSystem.start();
                }
                moveSystem.update();
                shootSystem.update();
                if (moveSystem.getState() != null) {
                    telemetry.addData("Current State Move", moveSystem.getState().getClass().getName());
                } else {
                    telemetry.addData("Current State Move", null);
                }
                telemetry.addData("Velocity of Launcher", robot.launcher.getVelocity());
                telemetry.addData("Current Mode of Carousel", robot.carousel.motor.getMode());
                telemetry.addData("Carousel is busy", robot.carousel.motor.isBusy());
                telemetry.addData("Carousel Power", robot.carousel.motor.getPower());
                telemetry.addData("Carousel Target Position", robot.carousel.motor.getTargetPosition());
                telemetry.addData("Carousel Current Position", robot.carousel.motor.getCurrentPosition());
                telemetry.addData("IMU", robot.imu.getRobotYawPitchRollAngles());
                telemetry.update();
                try {
                    MyDrawing.drawRobot(follower.getPose());
                    MyDrawing.drawPoseHistory(poseHistory);
                    MyDrawing.sendPacket();
                } catch (Exception e) {
                    //throw new RuntimeException("Drawing failed ", e);
                }
            }
        }
    void setPose(){
        if(alliance.equals(Alliance.BLUE)){
            startingPose = startingPose.mirror();
            obeliskView = obeliskView.mirror();
            shootPose = shootPose.mirror();
            firstPickUpStart = firstPickUpStart.mirror();
            firstPickUpEnd = firstPickUpEnd.mirror();
            secondPickUpStart = secondPickUpStart.mirror();
            secondPickUpEnd = secondPickUpEnd.mirror();
            park = park.mirror();
            farStartingPose = farStartingPose.mirror();
            farStartingPoseShoot = farStartingPoseShoot.mirror();
            moveBasic = moveBasic.mirror();
            nearEndPose = nearEndPose.mirror();
            farEndPose = farEndPose.mirror();
        }
    }

     */
}
