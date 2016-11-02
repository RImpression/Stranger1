package com.stranger.pre;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import com.example.examplep.R;
import com.stranger.activity.BaseActivity;
import com.stranger.activity.MainActivity;
import com.stranger.bean.DSUser;
import com.stranger.util.PhotoUtil;

import cn.bmob.im.BmobChat;
import cn.bmob.v3.BmobInstallation;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UploadFileListener;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
/**
 * 注册类
 * 调用相机，图库
 * @author RImpression
 *
 */
public class DSSignupActivity extends BaseActivity implements OnClickListener {
	EditText signup_username, signup_password, signup_surepassword;
	Button signup_bt;
	ImageView signup_image;
	MyDialog dialog;
	public final static String IMAGE_UNSPECIFIED = "image/*";
	private String imageDir;
	public String installationId;
	String avatarString;//头像String
	//裁剪头像保存路径
	String path;
	String dirpath = Environment.getExternalStorageDirectory() + "/DSImage/";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		BmobChat.getInstance(mApplication).init("3d822ee09aaf157011b512ebcbe99fbc");
		setContentView(R.layout.layout_signup);
		initView();
	}

	private void initView() {
		signup_username = (EditText) findViewById(R.id.signup_username);
		signup_password = (EditText) findViewById(R.id.signup_password);
		signup_surepassword = (EditText) findViewById(R.id.signup_surepassword);
		signup_bt = (Button) findViewById(R.id.signup_bt);
		signup_image = (ImageView) findViewById(R.id.signup_image);
		signup_bt.setOnClickListener(this);
		signup_image.setOnClickListener(this);
		dialog = new MyDialog(DSSignupActivity.this);
		// 获取Dialog控件ID
		dialog.getCameraBt().setOnClickListener(this);
		dialog.getTukeBt().setOnClickListener(this);
		dialog.getSureBt().setOnClickListener(this);
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.signup_bt: //注册
			Signup();
			break;

		case R.id.signup_image: //注册界面头像按钮
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

	// 注册
	private void Signup() {
		String username = signup_username.getText().toString();
		String password = signup_password.getText().toString();
		String surepassword = signup_surepassword.getText().toString();
		//姓名为空
		if(TextUtils.isEmpty(username)) {
			ShowToast("输入账号不能为空！");
			return;
		}
		//密码为空
		if(TextUtils.isEmpty(password)) {
			ShowToast("输入密码不能为空！");
			return;
		}
		//密码与确认密码不一致
		if(!surepassword.equals(password)) {
			ShowToast("输入密码不一致！");
			return;
		}
		
		final ProgressDialog progress = new ProgressDialog(DSSignupActivity.this);
		progress.setMessage("正在注册...");
		progress.setCanceledOnTouchOutside(false);
		progress.show();
		
		final DSUser user = new DSUser();
		user.setUsername(username);
		user.setPassword(password);
		user.setSex(true);
		user.setAvatar(avatarString);
		// 将User和设备绑定
		user.setDeviceType("android");
		user.setInstallId(installationId);
		user.setInstallId(BmobInstallation.getInstallationId(this));
		user.signUp(DSSignupActivity.this, new SaveListener() {


			@Override
			public void onSuccess() {
				progress.dismiss();
				// userManager.bindInstallationForRegister(user.getUsername());
				Toast.makeText(getApplicationContext(), "注册成功！",
						Toast.LENGTH_SHORT).show();
				
				mApplication.setCurrentUser(user);
				
				//发送广播通知登陆页退出
				sendBroadcast(new Intent(DSLoginActivity.ACTION_FINISH));
				Intent intent = new Intent();
				intent.setClass(DSSignupActivity.this, MainActivity.class);
				startActivity(intent);
				finish();
			}

			@Override
			public void onFailure(int arg0, String arg1) {
				Toast.makeText(getApplicationContext(), "注册失败:" + arg1,
						Toast.LENGTH_SHORT).show();
				progress.dismiss();
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
					//将头像剪成圆角
					photo = PhotoUtil.toRoundCorner(photo, 10);
					signup_image.setImageBitmap(photo);
					
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
				ShowToast("头像上传成功");
				avatarString = bmobFile.getFileUrl(DSSignupActivity.this);
			}
			
			@Override
			public void onFailure(int arg0, String arg1) {
				ShowToast("头像上传失败：" + arg1);
			}
		});
	}


}
