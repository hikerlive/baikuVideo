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

public class VideoActivity3 extends Activity implements SurfaceHolder.Callback{
	
	// Button myButton;
	ImageButton buttonStart;
	ImageButton buttonStop;
	MediaRecorder mediaRecorder;
	SurfaceHolder surfaceHolder;
	boolean recording;
	
	private static final String LAUNCH_ACTION = "com.baiku.android.VIDEO3";
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        recording = false;
        
        mediaRecorder = new MediaRecorder();
        initMediaRecorder();
        
        setContentView(R.layout.video3);
        
        SurfaceView myVideoView = (SurfaceView)findViewById(R.id.videoview);
        surfaceHolder = myVideoView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        
        /*
        myButton = (Button)findViewById(R.id.mybutton);
        myButton.setOnClickListener(myButtonOnClickListener);
        */
        buttonStart = (ImageButton)findViewById(R.id.btn_start);
        buttonStop = (ImageButton)findViewById(R.id.btn_stop);
        buttonStop.setEnabled(false);
        buttonStart.setOnClickListener(myButtonOnClickListener);
        buttonStop.setOnClickListener(myButtonOnClickListener);
        
    }
    
    private ImageButton.OnClickListener myButtonOnClickListener = new ImageButton.OnClickListener(){
		public void onClick(View arg0) {
			/*
			// TODO Auto-generated method stub
			if(recording){
				mediaRecorder.stop();
				mediaRecorder.release();
	            Intent data = new Intent();
	            Bundle extras = new Bundle();
	            extras.putInt("type", 0);
	            extras.putString("file", getVideoFilePath());
	            data.putExtras(extras);
	            setResult(RESULT_OK, data);				
				finish();
			}else{
				mediaRecorder.start();
				recording = true;
				myButton.setText("停止");
			}
			*/
			int id = arg0.getId();
			if (id == R.id.btn_start) {
				mediaRecorder.start();
				recording = true;
				buttonStart.setEnabled(false);
				buttonStop.setEnabled(true);
				
			} else if (id == R.id.btn_stop){
				mediaRecorder.stop();
				mediaRecorder.release();
	            Intent data = new Intent();
	            Bundle extras = new Bundle();
	            extras.putInt("type", 0);
	            extras.putString("file", getVideoFilePath());
	            data.putExtras(extras);
	            setResult(RESULT_OK, data);				
				finish();
			}
		}};
    
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		
	}
	public void surfaceCreated(SurfaceHolder arg0) {
		prepareMediaRecorder();
	}
	public void surfaceDestroyed(SurfaceHolder arg0) {

	}
	
	private void initMediaRecorder(){
		mediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
        CamcorderProfile camcorderProfile_HQ = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        mediaRecorder.setProfile(camcorderProfile_HQ);
        mediaRecorder.setOutputFile("/sdcard/baiku_video002.mp4");
        mediaRecorder.setMaxDuration(60000); // Set max duration 60 sec.
        mediaRecorder.setMaxFileSize(5000000); // Set max file size 5M
	}
	
	private void prepareMediaRecorder(){
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
	
	public String getVideoFilePath() {
		return "/sdcard/baiku_video002.mp4";
	}
	
	public static Intent createIntent() {
		Intent intent = new Intent(LAUNCH_ACTION);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		return intent;
	}
}