package com.group12.oose.fakeface;

/**
 * this class defines the mouth corner
 * @author JHU FAL2012 OOSE group 12
 *
 */

public class MouthCorner {
	/**
	 *  x coordinate of mouth Corner
	 */
	private int x;
	/**
	 *  y coordinate of mouth Corner
	 */
	private int y;
	public MouthCorner(){
		x = 0;
		y = 0;
	}
	public MouthCorner(int aX,int aY){
		x = aX;
		y = aY;
	}
	/**
	 * setMouthCorner
	 * @param aX
	 * @param aY
	 */
	public void setMouthCorner(int aX,int aY){
		x = aX;
		y = aY;
	}
	/**
	 * 
	 * @return x coordinate of mouth corner
	 */
	public int getMouthCornerX(){
		return x;
	}
	/**
	 * 
	 * @return x coordinate of mouth corner
	 */
	public int getMouthCornerY(){
		return y;
	}
	/**
	 * 
	 * @param aX
	 */
	public void setMouthCornerX(int aX){
		x = aX;
	}
	/**
	 * 
	 * @param aY
	 */
	public void setMouthCornerY(int aY){
		y = aY;
	}
	/**
	 * based on original mouth corner, face width and face height , compute the destination mouth corner
	 * @param distortionFactor
	 * @param faceWidth
	 * @param faceHeight
	 * @return destination mouth corner
	 */
	public MouthCorner computeDest(float distortionFactor,int faceWidth,int faceHeight){		
		MouthCorner destMouth = new MouthCorner();
		return destMouth;
	}	
	public String printString(){
		return " "+x+" "+y;
	}
}
