package com.group12.oose.fakeface;

import java.util.ArrayList;
import java.util.List;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import android.graphics.Bitmap;

/**
 * This class depicts the user in this system.
 * information of a user includes a unique ID and a profile photo
 * @author JHU Fall2012 OOSE group 12
 *
 */
public class User {
	/**
	 * unique id for each user
	 */
	private int userID;
	/**
	 * the profile photo for face recognition and other functionals (in opencv.core.Mat format)
	 */
	private Mat profilePhoto;
	/**
	 * all expressions of this user
	 */
	private List<Expression> expressions;
	/**
	 * profile photo in Bitmap format
	 */
	private Bitmap bitmapPhoto;
	/**
	 * getter of profile photo
	 * @return profile photo in Bitmap format
	 */
	public Bitmap getBitmapPhoto() {
		return bitmapPhoto;
	}

	/**
	 * setter of profile photo
	 * @param bitmapPhoto profile photo in Bitmap format
	 */
	public void setBitmapPhoto(Bitmap bitmapPhoto) {
		this.bitmapPhoto = bitmapPhoto;
	}

	/**
	 * constructor
	 */
	public User() {
		userID = 0;
		profilePhoto = null;
		expressions = new ArrayList<Expression>();
		bitmapPhoto = null;
	}

	/**
	 * convert Mat to Bitmap
	 */
	public void mat2bitmap()
	{
		if(profilePhoto==null)
			return;
		bitmapPhoto = Bitmap.createBitmap(profilePhoto.cols(), profilePhoto.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(profilePhoto, bitmapPhoto);
	}
	
	/**
	 * getter of user id
	 * @return
	 */
	public int getUserID() {
		return userID;
	}

	/**
	 * getter of user id
	 * 
	 * @param userID
	 */
	public void setUserID(int userID) {
		this.userID = userID;
	}

	/**
	 * getter of profile photo in mat format
	 * @return
	 */
	public Mat getProfilePhoto() {
		return profilePhoto;
	}

	/**
	 * setter of profile photo in mat format
	 * @param profilePhoto
	 */
	public void setProfilePhoto(Mat profilePhoto) {
		this.profilePhoto = profilePhoto;
	}
	
	/**
	 * calculate dissimilarity of the profile photo of this user and the input photo
	 * this is for face recognition
	 * @param detectedFace
	 * @return
	 */
	public double distance(Mat detectedFace)
	{
		//Mat grayImage = detectedFace.
		return (double)userID;
	}
    
	/**
	 * getter of the expressions
	 * @return
	 */
	public List<Expression> getExpressions() {
		return expressions;
	}

	/**
	 * setter of the expressions
	 * @param expressions
	 */
	public void setExpressions(List<Expression> expressions) {
		
		this.expressions = expressions;
	}

}
