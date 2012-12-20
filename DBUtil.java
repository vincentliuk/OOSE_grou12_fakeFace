package com.group12.oose.fakeface;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.opencv.core.Mat;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.util.Log;

/**
 * This class includes all database operations   
 * @author JHU Fall2012 OOSE group 12
 *
 */
public class DBUtil {

	/**
	 * the DatabaseHelper class used for connect to database
	 */
	private MyDBHelper myDBHelper;
	private int maxUserID;
	
	/**
	 * constructor
	 */
	public DBUtil(){
		myDBHelper = null;
		maxUserID = -1;
	}

	/**
	 * load all users from database
	 * @return a list of user
	 */
	public ArrayList<User> loadAllUsers()
	{
		
		ArrayList<User> allUsers = new ArrayList<User>();
		SQLiteDatabase db = myDBHelper.getWritableDatabase();
        String[] columns = {"UserID","Photo","Width","Height"};
        
        try{
        	Cursor cur = db.query("User",columns,null,null,null,null,null);
			cur.moveToFirst();
			
			for(int i=0;i<cur.getCount();i++){
				User tempUser = new User();
				tempUser.setUserID(cur.getInt(0));
				byte[] imageRaw = cur.getBlob(1);
				int width = cur.getInt(2);
				int height = cur.getInt(3);
				Bitmap bitmap = byteArray2Bitmap(imageRaw,width,height);
				
				if (bitmap==null)
					continue;
				tempUser.setBitmapPhoto(bitmap);
				allUsers.add(tempUser);
				cur.moveToNext();
			}
			cur.close();
			db.close();
			}catch(Exception ex){
			
			Log.e("DatabaseHandler.getCategoryJoke","exception: "+ex.getMessage()+ex.getLocalizedMessage()+ex.getCause());
		}
        
        return allUsers;

	}
	
