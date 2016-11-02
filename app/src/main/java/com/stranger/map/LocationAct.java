package com.stranger.map;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import cn.bmob.im.BmobUserManager;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.example.examplep.R;
import com.stranger.activity.BaseActivity;
import com.stranger.bean.DSUser;
import com.stranger.game.SeeColorMain;
import com.stranger.shake_find.Shake;
import com.stranger.sqlite.SqliteCommand;

/**
 * map定位类，用于定位用户当前所在的位置，并将该位置在地图上显示
 * 
 * @author lake
 * 
 */
public class LocationAct extends BaseActivity {

	// 定位服务的客户端
	LocationClient mlocaClient;

	public MyLocationListenner myListener = new MyLocationListenner();

	MapView mapView;
	// 百度地图显示
	BaiduMap baiduMap;

	// 是否首次定位
	boolean isFirstLoc = true;

	// 声明并构造数据库
	SqliteCommand sd = new SqliteCommand(LocationAct.this, "Stranger_Talking");

	BmobUserManager userManager;
	// 定位地址的id
	String id;
	int searchnum;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		sd.InsertTb("定位", 23.444423, 114.232354);
		// 注册SDK这一句非常重要，否则无法连接服务key
		SDKInitializer.initialize(this.getApplication());

		setContentView(R.layout.activity_location);

		/*
		 * // 将当前地图的模式设置为普通模式 currentMode = LocationMode.COMPASS; currentMarker
		 * = null;
		 * 
		 * // 设置定位图层显示方式：currentMode(定位图层显示方式), true(是否允许显示方向信息), //
		 * currentMarker(用户定位图标)
		 * 
		 * baiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
		 * LocationMode.NORMAL, true, null));
		 */

		// 地图初始化
		mapView = (MapView) findViewById(R.id.mapView);
		baiduMap = mapView.getMap();

		// 开启定位图层
		baiduMap.setMyLocationEnabled(true);

		// 定位初始化
		mlocaClient = new LocationClient(this);
		// 注册定位监听函数
		mlocaClient.registerLocationListener(myListener);

		// 定位支持选项
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 打开gps
		// 返回国测局经纬度坐标系：gcj02; 返回百度墨卡托坐标系 ：bd09; 返回百度经纬度坐标系 ：bd09ll
		option.setCoorType("bd09ll"); // 设置坐标类型
		// 获取设置的扫描间隔，单位是毫秒
		option.setScanSpan(1000);
		// 设置 LocationClientOption
		mlocaClient.setLocOption(option);
		mlocaClient.start();

		MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(14.0f);
		baiduMap.setMapStatus(msu);

	}

	// bitmap 描述信息
	private BitmapDescriptor bd;
	// 定义地图 Marker 覆盖物
	@SuppressWarnings("unused")
	private Marker mMarkerA;

	/**
	 * OverLay初始化覆盖物
	 */
	public void initOverlay(String name, double latitude, double longtitude) {

		// (LatLng表示坐标位置 第一个参数为维度，第一个参数为经度)
		LatLng ll = new LatLng(latitude, longtitude);

		// 这里是将图标转化为对象
		bd = BitmapDescriptorFactory.fromResource(R.drawable.icon_gcoding);
		// OverlayOptions地图覆盖物选型
		OverlayOptions oo = new MarkerOptions().position(ll).icon(bd).zIndex(2);
		// addOverlay在当前图层添加覆盖物对象
		mMarkerA = (Marker) (baiduMap.addOverlay(oo));

		// 初始化marker的监听事件
		initOverlayListener();
	}

	// 信息窗
	private InfoWindow mInfoWindow;

	/**
	 * OverLay的marker和地图的监听事件
	 */
	public void initOverlayListener() {

		// 设置坐标点击事件
		baiduMap.setOnMarkerClickListener(new OnMarkerClickListener() {
			@Override
			public boolean onMarkerClick(Marker marker) {
				// 得到当前marker的经纬度
				double latitude = marker.getPosition().latitude;
				double longitude = marker.getPosition().longitude;

				// Log.i("sys", "-----" + longitude + latitude);

				final String name = sd.QueryTb(LocationAct.this, latitude, longitude);

				// Log.i("sys", "-----" + name);

				// 点击marker后显示文字的按钮，初始化
				Button button = new Button(getApplicationContext());
				// 设置按钮图片背景
				button.setBackgroundResource(R.drawable.popup);
				// 设置按钮显示的文字
				button.setText(name);
				button.setTextColor(Color.BLACK);
				// 点击按钮后响应事件
				button.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						baiduMap.hideInfoWindow();
						Intent i = new Intent();					
						i.setClass(LocationAct.this, SeeColorMain.class);
						i.putExtra("username", name);
						startActivity(i);
						finish();
					}
				});

				LatLng ll = marker.getPosition();
				/*
				 * 在地图中显示一个信息窗口，可以设置一个View作为该窗口的内容， 也可以设置一个 BitmapDescriptor
				 * 作为该窗口的内容。-47为 Y轴偏移量
				 */
				mInfoWindow = new InfoWindow(button, ll, -47);
				baiduMap.showInfoWindow(mInfoWindow);// 显示消息窗

				return true;
			}
		});

		// 地图点击事件
		baiduMap.setOnMapClickListener(new OnMapClickListener() {

			@Override
			public void onMapClick(LatLng arg0) {
				// 点击地图时隐藏文字窗口
				baiduMap.hideInfoWindow();
			}

			@Override
			public boolean onMapPoiClick(MapPoi arg0) {
				return false;
			}
		});
	}

	/**
	 * 定位SDK监听函数
	 */
	public class MyLocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(final BDLocation location) {

			// map view 销毁后不在处理新接收的位置
			if (location == null || mapView == null)
				return;
			double latitude = location.getLatitude();
			double longitude = location.getLongitude();

			// 将定位后的我的坐标等信息上传到数据库更新
			sd.UpdateTb("定位", latitude, longitude);
			sd.CloseDb();

			// 定位数据建造器
			MyLocationData locData = new MyLocationData.Builder()
					.accuracy(location.getRadius())
					// 此处设置开发者获取到的方向信息，顺时针0-360
					.direction(100).latitude(latitude).longitude(longitude)
					.build();
			// 设置定位信息
			baiduMap.setMyLocationData(locData);

			// 是否是第一次定位
			if (isFirstLoc) {
				isFirstLoc = false;
				// 设置地理坐标
				LatLng ll = new LatLng(location.getLatitude(),
						location.getLongitude());

				HandlerThread handlerThread = new HandlerThread("url_Thread");
				handlerThread.start();
				handler = new Handler(handlerThread.getLooper());
				handler.post(urlThread);

				// 更新坐标
				MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
				// 以动画方式更新地图状态，动画耗时 300 ms
				baiduMap.animateMapStatus(u);
			}
		}
	}

	/**
	 * 将个人信息添加到Lbs后台
	 */
	class LbsThread extends Thread {
		String title;
		String latitude;
		String longitude;
		int searchnum;
		String id;

		public LbsThread(String title, String latitude, String longitude,
				int searchnum, String id) {
			super();
			this.title = title;
			this.latitude = latitude;
			this.longitude = longitude;
			this.searchnum = searchnum;
			this.id = id;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (searchnum == 0) {
				String uriAPI = "http://api.map.baidu.com/geodata/v3/poi/create"; // 这是我测试的本地,大家可以随意改
				/* 建立HTTPost对象 */
				HttpPost httpRequest = new HttpPost(uriAPI);
				/*
				 * NameValuePair实现请求参数的封装
				 */
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("ak",
						"oupGF4SZNArc9OnKwEYspWfo"));
				params.add(new BasicNameValuePair("geotable_id", "101783"));
				params.add(new BasicNameValuePair("coord_type", "3"));
				params.add(new BasicNameValuePair("latitude", latitude));
				params.add(new BasicNameValuePair("longitude", longitude));
				params.add(new BasicNameValuePair("title", title));

				try {
					/* 添加请求参数到请求对象 */
					httpRequest.setEntity(new UrlEncodedFormEntity(params,
							HTTP.UTF_8));
					/* 发送请求并等待响应 */
					HttpResponse httpResponse = new DefaultHttpClient()
							.execute(httpRequest);
					/* 若状态码为200 ok */
					if (httpResponse.getStatusLine().getStatusCode() == 200) {
						/* 读返回数据 */
						String strResult = EntityUtils.toString(httpResponse
								.getEntity());
						Log.i("sys", "success" + strResult);
					} else {
						Log.i("sys", "error");
					}

				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e) {

					e.printStackTrace();
				}
			} else {
				String uriAPI = "http://api.map.baidu.com/geodata/v3/poi/update"; // 这是我测试的本地,大家可以随意改
				/* 建立HTTPost对象 */
				HttpPost httpRequest = new HttpPost(uriAPI);
				/*
				 * NameValuePair实现请求参数的封装
				 */
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("ak",
						"oupGF4SZNArc9OnKwEYspWfo"));
				params.add(new BasicNameValuePair("geotable_id", "101783"));
				params.add(new BasicNameValuePair("coord_type", "3"));
				params.add(new BasicNameValuePair("latitude", latitude));
				params.add(new BasicNameValuePair("longitude", longitude));
				params.add(new BasicNameValuePair("title", title));
				params.add(new BasicNameValuePair("id", id));

				try {
					/* 添加请求参数到请求对象 */
					httpRequest.setEntity(new UrlEncodedFormEntity(params,
							HTTP.UTF_8));
					/* 发送请求并等待响应 */
					HttpResponse httpResponse = new DefaultHttpClient()
							.execute(httpRequest);
					/* 若状态码为200 ok */
					if (httpResponse.getStatusLine().getStatusCode() == 200) {
						/* 读返回数据 */
						String strResult = EntityUtils.toString(httpResponse
								.getEntity());
						Log.i("sys", "success" + strResult);
					} else {
						Log.i("sys", "error");
					}

				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e) {

					e.printStackTrace();
				}
			}
		}
	}

	@Override
	protected void onResume() {
		mapView.onResume();
		super.onResume();
	}

	@Override
	protected void onPause() {
		mapView.onPause();
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		// 退出时销毁定位
		mlocaClient.stop();
		// 关闭定位图层
		baiduMap.setMyLocationEnabled(false);
		mapView.onDestroy();
		mapView = null;
		super.onDestroy();
		// 关闭数据库
		sd.CloseDb();
	}

	// 初始化handler处理器
	Handler handler = new Handler();

	// 另辟新的线程，用来处理URL所在的线程
	Runnable urlThread = new Runnable() {

		@Override
		public void run() {
			// 从数据库查询定位的坐标
			String location = sd.QueryTb(LocationAct.this, "定位");
			// Log.i("sys", location);

			// 百度云检索提供的接口，可用浏览器直接打开
			String url = "http://api.map.baidu.com/geosearch/v3/nearby?"
					+ "ak=ocnQomwS4j25rhUWdgyRxy1K&geotable_id=101783"
					+ "&location="
					+ location
					+ "&radius=100000"
					+ "&mcode=82:83:D9:49:A7:16:E6:5E:7B:18:62:B9:6A:07:3D:21:B8:2A:5A:E7;com.example.examplep";
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
				String text = decodeUnicode(pageBuffer.toString());
				// Log.i("sys", "" + text);

				// 初始化Json对象，将文本转换为Json
				JSONObject json = new JSONObject(text);
				// Log.i("sys", "-----dan--------" + json);
				// 对Json格式文件进行分解
				Json_parser(json, location);

				br.close();
				in.close();

				// 关闭线程
				handler.removeCallbacks(urlThread);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	};

	/**
	 * JSON解析，从JSON数据获取我们需要的数据（经纬度，用户名）
	 * 
	 * @param json
	 */
	private void Json_parser(JSONObject json, String location) {
		userManager = BmobUserManager.getInstance(this);
		// 获取当前用户
		DSUser user = userManager.getCurrentUser(DSUser.class);
		String current_username = user.getUsername();

		try {
			// 根据名字返回Json数组
			JSONArray jsonArray = json.getJSONArray("contents");
			if (jsonArray != null && jsonArray.length() <= 0) {
				Toast.makeText(null, "没有符合要求的数据", Toast.LENGTH_SHORT).show();
			} else {
				searchnum = 0;
				for (int i = 0; i < jsonArray.length(); i++) {
					// 根据下标返回Json数组中的数据对象
					JSONObject jsono = (JSONObject) jsonArray.opt(i);
					// 在Json数据中提取出坐标
					JSONArray locArray = jsono.getJSONArray("location");
					double latitude = locArray.getDouble(1);
					double longitude = locArray.getDouble(0);
					// Log.i("sys", "----------" + longitude + "," + latitude);

					// 在Json数据中提取出用户名
					String name = jsono.getString("title");
					if (current_username.equals(name)) {
						id = jsono.getString("uid");
						searchnum = 1;
					}

					initOverlay(name, latitude, longitude);

					// 将解析后的数据逐条存入数据库
					sd.InsertTb(name, latitude, longitude);
					sd.CloseDb();

					// 更新地图显示
					LatLng ll = new LatLng(latitude, longitude);
					MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
					baiduMap.animateMapStatus(u);
				}
				String la = location.substring(0, 10);
				String lo = location.substring(11, 20);

				if (searchnum == 0) {
					new LbsThread(current_username, lo, la, searchnum, id)
							.start();
				} else {
					Log.i("lake", la + "--" + lo);
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * unicode转换成中文
	 * 
	 * @param theString
	 * @return 返回中文
	 */
	public static String decodeUnicode(String theString) {

		char aChar;
		int len = theString.length();
		StringBuffer outBuffer = new StringBuffer(len);

		for (int x = 0; x < len;) {
			/**
			 * 返回指定索引处的 char 值。索引范围为从 0 到 length() - 1。序列的第一 个 char 值位于索引
			 * 0处，第二个位于索引 1 处，依此类推，这类似于数组索引。
			 */
			aChar = theString.charAt(x++);
			if (aChar == '\\') {
				aChar = theString.charAt(x++);
				if (aChar == 'u') {
					// Read the xxxx
					int value = 0;
					for (int i = 0; i < 4; i++) {
						aChar = theString.charAt(x++);
						switch (aChar) {
						case '0':
						case '1':
						case '2':
						case '3':
						case '4':
						case '5':
						case '6':
						case '7':
						case '8':
						case '9':
							value = (value << 4) + aChar - '0';
							break;

						case 'a':
						case 'b':
						case 'c':
						case 'd':
						case 'e':
						case 'f':
							value = (value << 4) + 10 + aChar - 'a';
							break;

						case 'A':
						case 'B':
						case 'C':
						case 'D':
						case 'E':
						case 'F':
							value = (value << 4) + 10 + aChar - 'A';
							break;

						default:
							throw new IllegalArgumentException(
									"Malformed   \\uxxxx   encoding.");
						}

					}
					outBuffer.append((char) value);
				} else {
					if (aChar == 't')
						aChar = '\t';
					else if (aChar == 'r')
						aChar = '\r';
					else if (aChar == 'n')
						aChar = '\n';
					else if (aChar == 'f')
						aChar = '\f';
					outBuffer.append(aChar);
				}
			} else
				outBuffer.append(aChar);
		}

		return outBuffer.toString();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		getActionBar().setTitle(R.string.location);
		menu.findItem(R.id.action_personnal).setVisible(false);
		menu.findItem(R.id.action_more).setVisible(false);
		menu.findItem(R.id.action_save_info).setVisible(false);
		menu.findItem(R.id.action_stop).setVisible(false);
		menu.findItem(R.id.action_edit_info).setVisible(false);
		getActionBar().setIcon(null);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setDisplayShowHomeEnabled(false);
		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
		case android.R.id.home:
			finish();
			Intent i = new Intent();
			i.setClass(LocationAct.this, Shake.class);
			startActivity(i);
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

}
