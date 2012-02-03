package com.baiku.android;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.baiku.android.contact.ContactAdapter;

public class ContactActivity extends BaseActivity {
	private static final String TAG = "ContactActivity";
	private static final String LAUNCH_ACTION = "com.baiku.android.CONTACT";

	private ListView mListView;

	@Override
	protected boolean _onCreate(Bundle savedInstanceState) {
		if (!super._onCreate(savedInstanceState)) {
			return false;
		}

		setContentView(R.layout.contact);

		mListView = (ListView) findViewById(R.id.baiku_contact_listview);
		ContactAdapter adapter = new ContactAdapter(this, getData());

		// 添加并且显示
		mListView.setAdapter(adapter);

		// 添加点击
		mListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Toast.makeText(ContactActivity.this, "点击第" + arg2 + "个项目",
						Toast.LENGTH_LONG).show();
			}
		});

		// 添加长按点击
		mListView.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
			public void onCreateContextMenu(ContextMenu menu, View v,
					ContextMenuInfo menuInfo) {
				menu.setHeaderTitle("长按菜单选项");
				menu.add(1, 0, 0, "编辑");
				menu.add(0, 1, 0, "删除");
			}
		});

		return true;
	}

	private List<Map<String, String>> getData() {
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();

		ContentResolver contentResolver = getContentResolver();
		Uri uri = Uri.parse("content://com.android.contacts/contacts");
		Cursor cursor = contentResolver.query(uri, null, null, null, null);
		while (cursor.moveToNext()) {
			String id = cursor.getString(cursor
					.getColumnIndex(ContactsContract.Contacts._ID));
			String name = cursor.getString(cursor
					.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
			Map<String, String> map = new HashMap<String, String>();
			map.put("id", id);
			map.put("name", name);
			list.add(map);
		}
		cursor.close();

		return list;
	}
	
	public static Intent createIntent() {
		Intent intent = new Intent(LAUNCH_ACTION);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		return intent;
	}
}
