package com.axen.launcher.wp7.ui.apputil;

import java.util.Vector;

import com.axen.launcher.app.AppManager.AppItem;

import android.content.pm.ResolveInfo;

public interface ClassifyApp {
	
	/**
	 * 应用的分类，比如下简体中文下，以拼音分类。
	 * 在繁体中文下以笔画分类。
	 * @author lanhuanze
	 *
	 */
	public static class AppClass {
		public String info;
		public String showInfo;
		public int screenOffset;
		
		/**
		 * 未知的类型，比如在英文下，显示是汉字的应用
		 */
		public final static String CLASS_UNKOWN = "CLASS_UNKNOW";
		
		/**
		 * 数字，符号等开始的类型
		 */
		public final static String CLASS_BASIC = "#";
		
		public Vector<ResolveInfo> apps = new Vector<ResolveInfo>();
		
		public AppClass(String i, String show){
			info = i;
			showInfo = show;
		}
		
		public void add(ResolveInfo ri) {
			apps.add(ri);
		}

		public int size() {
			return apps.size();
		}

		public ResolveInfo get(int j) {
			return apps.get(j);
		}
	}
	
	public Vector<AppClass> classify(Vector<AppItem> apps);
	
	public Vector<AppClass> getClassifiedList();
	
	public int getMaxNameLen();
	
	public void setScrollPos(int y);
	public int getScrollPos();
	
	public void setNeedToScroll(boolean s);
	public boolean getNeedToScroll();
}
