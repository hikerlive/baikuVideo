package com.baiku.android.http;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;

public class CountMultipartEntity extends MultipartEntity {
	private UploadListener mUploadListener;
	private CountOutputStream mOutputStream;
	private OutputStream mLastOutput;
	
	public CountMultipartEntity(UploadListener listen) {
		super(HttpMultipartMode.BROWSER_COMPATIBLE);
		mUploadListener = listen;
	}

	public void writeTo(OutputStream out) {
		if (mLastOutput == null || mLastOutput != out) {
			mLastOutput = out;
			mOutputStream = new CountOutputStream(out);
		}
		try {
			super.writeTo(mOutputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private class CountOutputStream extends FilterOutputStream {
		private long mTransferred = 0;
		private OutputStream mWrapoutput;
		
		public CountOutputStream(final OutputStream out) {
			super(out);
			mWrapoutput = out;
		}
		
		public void write(byte[] b, int off, int len) throws IOException {
			mWrapoutput.write(b, off, len);
			++ mTransferred;
			
			if (mUploadListener != null) {
				mUploadListener.onUploadProgress(mTransferred);
			}
		}
		
		public void write(int b) throws IOException {
			super.write(b);
		}
	}
}