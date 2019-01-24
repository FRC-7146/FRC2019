/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package org.usfirst.frc.team7146.robot;

import java.util.HashMap;
import java.util.logging.Logger;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;

import org.usfirst.frc.team7146.robot.commands.AutoAlignCommand;
import org.usfirst.frc.team7146.robot.commands.CmdBase;
import org.usfirst.frc.team7146.robot.commands.ModeChangeCommand;

import edu.wpi.first.wpilibj.ADXRS450_Gyro;
import edu.wpi.first.wpilibj.BuiltInAccelerometer;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.SPI;
import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.Ultrasonic;
import edu.wpi.first.wpilibj.buttons.Button;
import edu.wpi.first.wpilibj.buttons.JoystickButton;
import edu.wpi.first.wpilibj.drive.MecanumDrive;
import edu.wpi.first.wpilibj.interfaces.Accelerometer;
import io.github.d0048.UltraRedAnalogDistanceSensor;

/**
 * This class is the glue that binds the controls on the physical operator
 * interface to the commands and command groups that allow control of the robot.
 */
public class OI {

	private static final Logger logger = Logger.getLogger(OI.class.getName());

	public Servo grabberServo = new Servo(RobotMap.MOTOR.GRABBER_PWN_SERVO);

	public SpeedController frontLeftMotor = new Spark(RobotMap.MOTOR.FL_MOTOR),
			rearLeftMotor = new Spark(RobotMap.MOTOR.BL_MOTOR), frontRightMotor = new Spark(RobotMap.MOTOR.FR_MOTOR),
			rearRightMotor = new Spark(RobotMap.MOTOR.BR_MOTOR);
	public MecanumDrive drive = new MecanumDrive(frontLeftMotor, rearLeftMotor, frontRightMotor, rearRightMotor);
	// TODO: Enable on release
	// public MecanumDrive drive = null;

	public Joystick mJoystick0 = new Joystick(0);// Dual Action
	public Button mXboxBtnA = new JoystickButton(mJoystick0, RobotMap.JS.NUM_XBOX_A),
			mXboxBtnB = new JoystickButton(mJoystick0, RobotMap.JS.NUM_XBOX_B),
			mXboxBtnX = new JoystickButton(mJoystick0, RobotMap.JS.NUM_XBOX_X),
			mXboxBtnY = new JoystickButton(mJoystick0, RobotMap.JS.NUM_XBOX_Y),
			mXboxBtnLb = new JoystickButton(mJoystick0, RobotMap.JS.NUM_XBOX_LB),
			mXboxBtnRb = new JoystickButton(mJoystick0, RobotMap.JS.NUM_XBOX_RB),
			mXboxBtnLt = new JoystickButton(mJoystick0, RobotMap.JS.NUM_XBOX_LT),
			mXboxBtnRt = new JoystickButton(mJoystick0, RobotMap.JS.NUM_XBOX_RT),
			mXboxBtnLftStk = new JoystickButton(mJoystick0, RobotMap.JS.NUM_XBOX_LEFT_STICK_BTN),
			mXboxBtnRghtStk = new JoystickButton(mJoystick0, RobotMap.JS.NUM_XBOX_RIGHT_STICK_BTN),
			mXboxBtnBack = new JoystickButton(mJoystick0, RobotMap.JS.NUM_XBOX_BACK),
			mXboxBtnStart = new JoystickButton(mJoystick0, RobotMap.JS.NUM_XBOX_START);
	// 0->x 1->y 2->z 3->slider
	// trigger->0 missle->1 others follow (naming-1)
	public Joystick mJoystick1 = new Joystick(1);// Extreme 3D
	public Button mTrigger = new JoystickButton(mJoystick1, 1), mMissle = new JoystickButton(mJoystick1, 2),
			mBtn3 = new JoystickButton(mJoystick1, 3), mBtn4 = new JoystickButton(mJoystick1, 4),
			mBtn5 = new JoystickButton(mJoystick1, 5), mBtn6 = new JoystickButton(mJoystick1, 6),
			mBtn7 = new JoystickButton(mJoystick1, 7);

	public Button autoBtn = mXboxBtnLb, precisionBtn = mXboxBtnRt, sportBtn = mXboxBtnRb;

	public ADXRS450_Gyro mGyro = new ADXRS450_Gyro(SPI.Port.kOnboardCS0);
	public Accelerometer mAccelerometer = new BuiltInAccelerometer(Accelerometer.Range.k4G);
	// public Ultrasonic frontDistamceSensor = new
	// Ultrasonic(RobotMap.ULTRASONIC.FRONT_TRG,
	// RobotMap.ULTRASONIC.FRONT_ECH);

	public UltraRedAnalogDistanceSensor ultraRedFwd = new UltraRedAnalogDistanceSensor(RobotMap.ULTRA_RED.F, "FRONT");
	public UltraRedAnalogDistanceSensor ultraRedBwd = new UltraRedAnalogDistanceSensor(RobotMap.ULTRA_RED.B, "BACK");
	public UltraRedAnalogDistanceSensor ultraRedLwd = new UltraRedAnalogDistanceSensor(RobotMap.ULTRA_RED.L, "LEFT");
	public UltraRedAnalogDistanceSensor ultraRedRwd = new UltraRedAnalogDistanceSensor(RobotMap.ULTRA_RED.R, "RIGHT");

	// TODO: Enable positions on release
	// public TalonSRX mTalon1 = new TalonSRX(0);

	public HashMap<String, CmdBase> mCommands = new HashMap<String, CmdBase>();

	public OI() {
	}

	public void mapOI() {
		mJoystick0.setXChannel(2);// xIn
		mJoystick0.setYChannel(3);// yIn
		mJoystick0.setZChannel(0);// zIn
		mJoystick0.setTwistChannel(1);// grabberIn
		// frontDistamceSensor.setAutomaticMode(true);
		// TODO: Enable positions on release
		// mTalon1.set(ControlMode.PercentOutput, 0.0); // Disable output coz not needed

		/* Btn bindings */
		precisionBtn.whenActive(new ModeChangeCommand(new RobotMap.MOTOR.PRECISION()));
		sportBtn.whenActive(new ModeChangeCommand(new RobotMap.MOTOR.SPORT()));
		precisionBtn.whenReleased(new ModeChangeCommand(new RobotMap.MOTOR.NORMAL()));
		sportBtn.whenReleased(new ModeChangeCommand(new RobotMap.MOTOR.NORMAL()));
		AutoAlignCommand aa = new AutoAlignCommand();
		autoBtn.toggleWhenPressed(aa);
		mTrigger.toggleWhenPressed(aa);
		logger.info("OI map init");

	}

}