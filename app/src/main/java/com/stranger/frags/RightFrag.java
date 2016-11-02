package com.stranger.frags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import cn.bmob.im.BmobUserManager;
import cn.bmob.im.bean.BmobChatUser;
import cn.bmob.im.db.BmobDB;
import cn.bmob.v3.listener.UpdateListener;

import com.example.examplep.R;
import com.stranger.activity.ChatActivity;
import com.stranger.activity.MainActivity;
import com.stranger.activity.NewFriendActivity;
import com.stranger.adapter.UserFriendAdapter;
import com.stranger.bean.DSUser;
import com.stranger.bean.MApplication;
import com.stranger.util.CharacterParser;
import com.stranger.util.CollectionUtils;
import com.stranger.util.PinyinComparator;
import com.stranger.view.MyLetterView;
import com.stranger.view.MyLetterView.OnTouchingLetterChangedListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
/**
 * 通讯录Fragment
 * @author RImperssion
 *
 */
public class RightFrag extends Fragment implements OnItemClickListener, OnItemLongClickListener, OnClickListener {

	private View parentView;
	private MainActivity mainActivity;
	MyLetterView right_letter;
	TextView dialog;//显示字母
	//新朋友
	private View layout_newfriend;
	ListView list_friends;
	//好友列表适配器
	private UserFriendAdapter userAdapter;
	//好友列表
	List<DSUser> friends = new ArrayList<DSUser>();
	//汉字转换成拼音
	private CharacterParser characterParser;
	//根据拼音排列数据
	private PinyinComparator pinyinComparator;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mainActivity = (MainActivity) activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		parentView = inflater.inflate(R.layout.frag_right, container, false);
		layout_newfriend = parentView.findViewById(R.id.layout_new);
		layout_newfriend.setOnClickListener(this);
		return parentView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		characterParser = CharacterParser.getInstance();
		pinyinComparator = new PinyinComparator();
		initListView();
		initRightLetterView();
	}
	
	private void initRightLetterView() {
		right_letter = (MyLetterView) parentView.findViewById(R.id.right_letter);
		dialog = (TextView) parentView.findViewById(R.id.dialog);
		right_letter.setTextView(dialog);
		right_letter.setOnTouchingLetterChangedListener(new LetterListViewListener()); 
	}
	
	private class LetterListViewListener implements OnTouchingLetterChangedListener {
		@Override
		public void onTouchingLetterChanged(String s) {
			//该字母首次出现的位置
			int position = userAdapter.getPositionForSection(s.charAt(0));
			if(position != -1) {
				list_friends.setSelection(position);
			}
		}
	}

	/*
	 * 为ListView填充数据
	 */
	private void filledData(List<BmobChatUser> datas) {
		friends.clear();
		int total = datas.size();
		for(int i=0;i<total;i++) {
			BmobChatUser user = datas.get(i);
			DSUser dsUser = new DSUser();
			dsUser.setAvatar(user.getAvatar());	//头像
			dsUser.setNick(user.getNick());		//昵称
			dsUser.setUsername(user.getUsername());	//用户名
			dsUser.setObjectId(user.getObjectId());	//用户ID
			dsUser.setContacts(user.getContacts()); //好友列表
			//汉字转换成拼音
			String username = dsUser.getUsername();
			//若没有username
			if(username != null) {
				String pinyin = characterParser.getSelling(dsUser.getUsername());
				String sortString = pinyin.substring(0,1).toUpperCase();
				//正则表达式，判断首字母是否是英文字母
				if(sortString.matches("[A-Z]")) {
					dsUser.setSortLetters(sortString.toUpperCase());
				} else {
					dsUser.setSortLetters("#");
				}
			} else {
				dsUser.setSortLetters("#");
			}
			friends.add(dsUser);
		}
		//根据a-z进行排序
		Collections.sort(friends,pinyinComparator);
	}

	//新朋友栏中的通知红点
	ImageView iv_msg_tips;
	
	private void initListView() {
		list_friends = (ListView) parentView.findViewById(R.id.lv_right);
		iv_msg_tips = (ImageView) parentView.findViewById(R.id.iv_msg_tips);
		userAdapter = new UserFriendAdapter(getActivity(), friends);
		list_friends.setAdapter(userAdapter);
		//单击进入聊天界面
		list_friends.setOnItemClickListener(this);	
		//长按删除
		list_friends.setOnItemLongClickListener(this);
		
	}
	
	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		if(isVisibleToUser) {
			queryMyfriends(); //获取好友列表
		}
		super.setUserVisibleHint(isVisibleToUser);
	}

	/*
	 * 获取好友列表
	 */
	private void queryMyfriends() {
		//根据是否有好友请求，判断显示新朋友栏的通知红点
		if(BmobDB.create(getActivity()).hasNewInvite()) {
			iv_msg_tips.setVisibility(View.VISIBLE);
		} else {
			iv_msg_tips.setVisibility(View.GONE);
		}
		
		//在这里再做一次本地的好友数据库的检查，是为了本地好友数据库中已经添加了对方
		//但是界面却没有显示出来的问题
		//重新设置下内存中保存的好友列表
		MApplication.getInstance().setContactList(CollectionUtils.list2map(BmobDB.create(getActivity()).getContactList()));
		
		Map<String, BmobChatUser> users = MApplication.getInstance().getContactList();
		//组装新的User
		filledData(CollectionUtils.map2list(users));
		if(userAdapter == null) {
			userAdapter = new UserFriendAdapter(getActivity(), friends);
			list_friends.setAdapter(userAdapter);
		} else {
			userAdapter.notifyDataSetChanged();
		}
	}
	
	private boolean hidden;
	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		this.hidden = hidden;
		if(!hidden) {
			refresh();
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if(!hidden) {
			refresh();
		}
	}

	private void refresh() {
		try {
			getActivity().runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					queryMyfriends();
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//单击进入聊天界面
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		// TODO Auto-generated method stub
		DSUser user = (DSUser) userAdapter.getItem(position);
		//先进入好友的详细资料页面
		Intent intent =new Intent(getActivity(),ChatActivity.class);
		intent.putExtra("user", user);
		startActivity(intent);
	}

	//长按删除联系人
	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		DSUser user = (DSUser) userAdapter.getItem(arg2);
		showDeleteDilalog(user);
		return true;
	}
	
	/*
	 * 长按显示删除联系人dialog
	 */
	public void showDeleteDilalog(final DSUser user) {
		//设置dialog样式
		AlertDialog.Builder deleteDialog = new AlertDialog.Builder(mainActivity);
		deleteDialog.setTitle("删除联系人");
		deleteDialog.setMessage("确定删除联系人"+ user.getUsername() + "?");
//		deleteDialog.setCancelable(false);
		deleteDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				deleteContact(user);
			}
		});
		deleteDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				
			}
		});
		//显示对话框
		deleteDialog.show();
		deleteDialog = null;
	}
	
	/*
	 * 删除联系人
	 */
	private void deleteContact(final DSUser user) {
		final ProgressDialog progress = new ProgressDialog(getActivity());
		progress.setMessage("正在删除...");
		progress.setCanceledOnTouchOutside(false);
		progress.show();
		BmobUserManager.getInstance(mainActivity).deleteContact(user.getObjectId(), new UpdateListener() {
			
			@Override
			public void onSuccess() {
				Toast.makeText(mainActivity, "删除联系人成功", Toast.LENGTH_SHORT).show();
				//删除内存
				MApplication.getInstance().getContactList().remove(user.getUsername());
				//更新界面
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						progress.dismiss();
						userAdapter.remove(user);
					}
				});
			}
			
			@Override
			public void onFailure(int arg0, String arg1) {
				Toast.makeText(mainActivity, "删除联系人失败", Toast.LENGTH_SHORT).show();
				progress.dismiss();
			}
		});
	}

	@Override
	public void onClick(View arg0) {
		Intent intent = new Intent();
		int view = arg0.getId();
		switch (view) {
		case R.id.layout_new:	//跳转到新朋友界面
			intent.setClass(mainActivity, NewFriendActivity.class);
			startActivity(intent);
			break;

		default:
			break;
		}
	}
}