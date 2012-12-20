package com.group12.oose.fakeface;

import java.text.DecimalFormat;

import org.opencv.core.Core;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;


/**
 * this class set face detector parameter
 * this is part of face detection example provided by OpenCV.
 * @author JHU Fall2012 OOSE group 12
 */
public class FpsMeter {
    private static final String TAG       = "OCVSample::FpsMeter";
    int                         step;
    int                         framesCouner;
    double                      freq;
    long                        prevFrameTime;
    String                      strfps;
    DecimalFormat               twoPlaces = new DecimalFormat("0.00");
    Paint                       paint;

    /**
     * initialize face detector
     */
    public void init() {
        step = 20;
        framesCouner = 0;
        freq = Core.getTickFrequency();
        prevFrameTime = Core.getTickCount();
        strfps = "";

        paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setTextSize(50);
    }

    /**
     * measure face number
     */
    public void measure() {
        framesCouner++;
        if (framesCouner % step == 0) {
            long time = Core.getTickCount();
            double fps = step * freq / (time - prevFrameTime);
            prevFrameTime = time;
            DecimalFormat twoPlaces = new DecimalFormat("0.00");
            strfps = twoPlaces.format(fps) + " FPS";
            Log.i(TAG, strfps);
        }
    }

    /**
     * show some information on the sreen
     */
    public void draw(Canvas canvas, float offsetx, float offsety) {
        canvas.drawText(strfps, 20 + offsetx, 10 + 50 + offsety, paint);
    }

}
