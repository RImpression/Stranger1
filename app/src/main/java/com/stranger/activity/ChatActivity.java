package com.stranger.activity;

import java.io.File;
import java.util.List;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.bmob.im.BmobChatManager;
import cn.bmob.im.BmobRecordManager;
import cn.bmob.im.bean.BmobChatUser;
import cn.bmob.im.bean.BmobInvitation;
import cn.bmob.im.bean.BmobMsg;
import cn.bmob.im.config.BmobConfig;
import cn.bmob.im.db.BmobDB;
import cn.bmob.im.inteface.EventListener;
import cn.bmob.im.inteface.OnRecordChangeListener;
import cn.bmob.im.inteface.UploadListener;
import cn.bmob.im.util.BmobLog;
import cn.bmob.v3.listener.PushListener;
import com.example.examplep.R;
import com.stranger.adapter.MessageChatAdapter;
import com.stranger.adapter.MessageChatAdapter.OnSeeDataLis;
import com.stranger.adapter.NewRecordPlayClickListener;
import com.stranger.bean.BmobConstants;
import com.stranger.pre.MyDialog;
import com.stranger.receiver.MyPushReceiver;
import com.stranger.util.CommonUtils;
import com.stranger.view.XListView;
import com.stranger.view.XListView.IXListViewListener;

/**
 * 聊天主界面
 * 
 * @author RImperssion
 * 
 */
