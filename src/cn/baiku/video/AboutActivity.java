package cn.baiku.video;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class AboutActivity extends Activity {
	private static final String TAG = "AboutActivity";
	private static final String LAUNCH_ACTION = "cn.baiku.video.ABOUT";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
	}
	
	public static Intent createIntent() {
		Intent intent = new Intent(LAUNCH_ACTION);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		return intent;
	}
}