/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package org.usfirst.frc.team7146.robot.subsystems;

import java.util.logging.Logger;

import javax.management.modelmbean.RequiredModelMBean;

import org.usfirst.frc.team7146.robot.Robot;
import org.usfirst.frc.team7146.robot.RobotMap;
import org.usfirst.frc.team7146.robot.commands.AutoAlignCommand;
import org.usfirst.frc.team7146.robot.commands.CmdBase;
import org.usfirst.frc.team7146.robot.commands.CmdGroupBase;
import org.usfirst.frc.team7146.robot.commands.ManualControlCommand;

import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.drive.MecanumDrive;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import io.github.d0048.Utils;

public class ChasisDriveSubsystem extends Subsystem {
	private static final Logger logger = Logger.getLogger(ChasisDriveSubsystem.class.getName());
	public static boolean DEBUG = true;

	SpeedController frontLeftMotor = Robot.mOI.frontLeftMotor, rearLeftMotor = Robot.mOI.rearLeftMotor,
			frontRightMotor = Robot.mOI.frontRightMotor, rearRightMotor = Robot.mOI.rearRightMotor;
	public MecanumDrive drive = Robot.mOI.drive;
	StatusSubsystem status = Robot.mStatusSubsystem;
	double currentYSpeed = 0, currentXSpeed = 0, currentZRotation = 0;

	public ChasisDriveSubsystem() {
	}

	public void pidTurn(double reqAng) {
		status.pullGyro();
		double rot = reqAng - status.absHeading;
		rot *= 0.05; // PID FACTOR
		safeDriveCartesian(currentYSpeed, currentXSpeed, rot);
	}

	public void safeDriveCartesian(double ySpeed, double xSpeed, double zRotation) {

		currentYSpeed = Utils.speedCalc(ySpeed, RobotMap.MOTOR.CURRENT_MODE.Y_LIMIT,
				RobotMap.MOTOR.CURRENT_MODE.Y_FACTOR);
		currentXSpeed = Utils.speedCalc(xSpeed, RobotMap.MOTOR.CURRENT_MODE.X_LIMIT,
				RobotMap.MOTOR.CURRENT_MODE.X_FACTOR);
		currentZRotation = Utils.speedCalc(zRotation, RobotMap.MOTOR.CURRENT_MODE.Z_LIMIT,
				RobotMap.MOTOR.CURRENT_MODE.Z_FACTOR);
		// drive.driveCartesian(currentYSpeed, currentXSpeed, currentZRotation);
		// TODO: Enable on release
		write_info();
	}

	public void stop() {
		safeDriveCartesian(0, 0, 0);
	}

	public void write_info() {
		SmartDashboard.putNumber("Y Speed", currentYSpeed);
		SmartDashboard.putNumber("X Speed", currentXSpeed);
		SmartDashboard.putNumber("Z Rotation", currentZRotation);
	}

	@Override
	protected void initDefaultCommand() {
		CmdGroupBase driveDaemon = new CmdGroupBase("Chasis Daemon", 100) {
			@Override
			protected void execute() {
				Robot.mChasisDriveSubsystem.write_info();
			}

			@Override
			protected boolean isFinished() {
				return false;
			}
		};
		driveDaemon.addParallel(new AutoAlignCommand());
		driveDaemon.addParallel(new ManualControlCommand());
		driveDaemon.publicRequires(this);

		// driveDaemon.addParallel(command);//manual drive command
		setDefaultCommand(driveDaemon);
	}

	void debug(String s) {
		if (DEBUG)
			logger.warning("[Chasis]" + s);
	}
}
