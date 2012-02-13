package cn.baiku.video.http;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.util.Log;

public class CustomHttpClient {
	private static final String CHARSET = HTTP.UTF_8;
	private static final String TAG = "CustomHttpClient";
	private static HttpClient customerHttpClient;
	private CustomHttpClient() {
	}
	public static synchronized HttpClient getHttpClient() {
		if (null == customerHttpClient) {
			HttpParams params = new BasicHttpParams();
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, CHARSET);
			HttpProtocolParams.setUseExpectContinue(params, true);
			HttpProtocolParams.setUserAgent(params, 
					 "Mozilla/5.0(Linux;U;Android 2.2.1;en-us;Nexus One Build.FRG83) "
                    + "AppleWebKit/553.1(KHTML,like Gecko) Version/4.0 Mobile Safari/533.1");
			
			// 从连接池中取连续的超时时间
			ConnManagerParams.setTimeout(params, 1000);
			HttpConnectionParams.setConnectionTimeout(params, 2000);
			HttpConnectionParams.setSoTimeout(params, 4000);
			
			// 设置我们的HttpClient支持HTTP和HTTPS两种模式
			SchemeRegistry schReg = new SchemeRegistry();
			schReg.register(new Scheme("http", 
					PlainSocketFactory.getSocketFactory(),
					80));
			schReg.register(new Scheme("https", 
					SSLSocketFactory.getSocketFactory(),
					443));
			
			// 使用线程安全的连接管理来创建HttpClient
			ClientConnectionManager conMgr = new ThreadSafeClientConnManager(
					params, schReg);
			customerHttpClient = new DefaultHttpClient(conMgr, params);
		}
		return customerHttpClient;
	}
	
	public static String post(String url, NameValuePair... params) {
		try {
			// 编码参数
			List<NameValuePair> formparams = new ArrayList<NameValuePair>();
			for (NameValuePair p : params) {
				formparams.add(p);
			}
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, CHARSET);
			
			// 创建POST请求
			HttpPost request = new HttpPost(url);
			request.setEntity(entity);
			
			// 发送请求
			HttpClient client = getHttpClient();
			HttpResponse response = client.execute(request);
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				throw new RuntimeException("请求参数");
			}
			HttpEntity resEntity = response.getEntity();
			return (resEntity == null) ? null : EntityUtils.toString(resEntity, CHARSET);
		} catch (UnsupportedEncodingException e) {
			Log.w(TAG, e.getMessage());
			return null;
		} catch (ClientProtocolException e) {
			Log.w(TAG, e.getMessage());
			return null;
		} catch (IOException e) {
			throw new RuntimeException("连接失败", e);
		}
	}
	
	public static void post(String url, String username, String pwd, String subject, String tags, 
			String type, File file, UploadListener listner) throws UnsupportedEncodingException {
		if (listner != null) listner.onUploadBegin();
		
		HttpClient httpclient = new DefaultHttpClient();
		
		HttpPost httppost = new HttpPost(url);  
 
        String str = new String(subject.getBytes("utf-8"),"iso-8859-1");
        StringBody _subject = new StringBody(str, Charset.forName("iso-8859-1"));
        
        String strTags = new String(tags.getBytes("utf-8"),"iso-8859-1");
        StringBody _tags = new StringBody(strTags, Charset.forName("iso-8859-1"));
  
        MultipartEntity reqEntity = new CountMultipartEntity(listner);
        reqEntity.addPart("username", new StringBody(username));
        reqEntity.addPart("password", new StringBody(pwd));
        reqEntity.addPart("subject", _subject);
        reqEntity.addPart("tags", _tags);
        reqEntity.addPart("type", new StringBody(type));
        if (file != null) {
        	FileBody _file = new FileBody(file);  
        	reqEntity.addPart("file", _file);
        }
        reqEntity.addPart("agent", new StringBody("baiku_android_agent"));
        httppost.setEntity(reqEntity);
        
        try {
            HttpResponse response = httpclient.execute(httppost);
            int statusCode = response.getStatusLine().getStatusCode();
            if(HttpStatus.SC_OK==statusCode){  
                HttpEntity entity = response.getEntity();  
                if (entity != null) {  
                    System.out.println(EntityUtils.toString(entity));  
                }  
                if (entity != null) {  
                    entity.consumeContent();  
                } 
            } else {
            	if (null != listner){
            		listner.onUploadFailed(statusCode);
            	}
            }
        } catch(Exception e) {
        	if (null != listner) {
        		listner.onUploadFinish();
        	}
        	e.printStackTrace();
        	Log.e(TAG, "exception", e.getCause());
        }
        if (null != listner) {
        	listner.onUploadFinish();
        }
	}
}	
