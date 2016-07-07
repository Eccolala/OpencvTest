package com.example.woops.opencvtest;


import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.VideoView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class MainActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private static final String TAG = "OCVSample::Activity";

    public static final int VIEW_MODE_RGBA = 0;
    public static final int VIEW_MODE_HIST = 1;
    public static final int VIEW_MODE_CANNY = 2;
    public static final int VIEW_MODE_SEPIA = 3;
    public static final int VIEW_MODE_SOBEL = 4;
    public static final int VIEW_MODE_ZOOM = 5;
    public static final int VIEW_MODE_PIXELIZE = 6;
    public static final int VIEW_MODE_POSTERIZE = 7;

    private MenuItem mItemPreviewRGBA;
    private MenuItem mItemPreviewHist;
    private MenuItem mItemPreviewCanny;
    private MenuItem mItemPreviewSepia;
    private MenuItem mItemPreviewSobel;
    private MenuItem mItemPreviewZoom;
    private MenuItem mItemPreviewPixelize;
    private MenuItem mItemPreviewPosterize;
    private CameraBridgeViewBase mOpenCvCameraView;

    private Size mSize0;

    private Mat mIntermediateMat;
    private Mat mMat0;
    private MatOfInt mChannels[];
    private MatOfInt mHistSize;
    private int mHistSizeNum = 20;
    private MatOfFloat mRanges;
    private Scalar mColorsRGB[];
    private Scalar mColorsHue[];
    private Scalar mWhilte;
    private Point mP1;
    private Point mP2;
    private float mBuff[];
    private Mat mSepiaKernel;

    public static int viewMode = VIEW_MODE_RGBA;

    private VideoView mVideoView;


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
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

    public MainActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);


        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial2_activity_surface_view);
        mOpenCvCameraView.setVisibility(CameraBridgeViewBase.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        mVideoView = (VideoView) findViewById(R.id.video_view);
        mVideoView.setVideoPath("android.resource://" + getPackageName() + "/" + R.raw.video);
        mVideoView.start();



    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");
        mItemPreviewRGBA = menu.add("Preview RGBA");
        mItemPreviewHist = menu.add("Histograms");
        mItemPreviewCanny = menu.add("Canny");
        mItemPreviewSepia = menu.add("Sepia");
        mItemPreviewSobel = menu.add("Sobel");
        mItemPreviewZoom = menu.add("Zoom");
        mItemPreviewPixelize = menu.add("Pixelize");
        mItemPreviewPosterize = menu.add("Posterize");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
        if (item == mItemPreviewRGBA)
            viewMode = VIEW_MODE_RGBA;
        if (item == mItemPreviewHist)
            viewMode = VIEW_MODE_HIST;
        else if (item == mItemPreviewCanny)
            viewMode = VIEW_MODE_CANNY;
        else if (item == mItemPreviewSepia)
            viewMode = VIEW_MODE_SEPIA;
        else if (item == mItemPreviewSobel)
            viewMode = VIEW_MODE_SOBEL;
        else if (item == mItemPreviewZoom)
            viewMode = VIEW_MODE_ZOOM;
        else if (item == mItemPreviewPixelize)
            viewMode = VIEW_MODE_PIXELIZE;
        else if (item == mItemPreviewPosterize)
            viewMode = VIEW_MODE_POSTERIZE;
        return true;
    }

    public void onCameraViewStarted(int width, int height) {
        mIntermediateMat = new Mat();
        mSize0 = new Size();
        mChannels = new MatOfInt[]{new MatOfInt(0), new MatOfInt(1), new MatOfInt(2)};
        mBuff = new float[mHistSizeNum];
        mHistSize = new MatOfInt(mHistSizeNum);
        mRanges = new MatOfFloat(0f, 256f);
        mMat0 = new Mat();
        mColorsRGB = new Scalar[]{new Scalar(200, 0, 0, 255), new Scalar(0, 200, 0, 255), new Scalar(0, 0, 200, 255)};
        mColorsHue = new Scalar[]{
                new Scalar(255, 0, 0, 255), new Scalar(255, 60, 0, 255), new Scalar(255, 120, 0, 255), new Scalar(255, 180, 0, 255), new Scalar(255, 240, 0, 255),
                new Scalar(215, 213, 0, 255), new Scalar(150, 255, 0, 255), new Scalar(85, 255, 0, 255), new Scalar(20, 255, 0, 255), new Scalar(0, 255, 30, 255),
                new Scalar(0, 255, 85, 255), new Scalar(0, 255, 150, 255), new Scalar(0, 255, 215, 255), new Scalar(0, 234, 255, 255), new Scalar(0, 170, 255, 255),
                new Scalar(0, 120, 255, 255), new Scalar(0, 60, 255, 255), new Scalar(0, 0, 255, 255), new Scalar(64, 0, 255, 255), new Scalar(120, 0, 255, 255),
                new Scalar(180, 0, 255, 255), new Scalar(255, 0, 255, 255), new Scalar(255, 0, 215, 255), new Scalar(255, 0, 85, 255), new Scalar(255, 0, 0, 255)
        };
        mWhilte = Scalar.all(255);
        mP1 = new Point();
        mP2 = new Point();

        // Fill sepia kernel
        mSepiaKernel = new Mat(4, 4, CvType.CV_32F);
        mSepiaKernel.put(0, 0, /* R */0.189f, 0.769f, 0.393f, 0f);
        mSepiaKernel.put(1, 0, /* G */0.168f, 0.686f, 0.349f, 0f);
        mSepiaKernel.put(2, 0, /* B */0.131f, 0.534f, 0.272f, 0f);
        mSepiaKernel.put(3, 0, /* A */0.000f, 0.000f, 0.000f, 1f);
    }

    public void onCameraViewStopped() {
        // Explicitly deallocate Mats
        if (mIntermediateMat != null)
            mIntermediateMat.release();

        mIntermediateMat = null;
    }


    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat rgba = inputFrame.rgba();
        Size sizeRgba = rgba.size();
