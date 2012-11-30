package com.group12.oose.fakeface;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import android.util.Log;

public class Expression {
	private static final String    TAG                 = "OOSE::Expression";
	private Mat                     faceImageRgbMat;
	private Mat						faceImageRgbForDistortion;
    private Mat                     faceImageGrayMat;
    private int 					mouthOriginalLeftX;
    private int 					mouthOriginalLeftY;
    private int 					mouthOriginalRightX;
    private int 					mouthOriginalRightY;
    private float 					distortionFactor;
    private int 					userId;
    private int						expressionId;
    
    public Expression(int aUserId,int aExpressionId){
    	//faceImageRgbMat = new Mat();
    	userId = aUserId;
    	expressionId = aExpressionId;
    	//faceImageGrayMat = new Mat();
    }
    public void setUserId(int aUserId){
    	userId = aUserId;
    }
    public int getUserId(){
    	return userId;
    }
    public void setExpressionId(int aExpressionId){
    	expressionId = aExpressionId;
    }
    public int getExpressionId(){
    	return expressionId;
    }
    public void setFaceImage(Mat aFaceMat){
    	//faceImageRgbMat = aFaceMat.clone();//whether here it should be aFaceMat.clone()???????
    	aFaceMat.copyTo(faceImageRgbMat);
        Imgproc.cvtColor(aFaceMat, faceImageGrayMat, Imgproc.COLOR_RGBA2GRAY);
    }
    public void setFaceImageFromSubmat(Mat aFaceMat){
    	faceImageRgbForDistortion = aFaceMat;
    }
    public Mat getFaceImage(){
    	return faceImageRgbMat;
    }
    public void setDistortionParameter(float aDistortionPara){
    	distortionFactor = aDistortionPara;
    }
    public float getDistortionParameter(){
    	return distortionFactor;
    }
    public void applyDistortion(){
    	mouthCornerDetection();
    	Mat faceRef = faceImageRgbForDistortion.clone();
    	mouthDistortionSobel(faceRef);
    	eyeDistortion(faceRef);
    	faceImageRgbMat = faceImageRgbForDistortion.clone(); 
    }
    private int[][] sobelEdgeDetection(){
    	Mat faceSubmatrixGray = new Mat();
    	Imgproc.cvtColor(faceImageRgbForDistortion, faceSubmatrixGray, Imgproc.COLOR_RGBA2GRAY);
    	double gradx = 0,grady=0,gradtotal =0;
    	int[][] edgeMat = new int[faceImageRgbForDistortion.rows()][faceImageRgbForDistortion.cols()];
    	/*for(int x =0;x<faceImageRgbForDistortion.cols();x++){
    		edgeMat[0][x]=0;
    		edgeMat[faceImageRgbForDistortion.rows()-1][x] = 0;
    	}
    	for(int y =1;y<faceImageRgbForDistortion.rows()-1;y++){
    		edgeMat[y][0]=0;
    		edgeMat[y][faceImageRgbForDistortion.rows()-1] = 0;
    	}*/
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
    private void mouthDistortionSobel(Mat refFace){
    	Log.v(TAG, "refFace"+refFace.channels()+" "+refFace.depth()+" "+refFace.elemSize()+" "+refFace.type());
    	int mouthDestLeftX = mouthOriginalLeftX-(int)(faceImageRgbForDistortion.cols()*distortionFactor*0.5);
        int mouthDestLeftY = mouthOriginalLeftY-(int)(faceImageRgbForDistortion.rows()*distortionFactor);
        int mouthDestRightX = mouthOriginalRightX+(int)(faceImageRgbForDistortion.cols()*distortionFactor*0.5);
        int mouthDestRightY = mouthOriginalRightY-(int)(faceImageRgbForDistortion.rows()*distortionFactor);;
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
    private void mouthDistortion(Mat refFace){
    	Log.v(TAG, "refFace"+refFace.channels()+" "+refFace.depth()+" "+refFace.elemSize()+" "+refFace.type());
    	mouthOriginalLeftX = (int)(0.31*faceImageRgbForDistortion.width());
        mouthOriginalLeftY = (int)(0.75*faceImageRgbForDistortion.height());
        mouthOriginalRightX = (int)(0.69*faceImageRgbForDistortion.width());
        mouthOriginalRightY = mouthOriginalLeftY;
    	int mouthDestLeftX = mouthOriginalLeftX-(int)(faceImageRgbForDistortion.cols()*distortionFactor*0.5);
        int mouthDestLeftY = mouthOriginalLeftY-(int)(faceImageRgbForDistortion.rows()*distortionFactor);
        int mouthDestRightX = mouthOriginalRightX+(int)(faceImageRgbForDistortion.cols()*distortionFactor*0.5);
        int mouthDestRightY = mouthOriginalRightY-(int)(faceImageRgbForDistortion.rows()*distortionFactor);;
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
    private double computeDistCoefMouth(int dx,int dy){
    	double sigma = faceImageRgbForDistortion.height()*0.1;//20.0;
    	double r = Math.pow(dx, 2.0)+Math.pow(dy, 2.0);
    	return Math.exp(-1.0*r/2.0/Math.pow(sigma, 2.0));
    }
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
    private double computeDistCoefEye(int dx,int dy){
    	double sigma = 5.0;
    	double r = Math.pow(dx, 2.0)+Math.pow(dy, 2.0);
    	return Math.exp(-1.0*r/2.0/Math.pow(sigma, 2.0));
    }
}
