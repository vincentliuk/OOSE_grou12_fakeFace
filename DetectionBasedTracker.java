package com.group12.oose.fakeface;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
/**
 * This class defines the facetracker
 * This is part of an example provided by OpenCV.
 * @author JHU Fall2012 OOSE group 12
 */
public class DetectionBasedTracker
{
    public DetectionBasedTracker(String cascadeName, int minFaceSize) {
        mNativeObj = nativeCreateObject(cascadeName, minFaceSize);
    }
    /**
     * start face tracker
     */
    public void start() {
        nativeStart(mNativeObj);
    }
    /**
     * stop face tracker
     */
    public void stop() {
        nativeStop(mNativeObj);
    }
    /**
     * set minimize Face size, faces smaller than this size will never be detected.
     */
    public void setMinFaceSize(int size) {
        nativeSetFaceSize(mNativeObj, size);
    }
    /**
     * detect face
     */
    public void detect(Mat imageGray, MatOfRect faces) {
        nativeDetect(mNativeObj, imageGray.getNativeObjAddr(), faces.getNativeObjAddr());
    }
    /**
     * release face tracker
     */
    public void release() {
        nativeDestroyObject(mNativeObj);
        mNativeObj = 0;
    }

    private long mNativeObj = 0;

    private static native long nativeCreateObject(String cascadeName, int minFaceSize);
    private static native void nativeDestroyObject(long thiz);
    private static native void nativeStart(long thiz);
    private static native void nativeStop(long thiz);
    private static native void nativeSetFaceSize(long thiz, int size);
    private static native void nativeDetect(long thiz, long inputImage, long faces);
}
