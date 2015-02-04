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

import com.axen.utils.AXLog;

/**
 * 信息管理类，主要是负责监听和报告当前系统的短信状态的。
 * 
 * @author lanhuanze
 * 
 */

public class MessageManager extends Manager {

	private static final String TAG = "MessageManager";

	private static final int MESSAGE_QUERY_SMS = 1;
	private static final int MESSAGE_QUERY_MMS = 2;

	private static final Uri SMS_URL = Uri.parse("content://sms");
	private static final Uri MMS_URL = Uri.parse("content://mms");
	private static final Uri MMS_INBOX_URL = Uri.parse("content://mms/inbox");

	private Context mContext = null;
	private ContentResolver mResolver = null;

	private List<MessageListener> listeners = null;

	private HandlerThread mThread = new HandlerThread(
			"ThreadHandler#MessageManager");
	
	private Handler mThreadHandler = null;
	private Handler.Callback mCallBack =new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			int unRead = 0;
			int num = 0;
			switch (msg.what) {
			case MESSAGE_QUERY_SMS:
				unRead = getUnreadSmsNumber();
				num = getSmsNumber();
				AXLog.d(TAG, "handleMessage sms, unRead " + unRead + ", num " + num);
				// 通知所有的Listeners.
				for (MessageListener l : listeners) {
					l.smsMessageChanged(num, unRead);
				}
				break;
			case MESSAGE_QUERY_MMS:
				unRead = getUnreadMmsNumber();
				num = getMmsNumber();
				
				AXLog.d(TAG, "handleMessage mms, unRead " + unRead + ", num " + num);
				
				// 通知所有的Listeners.
				for (MessageListener l : listeners) {
					l.mmsMessageChanged(num, unRead);
				}
				break;
			default:
				return false;
			}
			return true;
		}

	};

	/**
	 * 短信监听对象
	 */
	private ContentObserver mSmsObserver = new ContentObserver(mThreadHandler) {

		@Override
		public void onChange(boolean selfChange) {
			// 我们在这里通知mThreadHandler要开始查询短信了。
			// 避免太多的查询，所以要先将上一次还没处理完的消息删除。
			mThreadHandler.removeMessages(MESSAGE_QUERY_SMS);
			mThreadHandler.sendEmptyMessage(MESSAGE_QUERY_SMS);
			AXLog.d(TAG, "SMS database changed.");
		}

	};

	private ContentObserver mMmsObserver = new ContentObserver(mThreadHandler) {

		@Override
		public void onChange(boolean selfChange) {
			// 我们在这里通知mThreadHandler要开始查询短信了。
			mThreadHandler.removeMessages(MESSAGE_QUERY_MMS);
			mThreadHandler.sendEmptyMessage(MESSAGE_QUERY_MMS);
			AXLog.d(TAG, "SMS database changed.");
		}

	};

	private MessageManager() {
		AXLog.d(TAG, "Construct called.");
		mThread.start();
		mThreadHandler = new Handler(mThread.getLooper(), mCallBack);
	}

	private static final class SingletonHolder {
		public static final MessageManager _INSTANCE = new MessageManager();
	}

	/**
	 * 获得MessageManager的实例。
	 * 
	 * @return
	 */
	public static MessageManager getInstance() {
		return SingletonHolder._INSTANCE;
	}

	/**
	 * 初始化。如果没有初始化就调用其它的函数，会导致 IllegalStateException.
	 * 
	 * @param context
	 */
	@Override
	public synchronized void init(Context context) {
		if (context == null) {
			throw new NullPointerException("context in init can not be null.");
		}
		mContext = context;
		mResolver = context.getContentResolver();

		// 注册监听器
		mResolver.registerContentObserver(SMS_URL, true, mSmsObserver);
		mResolver.registerContentObserver(MMS_URL, true, mMmsObserver);
		listeners = new ArrayList<MessageListener>();
		// mThreadHandler.sendEmptyMessage(MESSAGE_QUERY_MMS);
		// mThreadHandler.sendEmptyMessage(MESSAGE_QUERY_SMS);
		mbInitialized = true;
	}

	/**
	 * 注册一个listener,当信息数据库有变化的时候会通知注册的listener.
	 * 
	 * @param l
	 */
	public void registerListener(MessageListener l) {
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
	public void unregisterListener(MessageListener l) {
		// 先检查状态。
		checkState();
		listeners.remove(l);
	}

	private int getUnreadSmsNumber() {
		Cursor c = mResolver.query(SMS_URL, null,"type = 1 and read = 0", null, null);
		if(c != null) {
			return c.getCount();
		}
		return 0;
	}

	private int getSmsNumber() {
		return 0;
	}

	private int getUnreadMmsNumber() {
		Cursor c = mResolver.query(MMS_INBOX_URL, null,  
                "read = 0", null, null);  
		if(c != null) {
			return c.getCount();
		}
		return 0;
	}

	private int getMmsNumber() {
		return 0;
	}
	
	@Override
	public void query() {
		mThreadHandler.sendEmptyMessage(MESSAGE_QUERY_SMS);
		mThreadHandler.sendEmptyMessage(MESSAGE_QUERY_MMS);
	}
}
