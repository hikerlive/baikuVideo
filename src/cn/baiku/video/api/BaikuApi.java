package cn.baiku.video.api;

import java.io.File;
import java.io.UnsupportedEncodingException;

import android.text.TextUtils;
import android.util.Log;
import cn.baiku.video.http.CustomHttpClient;

public class BaikuApi implements java.io.Serializable {
	public static final String TAG = "BaikuApi";
	
	private String mUserName = "";
	private String mPassword = "";
	private boolean mWapsiteTested = false;
	
	private static final String WAP_SHARE_URL = "http://share.baiku.cn/share";
	private static final String WAP_PROFILE_URL = "http://wap.baiku.cn/personalpage.action?reqcode=boxpage&boxtype=1";
	private static final String WAP_HOME_URL="http://wap.baiku.cn/homepage.action?reqcode=showhomepage";
	
	private static final String TEST_WAP_SHARE_URL = "http://10.130.29.232:8080/share";
	private static final String TEST_WAP_PROFILE_URL = "http://10.130.29.239:8080/personalpage.action?reqcode=boxpage&boxtype=1";
	private static final String TEST_WAP_HOME_URL = "http://10.130.29.239:8080/homepage.action?reqcode=showhomepage";
	
	public BaikuApi() {
	}
	
	public BaikuApi(String name, String password) {
		mUserName = name;
		mPassword = password;
	}
	
	public static boolean isValidCredentials(String name, String password) {
		return !TextUtils.isEmpty(name) && !TextUtils.isEmpty(password);
	}
	
	public void setCredentials(String name, String password) {
		mUserName = name;
		mPassword = password;
	}
	
	public void setWapsite(boolean bwapsite) {
		String waphint = (bwapsite ? "unline" : "online");
		Log.d(TAG, "wapsite connected " + waphint);
		mWapsiteTested = bwapsite;
	}
	
	public boolean getWapsite() {
		return mWapsiteTested;
	}
	
	public String getWapFirstUrl() {		
		return (mWapsiteTested) ? TEST_WAP_HOME_URL : WAP_HOME_URL;
	}
	
	public String getWapProfileUrl() {		
		return (mWapsiteTested) ? TEST_WAP_PROFILE_URL : WAP_PROFILE_URL;
	}
	
	public String getWapShareUrl() {
		return (mWapsiteTested) ? TEST_WAP_SHARE_URL : WAP_SHARE_URL;
	}
	
	public boolean isLoggedIn() {
		return isValidCredentials(mUserName, mPassword);
	}
	
	public String getUserName() {
		return mUserName;
	}
	
	public String getPassword() {
		return mPassword;
	}
	
	public void reset() {
		setCredentials("", "");
	}
	
	public void uploadStatus(String subject, String tags, String type, File file) throws UnsupportedEncodingException {
		CustomHttpClient.post(getWapShareUrl(), mUserName, mPassword, 
				subject, tags, type, file, null);
	}
	
	@Override
	public String toString() {
		return "";
	}
}