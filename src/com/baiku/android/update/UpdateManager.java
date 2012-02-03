package com.baiku.android.update;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;
import android.util.Log;

public class UpdateManager {
	private static final String TAG = "UpdateManager";
	private UpdateListener mUpdateListener = null;
	private static double CURRENT_VERSION=1.2;
	
	public void setListener(UpdateListener listen) {
		mUpdateListener = listen;
	}
	
	public UpdateListener getListener() {
		return mUpdateListener;
	}
	
	public void checkUpdate(String url, String requestJson) {
		try {
			Log.i(TAG, String.format("checkUpdate request: %s", requestJson));
			
			DefaultHttpClient httpclient = new DefaultHttpClient();
			HttpPost post = new HttpPost(url);
			post.setEntity(new StringEntity(requestJson));
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			
			String responseJson = httpclient.execute(post, responseHandler);
			Log.i(TAG, String.format("checkUpdate response: %s", responseJson));
			
			JSONObject root = new JSONObject(responseJson);
			int mode = root.getInt("result");
			String downloadUrl = root.getString("url");
			if (root.has("version")) {
				double version = root.getDouble("version");
				if (version <= CURRENT_VERSION) { // 无需更新
					return;
				}
			}
			if (downloadUrl.lastIndexOf(".apk") != -1) {
				if (mode == 1) {
					if (null != mUpdateListener) {
						Log.d(TAG, "onUpdateAvaliable");
						mUpdateListener.onUpdateAvaliable(downloadUrl);
					}
				} 
				else if (mode == 2) {
					if (null != mUpdateListener) {
						Log.d(TAG, "onUpdateForce");
						mUpdateListener.onUpdateForce(downloadUrl);
					}					
				}
				else if (mode == 3) {
					if (null != mUpdateListener) {
						Log.d(TAG, "onUpdateFromPage");
						mUpdateListener.onUpdateFromPage(downloadUrl);
					}					
				}
				else if (mode == 0) {
					if (null != mUpdateListener) {
						Log.d(TAG, "onUpdateNone");
						mUpdateListener.onUpdateNone(downloadUrl);
					}					
				}
			} else {
				if (null != mUpdateListener) {
					mUpdateListener.onUpdateNone(downloadUrl);
				}				
			}
		} catch (Exception e) {
			Log.e(TAG, String.format("checkUpdate exception error: %s", e.getMessage()));
			e.printStackTrace();
		}
	}

}
