package com.group12.oose.fakeface;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import android.graphics.Bitmap;
import android.util.Log;
/**
 * This class stores distortion parameter, distorted face and implements face distortion.
 * The header to face submatrix is the input. The corner of mouth is detected by the sobel edge detection.
 * Eye corner is predefined because it is difficult to detect the corner of eye.
 * Distortion is applied to the face according to the distortion parameter which is set by the slider.
 * Finally, the distorted face is cloned and stored. Since the header to face submatrix is used as the input, no copy back is needed.
 *   
 * @author JHU Fall2012 OOSE group 12
 *
 */

public class Expression {
	private static final String    TAG                 = "OOSE::Expression";
	/**
	 * distorted face
	 */
	private Mat                     faceImageRgbMat; 
	/**
	 * a header to the face submatrix
	 */
	private Mat						faceImageRgbForDistortion; 
	/**
	 * face matrix in gray which is used for sobel edge detection
	 */
    private Mat                     faceImageGrayMat;
    /**
     * coordinate X of detected left corner of mouth detected with edge detection  
     */
    private int 					mouthOriginalLeftX; 
    /**
     * coordinate Y of detected left corner of mouth detected with edge detection
     */
    private int 					mouthOriginalLeftY; 
    /**
     * //coordinate X of detected right corner of mouth detected with edge detection
     */
    private int 					mouthOriginalRightX;  
    /**
     * coordinate Y of detected right corner of mouth detected with edge detection
     */
    private int 					mouthOriginalRightY; 
    /**
     * distortion parameter set by the user or the slider
     */
    private float 					distortionFactor;
    /**
     * user ID for storage
     */
    private int 					userId; 
    /**
     * expression ID for storage
     */
    private int						expressionId; 
    /**
     * face image in bitmap format for storage
     */
    private Bitmap					faceImageBitmap;
    
