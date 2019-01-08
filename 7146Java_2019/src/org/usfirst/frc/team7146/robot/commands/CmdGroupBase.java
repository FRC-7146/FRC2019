package org.usfirst.frc.team7146.robot.commands;

import org.usfirst.frc.team7146.robot.Robot;

import edu.wpi.first.wpilibj.command.CommandGroup;

public class CmdGroupBase extends CommandGroup {
	public int priority = 100;

	public CmdGroupBase(int priority) {
		this.priority = priority;
	}
	public CmdGroupBase(String name, int priority) {
		super(name);
		this.priority = priority;
	}

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
