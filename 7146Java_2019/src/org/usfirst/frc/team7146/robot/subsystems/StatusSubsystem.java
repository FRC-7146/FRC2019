package org.usfirst.frc.team7146.robot.subsystems;

import java.util.logging.Logger;

import org.usfirst.frc.team7146.robot.Robot;
import org.usfirst.frc.team7146.robot.commands.CmdGroupBase;

import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.interfaces.Accelerometer;
import edu.wpi.first.wpilibj.interfaces.Gyro;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class StatusSubsystem extends Subsystem {
	private static final Logger logger = Logger.getLogger(StatusSubsystem.class.getName());
	public static boolean DEBUG = false;
	public Gyro mGyro = Robot.mOI.mGyro;
	public Accelerometer mAccel = Robot.mOI.mAccelerometer;
	public double absHeading = 0;

	public StatusSubsystem() {
		mGyro.reset();
	}

	@Override
	protected void initDefaultCommand() {
		this.setDefaultCommand(new CmdGroupBase("Status Deamon", 100) {
			@Override
			protected void execute() {
				super.execute();
				Robot.mStatusSubsystem.pullGyro();
				Robot.mStatusSubsystem.write_info();
			}
		});
	}

	public void pullGyro() {
		absHeading = (360 + (mGyro.getAngle() % 360)) % 360;// Since it could return (-inf,inf)
	}

	public void reset() {
		mGyro.reset();
	}

	public void write_info() {
		SmartDashboard.putNumber("Absolute Heading", absHeading);
		SmartDashboard.putNumber("Acc X", mAccel.getX());
		SmartDashboard.putNumber("Acc Y", mAccel.getY());
		SmartDashboard.putNumber("Acc Z", mAccel.getZ());
	}

	@Override
	protected void finalize() throws Throwable {
		mGyro.free();
		super.finalize();
	}

	void debug(String s) {
		if (DEBUG)
			logger.warning(s);
	}
}
