package org.usfirst.frc.team7146.robot.commands;

import java.util.logging.Logger;

import org.opencv.core.Point;
import org.usfirst.frc.team7146.robot.Robot;
import org.usfirst.frc.team7146.robot.RobotMap;
import org.usfirst.frc.team7146.robot.subsystems.VisionSubsystem;

import edu.wpi.first.wpilibj.buttons.Button;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import io.github.d0048.Utils;
import io.github.d0048.vision.SrcTypes;
import io.github.d0048.vision.CVDataSource;

public class AutoAlignCommand extends CmdGroupBase {
	private static final Logger logger = Logger.getLogger(AutoAlignCommand.class.getName());
	public static boolean DEBUG = false;

	public static boolean AUTO_ALIGNING = false;
	public Button triggerKey = Robot.mOI.autoBtn;

	public AutoAlignCommand() {
		super("Auto Align", 100);
		writeStatus();
	}

	@Override
	public synchronized void start() {
		super.start();
		VisionSubsystem.lazyness = 0;
		VisionSubsystem.isCVUsable = false;
		RobotMap.MOTOR.CURRENT_MODE = new RobotMap.MOTOR.AUTO();
		dataEligiable = false;
		AUTO_ALIGNING = true;
		lastTarget = VisionSubsystem.target.clone();
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
		if (!ManualControlCommand.manualOverAuto()) {// Human have highest priority
			if (VisionSubsystem.currentSource.src == SrcTypes.PI && VisionSubsystem.isCVUsable) {
				double xVec = SmartDashboard.getNumber("[PI]Recommended X", 0),
						yVec = SmartDashboard.getNumber("[PI]Recommended Y", 0),
						zRot = SmartDashboard.getNumber("[PI]Recommended Z", 0);
				if (zRot == 0)
					Robot.mChasisDriveSubsystem.pidTurnAbsolute(yVec, xVec,
							Utils.nearestHatchAngle(Robot.mStatusSubsystem.absHeading));
				else
					Robot.mChasisDriveSubsystem.safeDriveCartesian(yVec, xVec, zRot);
			} else if (VisionSubsystem.isCVEnabled && VisionSubsystem.isCVUsable
					&& (sampleVariance = Math.abs(lastTarget.x - (dataOffset = VisionSubsystem.target.x))) < 15) {
				dataEligiable = true;

				double yVec = 0, xVec = dataOffset / (dataOffset > 20 ? 70 : 45);
				xVec = xVec > 0 ? (xVec + 0.08) : (xVec - 0.08);
				xVec = Math.abs(dataOffset) < 2 ? 0 : xVec;
				yVec = Math.abs(dataOffset) < 10 ? 0.5 : 0;// drive forward
				Robot.mStatusSubsystem.pullGyro();
				Robot.mChasisDriveSubsystem.pidTurnAbsolute(yVec, xVec,
						Utils.nearestHatchAngle(Robot.mStatusSubsystem.absHeading));
			} else {
				Robot.mChasisDriveSubsystem.pidTurnAbsolute(0, 0,
						Utils.nearestHatchAngle(Robot.mStatusSubsystem.absHeading));
				dataEligiable = false;
			}
		}
		lastTarget = VisionSubsystem.target.clone();
		writeStatus();

	}

	@Override
	protected void end() {
		VisionSubsystem.lazyness = VisionSubsystem.lazynessIDLE;
		VisionSubsystem.isCVUsable = false;
		dataEligiable = false;
		lastTarget = null;
		AUTO_ALIGNING = false;
		writeStatus();
		RobotMap.MOTOR.CURRENT_MODE = new RobotMap.MOTOR.NORMAL();
		logger.warning("Auto Align End");
	}

	public final void writeStatus() {
		SmartDashboard.putBoolean("Auto Align Enabled", AUTO_ALIGNING);
		SmartDashboard.putBoolean("Auto Align Data Eligible", dataEligiable);
		SmartDashboard.putNumber("Auto Noise Value", sampleVariance);
		SmartDashboard.putNumber("Auto Y Offset", dataOffset);
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
