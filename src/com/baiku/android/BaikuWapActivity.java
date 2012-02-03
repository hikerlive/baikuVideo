package com.baiku.android;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.baiku.android.update.UpdateAdapter;
import com.baiku.android.update.UpdateListener;
import com.baiku.android.util.FileHelper;

public class BaikuWapActivity extends BaseActivity {
	private static final String TAG = "BaikuWapActivity";
	private static final String LAUNCH_ACTION = "com.baiku.android.BAIKUWAP";

	private static final int TAKE_PHOTO=1;
	private static final int TAKE_VIDEO=2;
	
	private WebView mWebView;
	private ProgressDialog mProgressDialog = null;
	
	// 更新所用
	private String mUpdateUrl = "";
	
	// 拍照所用
	private Uri mImageUri;
	private File mImageFile;
		
	@Override
	protected boolean _onCreate(Bundle savedInstanceState) {
		if (!super._onCreate(savedInstanceState)) {
			return false;
		}
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.baikuwap);
		
		String url = "";
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {
			url = extras.getString("url");
			Log.d(TAG, "receive a special url page: " + url);
		}
		
		/**
		 * URL有默认跳转，则是新弹出一个浏览器实例显示，不会在Activity内容页显示。
		 * 1.webview响应页面内的超链接，而不至于新弹出一个浏览器实例。
		 * 2.页面能够调用javascript代码。
		 */
		mWebView = (WebView)findViewById(R.id.baiku_wap_view);
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.requestFocus();
		mWebView.setWebViewClient(new BaikuWebViewClient());
		mWebView.setWebChromeClient(new BaikuWebChromeClient());
		mWebView.setDownloadListener(new BaikuWebDownloadListener());
		mWebView.loadUrl(((TextUtils.isEmpty(url)) ? BaikuApplication.mApi.getWapFirstUrl() : url));
		
		TextView footer_btn_home = (TextView)findViewById(R.id.footer_btn_home);
		TextView footer_btn_video = (TextView)findViewById(R.id.footer_btn_video);
		TextView footer_btn_photo = (TextView)findViewById(R.id.footer_btn_photo);
		TextView footer_btn_audio = (TextView)findViewById(R.id.footer_btn_audio);
		TextView footer_btn_upload = (TextView)findViewById(R.id.footer_btn_upload);

