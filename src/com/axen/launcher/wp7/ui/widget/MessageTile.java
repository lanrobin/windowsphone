package com.axen.launcher.wp7.ui.widget;

import android.content.Context;

import com.axen.launcher.app.TileItemInfo;
import com.axen.launcher.system.Manager;
import com.axen.launcher.system.MessageListener;
import com.axen.launcher.system.MessageManager;
import com.axen.utils.AXLog;

public class MessageTile extends IndicatorTile implements MessageListener {

	private static final String TAG = "MessageTile";
	private int mNewSmsCount = 0;
	private int mNewMmsCount = 0;
	private MessageManager mMessageManager = null;

	public MessageTile(Context context, Manager m, TileItemInfo tii) {
		super(context, m, tii);

		if (m instanceof MessageManager) {
			mMessageManager = (MessageManager) m;
			mMessageManager.registerListener(this);
			mMessageManager.query();
		}
	}

	@Override
	public void smsMessageChanged(int smsNumber, int newSmsNumber) {
		AXLog.d(TAG, "newSmsNumber = " + newSmsNumber);
		mNewSmsCount = newSmsNumber;
		final int count = mNewSmsCount + mNewMmsCount;
		Runnable r = new Runnable() {
			public void run() {
				if (count > 0) {
					updateIndicator(Integer.toString(count), VISIBLE);
				} else {
					updateIndicator(null, INVISIBLE);
				}

			}
		};
		updateIndicator(r);
	}

	@Override
	public void mmsMessageChanged(int mmsNumber, int newMmsNumber) {
		AXLog.d(TAG, "newMmsNumber = " + newMmsNumber);
		mNewMmsCount = newMmsNumber;
		final int count = mNewSmsCount + mNewMmsCount;
		Runnable r = new Runnable() {
			public void run() {
				if (count > 0) {
					updateIndicator(Integer.toString(count), VISIBLE);
				} else {
					updateIndicator(null, GONE);
				}

			}
		};
		updateIndicator(r);
	}

}
