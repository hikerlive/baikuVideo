package com.baiku.android;

import com.baiku.android.app.Preferences;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;

public class BaikuPreferenceActivity extends PreferenceActivity implements 
		SharedPreferences.OnSharedPreferenceChangeListener {
	
	private static final String TAG = "BaikuPreferenceActivity";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setResult(RESULT_OK);
		addPreferencesFromResource(R.xml.preferences);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		// Set up a listener whenever a key changes
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		
		Log.d(TAG, "key " + key);
		if (key.equalsIgnoreCase(Preferences.WAPSITE_KEY)) {
			boolean wapsite = BaikuApplication.mPref.getBoolean(Preferences.WAPSITE_KEY, false);
			String hint = wapsite ? "unline" : "online";
			Log.d(TAG, "wapsite" + hint);
		}
	}
}
