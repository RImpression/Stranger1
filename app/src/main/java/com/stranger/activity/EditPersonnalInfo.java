package com.stranger.activity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import cn.bmob.im.BmobUserManager;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;

import com.example.examplep.R;
import com.stranger.bean.DSUser;
import com.stranger.pre.MyDialog;
import com.stranger.util.ImageFileCache;
import com.stranger.util.PhotoUtil;
/**
 * 修改个人资料页
 * @author RImpression
 *
 */
public class EditPersonnalInfo extends BaseActivity implements OnClickListener{
	protected static final int LOAD_SUCCESS = 0;
	protected static final int LOAD_FAILURE = 1;
	//依次是昵称、账号、签名
	EditText info_edit_nick,info_edit_username,info_edit_style;
	//依次是性别、生日、星座
	TextView info_edit_sex,info_edit_birthday,info_edit_interest;
	private String nick,birthday,interest,style;
	//头像
	ImageView info_edit_avatar;
	//年、月、日
	private int year,month,day;
	//更改头像时的dialog
	MyDialog dialog;
	public final static String IMAGE_UNSPECIFIED = "image/*";
	private String imageDir;
	//头像String
	String avatarString;
	//裁剪头像保存路径
	String path;
	String dirpath = Environment.getExternalStorageDirectory() + "/DSImage/";
	
