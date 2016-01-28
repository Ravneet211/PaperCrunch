package com.example.ravneet.cameratester;
/**
 * Created by Ravneet on 1/20/16.
 */

import android.util.Log;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_core.CvScalar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeMap;

import static org.bytedeco.javacpp.opencv_core.CV_RGB;
import static org.bytedeco.javacpp.opencv_core.CvContour;
import static org.bytedeco.javacpp.opencv_core.CvMemStorage;
import static org.bytedeco.javacpp.opencv_core.CvRect;
import static org.bytedeco.javacpp.opencv_core.CvSeq;
import static org.bytedeco.javacpp.opencv_core.CvSize;
import static org.bytedeco.javacpp.opencv_core.IplConvKernel;
import static org.bytedeco.javacpp.opencv_core.IplImage;
import static org.bytedeco.javacpp.opencv_core.cvCreateImage;
import static org.bytedeco.javacpp.opencv_core.cvPoint;
import static org.bytedeco.javacpp.opencv_core.cvSize;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvLoadImage;
import static org.bytedeco.javacpp.opencv_imgproc.CV_AA;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.CV_CHAIN_APPROX_NONE;
import static org.bytedeco.javacpp.opencv_imgproc.CV_RETR_CCOMP;
import static org.bytedeco.javacpp.opencv_imgproc.CV_THRESH_BINARY_INV;
import static org.bytedeco.javacpp.opencv_imgproc.CV_THRESH_TRIANGLE;
import static org.bytedeco.javacpp.opencv_imgproc.MORPH_CROSS;
import static org.bytedeco.javacpp.opencv_imgproc.cvBoundingRect;
import static org.bytedeco.javacpp.opencv_imgproc.cvCreateStructuringElementEx;
import static org.bytedeco.javacpp.opencv_imgproc.cvCvtColor;
import static org.bytedeco.javacpp.opencv_imgproc.cvErode;
import static org.bytedeco.javacpp.opencv_imgproc.cvFindContours;
import static org.bytedeco.javacpp.opencv_imgproc.cvLine;
import static org.bytedeco.javacpp.opencv_imgproc.cvRectangle;
import static org.bytedeco.javacpp.opencv_imgproc.cvThreshold;

//import org.json.JSONObject;

public class GroceryLineBreakMatch {
    private static final String LOG_TAG = GroceryLineBreakMatch.class.getSimpleName();

    public static void main(String[] args) {
//		GroceryMatch.preProcess("/Users/sbhave/Downloads/highContrastReceipt.jpg",1);
//		GroceryMatch.preProcess("/Users/sbhave/Downloads/Walmart1101.jpg", 1);
//		GroceryMatch.preProcess("/Users/sbhave/Downloads/sampleBill.jpg", 1);
//		GroceryMatch.preProcess("/Users/sbhave/Downloads/TJ1625.jpg", 1);
//		GroceryLineBreakMatch.preProcess("/Users/sbhave/Downloads/TJ1474.jpg", 1);
//      GroceryLineBreakMatch.preProcess("/Users/sbhave/Downloads/chinese.jpg", 1);
//		GroceryLineBreakMatch.preProcess("/Users/sbhave/Downloads/TJ1625.jpg",1);
    }


