package org.usfirst.frc.team7146.robot.subsystems;

import java.util.logging.Logger;

import org.usfirst.frc.team7146.robot.Robot;
import org.usfirst.frc.team7146.robot.commands.CmdGroupBase;

import edu.wpi.first.wpilibj.Ultrasonic;
import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.interfaces.Accelerometer;
import edu.wpi.first.wpilibj.interfaces.Gyro;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

//TODO: Enable positions on release
public class StatusSubsystem extends Subsystem {
	private static final Logger logger = Logger.getLogger(StatusSubsystem.class.getName());
	public static boolean DEBUG = true;
	public Gyro mGyro = Robot.mOI.mGyro;
	public Accelerometer mAccel = Robot.mOI.mAccelerometer;
	public Ultrasonic mUltraS = Robot.mOI.frontDistamceSensor;
	public double absHeading = 0;
	public double heading = 0;
	// public TalonSRX mTalon1 = Robot.mOI.mTalon1;

	public StatusSubsystem() {
		super();
	}

	@Override
	protected void initDefaultCommand() {
		CmdGroupBase statusDaemon = new CmdGroupBase("Status Deamon", 100) {
			@Override
			protected void execute() {
				super.execute();
				Robot.mStatusSubsystem.pullGyro();
				Robot.mStatusSubsystem.write_info();
			}
		};
		statusDaemon.publicRequires(this);
		this.setDefaultCommand(statusDaemon);
		// setPosition(0);
		// startBetaIMULocalization();
	}

	public final void write_info() {
		// SmartDashboard.putNumber("Encoder Position", getPosition());
		SmartDashboard.putNumber("accXofs", accXofs);
		SmartDashboard.putNumber("accX", mAccel.getX() - accXofs);
	}

	double accXofs = 0;

	public void startBetaIMULocalization() {
		calibrateIMU((long) 1e9);
		double accX = mAccel.getX(), accY = mAccel.getY(), accZ = mAccel.getZ();
	}

	public void calibrateIMU(long timeNano) {
		long time = System.nanoTime();
		int count = 0;
		while (System.nanoTime() - time < timeNano) {
			count++;
			accXofs += mAccel.getX();
		}
		accXofs /= count;
	}

	/*
	 * public double getPosition() { return
	 * mTalon1.getSensorCollection().getQuadraturePosition(); }
	 * 
	 * public ErrorCode setPosition(int newPosition) { return
	 * mTalon1.getSensorCollection().setQuadraturePosition(newPosition, 19); }
	 */
	public void pullGyro() {
		heading = mGyro.getAngle();
		absHeading = (360 + (heading % 360)) % 360;// Since it could return (-inf,inf)
	}

	public void reset() {
		mGyro.reset();
	}

	void debug(String s) {
		if (DEBUG)
			logger.warning(s);
	}
}
