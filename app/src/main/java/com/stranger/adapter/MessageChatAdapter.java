package com.stranger.adapter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import com.example.examplep.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.stranger.activity.ImageBrowserActivity;
import com.stranger.util.ImageFileCache;
import com.stranger.util.TimeUtil;

import cn.bmob.im.BmobDownloadManager;
import cn.bmob.im.BmobUserManager;
import cn.bmob.im.bean.BmobMsg;
import cn.bmob.im.config.BmobConfig;
import cn.bmob.im.inteface.DownloadListener;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * 聊天消息的适配器
 * 
 */

public class MessageChatAdapter extends BaseListAdapter<BmobMsg> {

	// 文字 一个为发送，一个为接收
	private final int TYPE_SEND_TXT = 0;
	private final int TYPE_RECEIVER_TXT = 1;
	// 图片
	private final int TYPE_SEND_IMAGE = 2;
	private final int TYPE_RECEIVER_IMAGE = 3;
	// 语音
	private final int TYPE_SEND_VOICE = 4;
	private final int TYPE_RECEIVER_VOICE = 5;

	DisplayImageOptions options;

	// 用户的id
	String currentObjectId = "";

	public MessageChatAdapter(Context context, List<BmobMsg> msglist) {
		super(context, msglist);
		currentObjectId = BmobUserManager.getInstance(context)
				.getCurrentUserObjectId();
		options = new DisplayImageOptions.Builder()
				.showImageForEmptyUri(R.drawable.ic_launcher)
				.showImageOnFail(R.drawable.ic_launcher)
				.resetViewBeforeLoading(true).cacheOnDisc(true)
				.cacheInMemory(true).imageScaleType(ImageScaleType.EXACTLY)
				.bitmapConfig(Bitmap.Config.RGB_565).considerExifParams(true)
				.displayer(new FadeInBitmapDisplayer(300)).build();
	}

	@Override
	public int getItemViewType(int position) { // 类型
		BmobMsg msg = list.get(position);
		// 图片（发送或接收）
		if (msg.getMsgType() == BmobConfig.TYPE_IMAGE) {
			return msg.getBelongId().equals(currentObjectId) ? TYPE_SEND_IMAGE
					: TYPE_RECEIVER_IMAGE;
		}// 语音
		else if (msg.getMsgType() == BmobConfig.TYPE_VOICE) {
			return msg.getBelongId().equals(currentObjectId) ? TYPE_SEND_VOICE
					: TYPE_RECEIVER_VOICE;
		}// 文本
		else {
			return msg.getBelongId().equals(currentObjectId) ? TYPE_SEND_TXT
					: TYPE_RECEIVER_TXT;
		}
	}

	@Override
	public int getViewTypeCount() {
		return 6;
	}

	// 根据传入消息的类型，选择相应布局
	private View createViewByType(BmobMsg message, int position) {
		int type = message.getMsgType();
		if (type == BmobConfig.TYPE_IMAGE) {// 图片类型
			return getItemViewType(position) == TYPE_RECEIVER_IMAGE ? mInflater
					.inflate(R.layout.chat_image_left, null) : mInflater
					.inflate(R.layout.chat_image_right, null);
		} else if (type == BmobConfig.TYPE_VOICE) {// 语音类型
			return getItemViewType(position) == TYPE_RECEIVER_VOICE ? mInflater
					.inflate(R.layout.chat_voice_left, null) : mInflater
					.inflate(R.layout.chat_voice_right, null);
		} else {// 剩下默认的都是文本
			return getItemViewType(position) == TYPE_RECEIVER_TXT ? mInflater
					.inflate(R.layout.chat_text_left, null) : mInflater
					.inflate(R.layout.chat_text_right, null);
		}
	}

