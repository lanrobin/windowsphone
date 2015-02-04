package com.axen.launcher.system;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.provider.CallLog;

import com.axen.utils.AXLog;

public class CallManager extends Manager {

	private static final String TAG = "CallManager";

	private static final int CALL_LOG_QUERY = 1;

	//private static final Uri MISSING_CALL = Uri
		//	.parse("content://call_log/calls");

	private Context mContext = null;
	private ContentResolver mResolver = null;

	private List<CallListener> listeners = null;

	private HandlerThread mThread = new HandlerThread(
			"ThreadHandler#CallManager");
	private Handler mThreadHandler = null;
	private Handler.Callback mHandlerCallback = new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			int missing = 0;
			int out = 0;
			int received = 0;
			int rejected = 0;
			switch (msg.what) {
			case CALL_LOG_QUERY:
				missing = getMissingCall();
				out = getOutCall();
				received = getReceivedCall();
				rejected = getRejectedCall();

				AXLog.d(TAG, "missing:" + missing + ", out:" + out
						+ ", received:" + received + ", rejected:" + rejected);

				for (CallListener l : listeners) {
					l.updateCallLog(missing, out, received, rejected);
				}
				return true;
			default:
				return false;
			}
		}
	};

	/**
	 * 短信监听对象
	 */
	private ContentObserver mCallObserver = new ContentObserver(mThreadHandler) {

		@Override
		public void onChange(boolean selfChange) {
			// 我们在这里通知mThreadHandler要开始查询短信了。
			// 避免太多的查询，所以要先将上一次还没处理完的消息删除。
			mThreadHandler.removeMessages(CALL_LOG_QUERY);
			mThreadHandler.sendEmptyMessage(CALL_LOG_QUERY);
			AXLog.d(TAG, "Call Log database changed.");
		}

	};

	private CallManager() {
		AXLog.d(TAG, "constructor called.");
		mThread.start();
		mThreadHandler = new Handler(mThread.getLooper(), mHandlerCallback);
	}

	private static final class SingletonHolder {
		public static final CallManager _INSTANCE = new CallManager();
	}

	/**
	 * 获得MessageManager的实例。
	 * 
	 * @return
	 */
	public static CallManager getInstance() {
		return SingletonHolder._INSTANCE;
	}

	@Override
	public void init(Context context) {
		if (context == null) {
			throw new NullPointerException("context in init can not be null.");
		}
		mContext = context;
		mResolver = mContext.getContentResolver();

		// 注册监听器
		mResolver.registerContentObserver(CallLog.CONTENT_URI, false,
				mCallObserver);
		listeners = new ArrayList<CallListener>();
		// mThreadHandler.sendEmptyMessage(CALL_LOG_QUERY);
		mbInitialized = true;
	}

	/**
	 * 注册一个listener,当信息数据库有变化的时候会通知注册的listener.
	 * 
	 * @param l
	 */
	public void registerListener(CallListener l) {
		// 先检查状态。
		checkState();

		if (l == null) {
			throw new NullPointerException(
					"MessageListener in registerListener can not be null.");
		}

		if (!listeners.contains(l)) {
			listeners.add(l);
		}
	}

	/**
	 * 从Manager中移除Listener.
	 * 
	 * @param l
	 */
	public void unregisterListener(CallListener l) {
		// 先检查状态。
		checkState();
		listeners.remove(l);
	}
	
	

	@Override
	public void query() {
		mThreadHandler.sendEmptyMessage(CALL_LOG_QUERY);
	}

	private int getMissingCall() {

		Cursor c = mResolver.query(CallLog.Calls.CONTENT_URI, null, CallLog.Calls.TYPE
				+ " = " + CallLog.Calls.MISSED_TYPE + " and " + CallLog.Calls.NEW + " = 1",
				null,
				CallLog.Calls.DEFAULT_SORT_ORDER);

		if (c != null) {
			return c.getCount();
		}
		return 0;
	}

	private int getReceivedCall() {
		return 0;
	}

	private int getOutCall() {
		return 0;
	}

	private int getRejectedCall() {
		return 0;
	}
}
