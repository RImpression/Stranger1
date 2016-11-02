package com.stranger.game;

import java.util.List;

import cn.bmob.im.BmobUserManager;
import cn.bmob.v3.listener.FindListener;

import com.example.examplep.R;
import com.stranger.activity.MainActivity;
import com.stranger.activity.PersonnalInfo;
import com.stranger.bean.DSUser;
import com.stranger.map.LocationAct;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 对SeeColor整体界面的设置以及页面跳转的设置
 * 
 * @author lake
 * 
 */
@SuppressLint("ClickableViewAccessibility")
public class SeeColorMain extends Activity {

	int level, width, height, intTime, score, state;

	// SeeColor类对象
	SeeColor see = null;

	RelativeLayout game;
	TextView timetv, gettimetv, scoretv, getscoretv;

	public static final int PLAYING = 0;
	public static final int PAUSING = 1;

	private String username = "";

	BmobUserManager userManager;
	DSUser user;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game);

		userManager = BmobUserManager.getInstance(this);
		username = getIntent().getStringExtra("username");

		game = (RelativeLayout) findViewById(R.id.seeColorgame);
		timetv = (TextView) findViewById(R.id.time);
		gettimetv = (TextView) findViewById(R.id.gettime);
		scoretv = (TextView) findViewById(R.id.score);
		getscoretv = (TextView) findViewById(R.id.getscore);
		level = 2;
		getUser(username);
		init();
	}

	/*
	 * 根据用户名查询用户
	 */
	private void getUser(String name) {
		userManager.queryUser(name, new FindListener<DSUser>() {
			@Override
			public void onError(int arg0, String arg1) {
				level = 2;
				Log.i("RImpression", arg1 + "错误详情！");
			}

			@Override
			public void onSuccess(List<DSUser> dsuser) {
				if (dsuser != null && dsuser.size() > 0) {
					user = dsuser.get(0);

					if (user.getGamelevel() == null
							|| user.getGamelevel().equals("")) {
						level = 2;
					} else if (user.getGamelevel().equals("简单")) {
						level = 2;
					} else if (user.getGamelevel().equals("普通")) {
						level = 6;
					} else if (user.getGamelevel().equals("困难")) {
						level = 9;
					}
				} else {
					level = 2;
					Log.i("RImpression", "成功，但查无此人");
				}

			}
		});
	}

	/*
	 * 初始化游戏
	 */
	private void init() {
		game.removeAllViews();
		width = SeeColorMain.this.getWindow().getWindowManager()
				.getDefaultDisplay().getWidth();
		height = SeeColorMain.this.getWindow().getWindowManager()
				.getDefaultDisplay().getWidth();

		intTime = 5;
		score = 0;

		getscoretv.setText("  " + score);

		see = new SeeColor(this, height, width, level);
		see.setOnTouchListener(new MyListener());
		game.addView(see);

		// 初始化游戏线程
		beginTip();
	}

	// 开始游戏提示窗口
	private void beginTip() {
		// 弹窗初始化
		AlertDialog.Builder builder = new AlertDialog.Builder(SeeColorMain.this);
		builder.setIcon(R.drawable.login_top_icon);// 窗口头图标
		builder.setTitle("游戏规则");// 窗口名
		builder.setMessage("在所有色块中找出颜色不同的一块并点击，色块数会随着等级升高而不断增加。"
				+ "在规定时间内等级超过10就通过游戏啦~~快来挑战吧！ ");
		builder.setPositiveButton("立即开始",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						beginGame();
					}
				});
		AlertDialog dialog = builder.create();
		// 设置点击ALertDialog以外的位置不产生响应
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
	}

	// 返回键按下时
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			switch (state) {
			case PLAYING:
				pauseGame();
				stopMenu();
				break;
			case PAUSING:
				beginGame();
				break;
			}
		}
		return false;
	}

	// 暂停后的弹窗
	public void stopMenu() {

		// 弹窗初始化
		AlertDialog.Builder builder = new AlertDialog.Builder(SeeColorMain.this);
		builder.setIcon(R.drawable.login_top_icon);// 窗口头图标
		builder.setTitle("提示");// 窗口名
		builder.setMessage("游戏暂停中 ");
		builder.setPositiveButton("继续", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				beginGame();
			}
		});
		builder.setNeutralButton("重新开始", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				resumeGame();
			}
		});
		builder.setNegativeButton("返回上一界面",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						TurnToLocation();
					}
				});
		AlertDialog dialog = builder.create();
		// 设置点击ALertDialog以外的位置不产生响应
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();

	}

	// 图块点击响应
	class MyListener implements OnTouchListener {
		float x, y, d;

		public boolean onTouch(View arg0, MotionEvent m) {
			x = m.getX();
			y = m.getY();
			if (x > see.target_lX && x < see.target_rX && y > see.target_uY
					&& y < see.target_dY) {
				score++;
				getscoretv.setText("  " + score);
				level++;
				see.level = SeeColorTool.getLevel(level);
				see.invalidate();
			}
			return false;
		}
	}

	// 时间线程处理
	Handler h = new Handler();
	Runnable r = new Runnable() {
		public void run() {
			if (intTime >= 0 && state == PLAYING) {
				gettimetv.setText("  " + intTime + " s");
				intTime--;
				h.postDelayed(r, 1000);
			} else {
				if (score >= 0) {
					gamesuccessOver();
				} else {
					gamefailOver();
				}
			}
		}
	};

	public void beginGame() {
		h.post(r);
		state = PLAYING;
	}

	public void pauseGame() {
		h.removeCallbacks(r);
		state = PAUSING;
	}

	public void resumeGame() {
		init();
	}

	// 游戏结束
	public void gamesuccessOver() {

		h.removeCallbacks(r);

		// 弹窗初始化
		AlertDialog.Builder builder = new AlertDialog.Builder(SeeColorMain.this);
		builder.setIcon(R.drawable.login_top_icon);// 窗口头图标
		builder.setTitle("恭喜你过关");// 窗口名
		builder.setMessage("是否查看好友信息？ ");
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// add friend
				finish();
				Intent intent = new Intent();
				intent.setClass(SeeColorMain.this, PersonnalInfo.class);
				intent.putExtra("from", "add");
				intent.putExtra("username", username);
				startActivity(intent);
			}
		});
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				TurnToLocation();
			}
		});
		AlertDialog overdialog = builder.create();
		// 设置点击ALertDialog以外的位置不产生响应
		overdialog.setCanceledOnTouchOutside(false);
		overdialog.show();
	}

	public void gamefailOver() {
		// 移除线程
		h.removeCallbacks(r);
		// 弹窗初始化
		AlertDialog.Builder builder = new AlertDialog.Builder(SeeColorMain.this);
		builder.setIcon(R.drawable.login_top_icon);// 窗口头图标
		builder.setTitle("通关失败");// 窗口名
		builder.setMessage("是否重新开始？ ");
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				init();
			}
		});
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				TurnToLocation();
			}
		});
		AlertDialog overdialog = builder.create();
		// 设置点击ALertDialog以外的位置不产生响应
		overdialog.setCanceledOnTouchOutside(false);
		overdialog.show();
	}

	public void TurnToLocation() {
		Intent i = new Intent();
		i.setClass(SeeColorMain.this, LocationAct.class);
		startActivity(i);
		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.action_personnal).setVisible(false);
		menu.findItem(R.id.action_more).setVisible(false);
		menu.findItem(R.id.action_save_info).setVisible(false);
		menu.findItem(R.id.action_edit_info).setVisible(false);
		menu.findItem(R.id.action_stop).setVisible(true);
		getActionBar().setIcon(null);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setDisplayShowHomeEnabled(false);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {
			finish();
			Intent i = new Intent();
			i.setClass(SeeColorMain.this, LocationAct.class);
			startActivity(i);
		}
		if (id == R.id.action_stop) {
			pauseGame();
			stopMenu();
		}
		return super.onOptionsItemSelected(item);
	}

}
