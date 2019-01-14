/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package org.usfirst.frc.team7146.robot;

/**
 * The RobotMap is a mapping from the ports sensors and actuators are wired into
 * to a variable name. This provides flexibility changing wiring, makes checking
 * the wiring easier and significantly reduces the number of magic numbers
 * floating around.
 */
public class RobotMap {

	public static class MOTOR {
		public static final int GRABBER_PWN_SERVO = 9;
		public static final int LEFT_MOTOR_GROUP = 0;
		public static final int RIGHT_MOTOR_GROUP = 1;
		public static final int FL_MOTOR = 0, BL_MOTOR = 1, FR_MOTOR = 2, BR_MOTOR = 3;
		public static NORMAL CURRENT_MODE = new NORMAL();

		public static class NORMAL {
			public double X_FACTOR = 0.8, Y_FACTOR = 0.8, Z_FACTOR = 0.8;
			public double X_LIMIT = 0.5, Y_LIMIT = 0.5, Z_LIMIT = 0.5;
			public double SENSITIVITY = 0.05;

			@Override
			public String toString() {
				return "NORMAL";
			}
		}

		public static class PRECISION extends NORMAL {
			public double X_FACTOR = 0.3, Y_FACTOR = 0.3, Z_FACTOR = 0.3;
			public double X_LIMIT = 0.3, Y_LIMIT = 0.3, Z_LIMIT = 0.3;
			public double SENSITIVITY = 0.0;

			@Override
			public String toString() {
				return "PRECISION";
			}
		}

		public static class SPORT extends NORMAL {
			public double X_FACTOR = 1, Y_FACTOR = 1, Z_FACTOR = 1;
			public double X_LIMIT = 1, Y_LIMIT = 1, Z_LIMIT = 1;
			public double SENSITIVITY = 0.08;

			@Override
			public String toString() {
				return "SPORT";
			}
		}
	}

	public static class JS {
		public static final int NUM_XBOX_X = 1;
		public static final int NUM_XBOX_A = 2;
		public static final int NUM_XBOX_B = 3;
		public static final int NUM_XBOX_Y = 4;
		public static final int NUM_XBOX_LB = 5;
		public static final int NUM_XBOX_RB = 6;
		public static final int NUM_XBOX_LT = 7;
		public static final int NUM_XBOX_RT = 8;
		public static final int NUM_XBOX_LEFT_STICK_BTN = 11;
		public static final int NUM_XBOX_RIGHT_STICK_BTN = 12;
		public static final int NUM_XBOX_BACK = 9;
		public static final int NUM_XBOX_START = 10;
	}

	public static class SENSOR {
		public static final int NUM_LSW_UP = 1;
		public static final int NUM_LSW_MID = 2;
		public static final int NUM_LSW_DW = 0;
	}
}
