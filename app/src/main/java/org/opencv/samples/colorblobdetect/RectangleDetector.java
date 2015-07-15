package org.opencv.samples.colorblobdetect;

import java.util.ArrayList;
import java.util.List;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import android.graphics.Bitmap;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

/**
 * Created by domi on 06.07.15.
 */
public class RectangleDetector {

    Mat mDilatedMask = new Mat();
    Mat mHierarchy = new Mat();
    Mat mHierarchyWhite = new Mat();

    Mat hsvMat = new Mat();
    Mat hsvMatWhite = new Mat();
    Scalar lowerBlue = new Scalar(110, 50, 50);
    Scalar upperBlue = new Scalar(130, 255, 255);

    Scalar lowerWhite = new Scalar(0, 180, 0);
    Scalar upperWhite = new Scalar(255, 255, 255);

    Mat blueMask = new Mat();
    Mat whiteMask = new Mat();
    Mat dilatedMask = new Mat();

    Mat pyrDownMat = new Mat();

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
        Core.bitwise_and(hsvMat, hsvMat, hsvMat, blueMask);

        Imgproc.dilate(blueMask, dilatedMask, new Mat());

        Imgproc.findContours(dilatedMask, contours, mHierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);

        Imgproc.drawContours(rgbaImage, contours, -1, contourColor);


    }

    private Size kernelSize = new Size(5, 5);
    private Mat closedMat = new Mat();
    private List<MatOfPoint> contours = new ArrayList<>();

    public Rect trackBlue(Mat rgbaImage) {
        // resize
        Imgproc.pyrDown(rgbaImage, pyrDownMat);
        Imgproc.pyrDown(pyrDownMat, pyrDownMat);
        // convert to HSV
        Imgproc.cvtColor(pyrDownMat, hsvMat, Imgproc.COLOR_RGB2HSV);
        // blur already inculded in pyrDown
        // Imgproc.GaussianBlur(hsvMat, hsvMat, kernelSize, 0);

        // only blue
        Core.inRange(hsvMat, lowerBlue, upperBlue, blueMask);
        // dilate and erode - bridging the gap between areas
        // Imgproc.dilate(blueMask, dilatedMask, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, kernelSize));
        // dilate and erode in one operation to close gaps, see http://docs.opencv.org/modules/imgproc/doc/filtering.html#void morphologyEx(InputArray src, OutputArray dst, int op, InputArray kernel,
        // Point anchor, int iterations, int borderType, const Scalar& borderValue)
        Imgproc.morphologyEx(blueMask, closedMat, Imgproc.MORPH_CLOSE, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, kernelSize));

        // find contours
        contours.clear();
        Imgproc.findContours(closedMat, contours, mHierarchyWhite, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);

        // find largest contour
        double maxContourArea = 0.0;
        int largestContour = 0;
        for (int i = 0; i < contours.size(); i++) {
            double area = Imgproc.contourArea(contours.get(i));
            if (area > maxContourArea) {
                maxContourArea = area;
                largestContour = i;
            }
        }

        Rect rect = new Rect();

        if (!contours.isEmpty()) {
            rect = Imgproc.boundingRect(contours.get(largestContour));
            rect.x = rect.x * 4;
            rect.y = rect.y * 4;
            rect.width = rect.width * 4;
            rect.height = rect.height * 4;
            Imgproc.rectangle(rgbaImage, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), contourColor);
        }

        return rect;
    }

    private Mat closedMatWhite = new Mat();

    public Rect trackWhite(Mat rgbaImage, Rect roi) {

        Rect rect = new Rect();

        if (roi.area() > 0.0) {
            Mat cropped = new Mat(rgbaImage, roi);

            // convert to HSV
            Imgproc.cvtColor(cropped, hsvMatWhite, Imgproc.COLOR_RGB2HLS);

            // only white
            Core.inRange(hsvMatWhite, lowerWhite, upperWhite, whiteMask);

            // Imgproc.dilate(whiteMask, closedMatWhite, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, kernelSize));
            Imgproc.morphologyEx(whiteMask, closedMatWhite, Imgproc.MORPH_OPEN, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, kernelSize));

            // Imgproc.resize(closedMatWhite, rgbaImage, new Size(rgbaImage.width(), rgbaImage.height()));

            // find contours
            contours.clear();
            Imgproc.findContours(closedMatWhite, contours, mHierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);

            // find largest contour
            double maxContourArea = 0.0;
            int largestContour = 0;
            for (int i = 0; i < contours.size(); i++) {
                double area = Imgproc.contourArea(contours.get(i));
                if (area > maxContourArea) {
                    maxContourArea = area;
                    largestContour = i;
                }
            }

            if (!contours.isEmpty()) {
                rect = Imgproc.boundingRect(contours.get(largestContour));
                // Mat boundPoints = new Mat();
                // Imgproc.boxPoints(rect, boundPoints);
                // Imgproc.drawContours(rgbaImage, Imgproc.boxPoints();, -1, contourColor);
                if (rect.x + roi.x - 10 < rgbaImage.cols()) {
                    rect.x = rect.x + roi.x - 10;
                }
                if (rect.y + roi.y - 10 < rgbaImage.rows()) {
                    rect.y = rect.y + roi.y - 10;
                }
                if (rect.x + rect.width + 20 < rgbaImage.cols()) {
                    rect.width = rect.width + 20;
                }
                if (rect.y + rect.height + 20 < rgbaImage.rows()) {
                    rect.height = rect.height + 20;
                }
                Imgproc.rectangle(rgbaImage, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), contourColor);
            }
        }
        return rect;
    }

    public String processOcr(Mat rgbaImage, Rect roi) {
        String recognizedText = "";
        if (roi.area() > 0.0) {
            Mat cropped = new Mat(rgbaImage, roi);
            // pyrDownMat = rgbaImage;
            // Imgproc.pyrDown(rgbaImage, pyrDownMat);
            // Imgproc.pyrDown(pyrDownMat, pyrDownMat);

            // convert to grayscale
            Imgproc.cvtColor(cropped, cropped, Imgproc.COLOR_RGB2GRAY);
            Imgproc.threshold(cropped, cropped, 128, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
            Core.bitwise_not(cropped, cropped);

            Imgproc.resize(cropped, rgbaImage, new Size(rgbaImage.width(), rgbaImage.height()));

            // Imgproc.adaptiveThreshold(pyrDownMat, pyrDownMat, );
            Bitmap bmp = Bitmap.createBitmap(cropped.width(), cropped.height(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(cropped, bmp);
            baseApi.setImage(bmp);
            recognizedText = baseApi.getUTF8Text();
            Log.i("OCR", recognizedText);
            baseApi.clear();

        }
        return recognizedText;
    }
}
