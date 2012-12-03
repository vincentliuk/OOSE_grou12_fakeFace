package com.group12.oose.fakeface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

// add import
import android.R.layout;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;

import android.view.Menu;

import android.view.LayoutInflater;

import android.graphics.Bitmap;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import android.widget.TextView;
import android.view.View.OnClickListener;
// 

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * FakeFace App developed by JHU Fall2012 OOSE group 12
 * <p>
 * Main activity: Android App works by Activity. Everything is done
 * in this activity, including initiate and open camera and retrieve
 * images, face detection, distort the face and apply distortion, 
 * save Users by ID and photo storage in database, and all the UI buttons
 * and bar manipulation.
 * <\p>
 * This project is based on the Facedetection expamle provided by OpenCV.
 * @author JHU Fall2012 OOSE group 12
 */
public class FdActivity extends Activity implements OnSeekBarChangeListener,
		CvCameraViewListener {

	private static final String TAG = "OOSE::Activity";
	private static final Scalar FACE_RECT_COLOR = new Scalar(0, 255, 0, 255);
	
	public static final int JAVA_DETECTOR = 0;
	public static final int NATIVE_DETECTOR = 1;

	// delete
	private MenuItem mItemFace50;
	private MenuItem mItemFace40;
	private MenuItem mItemFace30;
	private MenuItem mItemFace20;
	private MenuItem mItemType;
    
	/**
	 * real-time screen image retrieval in OpenCv data structure Mat.
	 * colored mRgba and Grayed mGray
	 */
	private Mat mRgba;
	private Mat mGray;
	
	private File mCascadeFile;
	private CascadeClassifier mJavaDetector;
	private DetectionBasedTracker mNativeDetector;

	private int mDetectorType = JAVA_DETECTOR;
	private String[] mDetectorName;

	private float mRelativeFaceSize = 0.2f;
	private int mAbsoluteFaceSize = 0;

	/**
	 * The major CameraView declaration.
	 */
	private CameraBridgeViewBase mOpenCvCameraView;

	/**
	 * build an Expression class object currentExpression for
	 * the real-time manipulation of expression.
	 */
	private Expression currentExpression;
	
	/**
	 * slide bar declaration for distortion ajustment.
	 */
	private VerticalSeekBar eye_bar;

	/**
	 * image mat for snapShot.
	 */
	private Mat imageSnap;

	/** 
	 * Database declaration of users and currentUser.
	 */
	private MyDBHelper myDBHelper;
	private DBUtil databaseUtil;
	private User currentUser;
	private ArrayList<User> allUsers;
	
	/**
	 * All UI buttons declaration.
	 */
	private ArrayList<ImageButton> userButtons;
	private ArrayList<ImageButton> expressionButtons;
	private ImageButton snapShot;
	private ImageButton customer1ImageButton;
	private ImageButton customer2ImageButton;
	private ImageButton customer3ImageButton;
	private ImageButton addNewUserImageButton;
	private ImageButton distortion1ImageButton;
	private ImageButton distortion2ImageButton;
	
	/**
	 *  temp boolean mark for taking photo for new user.
	 */
	private boolean getPhoto;

	/** 
	 * load openCV library
	 */
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				Log.i(TAG, "OpenCV loaded successfully");

				// Load native library after(!) OpenCV initialization
				System.loadLibrary("detection_based_tracker");

				try {
					// load cascade file from application resources
					InputStream is = getResources().openRawResource(
							R.raw.lbpcascade_frontalface);
					File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
					mCascadeFile = new File(cascadeDir,
							"lbpcascade_frontalface.xml");
					FileOutputStream os = new FileOutputStream(mCascadeFile);

					byte[] buffer = new byte[4096];
					int bytesRead;
					while ((bytesRead = is.read(buffer)) != -1) {
						os.write(buffer, 0, bytesRead);
					}
					is.close();
					os.close();

					mJavaDetector = new CascadeClassifier(
							mCascadeFile.getAbsolutePath());
					if (mJavaDetector.empty()) {
						Log.e(TAG, "Failed to load cascade classifier");
						mJavaDetector = null;
					} else
						Log.i(TAG, "Loaded cascade classifier from "
								+ mCascadeFile.getAbsolutePath());

					mNativeDetector = new DetectionBasedTracker(
							mCascadeFile.getAbsolutePath(), 0);

					
					cascadeDir.delete();

				} catch (IOException e) {
					e.printStackTrace();
					Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
				}

				mOpenCvCameraView.enableView();
			}
				break;
			default: {
				super.onManagerConnected(status);
			}
				break;
			}
		}
	};

	/**
	 * Main Activity Constructor
	 */
	public FdActivity() {
		mDetectorName = new String[2];
		mDetectorName[JAVA_DETECTOR] = "Java";
		mDetectorName[NATIVE_DETECTOR] = "Native (tracking)";

		Log.i(TAG, "Instantiated new " + this.getClass());
	}

	/** 
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "called onCreate");
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
        //Load main xml file 
		setContentView(R.layout.face_detect_surface_view);
        
		//Distortion bar statement and add its listener
		eye_bar = (VerticalSeekBar) findViewById(R.id.eyeseekbar);
		eye_bar.setMax(100);
		eye_bar.setOnSeekBarChangeListener(this);
		eye_bar.setVisibility(View.INVISIBLE);

		// CameraView declaration 
		mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.fd_activity_surface_view);
		mOpenCvCameraView.setCvCameraViewListener(this);

		// Database initiation 
		myDBHelper = new MyDBHelper(getApplicationContext());

		databaseUtil = new DBUtil();
		databaseUtil.setMyDBHelper(myDBHelper);

		currentUser = new User();
		allUsers = databaseUtil.loadAllUsers();

		// expression initiation
		userButtons = new ArrayList<ImageButton>();
		expressionButtons = new ArrayList<ImageButton>();
		
		currentExpression = new Expression(0, 0);
		currentExpression.setDistortionParameter(0.1f);
		
		// user's candidates photos loaded as imageButton by method galleryButton.
		galleryButton();

		// initialize getphoto.
		getPhoto = false;

	}
    
	/**
	 * gallery button method. The candidate users photos displayed
	 * as imagebutton loaded on the top of screen for user's selection.
	 * Once the user select his/her own photo, the user's ID and his/her
	 * own expression will be loaded into the distortion button shown
	 * below for further distortion manipulation.
	 * @return
	 */
	public void galleryButton() {

		// add first candidate user view shown as imagebutton
		RelativeLayout mainLayout = (RelativeLayout) findViewById(R.id.main_layout1);
		View view = getLayoutInflater().inflate(R.layout.customer1_image,
		        mainLayout, false);
		mainLayout.addView(view);

		customer1ImageButton = (ImageButton) findViewById(R.id.customer1imageButton);
		
		
		// add second candidate user view shown as imagebutton.
		View view2 = getLayoutInflater().inflate(R.layout.customer2_image,
				mainLayout, false);
		mainLayout.addView(view2);
		final ImageButton customer2ImageButton = (ImageButton) findViewById(R.id.customer2imageButton);
		
		// add third candidate user view shown as imagebutton.
		View view3 = getLayoutInflater().inflate(R.layout.customer3_image,
				mainLayout, false);
		mainLayout.addView(view3);
		customer3ImageButton = (ImageButton) findViewById(R.id.customer3imageButton);
		
		// add new user button.
		View view4 = getLayoutInflater().inflate(R.layout.add_newuser, 
				mainLayout, false);
		mainLayout.addView(view4);
		addNewUserImageButton = (ImageButton) findViewById(R.id.addNewUser);
		
		// candidate user1 button listener. When click, get through into this 
		// user and load his/her expression into the distortion button.
		customer1ImageButton.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {

				customer2ImageButton.setVisibility(View.INVISIBLE);
				customer3ImageButton.setVisibility(View.INVISIBLE);
				
				if (allUsers != null && allUsers.size() > 0)
					currentUser = allUsers.get(0);
				currentUser.setExpressions(databaseUtil
						.loadAllExpressions(currentUser.getUserID()));

				distortionButton();

			}

		});
		
		// candidate user2 button listener. When click, this user photo displayed
		// on the customerbutton1, and get through into this user database and 
		// load his/her expression into the distortion button.
		customer2ImageButton.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {

				customer2ImageButton.setVisibility(View.INVISIBLE);
				customer3ImageButton.setVisibility(View.INVISIBLE);

				if (allUsers != null && allUsers.size() > 1) {
					currentUser = allUsers.get(1);
					currentUser.setExpressions(databaseUtil
							.loadAllExpressions(currentUser.getUserID()));
					customer1ImageButton.setImageBitmap(allUsers.get(1)
							.getBitmapPhoto());
				} else {
					// add new user
				}
				distortionButton();

			}

		});
		
		// candidate user3 button listener. When click, this user photo displayed
	    // on the customerbutton1, and get through into this user database and 
		// load his/her expression into the distortion button.
		customer3ImageButton.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {

				customer2ImageButton.setVisibility(View.INVISIBLE);
				customer3ImageButton.setVisibility(View.INVISIBLE);

				if (allUsers != null && allUsers.size() > 1) {
					currentUser = allUsers.get(1);
					currentUser.setExpressions(databaseUtil
							.loadAllExpressions(currentUser.getUserID()));
					customer1ImageButton.setImageBitmap(allUsers.get(1)
							.getBitmapPhoto());
				} else {
					// add new user
				}
				distortionButton();

			}

		});
		
		// add new user button. When click, set up a new user ID and simultaneously
		// take a picture as photo of this user, all these info stored into the
		// database
		addNewUserImageButton.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {

				
				currentUser = new User();
			
				getPhoto = true;
				
			}

		});
		Log.e("test", "test: after listener");
		int countUser = allUsers.size();
		userButtons.add(customer1ImageButton);
		userButtons.add(customer2ImageButton);
		userButtons.add(customer3ImageButton);
		Log.e("test", "test: add to list");
		int i = 0;
		for (i = 0; i < countUser && i < userButtons.size() - 1; ++i) {
				userButtons.get(i).setImageBitmap(
					allUsers.get(i).getBitmapPhoto());
		}

		
		Log.e("test", "test: set bitmap");
	}

	/**
	 * Distortion Button method. Once a user is identified by clicking 
	 * one of the gallerybutton, the user from data is loaded ant his/her
	 * expression pattern will be loaded at bottom as imagebutton. By 
	 * selecting the expression, the distortion effect will be added 
	 * to the real-time face, and a slide bar appears on the left for 
	 * adjusting the distortion extent. If the distortion is empty, by
	 * clicking that, the user will be required to add a new expression
	 * by pressing the snapshot button to be stored in the database for
	 * further loading.
	 * @return
	 */
	public void distortionButton() {

		RelativeLayout mainLayout = (RelativeLayout) findViewById(R.id.main_layout1);
		
		// add first distortion button.
		View view = getLayoutInflater().inflate(R.layout.distortion1_image,
				mainLayout, false);
		mainLayout.addView(view);
		distortion1ImageButton = (ImageButton) findViewById(R.id.distortion1imageButton);
		
		// add 2nd distortion button.
		View view2 = getLayoutInflater().inflate(R.layout.distortion2_image,
				mainLayout, false);
		mainLayout.addView(view2);
		distortion2ImageButton = (ImageButton) findViewById(R.id.distortion2imageButton);
		
		// distortion1 button listener. By clicking it, the distortion pattern 
		// will be loaded to the real-time face on the screen and the slide bar
		// is shown for adjusting the distortion.
		distortion1ImageButton.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {

				RelativeLayout mainLayout = (RelativeLayout) findViewById(R.id.main_layout1);

				distortion1ImageButton.setVisibility(View.INVISIBLE);
				distortion2ImageButton.setVisibility(View.INVISIBLE);
				if (currentUser.getExpressions().size() > 0) {
					currentExpression = currentUser.getExpressions().get(0);
					// set parameter
				} else {
					currentExpression = new Expression(currentUser.getUserID(),
							currentUser.getExpressions().size());

				}
				eye_bar.setVisibility(View.VISIBLE);

			}

		});
		
		// distortion2 button listener. By clicking it, the distortion pattern 
		// will be loaded to the real-time face on the screen and the slide bar
		// is shown for adjusting the distortion.
		distortion2ImageButton.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {

				RelativeLayout mainLayout = (RelativeLayout) findViewById(R.id.main_layout1);

				distortion1ImageButton.setVisibility(View.INVISIBLE);
				distortion2ImageButton.setVisibility(View.INVISIBLE);

				if (currentUser.getExpressions().size() > 1) {
					currentExpression = currentUser.getExpressions().get(1);
					// set parameter
				} else if (currentUser.getExpressions().size() == 1) {
					currentExpression = new Expression(currentUser.getUserID(),1);
				} else {
					return;
				}
				eye_bar.setVisibility(View.VISIBLE);
			}

		});
		expressionButtons.clear();
		expressionButtons.add(distortion1ImageButton);
		expressionButtons.add(distortion2ImageButton);
		for (int i = 0; i < expressionButtons.size()&&i<currentUser.getExpressions().size(); ++i)
	    {
			expressionButtons.get(i).setImageBitmap(currentUser.getExpressions().get(i).getFaceImageBitmap());
		}

	}

	@Override
	/**
	 * onPause
	 */
	public void onPause() {
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
		super.onPause();
	}
	/**
	 * onResume()
	 */
	@Override
	public void onResume() {
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this,
				mLoaderCallback);
	}
	/**
	 * onDestroy
	 */
	public void onDestroy() {
		super.onDestroy();
		mOpenCvCameraView.disableView();
	}
	/**
	 * actions when camera view is started.
	 */
	public void onCameraViewStarted(int width, int height) {
		mGray = new Mat();
		mRgba = new Mat();
	}
	/**
	 * actions when camera view is stopped.
	 */
	public void onCameraViewStopped() {
		mGray.release();
		mRgba.release();
	}

	private Bitmap bmp = null;
	
	/**
	 * The core fuction of this app.
	 * The image captured by the camera is the input. Then face detection is applied to this image.
	 * If there is no face detected(either no face or face size is too small), the image will be unchanged and returned. 
	 * If at least one face is detected, it will be distorted. The modified image will be returned.
	 */
	public Mat onCameraFrame(Mat inputFrame) {
		inputFrame.copyTo(mRgba);

		// snapshot button and its listener added for taking picture 
		// and add expression pattern to specific user
		snapShot = (ImageButton) findViewById(R.id.snapShot);
		snapShot.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {

				
				databaseUtil.addExpression(currentExpression);
			}

		});
		
		//convert color image to gray for face dection.
		Imgproc.cvtColor(inputFrame, mGray, Imgproc.COLOR_RGBA2GRAY);
		
		//set minimize face size
		if (mAbsoluteFaceSize == 0) {
			int height = mGray.rows();
			if (Math.round(height * mRelativeFaceSize) > 0) {
				mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
			}
			mNativeDetector.setMinFaceSize(mAbsoluteFaceSize);
		}

		MatOfRect faces = new MatOfRect();
		
		//choose face detector
		if (mDetectorType == JAVA_DETECTOR) {
			if (mJavaDetector != null)
				mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2,
						2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
						new Size(mAbsoluteFaceSize, mAbsoluteFaceSize),
						new Size());
		} else if (mDetectorType == NATIVE_DETECTOR) {
			if (mNativeDetector != null)
				mNativeDetector.detect(mGray, faces);
		} else {
			Log.e(TAG, "Detection method is not selected!");
		}
		
		//check whether there is face in the image.
		Rect[] facesArray = faces.toArray();
		if (facesArray.length > 0) {
			Log.v(TAG, "faces");
			Core.rectangle(mRgba, facesArray[0].tl(), facesArray[0].br(),
					FACE_RECT_COLOR, 3);

			// Apply distortion
			Rect detectedFace = facesArray[0];
			Mat faceRect = mRgba.submat(detectedFace);
			if (getPhoto) {
				//if a new user is created, the first detected face will be used as his/her profile image.
				Mat tempFace = faceRect.clone();
				currentUser.setProfilePhoto(tempFace);
				currentUser.setUserID(allUsers.size());
				
				databaseUtil.addUser(currentUser);
				
				currentUser.mat2bitmap();
				
				if (currentUser.getBitmapPhoto() != null
						&& userButtons.get(0) != null) {
					Log.v(TAG, "get photo3.3 " + userButtons.size());
					Bitmap bitmapPhoto = Bitmap.createBitmap(tempFace.cols(),
							tempFace.rows(), Bitmap.Config.ARGB_8888);
					Utils.matToBitmap(tempFace, bitmapPhoto);
					try {
						customer1ImageButton.setImageBitmap(bitmapPhoto);
					} catch (Exception e) {
						Log.v("exception", "test: " + e.toString());
					}
					
				getPhoto = false;
				
			}
			
			Log.v(TAG, "para" + currentExpression.getDistortionParameter());
			//apply distortion
			currentExpression.setFaceImageFromSubmat(faceRect);
			currentExpression.applyDistortion();
		}
		}
		return mRgba;
	}

	/**
	 * menu items
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.i(TAG, "called onCreateOptionsMenu");
		mItemFace50 = menu.add("Face size 50%");
		mItemFace40 = menu.add("Face size 40%");
		mItemFace30 = menu.add("Face size 30%");
		mItemFace20 = menu.add("Face size 20%");
		mItemType = menu.add(mDetectorName[mDetectorType]);
		return true;
	}

	/**
	 * menu items actions
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
		if (item == mItemFace50)
			setMinFaceSize(0.5f);
		else if (item == mItemFace40)
			setMinFaceSize(0.4f);
		else if (item == mItemFace30)
			setMinFaceSize(0.3f);
		else if (item == mItemFace20)
			setMinFaceSize(0.2f);
		else if (item == mItemType) {
			mDetectorType = (mDetectorType + 1) % mDetectorName.length;
			item.setTitle(mDetectorName[mDetectorType]);
			setDetectorType(mDetectorType);
		}
		return true;
	}

	/**
	 * set minimize face size
	 */
	private void setMinFaceSize(float faceSize) {
		mRelativeFaceSize = faceSize;
		mAbsoluteFaceSize = 0;
	}

	/**
	 * set detector
	 */
	private void setDetectorType(int type) {
		if (mDetectorType != type) {
			mDetectorType = type;

			if (type == NATIVE_DETECTOR) {
				Log.i(TAG, "Detection Based Tracker enabled");
				mNativeDetector.start();
			} else {
				Log.i(TAG, "Cascade detector enabled");
				mNativeDetector.stop();
			}
		}
	}
    
	/**
	 * slide bar listener.when the bar being moved, the distortion extent or parameter
	 * will be changed. The effect will be shown on the real-time face of user.
	 * @param seekbar the slide bar we are using for adjusting distortion
	 * @param progress the bar moves
	 * @param fromUser
	 * @return
	 */
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// TODO Auto-generated method stub
		currentExpression.setDistortionParameter(seekBar.getProgress() / 10.0f
				/ seekBar.getMax());
		String TAG = null;
		Log.v(TAG, "problemkl1" + progress);
	}

	/**
	 * part of original example
	 */
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

	}

	/**
	 * part of original example
	 */
	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

	}

}
