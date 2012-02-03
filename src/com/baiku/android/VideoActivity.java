package com.baiku.android;

import com.baiku.android.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class VideoActivity extends Activity { 
	private static final String TAG = "VideoActivity";
	private static final String LAUNCH_ACTION = "com.baiku.android.VIDEO";

	// view
	VideoSurface m_preview = null;
	LinearLayout m_previewLayout = null;  
    ImageButton m_btnStart = null;  
    ImageButton m_btnStop = null; 

    @Override  
    protected void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.video);  
  
        Log.i(TAG, "start ....");
        m_preview = new VideoSurface(this);  
        m_previewLayout = (LinearLayout) findViewById(R.id.preview);  
        m_previewLayout.addView(m_preview);  
  
        m_btnStart = (ImageButton) this.findViewById(R.id.btn_start);  
        m_btnStop = (ImageButton) this.findViewById(R.id.btn_stop);
        m_btnStop.setEnabled(false);
          
        // Add ClickListeners  
        m_btnStart.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				onRecStart(); 
			}
        });  
        m_btnStop.setOnClickListener(new View.OnClickListener() {  
            public void onClick(View v) {  
                onRecStop();  
            }  
        }); 
        Log.i(TAG, "end ....");
    } 
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent data = new Intent();
            Bundle extras = new Bundle();
            extras.putInt("type", 0);
            extras.putString("file", m_preview.getVideoFilePath());
            data.putExtras(extras);
            setResult(RESULT_OK, data);
            VideoActivity.this.finish();
    	}
    	return super.onKeyDown(keyCode, event);
    }
        
    private void onRecStart(){  
        m_preview.start();  
        m_btnStart.setEnabled(false);  
        m_btnStop.setEnabled(true);
    }  
      
    private void onRecStop(){  
        m_preview.stop();  
        m_btnStart.setEnabled(true);  
        m_btnStop.setEnabled(false);
    }
    
	public static Intent createIntent() {
		Intent intent = new Intent(LAUNCH_ACTION);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		return intent;
	}
}