package com.stranger.shake_find;

import com.example.examplep.R;
import com.stranger.activity.MainActivity;
import com.stranger.map.LocationAct;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

/**
 * 摇一摇功能
 * 
 * @author lake
 * 
 */


@SuppressLint("HandlerLeak")
public class Shake extends Activity {

	// 访问设备的传感器
	private SensorManager sensorManager;
	// 获取手机震动器对象
	private Vibrator vibrator;

	private final static int SENSOR_SHAKE = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_shake);

		// 获取系统传感器服务
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		// 获取系统振动器服务
		vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (sensorManager != null) {
			// 注册传感器监听，getDefaultSensor加载加速度传感器
			sensorManager.registerListener(sensorEventListener,
					sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
					SensorManager.SENSOR_DELAY_NORMAL);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (sensorManager != null) {
			// 取消传感器监听
			sensorManager.unregisterListener(sensorEventListener);
		}
	}

	// SensorManager传感器值改变时接收通知
	private SensorEventListener sensorEventListener = new SensorEventListener() {

		@Override
		public void onSensorChanged(SensorEvent event) {
			float[] values = event.values;
			float x = values[0]; // x轴方向的重力加速度，向右为正
			float y = values[1]; // y轴方向的重力加速度，向前为正
			float z = values[2]; // z轴方向的重力加速度，向上为正
			// 一般在这三个方向的重力加速度达到19就达到了摇晃手机的状态
			// Log.i("sys", "x轴方向的重力加速度" + x + "；y轴方向的重力加速度" + y + "；z轴方向的重力加速度"
			// + z);
			int accele = 19;
			if (Math.abs(x) > accele || Math.abs(y) > accele
					|| Math.abs(z) > accele) {
				vibrator.vibrate(200);
				Message msg = new Message();
				msg.what = SENSOR_SHAKE;
				handler.sendMessage(msg);
			}
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {

		}
	};
	
	
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
		menu.findItem(R.id.action_stop).setVisible(false);
		getActionBar().setIcon(null);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setDisplayShowHomeEnabled(false);
		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if(id == android.R.id.home) {
			finish();
			Intent i = new Intent();
			i.setClass(Shake.this, MainActivity.class);
			startActivity(i);
		}
		return super.onOptionsItemSelected(item);
	}

	/*
	 * 当传感器接受监听后，在这里进行处理
	 */
	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case SENSOR_SHAKE:
				Log.i("sys", "检测到摇晃，执行操作！");
				finish();
				Intent i = new Intent();
				i.setClass(Shake.this, LocationAct.class);
				startActivity(i);
				break;
			}
		}

	};

}
