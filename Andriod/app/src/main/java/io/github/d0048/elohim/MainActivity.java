package io.github.d0048.elohim;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        highH = (SeekBar) findViewById(R.id.highH);
        highH.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                GreenHSVHigh.val[0]=seekBar.getProgress();
                highHtxt.setText(seekBar.getProgress()+"");
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        highS = (SeekBar) findViewById(R.id.highS);
        highS.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                GreenHSVHigh.val[1]=seekBar.getProgress();
                highStxt.setText(seekBar.getProgress()+"");
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        highV = (SeekBar) findViewById(R.id.highV);
        highV.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                GreenHSVHigh.val[2]=seekBar.getProgress();
                highVtxt.setText(seekBar.getProgress()+"");
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        lowH = (SeekBar) findViewById(R.id.lowH);
        lowH.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                GreenHSVLow.val[0]=seekBar.getProgress();
                lowHtxt.setText(seekBar.getProgress()+"");
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        lowS = (SeekBar) findViewById(R.id.lowS);
        lowS.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                GreenHSVLow.val[1]=seekBar.getProgress();
                lowStxt.setText(seekBar.getProgress()+"");
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        lowV = (SeekBar) findViewById(R.id.lowV);
        lowV.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                GreenHSVLow.val[2]=seekBar.getProgress();
                lowVtxt.setText(seekBar.getProgress()+"");
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        highHtxt = (TextView) findViewById(R.id.HH);
        highStxt = (TextView) findViewById(R.id.HS);
        highVtxt = (TextView) findViewById(R.id.HV);
        lowHtxt = (TextView) findViewById(R.id.LH);
        lowStxt = (TextView) findViewById(R.id.LS);
        lowVtxt = (TextView) findViewById(R.id.LV);

        captureToggleBtn = (ToggleButton) findViewById(R.id.toggleButtonCapture);
        captureToggleBtn.setOnCheckedChangeListener(captureToggleFunc);
        captureToggleBtn.toggle();

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.HelloOpenCvView);
        mOpenCvCameraView.setCvCameraViewListener(this);
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

    Scalar GreenHSVLow = new Scalar(50, 50, 60), GreenHSVHigh = new Scalar(100, 100, 100);

    @Override
    //take image frame from camera modify it and display it on the screen.
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat frame = inputFrame.rgba();
        Imgproc.GaussianBlur(frame, frame, new Size(3, 3), 0);
        //Imgproc.bilateralFilter(frame,frame,9,75,75);
        Imgproc.cvtColor(frame, frame, Imgproc.COLOR_RGBA2RGB);
        Imgproc.cvtColor(frame, frame, Imgproc.COLOR_RGB2HSV);
        Core.inRange(frame, GreenHSVLow, GreenHSVHigh, frame);

        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(frame,contours,new Mat(),Imgproc.RETR_TREE,Imgproc.CHAIN_APPROX_SIMPLE);

        MatOfPoint max_contour = null;
        double maxArea = 0;
        Iterator<MatOfPoint> iterator = contours.iterator();
        while (iterator.hasNext()){
            MatOfPoint contour = iterator.next();
            double area = Imgproc.contourArea(contour);
            if(area > maxArea){
                maxArea = area;
                max_contour = contour;
            }
        }

        return frame;
    }
}
