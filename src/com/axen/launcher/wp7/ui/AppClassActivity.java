package com.axen.launcher.wp7.ui;

import java.util.List;

import com.axen.launcher.wp7.main.R;
import com.axen.launcher.wp7.main.WP7App;
import com.axen.launcher.wp7.main.WP7Configuration;
import com.axen.launcher.wp7.ui.anim.Rotate3DAnimation;
import com.axen.launcher.wp7.ui.apputil.ClassifyApp;
import com.axen.launcher.wp7.ui.apputil.ClassifyFactory;
import com.axen.launcher.wp7.ui.apputil.ClassifyApp.AppClass;
import com.axen.launcher.wp7.ui.size.Size;
import com.axen.launcher.wp7.ui.widget.AppClassAdapter;
import com.axen.launcher.wp7.ui.widget.ClassPositionView;
import com.axen.launcher.wp7.ui.widget.ClassView;
import com.axen.utils.AXLog;
import com.axen.utils.ResourceUtil;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.GridView;

public class AppClassActivity extends Activity implements View.OnClickListener, Animation.AnimationListener{

	public final static String ACTION_START = "com.axen.launcher.wp7.ACTION_START_AppClassActivity";
	public final static String POSITION_EXTRA = "com.axen.launcher.wp7.ACTION_POSITION_EXTRA";
	private static final String TAG = "AppClassActivity";
	
	private WP7Configuration mConf = WP7Configuration.getInstance();
	
	private GridView mGridView = null;
	private int mGridItemW = 0;
	private int mGridItemH = 0;
	private ResourceUtil mru = mConf.getRU();
	private ClassifyApp mAppClasses = null;
	private Size size = Size.get();
	
	private Rotate3DAnimation mShowAnim = null;
	private Rotate3DAnimation mHideAnim = null;
	
	private static final int ANIM_DURATION = 200;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		if(!ACTION_START.equals(intent.getAction())) {
			AXLog.d(TAG, "Action is not right, finish.");
			finish();
		}
		mAppClasses = ClassifyFactory.get();
		if(mAppClasses == null) {
			AXLog.w(TAG, "ClassifyApp is null.");
		}
		List<AppClass> list = mAppClasses.getClassifiedList();
		
		mGridItemW = size.getApp_class_view_w(); //mru.getPexil(R.dimen.app_class_view_w);
		mGridItemH = size.getApp_class_view_h(); //mru.getPexil(R.dimen.app_class_view_h);
		
		AppClassAdapter adapter = new AppClassAdapter(this, list, this, mGridItemW, mGridItemH);
		setContentView(R.layout.app_class_activity);
		mGridView = (GridView)findViewById(R.id.id_app_class_grid);
		mGridView.setColumnWidth(size.getApp_class_view_w());
		mGridView.setVerticalSpacing(size.getApp_class_view_space());
		mGridView.setHorizontalSpacing(size.getApp_class_view_space());
		mGridView.setAdapter(adapter);
		
		// 表示不需要滚动
		mAppClasses.setNeedToScroll(false);
		
		mShowAnim = new Rotate3DAnimation(90, 0, 0, 0, 0, 0,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		mHideAnim = new Rotate3DAnimation(0, 90, 0, 0, 0, 0,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		mShowAnim.setDuration(ANIM_DURATION);
		mShowAnim.setInterpolator( new DecelerateInterpolator());
		mHideAnim.setDuration(ANIM_DURATION);
		mHideAnim.setInterpolator(new AccelerateInterpolator());
		mShowAnim.setFillAfter(false);
		mHideAnim.setFillAfter(false);
	}
	
	

	@Override
	protected void onResume() {
		super.onResume();
		// childStartAnim(mGridView, mShowAnim);
		setFullScreen(mConf.getShowStatusbar());
	}



	@Override
	protected void onPause() {
		super.onPause();
		setResult(RESULT_CANCELED);
		childStartAnim(mGridView, mHideAnim, this);
		//finish();
	}

	private void childStartAnim(ViewGroup container, Rotate3DAnimation anim, Animation.AnimationListener l) {
		View v = null;
		Rotate3DAnimation a = null;
		for(int i = 0; i < container.getChildCount(); i ++) {
			try {
				a = anim.clone();
				v = container.getChildAt(i);
				v.startAnimation(a);
				
				if((l != null)&& (i == (container.getChildCount() - 1))) {
					a.setAnimationListener(l);
				}
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	
	@Override
	public void onBackPressed() {
		//super.onBackPressed();
		childStartAnim(mGridView, mHideAnim, this);
	}



	@Override
	public void onClick(View v) {
		ClassPositionView cv = null;
		if(v instanceof ClassPositionView) {
			cv  = (ClassPositionView)v;
			AppClass ac = cv.getAppClass();
			if(ac.size() > 0)
			{
				Intent intent = new Intent();
				intent.putExtra(POSITION_EXTRA, ac.screenOffset);
				setResult(RESULT_OK, intent);
				mAppClasses.setNeedToScroll(true);
				mAppClasses.setScrollPos(ac.screenOffset);
				//finish();
				childStartAnim(mGridView, mHideAnim, this);
			}
		}
	}



	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if(hasFocus) {
			childStartAnim(mGridView, mShowAnim, null);
		}
	}



	@Override
	public void onAnimationEnd(Animation animation) {
		finish();
	}



	@Override
	public void onAnimationRepeat(Animation animation) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void onAnimationStart(Animation animation) {
		// TODO Auto-generated method stub
		
	}

	private void setFullScreen(boolean show) {
		if (show) {
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
		} else {
			final WindowManager.LayoutParams attrs = getWindow()
					.getAttributes();
			attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
			getWindow().setAttributes(attrs);
			getWindow().clearFlags(
					WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

		}
	}
}