	/**
	 * load all expressions for the user with specified user id
	 * @param userID the user id indicates for which user need to load expressions 
	 * @return
	 */
	public ArrayList<Expression> loadAllExpressions(int userID)
	{
		ArrayList<Expression> allExpressions = new ArrayList<Expression>();
		SQLiteDatabase db = myDBHelper.getWritableDatabase();
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
				Bitmap bitmap = byteArray2Bitmap(imageRaw,width,height);
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
			Log.e("DatabaseHandler.getCategoryJoke","exception"+ex.getMessage()+ex.getLocalizedMessage()+ex.getCause());
		}
		return allExpressions;
	}
	
	/**
	 * insert a user into the database
	 * @param user
	 */
	public void addUser(User user)
	{
		user.mat2bitmap();
		if(user.getUserID()<=maxUserID)
			return;
		if (user.getProfilePhoto()==null)
		{
			Log.e("running", "Mat is null!");
			return;
		}
		byte[] outputPhoto = bitmap2ByteArray(user.getBitmapPhoto());
		
		if (outputPhoto==null)
		{
			Log.e("running", "photo is null!");
			return;
		}
		SQLiteDatabase db = myDBHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("UserID", user.getUserID());
		values.put("Photo", outputPhoto);
		values.put("Width", user.getBitmapPhoto().getWidth());
		values.put("Height", user.getBitmapPhoto().getHeight());
		try{
        	db.insert("User", null, values);
        }catch(Exception ex){
			Log.e("Exception: ",""+ex.getMessage());
		}
        db.close();
	}
	
	/**
	 * insert an expression into database
	 * @param expression
	 */
	public void addExpression(Expression expression)
	{
		//long countAdd
		
		byte[] outputPhoto = bitmap2ByteArray(expression.getFaceImageBitmap());
		if (outputPhoto==null)
			return;
		
		SQLiteDatabase db = myDBHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("UserID", expression.getUserId());
		values.put("ExpressionID", expression.getExpressionId());
		values.put("Photo", outputPhoto);
		values.put("Width", expression.getFaceImageBitmap().getWidth());
		values.put("Height", expression.getFaceImageBitmap().getHeight());
		values.put("Factor", Double.valueOf((double)expression.getDistortionParameter()));
		try{
        	db.insert("Expression", null, values);
        }catch(Exception ex){
			Log.e("Exception: ",""+ex.getMessage());
		}
        db.close();
	}
	
	/**
	 * update an expression in database
	 * @param expression
	 */
	public void updateExpression(Expression expression)
	{
		//expression.mat2Bitmap();
		int userID = expression.getUserId(); 
		SQLiteDatabase db = myDBHelper.getWritableDatabase();
		boolean ifFound = false;
        String[] columns = {"ExpressionID"};
        try{
			//openDataBase();
        	Cursor cur = db.query("Expression",columns,"UserID="+userID,null,null,null,null);
			cur.moveToFirst();
			for(int i=0;i<cur.getCount();i++){
				if(expression.getExpressionId() == cur.getInt(0))
					ifFound = true;
				cur.moveToNext();
			}
			cur.close();
		}catch(Exception ex){
			Log.e("DatabaseHandler.getCategoryJoke",""+ex.getMessage()+ex.getLocalizedMessage()+ex.getCause());
		}
        if (!ifFound)
        {
        	addExpression(expression);
        	return;
        }
        try{
        	String strFilter = "UserId=" + expression.getUserId()+" and ExpressionID="+expression.getExpressionId();
        	ContentValues args = new ContentValues();
        	args.put("Factor", expression.getDistortionParameter());
        	byte[] outputPhoto = bitmap2ByteArray(expression.getFaceImageBitmap());
    		if (outputPhoto==null)
    			return;
    		args.put("Photo", outputPhoto);
    		args.put("Height", expression.getFaceImageBitmap().getHeight());
    		args.put("Factor", Double.valueOf(expression.getDistortionParameter()));
    		db.update("Expression", args, strFilter, null);
			db.close();
		}catch(Exception ex){
			Log.e("DatabaseHandler.getCategoryJoke",""+ex.getMessage()+ex.getLocalizedMessage()+ex.getCause());
		}
	}
	/**
	 * convert a byte array to mat object
	 * @param input
	 * @return
	 */
	public static Mat byteArray2Mat(byte[] input)
	{
		if(input==null)
			return null;
		ByteArrayInputStream bis = new ByteArrayInputStream(input);
		ObjectInput in = null;
		Mat tempPhoto = null;
		try {
		  in = new ObjectInputStream(bis);
		  tempPhoto = (Mat)in.readObject();
		  bis.close();
		  in.close();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		return tempPhoto;
	}
	/**
	 * convert a mat object to byte array
	 * @param input
	 * @return
	 */
	public static byte[] mat2ByteArray(Mat input)
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = null;
		byte[] outputPhoto = null;
		try {
		  out = new ObjectOutputStream(bos);
		  out.writeObject(input);
		  outputPhoto = bos.toByteArray();
		  out.close();
		  bos.close();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		return outputPhoto;
	}
	/**
	 * convert a Bitmap object to byte array
	 * @param bitmap
	 * @return
	 */
	public static byte[] bitmap2ByteArray(Bitmap bitmap)
	{
		int bytes = bitmap.getWidth()*bitmap.getHeight()*4; 
    	ByteBuffer buffer = ByteBuffer.allocate(bytes); //Create a new buffer
    	bitmap.copyPixelsToBuffer(buffer); //Move the byte data to the buffer
    	byte[] array = buffer.array(); //Get the underlying array containing the data.
		return array;
	}
	/**
	 * convert a byte array to a bitmap object
	 * @param input byte array
	 * @param width width of the bitmap
	 * @param height height of the bitmap
	 * @return converted bitmap
	 */
	public static Bitmap byteArray2Bitmap(byte[] input, int width, int height)
	{
		ByteBuffer temp = ByteBuffer.wrap(input);
		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		bitmap.copyPixelsFromBuffer(temp);
		return bitmap;
		
	}
	/**
	 * getter of the myDBHelper
	 * @return
	 */
	public MyDBHelper getMyDBHelper() {
		return myDBHelper;
	}

	/**
	 * setter of myDBHelper
	 * @param myDBHelper
	 */
	public void setMyDBHelper(MyDBHelper myDBHelper) {
		this.myDBHelper = myDBHelper;
	}
	
}