@SuppressLint("ClickableViewAccessibility")
public class ChatActivity extends BaseActivity implements OnClickListener,
		IXListViewListener, EventListener {

	private LinearLayout layout_more, layout_add;
	private Button btn_chat_send, btn_chat_add, btn_chat_keyboard,
			btn_chat_voice;

	private TextView tv_picture, tv_camera, tv_voice_tips, tv_voice_tips_2;

	// 语音有关
	private RelativeLayout layout_speak;
	private ImageView btn_speak, iv_record;

	EditText chat_et_edmeg;
	XListView chat_listView;
	String targetId = "";
	BmobChatUser targetUser;

	BmobRecordManager recordManager;
	private static int MsgPagerNum;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat_ly_main);
		manager = BmobChatManager.getInstance(this);
		MsgPagerNum = 0;
		// 组装聊天对象
		targetUser = (BmobChatUser) getIntent().getSerializableExtra("user");
		Log.i("target", targetUser.getUsername() + "ddd");
		targetId = targetUser.getObjectId();
		// 注册广播接收器
		initNewMessageBroadCast();
		initView();
	}

	private void initView() {
		chat_listView = (XListView) findViewById(R.id.mListView);
		initBottomView();
		initXListView();
		initVoiceView();
	}

	private void initAddView() {
		tv_picture = (TextView) findViewById(R.id.tv_picture);
		tv_camera = (TextView) findViewById(R.id.tv_camera);
		tv_picture.setOnClickListener(this);
		tv_camera.setOnClickListener(this);
	}

	private void initBottomView() {
		// 最左边
		btn_chat_add = (Button) findViewById(R.id.btn_chat_add);
		btn_chat_add.setOnClickListener(this);
		// 最右边
		btn_chat_keyboard = (Button) findViewById(R.id.btn_chat_keyboard);
		btn_chat_voice = (Button) findViewById(R.id.btn_chat_voice);
		btn_chat_send = (Button) findViewById(R.id.btn_chat_send);
		btn_chat_keyboard.setOnClickListener(this);
		btn_chat_voice.setOnClickListener(this);
		btn_chat_send.setOnClickListener(this);

		layout_more = (LinearLayout) findViewById(R.id.layout_more);
		layout_add = (LinearLayout) findViewById(R.id.layout_add);
		layout_speak = (RelativeLayout) findViewById(R.id.layout_speak);
		// 最下面
		initAddView();

		// 最中间
		// 语音框
		btn_speak = (ImageView) findViewById(R.id.btn_speak);
		// 输入框
		chat_et_edmeg = (EditText) findViewById(R.id.chat_et_message);
		chat_et_edmeg.setOnClickListener(this);
		chat_et_edmeg.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				if (!TextUtils.isEmpty(s)) {
					btn_chat_send.setVisibility(View.VISIBLE);
					btn_chat_keyboard.setVisibility(View.GONE);
					btn_chat_voice.setVisibility(View.GONE);
				} else {
					if (btn_chat_voice.getVisibility() != View.VISIBLE) {
						btn_chat_voice.setVisibility(View.VISIBLE);
						btn_chat_send.setVisibility(View.GONE);
						btn_chat_keyboard.setVisibility(View.GONE);
					}
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
			}
		});

	}

	private void initXListView() {
		// 首先不允许加载更多
		chat_listView.setPullLoadEnable(false);
		// 允许下拉
		chat_listView.setPullRefreshEnable(true);
		// 设置监听器
		chat_listView.setXListViewListener(this);
		chat_listView.pullRefreshing();
		chat_listView.setDividerHeight(0);
		// 加载数据
		initOrRefresh();
		chat_listView.setSelection(mAdapter.getCount() - 1);
		chat_listView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				// TODO Auto-generated method stub
				hideSoftInputView();
				layout_more.setVisibility(View.GONE);
				layout_add.setVisibility(View.GONE);
				layout_speak.setVisibility(View.GONE);
				btn_chat_voice.setVisibility(View.GONE);
				btn_chat_keyboard.setVisibility(View.VISIBLE);
				btn_chat_send.setVisibility(View.GONE);
				return false;
			}
		});

		// 重发按钮的点击事件
		mAdapter.setOnInViewClickListener(R.id.iv_fail_resend,
				new MessageChatAdapter.onInternalClickListener() {

					@Override
					public void OnClickListener(View parentV, View v,
							Integer position, Object values) {
						// 重发消息
						showResendDialog(parentV, v, values);
					}
				});
	}

	/**
	 * 加载消息历史，从数据库中读出
	 */
	private List<BmobMsg> initMsgData() {
		List<BmobMsg> list = BmobDB.create(this).queryMessages(targetId,
				MsgPagerNum);
		return list;
	}

	/**
	 * 初始化语音布局
	 * 
	 * @Title: initVoiceView
	 * @Description: TODO
	 * @param
	 * @return void
	 * @throws
	 */
	private void initVoiceView() {
		tv_voice_tips_2 = (TextView) findViewById(R.id.tv_voice_tips_2);
		tv_voice_tips = (TextView) findViewById(R.id.tv_voice_tips);
		iv_record = (ImageView) findViewById(R.id.iv_record);
		btn_speak.setOnTouchListener(new VoiceTouchListen());
		initRecordManager();
	}

	/**
	 * 长按说话
	 * 
	 * @ClassName: VoiceTouchListen
	 * @Description: TODO
	 * @author smile
	 * @date 2014-7-1 下午6:10:16
	 */
	class VoiceTouchListen implements View.OnTouchListener {
		@SuppressLint("ClickableViewAccessibility")
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				if (!CommonUtils.checkSdCard()) {
					ShowToast("发送语音需要sdcard支持！");
					return false;
				}
				try {
					v.setPressed(true);
					btn_speak.setVisibility(View.GONE);
					tv_voice_tips_2.setVisibility(View.GONE);
					iv_record.setVisibility(View.VISIBLE);
					tv_voice_tips.setVisibility(View.VISIBLE);
					tv_voice_tips
							.setText(getString(R.string.voice_cancel_tips));
					// 开始录音
					recordManager.startRecording(targetId);
				} catch (Exception e) {
				}
				return true;
			case MotionEvent.ACTION_MOVE: {
				if (event.getY() < 0) {
					tv_voice_tips_2.setVisibility(View.GONE);
					tv_voice_tips
							.setText(getString(R.string.voice_cancel_tips));
					tv_voice_tips.setTextColor(Color.RED);
				} else {
					tv_voice_tips_2.setVisibility(View.GONE);
					tv_voice_tips.setText(getString(R.string.voice_up_tips));
					tv_voice_tips.setTextColor(Color.GRAY);
				}
				return true;
			}
			case MotionEvent.ACTION_UP:
				v.setPressed(false);
				btn_speak.setVisibility(View.VISIBLE);
				tv_voice_tips_2.setVisibility(View.VISIBLE);
				iv_record.setVisibility(View.GONE);
				tv_voice_tips.setVisibility(View.GONE);
				try {
					if (event.getY() < 0) {// 放弃录音
						recordManager.cancelRecording();
						Log.i("voice", "放弃发送语音");
					} else {
						int recordTime = recordManager.stopRecording();
						if (recordTime > 0) {
							// 发送语音文件
							Log.i("voice", "发送语音");
							sendVoiceMessage(
									recordManager.getRecordFilePath(targetId),
									recordTime);
						} else {// 录音时间过短，则提示录音过短的提示
							iv_record.setVisibility(View.GONE);
							tv_voice_tips.setVisibility(View.GONE);
							showShortToast().show();
						}
					}
				} catch (Exception e) {
					// TODO: handle exception
				}
				return true;
			default:
				return false;
			}
		}
	}

	/**
	 * 发送语音消息
	 * 
	 * @Title: sendImageMessage
	 * @Description: TODO
	 * @param @param localPath
	 * @return void
	 * @throws
	 */
	private void sendVoiceMessage(String local, int length) {
		manager.sendVoiceMessage(targetUser, local, length,
				new UploadListener() {

					@Override
					public void onStart(BmobMsg msg) {
						// TODO Auto-generated method stub
						refreshMessage(msg);
					}

					@Override
					public void onSuccess() {
						// TODO Auto-generated method stub
						mAdapter.notifyDataSetChanged();
					}

					@Override
					public void onFailure(int error, String arg1) {
						// TODO Auto-generated method stub
						mAdapter.notifyDataSetChanged();
					}
				});
	}

	private void initRecordManager() {
		// 语音相关管理器
		recordManager = BmobRecordManager.getInstance(this);
		// 设置音量大小监听--在这里开发者可以自己实现：当剩余10秒情况下的给用户的提示，类似微信的语音那样
		recordManager.setOnRecordChangeListener(new OnRecordChangeListener() {

			@Override
			public void onVolumnChanged(int value) {
				// TODO Auto-generated method stub
//				iv_record.setImageDrawable(getResources().getDrawable(
//						R.drawable.btn_voice_1));
			}

			@Override
			public void onTimeChanged(int recordTime, String localPath) {
				// TODO Auto-generated method stub
				BmobLog.i("voice", "已录音长度:" + recordTime);
				if (recordTime >= BmobRecordManager.MAX_RECORD_TIME) {// 1分钟结束，发送消息
					// 需要重置按钮
					btn_speak.setPressed(false);
					btn_speak.setClickable(false);
					// 取消录音框
					iv_record.setVisibility(View.INVISIBLE);
					tv_voice_tips.setVisibility(View.INVISIBLE);
					// 发送语音消息
					sendVoiceMessage(localPath, recordTime);
					// 是为了防止过了录音时间后，会多发一条语音出去的情况。
					handler.postDelayed(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							btn_speak.setClickable(true);
						}
					}, 1000);
				} else {

				}
			}
		});
	}

	Toast toast;

	/**
	 * 显示录音时间过短的Toast
	 * 
	 * @Title: showShortToast
	 * @return void
	 * @throws
	 */
	private Toast showShortToast() {
		if (toast == null) {
			toast = new Toast(this);
		}
		View view = LayoutInflater.from(this).inflate(
				R.layout.include_chat_voice_short, null);
		toast.setView(view);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.setDuration(50);
		return toast;
	}

	/**
	 * 界面刷新
	 * 
	 * @Title: initOrRefresh
	 * @Description: TODO
	 * @param
	 * @return void
	 * @throws
	 */
	private void initOrRefresh() {
		if (mAdapter != null) {
			if (MyPushReceiver.mNewNum != 0) {// 用于更新当在聊天界面锁屏期间来了消息，这时再回到聊天页面的时候需要显示新来的消息
				int news = MyPushReceiver.mNewNum;// 有可能锁屏期间，来了N条消息,因此需要倒叙显示在界面上
				int size = initMsgData().size();
				for (int i = (news - 1); i >= 0; i--) {
					mAdapter.add(initMsgData().get(size - (i + 1)));// 添加最后一条消息到界面显示
				}
				chat_listView.setSelection(mAdapter.getCount() - 1);
			} else {
				mAdapter.notifyDataSetChanged();
			}
		} else {
			mAdapter = new MessageChatAdapter(this, initMsgData());
			// 头像点击查看资料
			mAdapter.setLis(new OnSeeDataLis() {

				@Override
				public void onSeeData(String username) {
					Intent intent = new Intent();
					intent.setClass(ChatActivity.this, PersonnalInfo.class);
					intent.putExtra("from", "other");
					intent.putExtra("username", username);
					startActivity(intent);
				}
			});
			chat_listView.setAdapter(mAdapter);
		}
	}

	/**
	 * 显示重发按钮 showResendDialog
	 * 
	 * @Title: showResendDialog
	 * @Description: TODO
	 * @param @param recent
	 * @return void
	 * @throws
	 */
	public void showResendDialog(final View parentV, View v, final Object values) {
		MyDialog dialog = new MyDialog(this, "确定重发该消息", "确定", "取消", "提示", true);
		// 设置成功事件
		dialog.SetOnSuccessListener(new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialogInterface, int userId) {
				if (((BmobMsg) values).getMsgType() == BmobConfig.TYPE_IMAGE
						|| ((BmobMsg) values).getMsgType() == BmobConfig.TYPE_VOICE) {// 图片和语音类型的采用
					resendFileMsg(parentV, values);
				} else {
					resendTextMsg(parentV, values);
				}
				dialogInterface.dismiss();
			}
		});
		// 显示确认对话框
		dialog.show();
		dialog = null;
	}

	/**
	 * 重发文本消息
	 */
	private void resendTextMsg(final View parentV, final Object values) {
		BmobChatManager.getInstance(ChatActivity.this).resendTextMessage(
				targetUser, (BmobMsg) values, new PushListener() {

					@Override
					public void onSuccess() {
						// TODO Auto-generated method stub
						((BmobMsg) values)
								.setStatus(BmobConfig.STATUS_SEND_SUCCESS);
						parentV.findViewById(R.id.progress_load).setVisibility(
								View.INVISIBLE);
						parentV.findViewById(R.id.iv_fail_resend)
								.setVisibility(View.INVISIBLE);
						parentV.findViewById(R.id.tv_send_status)
								.setVisibility(View.VISIBLE);
						((TextView) parentV.findViewById(R.id.tv_send_status))
								.setText("已发送");
					}

					@Override
					public void onFailure(int arg0, String arg1) {
						// TODO Auto-generated method stub
						((BmobMsg) values)
								.setStatus(BmobConfig.STATUS_SEND_FAIL);
						parentV.findViewById(R.id.progress_load).setVisibility(
								View.INVISIBLE);
						parentV.findViewById(R.id.iv_fail_resend)
								.setVisibility(View.VISIBLE);
						parentV.findViewById(R.id.tv_send_status)
								.setVisibility(View.INVISIBLE);
					}
				});
		mAdapter.notifyDataSetChanged();
	}

	/**
	 * 重发图片消息
	 * 
	 * @Title: resendImageMsg
	 * @Description: TODO
	 * @param @param parentV
	 * @param @param values
	 * @return void
	 * @throws
	 */
	private void resendFileMsg(final View parentV, final Object values) {
		BmobChatManager.getInstance(ChatActivity.this).resendFileMessage(
				targetUser, (BmobMsg) values, new UploadListener() {

					@Override
					public void onStart(BmobMsg msg) {
						// TODO Auto-generated method stub
					}

					@Override
					public void onSuccess() {
						// TODO Auto-generated method stub
						((BmobMsg) values)
								.setStatus(BmobConfig.STATUS_SEND_SUCCESS);
						parentV.findViewById(R.id.progress_load).setVisibility(
								View.INVISIBLE);
						parentV.findViewById(R.id.iv_fail_resend)
								.setVisibility(View.INVISIBLE);
						if (((BmobMsg) values).getMsgType() == BmobConfig.TYPE_VOICE) {
							parentV.findViewById(R.id.tv_send_status)
									.setVisibility(View.GONE);
							parentV.findViewById(R.id.tv_voice_length)
									.setVisibility(View.VISIBLE);
						} else {
							parentV.findViewById(R.id.tv_send_status)
									.setVisibility(View.VISIBLE);
							((TextView) parentV
									.findViewById(R.id.tv_send_status))
									.setText("已发送");
						}
					}

					@Override
					public void onFailure(int arg0, String arg1) {
						// TODO Auto-generated method stub
						((BmobMsg) values)
								.setStatus(BmobConfig.STATUS_SEND_FAIL);
						parentV.findViewById(R.id.progress_load).setVisibility(
								View.INVISIBLE);
						parentV.findViewById(R.id.iv_fail_resend)
								.setVisibility(View.VISIBLE);
						parentV.findViewById(R.id.tv_send_status)
								.setVisibility(View.INVISIBLE);
					}
				});
		mAdapter.notifyDataSetChanged();
	}

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == NEW_MESSAGE) {
				BmobMsg message = (BmobMsg) msg.obj;
				String uid = message.getBelongId();
				BmobMsg m = BmobChatManager.getInstance(ChatActivity.this)
						.getMessage(message.getConversationId(),
								message.getMsgTime());
				if (!uid.equals(targetId)) {
					// 如果不是当前正在聊天的对象消息，不处理
					return;
				}
				mAdapter.add(m);
				// 取消当前聊天对象的未读标本
				BmobDB.create(ChatActivity.this).resetUnread(targetId);
			}
		}
	};

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.chat_et_message:// 点击文本输入框
			chat_listView.setSelection(chat_listView.getCount() - 1);
			if (layout_more.getVisibility() == View.VISIBLE) {
				layout_add.setVisibility(View.GONE);
				layout_more.setVisibility(View.GONE);
			}
			break;
		case R.id.btn_chat_voice:// 语音按钮
			layout_more.setVisibility(View.VISIBLE);
			layout_add.setVisibility(View.GONE);
			btn_chat_voice.setVisibility(View.GONE);
			btn_chat_keyboard.setVisibility(View.VISIBLE);
			layout_speak.setVisibility(View.VISIBLE);
			btn_speak.setVisibility(View.VISIBLE);
			hideSoftInputView();
			break;
		case R.id.btn_chat_add:// 添加按钮-显示图片、拍照、位置
			if (layout_more.getVisibility() == View.GONE) {
				layout_more.setVisibility(View.VISIBLE);
				layout_add.setVisibility(View.VISIBLE);
				hideSoftInputView();
			} else {
				if (layout_add.getVisibility() == View.VISIBLE) {
					layout_add.setVisibility(View.GONE);
					layout_speak.setVisibility(View.GONE);
				} else {
					layout_add.setVisibility(View.VISIBLE);
					layout_speak.setVisibility(View.GONE);
				}
			}
			break;
		case R.id.btn_chat_keyboard:// 键盘按钮，点击就弹出键盘并隐藏掉声音按钮
			showEditState(false);
			break;
		case R.id.btn_chat_send:
			final String msg = chat_et_edmeg.getText().toString();
			Log.i("bmob", msg + "");
			if (msg.equals("")) {
				ShowToast("请输入发送消息！");
				return;
			}
			// 组装BmobMessage对象
			BmobMsg message = BmobMsg.createTextSendMsg(this, targetId, msg);
			message.setExtra("Bmob");
			Log.i("bmob", message + "");
			// 默认发送完成，将数据保存到本地消息表和最近会话表中
			manager.sendTextMessage(targetUser, message);
			refreshMessage(message);// 刷新界面
			break;
		case R.id.tv_camera:// 拍照
			selectImageFromCamera();
			break;
		case R.id.tv_picture:// 图片
			selectImageFromLocal();
			break;
		default:
			break;
		}

	}

	private void showEditState(boolean isEmo) {
		chat_et_edmeg.setVisibility(View.VISIBLE);
		btn_chat_keyboard.setVisibility(View.GONE);
		btn_chat_voice.setVisibility(View.VISIBLE);
		layout_more.setVisibility(View.GONE);
		btn_speak.setVisibility(View.GONE);
		chat_et_edmeg.requestFocus();
		showSoftInputView();
	}

	// 显示软键盘
	public void showSoftInputView() {
		((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
				.showSoftInput(chat_et_edmeg, 0);
	}

	private String localCameraPath = "";// 拍照后得到的图片地址

	/**
	 * 启动相机拍照 startCamera
	 * 
	 * @Title: startCamera
	 * @throws
	 */
	public void selectImageFromCamera() {
		Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		File dir = new File(BmobConstants.BMOB_PICTURE_PATH);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		File file = new File(dir, String.valueOf(System.currentTimeMillis())
				+ ".jpg");
		localCameraPath = file.getPath();
		Uri imageUri = Uri.fromFile(file);
		openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
		startActivityForResult(openCameraIntent,
				BmobConstants.REQUESTCODE_TAKE_CAMERA);
	}

	/**
	 * 选择图片
	 * 
	 * @Title: selectImage
	 * @Description: TODO
	 * @param
	 * @return void
	 * @throws
	 */
	public void selectImageFromLocal() {
		Intent intent;
		if (Build.VERSION.SDK_INT < 19) {
			intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("image/*");
		} else {
			intent = new Intent(
					Intent.ACTION_PICK,
					android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		}
		startActivityForResult(intent, BmobConstants.REQUESTCODE_TAKE_LOCAL);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case BmobConstants.REQUESTCODE_TAKE_CAMERA:// 当取到值的时候才上传path路径下的图片到服务器
				sendImageMessage(localCameraPath);
				break;
			case BmobConstants.REQUESTCODE_TAKE_LOCAL:
				if (data != null) {
					Uri selectedImage = data.getData();
					if (selectedImage != null) {
						Cursor cursor = getContentResolver().query(
								selectedImage, null, null, null, null);
						cursor.moveToFirst();
						int columnIndex = cursor.getColumnIndex("_data");
						String localSelectPath = cursor.getString(columnIndex);
						cursor.close();
						if (localSelectPath == null
								|| localSelectPath.equals("null")) {
							ShowToast("找不到您想要的图片");
							return;
						}
						sendImageMessage(localSelectPath);
					}
				}
				break;
			}
		}
	}

	/**
	 * 默认先上传本地图片，之后才显示出来 sendImageMessage
	 * 
	 * @Title: sendImageMessage
	 * @Description: TODO
	 * @param @param localPath
	 * @return void
	 * @throws
	 */
	private void sendImageMessage(String local) {
		if (layout_more.getVisibility() == View.VISIBLE) {
			layout_more.setVisibility(View.GONE);
			layout_add.setVisibility(View.GONE);
		}
		manager.sendImageMessage(targetUser, local, new UploadListener() {

			@Override
			public void onStart(BmobMsg msg) {
				// TODO Auto-generated method stub
				Log.i("sys",
						"开始上传onStart：" + msg.getContent() + ",状态："
								+ msg.getStatus());
				refreshMessage(msg);
			}

			@Override
			public void onSuccess() {
				// TODO Auto-generated method stub
				mAdapter.notifyDataSetChanged();
			}

			@Override
			public void onFailure(int error, String arg1) {
				// TODO Auto-generated method stub
				mAdapter.notifyDataSetChanged();
			}
		});
	}

	public static final int NEW_MESSAGE = 0x001; // 收到消息
	NewBroadcastReceiver receiver;
	MessageChatAdapter mAdapter;

	// 注册广播接收器
	private void initNewMessageBroadCast() {
		receiver = new NewBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter(
				BmobConfig.BROADCAST_NEW_MESSAGE);
		// 设置广播的优先级大于Mainactivity,直接显示消息，而不是提示消息未读
		intentFilter.setPriority(5);
		registerReceiver(receiver, intentFilter);
	}

	// 新消息广播接收者
	private class NewBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context contenxt, Intent intent) {
			String from = intent.getStringExtra("fromId");
			String msgId = intent.getStringExtra("msgId");
			String msgTime = intent.getStringExtra("msgTime");
			// 收到这个广播的时候，message已经在消息表中，可直接获取
			if (TextUtils.isEmpty(from) && TextUtils.isEmpty(msgId)
					&& TextUtils.isEmpty(msgTime)) {
				BmobMsg msg = BmobChatManager.getInstance(ChatActivity.this)
						.getMessage(msgId, msgTime);
				if (!from.equals(targetId)) {
					// 如果不是当前正在聊天的对象消息，不处理
					return;
				}
				mAdapter.add(msg); // 添加到当前页面
				// 取消当前聊天对象的未读标本
				BmobDB.create(ChatActivity.this).resetUnread(targetId);
			}
			abortBroadcast();// 终结广播
		}

	}

	// 刷新界面
	private void refreshMessage(BmobMsg msg) {
		// 更新界面
		Log.i("lake", "" + msg);
		mAdapter.add(msg);
		chat_listView.setSelection(mAdapter.getCount() - 1);
		chat_et_edmeg.setText("");
	}

	@Override
	public void onMessage(BmobMsg message) {
		Message handlerMsg = handler.obtainMessage(NEW_MESSAGE);
		handlerMsg.obj = message;
		handler.sendMessage(handlerMsg);
	}

	@Override
	public void onNetChange(boolean isNetConnected) {
		if (!isNetConnected) {
			ShowToast("当前网络不可用，请检查您的网络");
		}
	}

	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub
		handler.postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				MsgPagerNum++;
				int total = BmobDB.create(ChatActivity.this)
						.queryChatTotalCount(targetId);
				BmobLog.i("记录总数：" + total);
				int currents = mAdapter.getCount();
				if (total <= currents) {
					ShowToast("聊天记录加载完了哦!");
				} else {
					List<BmobMsg> msgList = initMsgData();
					mAdapter.setList(msgList);
					chat_listView.setSelection(mAdapter.getCount() - currents
							- 1);
				}
				chat_listView.stopRefresh();
			}
		}, 1000);
	}

	@Override
	public void onLoadMore() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAddUser(BmobInvitation arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onOffline() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onReaded(String conversionId, String msgTime) {
		// TODO Auto-generated method stub
		if (conversionId.split("&")[1].equals(targetId)) {
			// 修改界面上指定消息的阅读状态
			for (BmobMsg msg : mAdapter.getList()) {
				if (msg.getConversationId().equals(conversionId)
						&& msg.getMsgTime().equals(msgTime)) {
					msg.setStatus(BmobConfig.STATUS_SEND_RECEIVERED);
				}
				mAdapter.notifyDataSetChanged();
			}
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		MyPushReceiver.list.remove(this);// 监听推送的消息
		// 停止录音
		if (recordManager.isRecording()) {
			recordManager.cancelRecording();
			iv_record.setVisibility(View.GONE);
			tv_voice_tips.setVisibility(View.GONE);
		}
		// 停止播放录音
		if (NewRecordPlayClickListener.isPlaying
				&& NewRecordPlayClickListener.currentPlayListener != null) {
			NewRecordPlayClickListener.currentPlayListener.stopPlayRecord();
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		getActionBar().setTitle("与" + targetUser.getUsername() + "会话");
		menu.findItem(R.id.action_edit_info).setVisible(false);
		menu.findItem(R.id.action_personnal).setVisible(true);
		menu.findItem(R.id.action_save_info).setVisible(false);
		menu.findItem(R.id.action_more).setVisible(false);
		menu.findItem(R.id.action_stop).setVisible(false);
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
			break;

		case R.id.action_personnal:
			Intent intent = new Intent();
			intent.setClass(getApplication(), PersonnalInfo.class);
			intent.putExtra("from", "other");
			intent.putExtra("username", targetUser.getUsername());
			startActivity(intent);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(receiver);
		super.onDestroy();
	}

}
