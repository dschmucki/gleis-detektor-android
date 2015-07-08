package org.opencv.samples.colorblobdetect;

import android.graphics.Bitmap;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by domi on 06.07.15.
 */
public class RectangleDetector {

    Mat mDilatedMask = new Mat();
    Mat mHierarchy = new Mat();

    Mat hsvMat = new Mat();
    Scalar lowerBlue = new Scalar(110, 50, 50);
    Scalar upperBlue = new Scalar(130, 255, 255);

    Mat blueMask = new Mat();
    Mat dilatedMask = new Mat();

    Scalar contourColor = new Scalar(255, 0, 0, 255);

    private List<MatOfPoint> mContours = new ArrayList<MatOfPoint>();

    TessBaseAPI baseApi = new TessBaseAPI();

    public RectangleDetector() {
        baseApi.setDebug(true);
        baseApi.init("/storage/emulated/0/download/", "eng");
    }

    public void process(Mat rgbaImage) {

        List<MatOfPoint> contours = new ArrayList<>();

        // convert RGBA to HSV
        Imgproc.cvtColor(rgbaImage, hsvMat, Imgproc.COLOR_RGB2HSV);
        // filter blue
        Core.inRange(hsvMat, lowerBlue, upperBlue, blueMask);
        // create picture which only contains blue - necessary?
//        Core.bitwise_and(hsvMat, hsvMat, hsvMat, blueMask);


//        Imgproc.Canny(rgbaImage, rgbaImage, );

        Imgproc.dilate(blueMask, dilatedMask, new Mat());

        Imgproc.findContours(dilatedMask, contours, mHierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
//
//        contours.clear();
        Imgproc.drawContours(rgbaImage, contours, -1, contourColor);

//        mContours.clear();
//        Iterator<MatOfPoint> each = contours.iterator();
//        while (each.hasNext()) {
//            MatOfPoint contour = each.next();
//            if (Imgproc.contourArea(contour) > mMinContourArea * maxArea) {
//            Core.multiply(contour, new Scalar(4, 4), contour);
//            mContours.add(contour);
//            }
//        }

    }

    public void processOcr(Mat rgbaImage) {
        Imgproc.pyrDown(rgbaImage, rgbaImage);
        Bitmap bmp = Bitmap.createBitmap(rgbaImage.width(), rgbaImage.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(rgbaImage, bmp);
        baseApi.setImage(bmp);
        String recognizedText = baseApi.getUTF8Text();
        Log.i("OCR", recognizedText);
        baseApi.clear();
    }

//    public List<MatOfPoint> getContours() {
//        return mContours;
//    }
}