	@Override
	public View bindView(int position, View convertView, ViewGroup parent) {

		final BmobMsg item = list.get(position);
		if (convertView == null) {
			convertView = createViewByType(item, position);
		}

		final ImageView iv_fail_resend = ViewHolder.get(convertView,
				R.id.iv_fail_resend);// 失败重发
		final TextView tv_send_status = ViewHolder.get(convertView,
				R.id.tv_send_status);// 发送状态

		// 文字
		ImageView iv_avatar = ViewHolder.get(convertView, R.id.iv_avatar);
		TextView tv_time = ViewHolder.get(convertView, R.id.tv_time);
		TextView tv_message = ViewHolder.get(convertView, R.id.chat_content);

		// 点击头像查看资料
		iv_avatar.setOnClickListener(new MOnClickLis(list.get(position)
				.getBelongUsername()));

		final String avatar = item.getBelongAvatar();
		if (avatar != null) {
			// 开启一个线程用于获取头像
			new MThread(avatar, iv_avatar).start();
		} else {
			iv_avatar.setImageResource(R.drawable.photo_normal);
		}

		// 图片
		ImageView iv_picture = ViewHolder.get(convertView, R.id.iv_picture);
		final ProgressBar progress_load = ViewHolder.get(convertView,
				R.id.progress_load);// 进度条

		tv_time.setText(TimeUtil.getChatTime(Long.parseLong(item.getMsgTime())));

		// 语音
		final ImageView iv_voice = ViewHolder.get(convertView, R.id.iv_voice);
		// 语音长度
		final TextView tv_voice_length = ViewHolder.get(convertView,
				R.id.tv_voice_length);

		// 根据类型显示内容
		final String text = item.getContent();
		switch (item.getMsgType()) {
		case BmobConfig.TYPE_TEXT:
			try {
				tv_message.setText(text);
			} catch (Exception e) {
			}
			break;

		case BmobConfig.TYPE_IMAGE:// 图片类
			try {
				if (text != null && !text.equals("")) {// 发送成功之后存储的图片类型的content和接收到的是不一样的
					dealWithImage(position, progress_load, iv_fail_resend,
							tv_send_status, iv_picture, item);
				}
				iv_picture.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						Intent intent = new Intent(mContext,
								ImageBrowserActivity.class);
						ArrayList<String> photos = new ArrayList<String>();
						photos.add(getImageUrl(item));
						intent.putStringArrayListExtra("photos", photos);
						intent.putExtra("position", 0);
						mContext.startActivity(intent);
					}
				});

			} catch (Exception e) {
			}
			break;
		case BmobConfig.TYPE_VOICE:// 语音消息
			try {
				if (text != null && !text.equals("")) {
					tv_voice_length.setVisibility(View.VISIBLE);
					String content = item.getContent();
					if (item.getBelongId().equals(currentObjectId)) {// 发送的消息
						if (item.getStatus() == BmobConfig.STATUS_SEND_RECEIVERED
								|| item.getStatus() == BmobConfig.STATUS_SEND_SUCCESS) {//
							// 当发送成功或者发送已阅读的时候，则显示语音长度
							tv_voice_length.setVisibility(View.VISIBLE);
							String length = content.split("&")[2];
							tv_voice_length.setText(length + "\''");
						} else {
							tv_voice_length.setVisibility(View.INVISIBLE);
						}
					} else {// 收到的消息
						boolean isExists = BmobDownloadManager
								.checkTargetPathExist(currentObjectId, item);
						if (!isExists) {// 若指定格式的录音文件不存在，则需要下载，因为其文件比较小，故放在此下载
							String netUrl = content.split("&")[0];
							final String length = content.split("&")[1];
							BmobDownloadManager downloadTask = new BmobDownloadManager(
									mContext, item, new DownloadListener() {

										@Override
										public void onStart() {
											// TODO Auto-generated method stub
											progress_load
													.setVisibility(View.VISIBLE);
											tv_voice_length
													.setVisibility(View.GONE);
											iv_voice.setVisibility(View.INVISIBLE);// 只有下载完成才显示播放的按钮
										}

										@Override
										public void onSuccess() {
											// TODO Auto-generated method stub
											progress_load
													.setVisibility(View.GONE);
											tv_voice_length
													.setVisibility(View.VISIBLE);
											tv_voice_length.setText(length
													+ "\''");
											iv_voice.setVisibility(View.VISIBLE);
										}

										@Override
										public void onError(String error) {
											// TODO Auto-generated method stub
											progress_load
													.setVisibility(View.GONE);
											tv_voice_length
													.setVisibility(View.GONE);
											iv_voice.setVisibility(View.INVISIBLE);
										}
									});
							downloadTask.execute(netUrl);
						} else {
							String length = content.split("&")[2];
							tv_voice_length.setText(length + "\''");
						}
					}
				}
				// 播放语音文件
				iv_voice.setOnClickListener(new NewRecordPlayClickListener(
						mContext, item, iv_voice));
			} catch (Exception e) {

			}

			break;
		default:
			break;
		}
		return convertView;
	}

	private OnSeeDataLis lis;

	public void setLis(OnSeeDataLis lis) {
		this.lis = lis;
	}

	public interface OnSeeDataLis {
		public void onSeeData(String username);
	}

	class MOnClickLis implements OnClickListener {

		private String username;

		public MOnClickLis(String username) {
			this.username = username;
		}

		@Override
		public void onClick(View arg0) {
			if (lis != null) {
				lis.onSeeData(username);
			}
		}
	}

	/**
	 * 获取图片的地址--
	 * 
	 * @Description: TODO
	 * @param @param item
	 * @param @return
	 * @return String
	 * @throws
	 */
	private String getImageUrl(BmobMsg item) {
		String showUrl = "";
		String text = item.getContent();
		if (item.getBelongId().equals(currentObjectId)) {//
			if (text.contains("&")) {
				showUrl = text.split("&")[0];
			} else {
				showUrl = text;
			}
		} else {// 如果是收到的消息，则需要从网络下载
			showUrl = text;
		}
		return showUrl;
	}

	/**
	 * 处理图片
	 * 
	 * @Description: TODO
	 * @param @param position
	 * @param @param progress_load
	 * @param @param iv_fail_resend
	 * @param @param tv_send_status
	 * @param @param iv_picture
	 * @param @param item
	 * @return void
	 * @throws
	 */
	private void dealWithImage(int position, final ProgressBar progress_load,
			ImageView iv_fail_resend, TextView tv_send_status,
			ImageView iv_picture, BmobMsg item) {
		String text = item.getContent();
		if (getItemViewType(position) == TYPE_SEND_IMAGE) {// 发送的消息
			if (item.getStatus() == BmobConfig.STATUS_SEND_START) {
				progress_load.setVisibility(View.VISIBLE);
				iv_fail_resend.setVisibility(View.INVISIBLE);
				tv_send_status.setVisibility(View.INVISIBLE);
			} else if (item.getStatus() == BmobConfig.STATUS_SEND_SUCCESS) {
				progress_load.setVisibility(View.INVISIBLE);
				iv_fail_resend.setVisibility(View.INVISIBLE);
				tv_send_status.setVisibility(View.VISIBLE);
				tv_send_status.setText("已发送");
			} else if (item.getStatus() == BmobConfig.STATUS_SEND_FAIL) {
				progress_load.setVisibility(View.INVISIBLE);
				iv_fail_resend.setVisibility(View.VISIBLE);
				tv_send_status.setVisibility(View.INVISIBLE);
			} else if (item.getStatus() == BmobConfig.STATUS_SEND_RECEIVERED) {
				progress_load.setVisibility(View.INVISIBLE);
				iv_fail_resend.setVisibility(View.INVISIBLE);
				tv_send_status.setVisibility(View.VISIBLE);
				tv_send_status.setText("已阅读");
			}
			// 如果是发送的图片的话，因为开始发送存储的地址是本地地址，
			// 发送成功之后存储的是本地地址+"&"+网络地址，因此需要判断下
			String showUrl = "";
			if (text.contains("&")) {
				showUrl = text.split("&")[0];
			} else {
				showUrl = text;
			}
			Log.i("lake", showUrl + "---");
			// 为了方便每次都是取本地图片显示
			ImageLoader.getInstance().displayImage(showUrl, iv_picture);
		} else {
			ImageLoader.getInstance().displayImage(text, iv_picture, options,
					new ImageLoadingListener() {

						@Override
						public void onLoadingStarted(String imageUri, View view) {
							// TODO Auto-generated method stub
							progress_load.setVisibility(View.VISIBLE);
						}

						@Override
						public void onLoadingFailed(String imageUri, View view,
								FailReason failReason) {
							// TODO Auto-generated method stub
							progress_load.setVisibility(View.INVISIBLE);
						}

						@Override
						public void onLoadingComplete(String imageUri,
								View view, Bitmap loadedImage) {
							// TODO Auto-generated method stub
							progress_load.setVisibility(View.INVISIBLE);
						}

						@Override
						public void onLoadingCancelled(String imageUri,
								View view) {
							// TODO Auto-generated method stub
							progress_load.setVisibility(View.INVISIBLE);
						}
					});
		}
	}

	@SuppressWarnings("unused")
	private static class AnimateFirstDisplayListener extends
			SimpleImageLoadingListener {

		static final List<String> displayedImages = Collections
				.synchronizedList(new LinkedList<String>());

		@Override
		public void onLoadingComplete(String imageUri, View view,
				Bitmap loadedImage) {
			if (loadedImage != null) {
				ImageView imageView = (ImageView) view;
				boolean firstDisplay = !displayedImages.contains(imageUri);
				if (firstDisplay) {
					FadeInBitmapDisplayer.animate(imageView, 500);
					displayedImages.add(imageUri);
				}
			}
		}
	}

	public static class ViewHolder {
		public static <T extends View> T get(View view, int id) {
			@SuppressWarnings("unchecked")
			SparseArray<View> viewHolder = (SparseArray<View>) view.getTag();
			if (viewHolder == null) {
				viewHolder = new SparseArray<View>();
				view.setTag(viewHolder);
			}
			View childView = viewHolder.get(id);
			if (childView == null) {
				childView = view.findViewById(id);
				viewHolder.put(id, childView);
			}
			return (T) childView;
		}
	}

	/**
	 * 加载本地图片 http://bbs.3gstdy.com
	 * 
	 * @param url
	 * @return
	 */
	public static Bitmap getLoacalBitmap(String url) {
		try {
			FileInputStream fis = new FileInputStream(url);
			return BitmapFactory.decodeStream(fis);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				List list = (List) msg.obj;
				Bitmap bitmap = (Bitmap) list.get(0);
				ImageView imageView = (ImageView) list.get(1);
				imageView.setImageBitmap(bitmap);
				break;
			}
		};
	};

	// 用于获取头像的线程
	class MThread extends Thread {
		String para;
		ImageView item;

		public void setPara(String para) {
			this.para = para;
		}

		public MThread(String para, ImageView item) {
			super();
			this.para = para;
			this.item = item;
		}

		@Override
		public void run() {
			Bitmap bitmap = ImageFileCache.getBitmap(para);
			Message msg = Message.obtain();
			List list = new ArrayList();
			list.add(bitmap);
			list.add(item);

			msg.what = 0;
			msg.obj = list;
			handler.sendMessage(msg);
		}
	}

}
