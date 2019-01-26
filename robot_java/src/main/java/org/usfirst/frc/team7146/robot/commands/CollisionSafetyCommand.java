package org.usfirst.frc.team7146.robot.commands;

import java.util.logging.Logger;

import org.usfirst.frc.team7146.robot.subsystems.ChasisDriveSubsystem;

public class CollisionSafetyCommand extends CmdBase {
    private static final Logger logger = Logger.getLogger(CollisionSafetyCommand.class.getName());
    public static boolean DEBUG = false;

    public CollisionSafetyCommand() {
        super("Collision Safety ON", 100);
    }

    @Override
    public synchronized void start() {
        super.start();
        ChasisDriveSubsystem.COLLISION_SAFETY = true;
        logger.warning("Collision Safety ON");
    }

    @Override
    protected void end() {
        ChasisDriveSubsystem.COLLISION_SAFETY = false;
        logger.warning("Collision Safety OFF");
    }

    @Override
    protected void interrupted() {
        end();
    }

    @Override
    protected boolean isFinished() {
        return false;
    }
}