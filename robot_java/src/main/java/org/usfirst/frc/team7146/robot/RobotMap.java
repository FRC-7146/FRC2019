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
			public double X_LIMIT = 0.4, Y_LIMIT = 0.4, Z_LIMIT = 0.3;
			// public double X_SENSITIVITY = 0.1, Y_SENSITIVITY = 0.15, Z_SENSITIVITY =
			// 0.15;
			public double X_SENSITIVITY = 0.04, Y_SENSITIVITY = 0.04, Z_SENSITIVITY = 0;

			@Override
			public String toString() {
				return "NORMAL";
			}

			public double getX_LIMIT() {
				return X_LIMIT;
			}

			public double getY_LIMIT() {
				return Y_LIMIT;
			}

			public double getZ_LIMIT() {
				return Z_LIMIT;
			}

		}

		public static class PRECISION extends NORMAL {
			public double X_LIMIT = 0.3, Y_LIMIT = 0.3, Z_LIMIT = 0.2;

			@Override
			public String toString() {
				return "PRECISION";
			}

			public double getX_LIMIT() {
				return X_LIMIT;
			}

			public double getY_LIMIT() {
				return Y_LIMIT;
			}

			public double getZ_LIMIT() {
				return Z_LIMIT;
			}
		}

		public static class SPORT extends NORMAL {
			public double X_LIMIT = 0.9, Y_LIMIT = 0.7, Z_LIMIT = 0.8;

			@Override
			public String toString() {
				return "SPORT";
			}

			public double getX_LIMIT() {
				return X_LIMIT;
			}

			public double getY_LIMIT() {
				return Y_LIMIT;
			}

			public double getZ_LIMIT() {
				return Z_LIMIT;
			}
		}

		public static class AUTO extends NORMAL {
			public double X_LIMIT = 0.5, Y_LIMIT = 0.4, Z_LIMIT = 0.5;

			@Override
			public String toString() {
				return "AUTO";
			}

			public double getX_LIMIT() {
				return X_LIMIT;
			}

			public double getY_LIMIT() {
				return Y_LIMIT;
			}

			public double getZ_LIMIT() {
				return Z_LIMIT;
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

	public static class ULTRA_RED {
		public static final int F = 0, B = 1, L = 2, R = 3;
	}
}