package com.axen.launcher.system;

import java.io.IOException;
import java.util.Random;
import java.util.Vector;

import com.axen.utils.AXLog;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore.Images.Media;

public class ImageManager extends Manager {

	private static final String TAG = "ImageManager";

	private static final int UPDATE_PHOTOS = 1;

	private Vector<Integer> mGallaryIds = new Vector<Integer>();

	private HandlerThread mThread = new HandlerThread(
			"ThreadHandler#MessageManager");

	private Handler mThreadHandler = null;
	private int mPhotoIndex = 0;

	private ContentResolver mResolver = null;
	private Context mContext = null;
	private Handler.Callback mCallBack = new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case UPDATE_PHOTOS:
				updateBitmaps();
				break;
			default:
				return false;
			}
			return true;
		}

	};

	private Random mRandom = new Random();

	private ImageManager() {
		AXLog.d(TAG, "Construct called.");
		mThread.start();
		mThreadHandler = new Handler(mThread.getLooper(), mCallBack);
	}

	private static final class Holder {
		public static final ImageManager _INSTANCE = new ImageManager();
	}

	public static ImageManager getInstance() {
		return Holder._INSTANCE;
	}

	private Integer getRandomId() {
		if (mGallaryIds.size() < 1) {
			return -1;
		}
		mPhotoIndex++;
		mPhotoIndex %= mGallaryIds.size();
		return mGallaryIds.get(mPhotoIndex);
	}

	public Bitmap getRandomBitmap(int width, int height) {
		int id = getRandomId();
		if (id == -1) {
			return null;
		}
		Bitmap bm = null;
		try {
			ParcelFileDescriptor fd = mResolver.openFileDescriptor(
					ContentUris.withAppendedId(Media.EXTERNAL_CONTENT_URI, id),
					"r");

			// 先获得长宽
			BitmapFactory.Options option = new BitmapFactory.Options();
			option.inJustDecodeBounds = true;
			BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor(), null,
					option);
			AXLog.d(TAG, "org w = " + option.outWidth + ", h = "
					+ option.outHeight + ", target w = " + width + ", h = "
					+ height);

			int wSimple = option.outWidth / width - 1;
			int hSimple = option.outHeight / height - 1;
			AXLog.d(TAG, "wSimple = " + wSimple + ", hSimple = " + hSimple);
			System.gc();
			option = new BitmapFactory.Options();
			option.inSampleSize = Math.min(wSimple, hSimple);
			if (option.inSampleSize < 2) {
				option.inSampleSize = 1;
			}
			bm = BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor(),
					null, option);
			AXLog.d(TAG,
					"after w = " + bm.getWidth() + ", h = " + bm.getHeight());
			fd.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bm;
	}

	public void updateBitmaps() {
		mGallaryIds.clear();
		try {
			Cursor c = Media.query(mResolver, Media.EXTERNAL_CONTENT_URI, null);
			if (c != null) {
				int idIndex = c.getColumnIndexOrThrow(Media._ID);
				if (c.getCount() > 0) {
					c.moveToFirst();
					while (c.moveToNext()) {
						mGallaryIds.add(c.getInt(idIndex));
					}
				}
			}
		} catch (Throwable e) {
			// 无论发生什么情况，我们都不能出错，最多就是取不到图片。

			mGallaryIds.clear();
		}
		if(mGallaryIds.size() > 0) {
		mPhotoIndex = mRandom.nextInt(mGallaryIds.size());
		}else {
			mPhotoIndex = 0;
		}
	}

	private ContentObserver mGalleryObserver = new ContentObserver(
			mThreadHandler) {

		@Override
		public void onChange(boolean selfChange) {
			// 我们在这里通知mThreadHandler要开始查询短信了。
			mThreadHandler.removeMessages(UPDATE_PHOTOS);
			mThreadHandler.sendEmptyMessage(UPDATE_PHOTOS);
			AXLog.d(TAG, "SMS database changed.");
		}

	};

	@Override
	public void init(Context context) {
		if (context == null) {
			throw new NullPointerException(
					"ImageManager.init with null context");
		}
		mResolver = context.getContentResolver();
		mResolver.registerContentObserver(Media.EXTERNAL_CONTENT_URI, true,
				mGalleryObserver);
		mContext = context;
		mbInitialized = true;
	}

	@Override
	public void query() {
		mThreadHandler.sendEmptyMessage(UPDATE_PHOTOS);
	}

}
