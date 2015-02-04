package com.axen.launcher.wp7.ui.widget;

import com.axen.launcher.wp7.main.R;
import com.axen.launcher.wp7.main.WP7App;
import com.axen.launcher.wp7.main.WP7Configuration;
import com.axen.launcher.wp7.ui.size.Size;
import com.axen.utils.AXLog;
import com.axen.utils.ResourceUtil;

import android.content.Context;
import android.content.pm.ResolveInfo;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AppItemView extends LinearLayout {	
	private static final String TAG = "AppItemView";
	
	private WP7Configuration mConf = WP7Configuration.getInstance();
	private Size size = Size.get();

	public AppItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
	}

	public AppItemView(Context context) {
		this(context, null);
	}
	
	
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mImageView = (ImageView)findViewById(R.id.id_app_image);
		mTextView = (TextView)findViewById(R.id.id_app_name);
		// mTextView.setPadding(size.getApp_icon_space(), 0, 0, 0);
		mTextView.setTextSize(size.getTile_name_font_h());
	}

	


	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		
		mImageView.layout(0, 0, size.getApp_icon_h(), size.getApp_icon_w());
		mTextView.layout(size.getApp_icon_w() + size.getApp_icon_space(), 0, r, b);
	}




	ImageView mImageView = null;
	TextView mTextView = null; 
	private ResolveInfo mResolveInfo = null;
	
	public void setAppItem(ResolveInfo ri) {
		mResolveInfo = ri;
	}
	
	public ResolveInfo getAppItem() {
		return mResolveInfo;
	}
	
	public void setImage(Drawable icon) {
		if(mImageView != null) {
			mImageView.setImageDrawable(icon);
		}
	}
	
	public void setImageBackgroundColor(int color) {
		if(mImageView != null) {
			mImageView.setBackgroundColor(color);
			mImageView.invalidate();
		}
	}
	
	public void setText(CharSequence text) {
		if(mTextView != null) {
			mTextView.setTextColor(mConf.getTextColor());
			mTextView.setTextSize(size.getApp_name_font_h());
			mTextView.setText(text);
		}
	}

	public void clearTextColor() {
		if(mTextView != null) {
			String text = mTextView.getText().toString();
			mTextView.setText(text);
		}
	}
	
	@Override
	public void draw(Canvas canvas) {
		AXLog.d(TAG, "draw");
		super.draw(canvas);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		AXLog.d(TAG, "onDraw");
		setImageBackgroundColor(mConf.getAccentColor());
		super.onDraw(canvas);
	}

	public void setTextColor(int textColor) {
		if(mTextView != null) {
			mTextView.setTextColor(textColor);
			mTextView.invalidate();
		}
	}
	
	
}
