package cn.baiku.video.util;

import java.io.File;
import java.io.IOException;

import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore.MediaColumns;

/**
 * 对SD卡文件的管理
 * @author Administrator
 */
public class FileHelper {
	private static final String TAG = "FileHelper";
	
	private static final String BASE_PATH = "baiku";
	
	public static File getBasePath() throws IOException {
		File basePath = new File(Environment.getExternalStorageDirectory(),
			BASE_PATH);
		
		if (!basePath.exists()) {
			if (!basePath.mkdirs()) {
				throw new IOException(String.format("%s cannot be created!", 
						basePath.toString()));
			}
		}
		
		if (!basePath.isDirectory()) {
			throw new IOException(String.format("%s is not a directory!",
					basePath.toString()));
		}
		
		return basePath;
	}
}