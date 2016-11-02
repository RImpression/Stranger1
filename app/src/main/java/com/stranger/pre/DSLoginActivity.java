package com.stranger.pre;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import cn.bmob.v3.listener.SaveListener;
import com.example.examplep.R;
import com.stranger.activity.BaseActivity;
import com.stranger.activity.MainActivity;
import com.stranger.bean.DSUser;

/**
 * 登陆类
 * 
 * @author RImpression
 * 
 */
public class DSLoginActivity extends BaseActivity implements OnClickListener {
	EditText login_name, login_password;
	Button login_login, login_signup;
	Intent intent;
	public static String ACTION_FINISH = "register.success.finish";

	private MyBroadcastReceiver broadcastReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.layout_login);

		init();
	}

	@Override
	protected void onDestroy() {
		if (broadcastReceiver != null) {
			unregisterReceiver(broadcastReceiver);
		}
		super.onDestroy();
	}

	private void init() {
		login_name = (EditText) findViewById(R.id.login_name);
		login_password = (EditText) findViewById(R.id.login_password);
		login_login = (Button) findViewById(R.id.login_login);
		login_signup = (Button) findViewById(R.id.login_signup);
		login_login.setOnClickListener(this);
		login_signup.setOnClickListener(this);
	}

	public class MyBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null && ACTION_FINISH.equals(intent.getAction())) {
				finish();
			}
		}
	}

	@Override
	public void onClick(View arg0) {
		intent = new Intent();
		switch (arg0.getId()) {
		// 登陆
		case R.id.login_login:
			// 显示ProgressDialog
			final ProgressDialog progress = new ProgressDialog(
					DSLoginActivity.this);
			progress.setMessage("正在登陆...");
			progress.setCanceledOnTouchOutside(false);
			progress.show();

			String username = login_name.getText().toString();
			String password = login_password.getText().toString();
			final DSUser user = new DSUser();
			user.setUsername(username);
			user.setPassword(password);
			user.login(this, new SaveListener() {

				@Override
				public void onSuccess() {
					progress.dismiss();
					updateUserInfos();
					mApplication.setCurrentUser(user);
					intent.setClass(DSLoginActivity.this, MainActivity.class);
					startActivity(intent);
					finish();
				}

				@Override
				public void onFailure(int arg0, String arg1) {
					Toast.makeText(getApplicationContext(), "登陆失败:" + arg1,
							Toast.LENGTH_SHORT).show();
					progress.dismiss();
				}
			});

			break;

		// 注册
		case R.id.login_signup:
			intent.setClass(DSLoginActivity.this, DSSignupActivity.class);
			startActivity(intent);
			broadcastReceiver = new MyBroadcastReceiver();
			IntentFilter filter = new IntentFilter(ACTION_FINISH);
			registerReceiver(broadcastReceiver, filter);
			break;

		}
	}
}