//        List<MyPoint> pList = new ArrayList<>();

        List<Double> mXlist = new ArrayList<>();
        List<Double> mYlist = new ArrayList<>();

        Mat rgbaInnerWindow;

        int rows = (int) sizeRgba.height;
        int cols = (int) sizeRgba.width;

        int left = cols / 8;
        int top = rows / 8;

        int width = cols * 3 / 4;
        int height = rows * 3 / 4;




                Mat hist = new Mat();
                int thikness = (int) (sizeRgba.width / (mHistSizeNum + 10) / 5);
                if (thikness > 5) thikness = 5;
                int offset = (int) ((sizeRgba.width - (5 * mHistSizeNum + 4 * 10) * thikness) / 2);
                // RGB
//                for (int c = 0; c < 3; c++) {
//                    Imgproc.calcHist(Arrays.asList(rgba), mChannels[c], mMat0, hist, mHistSize, mRanges);
//                    Core.normalize(hist, hist, sizeRgba.height / 2, 0, Core.NORM_INF);
//                    hist.get(0, 0, mBuff);
//                    for (int h = 0; h < mHistSizeNum; h++) {
//                        mP1.x = mP2.x = offset + (c * (mHistSizeNum + 10) + h) * thikness;
//                        mP1.y = sizeRgba.height - 1;
//                        mP2.y = mP1.y - 2 - (int) mBuff[h];
//                        Imgproc.line(rgba, mP1, mP2, mColorsRGB[c], thikness);
//                    }
//                }
                // Value and Hue
                Imgproc.cvtColor(rgba, mIntermediateMat, Imgproc.COLOR_RGB2HSV_FULL);
                // Value
