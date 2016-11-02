package com.stranger.frags;

import cn.bmob.im.bean.BmobChatUser;
import cn.bmob.im.bean.BmobRecent;
import cn.bmob.im.db.BmobDB;

import com.example.examplep.R;
import com.stranger.activity.ChatActivity;
import com.stranger.activity.MainActivity;
import com.stranger.adapter.MessageRecentAdapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
/**
 * 消息会话Fragment
 * @author RImperssion
 *
 */
public class LeftFrag extends Fragment implements OnItemClickListener, OnItemLongClickListener {
	ListView listView;
	//最近会话适配器
	MessageRecentAdapter messageAdapter;

	private View parentView;
	private MainActivity mainActivity;
	View agree_add_friend;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mainActivity = (MainActivity) activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		parentView = inflater.inflate(R.layout.frag_left, container, false);
		return parentView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		initView();
	}

	private void initView() {
		listView = (ListView) parentView.findViewById(R.id.lv_left);
		//单击进入聊天界面
		listView.setOnItemClickListener(this);
		//长按删除会话
		listView.setOnItemLongClickListener(this);
		messageAdapter = new MessageRecentAdapter(getActivity(), R.layout.list_item_message,
				BmobDB.create(getActivity()).queryRecents());
		listView.setAdapter(messageAdapter);
	}
	
	/*
	 * 删除会话 
	 */
	private void deleteRecent(BmobRecent recent) {
		messageAdapter.remove(recent);
		BmobDB.create(getActivity()).deleteRecent(recent.getTargetid());
		BmobDB.create(getActivity()).deleteMessages(recent.getTargetid());
	}
	/*
	 * 显示删除dialog 
	 */
	public void showDeleteDialog(final BmobRecent recent) {
		//设置dialog样式
				AlertDialog.Builder deleteDialog = new AlertDialog.Builder(mainActivity);
				deleteDialog.setTitle("删除会话");
				deleteDialog.setMessage("确定删除与"+ recent.getUserName() + "会话?");
				deleteDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						//确认删除会话
						deleteRecent(recent);
					}
				});
				deleteDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						//取消删除会话
					}
				});
				//显示dialog
				deleteDialog.show();
				deleteDialog = null;
	}
	

	//单击进入聊天界面
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		BmobRecent recent = messageAdapter.getItem(arg2);
		//重置未读消息
		BmobDB.create(getActivity()).resetUnread(recent.getTargetid());
		//组装聊天对象
		BmobChatUser user = new BmobChatUser();
		user.setAvatar(recent.getAvatar());//头像
		user.setUsername(recent.getUserName());
		user.setNick(recent.getNick());
		user.setObjectId(recent.getTargetid());
		//跳转
		Intent intent = new Intent(getActivity(),ChatActivity.class);
		intent.putExtra("user", user);
		startActivity(intent);
		
	}
	//长按删除会话
	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		BmobRecent recent = messageAdapter.getItem(arg2);
		showDeleteDialog(recent);
		return true;
	}
	
	private boolean hidden;
	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		this.hidden = hidden;
		if(!hidden){
			refresh();
		}
	}

	private void refresh() {
		try {
			getActivity().runOnUiThread(new Runnable() {
				public void run() {
					messageAdapter = new MessageRecentAdapter(getActivity(), R.layout.list_item_message, BmobDB.create(getActivity()).queryRecents());
					listView.setAdapter(messageAdapter);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if(!hidden){
			refresh();
		}
	}
	
}