package com.group12.oose.fakeface;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
     * left corner of mouth detected with edge detection  
     */
    private MouthCorner 			mouthLeftCorner; 					
    /**
     * right corner of mouth detected with edge detection
     */
    private MouthCorner 			mouthRightCorner;
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
     * set by the snap shot. If true, the face image will be stored in "faceImageRgbMat"
     */
    private boolean 				storeThisExpression;
    /**
     * an flag tells whether the distorted image is ready.
     */
    private boolean                 expressionReadyForStore;
    // some parameters used to locate the mouse area for edge detection and mouse distortion
    private static final double ratioMouseTopforEdgeDetection = 0.65;
    private static final double ratioMouseBottomforEdgeDetection = 0.8;
    private static final double ratioMouseLeftforEdgeDection = 0.3;
    private static final double ratioMouseRightforEdgeDetection = 0.7;
    private static final double ratioMouseMiddlex = 0.5;
    private static final double ratioMouseMiddley = 0.5;
    private static final double sobelGradPara = 50;   
    // some parameter for eye distortion
    private static final double ratioOriginalLeftEyeX = 0.21;
    private static final double ratioOriginalLeftEyeY = 0.4;
    private static final double ratioOnDistortionFactorx = 0.25;
    private static final double ratioOnDistortionFactory = 0.5;
    private static final double ratioOriginalRightEyeX = 0.79;
    private static final double ratioDestEyeAreaY = 0.25;
    private static final double ratioDestEyeMiddleY = 0.5;
    private static final double ratioDestEyeMiddleX = 0.5;   
    /*
     * mouse distortion parameter
     */
    private double mouseDistortionPara = 0.1;
    /*
     * eye distortion parameter
     */
    private double eyeDistortionPara = 5.0;
    /**
	 * @param aUserId,aExpressionId
	 */
    public Expression(int aUserId,int aExpressionId){
    	userId = aUserId;
    	expressionId = aExpressionId;
    	mouthLeftCorner = new MouthCorner();
    	mouthRightCorner = new MouthCorner();
    	storeThisExpression = false;
    	expressionReadyForStore = false;
    }
    /**
     * 
     * @param storeFlag
     */
    public void setStoreFlag(boolean storeFlag){
    	storeThisExpression=storeFlag;
    }
    public boolean checkWhetherFaceImageIsReady(){
    	return expressionReadyForStore;
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
        Imgproc.cvtColor(aFaceMat, faceImageGrayMat, Imgproc.COLOR_RGBA2GRAY); // this is for face detection.
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
    	mouthDistortionSobel();
    	eyeDistortion();
    	if(storeThisExpression){
    		faceImageRgbMat = faceImageRgbForDistortion.clone();
    		expressionReadyForStore = true;
    	}
    	else{
    		expressionReadyForStore = false;
    	}
    }
    /**
     * Sobel edge detection for mouth corner detection.
     * It is applied to part of the face for speed and accuracy consideration.
     * @return a binary int array in which 1 stands for edge. 
     */
    private int[][] sobelEdgeDetection(){
    	
    	Mat faceSubmatrixGray = new Mat();
    	Imgproc.cvtColor(faceImageRgbForDistortion, faceSubmatrixGray, Imgproc.COLOR_RGBA2GRAY);//convert to gray color image which is easy to use
    	byte buff[] = new byte[(int) (faceSubmatrixGray.total()*faceSubmatrixGray.channels())];
    	faceSubmatrixGray.get(0,0,buff);
    	double gradx = 0,grady=0,gradtotal =0; 
    	int[][] edgeMat = new int[faceImageRgbForDistortion.rows()][faceImageRgbForDistortion.cols()];
    	for(int y = (int)(ratioMouseTopforEdgeDetection*faceImageRgbForDistortion.rows());y<(int)(ratioMouseBottomforEdgeDetection*faceImageRgbForDistortion.rows());y++){
    		for(int x = (int)(ratioMouseLeftforEdgeDection*faceImageRgbForDistortion.cols());x<(int)(ratioMouseRightforEdgeDetection*faceImageRgbForDistortion.cols());x++){
    			gradx = faceSubmatrixGray.get(y-1, x+1)[0]+2*faceSubmatrixGray.get(y, x+1)[0]+faceSubmatrixGray.get(y+1,x+1)[0]-faceSubmatrixGray.get(y-1, x-1)[0]-2*faceSubmatrixGray.get(y, x-1)[0]-faceSubmatrixGray.get(y+1,x-1)[0];
    			grady = faceSubmatrixGray.get(y+1, x-1)[0]+2*faceSubmatrixGray.get(y+1, x)[0]+faceSubmatrixGray.get(y+1,x+1)[0]-faceSubmatrixGray.get(y-1, x-1)[0]-2*faceSubmatrixGray.get(y-1, x)[0]-faceSubmatrixGray.get(y-1,x+1)[0];
    			if(gradx<0) gradx = -gradx;
    			if(grady<0) grady = -grady;
    			gradtotal = gradx+grady;
    			edgeMat[y][x] =(gradtotal>sobelGradPara)?1:0;
    		}
    	}    	
    	return edgeMat;
    }
    private int[][] sobelEdgeDetectionBuffer(){    	
    	Mat faceSubmatrixGray = new Mat();
    	Imgproc.cvtColor(faceImageRgbForDistortion, faceSubmatrixGray, Imgproc.COLOR_RGBA2GRAY);//convert to gray color image which is easy to use
    	byte buff[] = new byte[(int) (faceSubmatrixGray.total()*faceSubmatrixGray.channels())];
    	faceSubmatrixGray.get(0,0,buff);
    	double gradx = 0,grady=0,gradtotal =0; 
    	int[][] edgeMat = new int[faceImageRgbForDistortion.rows()][faceImageRgbForDistortion.cols()];
    	for(int y = (int)(ratioMouseTopforEdgeDetection*faceImageRgbForDistortion.rows());y<(int)(ratioMouseBottomforEdgeDetection*faceImageRgbForDistortion.rows());y++){
    		for(int x = (int)(ratioMouseLeftforEdgeDection*faceImageRgbForDistortion.cols());x<(int)(ratioMouseRightforEdgeDetection*faceImageRgbForDistortion.cols());x++){
    			//gradx = faceSubmatrixGray.get(y-1, x+1)[0]+2*faceSubmatrixGray.get(y, x+1)[0]+faceSubmatrixGray.get(y+1,x+1)[0]-faceSubmatrixGray.get(y-1, x-1)[0]-2*faceSubmatrixGray.get(y, x-1)[0]-faceSubmatrixGray.get(y+1,x-1)[0];
    			gradx = buff[(y-1)*faceImageRgbForDistortion.cols()+x+1]+2*buff[y*faceImageRgbForDistortion.cols()+x+1]+buff[(y+1)*faceImageRgbForDistortion.cols()+x+1]-buff[(y-1)*faceImageRgbForDistortion.cols()+x-1]-2*buff[(y)*faceImageRgbForDistortion.cols()+x-1]-buff[(y+1)*faceImageRgbForDistortion.cols()+x-1];
    			//grady = faceSubmatrixGray.get(y+1, x-1)[0]+2*faceSubmatrixGray.get(y+1, x)[0]+faceSubmatrixGray.get(y+1,x+1)[0]-faceSubmatrixGray.get(y-1, x-1)[0]-2*faceSubmatrixGray.get(y-1, x)[0]-faceSubmatrixGray.get(y-1,x+1)[0];
    			gradx = buff[(y+1)*faceImageRgbForDistortion.cols()+x-1]+2*buff[(y+1)*faceImageRgbForDistortion.cols()+x]+buff[(y+1)*faceImageRgbForDistortion.cols()+x+1]-buff[(y-1)*faceImageRgbForDistortion.cols()+x-1]-2*buff[(y-1)*faceImageRgbForDistortion.cols()+x]-buff[(y-1)*faceImageRgbForDistortion.cols()+x+1];
    			if(gradx<0) gradx = -gradx;
    			if(grady<0) grady = -grady;
    			gradtotal = gradx+grady;
    			edgeMat[y][x] =(gradtotal>sobelGradPara)?1:0;
    		}
    	}    	
    	return edgeMat;
    }
    /**
     * mouth corner detection.
     * The search is in an area where the mouth corner locates with high probability. 
     */
    private void mouthCornerDetection(){
    	
    	int[][] edgeMat = sobelEdgeDetectionBuffer();//sobelEdgeDetection();    	
    	//Mouth corner point detection
    	mouthLeftCorner.setMouthCornerX((int)(ratioMouseMiddlex*faceImageRgbForDistortion.cols()));
    	mouthLeftCorner.setMouthCornerY((int)(ratioMouseMiddlex*faceImageRgbForDistortion.rows()));
    	mouthRightCorner.setMouthCornerX((int)(ratioMouseMiddlex*faceImageRgbForDistortion.cols()));
    	mouthRightCorner.setMouthCornerY((int)(ratioMouseMiddlex*faceImageRgbForDistortion.rows()));
        //left mouth corner detection
    	boolean cornerFound = false;
    	for(int x = (int)(ratioMouseLeftforEdgeDection*faceImageRgbForDistortion.cols());x<(int)(ratioMouseMiddlex*faceImageRgbForDistortion.cols());x++){
    		for(int y = (int)(ratioMouseTopforEdgeDetection*faceImageRgbForDistortion.rows());y<(int)(ratioMouseBottomforEdgeDetection*faceImageRgbForDistortion.rows());y++){   	
    			if(edgeMat[y][x]==1){
    				if(x<mouthLeftCorner.getMouthCornerX()){
    					mouthLeftCorner.setMouthCornerX(x);
    					mouthLeftCorner.setMouthCornerY(y);
    					cornerFound = true;
    					break;
    				}
    			}
    		}
    		if(cornerFound) break;
    	}
    	//right mouth corner
    	cornerFound = false;
    	for(int x = (int)(ratioMouseRightforEdgeDetection*faceImageRgbForDistortion.cols());x>(int)(ratioMouseMiddlex*faceImageRgbForDistortion.cols());x--){
    		for(int y = (int)(ratioMouseTopforEdgeDetection*faceImageRgbForDistortion.rows());y<(int)(ratioMouseBottomforEdgeDetection*faceImageRgbForDistortion.rows());y++){   	
    			if(edgeMat[y][x]==1){
    				if(x>mouthRightCorner.getMouthCornerX()){
    					mouthRightCorner.setMouthCornerX(x);
    					mouthRightCorner.setMouthCornerY(y);
    					cornerFound = true;
    					break;
    				}
    			}
    		}
    		if(cornerFound) break;
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
    private void mouthDistortionSobel(){
    	mouthCornerDetection();    	
    	//Destination of mouth corner computed
    	MouthCorner mouthDestLeftCorner = new MouthCorner(mouthLeftCorner.getMouthCornerX()-(int)(faceImageRgbForDistortion.cols()*distortionFactor*0.5),mouthLeftCorner.getMouthCornerY()-(int)(faceImageRgbForDistortion.rows()*distortionFactor));
    	MouthCorner mouthDestRightCorner = new MouthCorner(mouthRightCorner.getMouthCornerX()+(int)(faceImageRgbForDistortion.cols()*distortionFactor*0.5),mouthRightCorner.getMouthCornerY()-(int)(faceImageRgbForDistortion.rows()*distortionFactor));
    	int absDiffX = mouthLeftCorner.getMouthCornerX()-mouthDestLeftCorner.getMouthCornerX(); //distortion of mouth corner
        int absDiffY = mouthLeftCorner.getMouthCornerY()-mouthDestLeftCorner.getMouthCornerY(); //distortion of mouth corner
        int oriX,oriY; //the pixels in the distorted face will be filled with color obtained from (oriY,oriX) in original image
        double distortionCoefficient; //distortion coefficient indicates the attenuation according to a Gaussian distribution.  
        for(int y=(int)(faceImageRgbForDistortion.rows()*ratioMouseMiddley);y<faceImageRgbForDistortion.rows();y++){
        	for(int x=0;x<(int)(faceImageRgbForDistortion.cols()*ratioMouseMiddlex);x++){        	
        		//left side face
        		distortionCoefficient = computeDistCoefMouth(x-mouthDestLeftCorner.getMouthCornerX(),y-mouthDestLeftCorner.getMouthCornerY());
        		oriX = x+(int)(absDiffX*distortionCoefficient);
        		oriY = y+(int)(absDiffY*distortionCoefficient);
        		if(oriX!=x||oriY!=y){//if no distortion for this pixel, its color is not needed to change. put and get is time consuming.        			
        			faceImageRgbForDistortion.put(y, x, faceImageRgbForDistortion.get(oriY, oriX));
        		}
        	}
        }
        for(int y=(int)(faceImageRgbForDistortion.rows()*ratioMouseMiddley);y<faceImageRgbForDistortion.rows();y++){
        	for(int x=faceImageRgbForDistortion.cols();x>(int)(faceImageRgbForDistortion.cols()*ratioMouseMiddlex);x--){ 
        		distortionCoefficient = computeDistCoefMouth(x-mouthDestRightCorner.getMouthCornerX(),y-mouthDestRightCorner.getMouthCornerY());
        		oriX = x-(int)(absDiffX*distortionCoefficient);
        		oriY = y+(int)(absDiffY*distortionCoefficient);
        		if(oriX!=x||oriY!=y){
        			faceImageRgbForDistortion.put(y, x, faceImageRgbForDistortion.get(oriY, oriX));
        		}        			
        	}
        }
        }
    /**
     * mouth distortion with mouth corner predefined.
     * Because accuracy of sobel edge detection is sensitive to the sobel parameter, light, face characters etc.
     * very similar to mouthDistortionSobel
     */
    /*private void mouthDistortion(Mat refFace){
    	
    	Log.v(TAG, "refFace"+refFace.channels()+" "+refFace.depth()+" "+refFace.elemSize()+" "+refFace.type());
        mouthLeftCorner.setMouthCorner((int)(0.31*faceImageRgbForDistortion.width()), (int)(0.75*faceImageRgbForDistortion.height()));
        mouthRightCorner.setMouthCorner((int)(0.69*faceImageRgbForDistortion.width()), (int)(0.75*faceImageRgbForDistortion.height()));
        //These destination of mouth are set according to the largest distortion
        MouthCorner mouthDestLeftCorner = new MouthCorner(mouthLeftCorner.getMouthCornerX()-(int)(faceImageRgbForDistortion.cols()*distortionFactor*0.5),mouthLeftCorner.getMouthCornerY()-(int)(faceImageRgbForDistortion.rows()*distortionFactor));
    	MouthCorner mouthDestRightCorner = new MouthCorner(mouthRightCorner.getMouthCornerX()+(int)(faceImageRgbForDistortion.cols()*distortionFactor*0.5),mouthRightCorner.getMouthCornerY()-(int)(faceImageRgbForDistortion.rows()*distortionFactor));
    	int absDiffX = mouthLeftCorner.getMouthCornerX()-mouthDestLeftCorner.getMouthCornerX(); //distortion of mouth corner
        int absDiffY = mouthLeftCorner.getMouthCornerY()-mouthDestLeftCorner.getMouthCornerY(); //distortion of mouth corner
        int oriX,oriY;
        double distortionCoefficient;
        for(int y=(int)(faceImageRgbForDistortion.rows()*0.5);y<faceImageRgbForDistortion.rows();y++){
        	for(int x=0;x<(int)(faceImageRgbForDistortion.cols()*0.5);x++){        	
        		//left side face
        		distortionCoefficient = computeDistCoefMouth(x-mouthDestLeftCorner.getMouthCornerX(),y-mouthDestLeftCorner.getMouthCornerY());
        		oriX = x+(int)(absDiffX*distortionCoefficient);
        		oriY = y+(int)(absDiffY*distortionCoefficient);
        		if(oriX!=x||oriY!=y){//if no distortion for this pixel, its color is not needed to change. put and get is time consuming.
        			faceImageRgbForDistortion.put(y, x, faceImageRgbForDistortion.get(oriY, oriX));
        		}
        	}
        }
        for(int y=(int)(faceImageRgbForDistortion.rows()*0.5);y<faceImageRgbForDistortion.rows();y++){
        	for(int x=faceImageRgbForDistortion.cols();x<(int)(faceImageRgbForDistortion.cols()*0.5);x--){ 
        		distortionCoefficient = computeDistCoefMouth(x-mouthDestRightCorner.getMouthCornerX(),y-mouthDestRightCorner.getMouthCornerY());
        		oriX = x-(int)(absDiffX*distortionCoefficient);
        		oriY = y+(int)(absDiffY*distortionCoefficient);
        		if(oriX!=x||oriY!=y){
        			faceImageRgbForDistortion.put(y, x, faceImageRgbForDistortion.get(oriY, oriX));
        		}
        	}
        }
    }*/
    /** 
     * Gaussian distortion used for attenuation of mouth distortion 
     */
    private double computeDistCoefMouth(int dx,int dy){
    	double sigma = mouseDistortionPara*faceImageRgbForDistortion.height();//20.0;
    	double r = Math.pow(dx, 2.0)+Math.pow(dy, 2.0);
    	return Math.exp(-1.0*r/2.0/Math.pow(sigma, 2.0));
    }
    /**
     * Eye distortion, the left corner of left eye and right corner of right eye are predefined.
     * Due to complex environment around eye, it is difficult to detect the eye accurately. 
     */
    private void eyeDistortion(){
    	
    	//Log.v(TAG, "refFace"+refFace.channels()+" "+refFace.depth()+" "+refFace.elemSize()+" "+refFace.type());
    	int oriX,oriY;
    	int leftEyeOriginalX = (int)(ratioOriginalLeftEyeX*faceImageRgbForDistortion.cols());
        int leftEyeOriginalY = (int)(ratioOriginalLeftEyeY*faceImageRgbForDistortion.rows());
        int leftEyeDestX = leftEyeOriginalX-(int)(faceImageRgbForDistortion.cols()*distortionFactor*ratioOnDistortionFactorx);
        int leftEyeDestY = leftEyeOriginalY-(int)(faceImageRgbForDistortion.rows()*distortionFactor*ratioOnDistortionFactory);
        int rightEyeOriginalX = (int)(ratioOriginalRightEyeX*faceImageRgbForDistortion.cols());
        //int rightEyeOriginalY = leftEyeOriginalY;
        int rightEyeDestX = rightEyeOriginalX+(int)(faceImageRgbForDistortion.cols()*distortionFactor*ratioOnDistortionFactorx);
        int rightEyeDestY = leftEyeDestY;
        //distortion on left eye
        int absLeftEyeDiffX = leftEyeOriginalX-leftEyeDestX;
        int absLeftEyeDiffY = leftEyeOriginalY-leftEyeDestY;
        double distCoeffLeftEye;
        for(int y=(int)(faceImageRgbForDistortion.rows()*ratioDestEyeAreaY);y<(int)(faceImageRgbForDistortion.rows()*ratioDestEyeMiddleY);y++){
        	for(int x=0;x<(int)(faceImageRgbForDistortion.cols()*ratioDestEyeMiddleX);x++){        	
        		distCoeffLeftEye = computeDistCoefEye(x-leftEyeDestX,y-leftEyeDestY);
        		oriX = x+(int)(absLeftEyeDiffX*distCoeffLeftEye);
        		oriY = y+(int)(absLeftEyeDiffY*distCoeffLeftEye);
        		faceImageRgbForDistortion.put(y, x, faceImageRgbForDistortion.get(oriY, oriX));
        	}
        }
        //distortion on right eye
        int absRightEyeDiffX = rightEyeDestX-rightEyeOriginalX;
        int absRightEyeDiffY = absLeftEyeDiffY;
        double distCoeffRightEye;
        for(int y=(int)(faceImageRgbForDistortion.rows()*ratioDestEyeAreaY);y<(int)(faceImageRgbForDistortion.rows()*ratioDestEyeMiddleY);y++){
        	for(int x=(int)(faceImageRgbForDistortion.cols());x<(int)(faceImageRgbForDistortion.cols()*ratioDestEyeMiddleX);x--){        	
        		distCoeffRightEye = computeDistCoefEye(x-rightEyeDestX,y-rightEyeDestY);
        		oriX = x-(int)(absRightEyeDiffX*distCoeffRightEye);
        		oriY = y+(int)(absRightEyeDiffY*distCoeffRightEye);
        		faceImageRgbForDistortion.put(y, x, faceImageRgbForDistortion.get(oriY, oriX));
        	}
        }
    }
    /** 
     * Gaussian distortion used for attenuation of eye distortion 
     */
    private double computeDistCoefEye(int dx,int dy){
    	
    	double sigma = eyeDistortionPara;
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
	/**
	 * update this expression to database
	 * @param dbHelper to get a connection to database
	 */
	public void update2DB(MyDBHelper dbHelper)
	{
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		boolean ifFound = false;
        String[] columns = {"ExpressionID"};
        try{
			//openDataBase();
        	Cursor cur = db.query("Expression",columns,"UserID="+userId,null,null,null,null);
			cur.moveToFirst();
			for(int i=0;i<cur.getCount();i++){
				if(expressionId == cur.getInt(0))
					ifFound = true;
				cur.moveToNext();
			}
			cur.close();
			//db.close();
		}catch(Exception ex){
			Log.e("DatabaseHandler.getCategoryJoke",""+ex.getMessage()+ex.getLocalizedMessage()+ex.getCause());
		}
        if (!ifFound)
        {
        	Log.e("Database error:","not found in database");
        	add2DB(dbHelper);
        	return;
        }
        try{
        	String strFilter = "UserId=" + userId+" and ExpressionID="+expressionId;
        	ContentValues args = new ContentValues();
        	args.put("Factor", distortionFactor);
        	//byte[] outputPhoto = mat2ByteArray(expression.getFaceImage());
        	byte[] outputPhoto = DBUtil.bitmap2ByteArray(faceImageBitmap);
    		if (outputPhoto==null)
    			return;
    		args.put("Photo", outputPhoto);
    		args.put("Width", faceImageBitmap.getWidth());
    		args.put("Height", faceImageBitmap.getHeight());
    		args.put("Factor", Double.valueOf(distortionFactor));
    		db.update("Expression", args, strFilter, null);
			db.close();
		}catch(Exception ex){
			Log.e("DatabaseHandler.getCategoryJoke",""+ex.getMessage()+ex.getLocalizedMessage()+ex.getCause());
		}
	}
	
	/**
	 * to add this expression to database
	 * @param dbHelper to get connection to database
	 */
	public void add2DB(MyDBHelper dbHelper)
	{
		byte[] outputPhoto = DBUtil.bitmap2ByteArray(faceImageBitmap);
		if (outputPhoto==null)
			return;
		
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("UserID", this.userId);
		values.put("ExpressionID", expressionId);
		values.put("Photo", outputPhoto);
		values.put("Width", faceImageBitmap.getWidth());
		values.put("Height", faceImageBitmap.getHeight());
		values.put("Factor", Double.valueOf((double)distortionFactor));
		try{
        	long rowid = db.insert("Expression", null, values);
        }catch(Exception ex){
			Log.e("Exception: ",""+ex.getMessage());
		}
        db.close();
	}
}
