package org.usfirst.frc.team7146.robot.commands;

import java.util.logging.Logger;

import org.usfirst.frc.team7146.robot.Robot;
import org.usfirst.frc.team7146.robot.RobotMap;
import org.usfirst.frc.team7146.robot.subsystems.ChasisDriveSubsystem;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.buttons.Button;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class ManualControlCommand extends CmdGroupBase {
	private static final Logger logger = Logger.getLogger(ManualControlCommand.class.getName());
	public static boolean DEBUG = true;

	public Joystick js0 = Robot.mOI.mJoystick0;
	public Button sportBtn = Robot.mOI.sportBtn, precisionBtn = Robot.mOI.precisionBtn, disableBtn = Robot.mOI.autoBtn;;
	public Joystick js1 = Robot.mOI.mJoystick1;

	// 0->x 1->y 2->z 3->slider
	public ManualControlCommand() {
		super("Manual Control", 100);
	}

	@Override
	protected void execute() {
		double xIn = js0.getRawAxis(2), yIn = -js0.getRawAxis(3), zIn = js0.getRawAxis(0),
				grabberIn = js0.getRawAxis(1), povIn = js0.getPOV();
		double xIn1 = js1.getRawAxis(0), yIn1 = -js1.getRawAxis(1), zIn1 = js1.getRawAxis(2),
				throttle = js1.getRawAxis(3), povIn1 = js1.getPOV();
		try {
			Robot.mOI.grabberServo.set(Math.max(grabberIn, throttle));
			ChasisDriveSubsystem.collisionMarginCM = throttle * 20;
			// Manual control overrides auto control if necessary
			if (!AutoAlignCommand.AUTO_ALIGNING || manualOverAuto()) {
				if (povIn != -1) {
					Robot.mChasisDriveSubsystem.pidTurnAbsolute(yIn, xIn, povIn);
				} else if (isJS0Active()) {
					Robot.mChasisDriveSubsystem.safeDriveCartesian(yIn, xIn, zIn);
				} else {
					Robot.mChasisDriveSubsystem.absoluteSafeDriveCartesian(yIn1, xIn1, zIn1);
				}
			}
		} catch (Exception e) {
			debug("Error in Manual Drive Exec:");
			e.printStackTrace();
		}
		SmartDashboard.putString("Mobility Mode", RobotMap.MOTOR.CURRENT_MODE.toString());
		if (isJS0Active()) {
			SmartDashboard.putNumber("Y in", yIn);
			SmartDashboard.putNumber("X in", xIn);
			SmartDashboard.putNumber("Z in", zIn);
			SmartDashboard.putNumber("POV in", povIn);
			SmartDashboard.putString("Primary Controler:", js0.getName());
		} else {
			SmartDashboard.putNumber("Y in", yIn1);
			SmartDashboard.putNumber("X in", xIn1);
			SmartDashboard.putNumber("Z in", zIn1);
			SmartDashboard.putNumber("POV in", povIn1);
			SmartDashboard.putString("Primary Controler:", js1.getName());
		}
		SmartDashboard.putNumber("Grabber in", grabberIn);
		SmartDashboard.putBoolean("Manual Over Auto", manualOverAuto());
	}

	public static final boolean manualOverAuto() {
		return isJS0Active() || isJS1Active();
	}

	public static final boolean isJS0Active() {
		Joystick js0 = Robot.mOI.mJoystick0;
		double xIn = js0.getRawAxis(2), yIn = -js0.getRawAxis(3), zIn = js0.getRawAxis(0);
		return (Math.abs(xIn) + Math.abs(yIn) + Math.abs(zIn) > 0.1);
	}

	public static final boolean isJS1Active() {
		Joystick js1 = Robot.mOI.mJoystick1;
		double xIn1 = js1.getRawAxis(0), yIn1 = -js1.getRawAxis(1), zIn1 = js1.getRawAxis(2),
				throttle = js1.getRawAxis(3), povIn1 = js1.getPOV();
		return (Math.abs(xIn1) + Math.abs(yIn1) + Math.abs(zIn1) > 0.1) || povIn1 != -1;
	}

	@Override
	protected boolean isFinished() {
		return false;
	}

	void debug(String s) {
		if (DEBUG)
			logger.warning(s);
	}
}
