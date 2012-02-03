package com.baiku.android;

import com.baiku.android.app.Preferences;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.baiku.android.api.BaikuApi;

public class BaseActivity extends Activity {
	private static final String TAG = "BaseActivity";
	private static final String APP_EXIT_BROADCAST_ACTION = "com.baiku.android.APP_EXIT";
	
	private static final int RESULT_LOGOUT = RESULT_FIRST_USER + 1;
	
	protected static final int REQUEST_CODE_PREFERENCES = 10;
	
	protected static final int OPTIONS_MENU_ID_LOGOUT = 1;
	
	protected static final int OPTIONS_MENU_ID_PREFERENCES = 2;
	
	protected static final int OPTIONS_MENU_ID_ABOUT = 3;
	
	protected static final int OPTIONS_MENU_ID_EXIT = 13;

	protected boolean mIsLogout = false;
	
	private static int mRefCount;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		_onCreate(savedInstanceState);
	}
	
	protected boolean _onCreate(Bundle savedInstanceState) {
		if (!checkIsLoggedIn()) {
			return false;
		} else {
			mRefCount ++;
			Log.d(TAG, "onCreate() refCount: " + mRefCount);
			return true;
		}
	}
	
	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, "receive exit broadcast...");
			finish();
		}
	};
	
	@Override
	protected void onResume() {
		Log.d(TAG, "onResume.");
		super.onResume();
		IntentFilter filter = new IntentFilter();
		filter.addAction(APP_EXIT_BROADCAST_ACTION);
		registerReceiver(mBroadcastReceiver, filter);
	}
	
	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroy.");
		super.onDestroy();
		
		if (mRefCount > 0) {
			unregisterReceiver(mBroadcastReceiver);
			mRefCount --;
			Log.d(TAG, "onDestroy() refCount: " + mRefCount);
			if (mRefCount == 0 && mIsLogout == false) {
				Log.d(TAG, "clearCache for this usetime and real exit");
				
				BaikuApplication.clearCache(BaikuApplication.mContext, 0);
				// 退出后台线程，并且销毁静态变量。
				System.exit(0);
			}
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == REQUEST_CODE_PREFERENCES && resultCode == RESULT_OK) {
			handlePreferences();
		}
	}
	
	public BaikuApi getApi() {
		return BaikuApplication.mApi;
	}
	
	public void showLogin() {
		Intent intent = new Intent(this, LoginActivity.class);
		intent.putExtra(Intent.EXTRA_INTENT, getIntent());
		startActivity(intent);
	}
	
	protected boolean checkIsLoggedIn() {
		if (!getApi().isLoggedIn()) {
			Log.d(TAG, "Not logged in.");
			handleLoggedOut();
			return false;
		}	
		return true;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		MenuItem item;
		item = menu.add(0, OPTIONS_MENU_ID_PREFERENCES, 0,
				R.string.omenu_settings);
		item.setIcon(android.R.drawable.ic_menu_preferences);

		item = menu.add(0, OPTIONS_MENU_ID_LOGOUT, 0, R.string.omenu_signout);
		item.setIcon(android.R.drawable.ic_menu_revert);

		item = menu.add(0, OPTIONS_MENU_ID_ABOUT, 0, R.string.omenu_about);
		item.setIcon(android.R.drawable.ic_menu_info_details);

		item = menu.add(0, OPTIONS_MENU_ID_EXIT, 0, R.string.omenu_exit);
		item.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case OPTIONS_MENU_ID_LOGOUT:
			onLogout();
			return true;
		case OPTIONS_MENU_ID_PREFERENCES:
			handleSetting();
			return true;
		case OPTIONS_MENU_ID_ABOUT:
			Intent intent = new Intent().setClass(this, AboutActivity.class);
			startActivity(intent);
			return true;
		case OPTIONS_MENU_ID_EXIT:
			handleExit();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void onLogout() {
		Dialog dialog = new AlertDialog.Builder(BaseActivity.this)
			.setTitle("提示").setMessage("确实要注销吗?")
			.setPositiveButton("确定", new OnClickListener(){
				public void onClick(DialogInterface dialog, int which) {
					_logout();
				}
			}).setNegativeButton("取消", new OnClickListener(){
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			}).create();
		dialog.show();
	}
	
	private void _logout() {
		
		mIsLogout = true;
		
		// clear memory data.
		getApi().reset();
		
		// clear webview cache for this user.
		BaikuApplication.clearCache(BaikuApplication.mContext, 0);
		
		// Clear sharedPreferences
		SharedPreferences.Editor editor = BaikuApplication.mPref.edit();
		editor.clear();
		editor.commit();
		
		// relogin
		handleLoggedOut();
	}

	protected void handleLoggedOut() {
		/*
		if (isTaskRoot()) {
			showLogin();
		} else {
			setResult(RESULT_LOGOUT);
		}
		*/
		
		showLogin();
		finish();
	}

	protected void handleExit() {
		/*
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		startActivity(intent);
		*/
		Log.d(TAG, "send a exit broadcast msg...");
		Intent intent = new Intent();
		intent.setAction(APP_EXIT_BROADCAST_ACTION);
		sendBroadcast(intent);
	}

	protected void handleSetting() {
		/*
	     final Intent settings = new Intent(android.provider.Settings.ACTION_SETTINGS);     
         settings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |     
                 Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);          
         startActivity(settings);
         */
		Intent launchPreferencesIntent = new Intent().setClass(this,
				BaikuPreferenceActivity.class);
		startActivityForResult(launchPreferencesIntent,
				REQUEST_CODE_PREFERENCES);
	}
	
	protected void handlePreferences() {
		boolean isWapsiteTested = BaikuApplication.mPref.getBoolean(
				Preferences.WAPSITE_KEY, false);
		
		boolean isOldWapsiteTested = BaikuApplication.mApi.getWapsite();
		
		String hint = (isWapsiteTested) ? "unline" : "online";
		Log.d(TAG, String.format("wapsite connected site: %s", hint));
		BaikuApplication.mApi.setWapsite(isWapsiteTested);
		
		// changed the wapsite connect point, need to reload wapsite.
		if (isWapsiteTested != isOldWapsiteTested) {
			Intent intent = new Intent(this, BaikuWapActivity.class);
			intent.putExtra("url", BaikuApplication.mApi.getWapProfileUrl());
			startActivity(intent);
			finish();
		}
	}
}