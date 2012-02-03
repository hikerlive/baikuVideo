package com.baiku.android;

import java.io.FileOutputStream;

import com.baiku.android.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageButton;

public class CameraActivity extends Activity implements CameraCallback {
	private FrameLayout cameraholder = null;
	private CameraSurface camerasurface = null;
	private String mPhotoFilePath = "";
	private static final String LAUNCH_ACTION = "com.baiku.android.CAMERA";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera);

		cameraholder = (FrameLayout) findViewById(R.id.camera_preview);

		setupPictureMode();

		((ImageButton) findViewById(R.id.takepicture))
				.setOnClickListener(onButtonClick);
		((ImageButton) findViewById(R.id.about))
				.setOnClickListener(onButtonClick);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent data = new Intent();
			Bundle extras = new Bundle();
			extras.putInt("type", 2);
			extras.putString("file", mPhotoFilePath);
			data.putExtras(extras);
			setResult(RESULT_OK, data);
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	private void setupPictureMode() {
		camerasurface = new CameraSurface(this);

		cameraholder.addView(camerasurface, new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

		camerasurface.setCallback(this);
	}

	public void onJpegPictureTaken(byte[] data, Camera camera) {
		try {
			/*
			 * FileOutputStream outStream = new FileOutputStream(String.format(
			 * "/sdcard/baiku/%d.jpg", System.currentTimeMillis()));
			 */

			FileOutputStream outStream = new FileOutputStream(
					"/sdcard/baiku_pic001.jpg");
			outStream.write(data);
			outStream.close();
			mPhotoFilePath = "/sdcard/baiku_pic001.jpg";
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		camerasurface.startPreview();
	}

	public void onPreviewFrame(byte[] data, Camera camera) {
	}

	public void onRawPictureTaken(byte[] data, Camera camera) {
	}

	public void onShutter() {
	}

	public String onGetVideoFilename() {
		// String filename =
		// String.format("/sdcard/baiku/%d.3gp",System.currentTimeMillis());
		String filename = String.format("/sdcard/baiku/video001.3gp",
				System.currentTimeMillis());

		return filename;
	}

	private void displayAboutDialog() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle(getString(R.string.app_name));
		builder.setMessage("Sample application to demonstrate the use of Camera in Android\n\nVisit www.krvarma.com for more information.");

		builder.show();
	}

	private View.OnClickListener onButtonClick = new View.OnClickListener() {
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.about: {
				// displayAboutDialog();
				Intent intentData = new Intent();
				Bundle extras = new Bundle();
				extras.putInt("type", 2);
				extras.putString("file", mPhotoFilePath);
				intentData.putExtras(extras);
				setResult(RESULT_OK, intentData);
				finish();
				break;
			}
			case R.id.takepicture: {
				camerasurface.startTakePicture();
				break;
			}
			}
		}
	};

	public static Intent createIntent() {
		Intent intent = new Intent(LAUNCH_ACTION);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		return intent;
	}

}