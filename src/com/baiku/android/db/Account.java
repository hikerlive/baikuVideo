package com.baiku.android.db;

import android.os.Parcel;
import android.os.Parcelable;

public class Account implements Parcelable {
	public String id;
	public String name;
	public String password;
	public String loginTime;
	
	public Account(Parcel in) {
		
	}
	
	public static final Parcelable.Creator<Account> CREATOR = new Parcelable.Creator<Account>() {
		public Account createFromParcel(Parcel in) {
			return new Account(in);
		}

		public Account[] newArray(int size) {
			// return new Tweet[size];
			throw new UnsupportedOperationException();
		}
	};
	
	public int describeContents() {
		return 0;
	}
	
	public void writeToParcel(Parcel out, int flags) {
		
	}
	
	@Override
	public String toString() {
		return "";
	}
}