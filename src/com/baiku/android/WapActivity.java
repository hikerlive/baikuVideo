package com.baiku.android;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class WapActivity extends Activity{
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wap);
		
		WebView webview = (WebView)findViewById(R.id.test_wap);
		webview.getSettings().setJavaScriptEnabled(true);
		webview.requestFocus();
		webview.loadUrl("http://wap.baiku.cn/homepage.action?reqcode=showhomepage");
	}
}
