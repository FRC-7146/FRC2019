/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package org.usfirst.frc.team7146.robot;

import java.util.logging.Logger;

import org.usfirst.frc.team7146.robot.commands.CmdBase;
import org.usfirst.frc.team7146.robot.subsystems.ChasisDriveSubsystem;
import org.usfirst.frc.team7146.robot.subsystems.StatusSubsystem;

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.command.Scheduler;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import testCommands.AccelTestCMD;

public class Robot extends TimedRobot {
	private static final java.util.logging.Logger logger = Logger.getLogger(Robot.class.getName());
	public static boolean DEBUG = false;

	public static OI mOI;
	public static ChasisDriveSubsystem mChasisDriveSubsystem;
	public static StatusSubsystem mStatusSubsystem;

	Command mAutonomousCommand;
	SendableChooser<Command> mchooser = new SendableChooser<>();

	@Override
	public void robotInit() {
		mOI = new OI();
		// mOI.mGyro.calibrate();
		// mOI.mGyro.reset();
		// logger.warning("Gyro init success");
		mStatusSubsystem = new StatusSubsystem();
		mChasisDriveSubsystem = new ChasisDriveSubsystem();
		mOI.mapOI();
	}

	@Override
	public void disabledInit() {
	}

	@Override
	public void disabledPeriodic() {
		Scheduler.getInstance().run();
	}

	public void mPeriodic() {// execute everywhere except disable
	}

	@Override
	public void autonomousInit() {
	}

	@Override
	public void autonomousPeriodic() {
		Scheduler.getInstance().run();

	}

	@Override
	public void teleopInit() {

	}

	@Override
	public void teleopPeriodic() {
		try {
			this.mPeriodic();
			Scheduler.getInstance().run();
		} catch (Exception e) {
			logger.warning("Err in Teleop");
			e.printStackTrace();
		}
	}

	@Override
	public void testInit() {
	}

	@Override
	public void testPeriodic() {
		try {
			this.mPeriodic();
		} catch (Exception e) {
			logger.warning("Err in Test");
			e.printStackTrace();
		}
	}

	public static boolean cmdCanRun(CmdBase cmd) {
		for (String k : mOI.mCommands.keySet()) {
			if (mOI.mCommands.get(k).priority < cmd.priority && mOI.mCommands.get(k).isRunning()) {
				return false;
			}
		}
		return true;
	}
}
