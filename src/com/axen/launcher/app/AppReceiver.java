package com.axen.launcher.app;

import com.axen.utils.AXLog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AppReceiver extends BroadcastReceiver {

	private static final String TAG = "AppReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		if (context == null || intent == null) {
			AXLog.w(TAG, "context = " + context + ", intent = " + intent);
			return;
		}

		String action = intent.getAction();

		if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {

		} else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {

		} else if (Intent.ACTION_PACKAGE_CHANGED.equals(action)) {
			
		} else {
			AXLog.d(TAG, "Unknow action:" + action);
		}
	}

}
