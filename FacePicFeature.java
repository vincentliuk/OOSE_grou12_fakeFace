package com.group12.oose.fakeface;

import android.graphics.Bitmap;

/**
 * This class is for extracting face feature for
 * further face recognition when loading the existing
 * user's database.
 * @author JHU Fall2012 OOSE group 12
 *
 */
public class FacePicFeature {

	Byte[][] customerPic = null;
	/**
	 * constructor get current user photo
	 * and then apply feature extraction.
	 * @param customerPicture
	 */
	public FacePicFeature(Byte[][] customerPicture){
		
		customerPic = customerPicture;
		FacePicFeatureExtraction(customerPic);
		
	}
	
	// face feather average and variance as attributes
	// for face recognition use.
	double customerPicAverage[] = new double[9];
	double customerPicVariance[] = new double[9];
	
	/**
	 * method face feature extraction: by segmenting 
	 * the whole face into 9 parts, and calucate the 
	 * average of each part's pixels and its variance.
	 * Save as feature for callback.
	 * @param customerPic2
	 */
	public void FacePicFeatureExtraction(Byte[][] customerPic2){
		int width = customerPic2.length;
		int height = customerPic2[0].length;
		for (int i=0;i<=8;i++){
			int sizeCount = 0;
			for(int x=i*(width/3);x<=(i+1)*(width/3);x++){
				for(int y=i*(height/3);y<=(i+1)*(height/3);y++){
					
					customerPicAverage[i]=customerPicAverage[i]+customerPic2[x][y];
					sizeCount = sizeCount + 1;
				}		
			}
			customerPicAverage[i] = customerPicAverage[i]/sizeCount;
		}
		
	    for (int i=0;i<=8;i++){
			int sizeCount = 0;
			for(int x=i*(width/3);x<=(i+1)*(width/3);x++){
				for(int y=i*(height/3);y<=(i+1)*(height/3);y++){
					
					customerPicVariance[i]=customerPicVariance[i]+Math.pow((customerPic2[x][y]-customerPicAverage[i]), 2);
					sizeCount = sizeCount + 1;
				}		
			}
			customerPicVariance[i] = Math.pow(customerPicVariance[i]/sizeCount, 0.5);
		}
		
	}
	
	
}