    public static ArrayList<Integer> preProcess(String imgFile, int isTJ) {
        int erodeIters = 1;
        IplImage img = cvLoadImage(imgFile);
        Log.e(LOG_TAG,"Loaded image file");
        CvSize cvSize = cvSize(img.width(), img.height());
        Integer width = cvSize.width();
        Integer height = cvSize.height();
//		System.out.println(Integer.toString(width) + Integer.toString(height));
        IplImage gry=cvCreateImage(cvSize, img.depth(), 1);
        IplImage threshold = cvCreateImage(cvSize, img.depth(), 1);
        IplImage dilated = cvCreateImage(cvSize, img.depth(), 1);

        //Grayscale and Thresholding
        cvCvtColor(img, gry, CV_BGR2GRAY);
        double d = cvThreshold(gry, threshold, 155, 255, CV_THRESH_TRIANGLE);
        cvThreshold(threshold, threshold, 0, 255, CV_THRESH_BINARY_INV);
//		System.out.println("Threshold value" + Double.toString(d));
//      cvSaveImage("threshold.jpg", threshold);
        Log.e(LOG_TAG,"Saving threshold file");

        //Eroding
        IplConvKernel k = cvCreateStructuringElementEx(3,3,1,1,MORPH_CROSS);
        cvErode(threshold, dilated, k, erodeIters);
        //cvSaveImage("dilated.jpg", dilated);

        // Contours
        CvSeq contour=new CvSeq();
        CvSeq ptr = new CvSeq();
        CvRect rect = null;

        CvMemStorage memory=CvMemStorage.create();
        cvFindContours(dilated, memory, contour, Loader.sizeof(CvContour.class), CV_RETR_CCOMP, CV_CHAIN_APPROX_NONE);
        Log.e(LOG_TAG,"Found contours");
        CvScalar blue = CV_RGB(64, 64, 255);

        TreeMap<Integer, CvRect> rectToArea = new TreeMap<Integer, CvRect>(Collections.reverseOrder());

        int[] area = new int[contour.sizeof()];
        ArrayList<CvRect> rectArr = new ArrayList<CvRect>();
        int counter = 0;

        // Find all non null contours (bounding rectangles)
        for (ptr = contour; ptr != null; ptr = ptr.h_next(), counter++) {
            rect = cvBoundingRect(ptr, 0);
            int x= rect.x(),y=rect.y(),h=rect.height(),w=rect.width();
            int a = w * h;
            if (a != 0) {
                rectToArea.put(a, rect);
                rectArr.add(rect);
            }
            cvRectangle(img, cvPoint(x, y), cvPoint(x + w, y + h), blue, 1, CV_AA, 0);
        }
        Log.e(LOG_TAG,"Done with cvRectangle");

        CvRect rect1 = null;

       /* int[] xfreq = new int[width];
        int thresh_constant = 10000;
        for (int i = 0; i < width; i++) {
            int xpoll = i;
            for (int j = 0; j < rectArr.size(); j++) {
                rect1 = rectArr.get(j);
                if (rect1 != null) {
                    int x = rect1.x(); int y = rect1.y(); int w = rect1.width(); int h = rect1.height();
                    if ((xpoll < x + w) && (xpoll > x) && ((h*w) > thresh_constant)) {
                        xfreq[xpoll] += 1;
                    }
                }
            }
        }*/
        Log.e(LOG_TAG,"Calculated xfreq");
        // poll the pixels at each row and see how many contours intersect
        // the index of yfreq represents the particular y row
        // the element of the index of yfreq is how many contours intersect at that row
        int[] yfreq = new int[height];
        for (int i = 0; i < height; i++) {
            int ypoll = i;
            for (int j = 0; j < rectArr.size(); j++) {
                rect1 = rectArr.get(j);
                if (rect1 != null) {
                    int x = rect1.x(); int y = rect1.y(); int w = rect1.width(); int h = rect1.height();
                    if ((ypoll < y + h) && (ypoll > y)) {
                        yfreq[ypoll] += 1;
                    }
                }
            }
        }
        Log.e(LOG_TAG,"Calculated yFreq");

        ArrayList<Integer> lineBreaks = new ArrayList<Integer>();
        // Mark the areas where there is a 0 (or period of 0s) between two nonzero values
        // These are the line breaks
        int count = 0;
        int indicator = 0;
        while (count < yfreq.length) {
            if (yfreq[count] == 0 && indicator == 0) {
                lineBreaks.add(count);
                indicator = 1;
            } else if (yfreq[count] != 0 && indicator == 1) {
                indicator = 0;
            }
            count += 1;
        }

        for (int i = 0; i < lineBreaks.size(); i++) {
            cvLine(img, cvPoint(0, lineBreaks.get(i)), cvPoint(width, lineBreaks.get(i)), blue, 1, CV_AA, 0);
        }
        Log.e(LOG_TAG,"Drawing lines");
        // Print line breaks to see output for DEBUGGING purposes
	    /* NOTE TO RAVNEET:
	     * All you need to be concerned about is the lineBreaks arraylist which returns the y value
	     * at which there is a line break.
	     * The top and bottom boundaries of the image are always counted as y values.
	     * You can use this arrayList to crop the images at the given y values and analyze each line using OCR.
	     * One thing you may have to do is check whether on the right half of the image there is a rectangle that
	     * represents a price.
	     * If you don't get time, just crop the images as is.
	     * One quick check you can do is just to see if there are digits at all and if there are use this as
	     * an item, price match.
	    */
        //cvSaveImage("annotated.jpg", img);
        Log.e(LOG_TAG,"Saved annotated image");
        Log.e(LOG_TAG, lineBreaks.toString());
        return lineBreaks;

    }
}