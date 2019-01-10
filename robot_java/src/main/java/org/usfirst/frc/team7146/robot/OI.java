/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package org.usfirst.frc.team7146.robot;

import java.util.HashMap;
import java.util.logging.Logger;

import org.usfirst.frc.team7146.robot.RobotMap.MOTOR.PRECISION;
import org.usfirst.frc.team7146.robot.commands.CmdBase;
import org.usfirst.frc.team7146.robot.commands.CmdGroupBase;
import org.usfirst.frc.team7146.robot.commands.ModeChangeCommand;

import edu.wpi.first.wpilibj.ADXRS450_Gyro;
import edu.wpi.first.wpilibj.BuiltInAccelerometer;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.SPI;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.buttons.Button;
import edu.wpi.first.wpilibj.buttons.JoystickButton;
import edu.wpi.first.wpilibj.drive.MecanumDrive;
import edu.wpi.first.wpilibj.interfaces.Accelerometer;

/**
 * This class is the glue that binds the controls on the physical operator
 * interface to the commands and command groups that allow control of the robot.
 */
public class OI {

	private static final Logger logger = Logger.getLogger(OI.class.getName());

	public SpeedController frontLeftMotor = new Spark(RobotMap.MOTOR.FL_MOTOR),
			rearLeftMotor = new Spark(RobotMap.MOTOR.BL_MOTOR), frontRightMotor = new Spark(RobotMap.MOTOR.FR_MOTOR),
			rearRightMotor = new Spark(RobotMap.MOTOR.BR_MOTOR);
	// public MecanumDrive drive = new MecanumDrive(frontLeftMotor, rearLeftMotor,
	// frontRightMotor, rearRightMotor);
	public MecanumDrive drive = null;

	public Joystick mJoystick0 = new Joystick(0);
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

	public Button autoBtn = mXboxBtnLt, precisionBtn = mXboxBtnRt, sportBtn = mXboxBtnRb;

	public ADXRS450_Gyro mGyro = new ADXRS450_Gyro(SPI.Port.kOnboardCS0);
	public Accelerometer mAccelerometer = new BuiltInAccelerometer(Accelerometer.Range.k4G);

	public HashMap<String, CmdBase> mCommands = new HashMap<String, CmdBase>();

	public OI() {
	}

	public void mapOI() {
		/* Btn bindings */
		precisionBtn.whenActive(new ModeChangeCommand(new RobotMap.MOTOR.PRECISION()));
		sportBtn.whenActive(new ModeChangeCommand(new RobotMap.MOTOR.SPORT()));
		precisionBtn.whenReleased(new ModeChangeCommand(new RobotMap.MOTOR.NORMAL()));
		sportBtn.whenReleased(new ModeChangeCommand(new RobotMap.MOTOR.NORMAL()));
		logger.info("OI map init");

	}

}