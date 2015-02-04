package com.axen.launcher.wp7.ui.widget;


import com.axen.launcher.app.TileItemInfo;
import com.axen.launcher.wp7.main.R;
import com.axen.launcher.wp7.main.WP7App;
import com.axen.launcher.wp7.main.WP7Configuration;
import com.axen.launcher.wp7.ui.TileSpace;
import com.axen.launcher.wp7.ui.size.Size;
import com.axen.utils.ResourceUtil;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.ViewGroup;

public abstract class Tile extends ViewGroup {

	protected float mScale = 1.0f;
	protected int mAlpha = 255;
	
	protected int mState = TileSpace.STATE_NORMAL;
	protected int mTileNameBottomMargin = 0;
	private WP7Configuration mConf = WP7Configuration.getInstance();
	private ResourceUtil mru = mConf.getRU();
	private Size size = Size.get();
	
	protected int mWidth = 0;
	protected int mHeight = 0;
	protected int mAppNameH = 0;
	protected int mAppNameLeftMargin = 0;
	
	public Tile(Context context){
		this(context, null);
	}
	
	public Tile(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public Tile(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mTileNameBottomMargin = size.getTile_name_bottom_margin(); //mru.getPexil(R.dimen.tile_name_bottom_margin);
		mAppNameLeftMargin = size.getTile_name_left_margin(); //mru.getPexil(R.dimen.tile_name_left_margin);
		mAppNameH = size.getTile_name_h(); //mru.getPexil(R.dimen.tile_name_h);
		mWidth = size.getTile_w(); //mru.getPexil(R.dimen.tile_w);
		mHeight = size.getTile_h(); //mru.getPexil(R.dimen.tile_h);
	}
	
	public void setState(int state) {
		if(state == TileSpace.STATE_NORMAL) {
			mScale = 1.0f;
			mAlpha = 0xFF;
		}else if(state == TileSpace.STATE_EDIT) {
			mScale = 0.76744f;
			mAlpha = (int)(0xFF * 0.618);
		}
	}
	
	protected int getScaledValue(float v) {
		return (int)(v * mScale);
	}
	
	protected int getAlphaValue() {
		return mAlpha;
	}
	
	public abstract TileItemInfo getTii();

	public abstract boolean isWideTile();
	
	public abstract void setAppName(CharSequence name);
	public abstract void setAppIcon(Drawable icon);
	public abstract void setAppIcon(int resId);
	
	public abstract void pauseBackgroundThread();
	public abstract void resumeBackgroundThread();
}
