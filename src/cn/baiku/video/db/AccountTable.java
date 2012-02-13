package cn.baiku.video.db;

import android.provider.BaseColumns;

public final class AccountTable implements BaseColumns {
	public static final String TAG = "UserInfoTable";
	
	public static final String TABLE_NAME="account";
	public static final String ACCOUNT_NAME="name";
	public static final String ACCOUNT_PASSWORD="password";
	public static final String LOGIN_TIME="login_time";
	
	public static final String[] TABLE_COLUMNS = new String[] {_ID,
		ACCOUNT_NAME, ACCOUNT_PASSWORD, LOGIN_TIME};
	
	public static final String CREATE_TABLE = 
		"CREATE_TABLE " + TABLE_NAME + "("
		+ _ID + " text not null, "
		+ ACCOUNT_NAME + " text not null, "
		+ ACCOUNT_PASSWORD + " text not null, "
		+ "PRIMARY KEY (" + ACCOUNT_NAME + "))";
}