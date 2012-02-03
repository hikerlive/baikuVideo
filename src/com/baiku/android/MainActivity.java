package com.baiku.android;

import java.io.File;
import java.io.UnsupportedEncodingException;

import com.baiku.android.R;
import com.baiku.android.http.CustomHttpClient;
import com.baiku.android.http.UploadListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

public class MainActivity extends Activity {
	private static final String TAG = "MainActivity";
	
	private String mVideoFilePath="";
	private String mAudioFilePath="";
	private String mPhotoFilePath="";
	private int currentFormat = 0;
	
	private EditText mUsernameEdit;
	private EditText mPasswordEdit;
	private EditText mSubjectEdit;
	
	private Button mTakeVideo;
	private Button mTakeAudio;
	private Button mTakePhoto;
	private Button mTakeShare;
	private ProgressBar mProgressBar;
	
	private String mUsername = "";
	private String mPassword = "";
	private String mSubject = "";
	
    private UploadListener mUploadListener = new UploadAdapter();
    private class UploadAdapter implements UploadListener {
    	public void onUploadBegin() {
    		//mProgressBar.setVisibility(0);
    		//mProgressBar.setProgress(0);
    	}
    	public void onUploadProgress(long progress) {
    		Log.i(TAG, "progress:" + progress);
    		//mProgressBar.setProgress((int) progress);
    	}
    	public void onUploadFinish() {
    		//mProgressBar.setProgress(100);
    		showMessage("上传文件完成");
    	}
    	public void onUploadFailed(int statusCode) {
    		showMessage("上传文件失败,statuscode:" + statusCode);
    	}
    }
	
	private View.OnClickListener mButtonClick = new View.OnClickListener() {
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.take_video: {
					Intent intent = VideoActivity3.createIntent();
					startActivityForResult(intent, 0);
				}break;
				case R.id.take_audio:{
					Intent intent = AudioActivity.createIntent();
					startActivityForResult(intent, 0);
				}break;
				case R.id.take_photo:{
					Intent intent = CameraActivity.createIntent();
					startActivityForResult(intent, 0);
				}break;
				case R.id.take_share:{
					onClickShareFile();
				}break;
			}
		}
	};
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mUsernameEdit = (EditText)findViewById(R.id.username_edit);
        mPasswordEdit = (EditText)findViewById(R.id.password_edit);
        mSubjectEdit =(EditText)findViewById(R.id.subject_edit);
        
        mTakeVideo = (Button)findViewById(R.id.take_video);
        mTakeAudio = (Button)findViewById(R.id.take_audio);
        mTakePhoto = (Button)findViewById(R.id.take_photo);
        mTakeShare = (Button)findViewById(R.id.take_share);
        mProgressBar = (ProgressBar)findViewById(R.id.progressBar1);
        
        mTakeVideo.setOnClickListener(mButtonClick);
        mTakeAudio.setOnClickListener(mButtonClick);
        mTakePhoto.setOnClickListener(mButtonClick);
        mTakeShare.setOnClickListener(mButtonClick);
    }
    
    @Override
    public void onActivityResult(int reuqestCode, int resultCode, Intent data) {
    	if (resultCode == RESULT_OK) {
    		if (data != null) {
	    		Bundle bundle = data.getExtras();
	    		int fileType = bundle.getInt("type");
	    		String filePath = bundle.getString("file");
	    		if (fileType == 0) {
	    			mVideoFilePath = filePath;
	    		} else if (fileType == 1) {
	    			mAudioFilePath = filePath;
	    		} else if (fileType == 2) {
	    			mPhotoFilePath = filePath;
	    		}
    		}
    	}
    	super.onActivityResult(reuqestCode, resultCode, data);
    }
    
    private void onClickShareFile() {
    	mUsername = mUsernameEdit.getText().toString();
    	mPassword = mPasswordEdit.getText().toString();
    	mSubject = mSubjectEdit.getText().toString();
    	
    	if (TextUtils.isEmpty(mUsername)) {
    		showMessage("用户名不能为空,请输入.");
    		return;
    	}
    	
    	if (TextUtils.isEmpty(mPassword)) {
    		showMessage("密码不能为空,请输入.");
    		return;
    	}
    	
    	if (TextUtils.isEmpty(mSubject)) {
    		mSubject = "The message is from android platform phone!!!";
    	}
    	
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		String formats[] = {"视频", "音频", "图片"};
		
		builder.setTitle(getString(R.string.share_title))
			   .setSingleChoiceItems(formats, currentFormat, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					currentFormat = which;
					MainActivity.this.shareFile(which);
					dialog.dismiss();
				}
			   })
			   .show();    	
    }
    
    public void showMessage(String message) {
    	Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
    
    private void shareFile(int whichType) {
    	String filePath = "";
    	String fileType = "";
    	if (whichType == 0) {
    		filePath = mVideoFilePath;
    		fileType = "";
    	} else if (whichType == 1) {
    		filePath = mAudioFilePath;
    		fileType = "audio";
    	} else if (whichType == 2) {
    		filePath = mPhotoFilePath;
    		fileType = "";
    	}
    	
    	if (!TextUtils.isEmpty(filePath)) {
    		String url = "http://share.baiku.cn/share";
    		Log.i(TAG, "path=" + filePath + " type=" + fileType);
    		try {
    			File file = new File(filePath);
				CustomHttpClient.post(url, mUsername, mPassword, mSubject, "", fileType, file, mUploadListener);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
    	} else {
    		Toast.makeText(getApplicationContext(), "先做操作产生数据文件", Toast.LENGTH_SHORT).show();
    	}
    }
}