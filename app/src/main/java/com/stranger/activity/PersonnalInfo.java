package com.stranger.activity;

import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import cn.bmob.im.BmobChatManager;
import cn.bmob.im.BmobUserManager;
import cn.bmob.im.bean.BmobChatUser;
import cn.bmob.im.config.BmobConfig;
import cn.bmob.im.db.BmobDB;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.PushListener;

import com.example.examplep.R;
import com.stranger.bean.DSUser;
import com.stranger.bean.MApplication;
import com.stranger.pre.DSLoginActivity;
import com.stranger.util.CollectionUtils;
import com.stranger.util.ImageFileCache;

/**
 * 查看个人资料
 * 
 * @author RImpression
 * 
 */
public class PersonnalInfo extends BaseActivity implements OnClickListener {
	protected static final int LOAD_SUCCESS = 0;
	protected static final int LOAD_FAILURE = 1;
	// 依次是昵称、账号、性别、生日、星座、签名
	TextView info_nick, info_username, info_sex, info_birthday, info_interest,
			info_style;
	Button info_add_friend;// 添加好友按钮,注销按钮
	// 头像
	ImageView info_avatar;
	// BmobUserManager userManager;
	DSUser user;

	String from = "";
	String username = "";

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case LOAD_SUCCESS:
				Bitmap bitmap = (Bitmap) msg.obj;
				info_avatar.setImageBitmap(bitmap);
				break;
			case LOAD_FAILURE:
				ShowToast("頭像更新失敗");
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.personnal_info);
		from = getIntent().getStringExtra("from");// me add other
		username = getIntent().getStringExtra("username");
		Log.i("RImpression", "from的结果为：" + from);
		Log.i("RImpression", "username的结果为：" + username);
		// userManager = BmobUserManager.getInstance(this);
		initView();
	}

	private void initView() {
		info_avatar = (ImageView) findViewById(R.id.info_avatar);
		info_nick = (TextView) findViewById(R.id.info_nick);
		info_username = (TextView) findViewById(R.id.info_username);
		info_sex = (TextView) findViewById(R.id.info_sex);
		info_birthday = (TextView) findViewById(R.id.info_birthday);
		info_interest = (TextView) findViewById(R.id.info_interest);
		info_style = (TextView) findViewById(R.id.info_style);
		info_add_friend = (Button) findViewById(R.id.info_add_friend);

		if (from.equals("me")) {
			info_add_friend.setVisibility(View.GONE);
			info_interest.setOnClickListener(this);
		} else {
			if (from.equals("add")) {
				if (MApplication.getInstance().getContactList()
						.containsKey(username)) {// 是好友
					info_add_friend.setVisibility(View.GONE);
					info_interest.setOnClickListener(this);
				} else {
					info_add_friend.setVisibility(View.VISIBLE);
					info_add_friend.setOnClickListener(this);
					info_interest.setOnClickListener(this);
				}
			} else {
				if (MApplication.getInstance().getContactList()
						.containsKey(username)) {// 是好友
					info_add_friend.setVisibility(View.GONE);
					info_interest.setOnClickListener(this);
				} else {
					info_add_friend.setVisibility(View.GONE);
					info_interest.setOnClickListener(this);
				}
			}
			initOtherData(username);

		}

	}

	private void initOtherData(String name) {
		userManager.queryUser(name, new FindListener<DSUser>() {
			@Override
			public void onError(int arg0, String arg1) {
				Log.i("RImpression", arg1 + "错误详情！");
			}

			@Override
			public void onSuccess(List<DSUser> arg0) {
				if (arg0 != null && arg0.size() > 0) {
					user = arg0.get(0);
					info_add_friend.setEnabled(true);
					updateUser(user);
				} else {
					Log.i("RImpression", "成功，但查无此人");
				}

			}

		});

	}

	// 显示资料
	private void updateUser(DSUser user) {
		// 如果用户已设置头像，则显示，否则显示预设头像
		if (user.getAvatar() != null) {
			new MThread(user.getAvatar()).start();
		} else {
			info_avatar.setImageResource(R.drawable.photo_normal);
		}
		info_nick.setText(user.getNick());
		info_sex.setText(user.getSex() == true ? "男" : "女");
		info_username.setText(user.getUsername());
		info_birthday.setText(user.getBirthday());
		info_interest.setText(user.getInterest());
		info_style.setText(user.getStyle());

	}

	@Override
	protected void onResume() {
		super.onResume();
		if (from.equals("me")) {
			initMeData();
		}
	}

	// 当前用户
	private void initMeData() {
		DSUser user = userManager.getCurrentUser(DSUser.class);
		initOtherData(user.getUsername());
		updateUser(user);
	}

	class MThread extends Thread {
		String para;

		public void setPara(String para) {
			this.para = para;
		}

		public MThread(String para) {
			super();
			this.para = para;
		}

		@Override
		public void run() {
			Bitmap bitmap = ImageFileCache.getBitmap(para);
			Message msg = Message.obtain();
			msg.what = LOAD_SUCCESS;
			msg.obj = bitmap;
			handler.sendMessage(msg);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		Intent intent = new Intent();
		// 编辑资料按钮
		if (id == R.id.action_edit_info) {
			intent.setClass(PersonnalInfo.this, EditPersonnalInfo.class);
			startActivity(intent);
			finish();
		}
		if (id == android.R.id.home) {
			finish();
			Intent i = new Intent();
			i.setClass(PersonnalInfo.this, MainActivity.class);
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
		// 如果是自己，显示编辑资料按钮，否则不显示
		if (from.equals("me")) {
			menu.findItem(R.id.action_edit_info).setVisible(true);
			getActionBar().setTitle(R.string.personnalInfo_me);
		} else {
			menu.findItem(R.id.action_edit_info).setVisible(false);
			getActionBar().setTitle(R.string.personnalInfo_other);
		}
		getActionBar().setIcon(null);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setDisplayShowHomeEnabled(false);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void onClick(View arg0) {
		int view = arg0.getId();
		switch (view) {
		case R.id.info_add_friend:
			addFriend();
			break;
		case R.id.info_interest:
			if (from.equals("me")) {
				user = userManager.getCurrentUser(DSUser.class);
				IntentToConstellationActivity();
			} else {
				if (from.equals("add")) {
					IntentToConstelCompActivity();
				} else {
					if (MApplication.getInstance().getContactList()
							.containsKey(username)) {// 是好友
						IntentToConstelCompActivity();
					} else {
						IntentToConstellationActivity();
					}
				}
			}
			break;
		default:
			break;
		}
	}

	private void IntentToConstellationActivity() {
		finish();
		Intent i = new Intent();
		i.setClass(this, ConstellationActivity.class);
		i.putExtra("interest", user.getInterest());
		startActivity(i);
	}

	private void IntentToConstelCompActivity() {
		finish();
		Intent i = new Intent();
		i.setClass(this, Constell_CompActivity.class);
		i.putExtra("interest", user.getInterest());
		i.putExtra("username", username);
		startActivity(i);
	}

	// 添加好友，发送添加tag请求
	private void addFriend() {
		final ProgressDialog progress = new ProgressDialog(this);
		progress.setMessage("正在添加...");
		progress.setCanceledOnTouchOutside(false);
		progress.show();
		// 发送添加好友的tag请求
		BmobChatManager.getInstance(mApplication).sendTagMessage(
				BmobConfig.TAG_ADD_CONTACT, user.getObjectId(),
				new PushListener() {
					@SuppressWarnings("static-access")
					@Override
					public void onSuccess() {
						progress.dismiss();
						/*
						 * 设置验证
						 */
						if (!user.isAddreceipt()) {
							BmobUserManager.getInstance(
									mApplication.getInstance())
									.addContactAfterAgree(username,
											new FindListener<BmobChatUser>() {

												@Override
												public void onError(int arg0,
														final String arg1) {

												}

												@Override
												public void onSuccess(
														List<BmobChatUser> arg0) {
													// 保存到内存中
													MApplication
															.getInstance()
															.setContactList(
																	CollectionUtils
																			.list2map(BmobDB
																					.create(mApplication
																							.getInstance())
																					.getContactList()));
												}
											});
						}
						turnToMain();
					}

					@Override
					public void onFailure(int arg0, String arg1) {
						progress.dismiss();
						ShowToast("添加好友失败：" + arg1);
					}
				});
	}

	private void turnToMain() {

		AlertDialog.Builder builder = new AlertDialog.Builder(
				PersonnalInfo.this);
		builder.setIcon(R.drawable.login_top_icon);// 窗口头图标
		builder.setTitle("添加好友成功！");// 窗口名
		if (!user.isAddreceipt()) {
			builder.setMessage("是否返回主界面？ ");
		} else {
			builder.setMessage("等待对方验证！是否返回主界面？ ");
		}
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				finish();
				Intent i = new Intent();
				i.setClass(PersonnalInfo.this, MainActivity.class);
				startActivity(i);
			}
		});
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		AlertDialog overdialog = builder.create();
		// 设置点击ALertDialog以外的位置不产生响应
		overdialog.setCanceledOnTouchOutside(false);
		overdialog.show();
	}

}
