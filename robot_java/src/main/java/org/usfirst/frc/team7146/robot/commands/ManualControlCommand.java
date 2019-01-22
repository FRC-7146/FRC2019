package org.usfirst.frc.team7146.robot.commands;

import java.util.logging.Logger;

import org.usfirst.frc.team7146.robot.Robot;
import org.usfirst.frc.team7146.robot.RobotMap;

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
			// Manual control overrides auto control if necessary
			if (!AutoAlignCommand.AUTO_ALIGNING || manualOverAuto()) {
				if (povIn == -1)
					Robot.mChasisDriveSubsystem.safeDriveCartesian(yIn, xIn, zIn);
				else
					Robot.mChasisDriveSubsystem.pidTurnAbsolute(yIn, xIn, povIn);
			}
		} catch (Exception e) {
			debug("Error in Manual Drive Exec:");
			e.printStackTrace();
		}
		SmartDashboard.putString("Mobility Mode", RobotMap.MOTOR.CURRENT_MODE.toString());
		SmartDashboard.putNumber("Y in", yIn);
		SmartDashboard.putNumber("X in", xIn);
		SmartDashboard.putNumber("Z in", zIn);
		SmartDashboard.putNumber("POV in", povIn);
		SmartDashboard.putNumber("Grabber in", grabberIn);
		SmartDashboard.putBoolean("Manual Over Auto", manualOverAuto());
	}

	public static boolean manualOverAuto() {
		Joystick js0 = Robot.mOI.mJoystick0, js1 = Robot.mOI.mJoystick1;
		;
		double xIn = js0.getRawAxis(2), yIn = -js0.getRawAxis(3), zIn = js0.getRawAxis(0),
				grabberIn = js0.getRawAxis(1), povIn = js0.getPOV();
		double xIn1 = js1.getRawAxis(0), yIn1 = -js1.getRawAxis(1), zIn1 = js1.getRawAxis(2),
				throttle = js1.getRawAxis(3), povIn1 = js1.getPOV();
		return (Math.abs(xIn) + Math.abs(yIn) + Math.abs(zIn) > 0.1) || povIn != -1
				|| (Math.abs(xIn1) + Math.abs(yIn1) + Math.abs(zIn1) > 0.1) || povIn1 != -1;
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
