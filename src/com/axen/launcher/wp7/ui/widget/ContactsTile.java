package com.axen.launcher.wp7.ui.widget;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import com.axen.launcher.app.TileItemInfo;
import com.axen.launcher.system.ContactsManager;
import com.axen.launcher.wp7.main.R;
import com.axen.launcher.wp7.main.WP7App;
import com.axen.launcher.wp7.main.WP7Configuration;
import com.axen.launcher.wp7.ui.anim.Rotate3DAnimation;
import com.axen.launcher.wp7.ui.size.Size;
import com.axen.utils.AXLog;
import com.axen.utils.HSL;
import com.axen.utils.ImageUtil;
import com.axen.utils.ResourceUtil;

public class ContactsTile extends Tile implements Animation.AnimationListener {

	private static final String TAG = "ContactsTile";

	private static final int ROW_NUMBER = 3;
	private static final int COL_NUMBER = 3;
	private WP7Configuration mConf = WP7Configuration.getInstance();
	private ResourceUtil mru = mConf.getRU();
	
	private Size size = Size.get();

	private Random mRandom = new Random();

	private ImageView[] mIV = null;
	private int[] mColors = new int[COL_NUMBER];
	private Drawable[] mDrawables = new Drawable[ROW_NUMBER * COL_NUMBER];

	// 这是animation开始的时间
	private long[] mAnimStartTime = new long[] { 1000, 1500, 2500, 4250, 6000,
			7000, 9000, 8500, 500 };

	private TileItemInfo mInfo = null;

	private TextView mTileName = null;

	private ContactsManager mContactsManager = ContactsManager.getInstance();

	private static final int VIEW_UPDATE_INTERVAL = 10000;

	private static final long QUERY_DELAY_TIME = 1000;

	// private boolean mContinueing = true;

	private Rotate3DAnimation mAnim = new Rotate3DAnimation(180, 0, 0, 0, 0,
			0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
			0.5f);

	private Handler mHandler = new Handler();
	private UpdateViewThread mUpdateViewThread = new UpdateViewThread(
			"ContactsTile#mUpdateViewThread");

	private class UpdateViewThread extends Thread {
		public UpdateViewThread(String name) {
			super(name);
		}

		private boolean mContinueing = true;

		public void discard() {
			mContinueing = false;
		}

