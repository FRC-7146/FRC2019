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
		if (precisionBtn.get() && !(RobotMap.MOTOR.CURRENT_MODE instanceof RobotMap.MOTOR.PRECISION)) {
			RobotMap.MOTOR.CURRENT_MODE = new RobotMap.MOTOR.PRECISION();
			debug(RobotMap.MOTOR.CURRENT_MODE.toString() + " Mode Activated");
		} else if (sportBtn.get() && !(RobotMap.MOTOR.CURRENT_MODE instanceof RobotMap.MOTOR.SPORT)) {
			RobotMap.MOTOR.CURRENT_MODE = new RobotMap.MOTOR.SPORT();
			debug(RobotMap.MOTOR.CURRENT_MODE.toString() + " Mode Activated");
		} else {
			RobotMap.MOTOR.CURRENT_MODE = new RobotMap.MOTOR.NORMAL();
			debug(RobotMap.MOTOR.CURRENT_MODE.toString() + " Mode Activated");
		}

		double yIn = js.getY(), xIn = js.getX(), zIn = js.getZ(), povIn = js.getPOV();
		Robot.mChasisDriveSubsystem.safeDriveCartesian(yIn, xIn, zIn);
		SmartDashboard.putString("Mobility Mode", RobotMap.MOTOR.CURRENT_MODE.toString());
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
