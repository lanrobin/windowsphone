package com.axen.launcher.system;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.axen.utils.AXLog;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.provider.ContactsContract;
import android.text.TextUtils;

public class ContactsManager extends Manager {

	private ContentResolver mResolver = null;
	private Context mContext = null;

	private Object mListLocker = new Object();
	private List<Integer> mListPhotoIds = new ArrayList<Integer>();

	private final static int PHOTOS_CHANGED = 1;
	private static final String TAG = "ContactsManager";

	private HandlerThread mThread = new HandlerThread(
			"ThreadHandler#ContactsManager");

	private Handler mThreadHandler = null;

	private Random mRandom = new Random();

	private Handler.Callback mCallBack = new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case PHOTOS_CHANGED:
				updatePhotoPeople();
				return true;
			}
			return false;
		}

	};

	private ContactsManager() {
		AXLog.d(TAG, "Construct called.");
		mThread.start();
		mThreadHandler = new Handler(mThread.getLooper(), mCallBack);
	}

	@Override
	public void init(Context context) {
		if (context == null) {
			throw new NullPointerException(
					"ContactsManager.init with null pointer.");
		}
		mContext = context;
		mResolver = mContext.getContentResolver();
		mResolver.registerContentObserver(
				ContactsContract.Contacts.CONTENT_URI, true, mPhotoObserver);
		// mThreadHandler.sendEmptyMessage(PHOTOS_CHANGED);
		mbInitialized = true;
	}

	private ContentObserver mPhotoObserver = new ContentObserver(mThreadHandler) {

		@Override
		public void onChange(boolean selfChange) {
			// 我们在这里通知mThreadHandler要开始查询短信了。
			mThreadHandler.removeMessages(PHOTOS_CHANGED);
			mThreadHandler.sendEmptyMessage(PHOTOS_CHANGED);
			AXLog.d(TAG, "Photo database changed.");
		}

	};

	private static final class SingletonHolder {
		public static final ContactsManager _INSTANCE = new ContactsManager();
	}

	/**
	 * 获得MessageManager的实例。
	 * 
	 * @return
	 */
	public static ContactsManager getInstance() {
		return SingletonHolder._INSTANCE;
	}

	public Integer getRodamStringId() {
		int len = 0;
		int index = -1;
		Integer id = -1;
		synchronized (mListLocker) {
			if (mListPhotoIds != null) {
				len = mListPhotoIds.size();
				index = mRandom.nextInt(len);
				id = mListPhotoIds.get(index);
			}
		}
		return id;
	}

	public InputStream getPhotoDataById(int contactsId) {
		Uri uri = ContentUris.withAppendedId(
				ContactsContract.Contacts.CONTENT_URI, contactsId);
		InputStream is = null;
		is = ContactsContract.Contacts.openContactPhotoInputStream(mResolver,
				uri);
		return is;
	}

	public void updatePhotoPeople() {
		synchronized (mListLocker) {
			Cursor c = mResolver.query(ContactsContract.Contacts.CONTENT_URI,
					new String[] { ContactsContract.Contacts._ID,
							ContactsContract.Contacts.PHOTO_ID }, null, null,
					null);
			int index = c.getColumnIndexOrThrow(ContactsContract.Contacts._ID);
			int p = c.getColumnIndexOrThrow(ContactsContract.Contacts.PHOTO_ID);
			mListPhotoIds.clear();
			if (c != null) {
				c.moveToFirst();
				while (c.moveToNext()) {
					if (!TextUtils.isEmpty(c.getString(p))) {
						mListPhotoIds.add(c.getInt(index));
					}

					//AXLog.d(TAG, "With Photo Id:" + c.getInt(index)
						//	+ ", photo_id = " + c.getString(p));
				}
			}
		}
	}

	public int getPhotoUriSize() {
		return mListPhotoIds.size();
	}

	@Override
	public void query() {
		mThreadHandler.sendEmptyMessage(PHOTOS_CHANGED);
	}
}
