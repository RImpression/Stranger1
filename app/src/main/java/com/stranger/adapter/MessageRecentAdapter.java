package com.stranger.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import cn.bmob.im.bean.BmobRecent;
import cn.bmob.im.config.BmobConfig;
import cn.bmob.im.db.BmobDB;

import com.example.examplep.R;
import com.stranger.util.ImageFileCache;
import com.stranger.util.TimeUtil;

/**
 * 最近会话适配器
 * @author RImpression
 *
 */

public class MessageRecentAdapter extends ArrayAdapter<BmobRecent>{
	private LayoutInflater inflater;
	private List<BmobRecent> mData;
	private Context mContext;

	public MessageRecentAdapter(Context context, int resource,
			List<BmobRecent> objects) {
		super(context, resource, objects);
		inflater = LayoutInflater.from(context);
		this.mContext = context;
		mData = objects;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final BmobRecent item = mData.get(position);
		if(convertView == null) {
			convertView = inflater.inflate(R.layout.list_item_message, parent,false);
		}
		ImageView iv_recent_avatar = ViewHolder.get(convertView, R.id.iv_recent_avatar);//头像
		TextView tv_recent_name = ViewHolder.get(convertView, R.id.tv_recent_name);//姓名
		TextView tv_recent_msg = ViewHolder.get(convertView, R.id.tv_recent_msg);//消息
		TextView tv_recent_time = ViewHolder.get(convertView, R.id.tv_recent_time);//时间
		TextView tv_recent_unread = ViewHolder.get(convertView, R.id.tv_recent_unread);//未读条数
		//填充数据
		String avatar = item.getAvatar();
		tv_recent_name.setText(item.getUserName());
		tv_recent_time.setText(TimeUtil.getChatTime(item.getTime()));
		//如果头像不为空
		if(avatar != null) {
			new MThread(avatar, iv_recent_avatar).start();
		} else {
			iv_recent_avatar.setImageResource(R.drawable.photo_normal);
		}
		
		//显示内容
		if(item.getType()==BmobConfig.TYPE_TEXT){
			tv_recent_msg.setText(item.getMessage());
		}else if(item.getType()==BmobConfig.TYPE_IMAGE){
			tv_recent_msg.setText("[图片]");
		}else if(item.getType()==BmobConfig.TYPE_VOICE){
			tv_recent_msg.setText("[语音]");
		}
		
		//未读消息统计
		int num = BmobDB.create(mContext).getUnreadCount(item.getTargetid());
		if (num > 0) {
			tv_recent_unread.setVisibility(View.VISIBLE);
			tv_recent_unread.setText(num + "");
		} else {
			tv_recent_unread.setVisibility(View.GONE);
		}
		return convertView;
	}
	
	@SuppressWarnings("unchecked")
	public static class ViewHolder {
		public static <T extends View> T get(View view,int id) {
			SparseArray<View> viewHolder = (SparseArray<View>) view.getTag();
			if(viewHolder == null) {
				viewHolder = new SparseArray<View>();
				view.setTag(viewHolder);
			}
			View childView = viewHolder.get(id);
			if(childView == null) {
				childView = view.findViewById(id);
				viewHolder.put(id,childView);
			}
			return (T) childView;
		}
	}
	
	private Handler handler = new Handler(){
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
	
	class MThread extends Thread{
		String para;
		ImageView item;
		
		public void setPara(String para) {
			this.para = para;
		}
		
		
		public MThread(String para,ImageView item) {
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
