package com.axen.launcher.wp7.ui.widget;

import com.axen.launcher.wp7.main.R;
import com.axen.launcher.wp7.main.WP7App;
import com.axen.launcher.wp7.main.WP7Configuration;
import com.axen.launcher.wp7.ui.apputil.ClassifyApp.AppClass;
import com.axen.launcher.wp7.ui.size.Size;
import com.axen.utils.ResourceUtil;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class AppClassView extends ViewGroup {

	private int mAppItemH = 0;
	private int mAppItemW = 0;
	private int mAppItemSpace = 0;
	private int mAppItemLeftSpace = 0;
	private AppClass mAC = null;
	private WP7Configuration mConf = WP7Configuration.getInstance();
	private ResourceUtil mru = mConf.getRU();
	private Size size = Size.get();
	
	public AppClassView(Context context, AppClass ac) {
		this(context, null, ac);
	}
	
	public AppClassView(Context context, AttributeSet attrs, AppClass ac){
		this(context, attrs, 0, ac);
	}
	public AppClassView(Context context, AttributeSet attrs, int defStyle, AppClass ac) {
		super(context, attrs, defStyle);
		mAppItemH = size.getApp_icon_h(); //mru.getPexil(R.dimen.app_icon_h);
		mAppItemW = size.getApp_icon_w(); //mru.getPexil(R.dimen.app_icon_w);
		mAppItemSpace = size.getApp_icon_space(); //mru.getPexil(R.dimen.app_icon_space);
		mAppItemLeftSpace = size.getApp_left_space(); //mru.getPexil(R.dimen.app_left_space);
		mAC = ac;
		addView(new ClassView(context, mAC, mConf.getAccentColor(), Color.TRANSPARENT, mAppItemW, mAppItemH, true));
	}

	@Override
	protected void onLayout(boolean change, int l, int t, int r, int b) {
		int top  = 0;
		View v = null;
		for(int i = 0; i < getChildCount(); i++) {
			// mAC.screenOffset = top;
			v = getChildAt(i);
			v.layout(0, top, mAppItemW, top + mAppItemH);
			top += (mAppItemH + mAppItemSpace);
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		View v = null;
		for(int i = 0; i < getChildCount(); i++) {
			v = getChildAt(i);
			v.measure(widthMeasureSpec, heightMeasureSpec);
		}
	}

	public AppClass getAppClass() {
		return mAC;
	}

}
