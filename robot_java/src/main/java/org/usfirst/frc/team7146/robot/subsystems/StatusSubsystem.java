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
	// MjpegServer mjpegServer = mCameraServer.addServer("Cubejpeg");

	public static boolean isCVUsable = false;

	public void startVisionDeamon() {
		try {
			mCameraServer = CameraServer.getInstance();
			mUsbCamera = mCameraServer.startAutomaticCapture();
			cvSink = mCameraServer.getVideo();
			mUsbCamera.setFPS(10);
			mUsbCamera.setResolution(resolution[0], resolution[1]);
			cvSrcOut = mCameraServer.putVideo("src out", resolution[0], resolution[1]);
			cvSrcMask = mCameraServer.putVideo("src mask", resolution[0], resolution[1]);
		} catch (Exception e) {
			logger.warning("[CV] init failed:");
			e.printStackTrace();
		}
		Thread t = new Thread(() -> {
			Mat frame = new Mat();
			Mat dst = new Mat();
			while (!Thread.interrupted()) {
				try {
					if (0 == cvSink.grabFrame(frame)) {
						logger.warning("Error grabbing fram from camera");
					} else {
						Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2HSV);
						Core.inRange(frame, new Scalar(80, 40, 40), new Scalar(110, 360, 360), dst);

						List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
						Imgproc.findContours(dst, contours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

						List<MatOfPoint> maxContours = new ArrayList<MatOfPoint>();

						if (contours.size() >= 2) {
							for (int i = 0; i < 2; i++) {
								MatOfPoint maxContour = contours.get(0);
								double maxArea = 0;
								Iterator<MatOfPoint> iterator = contours.iterator();
								while (iterator.hasNext()) {
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
							Imgproc.drawContours(frame, maxContours, -1, new Scalar(100, 256, 0), 1);

							MatOfPoint2f cnt1 = new MatOfPoint2f(), cnt2 = new MatOfPoint2f();
							maxContours.get(0).convertTo(cnt1, CvType.CV_32F);
							maxContours.get(1).convertTo(cnt2, CvType.CV_32F);
							RotatedRect rec1 = Imgproc.minAreaRect(cnt1);
							RotatedRect rec2 = Imgproc.minAreaRect(cnt2);

							Point points[] = new Point[4];

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

	void debug(String s) {
		if (DEBUG)
			logger.warning(s);
	}
}
