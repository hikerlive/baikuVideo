package com.baiku.android.app;

import com.baiku.android.BaikuApplication;

public class LazyImageLoader {
	private static final String TAG = "LazyImageLoader";
	private ImageManager mImageManager = new ImageManager(BaikuApplication.mContext);
	
	public ImageManager getImageManager() {
		return mImageManager;
	}
	
}
