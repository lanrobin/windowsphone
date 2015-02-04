package com.axen.launcher.wp7.ui.widget;

import com.axen.launcher.wp7.main.R;
import com.axen.launcher.wp7.main.WP7App;
import com.axen.launcher.wp7.main.WP7Configuration;
import com.axen.launcher.wp7.ui.size.Size;
import com.axen.utils.ResourceUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

public class AppMenuDialog extends AlertDialog {
	
	private TextView mPinToStart = null;
	private TextView mRateAndReview = null;
	private TextView mUninstall = null;
	private View mHost = null;
	private View mContextView = null;
	private WP7Configuration mConf = WP7Configuration.getInstance();
	private ResourceUtil mru = mConf.getRU();
	private Size size = Size.get();
/*
	protected AppMenuDialog(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
	}

	protected AppMenuDialog(Context context, int theme) {
		super(context, theme);
	}
	*/
	
	@SuppressWarnings("static-access")
	public AppMenuDialog(Context context, int theme, View host, Activity owner) {
		super(context, theme);
		mHost = host;
		setOwnerActivity(owner);
		mContextView = getLayoutInflater().from(getContext()).inflate(R.layout.app_context_menu, null);
		mPinToStart = (TextView)mContextView.findViewById(R.id.app_menu_pin_to_start);
		mRateAndReview = (TextView)mContextView.findViewById(R.id.app_menu_rate_and_review);
		mUninstall = (TextView)mContextView.findViewById(R.id.app_menu_uninstall);
		
		mPinToStart.setTag(mHost.getTag());
		mRateAndReview.setTag(mHost.getTag());
		mUninstall.setTag(mHost.getTag());
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/*
		LayoutParams lp = new LayoutParams( 
				mru.getPexil(R.dimen.screen_w), LayoutParams.FILL_PARENT);
				*/
		LayoutParams lp = new LayoutParams( 
				size.getScreen_w(), LayoutParams.MATCH_PARENT);
		// setContentView(R.layout.app_context_menu);
		setContentView(mContextView, lp);
		//getWindow().setWindowAnimations(R.style.appContextMenuAnim);

	}
	
	public void setOnClickListener(View.OnClickListener l) {
		mPinToStart.setOnClickListener(l);
		mRateAndReview.setOnClickListener(l);
		mUninstall.setOnClickListener(l);
	}
	
	public void setItemVisibility(int id, int visibility) {
		View v = findViewById(id);
		if(v != null) {
			v.setVisibility(visibility);
		}
	}
	
	public void setDisable(int id, boolean enabled) {
		View v = findViewById(id);
		if(v != null) {
			if(v instanceof TextView) {
				((TextView)v).setTextColor(mConf.getDisabledTextColor());
			}
			v.setEnabled(enabled);
		}
	}
	/*
	public int getHeight() {
		if(mContextView != null) {
			return mContextView.getHeight();
		}
		return 0;
	}
	*/
}