		public void run() {
			while (mContinueing) {
				if (mContactsManager.getPhotoUriSize() > 0) {
					/**
					 * 这里我们要把图片填上去
					 */
					for (int i = 0; i < mDrawables.length; i++) {
						mDrawables[i] = null;
					}

					int mode = mRandom.nextInt(3);
					if (mode == 2) {
						generateBackgroundTwo(2);
						generateBackgroundOne(5);
					} else if (mode == 1) {
						generateBackgroundOne(9);
						generateBackgroundOne(8);
					}

				}
				mHandler.post(new Runnable() {
					public void run() {
						updateChildBackground();
					}
				});
				try {
					Thread.sleep(VIEW_UPDATE_INTERVAL);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	};

	private void generateBackgroundOne(int leftSlots) {
		int id = mContactsManager.getRodamStringId();
		InputStream is = mContactsManager.getPhotoDataById(id);

		// 把图片分成四个
		if (is != null) {
			byte[] data = null;
			try {
				data = new byte[is.available()];
				is.read(data);
				Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length);
				Bitmap newBmp = ImageUtil.getResizedBitmap(bm, mWidth
						/ ROW_NUMBER + 1, mHeight / COL_NUMBER + 1);
				int fill = mRandom.nextInt(leftSlots + 1);
				int nullNum = 0;
				for (int i = 0; i < mDrawables.length; i++) {
					if (mDrawables[i] == null) {
						nullNum++;
						if (fill == nullNum) {
							mDrawables[i] = new BitmapDrawable(getContext()
									.getResources(), newBmp);

							// 只填充一个
							break;
						}
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void generateBackgroundTwo(int num) {
		int id = mContactsManager.getRodamStringId();
		InputStream is = mContactsManager.getPhotoDataById(id);

		// 把图片分成四个
		if (is != null) {
			byte[] data = null;
			try {
				int splitNumber = num;
				data = new byte[is.available()];
				is.read(data);
				Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length);
				Bitmap newBmp = ImageUtil.getResizedBitmap(bm, mWidth
						* splitNumber / ROW_NUMBER, mHeight * splitNumber
						/ COL_NUMBER);
				Bitmap[] v = ImageUtil.splitBitmap(newBmp, splitNumber
						* splitNumber);
				if (v != null && v.length == splitNumber * splitNumber) {
					/**
					 * 把图片填上去 1 1 0 1 1 0 0 0 0
					 */
					int startFillX = mRandom.nextInt(2);
					int startFillY = mRandom.nextInt(2);
					for (int i = 0; i < splitNumber; i++) {
						for (int j = 0; j < splitNumber; j++) {
							mDrawables[(startFillY + j) * COL_NUMBER + i
									+ startFillX] = new BitmapDrawable(
									getContext().getResources(), v[j
											* splitNumber + i]);
						}
					}

				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public ContactsTile(Context context, TileItemInfo tii) {
		super(context);
		mInfo = tii;
		mIV = new ImageView[ROW_NUMBER * COL_NUMBER];

		int cw = mWidth / COL_NUMBER;
		int ch = mHeight / COL_NUMBER;
		updatePlatte();
		ViewGroup.LayoutParams lp = null;
		// 生成九个小板
		for (int i = 0; i < ROW_NUMBER * COL_NUMBER; i++) {
			lp = new ViewGroup.LayoutParams(cw, ch);
			mIV[i] = new ImageView(context);
			mIV[i].setLayoutParams(lp);
			mIV[i].setBackgroundColor(mColors[mRandom.nextInt(COL_NUMBER)]);
			// tag是它自己的序号
			mIV[i].setTag(i);
			addView(mIV[i], lp);
		}

		mTileName = new TextView(context, null, R.style.tile_name);
		setBackgroundColor(R.color.white_color);
		setAppName(mInfo.tileName);
		updatePlatte();
		updateChildBackground();
		mTileName.bringToFront();

		mAnim.setFillAfter(false);
		mAnim.setFillBefore(false);
		mAnim.setDuration(1000);

		mHandler.postDelayed(new Runnable() {
			public void run() {
				mContactsManager.query();
			}
		}, QUERY_DELAY_TIME);
	}

	@Override
	public TileItemInfo getTii() {
		return mInfo;
	}

	@Override
	public boolean isWideTile() {
		return mInfo.wideTile;
	}

	@Override
	public void setAppName(CharSequence name) {
		mTileName.setTextSize(size.getTile_name_font_h());
		mTileName.setText(name);
		ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		addView(mTileName, lp);
	}

	@Override
	public void setAppIcon(Drawable icon) {

	}

	@Override
	public void setAppIcon(int resId) {

	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int cw = mWidth / COL_NUMBER;
		int ch = mHeight / COL_NUMBER;
		int leftW = mWidth - cw * COL_NUMBER; // 这是剩余的，因为不会整除。
		int leftH = mHeight - ch * COL_NUMBER; // 而且数字只可能为0，1，2
		int cl = 0;
		int ct = 0;
		int cr = 0;
		int cb = 0;
		AXLog.d(TAG, "---------\n");
		for (int i = 0; i < mIV.length; i++) {
			// 如果是翻转的小view
			cl = (i % COL_NUMBER) * cw;
			ct = (i / COL_NUMBER) * ch;
			cr = (i % COL_NUMBER + 1) * cw;
			cb = (i / COL_NUMBER + 1) * ch;

			if (leftW > 1) {
				cr += 1;
				cb += 1;
			}

			AXLog.d(TAG, "cl=" + cl + ", ct=" + ct + ",cr=" + cr + ", cb=" + cb);
			mIV[i].layout(cl, ct, cr, cb);
			/*
			 * mIV[i].layout(l + (i % COL_NUMBER) * cw, t + (i / COL_NUMBER) *
			 * ch, l + (i % COL_NUMBER + 1) * cw, (i / COL_NUMBER + 1) ch);
			 */
		}

		mTileName.layout(getScaledValue(mAppNameLeftMargin),
				getScaledValue(mHeight - mAppNameH - mTileNameBottomMargin),
				getScaledValue(mWidth), getScaledValue(mHeight
						- mTileNameBottomMargin));
	}

	/**
	 * 生成调色板
	 */
	private void updatePlatte() {
		// int [] baseColor = new int[ROW_NUMBER];
		int color = mConf.getAccentColor();
		float temp = 0;
		// 根据 HSL模式来算效果比较好
		HSL hsl = HSL.fromARGB(color);
		temp = 100 - hsl.getL();
		float orgL = hsl.getL();
		// 先算出三种基色
		for (int i = 0; i < ROW_NUMBER; i++) {
			hsl.setL(orgL + (float) (temp * 0.25 * (mRandom.nextInt(4) + 1)));
			mColors[i] = HSL.HSL2ARGB(hsl);
		}
	}

	private void updateChildBackground() {
		Rotate3DAnimation a = null;

		// 先随机交换
		long tmp = 0;
		int swapIndex = 0;
		for (int i = 0; i < mAnimStartTime.length; i++) {
			swapIndex = mRandom.nextInt(mAnimStartTime.length);
			tmp = mAnimStartTime[i];
			mAnimStartTime[i] = mAnimStartTime[swapIndex];
			mAnimStartTime[swapIndex] = tmp;
		}

		for (int i = 0; i < mIV.length; i++) {
			final int index = i;
			try {
				a = mAnim.clone();
			} catch (CloneNotSupportedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			final Rotate3DAnimation ra = a;
			// mIV[i].setImageDrawable(null);
			// mIV[i].setBackgroundColor(mColors[mRandom.nextInt(COL_NUMBER)]);
			// mIV[i].setImageResource(R.drawable.accenter_color_brown);
			// mIV[i].invalidate();
			if (a != null) {
				a.setAttachView(mIV[i]);
				a.setAnimationListener(this);
				mHandler.postDelayed(new Runnable() {
					public void run() {
						mIV[index].startAnimation(ra);
					}
				}, mAnimStartTime[i]);

			}

			/**
			 * if (mDrawables[i] != null) {
			 * mIV[i].setImageDrawable(mDrawables[i]); }
			 */
		}
	}

	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
		updatePlatte();
		// updateChildBackground();
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		mUpdateViewThread.start();
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		// mContinueing = false;
		if (mUpdateViewThread != null) {
			mUpdateViewThread.discard();
			mUpdateViewThread = null;
		}
	}

	@Override
	public void pauseBackgroundThread() {
		// 停止照片更新线程
		if (mUpdateViewThread != null) {
			mUpdateViewThread.discard();
			mUpdateViewThread = null;
		}
	}

	@Override
	public void resumeBackgroundThread() {

		// 重新开始
		mUpdateViewThread = new UpdateViewThread(
				"ContactsTile#mUpdateViewThread");

		mUpdateViewThread.start();
	}

	@Override
	public void onAnimationEnd(Animation animation) {

	}

	@Override
	public void onAnimationRepeat(Animation animation) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAnimationStart(Animation animation) {
		Rotate3DAnimation a = (Rotate3DAnimation) animation;
		final ImageView iv = (ImageView) a.getAttachView();
		if (iv != null) {
			final Integer index = (Integer) iv.getTag();
			iv.setImageDrawable(null);
			mHandler.postDelayed(new Runnable() {
				public void run() {
					iv.setImageDrawable(mDrawables[index]);
					iv.setBackgroundColor(mColors[mRandom.nextInt(COL_NUMBER)]);
				}
			}, 500);
			
		}
	}

}
