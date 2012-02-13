package cn.baiku.video;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import cn.baiku.video.update.UpdateAdapter;
import cn.baiku.video.update.UpdateListener;

public class WapActivity extends BaseActivity{
	private static final String TAG = "WapWrapActivity";
	private WebView mWebView;
	private ProgressDialog mProgressDialog = null;
	private String mUpdateUrl = "";
	
	@Override
	protected boolean _onCreate(Bundle savedInstanceState) {
		if (!super._onCreate(savedInstanceState)){
			return false;
		}
		setContentView(R.layout.wap);
		
		Log.d(TAG, "_onCreate()");
		
		/**
		 * URL有默认跳转，则是新弹出一个浏览器实例显示，不会在Activity内容页显示。
		 * 1.webview响应页面内的超链接，而不至于新弹出一个浏览器实例。
		 * 2.页面能够调用javascript代码。
		 */
		mWebView = (WebView)findViewById(R.id.test_wap);
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.requestFocus();
		
		mWebView.setWebViewClient(new BaikuWebViewClient());
		mWebView.setWebChromeClient(new BaikuWebChromeClient());
		mWebView.setDownloadListener(new BaikuWebDownloadListener());
		mWebView.loadUrl(BaikuApplication.mApi.getWapFirstUrl());
		
		return true;
	}
	
	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroy()");
		if (null != mProgressDialog) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
		super.onDestroy();
	}
	
	@Override
	protected void onResume() {
		Log.d(TAG, "onResume()");
		super.onResume();
		if (BaikuApplication.mbReloadUrl) {
			mWebView.loadUrl(BaikuApplication.mApi.getWapProfileUrl());
			BaikuApplication.mbReloadUrl = false;
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		/**
		 * 用webview点链接看了很多页以后为了让WebView支持回退功能，需要覆盖Activity类的
		 * onKeyDown()方法，如果不做任何处理，点击系统回退键，整个浏览器会调用finish()
		 * 而结束自身，而不会返回到上一页面。
		 */
		if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
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
			mProgressDialog = ProgressDialog.show(WapActivity.this, "", 
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
        
        // TODO real exit application.
        // super.handleExit();
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
