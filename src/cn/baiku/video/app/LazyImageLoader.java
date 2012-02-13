package cn.baiku.video.app;

import cn.baiku.video.BaikuApplication;

public class LazyImageLoader {
	private static final String TAG = "LazyImageLoader";
	private ImageManager mImageManager = new ImageManager(BaikuApplication.mContext);
	
	public ImageManager getImageManager() {
		return mImageManager;
	}
	
}
