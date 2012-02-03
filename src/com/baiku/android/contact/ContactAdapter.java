package com.baiku.android.contact;

import com.baiku.android.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class ContactAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	private List<Map<String, String>> mListData;
	private Map<Integer, Map<String, String>> mSelectMap = new HashMap<Integer, Map<String, String>>();

	private class ViewHolder {
		public ImageView img;
		public TextView title;
		public CheckBox checkbox;
	}

	public ContactAdapter(Context context, List<Map<String, String>> data) {
		this.mInflater = LayoutInflater.from(context);
		this.mListData = data;
	}

	public int getCount() {
		return mListData.size();
	}

	public Object getItem(int position) {
		return mListData.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.contact_item, null);
			
			final View view = convertView;
			holder.img = (ImageView)convertView.findViewById(R.id.contact_image);
			holder.title = (TextView)convertView.findViewById(R.id.contact_title);
			holder.checkbox = (CheckBox)convertView.findViewById(R.id.contact_checkbox);
			holder.checkbox.setOnClickListener(new View.OnClickListener(){
				public void onClick(View v) {
					if (mSelectMap.get(position) != null) {
						mSelectMap.remove(position);
					} else {
						mSelectMap.put(position, mListData.get(position));
					}
				}
			});
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder)convertView.getTag();
		}
		
		holder.img.setBackgroundResource(R.drawable.logo);
		holder.title.setText(mListData.get(position).get("name"));
		
		if (mSelectMap.get(position) != null) {
			holder.checkbox.setChecked(true);
		} else {
			holder.checkbox.setChecked(false);
		}
		
		return convertView;
	}
}
