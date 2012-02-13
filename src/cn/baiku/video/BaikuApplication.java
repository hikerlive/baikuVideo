package cn.baiku.video;

import java.io.File;
import java.util.Date;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.util.Log;
import cn.baiku.video.api.BaikuApi;
import cn.baiku.video.app.LazyImageLoader;
import cn.baiku.video.app.Preferences;
import cn.baiku.video.db.BaikuDatabase;
import cn.baiku.video.update.UpdateManager;

public class BaikuApplication extends Application {
	public static final String TAG = "BaikuMediaApplication";
	public static BaikuDatabase mDb;
	public static BaikuApi mApi;
	public static SharedPreferences mPref;
	public static Context mContext;
	public static LazyImageLoader mImageLoader;
	public static UpdateManager mUpdateManager;
	private static boolean mIsCanCheckUpdate;
	private static String mImageType = "";
	private static Uri mImageUri = null;
	public static boolean mbReloadUrl = false;
	
	@Override
	public void onCreate() {
		Log.i(TAG, "create application");
		super.onCreate();
		
		mContext = getApplicationContext();
		mImageLoader = new LazyImageLoader();
		mUpdateManager = new UpdateManager();
		mDb = BaikuDatabase.getInstance(this);
		mApi = new BaikuApi();
		mIsCanCheckUpdate = true;
		
		mPref = PreferenceManager.getDefaultSharedPreferences(this);
		String username = mPref.getString(Preferences.USERNAME_KEY, "");
		String password = mPref.getString(Preferences.PASSWORD_KEY, "");
		boolean wapsite = mPref.getBoolean(Preferences.WAPSITE_KEY, false);
		if (BaikuApi.isValidCredentials(username, password)) {
			mApi.setCredentials(username, password);
			mApi.setWapsite(wapsite);
		}
	}
	
	@Override
	public void onTerminate() {
		Log.i(TAG, "terminate application");
		mDb.closeDatabase();
		super.onTerminate();
	}

	/**
	 * helper method of clearCacheFolder, recursive
	 * returns number of deleteFiles
	 */
	private static int clearCacheFolder(final File dir, final int numDays) {
		int deleteFiles = 0;
		if (dir != null && dir.isDirectory()) {
			try {
				for (File child:dir.listFiles()) {
					// first delete subdirectories recursively
					if (child.isDirectory()) {
						deleteFiles += clearCacheFolder(child, numDays);
					}
					
					// then delete the files and subdirectories in this dir
					// only empty directories can be deleted, so subdirs have done been first
					if (child.lastModified() < new Date().getTime() - numDays * DateUtils.DAY_IN_MILLIS) {
						if (child.delete()) {
							deleteFiles ++;
						}
					}
				}
			} catch (Exception e) {
				Log.e(TAG, String.format("Failed to clean the cache, error %s", e.getMessage()));
			}
		}
		return deleteFiles;
	}
	
	/**
	 * Delete the files older than the num days from application cache
	 * 0 means all files
	 */
	public static void clearCache(final Context context, final int numDays) {
		Log.i(TAG, String.format("Starting cache prune, deleting files order than %d days", numDays));
		int deleteFiles = clearCacheFolder(context.getCacheDir(), numDays);
		Log.i(TAG, String.format("Cache pruning completed, %d files deleted", deleteFiles));
	}
	
	public static boolean isCanCheckedUpdate() {
		if (mIsCanCheckUpdate) {
			mIsCanCheckUpdate = false;
			return true; 
		}
		
		return mIsCanCheckUpdate;
	}
	
	public static void setImage(String imageType, Uri uri) {
		mImageType = imageType;
		mImageUri = uri;
	}
	public static String getImageType() {
		return mImageType; 
	}
	public static Uri getImageUri() {
		return mImageUri;
	}

}