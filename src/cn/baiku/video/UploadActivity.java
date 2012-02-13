package cn.baiku.video;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.MediaColumns;
import android.provider.MediaStore.Video;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import cn.baiku.video.app.ImageManager;
import cn.baiku.video.task.GenericTask;
import cn.baiku.video.task.TaskAdapter;
import cn.baiku.video.task.TaskFeedback;
import cn.baiku.video.task.TaskListener;
import cn.baiku.video.task.TaskParams;
import cn.baiku.video.task.TaskResult;

public class UploadActivity extends BaseActivity{
	private static final String TAG = "UploadWrapActivity";
	private static final String LAUNCH_ACTION = "cn.baiku.video.UPLOAD";
	private static final String EXTRA_TYPE = "type";
	public static final String SOURCE_VIDEO = "video";
	public static final String SOURCE_AUDIO = "audio";
	public static final String SOURCE_PHOTO = "photo";
	public static final String SOURCE_SELF = "self";
	private static final String SIS_RUNNING_KEY = "running";
	
	private static final int REQUEST_LOCAL_VIDEO = 1;
	private static final int REQUEST_LOCAL_PHOTO = 2;
	
	private Uri mUri = null;
	private String mType = "";
	private long startTime = -1;
	private long endTime = -1;
	
	private GenericTask mUploadTask = null;
	
	// ui-elements
	private TextView mLocalVideo;
	private TextView mLocalPhoto;
	private EditText mEditTitle;
	private EditText mEditTag1;
	private EditText mEditTag2;
	private EditText mEditTag3;
	private Button mBtnUpload;
	private ImageView mImagePreview;
	private ImageView mImageDelete;
	