		footer_btn_home.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Log.i(TAG, "home click");
				mWebView.loadUrl(BaikuApplication.mApi.getWapProfileUrl());
			}
		});		
		
		footer_btn_video.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Log.i(TAG, "video click");
				Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
				startActivityForResult(intent, TAKE_VIDEO);
			}
		});
		
		footer_btn_photo.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				try {					
					Log.i(TAG, "photo click");
					mImageFile = new File(FileHelper.getBasePath(), "upload.jpg");
					mImageUri = Uri.fromFile(mImageFile);
					Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
					startActivityForResult(intent, TAKE_PHOTO);
				} catch (Exception e) {
					Log.e(TAG, e.getMessage());
				}
			}
		});
		
		footer_btn_audio.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Log.i(TAG, "audio click");
				// Intent intent = ContactActivity.createIntent();
				// startActivity(intent);
			}
		});
		
		footer_btn_upload.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = UploadActivity.createIntent();
				startActivity(intent);
			}
		});
		
		return true;
	}
	
	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroy");
		if (null != mProgressDialog) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
		super.onDestroy();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		/**
		 * 用webview点链接看了很多页以后为了让WebView支持回退功能，需要覆盖Activity类的
		 * onKeyDown()方法，如果不做任何处理，点击系统回退键，整个浏览器会调用finish()
		 * 而结束自身，而不会返回到上一页面。
		 */
		if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
			/*
			if (!mUrlStack.isEmpty()) {
				UrlPosition urlpos = mUrlStack.getFirst();
				if (null != urlpos) {
					mScrollX = urlpos.x;
					mScrollY = urlpos.y;
					mUrlStack.removeFirst();
					Log.d(TAG, String.format("pop scroll x:%d, y:%d", urlpos.x, urlpos.y));
				}
			} else {
				mScrollX = 0;
				mScrollY = 0;
				Log.d(TAG, "stack top, default value");
			}
			mIsUrlBacked = true;
			*/
			
			/** goBack() 表示返回WebView的上一页面 */
			mWebView.goBack();
			Log.i(TAG, "webview go back...");
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}
	
	private void onLoadStarted() {
		if (null == mProgressDialog) {
			Log.d(TAG, "progress dialog show by started...");
			mProgressDialog = ProgressDialog.show(BaikuWapActivity.this, "", 
				getString(R.string.page_status_updating));
			if (null != mProgressDialog) {
				mProgressDialog.setCancelable(true);
			}
		}
	}
	
	private void onLoadFinished() {
		if (null != mProgressDialog) {
			Log.d(TAG, "progress dialog dismiss by finished.");
			mProgressDialog.setMessage(getString(R.string.page_status_success));
			mProgressDialog.dismiss();
			mProgressDialog = null;
			
			if (BaikuApplication.isCanCheckedUpdate()) {
				Log.d(TAG, "after first load homepage url, then doUpdateChecked.");
				doCheckUpdate();
			}
		}
	}
	
	private void onLoadFailed() {
		if (null != mProgressDialog) {
			Log.d(TAG, "progress dialog dismiss by failed.");
			mProgressDialog.setMessage(getString(R.string.page_status_unable_to_update));
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
	}
	
	private class BaikuWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			Log.d(TAG, "shouldOverrideUrlLoading");
			view.loadUrl(url);
			return true;
		}
		
		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			Log.d(TAG, "web load start: " + url);
			onLoadStarted();
		}
		
		@Override
		public void onPageFinished(WebView view, String url) {
			Log.d(TAG, "web load finished: " + url);
			onLoadFinished();
		}
		
		@Override
		public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
			Log.d(TAG, "web loading " + failingUrl + " error:" + errorCode + " description" + description);
			onLoadFailed();
		}
	}
	
	private class BaikuWebChromeClient extends WebChromeClient {
		public void onProgressChanged(WebView view, int newProgress) {
			// Log.d(TAG, "web load progress: " + newProgress);
		}
	}
	
	private class BaikuWebDownloadListener implements DownloadListener {
		public void onDownloadStart(String url, String userAgent, String contentDispostion, String mimetype, long contentLength) {
			Log.d(TAG, String.format("onDownloadStart url: %s", url));
			Uri uri = Uri.parse(url);
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			startActivity(intent);
		}
	}
	
	protected void onActivityResult(int requsetCode, int resultCode, Intent data) {
		super.onActivityResult(requsetCode, resultCode, data);
		
		try {
			if (resultCode == Activity.RESULT_OK) {
				if (requsetCode == TAKE_PHOTO) {
					Log.d(TAG, "photo uri" + mImageUri.getPath());
					Intent intent = UploadActivity.createIntent(UploadActivity.SOURCE_PHOTO, mImageUri);
					startActivity(intent);
				} else if (requsetCode == TAKE_VIDEO) {
					Uri uri = data.getData();
					if (null != uri) {
						Log.d(TAG, "video uri" + getRealPathFromURI(uri));
						Intent intent = UploadActivity.createIntent(UploadActivity.SOURCE_VIDEO, uri);
						startActivity(intent);
					} else {
						Log.e(TAG, "video uri is empty..");
					}
				}
			}
		} catch (Exception e) {
			Log.e(TAG, "requestCode: " + requsetCode);
			e.printStackTrace();
		}
	}
	
	/**
	 * get real path from uri
	 */
	private String getRealPathFromURI(Uri contentUri) {
		String[] proj = { MediaColumns.DATA };
		Cursor cursor = managedQuery(contentUri, proj, null, null, null);
		int column_index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

	private UpdateListener mUpdateListener = new UpdateAdapter() {
		@Override
		public void onUpdateAvaliable(String url) {
			doUpdateAvaliable(url);
		}
		
		@Override
		public void onUpdateForce(String url) {
			doUpdateForce(url);
		}
		
		@Override
		public void onUpdateFromPage(String url) {
		}
		
		@Override
		public void onUpdateNone(String url) {
		}
		
		@Override
		public String getName() {
			return "updateListener";
		}
	};
	private void doUpdateAvaliable(String url) {
		Log.i(TAG, "doUpdateAvaliable, url: " + url);
		mUpdateUrl = url;
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(false);
		builder.setTitle("佰酷");
		builder.setMessage("发现可用更新，是否下载？");
		builder.setPositiveButton("下载", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				doDownload();
			}
		});
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		builder.show();	
	}
	private void doUpdateForce(String url) {
		Log.i(TAG, "doUpdateForce url: " + url);
		mUpdateUrl = url;
		doDownload();
	}
	private void doDownload() {
		Log.i(TAG, "doDownload url: " + mUpdateUrl);
		if (TextUtils.isEmpty(mUpdateUrl)) {
			return;
		}
		
        Uri uri = Uri.parse(mUpdateUrl);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setClassName("com.android.browser", "com.android.browser.BrowserActivity");
        startActivity(intent); 
        
        super.handleExit();
	}
	private void doCheckUpdate() {
		String phoneModel = Build.MODEL;
		
		TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		String phoneNumber = tm.getLine1Number();
		
		DisplayMetrics metric = new DisplayMetrics();
	    getWindowManager().getDefaultDisplay().getMetrics(metric);
	    int screenWidth = metric.widthPixels;
	    int screenHeight = metric.heightPixels; 
	    
	    String url = "http://update.baiku.cn/update/check_update.jsp";
	    String devInfo = "{\"name\":\""+phoneModel+"\",\"number\":\""+phoneNumber+"\",\"screen\":\""+screenWidth+"*"+screenHeight+"\"}";
		String request = "{\"version\":\"1.0.1\", \"opt_src\":\"baiku_android_v1\", \"dev_info\":"+devInfo+"}";
	    
		BaikuApplication.mUpdateManager.setListener(mUpdateListener);
		BaikuApplication.mUpdateManager.checkUpdate(url, request);
	}
}