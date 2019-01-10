package org.usfirst.frc.team7146.robot.commands;

import java.util.logging.Logger;

import org.opencv.core.Mat;
import org.usfirst.frc.team7146.robot.Robot;
import org.usfirst.frc.team7146.robot.subsystems.StatusSubsystem;

import edu.wpi.first.wpilibj.buttons.Button;

public class AutoAlignCommand extends CmdGroupBase {
	private static final Logger logger = Logger.getLogger(AutoAlignCommand.class.getName());
	public static boolean DEBUG = false;

	public Button triggerKey = Robot.mOI.autoBtn;

	public AutoAlignCommand() {
		super("Auto Align", 100);
	}

	@Override
	protected void execute() {
		if (triggerKey.get() && StatusSubsystem.isCVUsable) {
			logger.warning("Auto Align");
		}
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
