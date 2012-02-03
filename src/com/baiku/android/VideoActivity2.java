package com.baiku.android;
 
import com.baiku.android.R;

import android.app.Activity;
import android.media.MediaRecorder;
import android.media.MediaRecorder.AudioSource;
import android.media.MediaRecorder.OutputFormat;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
 
public class VideoActivity2 extends Activity {
	Button start,stop;
	MediaRecorder mr;
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video2);
        start=(Button)findViewById(R.id.start);
        stop=(Button)findViewById(R.id.stop);
 
 
        start.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				// TODO Auto-generated method stub
		        mr=new MediaRecorder();
		        mr.setAudioSource(AudioSource.MIC);
		        //设置音源,这里是来自麦克风,虽然有VOICE_CALL,但经真机测试,不行
		        mr.setOutputFormat(OutputFormat.RAW_AMR);
		        //输出格式
		        mr.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		        //编码
		        mr.setOutputFile("/sdcard/1.amr");
		        //输出文件路径,貌似文件必须是不存在的,不会自己清空
				try{
					mr.prepare();
					//做些准备工作
					mr.start();
					//开始
				}catch(Exception e){
					e.printStackTrace();
				}
			}});
        stop.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mr.stop();
				//停止
				mr.release();
				//释放
			}});
 
    }
}