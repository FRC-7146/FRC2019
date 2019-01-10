package org.usfirst.frc.team7146.robot.commands;

import java.util.logging.Logger;
import org.usfirst.frc.team7146.robot.RobotMap;

public class ModeChangeCommand extends CmdGroupBase {
	private static final Logger logger = Logger.getLogger(ModeChangeCommand.class.getName());
	public static boolean DEBUG = false;

	public RobotMap.MOTOR.NORMAL n;

	public ModeChangeCommand(RobotMap.MOTOR.NORMAL n) {
		super("Mode change", 100);
		this.n = n;
	}

	@Override
	protected boolean isFinished() {
		return RobotMap.MOTOR.CURRENT_MODE.toString().equals(n.toString());
	}

	@Override
	protected void execute() {
		RobotMap.MOTOR.CURRENT_MODE = n;
		logger.warning("Mode: " + n.toString() + " Activated");
	}

	@Override
	protected void interrupted() {
		RobotMap.MOTOR.CURRENT_MODE = new RobotMap.MOTOR.NORMAL();
	}
}
