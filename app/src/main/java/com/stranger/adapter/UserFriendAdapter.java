package com.stranger.adapter;

import java.util.ArrayList;
import java.util.List;

import com.example.examplep.R;
import com.stranger.bean.DSUser;
import com.stranger.util.ImageFileCache;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 用户好友的适配器
 * 
 * @author RImpression
 * 
 */
public class UserFriendAdapter extends BaseAdapter {
	private Context context;
	private List<DSUser> data;

	public UserFriendAdapter(Context context, List<DSUser> datas) {
		this.context = context;
		this.data = datas;
	}

	// 用于更新listview
	public void updateListView(List<DSUser> list) {
		this.data = list;
		notifyDataSetChanged();
	}

	public void remove(DSUser user) {
		this.data.remove(user);
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Object getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder viewHolder = null;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.item_user_friend, null);
			viewHolder = new ViewHolder();
			viewHolder.alpah = (TextView) convertView.findViewById(R.id.alpha);
			viewHolder.name = (TextView) convertView
					.findViewById(R.id.tv_friend_name);
			viewHolder.avatar = (ImageView) convertView
					.findViewById(R.id.img_friend_avatar);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		// 获取好友对象
		DSUser friend = data.get(position);
		// 获取好友昵称和头像
		final String name = friend.getUsername();
		final String avatar = friend.getAvatar();

		viewHolder.name.setText(name);
		if (!TextUtils.isEmpty(avatar)) {
			new MThread(avatar, viewHolder.avatar).start();
		} else {
			viewHolder.avatar.setImageDrawable(context.getResources().
					getDrawable(R.drawable.photo_normal));
		}
		
		//根据position获取分类的首字母的Char ascii值
		int section = getSectionForPosition(position);
		//如果当前位置等于该分类首字母的Char的位置，则认为是第一次出现
		if(position == getPositionForSection(section)) {
			viewHolder.alpah.setVisibility(View.VISIBLE);
			viewHolder.alpah.setText(friend.getSortLetters());
		} else {
			viewHolder.alpah.setVisibility(View.GONE);
		}

		return convertView;
	}

	/*
	 * 根据分类的首字母的Char ascii值获取第一次出现该首字母的位置
	 */
	@SuppressLint("DefaultLocale")
	public int getPositionForSection(int section) {
		for(int i=0;i<getCount();i++) {
			String sortStr = data.get(i).getSortLetters();
			char firstChar = sortStr.toUpperCase().charAt(0);
			if(firstChar == section) {
				return i;
			}
		}
		return -1;
	}

	/*
	 * 根据ListView的当前位置获取分类的首字母的Char ascii值
	 */
	public int getSectionForPosition(int position) {
		return data.get(position).getSortLetters().charAt(0);
	}

	static class ViewHolder {
		ImageView avatar;// 头像
		TextView name;// 名字
		TextView alpah;//首字母提示
	}

	public Object[] getSectObjects() {
		return null;
	}

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				List list = (List) msg.obj;
				Bitmap bitmap = (Bitmap) list.get(0);
				ImageView imageView = (ImageView) list.get(1);
				imageView.setImageBitmap(bitmap);
				break;
			}
		};
	};

	class MThread extends Thread {
		String para;
		ImageView item;

		public void setPara(String para) {
			this.para = para;
		}

		public MThread(String para, ImageView item) {
			super();
			this.para = para;
			this.item = item;
		}

		@Override
		public void run() {
			Bitmap bitmap = ImageFileCache.getBitmap(para);
			Message msg = Message.obtain();
			List list = new ArrayList();
			list.add(bitmap);
			list.add(item);

			msg.what = 0;
			msg.obj = list;
			handler.sendMessage(msg);
		}
	}

}
