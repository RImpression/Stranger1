package com.stranger.activity;

import java.util.Iterator;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import cn.bmob.im.BmobChat;
import cn.bmob.im.BmobUserManager;
import cn.bmob.im.bean.BmobInvitation;
import cn.bmob.im.db.BmobDB;
import cn.bmob.v3.listener.UpdateListener;

import com.example.examplep.R;
import com.stranger.bean.DSUser;
import com.stranger.bean.MApplication;
import com.stranger.frags.MainFrag;
import com.stranger.shake_find.Shake;
import com.stranger.util.CollectionUtils;

/**
 * 加载Fragment
 * 
 * @author RImperssion
 * 
 */
@SuppressLint("InflateParams")
public class MainActivity extends BaseActivity implements OnClickListener {

	public static String LOGOUT_FINISH = "logout_success_finish";
	private MyBroadcastReceiver broadcastReceiver;
	private PopupWindow popupwindow;
	BmobUserManager userManager;
	DSUser user;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		userManager = BmobUserManager.getInstance(this);
		user = userManager.getCurrentUser(DSUser.class);

		BmobChat.getInstance(this).startPollService(30);
		changeFrag(MainFrag.class.getName(), R.id.container); // 加载主fragment

		AgreeInvitation();
	}

	@SuppressWarnings("static-access")
	private void AgreeInvitation() {
		if (!user.isAddreceipt()) {

			Iterator<BmobInvitation> i = BmobDB.create(this)
					.queryBmobInviteList().iterator();
			while (i.hasNext()) {
				/**
				 * 把遍历得到的PhotoItem类型对象赋值给PhotoItem类型对象temp
				 */
				final BmobInvitation msg = i.next();

				Log.i("get", msg.getTime() + "");

				BmobUserManager.getInstance(mApplication.getInstance())
						.agreeAddContact(msg, new UpdateListener() {

							@Override
							public void onSuccess() {
								BmobDB.create(mApplication.getInstance())
										.deleteInviteMsg(msg.getFromid(),
												Long.toString(msg.getTime()));
								// 保存到application中方便比较
								MApplication.getInstance().setContactList(
										CollectionUtils.list2map(BmobDB.create(
												mApplication.getInstance())
												.getContactList()));
							}

							@Override
							public void onFailure(int arg0, final String arg1) {
								ShowToast("添加失败: " + arg1);
							}
						});
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onDestroy() {
		BmobChat.getInstance(this).stopPollService();
		if (broadcastReceiver != null) {
			unregisterReceiver(broadcastReceiver);
		}
		super.onDestroy();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		Intent i = new Intent();
		// 摇一摇按钮
		Log.i("sys", id + "---------" + R.id.action_settings + "-------"
				+ R.id.action_personnal);
		if (id == R.id.action_more) {
			if (popupwindow != null && popupwindow.isShowing()) {
				popupwindow.dismiss();
				return false;
			} else {
				// 初始化并设置下窗口
				initmPopupWindowView();
				popupwindow
						.showAsDropDown(findViewById(R.id.action_more), 0, 0);
			}
		}
		// 个人资料按钮
		if (id == R.id.action_personnal) {
			finish();
			i.setClass(MainActivity.this, PersonnalInfo.class);
			i.putExtra("from", "me");
			startActivity(i);
			broadcastReceiver = new MyBroadcastReceiver();
			IntentFilter filter = new IntentFilter(LOGOUT_FINISH);
			registerReceiver(broadcastReceiver, filter);
		}
		return super.onOptionsItemSelected(item);
	}

	public void initmPopupWindowView() {

		// // 获取自定义布局文件pop.xml的视图
		View customView = getLayoutInflater().inflate(R.layout.popview_more,
				null, false);
		// 创建PopupWindow实例,200,150分别是宽度和高度
		popupwindow = new PopupWindow(customView, 380, 250);
		// 设置动画效果 [R.style.AnimationFade 是自己事先定义好的]
		popupwindow.setAnimationStyle(R.style.AnimationFade);
		// 自定义view添加触摸事件
		customView.setOnTouchListener(new OnTouchListener() {

			@SuppressLint("ClickableViewAccessibility")
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (popupwindow != null && popupwindow.isShowing()) {
					popupwindow.dismiss();
					popupwindow = null;
				}

				return false;
			}

		});

		/** 在这里可以实现自定义视图的功能 */
		ImageView iv_shake = (ImageView) customView
				.findViewById(R.id.action_shake);
		ImageView iv_set = (ImageView) customView
				.findViewById(R.id.action_settings);
		TextView tv_shake = (TextView) customView.findViewById(R.id.tv_shake);
		TextView tv_settings = (TextView) customView
				.findViewById(R.id.tv_settings);
		iv_shake.setOnClickListener(this);
		iv_set.setOnClickListener(this);
		tv_shake.setOnClickListener(this);
		tv_settings.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Intent i = new Intent();
		switch (v.getId()) {
		case R.id.action_shake:
		case R.id.tv_shake:
			finish();
			i.setClass(MainActivity.this, Shake.class);
			startActivity(i);
			break;
		case R.id.action_settings:
		case R.id.tv_settings:
			finish();
			i.setClass(MainActivity.this, SettingsActivity.class);
			startActivity(i);
			break;
		default:
			break;
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		getActionBar().setLogo(R.drawable.ic_launcher);
		getActionBar().setTitle(R.string.main_title);
		menu.findItem(R.id.action_personnal).setVisible(true);
		menu.findItem(R.id.action_more).setVisible(true);
		menu.findItem(R.id.action_edit_info).setVisible(false);
		menu.findItem(R.id.action_save_info).setVisible(false);
		menu.findItem(R.id.action_stop).setVisible(false);
		getActionBar().setDisplayHomeAsUpEnabled(false);
		getActionBar().setDisplayShowHomeEnabled(false);
		return super.onPrepareOptionsMenu(menu);
	}

	public class MyBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null && LOGOUT_FINISH.equals(intent.getAction())) {
				finish();
			}
		}
	}

	/**
	 * 重写返回，连按两次退出
	 */
	private static long backTime;

	@Override
	public void onBackPressed() {
		if (backTime + 2000 > System.currentTimeMillis()) {
			super.onBackPressed();
		} else {
			ShowToast("再按一次退出程序");
		}
		backTime = System.currentTimeMillis();
	}

}