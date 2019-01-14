package org.usfirst.frc.team7146.robot.subsystems;

import java.util.ArrayList;
import java.util.Arrays;
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

public class StatusSubsystem extends Subsystem {
	private static final Logger logger = Logger.getLogger(StatusSubsystem.class.getName());
	public static boolean DEBUG = true;
	public Gyro mGyro = Robot.mOI.mGyro;
	public Accelerometer mAccel = Robot.mOI.mAccelerometer;
	public double absHeading = 0;

	public StatusSubsystem() {

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
	int[] resolution = { 60, 30 };
	Scalar LOWER_BOUND = new Scalar(80, 40, 40), UPPER_BOUND = new Scalar(110, 360, 360);
	int EXPLOSURE = -1;// TODO: Calibrate Camera EXPLOSURE

	public static boolean isCVUsable = false;

	public void startVisionDeamon() {
		try {
			mCameraServer = CameraServer.getInstance();
			mUsbCamera = mCameraServer.startAutomaticCapture();
			cvSink = mCameraServer.getVideo();
			mUsbCamera.setFPS(10);
			mUsbCamera.setResolution(resolution[0], resolution[1]);
			mUsbCamera.setExposureAuto();
			cvSrcOut = mCameraServer.putVideo("src out", resolution[0], resolution[1]);
			cvSrcMask = mCameraServer.putVideo("src mask", resolution[0], resolution[1]);
			putCVInfo();
		} catch (Exception e) {
			logger.warning("[CV] init failed:");
			e.printStackTrace();
		}
		Thread t = new Thread(() -> {
			Mat frame = new Mat();
			Mat dst = new Mat();
			Point points[] = new Point[4];
			List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
			List<MatOfPoint> maxContours = new ArrayList<MatOfPoint>();

			int gcIteration = 0;
			while (!Thread.interrupted()) {
				try {
					if (0 == cvSink.grabFrame(frame)) {
						logger.warning("Error grabbing fram from camera");
					} else {
						Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2HSV);
						Core.inRange(frame, LOWER_BOUND, UPPER_BOUND, dst);

						Imgproc.findContours(dst, contours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

						if (contours.size() >= 2) {
							for (int i = 0; i < 2; i++) { // find 2 largest contours
								MatOfPoint maxContour = contours.get(0);
								double maxArea = 0;
								for (MatOfPoint contour : contours) {
									double area = Imgproc.contourArea(contour);
									if (area > maxArea) {
										maxArea = area;
										maxContour = contour;
									}
								}
								maxContours.add(maxContour);
								contours.remove(maxContour);
							}
							// TODO: Need to add left2right iteration=========
							if (!contours.isEmpty()) {
								double maxArea = Imgproc.contourArea(contours.get(0));
								for (MatOfPoint contour : contours) {
									double area = Imgproc.contourArea(contour);
									if (area / maxArea > 0.65)
										maxContours.add(contour);
								}
							}
							// ===============================================

							Imgproc.drawContours(frame, maxContours, -1, new Scalar(100, 256, 0), 1);
							MatOfPoint2f cnt1 = new MatOfPoint2f(), cnt2 = new MatOfPoint2f();
							maxContours.get(0).convertTo(cnt1, CvType.CV_32F);
							maxContours.get(1).convertTo(cnt2, CvType.CV_32F);
							RotatedRect rec1 = Imgproc.minAreaRect(cnt1);
							RotatedRect rec2 = Imgproc.minAreaRect(cnt2);

							rec1.points(points);
							for (int i = 0; i < 4; ++i) {
								Imgproc.line(frame, points[i], points[(i + 1) % 4], new Scalar(0, 0, 0), 1);
							}
							rec2.points(points);
							for (int i = 0; i < 4; ++i) {
								Imgproc.line(frame, points[i], points[(i + 1) % 4], new Scalar(0, 0, 0), 1);
							}
						}
						cvSrcOut.putFrame(frame);
						cvSrcMask.putFrame(dst);

						if (!contours.isEmpty())
							contours.forEach((MatOfPoint c) -> {
								c.release();
							});
						contours.clear();
						if (!maxContours.isEmpty())
							maxContours.forEach((MatOfPoint c) -> {
								c.release();
							});
						maxContours.clear();
						if (gcIteration++ > 1000) {
							System.gc();
							gcIteration = 0;
						}
					}
				} catch (Exception e) {
					logger.warning("Error in CV Thread:");
					e.printStackTrace();
				}
			}
		});
		t.start();
	}

	public static void drawRotatedRect(Mat image, RotatedRect rotatedRect, Scalar color, int thickness) {
		Point[] vertices = new Point[4];
		rotatedRect.points(vertices);
		MatOfPoint points = new MatOfPoint(vertices);
		Imgproc.drawContours(image, Arrays.asList(points), -1, color, thickness);
	}

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