	BmobUserManager userManager;
	DSUser user;
	
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case LOAD_SUCCESS:
				Bitmap bitmap = (Bitmap) msg.obj;
				info_edit_avatar.setImageBitmap(bitmap);
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
		setContentView(R.layout.edit_ps_info);
		userManager = BmobUserManager.getInstance(this);
		//获取当前用户
		user = userManager.getCurrentUser(DSUser.class);
		initView();
	}
	
	
	private void initView() {
		info_edit_avatar = (ImageView) findViewById(R.id.info_edit_avatar);
		info_edit_nick = (EditText) findViewById(R.id.info_edit_nick);
		info_edit_username = (EditText) findViewById(R.id.info_edit_username);
		info_edit_sex = (TextView) findViewById(R.id.info_edit_sex);
		info_edit_birthday = (TextView) findViewById(R.id.info_edit_birthday);
		info_edit_interest = (TextView) findViewById(R.id.info_edit_interest);
		info_edit_style = (EditText) findViewById(R.id.info_edit_style);
		info_edit_sex.setOnClickListener(this);
		info_edit_birthday.setOnClickListener(this);
		info_edit_avatar.setOnClickListener(this);
		
		//如果用户已设置头像，则显示，否则显示预设头像
		if(user.getAvatar() != null){
			new MThread(user.getAvatar()).start();
		} 
		
		//创建MyDialog对象
		dialog = new MyDialog(EditPersonnalInfo.this);
		// 获取Dialog控件ID
		dialog.getCameraBt().setOnClickListener(this);
		dialog.getTukeBt().setOnClickListener(this);
		dialog.getSureBt().setOnClickListener(this);
		
		info_edit_nick.setText(user.getNick());
		info_edit_username.setText(user.getUsername());
		info_edit_sex.setText(user.getSex()==true ? "男" : "女");
		info_edit_birthday.setText(user.getBirthday());
		info_edit_interest.setText(user.getInterest());
		info_edit_style.setText(user.getStyle());
		
	}
	
	class MThread extends Thread {
		String para;
		
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
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.info_edit_sex:	//选择性别dialog
			ShowSexDialog();
			break;
			
		case R.id.info_edit_birthday: //选择生日dialog
			//初始化年月日
			year = 1990; month = 0; day = 1;
			//创建DatePickerDialog对象
			DatePickerDialog pickerDialog = new DatePickerDialog(EditPersonnalInfo.this, Datelistener, year, month, day);
			pickerDialog.setTitle("请选择您的生日...");
			pickerDialog.setIcon(R.drawable.info_birthday);
			pickerDialog.show();//显示DatePickerDialog组件
			break;
			
		case R.id.info_edit_avatar://设置头像
			//如果用户已设置头像，则显示，否则显示预设头像
			if(user.getAvatar() != null){
				new MThread(user.getAvatar()).start();
			}
			dialog.show();
			break;
			
		case R.id.dialog_camera: // dialog选择相机
			new DateFormat();
			imageDir = DateFormat.format("yyyyMMdd_hhmmss",
					Calendar.getInstance(Locale.CHINA))
					+ ".jpg";
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			File pictureFile = new File(
					Environment.getExternalStorageDirectory() + "/DSImage");
			if (!pictureFile.exists()) {
				pictureFile.mkdirs();
			}
			intent.putExtra(MediaStore.EXTRA_OUTPUT,
					Uri.fromFile(new File(pictureFile + "/" + imageDir)));
			startActivityForResult(intent, 1);
			break;

		case R.id.dialog_tuku: // dialog选择图库
			Intent intent2 = new Intent(Intent.ACTION_GET_CONTENT);
			intent2.setType(IMAGE_UNSPECIFIED);
			Intent wrapperIntent = Intent.createChooser(intent2, null);
			startActivityForResult(wrapperIntent, 0);
			break;

		case R.id.dialog_sure: // dialog确认选择
			dialog.dismiss();
			break;
		}
		
	}

	/*
	 * DatePickerDialog监听器
	 */
	private DatePickerDialog.OnDateSetListener Datelistener = new DatePickerDialog.OnDateSetListener() {
		
		@Override
		public void onDateSet(DatePicker view, int myyear, int monthOfYear, int dayOfMonth) {
			year = myyear;
			month = monthOfYear;
			day = dayOfMonth;
			//更新时间
			updateDate();
		}
		
		//当DatePickerDialog关闭时，更新日期显示
		private void updateDate() {
			birthday = year + "-" + (month+1) + "-" + day;
			interest = getAstro(month+1, day);
			user.setBirthday(birthday);
			user.setInterest(interest);
			//更新用户信息
			user.update(getApplicationContext(), new UpdateListener() {
				
				@Override
				public void onSuccess() {
					info_edit_birthday.setText(birthday);
					info_edit_interest.setText(interest);
				}
				
				@Override
				public void onFailure(int arg0, String arg1) {
					ShowToast("更改生日失败：" + arg1);
				}
			});
		}
	};
	
	
	/*
	 * 根据月份和日期，计算星座
	 */
	private String getAstro(int m,int d) {
		String[] astro = new String[]{"魔蝎座","水瓶座","双鱼座","白羊座","金牛座",
				"双子座","巨蟹座","狮子座","处女座","天秤座","天蝎座","射手座","魔蝎座"};
		int[] arr = new int[]{20,19,21,20,21,22,23,23,23,24,23,22};//两个星座分割日
		int index = m;
		if(day<arr[m - 1]) {
			index = index - 1;
		}
		//返回索引指向的星座
		return astro[index];
	}

	/*
	 * 选择性别dialog
	 */
	String[] sexs = new String[]{"男","女"};
	private void ShowSexDialog() {
		new AlertDialog.Builder(this)
		.setTitle("请选择性别...")
		.setIcon(R.drawable.info_sexs)
		.setSingleChoiceItems(sexs, 0, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (which == 0) {
					user.setSex(true);
				} else {
					user.setSex(false);
				}
				//更新用户信息
				user.update(getApplicationContext(), new UpdateListener() {
					
					@Override
					public void onSuccess() {
						info_edit_sex.setText(user.getSex()==true ? "男" : "女");
					}
					
					@Override
					public void onFailure(int arg0, String arg1) {
						ShowToast("性别更改失败：" + arg1);
					}
				});
				dialog.dismiss();
			}
		})
		.setNegativeButton("取消", null)
		.show();
	}


	/*
	 * 保存资料
	 */
	private void Saveinfo() {
		nick = info_edit_nick.getText().toString();
		style = info_edit_style.getText().toString();
		//更改信息
		user.setNick(nick);
		user.setStyle(style);
		user.setAvatar(avatarString);
		Log.i("Imperssion", user.getNick() + "修改成功！");
		user.update(this, new UpdateListener() {
			
			@Override
			public void onSuccess() {
				ShowToast("资料更新成功！");
				//跳转回查看个人资料页
				Intent intent = new Intent();
				intent.setClass(EditPersonnalInfo.this, PersonnalInfo.class);
				intent.putExtra("from", "me");
				startActivity(intent);
				finish();
			}
			
			@Override
			public void onFailure(int arg0, String arg1) {
				ShowToast("资料更新失败：" + arg1);
			}
		});
	}
	
	// 获取缩略图
		public void photoZoom(Uri uri) {
			Intent intent = new Intent("com.android.camera.action.CROP");
			intent.setDataAndType(uri, IMAGE_UNSPECIFIED);
			intent.putExtra("crop", "true");
			intent.putExtra("aspectX", 1); 
			intent.putExtra("aspectY", 1);
			intent.putExtra("outputX", 250);
			intent.putExtra("outputY", 250);
			intent.putExtra("return-data", true);
			startActivityForResult(intent, 2);
		}

		// 根据startActivityForResult传递的数值保存图片到SD卡
		@Override
		protected void onActivityResult(int requestCode, int resultCode, Intent data) {
			if (resultCode == RESULT_OK) {
				if (requestCode == 0) {
					photoZoom(data.getData());
				}
				if (requestCode == 1) {
					File picture = new File(
							Environment.getExternalStorageDirectory() + "/DSImage/"
									+ imageDir);
					photoZoom(Uri.fromFile(picture));
				}
				if (requestCode == 2) {
					Bundle extras = data.getExtras();
					if (extras != null) {
						Bitmap photo = extras.getParcelable("data");
						ByteArrayOutputStream stream = new ByteArrayOutputStream();
						photo.compress(Bitmap.CompressFormat.JPEG, 75, stream);
						//将头像转换成圆角
						photo = PhotoUtil.toRoundCorner(photo, 10);
						info_edit_avatar.setImageBitmap(photo);
						
						//保存图片
						String filename = new SimpleDateFormat("yyMMddHHmmss").format(new Date())+".png";
						path = dirpath + filename;
						PhotoUtil.saveBitmap(dirpath, filename, photo, true);
						//上传头像
						if(photo != null && photo.isRecycled()){
							photo.recycle();
						}
						uploadAvator();
						dialog.dismiss();
					}
				}
			}
			super.onActivityResult(requestCode, resultCode, data);
		}


		private void uploadAvator() {
			final BmobFile bmobFile = new BmobFile(new File(path));
			bmobFile.upload(this, new UploadFileListener() {
				
				@Override
				public void onSuccess() {
					avatarString = bmobFile.getFileUrl(EditPersonnalInfo.this);
				}
				
				@Override
				public void onFailure(int arg0, String arg1) {
					ShowToast("头像上传失败：" + arg1);
				}
			});
		}
		
		@Override
		public boolean onCreateOptionsMenu(Menu menu) {
			getMenuInflater().inflate(R.menu.main, menu);
			return true;
		}
		
		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			int id = item.getItemId();
			if(id == R.id.action_save_info) {
				Saveinfo();
			}
			if(id == android.R.id.home) {
				finish();
			}
			return super.onOptionsItemSelected(item);
		}
		
		@Override
		public boolean onPrepareOptionsMenu(Menu menu) {
			getActionBar().setTitle(R.string.editPersonnalInfo);
			menu.findItem(R.id.action_save_info).setVisible(true);
			menu.findItem(R.id.action_personnal).setVisible(false);
			menu.findItem(R.id.action_edit_info).setVisible(false);
			menu.findItem(R.id.action_more).setVisible(false);
			menu.findItem(R.id.action_stop).setVisible(false);
			getActionBar().setDisplayHomeAsUpEnabled(true);
			getActionBar().setDisplayShowHomeEnabled(false);
			return super.onPrepareOptionsMenu(menu);
		}

}
