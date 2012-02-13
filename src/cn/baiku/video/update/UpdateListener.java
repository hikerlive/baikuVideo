package cn.baiku.video.update;

public interface UpdateListener {
	String getName();
	
	void onUpdateAvaliable(String url);
	
	void onUpdateForce(String url);
	
	void onUpdateFromPage(String url);
	
	void onUpdateNone(String url);
}
