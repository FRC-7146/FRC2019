package io.github.d0048;

import java.util.List;
import java.util.logging.Logger;

import org.opencv.core.Mat;

public class Utils {
	private static final Logger logger = Logger.getLogger(Utils.class.getName());
	public static boolean DEBUG = false;

	public static double speedCalc(double input, double absLimit, double factor) {
		if (Math.abs(input) > absLimit) {
			if (input > 0) {
				return absLimit;
			} else {
				return -absLimit;
			}
		}
		return input * factor;
	}

	public static void release(Mat o) {
		if (o != null) {
			try {
				((Mat) o).release();
			} finally {
			}
		}
	}

	public static void release(Object[] os) {
		for (Object o : os) {
			try {
				release((Mat) o);
			} finally {
			}
		}
	}

	public static void release(List<Mat> os) {
		for (Object o : os) {
			try {
				((Mat) o).release();
			} finally {
			}
		}
	}

	void debug(String s) {
		if (DEBUG)
			logger.warning(s);
	}

}
