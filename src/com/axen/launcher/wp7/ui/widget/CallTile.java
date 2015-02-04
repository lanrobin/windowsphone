package com.axen.launcher.wp7.ui.widget;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.axen.launcher.app.TileItemInfo;
import com.axen.launcher.system.CallListener;
import com.axen.launcher.system.CallManager;
import com.axen.launcher.system.Manager;
import com.axen.utils.AXLog;

public class CallTile extends IndicatorTile implements CallListener {

	private static final String TAG = "CallTile";
	private CallManager mCallManager = null;

	public CallTile(Context context, Manager m, TileItemInfo tii) {
		super(context, m, tii);

		TelephonyManager manager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		String carrierName = manager.getNetworkOperatorName();
		if (!TextUtils.isEmpty(carrierName)) {
			setAppName(carrierName);
		}
		if (m instanceof CallManager) {
			mCallManager = (CallManager) m;
			mCallManager.registerListener(this);
			mHandler.postDelayed(new Runnable() {
				public void run() {
					mCallManager.query();
				}
			}, QUERY_DELAY_TIME);
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
	}

	@Override
	public void updateCallLog(int missing, int out, int received, int rejected) {
		AXLog.d(TAG, "missing = " + missing);
		final int missCall = missing;
		Runnable r = new Runnable() {
			public void run() {
				AXLog.d(TAG, "missCall = " + missCall);
				if (missCall > 0) {
					updateIndicator(Integer.toString(missCall), VISIBLE);
				} else {
					updateIndicator(null, INVISIBLE);
				}
			}
		};

		updateIndicator(r);
	}

}
