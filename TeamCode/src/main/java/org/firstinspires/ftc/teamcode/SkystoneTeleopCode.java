package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;

import java.util.ArrayList;

@TeleOp(name="SkystoneTeleopCode")
public class SkystoneTeleopCode extends LinearOpMode {
    public SkystoneRobot2 robot = new SkystoneRobot2();
    double robotAngle = 0;  // heading (in degrees) robot is to maintain. is set by gamepad1.right_stick.
    double leftStickAngle = 0;
    double theta;  // difference between robot heading and the direction it should travel towards.
    double leftStickR = 0; // distance from 0-1 from gamepad1.left_stick center to edge. is used to set power level to drive train motors.
    double xWheelsPower; // wheel 2 and 3 power
    double yWheelsPower; // wheel 1 and 4 power
    double speed;  // speed adjustment for robot to turn towards robotAngle.
    double difference;
    double sign;

    @Override
    public void runOpMode() {
        robot.init(hardwareMap, this);
        double adjustAngle = 0;
        // adjustAngle = this.get_coordinates(); // doesn't work. yet.

        //Start robot with front just up against launch zone line and side against blue wall, intake facing the target.
        robot.odometer.x = 87;
        robot.odometer.y = 73;

        robot.capstoneClaw.setPosition(0);

        if (Math.abs(gamepad2.right_stick_x) + Math.abs(gamepad2.right_stick_y) > 0.2) {
            adjustAngle = Math.atan2(gamepad2.right_stick_x, -gamepad2.right_stick_y) + Math.PI / 2;
        }
        robotAngle = -(adjustAngle * 180) / Math.PI;
        telemetry.addData("Status", "Initialized. Please do something already.");
        telemetry.addData("adjustAngle", (adjustAngle * 180) / Math.PI);
        telemetry.update();

        // Wait for the game to start

        waitForStart();

        // run until the end of the match (driver presses STOP)
        while (opModeIsActive()) {

            // #######################################################
            //  ###### CONTROLS TO MAKE THE DRIVE TRAIN MOVE. ######
            robot.angles = robot.imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
            // now use right stick input to get the robot's heading. the if statement ensures that the joystick is a distance away from the center where readings will be accurate.
            if (Math.abs(gamepad1.right_stick_x) + Math.abs(gamepad1.right_stick_y) > 0.6) {
                robotAngle = (-Math.atan2(gamepad1.right_stick_x, -gamepad1.right_stick_y) * 180 / Math.PI) - 90;
                if (robotAngle <= -180) {
                    robotAngle += 360;
                }
                if (robotAngle >= 180) {
                    robotAngle -= 360;
                }
            }
            if (gamepad1.left_stick_x != 0 || gamepad1.left_stick_y != 0) {
                leftStickAngle = -Math.atan2(gamepad1.left_stick_x, -gamepad1.left_stick_y) + (Math.PI * 5 / 4);
                if (leftStickAngle >= Math.PI) {
                    leftStickAngle -= Math.PI * 2;
                }
                theta = robotAngle / 180 * Math.PI - leftStickAngle;
                xWheelsPower = Math.cos(theta);
                yWheelsPower = Math.sin(theta);
            } else {
                xWheelsPower = 0;
                yWheelsPower = 0;
            }
            difference = robot.angles.firstAngle - robotAngle - (adjustAngle * 180) / Math.PI;
            if (Math.abs(difference) > 180) {
                if (difference < 0) {
                    sign = -1;
                } else {
                    sign = 1;
                }
                difference = sign * (360 - Math.abs(difference));
                speed = -(difference) / 80;
            } else {
                speed = (difference) / 80;
            }

            leftStickR = Math.sqrt((Math.pow(gamepad1.left_stick_x, 2) + Math.pow(gamepad1.left_stick_y, 2))) * 1.42;

            // this code here ensures that the robot can turn without having to strafe or sit there making a whining noise.
            double speed1 = speed, speed2 = speed; // speed1 is for the front wheels 1 and 2, and speed2 is for the back wheels 3 and 4.

            if (0.1 <= Math.abs(speed) && Math.abs(speed) < 0.2) {
                // only the back wheels move, meaning that the robot can turn but at a lower speed.
                speed2 *= 2;
                speed1 = 0;
            } else if (Math.abs(speed) < 0.1) {
                // at a certain threshold you'll get no movement, but the motors will whine. thus, it's best to just stop them.
                speed1 = 0;
                speed2 = 0;
            }
            robot.wheel1.setPower(yWheelsPower * leftStickR + speed1);
            robot.wheel4.setPower(yWheelsPower * leftStickR - speed2);
            robot.wheel2.setPower(xWheelsPower * leftStickR - speed1);
            robot.wheel3.setPower(xWheelsPower * leftStickR + speed2);

            ArrayList<Double> list = robot.odometer.getCurrentCoordinates();
            telemetry.addData("X value:", list.get(1));
            telemetry.addData("Y value:", list.get(2));

            ////// UPDATE TELEMETRY //////
            telemetry.update();

            /////// CAPSTONE PLACER /////////
            if (gamepad1.x){
                robot.capstoneClaw.setPosition(1);
            }

            if (gamepad1.y){
                robot.capstoneHinge.setPosition(0);
            } else if (gamepad1.a) {
                robot.capstoneHinge.setPosition(1);
            }

            /////// FOUNDATION GRABBERS /////

            ////// THEY GO UP ///////
            if (gamepad1.left_bumper){
                robot.foundationGrabberL.setPosition(0.5);
                robot.foundationGrabberR.setPosition(1);
            }
            ////// THEY GO DOWN /////////
            else if (gamepad1.right_bumper){
                robot.foundationGrabberR.setPosition(0);
                robot.foundationGrabberL.setPosition(1);
            }
            /////// LINEAR SLIDE AKA HAND LIFT ///////

            if (gamepad1.left_trigger > 0){
                robot.lift2.setPower(gamepad1.left_trigger);
                robot.lift3.setPower(gamepad1.left_trigger);
            } else if (gamepad1.right_trigger >= 0){
                robot.lift2.setPower(-gamepad1.right_trigger);
                robot.lift3.setPower(-gamepad1.right_trigger);
            }

            ///////////////////// HAND ////////////////////////////
            if (gamepad1.dpad_right){
                robot.mainClawL.setPosition(0.8);
                robot.mainClawR.setPosition(0.1);
            }
            else if (gamepad1.dpad_left) {
                robot.mainClawL.setPosition(0.1);
                robot.mainClawR.setPosition(0.8);
            }

        }
    }
}
