
/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import edu.wpi.cscore.CvSink;
import edu.wpi.cscore.CvSource;
import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.vision.VisionPipeline;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class MyPipeline implements VisionPipeline {

    private static final Logger logger = Logger.getLogger(MyPipeline.class.getName());
    public static boolean DEBUG = true;

    public static double minRecArea = 25;
    // public CameraServer mCameraServer;
    // public UsbCamera mUsbCamera;
    CvSink cvSink;
    CvSource cvSrcOut, cvSrcMask;
    int[] resolution = { 600, 800 };
    Scalar LOWER_BOUND = new Scalar(70, 60, 60), UPPER_BOUND = new Scalar(100, 360, 360);
    public static int EXPLOSURE = -1;// TODO: Calibrate Camera EXPLOSURE

    public static boolean isCVEnabled = true;
    public static boolean isCVUsable = false;
    public static Point target = new Point(), realTarget = new Point();
    double recX = 0, recY = 0, recZ = 0;

    public MyPipeline() {
        super();
        try {
            cvSrcOut = CameraServer.getInstance().putVideo("PI Out", resolution[0], resolution[1]);
            putCVInfo();
            new Thread(() -> {
                while (!Thread.interrupted()) {
                    pollSDBConfig();
                    try {
                        Thread.sleep(400, 0);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (Exception e) {
            logger.warning("[CV] init failed:");
            e.printStackTrace();
        }
    }

    int gcIteration = 0, lazyIteration = 0;
    Mat dst = new Mat();
    List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
    List<MatOfPoint> maxContours = new ArrayList<MatOfPoint>();
    List<RotatedRect> possibleRects = new ArrayList<>();

    @Override
    public void process(Mat frame) {
        try {
            Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2HSV);
            Core.inRange(frame, LOWER_BOUND, UPPER_BOUND, dst);
            Imgproc.cvtColor(frame, frame, Imgproc.COLOR_HSV2BGR);
            contours.clear();
            maxContours.clear();
            Imgproc.findContours(dst, contours, new Mat(), Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
            Utils.release(dst);
            if (contours.size() >= 2) {
                for (int i = 0; i < 2; i++) { // find 2 largest contours
                    MatOfPoint maxContour = contours.get(0);
                    double maxArea = minRecArea;// Also threshold of min cont area
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
                        if (area / maxArea > 0.7 && area > minRecArea) {
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
                    drawRotatedRect(frame, possibleRects.get(possibleRects.size() - 1), new Scalar(0, 250, 0), 1);
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
                RotatedRect matchedRec = null;
                if ((matchedRec = searchClosestRectMatch(isLeft(centerRec), centerRec, possibleRects)) != null) {
                    Imgproc.putText(frame, isLeft(centerRec) ? "-->" : "<--", centerRec.center, Core.FONT_HERSHEY_PLAIN,
                            2, new Scalar(250, 100, 250), 1);
                    drawRotatedRect(frame, centerRec, new Scalar(256, 160, 256), 4);
                    Imgproc.line(frame, centerRec.center, matchedRec.center, new Scalar(250, 100, 250), 5);
                    target.x = (centerRec.center.x + matchedRec.center.x) / 2;
                    target.y = (centerRec.center.y + matchedRec.center.y) / 2;
                    label(frame, center, new Scalar(0, 0, 0));
                    label(frame, target, new Scalar(250, 50, 50));
                    realTarget = target.clone();
                    target.x -= center.x;
                    target.y -= center.y;
                    isCVUsable = true;
                    recX = center.x > 0 ? 0.3 : -0.3;
                    recY = Math.abs(center.x) < 40 ? 0.4 : 0;
                } else {
                    isCVUsable = false;
                }
            } else {
                isCVUsable = false;
            }
            cvSrcOut.putFrame(frame);
            // Utils.release(frame);
            // Garbage Collection
            if (gcIteration++ > 60) {
                System.gc();
                gcIteration = 0;
            }
        } catch (Exception e) {
            logger.warning("Error in CV Thread:");
            e.printStackTrace();
            isCVUsable = false;
        }

    }

    public final void write_info() {
        SmartDashboard.putBoolean("[PI]CV Target Status", isCVUsable);
        SmartDashboard.putNumber("[PI]Recommended X", recX);
        SmartDashboard.putNumber("[PI]Recommended Y", recY);
        SmartDashboard.putNumber("[PI]Recommended Z", recZ);
        SmartDashboard.putNumber("[PI]Auto Y Offset", target.y);
    }

    public final void putCVInfo() {
        SmartDashboard.putNumber("HIGH H", UPPER_BOUND.val[0]);
        SmartDashboard.putNumber("HIGH S", UPPER_BOUND.val[1]);
        SmartDashboard.putNumber("HIGH V", UPPER_BOUND.val[2]);
        SmartDashboard.putNumber("LOW H", LOWER_BOUND.val[0]);
        SmartDashboard.putNumber("LOW S", LOWER_BOUND.val[1]);
        SmartDashboard.putNumber("LOW V", LOWER_BOUND.val[2]);
        SmartDashboard.putNumber("EXPLOSURE(-1: auto)", EXPLOSURE);
        SmartDashboard.putNumber("Minimun Rectangle Area", minRecArea);
    }

    public final void pollSDBConfig() {
        try {
            UPPER_BOUND.val[0] = SmartDashboard.getNumber("HIGH H", UPPER_BOUND.val[0]);
            UPPER_BOUND.val[1] = SmartDashboard.getNumber("HIGH S", UPPER_BOUND.val[1]);
            UPPER_BOUND.val[2] = SmartDashboard.getNumber("HIGH V", UPPER_BOUND.val[2]);
            LOWER_BOUND.val[0] = SmartDashboard.getNumber("LOW H", LOWER_BOUND.val[0]);
            LOWER_BOUND.val[1] = SmartDashboard.getNumber("LOW S", LOWER_BOUND.val[1]);
            LOWER_BOUND.val[2] = SmartDashboard.getNumber("LOW V", LOWER_BOUND.val[2]);
            EXPLOSURE = (int) SmartDashboard.getNumber("EXPLOSURE(-1: auto)", EXPLOSURE);
        } catch (Exception e) {
            logger.warning("[CV] poll failed:" + e.getMessage());
        }
    }

    static Point[] vertices = new Point[4];

    static final void drawRotatedRect(Mat image, RotatedRect rotatedRect, Scalar color, int thickness) {
        rotatedRect.points(vertices);
        MatOfPoint points = new MatOfPoint(vertices);
        Imgproc.drawContours(image, Arrays.asList(points), -1, color, thickness);
        Utils.releaseMoP(points);
    }

    static final boolean isLeft(RotatedRect rec) {
        return rec.angle < -45;
    }

    // Right one: rot > -45
    // Left one: rot < -45
    // dir=1 -> search right
    // dir=0 -> search left
    static final RotatedRect searchClosestRectMatch(boolean dir, RotatedRect centerRec, List<RotatedRect> rects) {
        RotatedRect matchRec = null;
        if (rects.size() < 2)
            return null;
        double minDist = 99999 * (dir ? 1 : -1);
        double dist;
        for (RotatedRect rec : rects) {
            dist = rec.center.x - centerRec.center.x; // positive when search right
            if (dir && !isLeft(rec) && dist > 0 && (dist) < Math.abs(minDist)) {// search right for a right one
                if (Math.abs(centerRec.center.y - rec.center.y) < centerRec.size.width * 3) {
                    minDist = dist;
                    matchRec = rec;
                }
            } else if (!dir && isLeft(rec) && dist < 0 && (dist) > minDist) { // search left for a left one
                if (Math.abs(centerRec.center.y - rec.center.y) < centerRec.size.width * 3) {
                    minDist = dist;
                    matchRec = rec;
                }
            }
        }
        return matchRec;
    }

    static final Point centerOf(Mat m) {
        Point center = new Point();
        center.x = m.width() / 2;
        center.y = m.height() / 2;
        return center;
    }

    static final void label(Mat src, Point p, Scalar color) {
        Imgproc.drawMarker(src, p, color, Imgproc.MARKER_CROSS, 8, 2, 1);
    }

    static final double euclideanDistance(Point a, Point b) {
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

    void debug(String s) {
        if (DEBUG)
            logger.warning(s);
    }
}

class Utils {
    private static final Logger logger = Logger.getLogger(Utils.class.getName());
    public static boolean DEBUG = false;

    public static double[] absVecRet = { 0, 0 };// Y,X

    public static final double[] absoluteVector2Relative(double relY, double relX, double absHeading) {
        absHeading = (absHeading / 360) * 2 * Math.PI;
        absVecRet[1] = Math.cos(absHeading) * relX - Math.sin(absHeading) * relY; // cal X
        absVecRet[0] = Math.sin(absHeading) * relX + Math.cos(absHeading) * relY; // cal Y
        return absVecRet;
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
