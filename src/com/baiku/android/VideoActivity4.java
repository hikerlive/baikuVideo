package com.baiku.android;

import java.io.IOException;

import com.baiku.android.R;

import android.app.Activity;
import android.content.Intent;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class VideoActivity4 extends Activity implements SurfaceHolder.Callback{
	private MediaRecorder mediaRecorder;
	private SurfaceHolder surfaceHolder;
	private String audioFilePath = "/sdcard/baiku_video001.3gp";
	
	private ImageButton buttonStart;
	private ImageButton buttonStop;
	
	private static final String LAUNCH_ACTION = "com.baiku.android.VIDEO";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video);
		
		mediaRecorder = new MediaRecorder();  
		mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);  
		mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);  
        
		mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP); 
        
		mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);  
		mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
        
		mediaRecorder.setOutputFile(audioFilePath);
        
		mediaRecorder.setVideoSize(240, 320);  
		mediaRecorder.setVideoFrameRate(15);  
		
        SurfaceView myVideoView = (SurfaceView)findViewById(R.id.videoview);
        surfaceHolder = myVideoView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        
        buttonStart = (ImageButton)findViewById(R.id.btn_start);
        buttonStop = (ImageButton)findViewById(R.id.btn_stop);
        buttonStart.setOnClickListener(myButtonOnClickListener);
        buttonStop.setOnClickListener(myButtonOnClickListener);
        
        buttonStop.setEnabled(false);
	}
	
	private Button.OnClickListener myButtonOnClickListener = new Button.OnClickListener(){
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.btn_start:{
					mediaRecorder.start();
					buttonStop.setEnabled(true);
				}break;
				case R.id.btn_stop: {
					mediaRecorder.stop();
					mediaRecorder.release();
					VideoActivity4.this.finish();
				}
			}
		}
	};
	
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
	}
	public void surfaceCreated(SurfaceHolder arg0) {
		mediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());
		try {
			mediaRecorder.prepare();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void surfaceDestroyed(SurfaceHolder arg0) {
	}
	
	public Intent createIntent() {
		Intent intent = new Intent(LAUNCH_ACTION);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		return intent;		
	}
}