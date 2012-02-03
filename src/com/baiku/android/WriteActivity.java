package com.baiku.android;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;

public class WriteActivity extends Activity {
	private static final String LAUNCH_ACTION = "com.baiku.android.WRITE";
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.write);
	}
	
	public static Intent createIntent() {
		Intent intent = new Intent(LAUNCH_ACTION);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		return intent;
	}
}