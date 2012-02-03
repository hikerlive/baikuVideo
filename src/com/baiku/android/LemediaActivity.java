package com.baiku.android;

import android.app.Activity;
import android.os.Bundle;

public class LemediaActivity extends Activity {
	private static final String TAG = "LemediaActivity";
	private static final String LAUNCH_ACTION = "com.baiku.android.LEMEDIA";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lemedia);
	}
}