package com.stranger.pre;


import cn.bmob.im.BmobUserManager;

import com.example.examplep.R;
import com.stranger.activity.BaseActivity;
import com.stranger.activity.MainActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
/**
 * 启动欢迎页，淡入淡出效果2.5S
 * @author RImpression
 *
 */
public class WelcomeActivity extends BaseActivity implements AnimationListener {
	private ImageView imageView = null;
	private Animation alphaAnimation = null;
	BmobUserManager userManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//屏蔽标题栏
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_welcome);
		userManager = BmobUserManager.getInstance(this);
		imageView = (ImageView) findViewById(R.id.welcome_image_view);
		alphaAnimation = AnimationUtils.loadAnimation(this, R.anim.welcome_alpha);
		//启动Fill保持
		alphaAnimation.setFillEnabled(true);	
		//设置动画的最后一帧是保持在View上面
		alphaAnimation.setFillAfter(true);		
		imageView.setAnimation(alphaAnimation);
		//为动画设置监听
		alphaAnimation.setAnimationListener(this); 
	}

	@Override
	public void onAnimationEnd(Animation arg0) {
		//动画结束时结束欢迎界面并转到软件的主界面
		//Intent intent = new Intent(this,DSLoginActivity.class);
		//startActivity(intent);
		this.finish();
	}

	@Override
	public void onAnimationRepeat(Animation arg0) {
		
	}

	@Override
	public void onAnimationStart(Animation arg0) {
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		//用户存在是自动登陆
		if(userManager.getCurrentUser() != null) {
			updateUserInfos();
			handler.sendEmptyMessageDelayed(100, 2000);
		} else {
			handler.sendEmptyMessageDelayed(200, 2000);
		}
	}
	
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			Intent intent = new Intent();
			switch (msg.what) {
			case 100:
				startActivity(intent.setClass(mApplication, MainActivity.class));
				finish();
				break;

			case 200:
				startActivity(intent.setClass(mApplication, DSLoginActivity.class));
				finish();
				break;
			}
		}
	};
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		//在欢迎界面屏蔽BACK键
		if(keyCode==KeyEvent.KEYCODE_BACK) {
			return false;
		}
		return false;
	}

}