    /**
	 * @param aUserId,aExpressionId
	 */
    public Expression(int aUserId,int aExpressionId){    	
    	//faceImageRgbMat = new Mat();
    	userId = aUserId;
    	expressionId = aExpressionId;
    	//faceImageGrayMat = new Mat();
    }
    /**
     * setUserId
     */
    public void setUserId(int aUserId){    	
    	userId = aUserId;
    }
    /**
     * @return current userId
     */
    public int getUserId(){
       	return userId;
    }
    /**
     * setExpressionId
     */
    public void setExpressionId(int aExpressionId){    	
    	expressionId = aExpressionId;
    }
    /**
     * @return expression Id
     */
    public int getExpressionId(){    	
    	return expressionId;
    }
    /**
     * set face image, this face image will be overwritten when face distortion is implemented.
     */
    public void setFaceImage(Mat aFaceMat){    	
    	//faceImageRgbMat = aFaceMat.clone();//whether here it should be aFaceMat.clone()???????
    	aFaceMat.copyTo(faceImageRgbMat);
        Imgproc.cvtColor(aFaceMat, faceImageGrayMat, Imgproc.COLOR_RGBA2GRAY);
    }
    /**
     * a header to the face submatrix is the input. 
     * This method is used for face distortion.
     */
    public void setFaceImageFromSubmat(Mat aFaceMat){
    	
    	faceImageRgbForDistortion = aFaceMat;
    }
    /**
     * @return current distorted face image
     */
    public Mat getFaceImage(){
    	
    	return faceImageRgbMat;
    }
    /**
     * set distortion parameter, larger parameter for bigger distortion, smaller parameter for smaller distortion
     * The distortion parameter must be in the interval [0,1];
     */
    public void setDistortionParameter(float aDistortionPara){
    	
    	distortionFactor = aDistortionPara/10.0f;
    }
    /**
     * @return current distortion parameter
     */
    public float getDistortionParameter(){
    	
    	return distortionFactor;
    }
    /**
     * Apply distortion to the face which the "faceImageRgbForDistortion" is pointed to.
     * mouth corners are detected through Sobel edge detection. 
     * Eye corners are predefined since it is difficult to detect the corner accurately.
     * The distorted face is cloned and stored in "faceImageRgbMat".
     */
    public void applyDistortion(){
    	
    	mouthCornerDetection();
    	Mat faceRef = faceImageRgbForDistortion.clone();
    	mouthDistortionSobel(faceRef);
    	eyeDistortion(faceRef);
    	faceImageRgbMat = faceImageRgbForDistortion.clone(); 
    }
    /**
     * Sobel edge detection for mouth corner detection.
     * It is applied to part of the face for speed and accuracy consideration.
     * @return a binary int array in which 1 stands for edge. 
     */
    private int[][] sobelEdgeDetection(){
    	
    	Mat faceSubmatrixGray = new Mat();
    	Imgproc.cvtColor(faceImageRgbForDistortion, faceSubmatrixGray, Imgproc.COLOR_RGBA2GRAY);//convert to gray color image which is easy to use
    	double gradx = 0,grady=0,gradtotal =0; 
    	int[][] edgeMat = new int[faceImageRgbForDistortion.rows()][faceImageRgbForDistortion.cols()];
    	for(int y = (int)(0.65*faceImageRgbForDistortion.rows());y<(int)(0.8*faceImageRgbForDistortion.rows());y++){
    		for(int x = (int)(0.3*faceImageRgbForDistortion.cols());x<(int)(0.7*faceImageRgbForDistortion.cols());x++){
    			gradx = faceSubmatrixGray.get(y-1, x+1)[0]+2*faceSubmatrixGray.get(y, x+1)[0]+faceSubmatrixGray.get(y+1,x+1)[0]-faceSubmatrixGray.get(y-1, x-1)[0]-2*faceSubmatrixGray.get(y, x-1)[0]-faceSubmatrixGray.get(y+1,x-1)[0];
    			grady = faceSubmatrixGray.get(y+1, x-1)[0]+2*faceSubmatrixGray.get(y+1, x)[0]+faceSubmatrixGray.get(y+1,x+1)[0]-faceSubmatrixGray.get(y-1, x-1)[0]-2*faceSubmatrixGray.get(y-1, x)[0]-faceSubmatrixGray.get(y-1,x+1)[0];
    			if(gradx<0) gradx = -gradx;
    			if(grady<0) grady = -grady;
    			gradtotal = gradx+grady;
    			edgeMat[y][x] =(gradtotal>50)?1:0;
    		}
    	}    	
    	return edgeMat;
    }
    /**
     * mouth corner detection.
     * The search is in an area where the mouth corner locates with high probability. 
     */
    private void mouthCornerDetection(){
    	
    	//Mat edgeMat = mRgba.clone();
    	int[][] edgeMat = sobelEdgeDetection();    	
    	//Mouth corner point detection
    	mouthOriginalLeftX = (int)(0.5*faceImageRgbForDistortion.cols());
        mouthOriginalLeftY = (int)(0.5*faceImageRgbForDistortion.rows());
        mouthOriginalRightX = (int)(0.5*faceImageRgbForDistortion.cols());
        mouthOriginalRightY = (int)(0.5*faceImageRgbForDistortion.rows());
        //left mouth corner detection
    	for(int y = (int)(0.65*faceImageRgbForDistortion.rows());y<(int)(0.8*faceImageRgbForDistortion.rows());y++){   	
    		for(int x = (int)(0.3*faceImageRgbForDistortion.cols());x<(int)(0.5*faceImageRgbForDistortion.cols());x++){
    			if(edgeMat[y][x]==1){
    				if(x<mouthOriginalLeftX){
    					mouthOriginalLeftX = x;
    					mouthOriginalLeftY = y;    							
    				}
    			}
    		}
    	}
    	//right mouth corner
    	for(int y = (int)(0.65*faceImageRgbForDistortion.rows());y<(int)(0.8*faceImageRgbForDistortion.rows());y++){   	
    		for(int x = (int)(0.5*faceImageRgbForDistortion.cols());x<(int)(0.7*faceImageRgbForDistortion.cols());x++){
    			if(edgeMat[y][x]==1){
    				if(x>mouthOriginalRightX){
    					mouthOriginalRightX = x;
    					mouthOriginalRightY = y;    							
    				}
    			}
    		}
    	}
    }
    /**
     * mouth distortion with mouth corner detected by sobel edge detection.
     * The destination of mouth corner is computed first according to mouth corner and distortion parameter.
     * Then the mouth area and area around mouth in the distorted face are filled regarding to its distance to the destination of mouth corner. 
     * The area around destination of mouth receives largest distortion while area further receives smaller distortion. 
     * The attenuation is according to Gaussian distribution.
     * BTW, it is assumed that the mouth is symmetricly located in the detected face. 
     */
    private void mouthDistortionSobel(Mat refFace){
    	
    	Log.v(TAG, "refFace"+refFace.channels()+" "+refFace.depth()+" "+refFace.elemSize()+" "+refFace.type());
    	//Destination of mouth corner computed
    	int mouthDestLeftX = mouthOriginalLeftX-(int)(faceImageRgbForDistortion.cols()*distortionFactor*0.5);
        int mouthDestLeftY = mouthOriginalLeftY-(int)(faceImageRgbForDistortion.rows()*distortionFactor);
        int mouthDestRightX = mouthOriginalRightX+(int)(faceImageRgbForDistortion.cols()*distortionFactor*0.5);
        int mouthDestRightY = mouthOriginalRightY-(int)(faceImageRgbForDistortion.rows()*distortionFactor);
    	int mouthMiddleX = (int)(0.5*faceImageRgbForDistortion.cols()); //middle of mouth
        int absDiffX = mouthOriginalLeftX-mouthDestLeftX; //distortion of mouth corner
        int absDiffY = mouthOriginalLeftY-mouthDestLeftY; //distortion of mouth corner
        int oriX,oriY; //the pixels in the distorted face will be filled with color obtained from (oriY,oriX) in original image
        double distortionCoefficient; //distortion coefficient indicates the attenuation according to a Gaussian distribution.  
        for(int x=0;x<faceImageRgbForDistortion.cols();x++){
        	for(int y=(int)(faceImageRgbForDistortion.rows()*0.5);y<faceImageRgbForDistortion.rows();y++){
        		if(x<mouthMiddleX){ //left side face
        			distortionCoefficient = computeDistCoefMouth(x-mouthDestLeftX,y-mouthDestLeftY);
        			oriX = x+(int)(absDiffX*distortionCoefficient);
        			oriY = y+(int)(absDiffY*distortionCoefficient);
        			if(oriX!=x||oriY!=y){//if no distortion for this pixel, its color is not needed to change. put and get is time consuming.
        			//Log.v(TAG, ""+mouthOriginalLeftX+" "+mouthOriginalLeftY+" "+mouthOriginalRightX+" "+mouthDestLeftX+" "+mouthDestRightX+" "+mouthDestRightY+" "+x+" "+y+" "+(x-oriX)+" "+(y-oriY));
        				faceImageRgbForDistortion.put(y, x, refFace.get(oriY, oriX));
        			}
        		}
        		else{
        			distortionCoefficient = computeDistCoefMouth(x-mouthDestRightX,y-mouthDestRightY);
        			oriX = x-(int)(absDiffX*distortionCoefficient);
        			oriY = y+(int)(absDiffY*distortionCoefficient);
        			if(oriX!=x||oriY!=y){
        				faceImageRgbForDistortion.put(y, x, refFace.get(oriY, oriX));
        			}
        			//Log.v(TAG, ""+mouthOriginalLeftX+" "+mouthOriginalLeftY+" "+mouthOriginalRightX+" "+mouthDestLeftX+" "+mouthDestRightX+" "+mouthDestRightY+" "+x+" "+y+" "+(x-oriX)+" "+(y-oriY));
        		}
        	}
        }
    }
    /**
     * mouth distortion with mouth corner predefined.
     * Because accuracy of sobel edge detection is sensitive to the sobel parameter, light, face characters etc.
     * very similar to mouthDistortionSobel
     */
    private void mouthDistortion(Mat refFace){
    	
    	Log.v(TAG, "refFace"+refFace.channels()+" "+refFace.depth()+" "+refFace.elemSize()+" "+refFace.type());
    	mouthOriginalLeftX = (int)(0.31*faceImageRgbForDistortion.width());
        mouthOriginalLeftY = (int)(0.75*faceImageRgbForDistortion.height());
        mouthOriginalRightX = (int)(0.69*faceImageRgbForDistortion.width());
        mouthOriginalRightY = mouthOriginalLeftY;
        //These destination of mouth are set according to the largest distortion
    	int mouthDestLeftX = mouthOriginalLeftX-(int)(faceImageRgbForDistortion.cols()*distortionFactor*0.5);
        int mouthDestLeftY = mouthOriginalLeftY-(int)(faceImageRgbForDistortion.rows()*distortionFactor);
        int mouthDestRightX = mouthOriginalRightX+(int)(faceImageRgbForDistortion.cols()*distortionFactor*0.5);
        int mouthDestRightY = mouthOriginalRightY-(int)(faceImageRgbForDistortion.rows()*distortionFactor);
    	int mouthMiddleX = (int)(0.5*faceImageRgbForDistortion.cols());
        int absDiffX = mouthOriginalLeftX-mouthDestLeftX;
        int absDiffY = mouthOriginalLeftY-mouthDestLeftY;
        int oriX,oriY;
        double distortionCoefficient;
        for(int x=0;x<faceImageRgbForDistortion.cols();x++){
        	for(int y=(int)(faceImageRgbForDistortion.rows()*0.5);y<faceImageRgbForDistortion.rows();y++){
        		if(x<mouthMiddleX){ //left side face
        			distortionCoefficient = computeDistCoefMouth(x-mouthDestLeftX,y-mouthDestLeftY);
        			oriX = x+(int)(absDiffX*distortionCoefficient);
        			oriY = y+(int)(absDiffY*distortionCoefficient);
        			if(oriX!=x||oriY!=y){
        			//Log.v(TAG, ""+mouthOriginalLeftX+" "+mouthOriginalLeftY+" "+mouthOriginalRightX+" "+mouthDestLeftX+" "+mouthDestRightX+" "+mouthDestRightY+" "+x+" "+y+" "+(x-oriX)+" "+(y-oriY));
        				faceImageRgbForDistortion.put(y, x, refFace.get(oriY, oriX));
        			}
        		}
        		else{
        			distortionCoefficient = computeDistCoefMouth(x-mouthDestRightX,y-mouthDestRightY);
        			oriX = x-(int)(absDiffX*distortionCoefficient);
        			oriY = y+(int)(absDiffY*distortionCoefficient);
        			if(oriX!=x||oriY!=y){
        				faceImageRgbForDistortion.put(y, x, refFace.get(oriY, oriX));
        			}
        			//Log.v(TAG, ""+mouthOriginalLeftX+" "+mouthOriginalLeftY+" "+mouthOriginalRightX+" "+mouthDestLeftX+" "+mouthDestRightX+" "+mouthDestRightY+" "+x+" "+y+" "+(x-oriX)+" "+(y-oriY));
        		}
        	}
        }
    }
    /** 
     * Gaussian distortion used for attenuation of mouth distortion 
     */
    private double computeDistCoefMouth(int dx,int dy){
    	
    	double sigma = faceImageRgbForDistortion.height()*0.1;//20.0;
    	double r = Math.pow(dx, 2.0)+Math.pow(dy, 2.0);
    	return Math.exp(-1.0*r/2.0/Math.pow(sigma, 2.0));
    }
    /**
     * Eye distortion, the left corner of left eye and right corner of right eye are predefined.
     * Due to complex environment around eye, it is difficult to detect the eye accurately. 
     */
    private void eyeDistortion(Mat refFace){
    	
    	Log.v(TAG, "refFace"+refFace.channels()+" "+refFace.depth()+" "+refFace.elemSize()+" "+refFace.type());
    	int oriX,oriY;
    	int leftEyeOriginalX = (int)(0.21*faceImageRgbForDistortion.cols());
        int leftEyeOriginalY = (int)(0.4*faceImageRgbForDistortion.rows());
        int leftEyeDestX = leftEyeOriginalX-(int)(faceImageRgbForDistortion.cols()*distortionFactor*0.25);
        int leftEyeDestY = leftEyeOriginalY-(int)(faceImageRgbForDistortion.rows()*distortionFactor*0.5);
        int rightEyeOriginalX = (int)(0.79*faceImageRgbForDistortion.cols());
        //int rightEyeOriginalY = leftEyeOriginalY;
        int rightEyeDestX = rightEyeOriginalX+(int)(faceImageRgbForDistortion.cols()*distortionFactor*0.25);
        int rightEyeDestY = leftEyeDestY;
        //distortion on left eye
        int absLeftEyeDiffX = leftEyeOriginalX-leftEyeDestX;
        int absLeftEyeDiffY = leftEyeOriginalY-leftEyeDestY;
        double distCoeffLeftEye;
        for(int x=0;x<(int)(faceImageRgbForDistortion.cols()*0.5);x++){
        	for(int y=(int)(faceImageRgbForDistortion.rows()*0.25);y<(int)(faceImageRgbForDistortion.rows()*0.5);y++){
        		distCoeffLeftEye = computeDistCoefEye(x-leftEyeDestX,y-leftEyeDestY);
        		oriX = x+(int)(absLeftEyeDiffX*distCoeffLeftEye);
        		oriY = y+(int)(absLeftEyeDiffY*distCoeffLeftEye);
        		faceImageRgbForDistortion.put(y, x, refFace.get(oriY, oriX));
        	}
        }
        //distortion on right eye
        int absRightEyeDiffX = rightEyeDestX-rightEyeOriginalX;
        int absRightEyeDiffY = absLeftEyeDiffY;
        double distCoeffRightEye;
        for(int x=(int)(faceImageRgbForDistortion.cols()*0.5);x<(int)(faceImageRgbForDistortion.cols());x++){
        	for(int y=(int)(faceImageRgbForDistortion.rows()*0.25);y<(int)(faceImageRgbForDistortion.rows()*0.5);y++){
        		distCoeffRightEye = computeDistCoefEye(x-rightEyeDestX,y-rightEyeDestY);
        		oriX = x-(int)(absRightEyeDiffX*distCoeffRightEye);
        		oriY = y+(int)(absRightEyeDiffY*distCoeffRightEye);
        		faceImageRgbForDistortion.put(y, x, refFace.get(oriY, oriX));
        	}
        }
    }
    /** 
     * Gaussian distortion used for attenuation of eye distortion 
     */
    private double computeDistCoefEye(int dx,int dy){
    	
    	double sigma = 5.0;
    	double r = Math.pow(dx, 2.0)+Math.pow(dy, 2.0);
    	return Math.exp(-1.0*r/2.0/Math.pow(sigma, 2.0));
    }
    /**
     * @return face image in bitmap format
     */
    public Bitmap getFaceImageBitmap() {
    	
    	mat2Bitmap();
		return faceImageBitmap;
	}
    /**
	 * set face image in Bitmap
	 */
	public void setFaceImageBitmap(Bitmap faceImagebitmap) {
		
		this.faceImageBitmap = faceImagebitmap;
	}
	/**
	 * convert mat to bitmap
	 */
	public void mat2Bitmap(){
		
		if(faceImageRgbMat==null)
			return;
		faceImageBitmap = Bitmap.createBitmap(faceImageRgbMat.cols(), faceImageRgbMat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(faceImageRgbMat, faceImageBitmap);	
	}
}