//                Imgproc.calcHist(Arrays.asList(mIntermediateMat), mChannels[2], mMat0, hist, mHistSize, mRanges);
//                Core.normalize(hist, hist, sizeRgba.height / 2, 0, Core.NORM_INF);
//                hist.get(0, 0, mBuff);
//                for (int h = 0; h < mHistSizeNum; h++) {
//                    mP1.x = mP2.x = offset + (3 * (mHistSizeNum + 10) + h) * thikness;
//                    mP1.y = sizeRgba.height - 1;
//                    mP2.y = mP1.y - 2 - (int) mBuff[h];
//                    Imgproc.line(rgba, mP1, mP2, mWhilte, thikness);
//                }
                // Hue
                Imgproc.calcHist(Arrays.asList(mIntermediateMat), mChannels[0], mMat0, hist, mHistSize, mRanges);
                Core.normalize(hist, hist, sizeRgba.height / 2, 0, Core.NORM_INF);
                hist.get(0, 0, mBuff);
                for (int h = 0; h < mHistSizeNum; h++) {
                    mP1.x = mP2.x = (4 * (mHistSizeNum + 10) + h) * thikness - 200;
                    mP1.y = sizeRgba.height - 1;
                    mP2.y = mP1.y - 2 - (int) mBuff[h];
//                    Imgproc.line(rgba, mP1, mP2, mColorsHue[h], thikness);

//                    MyPoint mPoint = new MyPoint(mP1.x, mP2.y);
                    //将每条线的横坐标保存起来
//                    pList.add(mPoint);

                    mXlist.add(mP1.x);
                    mYlist.add(mP2.y);

//                    Log.d("MyPoint x", String.valueOf(mXlist.get));
//                    Log.d("MyPoint y", String.valueOf(mPoint.y));
                }

                int position = mYlist.indexOf(Collections.min(mYlist));


                if (position < 5){
                    Imgproc.putText(rgba, "name:banana", new Point(550, 150), 7, 3.0, new Scalar(199, 199, 57),4);
                    Imgproc.putText(rgba, "price:2$", new Point(550, 250), 7, 3.0, new Scalar(199, 199, 57),4);
                    Imgproc.putText(rgba, "advice:a little", new Point(550, 350), 7, 3.0, new Scalar(199, 199, 57),4);
                    Imgproc.putText(rgba, "VA:0.01g", new Point(550, 450), 7, 3.0, new Scalar(199, 199, 57),4);
                    Imgproc.putText(rgba, "Ca:7mg", new Point(550, 550), 7, 3.0, new Scalar(199, 199, 57),4);
                }else if (position > 16){
                    Imgproc.putText(rgba, "name:apple", new Point(600, 200), 4, 4.0, new Scalar(199, 64, 57));
                    Imgproc.putText(rgba, "price:12$", new Point(600, 350), 5, 4.0, new Scalar(199, 64, 57));
                    Imgproc.putText(rgba, "advice:you can eat some", new Point(600, 500), 6, 4.0, new Scalar(199, 64, 57));
                }else {
                    Imgproc.putText(rgba, "name:eggplant", new Point(550, 150), 7, 3.0, new Scalar(199, 64, 57),4);
                    Imgproc.putText(rgba, "price:5$", new Point(550, 250), 7, 3.0, new Scalar(199, 64, 57),4);
                    Imgproc.putText(rgba, "advice:Cannot eat", new Point(550, 350), 7, 3.0, new Scalar(199, 64, 57),4);
                    Imgproc.putText(rgba, "protein:2.3g", new Point(550, 450), 7, 3.0, new Scalar(199, 64, 57),4);
                    Imgproc.putText(rgba, "fat:0.1g", new Point(550, 550), 7, 3.0, new Scalar(199, 64, 57),4);
                }
                Log.d("number",position + "");
//                Imgproc.putText(rgba, String.valueOf(position), new Point(800, 360), 4, 1.0, new Scalar(69, 203, 247));







        return rgba;
    }

}
