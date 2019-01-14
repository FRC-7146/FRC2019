package io.github.d0048;

import java.util.List;
import java.util.logging.Logger;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;

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
				if (!((Mat) o).empty())
					((Mat) o).release();
			} finally {
			}
		}
	}

	public static void release(Object[] os) {
		for (Object o : os) {
			try {
				if (!((Mat) o).empty())
					release((Mat) o);
			} finally {
			}
		}
	}

	public static void release(List<Mat> os) {
		if (!os.isEmpty())
			for (Object o : os) {
				try {
					if (!((Mat) o).empty())
						((Mat) o).release();
				} finally {
				}
			}
	}

	public static void releaseMoPs(List<MatOfPoint> os) {
		if (!os.isEmpty())
			for (Object o : os) {
				try {
					if (!((MatOfPoint) o).empty())
						((MatOfPoint) o).release();
				} finally {
				}
			}
	}

	public static void releaseMoP(MatOfPoint o) {
		try {
			if (!((MatOfPoint) o).empty())
				((MatOfPoint) o).release();
		} finally {
		}
	}

	public static void releaseMoP2fs(List<MatOfPoint2f> os) {
		if (!os.isEmpty())
			for (Object o : os) {
				try {
					if (!((MatOfPoint2f) o).empty())

						((MatOfPoint2f) o).release();
				} finally {
				}
			}
	}

	public static void releaseMoP2f(MatOfPoint2f o) {
		try {
			if (!((MatOfPoint2f) o).empty())

				((MatOfPoint2f) o).release();
		} finally {
		}

	}

	void debug(String s) {
		if (DEBUG)
			logger.warning(s);
	}

}
