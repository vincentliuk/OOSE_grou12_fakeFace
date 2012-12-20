package com.group12.oose.fakeface;

import java.util.ArrayList;
import java.util.List;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.util.Log;

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

	/**
	 * load all expressions into expression list
	 * @param dbHelper the database helper to get a connection to database
	 */
	public void loadAllExpressions(MyDBHelper dbHelper)
	{
		expressions.clear();
		ArrayList<Expression> allExpressions = new ArrayList<Expression>();
		SQLiteDatabase db = dbHelper.getWritableDatabase();
        String[] columns = {"UserID","ExpressionID","Factor","Photo","Width","Height"};
        try{
			Cursor cur = db.query("Expression",columns,"UserID="+userID,null,null,null,null);
			cur.moveToFirst();
			for(int i=0;i<cur.getCount();i++){
				Expression tempExpression = new Expression(userID, cur.getInt(1));
				tempExpression.setDistortionParameter((float)cur.getDouble(2));
				byte[] imageRaw = cur.getBlob(3);
				int width = cur.getInt(4);
				int height = cur.getInt(5);
				Bitmap bitmap = DBUtil.byteArray2Bitmap(imageRaw, width, height);
				//Mat tempPhoto = byteArray2Mat(matRaw);
				if (bitmap==null)
					continue;
				tempExpression.setFaceImageBitmap(bitmap);
				allExpressions.add(tempExpression);
				cur.moveToNext();
			}
			cur.close();
			db.close();
		}catch(Exception ex){
			Log.e("DatabaseHandler.getCategoryJoke",""+ex.getMessage()+ex.getLocalizedMessage()+ex.getCause());
		}
        expressions = allExpressions;
	}
	
	/**
	 * Add a new expression to this user
	 * @param dbHelper to get a connection to this database
	 * @param expression the expression need to 
	 */
	public void addNewExpression(MyDBHelper dbHelper, Expression expression)
	{
		expression.add2DB(dbHelper);
        expressions.add(expression);
	}
	
	/**
	 * update an expression for this user
	 * @param dbHelper to get connection to database
	 * @param expression the expression to be updated
	 */
	public void updateExpression(MyDBHelper dbHelper, Expression expression)
	{
		expression.update2DB(dbHelper);
	}
	/**
	 * add this user back to database
	 * @param myDBHelper to get a connection to database
	 */
	public void add2DB(MyDBHelper myDBHelper)
	{
		mat2bitmap();
		if (getProfilePhoto()==null)
		{
			Log.e("running", "Mat is null!");
			return;
		}
		byte[] outputPhoto = DBUtil.bitmap2ByteArray(getBitmapPhoto());
		
		if (outputPhoto==null)
		{
			Log.e("running", "photo is null!");
			return;
		}
		SQLiteDatabase db = myDBHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("UserID", getUserID());
		values.put("Photo", outputPhoto);
		values.put("Width", getBitmapPhoto().getWidth());
		values.put("Height", getBitmapPhoto().getHeight());
		try{
        	db.insert("User", null, values);
        }catch(Exception ex){
			Log.e("Exception: ",""+ex.getMessage());
		}
        db.close();
	}
}
