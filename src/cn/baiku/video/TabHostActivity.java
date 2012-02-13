package cn.baiku.video;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TabHost;
import cn.baiku.video.util.FileHelper;

public class TabHostActivity extends TabActivity implements OnCheckedChangeListener {
	private final static String TAG = "TabHostActivity";
	public final static String TAB_TAG_HOME = "tab_home";
	public final static String TAB_TAG_VIDEO = "tab_video";
	public final static String TAB_TAG_PHOTO = "tab_photo";
	public final static String TAB_TAG_AUDIO = "tab_audio";
	public final static String TAB_TAG_UPLOAD = "tab_upload";
	public final static String TAB_CHANGED_ACTION = "cn.baiku.video.TABCHANGED";
	
	private TabHost mTabHost;
	private RadioGroup mRadioGroup; 
	
	private Intent mWapIntent;
	private Intent mVideoIntent;
	private Intent mPhotoIntent;
	private Intent mAudioIntent;
	private Intent mUploadIntent;
	private static int mRefCount;

	public int[] mTabIds = {
			R.id.radio_button0,
			R.id.radio_button1,
			R.id.radio_button2,
			R.id.radio_button3,
			R.id.radio_button4,
	};
	
	private static final int TAKE_PHOTO=1;
	private static final int TAKE_VIDEO=2;
	private Uri mImageUri;
	private File mImageFile;
	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, "receive tab changed action...");
			Bundle bundle = intent.getExtras();
			if (null != bundle) {
				String tag1 = bundle.getString("tag1");
				String tag2 = bundle.getString("tag2");
				Log.d(TAG, "tag1: " + tag1 + " tag2: " + tag2);
				
				// 调用mRadioGroup.check()方法将触发onCheckedChanged()函数。
				if (tag2.equalsIgnoreCase(TAB_TAG_HOME)) {
					BaikuApplication.mbReloadUrl = true;
					mRadioGroup.check(R.id.radio_button0);
				} else if (tag2.equalsIgnoreCase(TAB_TAG_UPLOAD)) {
					mRadioGroup.check(R.id.radio_button4);
				} else {
					mTabHost.setCurrentTabByTag(tag2);
				}
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!BaikuApplication.mApi.isLoggedIn()) {
			Log.d(TAG, "Not logged in");
			Intent intent = new Intent(this, LoginActivity.class);
			intent.putExtra(Intent.EXTRA_INTENT, getIntent());
			startActivity(intent);
			finish();
			return;
		}
		
		setContentView(R.layout.main_frame);
		Log.d(TAG, "onCreate()");
		
		mRadioGroup = (RadioGroup)findViewById(R.id.main_radio);
		mRadioGroup.setOnCheckedChangeListener(this);
		
		mWapIntent = new Intent(this, WapActivity.class);
		mVideoIntent = mWapIntent;
		mPhotoIntent = mWapIntent;
		mAudioIntent = new Intent(this, AudioActivity.class);
		mUploadIntent = new Intent(this, UploadActivity.class);
		
		mTabHost = getTabHost();
		TabHost localTabHost = this.mTabHost;
		localTabHost.addTab(buildTabSpec(TAB_TAG_HOME, R.string.footer_btn_home, R.drawable.detail_home_selected, mWapIntent));
		localTabHost.addTab(buildTabSpec(TAB_TAG_VIDEO, R.string.footer_btn_video, R.drawable.detail_video_selected, mVideoIntent));
		localTabHost.addTab(buildTabSpec(TAB_TAG_PHOTO, R.string.footer_btn_photo, R.drawable.detail_photo_selected, mPhotoIntent));
		localTabHost.addTab(buildTabSpec(TAB_TAG_AUDIO, R.string.footer_btn_audio, R.drawable.detail_audio_selected, mAudioIntent));
		localTabHost.addTab(buildTabSpec(TAB_TAG_UPLOAD, R.string.footer_btn_upload, R.drawable.detail_upload_selected, mUploadIntent));
		mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
			public void onTabChanged(String tabId) {
				Log.d(TAG, "onTabChanged " + tabId);
				if (tabId.equalsIgnoreCase(TAB_TAG_PHOTO)) {
					doTakePhoto();
				} else if (tabId.equalsIgnoreCase(TAB_TAG_VIDEO)) {
					doTakeVideo();
				}
			}
		});
	}

	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch (checkedId) {
		case R.id.radio_button0:
			Log.d(TAG, "home");
			BaikuApplication.mbReloadUrl = true;
			this.mTabHost.setCurrentTabByTag(TAB_TAG_HOME);
			break;
		case R.id.radio_button1:
			Log.d(TAG, "video");
			this.mTabHost.setCurrentTabByTag(TAB_TAG_VIDEO);
			break;
		case R.id.radio_button2:
			Log.d(TAG, "photo");
			this.mTabHost.setCurrentTabByTag(TAB_TAG_PHOTO);
			break;
		case R.id.radio_button3:
			Log.d(TAG, "audio");
			this.mTabHost.setCurrentTabByTag(TAB_TAG_AUDIO);
			break;
		case R.id.radio_button4:
			Log.d(TAG, "upload");
			this.mTabHost.setCurrentTabByTag(TAB_TAG_UPLOAD);
			break;			
		}
	}
	
	@Override
	protected void onActivityResult(int requsetCode, int resultCode, Intent data) {
		super.onActivityResult(requsetCode, resultCode, data);
		Log.d(TAG, "onActivityResult()");
		try {
			if (resultCode == Activity.RESULT_OK) {
				if (requsetCode == TAKE_PHOTO) {
					Log.d(TAG, "photo uri" + mImageUri.getPath());
					BaikuApplication.setImage(UploadActivity.SOURCE_PHOTO, mImageUri);
					mRadioGroup.check(R.id.radio_button4);
					// this.mTabHost.setCurrentTabByTag(TAB_TAG_UPLOAD);
				} else if (requsetCode == TAKE_VIDEO) {
					Uri uri = data.getData();
					if (null != uri) {
						Log.d(TAG, "video uri" + getRealPathFromURI(uri));
						BaikuApplication.setImage( UploadActivity.SOURCE_VIDEO, uri);
						mRadioGroup.check(R.id.radio_button4);
						// this.mTabHost.setCurrentTabByTag(TAB_TAG_UPLOAD);
					} else {
						Log.d(TAG, "video uri is empty..");
					}
				}
			}
		} catch (Exception e) {
			Log.e(TAG, "requestCode: " + requsetCode);
			e.printStackTrace();
		}		
	}

	@Override
	protected void onResume() {
		Log.d(TAG, "onResume()");
		super.onResume();
		mRefCount ++;
		IntentFilter filter = new IntentFilter();
		filter.addAction(TAB_CHANGED_ACTION);
		registerReceiver(mBroadcastReceiver, filter);

	}
	
	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroy()");
		if (mRefCount > 0) {
			unregisterReceiver(mBroadcastReceiver);
			mRefCount --;
		}
		super.onDestroy();
	}
	
	private void doTakeVideo() {
		Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
		startActivityForResult(intent, TAKE_VIDEO);
	}
	
	private void doTakePhoto() {
		try {
			mImageFile = new File(FileHelper.getBasePath(), "upload.jpg");
			mImageUri = Uri.fromFile(mImageFile);
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
			startActivityForResult(intent, TAKE_PHOTO);	
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private TabHost.TabSpec buildTabSpec(String tag, int resLabel, int resIcon, final Intent content) {
		return mTabHost.newTabSpec(tag)
			.setIndicator(getString(resLabel),getResources().getDrawable(resIcon))
			.setContent(content);
	}
	
	private String getRealPathFromURI(Uri contentUri) {
		String[] proj = { MediaColumns.DATA };
		Cursor cursor = managedQuery(contentUri, proj, null, null, null);
		int column_index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}
}
	