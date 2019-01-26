package org.usfirst.frc.team7146.robot.commands;

import org.usfirst.frc.team7146.robot.Robot;

import edu.wpi.first.wpilibj.command.Command;

public class CmdBase extends Command {
	public int priority = 100;

	public CmdBase(int priority) {
		this.priority = priority;
	}

	public CmdBase(String name, int priority) {
		super(name);
		this.priority = priority;
	}

	public CmdBase(double timeout, int priority) {
		super(timeout);
		this.priority = priority;
	}

	public CmdBase(String name, double timeout, int priority) {
		super(name, timeout);
		this.priority = priority;
	}

	@Override
	protected void end() {
		Robot.mOI.mCommands.remove(this.getName());
	}

	@Override
	protected boolean isFinished() {
		return false;
	}

	@Override
	protected void execute() {
	}

}
