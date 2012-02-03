package com.baiku.android;  
  
import java.io.IOException;  
  
import android.content.Context;  
import android.media.MediaRecorder;  
import android.util.Log;  
import android.view.SurfaceHolder;  
import android.view.SurfaceView;  

public class VideoSurface extends SurfaceView implements SurfaceHolder.Callback {  
	private static final String TAG = "VideoSurface";
	
    MediaRecorder m_mediaRecorder;  
    SurfaceHolder m_sufaceHolder;  
    
    boolean m_bNowRecording = false;
    private String m_audioFilePath = "";
  
    public VideoSurface(Context context) {  
        super(context);  
        
        m_audioFilePath = "/sdcard/baiku_video001.3gp";
        
		m_sufaceHolder = getHolder();
		m_sufaceHolder.addCallback(this);
		m_sufaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
  
		initialize();
    }  
    
    private void initialize() {
        m_mediaRecorder = new MediaRecorder();  
        m_mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);  
        m_mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);  
        
        m_mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP); 
        
        m_mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);  
        m_mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
        
        m_mediaRecorder.setOutputFile(m_audioFilePath);
        
        m_mediaRecorder.setVideoSize(240, 320);  
        m_mediaRecorder.setVideoFrameRate(15);   	
    }
   
    public void surfaceCreated(SurfaceHolder holder) {
    	Log.i(TAG, "surfaceCreate");
        m_mediaRecorder.setPreviewDisplay(m_sufaceHolder.getSurface());  
        if (m_mediaRecorder != null) {  
            try {  
                m_mediaRecorder.prepare();  
            } catch (IllegalStateException e) {  
                Log.d("yeongeon", "==[A]====>" + e.toString());  
            } catch (IOException e) {  
                Log.d("yeongeon", "==[B]====>" + e.toString());  
            }  
        }  
    }  
  
    public void surfaceDestroyed(SurfaceHolder holder) { 
    	Log.i(TAG, "surfaceDestroyed");
    	if (null != m_mediaRecorder) {
    		m_mediaRecorder.reset();
    		m_mediaRecorder.release();
    		m_mediaRecorder = null;
    	}
    }  
 
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    	Log.i(TAG, "surfaceChanged");
    }  
 
    public void start() { 
    	if (null != m_mediaRecorder) {
	        m_mediaRecorder.start();  
	        m_bNowRecording = true;  
    	}
    }
  
    public void stop() {  
    	if (null != m_mediaRecorder) {
	        try {  
	            m_mediaRecorder.stop(); 
	        } catch (IllegalStateException e) {  
	            Log.d("yeongeon", "==[G]====>" + e.toString());
	        }
	        m_mediaRecorder.reset();
	        m_mediaRecorder.release();  
	        m_mediaRecorder = null; 
    	}
        m_bNowRecording = false;  
    }
    
    public String getVideoFilePath() {
    	return m_audioFilePath;
    }
}  