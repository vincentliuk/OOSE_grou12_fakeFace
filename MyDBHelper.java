package com.group12.oose.fakeface;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * This class is responsible for create the database and all tables in this database   
 * @author JHU Fall2012 OOSE group 12
 *
 */
public class MyDBHelper extends SQLiteOpenHelper {
	//name of the database
	/**
	 * database name
	 */
	private static final String DATABASE_NAME = "Fackface";
	
	//first table: User
	private static final String A_ID = "UserID";
	private static final String IMAGE = "Photo";
	private static final String WIDTH = "Width";
	private static final String HEIGHT = "Height";
	private static final int DATABASE_VERSION = 2;
    private static final String USER_TABLE_NAME = "User";
    private static final String USER_TABLE_CREATE =
                "CREATE TABLE " + USER_TABLE_NAME + " (" +
                A_ID + " INTEGER , " +
                WIDTH + " INTEGER , " +
                HEIGHT + " INTEGER , " +
                IMAGE + " BLOB);";
    //second table Expression
    private static final String EXPRESSION_TABLE_NAME = "Expression";
    private static final String E_ID = "ExpressionID";
    private static final String E_PARA = "Factor";
    private static final String E_PHOTO = "Photo";
    private static final String EXPRESSION_TABLE_CREATE = 
    		"CREATE TABLE " + EXPRESSION_TABLE_NAME + " (" +
              A_ID + " INTEGER , " +
    				E_ID + " INTEGER , " +
                WIDTH + " INTEGER , " +
                HEIGHT + " INTEGER , " +
                    E_PHOTO + " BLOB, " +
    				E_PARA + " REAL);";
    /**
     * constructor
     * @param context application context
     */
	public MyDBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		// TODO Auto-generated constructor stub
	}

	/**
	 * this function define the operations executed when the database created.
	 * Two tables are created: user and expression
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL(USER_TABLE_CREATE);
		db.execSQL(EXPRESSION_TABLE_CREATE);
		Log.e("create database","xxxx");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}

}
