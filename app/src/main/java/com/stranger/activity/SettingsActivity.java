package com.stranger.activity;

import java.util.Iterator;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import cn.bmob.im.BmobUserManager;
import cn.bmob.im.bean.BmobInvitation;
import cn.bmob.im.db.BmobDB;
import cn.bmob.v3.listener.UpdateListener;

import com.example.examplep.R;
import com.stranger.adapter.OnToggleStateChangeListener;
import com.stranger.bean.DSUser;
import com.stranger.bean.MApplication;
import com.stranger.pre.DSLoginActivity;
import com.stranger.view.SlipButton;

public class SettingsActivity extends BaseActivity implements OnClickListener,
		OnToggleStateChangeListener {

	BmobUserManager userManager;
	DSUser user;
	private PopupWindow popupwindow;
	SlipButton slideButton;
	Button info_cancellation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		TextView tv_gamelevel = (TextView) findViewById(R.id.tv_gamelevel);
		tv_gamelevel.setOnClickListener(this);
		userManager = BmobUserManager.getInstance(this);
		// 获取当前用户
		user = userManager.getCurrentUser(DSUser.class);

		slideButton = (SlipButton) findViewById(R.id.slidebutton_add);
		slideButton.setOnToggleStateChangeListener(this);

		if (user.isAddreceipt()) {
			slideButton.setToggleState(true);
		} else {
			slideButton.setToggleState(false);
		}
		
		info_cancellation = (Button) findViewById(R.id.info_cancellation);
		info_cancellation.setOnClickListener(this);
	}

	@SuppressWarnings("static-access")
	@Override
	public void onToggleStateChange(boolean b) {
		// TODO Auto-generated method stub
		if (b) {
			Toast.makeText(SettingsActivity.this, "开关打开", Toast.LENGTH_SHORT)
					.show();
			slideButton.setToggleState(true);
			user.setAddreceipt(true);
		} else {
			Toast.makeText(SettingsActivity.this, "开关关闭", Toast.LENGTH_SHORT)
					.show();
			slideButton.setToggleState(false);
			user.setAddreceipt(false);
			Iterator<BmobInvitation> i = BmobDB.create(this)
					.queryBmobInviteList().iterator();
			while (i.hasNext()) {
				/**
				 * 把遍历得到的PhotoItem类型对象赋值给PhotoItem类型对象temp
				 */
				final BmobInvitation msg = i.next();
				BmobDB.create(mApplication.getInstance()).deleteInviteMsg(
						msg.getFromid(), Long.toString(msg.getTime()));
			}
		}
		user.update(SettingsActivity.this, new UpdateListener() {

			@Override
			public void onSuccess() {
			}

			@Override
			public void onFailure(int arg0, String arg1) {
				ShowToast("设置失败：" + arg1);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	// 初始化下拉窗口
	@SuppressLint("InflateParams")
	public void initmPopupWindowView() {

		// // 获取自定义布局文件pop.xml的视图
		final View customView = getLayoutInflater().inflate(
				R.layout.popview_settings, null, false);
		// 创建PopupWindow实例,200,150分别是宽度和高度
		popupwindow = new PopupWindow(customView, 300, 300);
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

		RadioGroup choose_gamelevel = (RadioGroup) customView
				.findViewById(R.id.choose_gamelevel);
		choose_gamelevel
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(RadioGroup group, int arg1) {
						// TODO Auto-generated method stub
						// 获取变更后的选中项的ID
						int radioButtonId = group.getCheckedRadioButtonId();
						// 根据ID获取RadioButton的实例
						RadioButton rb = (RadioButton) customView
								.findViewById(radioButtonId);
						// Log.i("lake", rb.getText() + "");
						user.setGamelevel(rb.getText().toString());
						user.update(SettingsActivity.this,
								new UpdateListener() {

									@Override
									public void onSuccess() {
										ShowToast("设置成功！");
										// 跳转回查看个人资料页
										popupwindow.dismiss();
									}

									@Override
									public void onFailure(int arg0, String arg1) {
										ShowToast("设置失败：" + arg1);
									}
								});
					}
				});
	}

	@SuppressLint("NewApi")
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.info_cancellation:
			// 发送广播通知主页退出
			sendBroadcast(new Intent(MainActivity.LOGOUT_FINISH));
			logout();
			break;
		case R.id.tv_gamelevel:
			if (popupwindow != null && popupwindow.isShowing()) {
				popupwindow.dismiss();
			} else {
				// 初始化并设置下窗口
				initmPopupWindowView();

				popupwindow.showAsDropDown(findViewById(R.id.tv_gamelevel), 0,
						0, Gravity.RIGHT);
			}
			break;
		default:
			break;
		}
	}
	
	// 注销登陆
		private void logout() {
			MApplication.getInstance().logout();
			finish();
			startActivity(new Intent(SettingsActivity.this, DSLoginActivity.class));
		}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {
			finish();
			Intent i = new Intent();
			i.setClass(SettingsActivity.this, MainActivity.class);
			startActivity(i);
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.action_personnal).setVisible(false);
		menu.findItem(R.id.action_more).setVisible(false);
		menu.findItem(R.id.action_save_info).setVisible(false);
		menu.findItem(R.id.action_stop).setVisible(false);
		menu.findItem(R.id.action_edit_info).setVisible(false);
		getActionBar().setIcon(R.drawable.ic_launcher);
		getActionBar().setTitle(R.string.h_setting);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setDisplayShowHomeEnabled(false);
		return super.onPrepareOptionsMenu(menu);
	}

}
