package com.baiku.android.db;

import com.baiku.android.db.Account;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class BaikuDatabase {
	private static final String TAG = "BaikuDatabase";
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "baiku_media";
	
	private static BaikuDatabase instance = null;
	private static DatabaseHelper helper = null;
	private Context context = null;

	private static class DatabaseHelper extends SQLiteOpenHelper {
		public DatabaseHelper(Context context, String name, 
				CursorFactory factory, int version) {
			super(context, name, factory, version);
		}
		
		public DatabaseHelper(Context context, String name) {
			this(context, name, null, DATABASE_VERSION);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.i(TAG, "create database");
			db.execSQL(AccountTable.CREATE_TABLE);
		}
		
		@Override
		public synchronized void close() {
			Log.i(TAG, "close");
			super.close();
		}
		
		@Override
		public void onOpen(SQLiteDatabase db) {
			Log.i(TAG, "open database");
			super.onOpen(db);
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.i(TAG, "upgrade database");
			db.execSQL("DROP TABLE IF EXISTS " + AccountTable.TABLE_NAME);
		}
	}
	
	public static synchronized BaikuDatabase getInstance(Context context) {
		Log.i(TAG, "get instance");
		if (null == instance) {
			instance = new BaikuDatabase(context);
		}
		return instance;
	}
	
	private BaikuDatabase(Context context) {
		this.context = context;
		helper = new DatabaseHelper(context, DATABASE_NAME);
	}
	
	public void closeDatabase() {
		Log.i(TAG, "close database");
		if (null != instance) {
			helper.close();
			instance = null;
		}
	}
	
	// Account
	public long insertAccout(Account account) {
		Log.i(TAG, "insert account");
		SQLiteDatabase db = helper.getWritableDatabase();
		if (isAccountExist(account.name)) {
			Log.w(TAG, "account name " + account.name + "is exists.");
			return -1;
		}
		
		ContentValues values = new ContentValues();
		values.put(AccountTable._ID, account.id);
		values.put(AccountTable.ACCOUNT_NAME, account.name);
		values.put(AccountTable.ACCOUNT_PASSWORD, account.password);
		values.put(AccountTable.LOGIN_TIME, account.loginTime);
		long id = db.insert(AccountTable.TABLE_NAME, null, values);
		if (-1 == id) {
			Log.e(TAG, "can't insert account :" + account.toString());
		}
		
		return id;
	}
	
	public Cursor fetchAllAccounts() {
		Log.i(TAG, "fetchAllAccounts");
		SQLiteDatabase db = helper.getReadableDatabase();
		return db.query(AccountTable.TABLE_NAME, AccountTable.TABLE_COLUMNS,
			null, null, null, null, null);
	}
	
	public boolean isAccountExist(String name) {
		SQLiteDatabase db = helper.getWritableDatabase();
		boolean result = false;
		Cursor cursor = db.query(AccountTable.TABLE_NAME,
				new String[] {AccountTable._ID}, AccountTable._ID + " =? ", 
				new String[] {name}, null, null, null);
		
		if (cursor != null && cursor.getCount() > 0) {
			result = true;
		}
		cursor.close();
		return result;
	}
}