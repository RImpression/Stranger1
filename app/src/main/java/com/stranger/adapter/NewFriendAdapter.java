package com.stranger.adapter;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import cn.bmob.im.BmobUserManager;
import cn.bmob.im.bean.BmobInvitation;
import cn.bmob.im.config.BmobConfig;
import cn.bmob.im.db.BmobDB;
import cn.bmob.v3.listener.UpdateListener;

import com.example.examplep.R;
import com.stranger.adapter.MessageChatAdapter.ViewHolder;
import com.stranger.bean.DSUser;
import com.stranger.bean.MApplication;
import com.stranger.util.CollectionUtils;
import com.stranger.util.ImageFileCache;

@SuppressLint("InflateParams")
public class NewFriendAdapter extends BaseListAdapter<BmobInvitation> {

	BmobUserManager userManager;
	DSUser user;

	public NewFriendAdapter(Context context, List<BmobInvitation> list) {
		super(context, list);
	}

	@SuppressWarnings("deprecation")
	@Override
	public View bindView(int arg0, View convertView, ViewGroup arg2) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.list_additem_friend, null);
		}
		final BmobInvitation msg = getList().get(arg0);
		TextView name = ViewHolder.get(convertView, R.id.list_additem_name);
		ImageView iv_avatar = ViewHolder.get(convertView,
				R.id.list_additem_avatar);

		final Button btn_add = ViewHolder.get(convertView, R.id.list_btn_add);

		String avatar = msg.getAvatar();

		if (avatar != null) {
			new MThread(avatar, iv_avatar).start();
		} else {
			iv_avatar.setImageResource(R.drawable.photo_normal);
		}

		userManager = BmobUserManager.getInstance(mContext);
		user = userManager.getCurrentUser(DSUser.class);

		Log.i("get", msg.getTime() + "");
		int status = msg.getStatus();
		if (status == BmobConfig.INVITE_ADD_NO_VALIDATION
				|| status == BmobConfig.INVITE_ADD_NO_VALI_RECEIVED) {
			if (!user.isAddreceipt()) {
				agressAdd(btn_add, msg);
			} else {
				btn_add.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						agressAdd(btn_add, msg);
					}
				});
			}
		} else if (status == BmobConfig.INVITE_ADD_AGREE) {
			btn_add.setText("已同意");
			btn_add.setBackgroundDrawable(null);
			btn_add.setTextColor(mContext.getResources().getColor(
					R.color.base_color_text_black));
			btn_add.setEnabled(false);
		}
		name.setText(msg.getFromname());

		return convertView;
	}

	/**
	 * 添加好友 agressAdd
	 * 
	 * @Title: agressAdd
	 * @Description: TODO
	 * @param @param btn_add
	 * @param @param msg
	 * @return void
	 * @throws
	 */
	private void agressAdd(final Button btn_add, final BmobInvitation msg) {
		final ProgressDialog progress = new ProgressDialog(mContext);
		progress.setMessage("正在添加...");
		progress.setCanceledOnTouchOutside(false);
		progress.show();
		try {
			// 同意添加好友
			BmobUserManager.getInstance(mContext).agreeAddContact(msg,
					new UpdateListener() {

						@SuppressWarnings("deprecation")
						@Override
						public void onSuccess() {
							progress.dismiss();
							btn_add.setText("已同意");
							btn_add.setBackgroundDrawable(null);
							btn_add.setTextColor(mContext.getResources()
									.getColor(R.color.base_color_text_black));
							btn_add.setEnabled(false);
							// 保存到application中方便比较
							MApplication.getInstance().setContactList(
									CollectionUtils.list2map(BmobDB.create(
											mContext).getContactList()));
						}

						@Override
						public void onFailure(int arg0, final String arg1) {
							progress.dismiss();
							ShowToast("添加失败: " + arg1);
						}
					});
		} catch (final Exception e) {
			progress.dismiss();
			ShowToast("添加失败: " + e.getMessage());
		}
	}

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				@SuppressWarnings("rawtypes")
				List list = (List) msg.obj;
				Bitmap bitmap = (Bitmap) list.get(0);
				ImageView imageView = (ImageView) list.get(1);
				imageView.setImageBitmap(bitmap);
				break;
			}
		};
	};

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

		@SuppressWarnings("unchecked")
		@Override
		public void run() {
			Bitmap bitmap = ImageFileCache.getBitmap(para);
			Message msg = Message.obtain();
			@SuppressWarnings("rawtypes")
			List list = new ArrayList();
			list.add(bitmap);
			list.add(item);

			msg.what = 0;
			msg.obj = list;
			handler.sendMessage(msg);
		}
	}

}
