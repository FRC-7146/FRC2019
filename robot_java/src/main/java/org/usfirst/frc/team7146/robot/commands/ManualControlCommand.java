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

	public Joystick js = Robot.mOI.mJoystick0;
	public Button sportBtn = Robot.mOI.sportBtn, precisionBtn = Robot.mOI.precisionBtn, disableBtn = Robot.mOI.autoBtn;;

	public ManualControlCommand() {
		super("Manual Control", 100);
	}

	@Override
	protected void execute() {
		if (disableBtn.get()) // Disable manual control if auto enabled
			return;

		double yIn = js.getY(), xIn = js.getX(), zIn = js.getZ(), grabberIn = js.getTwist(), povIn = js.getPOV();
		try {
			if (povIn == -1)
				Robot.mChasisDriveSubsystem.safeDriveCartesian(yIn, xIn, zIn);
			else
				Robot.mChasisDriveSubsystem.pidTurn(povIn);
			Robot.mOI.grabberServo.set(grabberIn);
		} catch (Exception e) {
			// not installed
		}
		SmartDashboard.putString("Mobility Mode", RobotMap.MOTOR.CURRENT_MODE.toString());
		SmartDashboard.putNumber("Y in", yIn);
		SmartDashboard.putNumber("X in", xIn);
		SmartDashboard.putNumber("Z in", zIn);
		SmartDashboard.putNumber("POV in", povIn);
		SmartDashboard.putNumber("Grabber in", grabberIn);
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
