package org.usfirst.frc.team7146.robot.subsystems;

import java.util.logging.Logger;

import org.usfirst.frc.team7146.robot.Robot;
import org.usfirst.frc.team7146.robot.commands.CmdGroupBase;

import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.interfaces.Accelerometer;
import edu.wpi.first.wpilibj.interfaces.Gyro;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import io.github.d0048.UltraRedAnalogDistanceSensor;

//TODO: Enable positions on release
public class StatusSubsystem extends Subsystem {
	private static final Logger logger = Logger.getLogger(StatusSubsystem.class.getName());
	public static boolean DEBUG = true;

	public Gyro mGyro = Robot.mOI.mGyro;
	public Accelerometer mAccel = Robot.mOI.mAccelerometer;
	public double absHeading = 0;
	public double heading = 0;
	// TODO:limit drive
	public UltraRedAnalogDistanceSensor ultraRedFwd = Robot.mOI.ultraRedFwd;
	public UltraRedAnalogDistanceSensor ultraRedBwd = Robot.mOI.ultraRedBwd;
	public UltraRedAnalogDistanceSensor ultraRedLwd = Robot.mOI.ultraRedLwd;
	public UltraRedAnalogDistanceSensor ultraRedRwd = Robot.mOI.ultraRedRwd;

	// public TalonSRX mTalon1 = Robot.mOI.mTalon1;

	public StatusSubsystem() {
		super();
	}

	@Override
	protected void initDefaultCommand() {
		/*
		 * CmdGroupBase statusDaemon = new CmdGroupBase("Status Deamon", 100) {
		 * 
		 * @Override protected void execute() { super.execute();
		 * Robot.mStatusSubsystem.pullGyro(); Robot.mStatusSubsystem.write_info();
		 * pullDist(); } }; statusDaemon.publicRequires(this);
		 * this.setDefaultCommand(statusDaemon);
		 */
		new Thread(() -> {
			while (!Thread.interrupted()) {
				Robot.mStatusSubsystem.pullGyro();
				Robot.mStatusSubsystem.write_info();
				pullDist();
				try {
					Thread.sleep(50, 0);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
		// setPosition(0);
	}

	double dstFw = ultraRedFwd.getDistance(), dstBw = ultraRedBwd.getDistance(), dstLw = ultraRedLwd.getDistance(),
			dstRw = ultraRedRwd.getDistance();

	/**
	 * 
	 * @return {F,B,L,R}
	 */
	public final double[] getDistances() {
		double[] arr = { dstFw, dstBw, dstLw, dstRw };
		return arr;
	}

	public final void write_info() {
		// SmartDashboard.putNumber("Encoder Position", getPosition());
		SmartDashboard.putNumber("[UR] " + ultraRedFwd.getName(), dstFw);
		SmartDashboard.putNumber("[UR] " + ultraRedBwd.getName(), dstBw);
		SmartDashboard.putNumber("[UR] " + ultraRedLwd.getName(), dstLw);
		SmartDashboard.putNumber("[UR] " + ultraRedRwd.getName(), dstRw);
	}

	/*
	 * public double getPosition() { return
	 * mTalon1.getSensorCollection().getQuadraturePosition(); }
	 * 
	 * public ErrorCode setPosition(int newPosition) { return
	 * mTalon1.getSensorCollection().setQuadraturePosition(newPosition, 19); }
	 */
	public void pullDist() {
		dstFw = dstFw * 0.7 + 0.3 * ultraRedFwd.getDistance();
		dstBw = dstBw * 0.7 + 0.3 * ultraRedBwd.getDistance();
		dstLw = dstLw * 0.7 + 0.3 * ultraRedLwd.getDistance();
		dstRw = dstRw * 0.7 + 0.3 * ultraRedRwd.getDistance();
	}

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
