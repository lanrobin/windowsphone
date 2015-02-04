package com.axen.utils;

import android.util.Log;

public class AXLog {
	
	/**
	 * 是不是要DEBUG；
	 */
	private static final boolean DEBUG = false;
	
	/**
	 * 封闭Log.d，以使用方便，并且能在Release的时候去掉。
	 * @param TAG
	 * @param msg
	 */
	public static void d(String TAG, String msg) {
		if(DEBUG) {
			Log.d(TAG, msg);
		}
	}
	
	/**
	 * 
	 */
	public static void w(String TAG, String msg) {
		Log.w(TAG, msg);
	}
}
