package org.usfirst.frc.team7146.robot.subsystems;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.imgproc.Imgproc;
import org.usfirst.frc.team7146.robot.Robot;
import org.usfirst.frc.team7146.robot.commands.CmdGroupBase;

import edu.wpi.cscore.CvSink;
import edu.wpi.cscore.CvSource;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.interfaces.Accelerometer;
import edu.wpi.first.wpilibj.interfaces.Gyro;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import io.github.d0048.Utils;

public class StatusSubsystem extends Subsystem {
	private static final Logger logger = Logger.getLogger(StatusSubsystem.class.getName());
	public static boolean DEBUG = true;
	public Gyro mGyro = Robot.mOI.mGyro;
	public Accelerometer mAccel = Robot.mOI.mAccelerometer;
	public double absHeading = 0;

	public StatusSubsystem() {
		super();
	}

	@Override
	protected void initDefaultCommand() {
		CmdGroupBase statusDaemon = new CmdGroupBase("Status Deamon", 100) {
			@Override
			protected void execute() {
				super.execute();
				pollSDBConfig();
				Robot.mStatusSubsystem.pullGyro();
				Robot.mStatusSubsystem.write_info();
			}
		};
		statusDaemon.publicRequires(this);
		this.setDefaultCommand(statusDaemon);
		putCVInfo();
		startVisionDeamon();
	}

	public void pullGyro() {
		absHeading = (360 + (mGyro.getAngle() % 360)) % 360;// Since it could return (-inf,inf)
	}

	public void reset() {
		mGyro.reset();
	}

	public void write_info() {
		SmartDashboard.putNumber("Absolute Heading", absHeading);
		SmartDashboard.putNumber("Acc X", mAccel.getX());
		SmartDashboard.putNumber("Acc Y", mAccel.getY());
		SmartDashboard.putNumber("Acc Z", mAccel.getZ());
	}

	public CameraServer mCameraServer;
	public UsbCamera mUsbCamera;
	CvSink cvSink;
	CvSource cvSrcOut, cvSrcMask;
	int[] resolution = { 20, 50 };
	Scalar LOWER_BOUND = new Scalar(40, 40, 40), UPPER_BOUND = new Scalar(90, 360, 360);
	int EXPLOSURE = -1;// TODO: Calibrate Camera EXPLOSURE

	public static boolean isCVEnabled = true;
	public static boolean isCVUsable = false;

