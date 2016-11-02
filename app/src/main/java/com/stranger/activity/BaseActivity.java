package com.stranger.activity;

import java.io.File;
import java.util.List;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.stranger.bean.MApplication;
import com.stranger.util.CollectionUtils;

import cn.bmob.im.BmobChat;
import cn.bmob.im.BmobChatManager;
import cn.bmob.im.BmobUserManager;
import cn.bmob.im.bean.BmobChatUser;
import cn.bmob.v3.listener.FindListener;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

/**
 * 基类 ShowToast和更新好友列表
 * 
 * @author RImperssion
 * 
 */
public class BaseActivity extends FragmentActivity {

	BmobUserManager userManager;
	BmobChatManager manager;

	public FragmentManager fragmentManager;
	public MApplication mApplication;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		BmobChat.getInstance(this).init("3d822ee09aaf157011b512ebcbe99fbc");
		userManager = BmobUserManager.getInstance(this);
		manager = BmobChatManager.getInstance(this);
		fragmentManager = getSupportFragmentManager();
		mApplication = (MApplication) getApplication();
		initImageLoader(mApplication);
	}

	/** 初始化ImageLoader */
	public static void initImageLoader(Context context) {
		File cacheDir = StorageUtils.getOwnCacheDirectory(context,
				"example/Cache");// 获取到缓存的目录地址
		// 创建配置ImageLoader(所有的选项都是可选的,只使用那些你真的想定制)，这个可以设定在APPLACATION里面，设置为全局的配置参数
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				context)
				// 线程池内加载的数量
				.threadPoolSize(3).threadPriority(Thread.NORM_PRIORITY - 2)
				.memoryCache(new WeakMemoryCache())
				.denyCacheImageMultipleSizesInMemory()
				.discCacheFileNameGenerator(new Md5FileNameGenerator())
				// 将保存的时候的URI名称用MD5 加密
				.tasksProcessingOrder(QueueProcessingType.LIFO)
				.discCache(new UnlimitedDiscCache(cacheDir))// 自定义缓存路径
				// .defaultDisplayImageOptions(DisplayImageOptions.createSimple())
				.writeDebugLogs() // Remove for release app
				.build();
		// Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(config);// 全局初始化此配置
	}
	
	/**
	 * 根据类名载入Fragment
	 * 
	 * @param fragmentTag
	 *            类名
	 * @return Fragment的实例
	 */
	public Fragment loadFragmentByName(String fragmentTag) {
		try {
			return (Fragment) Class.forName(fragmentTag).newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 加载fragment
	 * 
	 * @param fragmentTag
	 *            类名
	 * @param containerId
	 *            容器的ID
	 * @param add2BaackStack
	 *            是否加入栈中
	 * @param bundle
	 *            参数
	 */
	public void changeFrag(String fragmentTag, int containerId,
			boolean add2BaackStack, Bundle bundle) {
		Fragment targetFragment = null;
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		targetFragment = fragmentManager.findFragmentByTag(fragmentTag);
		if (targetFragment != null) {
			if (bundle != null) {
				targetFragment.getArguments().clear();
				targetFragment.setArguments(bundle);
			}
			transaction.show(targetFragment);
		}
		if (targetFragment == null) {
			targetFragment = loadFragmentByName(fragmentTag);
			if (add2BaackStack) {
				transaction.addToBackStack(fragmentTag);
			}
			if (bundle != null) {
				if (targetFragment.getArguments() != null) {
					targetFragment.getArguments().clear();
				}
				targetFragment.setArguments(bundle);
			}
			transaction.add(containerId, targetFragment, fragmentTag);
		}
		transaction.commit();
	}

	/**
	 * 
	 * @param fragmentTag
	 */
	public void removeFrag(String fragmentTag) {
		Fragment targetFragment = null;
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		targetFragment = fragmentManager.findFragmentByTag(fragmentTag);
		if (targetFragment != null) {
			transaction.remove(targetFragment);
		}
		transaction.commit();
	}

	/**
	 * 重载的方法 参数为空
	 * 
	 * @param fragmentTag
	 * @param containerId
	 * @param add2BaackStack
	 */
	public void changeFrag(String fragmentTag, int containerId,
			boolean add2BaackStack) {
		this.changeFrag(fragmentTag, containerId, add2BaackStack, null);
	}

	/**
	 * 重载的方法 参数为空 默认不加入栈中
	 * 
	 * @param fragmentTag
	 * @param containerId
	 */
	public void changeFrag(String fragmentTag, int containerId) {
		this.changeFrag(fragmentTag, containerId, false);
	}

	// 显示Toast
	Toast mToast;

	public void ShowToast(final String text) {
		if (!TextUtils.isEmpty(text)) {
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					if (mToast == null) {
						mToast = Toast.makeText(getApplicationContext(), text,
								Toast.LENGTH_LONG);
					} else {
						mToast.setText(text);
					}
					mToast.show();
				}
			});

		}
	}

	public void ShowToast(final int resId) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (mToast == null) {
					mToast = Toast.makeText(
							BaseActivity.this.getApplicationContext(), resId,
							Toast.LENGTH_LONG);
				} else {
					mToast.setText(resId);
				}
				mToast.show();
			}
		});
	}

	// 更新好友列表
	public void updateUserInfos() {
		userManager.queryCurrentContactList(new FindListener<BmobChatUser>() {

			@Override
			public void onError(int arg0, String arg1) {
				//ShowToast("查询好友失败！");
			}

			@Override
			public void onSuccess(List<BmobChatUser> arg0) {
				MApplication.getInstance().setContactList(
						CollectionUtils.list2map(arg0));
			}
		});
	}

	public void hideSoftInputView() {
		InputMethodManager manager = ((InputMethodManager) this
				.getSystemService(Activity.INPUT_METHOD_SERVICE));
		if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
			if (getCurrentFocus() != null)
				manager.hideSoftInputFromWindow(getCurrentFocus()
						.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}
}
