package com.baiku.android.update;

public abstract class UpdateAdapter implements UpdateListener{
	public abstract String getName();
	
	public void onUpdateAvaliable(String url) {
	}
	
	public void onUpdateForce(String url) {
	}
	
	public void onUpdateFromPage(String url) {
	}
	
	public void onUpdateNone(String url) {
	}
}
