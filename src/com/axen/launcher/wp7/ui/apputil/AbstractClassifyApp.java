package com.axen.launcher.wp7.ui.apputil;

import java.util.Vector;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;

public abstract class AbstractClassifyApp implements ClassifyApp {

	protected Context mContext;
	protected PackageManager mPM;
	
	public AbstractClassifyApp(Context context){
		mContext = context;
		mPM = mContext.getPackageManager();
	}

	protected String getAppName(ResolveInfo ri) {
		String name  = ri.loadLabel(mPM).toString();
		return name;
	}
	
	protected char getAppNameFirstChar(ResolveInfo ri) {
		String name = getAppName(ri);
		if(!TextUtils.isEmpty(name)) {
			return name.charAt(0);
		}
		return 0;
	}
	
	protected boolean isDigital(char c) {
		boolean ret = false;
		if(c <= 0x40) {
			ret = true;
		}else if(c >= 0x5B && c <= 0x60) {
			ret = true;
		}else if(c >= 0x7B && c<= 0x7F) {
			ret = true;
		}
		return ret;
	}
	
	protected boolean isAlphaBeta(char c) {
		return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
	}
	
	protected static Vector<AppClass> sAppClasses = new Vector<AppClass>();


	protected void clearClassified() {
		for (AppClass ac : sAppClasses) {
			ac.apps.clear();
		}
	}
	
	@Override
	public Vector<AppClass> getClassifiedList() {
		return sAppClasses;
	}
	
	/**
	 * 其实可以是整数的，但是为了表示不需要滚动，所以用了特殊的NaN来表示。
	 */
	private int mScrollY = 0;
	private boolean mNeedToScroll = false;
	
	@Override
	public void setScrollPos(int pos) {
		mScrollY = pos;
	}
	
	@Override
	public int getScrollPos(){
		return mScrollY;
	}
	
	@Override
	public void setNeedToScroll(boolean s){
		mNeedToScroll = s;
	}
	@Override
	public boolean getNeedToScroll(){
		return mNeedToScroll;
	}
}
