package cn.baiku.video.app;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class ImageManager implements ImageCache{
	private static final String TAG = "ImageManager";
	
	public static final int DEFAULT_COMPRESS_QUALITY = 90;
	public static final int IMAGE_MAX_WIDTH = 400;
	public static final int IMAGE_MAX_HEIGHT = 800;
	
	private Context mContext;
	// In memory cache
	private Map<String, SoftReference<Bitmap>> mCache;
	// MD5 hasher
	private MessageDigest mDigest;
	
	public ImageManager(Context context) {
		mContext = context;
		mCache = new HashMap<String, SoftReference<Bitmap>>();
		
		try {
			mDigest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			// This shouldn't happen.
			throw new RuntimeException("No MD5 algorithm.");
		}
	}
	
	public void setContext(Context context) {
		mContext = context;
	}
	
	private String getHashString(MessageDigest digest) {
		StringBuilder builder = new StringBuilder();
		for (byte b: digest.digest()) {
			builder.append(Integer.toHexString((b >> 4) & 0xf));
			builder.append(Integer.toHexString(b & 0xf));			
		}
		return builder.toString();
	}
	
	// Compress and resize the image
	public File compressImage(File targetFile, int quality) throws IOException {
		String filePath = targetFile.getAbsolutePath();
		
		// 1. Calculate scale
		int scale = 1;
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, opt);
		if (opt.outWidth > IMAGE_MAX_WIDTH || opt.outHeight > IMAGE_MAX_HEIGHT) {
			scale = (int)Math.pow(
					2.0, 
					(int)Math.round(Math.log(IMAGE_MAX_WIDTH 
							/ (double)Math.max(opt.outHeight, opt.outWidth))
							/ Math.log(0.5)));
		}
		Log.d(TAG, scale + " scale");
		
		// 2. File -> Bitmap (Returning a smaller bitmap)
		opt.inJustDecodeBounds = false;
		opt.inSampleSize = scale;
		Bitmap bitmap = BitmapFactory.decodeFile(filePath, opt);
		
		// 3. Bitmap -> File
		writeFile(filePath, bitmap, quality);
		
		// 4. Get resized image file
		// 分享接口处理时需要后缀，故加上后缀。
		String strPath = getMd5(filePath) + ".jpg"; 
		File compressedImage = mContext.getFileStreamPath(strPath);
		return compressedImage;
	}
	
	private void writeFile(String file, Bitmap bitmap, int quality) {
		if (bitmap == null) {
			Log.w(TAG, "can't write file. Bitmap is null.");
			return;
		}
		
		BufferedOutputStream bos = null;
		try {
			// 分享接口处理时需要后缀，故加上后缀。
			String hasedUrl = getMd5(file) + ".jpg";
			bos = new BufferedOutputStream(mContext.openFileOutput(hasedUrl,
					Context.MODE_PRIVATE));
			bitmap.compress(Bitmap.CompressFormat.JPEG, quality, bos); // PNG
			Log.d(TAG, "Writing file: " + file);
		} catch (IOException ioe) {
			Log.e(TAG, ioe.getMessage());
		} finally {
			try {
				if (bos != null) {
					bitmap.recycle();
					bos.flush();
					bos.close();
				}
			} catch (IOException ioe) {
				Log.e(TAG, "Could not close file.");
			}
		}
	}
	
	private String getMd5(String url) {
		mDigest.update(url.getBytes());
		return getHashString(mDigest);
	}
	
	public Bitmap get(String url) {
		return null; 
	}
	
	public void put(String url, Bitmap bitmap) {
		
	}
}
