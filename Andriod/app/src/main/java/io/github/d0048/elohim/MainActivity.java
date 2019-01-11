package io.github.d0048.elohim;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    static {
        if (!OpenCVLoader.initDebug()) {
        }
    }

    private ToggleButton captureToggleBtn;
    private CameraBridgeViewBase mOpenCvCameraView;

    ToggleButton.OnCheckedChangeListener captureToggleFunc = new ToggleButton.OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton toggleButton, boolean isChecked) {
            if (isChecked) {//enable
                toggleButton.setText("On");
                mOpenCvCameraView.enableView();
            } else {
                toggleButton.setText("OFF");
                mOpenCvCameraView.disableView();
            }
        }
    };
    private SeekBar highH, highS, highV, lowH, lowS, lowV;
    private TextView highHtxt, highStxt, highVtxt, lowHtxt, lowStxt, lowVtxt;

    EditText roborioIPEditText, modeEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        highH = (SeekBar) findViewById(R.id.highH);
        highH.setProgress((int) GreenHSVHigh.val[0]);
        highH.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                GreenHSVHigh.val[0] = seekBar.getProgress();
                highHtxt.setText(seekBar.getProgress() + "");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        highS = (SeekBar) findViewById(R.id.highS);
        highS.setProgress((int) GreenHSVHigh.val[1]);
        highS.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                GreenHSVHigh.val[1] = seekBar.getProgress();
                highStxt.setText(seekBar.getProgress() + "");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        highV = (SeekBar) findViewById(R.id.highV);
        highV.setProgress((int) GreenHSVHigh.val[2]);
        highV.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                GreenHSVHigh.val[2] = seekBar.getProgress();
                highVtxt.setText(seekBar.getProgress() + "");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        lowH = (SeekBar) findViewById(R.id.lowH);
        lowH.setProgress((int) GreenHSVLow.val[0]);
        lowH.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                GreenHSVLow.val[0] = seekBar.getProgress();
                lowHtxt.setText(seekBar.getProgress() + "");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        lowS = (SeekBar) findViewById(R.id.lowS);
        lowS.setProgress((int) GreenHSVLow.val[1]);
        lowS.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                GreenHSVLow.val[1] = seekBar.getProgress();
                lowStxt.setText(seekBar.getProgress() + "");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        lowV = (SeekBar) findViewById(R.id.lowV);
        lowV.setProgress((int) GreenHSVLow.val[2]);
        lowV.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                GreenHSVLow.val[2] = seekBar.getProgress();
                lowVtxt.setText(seekBar.getProgress() + "");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        highHtxt = (TextView) findViewById(R.id.HH);
        highHtxt.setText(GreenHSVHigh.val[0] + "");
        highStxt = (TextView) findViewById(R.id.HS);
        highStxt.setText(GreenHSVHigh.val[1] + "");
        highVtxt = (TextView) findViewById(R.id.HV);
        highVtxt.setText(GreenHSVHigh.val[2] + "");
        lowHtxt = (TextView) findViewById(R.id.LH);
        lowHtxt.setText(GreenHSVLow.val[0] + "");
        lowStxt = (TextView) findViewById(R.id.LS);
        lowStxt.setText(GreenHSVLow.val[1] + "");
        lowVtxt = (TextView) findViewById(R.id.LV);
        lowVtxt.setText(GreenHSVLow.val[2] + "");

        modeEditText = (EditText) findViewById(R.id.modeText);

        roborioIPEditText = (EditText) findViewById(R.id.roborioIPText);
        roborioIPEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().endsWith("-")) {
                    //TODO: capture and connect ip address
                    try {
                        String addr = editable.toString().split(":")[0];
                        int port = Integer.parseInt(editable.toString().split(":")[1]);
                        Socket clientSocket = new Socket(addr, port);
                        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
                        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        captureToggleBtn = (ToggleButton) findViewById(R.id.toggleButtonCapture);
        captureToggleBtn.setOnCheckedChangeListener(captureToggleFunc);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.HelloOpenCvView);
        mOpenCvCameraView.setCvCameraViewListener(this);
        captureToggleBtn.toggle();
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        //check that you are able to use opencv
        public void onManagerConnected(int status) {
            switch (status) {
                //if you can initialize openCV successfully then record so in log
                case LoaderCallbackInterface.SUCCESS: {
                    mOpenCvCameraView.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        //OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallback);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            //disable camera connection and stop the delivery of frames
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            //disable camera connection and stop the delivery of frames
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        //Display the image constantly regardless of phone orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    @Override
    public void onCameraViewStopped() {
    }

    Scalar GreenHSVLow = new Scalar(56, 65, 75), GreenHSVHigh = new Scalar(120, 350, 350);

    @Override
    //take image frame from camera modify it and display it on the screen.
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        Mat frame = inputFrame.rgba(), frameHSV = frame.clone(), mask = frame.clone();
        Imgproc.GaussianBlur(frame, frame, new Size(3, 3), 0);
        Imgproc.cvtColor(frame, frame, Imgproc.COLOR_RGBA2RGB);
        Imgproc.cvtColor(frame, frameHSV, Imgproc.COLOR_RGB2HSV);
        Core.inRange(frameHSV, GreenHSVLow, GreenHSVHigh, mask);

        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(mask, contours, mask, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        List<MatOfPoint> maxContours = new ArrayList<>();

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

            if (!contours.isEmpty()) {
                double maxArea = Imgproc.contourArea(maxContours.get(0));
                for (MatOfPoint contour : contours) {
                    double area = Imgproc.contourArea(contour);
                    if (area / maxArea > 0.65)
                        maxContours.add(contour);
                }
            }
            Imgproc.drawContours(frame, maxContours, -1, new Scalar(100, 256, 0), 3);


            List<RotatedRect> possibleRects = new ArrayList<>();
            MatOfPoint2f cnt = new MatOfPoint2f();
            for (MatOfPoint c : maxContours) {
                c.convertTo(cnt, CvType.CV_32F);
                possibleRects.add(Imgproc.minAreaRect(cnt));
            }
            cnt.release();

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
                drawRect(frame,rec,new Scalar(50,50,150),3);
            }
            drawRect(frame,centerRec,new Scalar(250,100,250),3);

            label(frame, center, new Scalar(250, 50, 50));
        }

        for (MatOfPoint c : maxContours)
            c.release();
        for (MatOfPoint c : contours)
            c.release();
        System.gc();
        switch (Integer.parseInt(modeEditText.getText().toString())) {
            case 0:
                mask.release();
                frameHSV.release();
                return frame;
            default:
                mask.copyTo(frame);
                frameHSV.release();
                mask.release();
                return frame;
        }
    }

    public Point centerOf(Mat m) {
        Point center = new Point();
        center.x = m.width() / 2;
        center.y = m.height() / 2;
        return center;
    }

    public void label(Mat src, Point p, Scalar color) {
        Imgproc.drawMarker(src, p, color, Imgproc.MARKER_CROSS, 100, 4, 1);
    }

    public double euclideanDistance(Point a, Point b) {
        double distance = 0.0;
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
    Point points[] = new Point[4];
    public void drawRect(Mat src, RotatedRect rec,Scalar color, int thickness){
        try{
        rec.points(points);
        for (int i = 0; i < 4; ++i) {
            Imgproc.line(src, points[i], points[(i + 1) % 4], color, thickness);
        }}catch (Exception e){System.out.println("Something went wrong in drawRect function: " + e.getMessage());}
    }
}