	public void startVisionDeamon() {
		try {
			mCameraServer = CameraServer.getInstance();
			mUsbCamera = mCameraServer.startAutomaticCapture();
			cvSink = mCameraServer.getVideo();
			mUsbCamera.setFPS(10);
			mUsbCamera.setResolution(resolution[0], resolution[1]);
			if (EXPLOSURE == -1)
				mUsbCamera.setExposureAuto();
			else
				mUsbCamera.setExposureManual(EXPLOSURE);
			cvSrcOut = mCameraServer.putVideo("src out", resolution[0], resolution[1]);
			cvSrcMask = mCameraServer.putVideo("src mask", resolution[0], resolution[1]);
			putCVInfo();
		} catch (Exception e) {
			logger.warning("[CV] init failed:");
			e.printStackTrace();
		}
		Thread t = new Thread(() -> {
			int gcIteration = 0;
			Mat frame = new Mat();
			Mat dst = new Mat();
			List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
			List<MatOfPoint> maxContours = new ArrayList<MatOfPoint>();
			List<RotatedRect> possibleRects = new ArrayList<>();
			while (!Thread.interrupted()) {
				if (!isCVEnabled)
					continue;
				try {
					if (0 == cvSink.grabFrame(frame)) {
						logger.warning("Error grabbing fram from camera");
						continue;
					} else {
						Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2HSV);
						Core.inRange(frame, LOWER_BOUND, UPPER_BOUND, dst);
						Imgproc.cvtColor(frame, frame, Imgproc.COLOR_HSV2BGR);
						contours.clear();
						maxContours.clear();
						Imgproc.findContours(dst, contours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
						Utils.release(dst);
						if (contours.size() >= 2) {
							for (int i = 0; i < 2; i++) { // find 2 largest contours
								MatOfPoint maxContour = contours.get(0);
								double maxArea = 0;
								for (Iterator<MatOfPoint> iterator = contours.iterator(); iterator.hasNext();) {
									MatOfPoint contour = iterator.next();
									double area = Imgproc.contourArea(contour);
									if (area > maxArea) {
										maxArea = area;
										maxContour = contour;
									}
								}
								maxContours.add(maxContour);
								contours.remove(maxContour);
							}
							if (!contours.isEmpty()) {
								double maxArea = Imgproc.contourArea(maxContours.get(0));
								for (Iterator<MatOfPoint> iterator = contours.iterator(); iterator.hasNext();) {
									MatOfPoint contour = iterator.next();
									double area = Imgproc.contourArea(contour);
									if (area / maxArea > 0.65) {
										maxContours.add(contour);
										iterator.remove();
									}
								}
							}
							Utils.releaseMoPs(contours);
							Imgproc.drawContours(frame, maxContours, -1, new Scalar(100, 256, 0), 1);
							possibleRects.clear();
							MatOfPoint2f cnt = new MatOfPoint2f();
							for (MatOfPoint c : maxContours) {
								c.convertTo(cnt, CvType.CV_32F);
								possibleRects.add(Imgproc.minAreaRect(cnt));
								drawRotatedRect(frame, possibleRects.get(possibleRects.size() - 1),
										new Scalar(0, 250, 0), 1);
							}
							cnt.release();
							Utils.releaseMoPs(maxContours);
							Point center = centerOf(frame);
							label(frame, center, new Scalar(250, 50, 50));

							double minDist = euclideanDistance(possibleRects.get(0).center, center);
							RotatedRect centerRec = possibleRects.get(0);

							for (RotatedRect rec : possibleRects) {
								double dist = euclideanDistance(rec.center, center);
								if (dist < minDist) {
									minDist = dist;
									centerRec = rec;
								}
								drawRotatedRect(frame, rec, new Scalar(50, 50, 150), 3);
							}
							drawRotatedRect(frame, centerRec, new Scalar(250, 100, 250), 4);

							// Right one: rot > -45
							// Left one: rot < -45
							Imgproc.putText(frame, isLeft(centerRec) ? "-->" : "<--", centerRec.center,
									Core.FONT_HERSHEY_PLAIN, 2, new Scalar(250, 100, 250), 1);

							RotatedRect matchedRec = null;
							Point taget = null;
							if ((matchedRec = searchClosestRectMatch(isLeft(centerRec), centerRec,
									possibleRects)) != null) {// if left one then search right for a right one
								drawRotatedRect(frame, centerRec, new Scalar(256, 160, 256), 4);
								Imgproc.line(frame, centerRec.center, matchedRec.center, new Scalar(250, 100, 250), 5);
								taget = new Point();
								taget.x = (centerRec.center.x + matchedRec.center.x) / 2;
								taget.y = (centerRec.center.y + matchedRec.center.y) / 2;
								label(frame, center, new Scalar(0, 0, 0));
								label(frame, taget, new Scalar(250, 50, 50));
							}
						}
						cvSrcOut.putFrame(frame);
						// cvSrcMask.putFrame(dst);
						// Garbage Collection
						Utils.release(frame);
						Utils.release(dst);
						if (gcIteration++ > 10) {
							System.gc();
							gcIteration = 0;
						}
					}
				} catch (Exception e) {
					logger.warning("Error in CV Thread:");
					e.printStackTrace();
					isCVUsable = false;
				}
			}
		});
		t.start();
	}

	static Point[] vertices = new Point[4];

	public static void drawRotatedRect(Mat image, RotatedRect rotatedRect, Scalar color, int thickness) {
		rotatedRect.points(vertices);
		MatOfPoint points = new MatOfPoint(vertices);
		Imgproc.drawContours(image, Arrays.asList(points), -1, color, thickness);
		Utils.releaseMoP(points);
	}

