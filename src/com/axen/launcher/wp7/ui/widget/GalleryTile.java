package com.axen.launcher.wp7.ui.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import com.axen.launcher.app.TileItemInfo;
import com.axen.launcher.system.ImageManager;
import com.axen.launcher.wp7.main.R;
import com.axen.launcher.wp7.main.WP7App;
import com.axen.launcher.wp7.main.WP7Configuration;
import com.axen.launcher.wp7.ui.size.Size;
import com.axen.utils.ImageUtil;
import com.axen.utils.ResourceUtil;

public class GalleryTile extends Tile {

	private TileItemInfo mInfo = null;
	
	private WP7Configuration mConf = WP7Configuration.getInstance();
	private ResourceUtil mru = mConf.getRU();
	private Size size = Size.get();

	private ImageView mIV = null;
	private TextView mTileName = null;

	private int mTileSpaces = 0;

	private ImageManager mManager = ImageManager.getInstance();

	private Handler mHandler = new Handler();

	private static final int PREPARED_NUMBER = 3;

	private static final int UPDATE_INTERVAL = 15000; // 更新图片的时间

	private Bitmap[] mPreparedBitmaps = new Bitmap[PREPARED_NUMBER];

	private UpdateViewThread mThread = new UpdateViewThread(
			"GalleryTile#UpdateViewThread");

	private AnimationSet mAnimSet = new AnimationSet(false);

	private int mAnimRepeatTime = 0;

	private AnimationListener mAnimLis1 = new AnimationListener() {

		@Override
		public void onAnimationEnd(Animation animation) {

		}

		@Override
		public void onAnimationRepeat(Animation animation) {

		}

		@Override
		public void onAnimationStart(Animation animation) {
			mIV.setImageBitmap(mPreparedBitmaps[mAnimRepeatTime]);
		}

	};

	private AnimationListener mAnimLis2 = new AnimationListener() {

		@Override
		public void onAnimationEnd(Animation animation) {
			mAnimRepeatTime++;
			if (mAnimRepeatTime >= PREPARED_NUMBER) {
				updateImage();
			} else {
				mIV.startAnimation(mAnimSet);
			}
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onAnimationStart(Animation animation) {
			// TODO Auto-generated method stub

		}

	};

	private class UpdateViewThread extends Thread {
		public UpdateViewThread(String name) {
			super(name);
		}

		private boolean mContinueing = true;
		
		// 在没有图片的情况下，要用这几张图片来填充。
		private int[] defaultImags = new int[] {
				R.raw.default_1, R.raw.default_2, R.raw.default_3
		};

		public void discard() {
			mContinueing = false;
		}

		public void run() {
			while (mContinueing) {

				Bitmap bm = null;
				for (int i = 0; i < PREPARED_NUMBER; i++) {
					bm = mManager.getRandomBitmap(mWidth, 2 * mHeight);
					
					if(bm == null) {
						bm = BitmapFactory.decodeStream(getContext().getResources().openRawResource(defaultImags[i]));
					}
					if (bm != null) {
						// 在这里我们要对大的图片进行裁剪和缩放，使之能刚好放入gallery view
						if (bm.getWidth() > mWidth
								&& bm.getHeight() > 2 * mHeight) {
							int left = bm.getWidth() - mWidth;
							left >>= 1;
							bm = Bitmap.createBitmap(bm, left, 0, mWidth,
									2 * mHeight);
						}

						mPreparedBitmaps[i] = bm;
					} 
				}

				mHandler.post(new Runnable() {
					public void run() {
						updateImage();
					}
				});

				try {
					Thread.sleep(UPDATE_INTERVAL * PREPARED_NUMBER);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	};

	public GalleryTile(Context context, TileItemInfo tii) {
		super(context);
		mInfo = tii;
		mIV = new ImageView(context);
		mTileName = new TextView(context, null, R.style.tile_name);

		mTileSpaces = size.getTiles_space(); //mru.getPexil(R.dimen.tiles_space);

		// 如果是宽的Tile，则占用整行
		if (isWideTile()) {
			mWidth += (mTileSpaces + mWidth);
		}

		LayoutParams lp = new LayoutParams(mWidth, mHeight * 2);
		addView(mIV, lp);
		mIV.setBackgroundColor(mConf.getAccentColor());
		setBackgroundColor(mConf.getAccentColor());
		mManager.query();
		mThread.start();

		setAppName(mInfo.tileName);
		
		TranslateAnimation anim = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
				0.0f, Animation.RELATIVE_TO_SELF, 1.0f,
				Animation.RELATIVE_TO_SELF, 0f);
		anim.setDuration(500);
		anim.setStartOffset(0);
		anim.setAnimationListener(mAnimLis1);
		mAnimSet.addAnimation(anim);

		anim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
				Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
				0f, Animation.RELATIVE_TO_SELF, -0.5f);
		anim.setDuration(14500);
		anim.setStartOffset(500);
		anim.setAnimationListener(mAnimLis2);
		mAnimSet.addAnimation(anim);
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
		removeView(mTileName);
		mTileName.setText(name);
		mTileName.setTextSize(size.getTile_name_font_h());
		LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		addView(mTileName, lp);
		mTileName.bringToFront();
	}

	@Override
	public void setAppIcon(Drawable icon) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setAppIcon(int resId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void pauseBackgroundThread() {
		if (mThread != null) {
			mThread.discard();
			mThread = null;
		}
	}

	@Override
	public void resumeBackgroundThread() {
		mThread = new UpdateViewThread("GalleryTile#UpdateViewThread");
		mThread.start();
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		mIV.layout(0, 0, mWidth, mHeight * 2);
		mTileName.layout(getScaledValue(mAppNameLeftMargin),
				getScaledValue(mHeight - mAppNameH - mTileNameBottomMargin),
				getScaledValue(mWidth), getScaledValue(mHeight
						- mTileNameBottomMargin));
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		updateImage();
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		mIV.clearAnimation();
		pauseBackgroundThread();
	}

	public void updateImage() {
		mAnimRepeatTime = 0;
		mIV.clearAnimation();
		mIV.startAnimation(mAnimSet);
	}
	
	@Override
	public void draw(Canvas canvas) {
		setBackgroundColor(mConf.getAccentColor());
		mIV.setBackgroundColor(mConf.getAccentColor());
		super.draw(canvas);
	}
}
