/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package org.usfirst.frc.team7146.robot.subsystems;

import java.util.logging.Logger;

import org.usfirst.frc.team7146.robot.Robot;
import org.usfirst.frc.team7146.robot.RobotMap;
import org.usfirst.frc.team7146.robot.commands.CmdGroupBase;
import org.usfirst.frc.team7146.robot.commands.ManualControlCommand;

import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.drive.MecanumDrive;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import io.github.d0048.Utils;

public class ChasisDriveSubsystem extends Subsystem {
	private static final Logger logger = Logger.getLogger(ChasisDriveSubsystem.class.getName());
	public static boolean DEBUG = true;

	SpeedController frontLeftMotor = Robot.mOI.frontLeftMotor, rearLeftMotor = Robot.mOI.rearLeftMotor,
			frontRightMotor = Robot.mOI.frontRightMotor, rearRightMotor = Robot.mOI.rearRightMotor;
	public MecanumDrive drive = Robot.mOI.drive;
	StatusSubsystem status = Robot.mStatusSubsystem;

	public static boolean COLLISION_SAFETY = false;
	public static double collisionMarginCM = 25;
	public double currentYSpeed = 0, currentXSpeed = 0, currentZRotation = 0;

	public ChasisDriveSubsystem() {
	}

	// TODO: Can not turn negative for some reason
	double tol = 3;// Degree allowed to be wrong

	public void pidTurnAbsolute(double ySpeed, double xSpeed, double requestAng) {
		status.pullGyro();
		double rot = requestAng - status.absHeading;
		rot %= 360;
		if (Math.abs(rot) > 180) {
			if (rot > 0)// left turn instead
				rot = (rot - 360);
			else if (rot < 0) {// right turn instead
				rot = rot + 360;
			}
		}
		rot = Math.abs(rot) > tol ? rot : 0;
		rot = rot / (rot > 45 ? 30 : 10); // PID FACTOR
		safeDriveCartesian(ySpeed, xSpeed, rot);
	}

	public void safeDriveCartesian(double ySpeed, double xSpeed, double zRotation) {
		currentYSpeed = Utils.speedCalc(ySpeed, RobotMap.MOTOR.CURRENT_MODE.getY_LIMIT(),
				RobotMap.MOTOR.CURRENT_MODE.Y_SENSITIVITY);
		currentXSpeed = Utils.speedCalc(xSpeed, RobotMap.MOTOR.CURRENT_MODE.getX_LIMIT(),
				RobotMap.MOTOR.CURRENT_MODE.X_SENSITIVITY);
		currentZRotation = Utils.speedCalc(zRotation, RobotMap.MOTOR.CURRENT_MODE.getZ_LIMIT(),
				RobotMap.MOTOR.CURRENT_MODE.Z_SENSITIVITY);
		if (COLLISION_SAFETY) {
			double[] dsts = status.getDistances();
			currentXSpeed = Utils.collisionCalc(dsts[2], dsts[3], currentXSpeed, collisionMarginCM);
			currentXSpeed = Utils.collisionCalc(dsts[0], dsts[1], currentYSpeed, collisionMarginCM);
		}
		drive.driveCartesian(currentXSpeed, currentYSpeed, currentZRotation);
		write_info();
	}

	// Notice here X,Y inputs are global coords relative to initial(operator)
	// position
	public void absoluteSafeDriveCartesian(double ySpeed, double xSpeed, double zRotation) {
		// Perform a -\theta rotation to vector
		status.pullGyro();
		double[] rotated = Utils.absoluteVector2Relative(ySpeed, xSpeed, status.absHeading);
		safeDriveCartesian(rotated[0], rotated[1], zRotation);
	}

	public void stop() {
		safeDriveCartesian(0, 0, 0);
	}

	public final void write_info() {
		SmartDashboard.putNumber("Y Speed", currentYSpeed);
		SmartDashboard.putNumber("X Speed", currentXSpeed);
		SmartDashboard.putNumber("Z Rotation", currentZRotation);
		SmartDashboard.putBoolean("Collision Safety Switch", COLLISION_SAFETY);
		SmartDashboard.putNumber("Collision Safety Margin", collisionMarginCM);
	}

	@Override
	protected void initDefaultCommand() {
		CmdGroupBase driveDaemon = new CmdGroupBase("Chasis Daemon", 100) {
			boolean i = true;

			@Override
			protected void execute() {
				if (i)
					Robot.mChasisDriveSubsystem.write_info();
				i = !i;
			}

			@Override
			protected boolean isFinished() {
				return false;
			}
		};
		driveDaemon.addParallel(new ManualControlCommand());
		driveDaemon.publicRequires(this);

		setDefaultCommand(driveDaemon);
	}

	void debug(String s) {
		if (DEBUG)
			logger.warning("[Chasis]" + s);
	}
}
