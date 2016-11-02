package com.stranger.frags;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.example.examplep.R;
import com.stranger.activity.MainActivity;
import com.stranger.adapter.CustomVPAdapter;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainFrag extends Fragment implements OnClickListener {

	private View parentView;
	private MainActivity mainActivity;

	private CustomVPAdapter vpAdapter;
	private ViewPager vp;
	private TextView tvLeft,tvRight;	//tab文字 （消息，通讯录）
	private ImageView tabLine; //滑动线
	private int mScreen1_2; //获取屏幕的二分之一
	private LinearLayout tabLeft,tabRight;
	private List<Fragment> fragments = new ArrayList<Fragment>();

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mainActivity = (MainActivity) activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		parentView = inflater.inflate(R.layout.frag_main, container, false);
		setOverflowShowingAlways();
		initView();
		
		return parentView;
	}

	private void initView() {
		tvLeft = (TextView) parentView.findViewById(R.id.textLeft);
		tvRight = (TextView) parentView.findViewById(R.id.textRight);
		tabLeft = (LinearLayout) parentView.findViewById(R.id.tab_left);
		tabLeft.setOnClickListener(this);
		tabRight = (LinearLayout) parentView.findViewById(R.id.tab_right);
		tabRight.setOnClickListener(this);
		initTabLine();
		
		fragments.add(new LeftFrag());
		fragments.add(new RightFrag());

		vpAdapter = new CustomVPAdapter(getChildFragmentManager());
		vpAdapter.setFragments(fragments);

		vp = (ViewPager) parentView.findViewById(R.id.vp_main);

		vp.setAdapter(vpAdapter);
		vp.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int pos) {
				resetTvColor(pos);
			}

			@Override
			public void onPageScrolled(int pos, float offset, int offsetPx) {
				LinearLayout.LayoutParams lp = (android.widget.LinearLayout.LayoutParams) tabLine.getLayoutParams();
				lp.leftMargin = (int) (mScreen1_2*offset+pos*mScreen1_2);
				tabLine.setLayoutParams(lp);
			}
			
			@Override
			public void onPageScrollStateChanged(int pos) {
				
			}
		});
	}

	private void initTabLine() {
		tabLine = (ImageView) parentView.findViewById(R.id.tab_line);
		Display display =  getActivity().getWindow().getWindowManager().getDefaultDisplay();
		DisplayMetrics metrics = new DisplayMetrics();
		display.getMetrics(metrics);
		mScreen1_2 = metrics.widthPixels/2; //获取屏幕的二分之一
		LayoutParams lp = tabLine.getLayoutParams();
		lp.width = mScreen1_2;
		tabLine.setLayoutParams(lp);
	}

	private void setOverflowShowingAlways() {
		try {  
	        ViewConfiguration config = ViewConfiguration.get(mainActivity);
	        Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");  
	        menuKeyField.setAccessible(true);  
	        menuKeyField.setBoolean(config, false);  
	    } catch (Exception e) {  
	        e.printStackTrace();
	    }
	}
	
	private void resetTvColor(int pos) {
		tvLeft.setTextColor(Color.BLACK);
		tvRight.setTextColor(Color.BLACK);
		
		switch (pos) {
		case 0:
			tvLeft.setTextColor(getActivity().getResources().getColor(R.color.blue));
			break;
		case 1:
			tvRight.setTextColor(getActivity().getResources().getColor(R.color.blue));
			break;
		default:
			break;
		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.tab_left:
			vp.setCurrentItem(0);
			break;
		case R.id.tab_right:
			vp.setCurrentItem(1);
			break;
		default:
			break;
		}
	}
}