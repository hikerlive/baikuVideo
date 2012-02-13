package cn.baiku.video;

import java.io.File;
import java.io.IOException;

import android.content.Intent;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import cn.baiku.video.util.FileHelper;

public class AudioActivity extends BaseActivity{
	private static final String TAG = "AudioWrapActivity";
	
	private MediaRecorder recorder = null;
	private int currentFormat = 0;
	private int output_formats[] = { MediaRecorder.OutputFormat.MPEG_4, MediaRecorder.OutputFormat.THREE_GPP };
	private String file_exts[] = { ".mp4", ".3gp" }; 
	private String mAudioFilePath = "";
	
	private Button mBtnStart;
	private Button mBtnStop;
	
	@Override
	protected boolean _onCreate(Bundle savedInstanceState) {
		if (!super._onCreate(savedInstanceState)) {
			return false;
		}
		setContentView(R.layout.audio);
		Log.d(TAG, "onCreate()");
		
		mBtnStart = (Button)findViewById(R.id.btnStart);
		mBtnStop = (Button)findViewById(R.id.btnStop);
		mBtnStart.setOnClickListener(btnClick);
		mBtnStop.setOnClickListener(btnClick);
		return true;
	}
	
	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroy()");
		if (null != recorder) {
			recorder.stop();
			recorder.reset();
			recorder.release();
			recorder = null;
		}
		super.onDestroy();
	}
	
    private View.OnClickListener btnClick = new View.OnClickListener() {
		public void onClick(View v) {
			switch(v.getId()){
				case R.id.btnStart:{
					Log.d(TAG, "Start Recording");
					doStartRecord();
					break;
				}
				case R.id.btnStop:{
					Log.d(TAG, "Stop Recording");
					doStopRecord();
					break;
				}
			}
		}
	};
	
	private void doStartRecord() {
		try {
			mAudioFilePath = FileHelper.getBasePath() + "audio001." + file_exts[currentFormat];
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		recorder = new MediaRecorder();
		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		recorder.setOutputFormat(output_formats[currentFormat]);
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		recorder.setOutputFile(mAudioFilePath);
		recorder.setOnErrorListener(errorListener);
		recorder.setOnInfoListener(infoListener);
		
		try {
			recorder.prepare();
			recorder.start();
			mBtnStart.setEnabled(false);
			mBtnStop.setEnabled(true);
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void doStopRecord() {
		if (null != recorder) {
			recorder.stop();
			recorder.reset();
			recorder.release();
			recorder = null;
			
			mBtnStart.setEnabled(true);
			mBtnStop.setEnabled(false);
			
			File file = new File(mAudioFilePath);
			if (file.exists()) {
				Uri uri = Uri.fromFile(file);
				BaikuApplication.setImage(UploadActivity.SOURCE_AUDIO, uri);

				// TODO goto upload activity.
				Intent intent = new Intent();
				intent.setAction(TabHostActivity.TAB_CHANGED_ACTION);
				intent.putExtra("tag1", TabHostActivity.TAB_TAG_AUDIO);
				intent.putExtra("tag2", TabHostActivity.TAB_TAG_UPLOAD);
				sendBroadcast(intent);
			}
		}
	}
	
	private MediaRecorder.OnErrorListener errorListener = new MediaRecorder.OnErrorListener() {
		public void onError(MediaRecorder mr, int what, int extra) {
			Log.i(TAG, "Error: " + what + ", " + extra);
		}
	};
	
	private MediaRecorder.OnInfoListener infoListener = new MediaRecorder.OnInfoListener() {
		public void onInfo(MediaRecorder mr, int what, int extra) {
			Log.i(TAG, "Warning: " + what + ", " + extra);
		}
	};
}
