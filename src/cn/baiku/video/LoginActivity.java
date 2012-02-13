package cn.baiku.video;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import cn.baiku.video.app.Preferences;
import cn.baiku.video.task.GenericTask;
import cn.baiku.video.task.TaskAdapter;
import cn.baiku.video.task.TaskFeedback;
import cn.baiku.video.task.TaskListener;
import cn.baiku.video.task.TaskParams;
import cn.baiku.video.task.TaskResult;

public class LoginActivity extends Activity {
	private static final String TAG = "LoginActivity";
	private static final String LAUNCH_ACTION = "cn.baiku.video.LOGIN";
	private static final String SIS_RUNNING_KEY = "running";
	
	private TextView mTextProgress;
	private EditText mEditName;
	private EditText mEditPassword;
	private Button mBtnLogin;
	
	private String mUserName;
	private String mPassword;
	
	private SharedPreferences mPref;
	
	private GenericTask mLoginTask;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// No Title bar
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		requestWindowFeature(Window.FEATURE_PROGRESS);
		
		mPref = PreferenceManager.getDefaultSharedPreferences(this);
		
		setContentView(R.layout.login);
		
		Log.d(TAG, "onCreate()");
		
		// TextView中嵌入HTML链接
		TextView registerLink = (TextView) findViewById(R.id.register_link);
		registerLink.setMovementMethod(LinkMovementMethod.getInstance());
		
		mEditName = (EditText)findViewById(R.id.username_edit);
		mEditPassword = (EditText)findViewById(R.id.password_edit);
		
		mBtnLogin = (Button)findViewById(R.id.signin_button);
		mBtnLogin.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				doLogin();
			}
		});
		
		mTextProgress = (TextView)findViewById(R.id.progress_text);
	}
	
	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestory.");
		if (mLoginTask != null 
				&& mLoginTask.getStatus() == GenericTask.Status.RUNNING) {
			// Doesn't really cancel execution (we let it continue running).
			// See the SendTask code for more details.
			mLoginTask.cancel(true);
		}
		super.onDestroy();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (mLoginTask != null
				&& mLoginTask.getStatus() == GenericTask.Status.RUNNING) {
			outState.putBoolean(SIS_RUNNING_KEY, true);
		}
	}
	
	protected void doLogin() {
		mUserName = mEditName.getText().toString();
		mPassword = mEditPassword.getText().toString();
		
		Log.d(TAG, "username:" + mUserName + " password:" + mPassword);
		if (TextUtils.isEmpty(mUserName) || TextUtils.isEmpty(mPassword)) {
			updateProgress(getString(R.string.login_status_null_username_or_password));
			return;
		}
		
		if (!(mLoginTask != null && mLoginTask.getStatus() == GenericTask.Status.RUNNING)) {
			mLoginTask = new LoginTask();
			mLoginTask.setListener(mLoginTaskListener);
			
			TaskParams params = new TaskParams();
			params.put("username", mUserName);
			params.put("password", mPassword);
			mLoginTask.execute(params);
		}
	}
	
	private void onLoginBegin() {
		disableLogin();
		TaskFeedback.getInstance(TaskFeedback.DIALOG_MODE, LoginActivity.this)
			.start(getString(R.string.login_status_logging_in));
	}
 	
	private void onLoginSuccess() {
		mEditName.setText("");
		mEditPassword.setText("");
		
		Log.d(TAG, "storing credentials.");
		
		TaskFeedback.getInstance(TaskFeedback.DIALOG_MODE, LoginActivity.this)
			.success();
		
		BaikuApplication.mApi.setCredentials(mUserName, mPassword);
		
		Intent intent = getIntent().getParcelableExtra(Intent.EXTRA_INTENT);
		String action = intent.getAction();
		
		if (intent.getAction() == null || !Intent.ACTION_SEND.equals(action)) {
			intent = new Intent(this, TabHostActivity.class);
		}
		startActivity(intent);
		finish();
	}
	
	private void onLoginFailture(String reason) {
		TaskFeedback.getInstance(TaskFeedback.DIALOG_MODE, LoginActivity.this)
			.failed(reason);
		enableLogin();
	}
	
	private void enableLogin() {
		mEditName.setEnabled(true);
		mEditPassword.setEnabled(true);
		mBtnLogin.setEnabled(true);
	}
	
	private void disableLogin() {
		mEditName.setEnabled(false);
		mEditPassword.setEnabled(false);
		mBtnLogin.setEnabled(false);		
	}
	
	private void updateProgress(String progress) {
		mTextProgress.setText(progress);
	}
	
	private class LoginTask extends GenericTask {
		private String msg = getString(R.string.login_status_failure);
		public String getMsg() {
			return msg;
		}
		protected TaskResult _doInBackground(TaskParams... params) {
			TaskParams param = 	params[0];
			try {
				String username = param.getString("username");
				String password = param.getString("password");
				
				// 写入当前数据
				SharedPreferences.Editor editor = mPref.edit();
				editor.putString(Preferences.USERNAME_KEY, username);
				editor.putString(Preferences.PASSWORD_KEY, password);
				editor.putBoolean(Preferences.WAPSITE_KEY, false);
				editor.commit();
			} catch (Exception e) {
				Log.e(TAG, e.getMessage(), e);
				return TaskResult.FAILED;
			}
			return TaskResult.OK;
		}
	}
	
	private TaskListener mLoginTaskListener = new TaskAdapter() {
		@Override
		public void onPreExecute(GenericTask task) {
			onLoginBegin();
		}
		
		@Override
		public void onProgressUpdate(GenericTask task, Object param) {
		}
		
		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			if (result == TaskResult.OK) {
				onLoginSuccess();
			} else {
				onLoginFailture(((LoginTask)task).getMsg());
			}
		}
		
		@Override
		public String getName() {
			return "Login";
		}
	};

}