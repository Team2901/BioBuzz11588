package org.firstinspires.ftc.teamcode.autonomous.StateMachine.PlagarismParts;

class MovingStateMachine {
    /*
    ShootingStateMachine shootSystem;
    Follower follower;
    int orderIndex;
    ArrayList<MyState> order;
    public MovingStateMachine(ShootingStateMachine _shootSystem, ArrayList<MyState> _order) {
        shootSystem = _shootSystem;
        order = _order;
        cancel();
    }
    public MovingStateMachine(ArrayList<MyState> _order, Follower _follower) {
        order = _order;
        follower = _follower;
        cancel();
    }
    public MyState getState() {
        if(isInBounds(orderIndex)){
            return order.get(orderIndex);
        }
        return null;
    }
    public void cancel(){
        orderIndex = -1;
    }
    public void start(){
        orderIndex = 0;
        order.get(orderIndex).enter();
    }
    public boolean isInBounds(int num){
        return num>-1 && num<order.size();
    }
    public void update() {
        if(orderIndex < 0){
            return;
        }
        MyState nextState = null;
        if(order.get(orderIndex).isEnded()){
            order.get(orderIndex).leaving();
            if(isInBounds(orderIndex+1)){
                orderIndex++;
                nextState = order.get(orderIndex);
            }else{ // This is so that if not entering END state; state will be null
                orderIndex = -1;
            }
        }
        if (nextState != null) {
            order.get(orderIndex).enter();
        }
        if(isInBounds(orderIndex)){
            order.get(orderIndex).update();
        }

    }

    static class Move1 extends MyState {
        Hardware robot;
        int k;
        public Move1(Hardware robot, int k){
            this.k = k;
            this.robot = robot;
        }
        @Override
        void enter() {
            robot.move(-43, 0);
        }

        @Override
        boolean isEnded() {
            return !robot.isDriveBusy();
        }
    }
    static class Shoot extends MyState {
        ShootingStateMachine shootSystem;
        CanOpen canOpen;
        public Shoot(ShootingStateMachine shootSystem, CanOpen _canOpen){
            canOpen = _canOpen;
            this.shootSystem = shootSystem;
        }
        @Override
        void enter() {
            canOpen.value = true;
        }

        @Override
        boolean isEnded() {
            return shootSystem.orderIndex == shootSystem.order.size()-1;
        }
    }
    static class Park1 extends MyState {
        Hardware robot;
        int k;
        public Park1(Hardware robot, int k){
            this.k = k;
            this.robot = robot;
        }
        @Override
        void enter() {
            robot.move(0, k*25);
        }

        @Override
        boolean isEnded() {
            return !robot.isDriveBusy();
        }
    }
    static class waitUntil extends MyState {
        ElapsedTime timer = new ElapsedTime();
        double targetTime;
        waitUntil (int sec){
            this.targetTime = sec;
        }
        @Override
        void enter() {
            timer.reset();
        }
        void update() {

        }
        @Override
        boolean isEnded() {
            return (timer.time() > targetTime);
        }
    }
    static class PedroPathingMove extends MyState {
        Follower follower;
        Pose targetPose;
        Double timeOutConstraint;
        Double translationalConstraint;
        Double velocityConstraint;
        Double headingConstraint;
        Double tValueConstraint;
        static PedroPathingMove createWithTimeOutConstrain(Follower _follower, Pose _targetPose, double _timeOutConstraint){
            PedroPathingMove path = new PedroPathingMove(_follower, _targetPose);
            path.timeOutConstraint = _timeOutConstraint;
            return path;
        }
        public PedroPathingMove(Follower _follower, Pose _targetPose){
            follower = _follower;
            targetPose = _targetPose;
            tValueConstraint = 0.995;
            velocityConstraint = 0.1;
            translationalConstraint = 0.1;
            headingConstraint = 0.007;
            timeOutConstraint = 200.0;
        }
        @Override
        void enter() {
            PathBuilder builder;
            Supplier<PathChain> pathChain;
            builder =follower.pathBuilder() //120, 120, 45
                    .addPath(new Path(new BezierLine(follower::getPose, targetPose)))
                    .setLinearHeadingInterpolation(follower.getHeading(), targetPose.getHeading());
            if(timeOutConstraint != null){
                builder.setTimeoutConstraint(timeOutConstraint);
            }
            if(translationalConstraint != null){
                builder.setTranslationalConstraint(translationalConstraint);
            }
            if(velocityConstraint != null){
                builder.setVelocityConstraint(velocityConstraint);
            }
            if(headingConstraint != null){
                builder.setHeadingConstraint(headingConstraint);
            }
            if(tValueConstraint != null){
                builder.setTValueConstraint(tValueConstraint);
            }
            pathChain = builder::build;
            follower.followPath(pathChain.get(), false);
            follower.setMaxPower(0.75);

        }

        @Override
        boolean isEnded() {
            return !follower.isBusy();
        }

        @Override
        void update() {
            super.update();
            follower.update();
        }
    }
    static class PickUpBalls extends MyState {
        Follower follower;
        Pose targetPose;
        Hardware robot;
        double initialPosition;
        double stuckAt;
        double tolerance = 10;
        public PickUpBalls(Hardware _robot, Follower _follower, Pose _targetPose){
            robot = _robot;
            follower = _follower;
            targetPose = _targetPose;
        }
        @Override
        void enter() {
            robot.carousel.freeRotation();
            Supplier<PathChain> pathChain;
            pathChain = () -> follower.pathBuilder() //120, 120, 45
                    .addPath(new Path(new BezierLine(follower::getPose, targetPose)))
                    .setLinearHeadingInterpolation(follower.getHeading(), targetPose.getHeading())
                    .setTranslationalConstraint(0.1)
                    .build();
            follower.followPath(pathChain.get(), false);
            follower.setMaxPower(0.7);
        }

        @Override
        boolean isEnded() {
            return !follower.isBusy();
        }

        @Override
        void update() {
            super.update();
            follower.update();
        }

        @Override
        void leaving() {
            super.leaving();
        }
    }
     */
}
