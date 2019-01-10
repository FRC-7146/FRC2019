package testCommands;

import java.math.BigDecimal;
import java.util.logging.Logger;

import org.usfirst.frc.team7146.robot.Robot;
import org.usfirst.frc.team7146.robot.commands.CmdBase;

public class AccelTestCMD extends CmdBase {
	private static final java.util.logging.Logger logger = Logger.getLogger(AccelTestCMD.class.getName());
	public static CmdBase instance;
	double ofs = 0;

	public AccelTestCMD() {
		super("AccelTestCMD", 99);
	}

	// Called just before this Command runs the first time
	@Override
	protected void initialize() {

	}

	// Called repeatedly when this Command is scheduled to run
	public BigDecimal v = new BigDecimal(0), x = new BigDecimal(0), dt = new BigDecimal(1 / 3);
	int itr = 5, i = itr;

	// Make this return true when this Command no longer needs to run execute()
	@Override
	protected boolean isFinished() {
		return false;
	}

	// Called once after isFinished returns true
	@Override
	protected void end() {
		super.end();
		Robot.mChasisDriveSubsystem.stop();
		logger.info("Instance destroyed");
		AccelTestCMD.instance = null;
	}

	// Called when another command which requires one or more of the same
	// subsystems is scheduled to run
	@Override
	protected void interrupted() {
		end();
	}

}
