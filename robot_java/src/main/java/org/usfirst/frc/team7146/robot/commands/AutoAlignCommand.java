package org.usfirst.frc.team7146.robot.commands;

import java.util.logging.Logger;

import org.opencv.core.Point;
import org.usfirst.frc.team7146.robot.Robot;
import org.usfirst.frc.team7146.robot.RobotMap;
import org.usfirst.frc.team7146.robot.subsystems.StatusSubsystem;

import edu.wpi.first.wpilibj.buttons.Button;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import io.github.d0048.Utils;

public class AutoAlignCommand extends CmdGroupBase {
	private static final Logger logger = Logger.getLogger(AutoAlignCommand.class.getName());
	public static boolean DEBUG = false;

	public static boolean AUTO_ALIGNING = false;
	public Button triggerKey = Robot.mOI.autoBtn;

	public AutoAlignCommand() {
		super("Auto Align", 100);
		addParallel(new ModeChangeCommand(new RobotMap.MOTOR.AUTO()));
		writeStatus();
	}

	@Override
	public synchronized void start() {
		super.start();
		StatusSubsystem.isCVUsable = false;
		dataEligiable = false;
		AUTO_ALIGNING = true;
		lastTarget = StatusSubsystem.target.clone();
		logger.warning("Auto Align");
		writeStatus();
	}

	// NOTE: Target is already in offset mode where x-=center.x, y-=center.y
	Point lastTarget = null;
	boolean dataEligiable = false;
	double dataOffset = 0;
	double sampleVariance = 0;

	@Override
	protected void execute() {
		if (StatusSubsystem.isCVEnabled && StatusSubsystem.isCVUsable
				&& (sampleVariance = Math.abs(lastTarget.x - (dataOffset = StatusSubsystem.target.x))) < 15) {

			dataEligiable = true;
			double yVec = dataOffset / 16, xVec = 0;// TODO: drive forard on stable
			Robot.mStatusSubsystem.pullGyro();
			Robot.mChasisDriveSubsystem.pidTurnAbsolute(yVec, xVec,
					Utils.nearestHatchAngle(Robot.mStatusSubsystem.absHeading));
		} else {
			Robot.mChasisDriveSubsystem.pidTurnAbsolute(0, 0,
					Utils.nearestHatchAngle(Robot.mStatusSubsystem.absHeading));
			dataEligiable = false;
		}
		lastTarget = StatusSubsystem.target.clone();
		writeStatus();

	}

	@Override
	protected void end() {
		StatusSubsystem.isCVUsable = false;
		dataEligiable = false;
		lastTarget = null;
		AUTO_ALIGNING = false;
		writeStatus();
		RobotMap.MOTOR.CURRENT_MODE = new RobotMap.MOTOR.NORMAL();
		logger.warning("Auto Align End");
	}

	public void writeStatus() {
		SmartDashboard.putBoolean("Auto Align Enabled", AUTO_ALIGNING);
		SmartDashboard.putBoolean("Auto Align Data Eligible", dataEligiable);
		SmartDashboard.putNumber("Auto Y Offset", dataOffset);
		SmartDashboard.putNumber("Auto Noise Value", sampleVariance);
	}

	@Override
	protected void interrupted() {
		end();
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
