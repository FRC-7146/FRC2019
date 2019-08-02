package io.github.d0048;

import java.util.List;
import java.util.logging.Logger;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;

public class Utils {
	private static final Logger logger = Logger.getLogger(Utils.class.getName());
	public static boolean DEBUG = false;

	public static final double collisionCalc(double posDst, double negDst, double valIn, double collisionMarginDst) {
		double dst = valIn > 0 ? posDst : negDst;
		return dst > collisionMarginDst ? valIn : (valIn > 0 ? -0.5 : 0.5);
	}

	public static double[] absVecRet = { 0, 0 };// Y,X

	public static final double[] absoluteVector2Relative(double relY, double relX, double absHeading) {
		absHeading = (absHeading / 360) * 2 * Math.PI;
		absVecRet[1] = Math.cos(absHeading) * relX - Math.sin(absHeading) * relY; // cal X
		absVecRet[0] = Math.sin(absHeading) * relX + Math.cos(absHeading) * relY; // cal Y
		return absVecRet;
	}

	// TODO: It's actually 60,30 degree instead of 45
	// static final double[] allAngles = { 359, 0, 45, 90, 135, 225, 270, 315 };
	static final double[] allAngles = { 359, 0, 30, 90, 150, 210, 270, 330 };

	/**
	 * @return The nearest angle needed for hatch panel
	 */
	public static final double nearestHatchAngle(double currentAngle) {
		double maxdiff = 360, nearestAngle = 0;
		for (double a : allAngles) {
			double diff = Math.abs(a - currentAngle);
			if (diff < maxdiff) {
				maxdiff = diff;
				nearestAngle = a;
			}
		}
		return nearestAngle;
	}

	public static final double speedCalc(double input, double absLimit, double sensitivity) {
		double ret = (input * absLimit);
		if (Math.abs(ret) > absLimit) {
			if (input > 0) {
				return absLimit;
			} else {
				return -absLimit;
			}
		}
		// return ret;
		return ret > 0 ? ret + sensitivity : ret - sensitivity;
	}

	public static final void release(Mat o) {
		if (o != null) {
			try {
				if (!((Mat) o).empty())
					((Mat) o).release();
			} finally {
			}
		}
	}

	public static final void release(Object[] os) {
		for (Object o : os) {
			try {
				if (!((Mat) o).empty())
					release((Mat) o);
			} finally {
			}
		}
	}

	public static final void release(List<Mat> os) {
		if (!os.isEmpty())
			for (Object o : os) {
				try {
					if (!((Mat) o).empty())
						((Mat) o).release();
				} finally {
				}
			}
	}

	public static final void releaseMoPs(List<MatOfPoint> os) {
		if (!os.isEmpty())
			for (Object o : os) {
				try {
					if (!((MatOfPoint) o).empty())
						((MatOfPoint) o).release();
				} finally {
				}
			}
	}

	public static final void releaseMoP(MatOfPoint o) {
		try {
			if (!((MatOfPoint) o).empty())
				((MatOfPoint) o).release();
		} finally {
		}
	}

	public static final void releaseMoP2fs(List<MatOfPoint2f> os) {
		if (!os.isEmpty())
			for (MatOfPoint2f o : os) {
				try {
					if (!o.empty())
						((MatOfPoint2f) o).release();
				} finally {
				}
			}
	}

	public static final void releaseMoP2f(MatOfPoint2f o) {
		try {
			if (!o.empty())
				((MatOfPoint2f) o).release();
		} finally {
		}

	}

	void debug(String s) {
		if (DEBUG)
			logger.warning(s);
	}

}
