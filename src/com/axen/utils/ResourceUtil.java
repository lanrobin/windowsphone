package com.axen.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

public class ResourceUtil {

	private int  getPexil(int resId) {
		int value = mRes.getDimensionPixelSize(resId);
		// return (int)(value * mDensity + 0.5f);
		return value;
	}
	
	public float getDimen(int resId) {
		return mRes.getDimension(resId);
	}
	
	public ResourceUtil(Context c) {
		mContext = c;
		mRes = c.getResources();
		mDensity = mRes.getDisplayMetrics().density;
	}
	
	public int getColor(int resId) {
		return mRes.getColor(resId);
	}
	
	public Drawable getDrawable(int resId) {
		return mRes.getDrawable(resId);
	}
	
	public String[] getStringArray(int resId) {
		return mRes.getStringArray(resId);
	}
	
	private Resources mRes = null;
	private Context mContext = null;
	private float mDensity = 0;
}
