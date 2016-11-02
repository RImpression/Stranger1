package com.stranger.activity;

import java.io.DataInputStream;
import java.io.InputStream;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import cn.bmob.im.BmobUserManager;

import com.example.examplep.R;
import com.stranger.bean.DSUser;

public class Constell_CompActivity extends Activity {

	private TextView tv_name_me, tv_name_he, tv_both, tv_long, tv_comp,
			tv_friendship, tv_love, tv_marry, tv_family, tv_word, rb_word,
			tv_special, rb_special;
	private RatingBar rb_both, rb_long, rb_friendship, rb_love, rb_marry,
			rb_family;
	private ImageView iv_star_me, iv_exchange, iv_star_he;

	BmobUserManager userManager;
	DSUser user;
	String username;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_constell_comp);
		initView();
		userManager = BmobUserManager.getInstance(this);

		DSUser user = userManager.getCurrentUser(DSUser.class);
		String star_me = user.getInterest();
		String star_he = getIntent().getStringExtra("interest");
		username = getIntent().getStringExtra("username");

		tv_name_me.setText(star_me);
		tv_name_he.setText(star_he);
		setImage(iv_star_me, star_me);
		setImage(iv_star_he, star_he);
		iv_exchange.setImageResource(R.drawable.iv_exchange);

		int id_me = selectId(star_me);
		int id_he = selectId(star_he);

		ReadStar(this, "star1.dat", id_me, id_he);
		ReadWord1(this, "star2.dat", id_he);
	}

	// 初始化控件
	private void initView() {
		tv_name_me = (TextView) findViewById(R.id.tv_name_me);
		tv_name_he = (TextView) findViewById(R.id.tv_name_he);
		tv_both = (TextView) findViewById(R.id.tv_both);
		tv_long = (TextView) findViewById(R.id.tv_long);
		tv_comp = (TextView) findViewById(R.id.tv_comp);
		tv_friendship = (TextView) findViewById(R.id.tv_friendship);
		tv_love = (TextView) findViewById(R.id.tv_love);
		tv_marry = (TextView) findViewById(R.id.tv_marry);
		tv_family = (TextView) findViewById(R.id.tv_family);
		tv_word = (TextView) findViewById(R.id.tv_word);
		tv_special = (TextView) findViewById(R.id.tv_special);
		rb_special = (TextView) findViewById(R.id.rb_special);
		rb_word = (TextView) findViewById(R.id.rb_word);
		rb_both = (RatingBar) findViewById(R.id.rb_both);
		rb_love = (RatingBar) findViewById(R.id.rb_love);
		rb_long = (RatingBar) findViewById(R.id.rb_long);
		rb_friendship = (RatingBar) findViewById(R.id.rb_friendship);
		rb_love = (RatingBar) findViewById(R.id.rb_love);
		rb_marry = (RatingBar) findViewById(R.id.rb_marry);
		rb_family = (RatingBar) findViewById(R.id.rb_family);
		iv_star_me = (ImageView) findViewById(R.id.iv_star_me);
		iv_exchange = (ImageView) findViewById(R.id.iv_exchange);
		iv_star_he = (ImageView) findViewById(R.id.iv_star_he);
	}

	/**
	 * 将名字转换为id
	 * 
	 * @param star
	 * @return
	 */
	private int selectId(String star) {
		if (star.equals("白羊座")) {
			return 0;
		} else if (star.equals("金牛座")) {
			return 1;
		} else if (star.equals("双子座")) {
			return 2;
		} else if (star.equals("巨蟹座")) {
			return 3;
		} else if (star.equals("狮子座")) {
			return 4;
		} else if (star.equals("处女座")) {
			return 5;
		} else if (star.equals("天秤座")) {
			return 6;
		} else if (star.equals("天蝎座")) {
			return 7;
		} else if (star.equals("射手座")) {
			return 8;
		} else if (star.equals("魔蝎座")) {
			return 9;
		} else if (star.equals("水瓶座")) {
			return 10;
		} else if (star.equals("双鱼座")) {
			return 11;
		}
		return 0;
	}

	/**
	 * 读取星座速配信息
	 * 
	 * @param context
	 * @param path
	 * @return
	 */
	private void ReadStar(Context context, String path, int id_me, int id_he) {
		int star[][][] = null;
		try {
			InputStream in = context.getResources().getAssets().open(path);
			DataInputStream dis = new DataInputStream(in);

			int x = dis.readInt();
			int y = dis.readInt();
			int z = dis.readInt();

			star = new int[x][y][z];

			for (int i = 0; i < star.length; i++) {
				for (int j = 0; j < star[i].length; j++) {
					for (int k = 0; k < star[i][j].length; k++) {
						star[i][j][k] = dis.readInt();
						if (id_me == i && id_he == j) {
							switch (k) {
							case 0:
								rb_both.setRating(star[i][j][k]);
								break;
							case 1:
								rb_long.setRating(star[i][j][k]);
								break;
							case 2:
								rb_friendship.setRating(star[i][j][k]);
								break;
							case 3:
								rb_love.setRating(star[i][j][k]);
								break;
							case 4:
								rb_marry.setRating(star[i][j][k]);
								break;
							case 5:
								rb_family.setRating(star[i][j][k]);
								break;
							default:
								break;
							}
						}
						// Log.i("sys", star[i][j][k] + "");
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 读取文本信息生成星座性格信息(其中为文本String数组)
	 * 
	 * @param context
	 * @param path
	 * @return
	 */
	private void ReadWord1(Context context, String path, int id_he) {
		String map[][][] = null;
		try {
			InputStream in = context.getResources().getAssets().open(path);
			DataInputStream dis = new DataInputStream(in);

			int x = dis.readInt();
			int y = dis.readInt();

			map = new String[x][y][1];

			for (int i = 0; i < 2; i++) {
				for (int j = 0; j < 12; j++) {
					map[i][j][0] = dis.readUTF();
					if (j == id_he) {
						if (i == 1) {
							rb_special.setText(map[i][j][0]);
						}
						if (i == 0) {
							rb_word.setText(map[i][j][0]);
						}
					}
				}

			}

			dis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 根据星座选择图片
	 * 
	 * @param star
	 */
	private void setImage(ImageView iv_star, String star) {
		if (star.equals("白羊座")) {
			iv_star.setImageDrawable(getResources().getDrawable(
					R.drawable.constellation1));
		} else if (star.equals("金牛座")) {
			iv_star.setImageDrawable(getResources().getDrawable(
					R.drawable.constellation2));
		} else if (star.equals("双子座")) {
			iv_star.setImageDrawable(getResources().getDrawable(
					R.drawable.constellation3));
		} else if (star.equals("巨蟹座")) {
			iv_star.setImageDrawable(getResources().getDrawable(
					R.drawable.constellation4));
		} else if (star.equals("狮子座")) {
			iv_star.setImageDrawable(getResources().getDrawable(
					R.drawable.constellation5));
		} else if (star.equals("处女座")) {
			iv_star.setImageDrawable(getResources().getDrawable(
					R.drawable.constellation6));
		} else if (star.equals("天秤座")) {
			iv_star.setImageDrawable(getResources().getDrawable(
					R.drawable.constellation7));
		} else if (star.equals("天蝎座")) {
			iv_star.setImageDrawable(getResources().getDrawable(
					R.drawable.constellation8));
		} else if (star.equals("射手座")) {
			iv_star.setImageDrawable(getResources().getDrawable(
					R.drawable.constellation9));
		} else if (star.equals("魔蝎座")) {
			iv_star.setImageDrawable(getResources().getDrawable(
					R.drawable.constellation10));
		} else if (star.equals("水瓶座")) {
			iv_star.setImageDrawable(getResources().getDrawable(
					R.drawable.constellation11));
		} else if (star.equals("双鱼座")) {
			iv_star.setImageDrawable(getResources().getDrawable(
					R.drawable.constellation12));
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if (id == android.R.id.home) {
			finish();
			Intent i = new Intent();
			i.putExtra("from", "add");
			i.putExtra("username", username);
			i.setClass(Constell_CompActivity.this, PersonnalInfo.class);
			startActivity(i);
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		getActionBar().setTitle(R.string.star_he);
		getActionBar().setIcon(null);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setDisplayShowHomeEnabled(false);
		return super.onPrepareOptionsMenu(menu);
	}

}