	@Override
	protected boolean _onCreate(Bundle savedInstanceState) {
		if (!super._onCreate(savedInstanceState)) {
			return false;
		}
		setContentView(R.layout.upload);
		
		mLocalVideo = (TextView)findViewById(R.id.upload_local_video);
		mLocalPhoto = (TextView)findViewById(R.id.upload_local_photo);
		mEditTitle = (EditText)findViewById(R.id.upload_title_edit);
		// mEditDescription = (EditText)findViewById(R.id.upload_desc_edit);
		mEditTag1 = (EditText)findViewById(R.id.upload_tag_edit1);
		mEditTag2 = (EditText)findViewById(R.id.upload_tag_edit2);
		mEditTag3 = (EditText)findViewById(R.id.upload_tag_edit3);
		
		mBtnUpload = (Button)findViewById(R.id.upload_transfer);
		mBtnUpload.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				doUpload();
			}
		});
		mImagePreview = (ImageView)findViewById(R.id.preview);		
		mImageDelete = (ImageView)findViewById(R.id.image_delete);
		mImageDelete.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Log.i(TAG, "pick image delete");
				Intent intent = getIntent();
				intent.setAction(null);
				resetActivityStatus();
			}
		});
		
		mLocalVideo.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Log.i(TAG, "local video pick");
				Intent intent = new Intent();
				intent.setType("video/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(intent, REQUEST_LOCAL_VIDEO);
			}
		});
		
		mLocalPhoto.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Log.i(TAG, "local photo pick");
				Intent intent = new Intent();
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(intent, REQUEST_LOCAL_PHOTO);
			}
		});
		
		/**
		 * 处理外部传入的调用参数，如分享接口参数。
		 */
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (null != extras) {
			String type = intent.getType();
			if (type.indexOf("image/") != -1) {
				mType = SOURCE_PHOTO;
				mUri = (Uri)extras.get(Intent.EXTRA_STREAM);
				setDisplayParam(mUri);
			} 
			else if (type.indexOf("video/") != -1) {
				mType = SOURCE_VIDEO;
				mUri = (Uri)extras.get(Intent.EXTRA_STREAM);
				setDisplayParam(mUri);
			}
			else if (type.indexOf("text/") != -1) {
				mType = SOURCE_SELF;
				mUri = null;
				String title = extras.getString(Intent.EXTRA_TEXT);
				setDisplayParam(title);
			}
		}
		
		return true;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "onResume");
		
		try {
			mType = BaikuApplication.getImageType();
			mUri = BaikuApplication.getImageUri();
			if (mUri != null) {
				setDisplayParam(mUri); 
				BaikuApplication.setImage("", null);
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
			e.printStackTrace();
		}
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	Log.d(TAG, "onActivityResult()");
    	try {
    		if (resultCode == Activity.RESULT_OK) {
    			if (requestCode == REQUEST_LOCAL_VIDEO) {
	    			// mUri = data.getData();
	    			// mType = SOURCE_VIDEO;
	    			// setDisplayParam(mUri);
	    			BaikuApplication.setImage(SOURCE_VIDEO, data.getData());
    			} else if (requestCode == REQUEST_LOCAL_PHOTO) {
    				// mUri = data.getData();
    				// mType = SOURCE_PHOTO;
    				// setDisplayParam(mUri);
    				BaikuApplication.setImage(SOURCE_PHOTO, data.getData());
    			}
    		} 
    	} catch (Exception e) {
    		Log.e(TAG, "requestCode: " + requestCode);
    		e.printStackTrace();
    	}
    }
	
	public static Intent createIntent(String sourceType, Uri uri) {
		Intent intent = new Intent(LAUNCH_ACTION);
		intent.putExtra(EXTRA_TYPE, sourceType);
		intent.putExtra(Intent.EXTRA_STREAM, uri);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		return intent;
	}
	
	protected void doUpload() {
		Log.d(TAG, "doUpload ...");
		startTime = System.currentTimeMillis();
		
		String title = mEditTitle.getText().toString();
		String tag1 = mEditTag1.getText().toString();
		String tag2 = mEditTag2.getText().toString();
		String tag3 = mEditTag3.getText().toString();
		String tags = tag1 + "," + tag2 + "," + tag3;
		
		String path = "";
		if (null != mUri) {
			if (mUri.getScheme().equals("content")) {
				path = getRealPathFromURI(mUri);
			} else {
				path = mUri.getPath();
			}
		}
		
		if (TextUtils.isEmpty(title)) {
			updateProgress(getString(R.string.upload_status_null_title));
			return;			
		}
	
		if (mUploadTask == null || mUploadTask.getStatus() != GenericTask.Status.RUNNING) {
			Log.d(TAG, "subject:" + title + " tags:" + tags + " type:" + mType + " path:" + path);
			mUploadTask = new UploadTask();
			mUploadTask.setListener(mUploadTaskListener);
			
			TaskParams params = new TaskParams();
			params.put("subject", title);
			params.put("tags", tags);
			params.put("type", mType);
			params.put("path", path);
			mUploadTask.execute(params);
		}
	}

	private void resetActivityStatus() {
		mImagePreview.setVisibility(View.INVISIBLE);
		mImageDelete.setVisibility(View.INVISIBLE);
		
		mEditTitle.setText("");
		// mEditDescription.setText("");
		mEditTag1.setText("");
		mEditTag2.setText("");
		mEditTag3.setText("");
		
		mUri = null;
		mType = "";
		
		startTime = -1;
		endTime = -1;
	}
	
	private void setDisplayParam(Uri uri) {
	    	if (null == uri) {
	    		Log.e(TAG, "uri is null...");
	    		return;
	    	}
	    	
	    	String path = "";
	    	if (uri.getScheme().equals("content")) {
	    		path = getRealPathFromURI(uri);
	    	} else {
	    		path = uri.getPath();
	    	}
	    	
	    	if (TextUtils.isEmpty(path)) {
	    		return;
	    	}
	    	
	    	// 设置编辑框
	    	String strTitle="", strDesc="";
	    	int idx = path.lastIndexOf('/');
	    	if (idx != -1) {
	    		strDesc = path.substring(idx+1);
	    		int idx2 = strDesc.lastIndexOf('.');
	    		if (idx2 != -1) {
	    			strTitle = strDesc.substring(0, idx2);
	    		}
	    	}
	    	if (!TextUtils.isEmpty(strTitle)) {
	    		// mEditTitle.setText(strTitle);
	    	}
	    	if (!TextUtils.isEmpty(strDesc)) {
	    		// mEditDescription.setText(strDesc);
	    	}
	    	mEditTag1.setText("原创");
	    	mEditTag2.setText("");
	    	mEditTag3.setText("");

	    	// 设置图片
	    	if (mType.equalsIgnoreCase("photo")) {
		    	mImagePreview.setVisibility(View.VISIBLE);
		    	mImageDelete.setVisibility(View.VISIBLE);
		    	mImagePreview.setImageBitmap(createThumbnailBitmap(mUri, 400));
	    	} else if (mType.equalsIgnoreCase("video")) {
		    	mImagePreview.setVisibility(View.VISIBLE);
		    	mImageDelete.setVisibility(View.VISIBLE);
		    	mImagePreview.setImageBitmap(createVideoThumbnail(path));
	    	} else {
	    		mImagePreview.setVisibility(View.INVISIBLE);
	    		mImageDelete.setVisibility(View.INVISIBLE);
	    	}
	    }
	    
	private void setDisplayParam(String title) {
    	if (title != null) {
    		mEditTitle.setText(title);
    	}
	}
	
    private Bitmap createThumbnailBitmap(Uri uri, int size) {
    	InputStream input = null;
    	try {
    		input = getContentResolver().openInputStream(uri);
    		BitmapFactory.Options options = new BitmapFactory.Options();
    		options.inJustDecodeBounds = true;
    		BitmapFactory.decodeStream(input, null, options);
    		input.close();
    		
    		// Compute the scale
    		int scale = 1;
    		while ((options.outWidth /scale > size)
    				|| (options.outHeight / scale > size)) {
    			scale *=2;
    		}
    		
    		options.inJustDecodeBounds = false;
    		options.inSampleSize = scale;
    		
    		input = getContentResolver().openInputStream(uri);
    		return BitmapFactory.decodeStream(input, null, options);
    	} catch (IOException e) {
    		Log.w(TAG, e);
    		return null;
    	} finally {
    		if (input != null) {
    			try {
    				input.close();
    			} catch (IOException e) {
    				Log.w(TAG, e);
    			}
    		}
    	}
    }
    
    private Bitmap createVideoThumbnail(String path) {
    	/**
    	 * Android 2.2版本后可以直接通过ThumbnailUtils来获得视频缩略图
    	 * 但是之前版本仍会有问题。
    	 */
    	Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(path, Video.Thumbnails.MICRO_KIND);
    	return bitmap;
    }
    
	private String getRealPathFromURI(Uri contentUri) {
		if (null == contentUri) {
			return "";
		}
		String[] proj = { MediaColumns.DATA };
		Cursor cursor = managedQuery(contentUri, proj, null, null, null);
		int column_index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

	private void updateProgress(String progress) {
		Toast.makeText(getApplicationContext(), progress, Toast.LENGTH_SHORT).show();
	}
	
	private void enableEntry() {
		mBtnUpload.setEnabled(true);
	}
	
	private void disableEntry() {
		mBtnUpload.setEnabled(false);
	}
	
	private ImageManager getImageManager() {
		return BaikuApplication.mImageLoader.getImageManager();
	}
	
	private class UploadTask extends GenericTask {
		@Override
		protected TaskResult _doInBackground(TaskParams... params) {
			TaskParams param = params[0];
			try {
				String subject = param.getString("subject");
				String tags = param.getString("tags");
				String type = param.getString("type");
				String path = param.getString("path");
				File file = null;
				
				if (mType.equalsIgnoreCase(SOURCE_PHOTO)) {
					if (!TextUtils.isEmpty(path)) {
						file = new File(path);
						try {
							if (path.endsWith(".jpg") || path.endsWith(".png")) {
								file = getImageManager().compressImage(file, 100);
							}
						} catch (IOException ioe) {
							Log.e(TAG, "Can't compress images.");
						}
					}
				}
				else if (mType.equalsIgnoreCase(SOURCE_VIDEO)) {
					if (!TextUtils.isEmpty(path)) {
						file = new File(path);
					}
				}
				else if (mType.equalsIgnoreCase(SOURCE_AUDIO)) {
					if (!TextUtils.isEmpty(path)) {
						file = new File(path);
					}					
				}
				else if (mType.equalsIgnoreCase(SOURCE_SELF)) {
						
				}
				getApi().uploadStatus(subject, tags, type, file);
			} catch (Exception e) {
				Log.e(TAG, e.getMessage(), e);
				return TaskResult.IO_ERROR;
			}
			return TaskResult.OK;
		}
	}
	
	private TaskListener mUploadTaskListener = new TaskAdapter() {
		@Override
		public void onPreExecute(GenericTask task) {
			onUploadBegin();
		}
		
		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			endTime = System.currentTimeMillis();
			Log.d("LDS", "Sended a status in " + (endTime - startTime));
			
			if (result == TaskResult.AUTH_ERROR) {
				// logout();
			} else if (result == TaskResult.OK) {
				onUploadSuccess();
			} else if (result == TaskResult.IO_ERROR) {
				onUploadFailure();
			}			
		}
		
		@Override
		public String getName() {
			return "UploadTask";
		}
	};	

	private void onUploadBegin() {
		disableEntry();
		TaskFeedback.getInstance(TaskFeedback.DIALOG_MODE, UploadActivity.this)
			.start(getString(R.string.upload_status_uploading_in));
	}
	
	private void onUploadSuccess() {
		Log.d(TAG, "onUploadSuccess");
		TaskFeedback.getInstance(TaskFeedback.DIALOG_MODE, UploadActivity.this)
			.success();
		
		enableEntry();
		
		BaikuApplication.setImage("", null);
		resetActivityStatus();
		
		// TODO goto refresh home page
		Intent intent = new Intent();
		intent.setAction(TabHostActivity.TAB_CHANGED_ACTION);
		intent.putExtra("tag1", TabHostActivity.TAB_TAG_UPLOAD);
		intent.putExtra("tag2", TabHostActivity.TAB_TAG_HOME);
		sendBroadcast(intent);
	}
	
	private void onUploadFailure() {
		TaskFeedback.getInstance(TaskFeedback.DIALOG_MODE, UploadActivity.this)
		.failed(getString(R.string.upload_status_failure));
		enableEntry();
	}	
}
