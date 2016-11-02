package com.stranger.activity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.examplep.R;

public class ConstellationActivity extends Activity {

	private TextView tv_name, tv_date, tv_all, tv_love, tv_work, tv_money,
			tv_health, tv_talk, tv_color, tv_num, tv_star, tv_charater,
			rb_health, rb_talk, rb_color, rb_num, rb_star, rb_charater;
	private RatingBar rb_all, rb_love, rb_work, rb_money;
	private ImageView iv_star;

	String text = "";

	@SuppressLint("HandlerLeak")
	final private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				String data = (String) msg.obj;
				Log.i("get", data + "----------------------------------------");
				if (data == null) {
					setProgressBarIndeterminateVisibility(true);
				} else {
					Json_parser(data);
					setProgressBarIndeterminateVisibility(false);
				}
				break;
			default:
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_constellation);
		initView();
		// 选择星座id
		String star = getIntent().getStringExtra("interest");

		String id = selectId(star);

		setImage(star);

		setProgressBarIndeterminateVisibility(true);

		new MThread(id).start();

	}

	// 初始化控件
	private void initView() {
		tv_name = (TextView) findViewById(R.id.tv_name);
		tv_date = (TextView) findViewById(R.id.tv_date);
		tv_all = (TextView) findViewById(R.id.tv_all);
		tv_love = (TextView) findViewById(R.id.tv_love);
		tv_work = (TextView) findViewById(R.id.tv_work);
		tv_money = (TextView) findViewById(R.id.tv_money);
		tv_health = (TextView) findViewById(R.id.tv_health);
		tv_talk = (TextView) findViewById(R.id.tv_talk);
		tv_color = (TextView) findViewById(R.id.tv_color);
		tv_num = (TextView) findViewById(R.id.tv_num);
		tv_star = (TextView) findViewById(R.id.tv_star);
		tv_charater = (TextView) findViewById(R.id.tv_charater);
		rb_health = (TextView) findViewById(R.id.rb_health);
		rb_talk = (TextView) findViewById(R.id.rb_talk);
		rb_color = (TextView) findViewById(R.id.rb_color);
		rb_num = (TextView) findViewById(R.id.rb_num);
		rb_star = (TextView) findViewById(R.id.rb_star);
		rb_charater = (TextView) findViewById(R.id.rb_charater);
		rb_all = (RatingBar) findViewById(R.id.rb_all);
		rb_love = (RatingBar) findViewById(R.id.rb_love);
		rb_work = (RatingBar) findViewById(R.id.rb_work);
		rb_money = (RatingBar) findViewById(R.id.rb_money);
		iv_star = (ImageView) findViewById(R.id.iv_star);
	}

	/**
	 * 根据星座选择图片
	 * 
	 * @param star
	 */
	private void setImage(String star) {
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

	/**
	 * 将名字转换为id
	 * 
	 * @param star
	 * @return
	 */
	private String selectId(String star) {
		if (star.equals("白羊座")) {
			return "0";
		} else if (star.equals("金牛座")) {
			return "1";
		} else if (star.equals("双子座")) {
			return "2";
		} else if (star.equals("巨蟹座")) {
			return "3";
		} else if (star.equals("狮子座")) {
			return "4";
		} else if (star.equals("处女座")) {
			return "5";
		} else if (star.equals("天秤座")) {
			return "6";
		} else if (star.equals("天蝎座")) {
			return "7";
		} else if (star.equals("射手座")) {
			return "8";
		} else if (star.equals("魔蝎座")) {
			return "9";
		} else if (star.equals("水瓶座")) {
			return "10";
		} else if (star.equals("双鱼座")) {
			return "11";
		}
		return null;
	}

	/**
	 * 获取星座运势信息
	 * 
	 * @author Administrator
	 * 
	 */
	class MThread extends Thread {
		String id;

		public void setPara(String id) {
			this.id = id;
		}

		public MThread(String id) {
			super();
			this.id = id;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			String url = "http://api.uihoo.com/astro/astro.http.php?fun=day&id="
					+ id + "&format=json";
			try {
				// 通过URL取得接口的链接，并获得网页的内容
				URLConnection urltext = new URL(url).openConnection();
				// 将网页内容写入输入流
				InputStream in = urltext.getInputStream();
				// 读取输入流的数据
				BufferedReader br = new BufferedReader(new InputStreamReader(
						in, "utf-8"));
				String line;
				// StringBuffer是字符串变量，它的对象是可以扩充和修改的
				StringBuffer pageBuffer = new StringBuffer();
				while ((line = br.readLine()) != null) {
					pageBuffer.append(line);
				}

				// 对获得的数据进行解析，将乱码转化为中文
				text = pageBuffer.toString();

				br.close();
				in.close();

				// 需要数据传递
				mHandler.sendEmptyMessage(0);
				Message msg = new Message();
				msg.obj = text;
				mHandler.sendMessage(msg);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}

	/**
	 * 解析星座运势的json格式
	 * 
	 * @param response
	 */
	private void Json_parser(String response) {
		try {
			JSONArray jsonArray = new JSONArray(response);
			int length = jsonArray.length();

			int i = 0;

			while (i < 4) {
				JSONObject jsonObject = (JSONObject) jsonArray.get(i);
				String title = jsonObject.getString("title");
				int rank = jsonObject.getInt("rank");
				switch (i) {
				case 0:
					tv_all.setText(title);
					rb_all.setRating(rank);
					break;
				case 1:
					tv_love.setText(title);
					rb_love.setRating(rank);
					break;
				case 2:
					tv_work.setText(title);
					rb_work.setRating(rank);
					break;
				case 3:
					tv_money.setText(title);
					rb_money.setRating(rank);
					break;
				default:
					break;
				}

				Log.i("lake", title + rank);
				i++;
			}

			while (i < 10) {
				JSONObject jsonObject = (JSONObject) jsonArray.get(i);
				String title = jsonObject.getString("title");
				String value = jsonObject.getString("value");
				switch (i) {
				case 4:
					tv_health.setText(title);
					rb_health.setText(value);
					break;
				case 5:
					tv_talk.setText(title);
					rb_talk.setText(value);
					break;
				case 6:
					tv_color.setText(title);
					rb_color.setText(value);
					break;
				case 7:
					tv_num.setText(title);
					rb_num.setText(value);
					break;
				case 8:
					tv_star.setText(title);
					rb_star.setText(value);
					break;
				case 9:
					tv_charater.setText(title);
					rb_charater.setText(value);
					break;
				default:
					break;
				}

				Log.i("lake", title + value);
				i++;
			}

			while (i < 11) {
				JSONObject jsonObject = (JSONObject) jsonArray.get(i);
				String enName = jsonObject.getString("en");
				String chName = jsonObject.getString("cn");
				tv_name.setText(chName + " 今日运势");

				Log.i("lake", enName);
				Log.i("lake", chName);
				i++;
			}

			while (i < length) {
				String date = (String) jsonArray.get(i);
				tv_date.setText(date);

				Log.i("lake", date);
				i++;
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if (id == android.R.id.home) {
			finish();
			Intent i = new Intent();
			i.putExtra("from", "me");
			i.setClass(ConstellationActivity.this, PersonnalInfo.class);
			startActivity(i);
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		getActionBar().setTitle(R.string.star_mine);
		getActionBar().setIcon(null);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setDisplayShowHomeEnabled(false);
		return super.onPrepareOptionsMenu(menu);
	}

}
