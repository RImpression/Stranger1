package com.stranger.bean;

import java.util.HashMap;
import java.util.Map;

import android.app.Application;
import cn.bmob.im.BmobUserManager;
import cn.bmob.im.bean.BmobChatUser;

import com.stranger.util.SharePreferenceUtil;

public class MApplication extends Application {
	private DSUser currentUser;
	public static MApplication mInstance;

	public DSUser getCurrentUser() {
		return currentUser;
	}

	public void setCurrentUser(DSUser currentUser) {
		this.currentUser = currentUser;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		//注册Bmob SDK
		mInstance = this;
	}
	
	public static MApplication getInstance() {
		return mInstance;
	}
	
	// 单例模式，才能及时返回数据
		SharePreferenceUtil mSpUtil;
		public static final String PREFERENCE_NAME = "_sharedinfo";

		public synchronized SharePreferenceUtil getSpUtil() {
			if (mSpUtil == null) {
				String currentId = BmobUserManager.getInstance(
						getApplicationContext()).getCurrentUserObjectId();
				String sharedName = currentId + PREFERENCE_NAME;
				mSpUtil = new SharePreferenceUtil(this, sharedName);
			}
			return mSpUtil;
		}
	
	private Map<String, BmobChatUser> contactList = new HashMap<String, BmobChatUser>();

	/**
	 * 获取内存中好友user list
	 * 
	 * @return
	 */
	public Map<String, BmobChatUser> getContactList() {
		return contactList;
	}

	/**
	 * 设置好友user list到内存中
	 * @param contactList
	 */
	public void setContactList(Map<String, BmobChatUser> contactList) {
		if (this.contactList != null) {
			this.contactList.clear();
		}
		this.contactList = contactList;
	}

	/**
	 * 退出登录，清空缓存数据
	 */
	public void logout() {
		BmobUserManager.getInstance(getApplicationContext()).logout();
		setContactList(null);
	}
	
}