	public boolean isLeft(RotatedRect rec) {
		return rec.angle < -45;
	}

	// Right one: rot > -45
	// Left one: rot < -45
	// dir=1 -> search right
	// dir=0 -> search left
	public RotatedRect searchClosestRectMatch(boolean dir, RotatedRect centerRec, List<RotatedRect> rects) {
		RotatedRect matchRec = null;
		double minDist = 99999 * (dir ? 1 : -1);
		double dist;
		for (RotatedRect rec : rects) {
			dist = rec.center.x - centerRec.center.x; // positive when search right
			if (dir && !isLeft(rec) && dist > 0 && (dist) < Math.abs(minDist)) {// search right for a right one
				if (Math.abs(centerRec.center.y - rec.center.y) < centerRec.size.width * 2) {
					minDist = dist;
					matchRec = rec;
				}
			} else if (!dir && isLeft(rec) && dist < 0 && (dist) > minDist) { // search left for a left one
				if (Math.abs(centerRec.center.y - rec.center.y) < centerRec.size.width * 2) {
					minDist = dist;
					matchRec = rec;
				}
			}
		}
		return matchRec;
	}

	public Point centerOf(Mat m) {
		Point center = new Point();
		center.x = m.width() / 2;
		center.y = m.height() / 2;
		return center;
	}

	public void label(Mat src, Point p, Scalar color) {
		Imgproc.drawMarker(src, p, color, Imgproc.MARKER_CROSS, 8, 2, 1);
	}

	public double euclideanDistance(Point a, Point b) {
		double distance = 99999;
		try {
			if (a != null && b != null) {
				double xDiff = a.x - b.x;
				double yDiff = a.y - b.y;
				distance = Math.sqrt(Math.pow(xDiff, 2) + Math.pow(yDiff, 2));
			}
		} catch (Exception e) {
			System.out.println("Something went wrong in euclideanDistance function: " + e.getMessage());
		}
		return distance;
	}
	// -----------------------

	public void pollSDBConfig() {
		try {
			UPPER_BOUND.val[0] = SmartDashboard.getNumber("HIGH H", UPPER_BOUND.val[0]);
			UPPER_BOUND.val[1] = SmartDashboard.getNumber("HIGH S", UPPER_BOUND.val[1]);
			UPPER_BOUND.val[2] = SmartDashboard.getNumber("HIGH V", UPPER_BOUND.val[2]);
			LOWER_BOUND.val[0] = SmartDashboard.getNumber("LOW H", LOWER_BOUND.val[0]);
			LOWER_BOUND.val[1] = SmartDashboard.getNumber("LOW S", LOWER_BOUND.val[1]);
			LOWER_BOUND.val[2] = SmartDashboard.getNumber("LOW V", LOWER_BOUND.val[2]);
			EXPLOSURE = (int) SmartDashboard.getNumber("EXPLOSURE(-1: auto)", EXPLOSURE);
			if (EXPLOSURE != -1)
				mUsbCamera.setExposureManual(EXPLOSURE);
			else
				mUsbCamera.setExposureAuto();

		} catch (Exception e) {
			logger.warning("[CV] poll failed:" + e.getMessage());
		}
	}

	public void putCVInfo() {
		SmartDashboard.putNumber("HIGH H", UPPER_BOUND.val[0]);
		SmartDashboard.putNumber("HIGH S", UPPER_BOUND.val[1]);
		SmartDashboard.putNumber("HIGH V", UPPER_BOUND.val[2]);
		SmartDashboard.putNumber("LOW H", LOWER_BOUND.val[0]);
		SmartDashboard.putNumber("LOW S", LOWER_BOUND.val[1]);
		SmartDashboard.putNumber("LOW V", LOWER_BOUND.val[2]);
		SmartDashboard.putNumber("EXPLOSURE(-1: auto)", EXPLOSURE);
	}

	void debug(String s) {
		if (DEBUG)
			logger.warning(s);
	}
}
