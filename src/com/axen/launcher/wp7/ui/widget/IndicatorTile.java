package com.axen.launcher.wp7.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

import com.axen.launcher.app.TileItemInfo;
import com.axen.launcher.system.Manager;
import com.axen.launcher.wp7.main.R;
import com.axen.launcher.wp7.main.WP7App;
import com.axen.launcher.wp7.main.WP7Configuration;
import com.axen.launcher.wp7.ui.size.Size;
import com.axen.utils.AXLog;
import com.axen.utils.ResourceUtil;

public abstract class IndicatorTile extends Tile {

	private static final String TAG = "IndicatorTile";

	protected Manager mManager;
	private TileItemInfo mInfo = null;

	protected TextView mTileName = null;
	protected ImageView mIcon = null;
	protected TextView mIndicator = null;
	
	private WP7Configuration mConf = WP7Configuration.getInstance();
	private ResourceUtil mru = mConf.getRU();
	private Size size = Size.get();

	private Drawable mIconDrawable = null;

	private int mDrawableW = 0;
	private int mDrawableH = 0;

	protected Handler mHandler = new Handler();

	protected final static int QUERY_DELAY_TIME = 2500;
	protected final static int REQUEST_LAYOUT_DELAY_TIME = 500;

	public IndicatorTile(Context context, Manager m, TileItemInfo tii) {
		this(context, null, m, tii);
	}

	public IndicatorTile(Context context, AttributeSet attrs, Manager m,
			TileItemInfo tii) {
		this(context, attrs, 0, m, tii);
	}

	public IndicatorTile(Context context, AttributeSet attrs, int defStyle,
			Manager m, TileItemInfo tii) {
		super(context, attrs, defStyle);
		mManager = m;
		mInfo = tii;

		mDrawableW = size.getTile_icon_w(); //mru.getPexil(R.dimen.tile_icon_w);
		mDrawableH = size.getTile_icon_h(); //mru.getPexil(R.dimen.tile_icon_h);

		mTileName = new TextView(context, null, R.style.tile_name);
		// mAppName.setTextSize(getScaledValue(mAppName.getTextSize()));
		mIcon = new ImageView(context);

		mIndicator = new TextView(context, null, R.style.tile_indicator);
		setBackgroundColor(mConf.getAccentColor());

		// 如果是宽Tile,则要占用一行
		if (mInfo.wideTile) {
			int space = size.getTiles_space(); //mru.getPexil(R.dimen.tiles_space);
			mWidth = 2 * mWidth + space;
		}

		if (mInfo.isDefault || mInfo.isFixTile()) {
			mIconDrawable = mInfo.appDefaultIconDrawable;
		} else {
			mIconDrawable = mInfo.appIconDrawable;
		}

		setIndicator(null);
		setAppName(mInfo.tileName);
		// setAppIcon(R.drawable.app_ie);
		setAppIcon(mIconDrawable);
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
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		AXLog.d(TAG, "onLayout l =" + l + ", t = " + t + ", r = " + r
				+ ", b = " + b);
		if (mIndicator == null || mIcon == null || mTileName == null) {
			AXLog.d(TAG, "onLayout null return");
			return;
		}

		// int tvHeigth = mTileName.getMeasuredHeight();
		if (mIndicator.getVisibility() != VISIBLE) {
			AXLog.d(TAG, "indicatorText = " + mIndicator.getText());
			int vPad = getScaledValue(r - l - mDrawableW);
			vPad /= 2;
			int hPad = getScaledValue(b - t - mDrawableH);
			hPad /= 2;
			mTileName.layout(getScaledValue(mAppNameLeftMargin), getScaledValue(mHeight - mAppNameH - mTileNameBottomMargin),
					getScaledValue(mWidth), getScaledValue(mHeight - mTileNameBottomMargin));

			mIcon.layout(vPad, hPad, vPad + mDrawableW, hPad + mDrawableH);

			mIndicator.layout(vPad + mDrawableW, hPad, vPad + mDrawableW, hPad
					+ mDrawableH);
		} else {
			int indicatorW = mDrawableW / 2; // mIndicator.getWidth();
			int vPad = getScaledValue(r - l - (mDrawableW + indicatorW));
			vPad /= 2;
			int hPad = getScaledValue(b - t - mDrawableH);
			hPad /= 2;

			mTileName.layout(getScaledValue(mAppNameLeftMargin), getScaledValue(mHeight - mAppNameH - mTileNameBottomMargin),
					getScaledValue(mWidth), getScaledValue(mHeight - mTileNameBottomMargin));
			mIcon.layout(vPad, hPad, vPad + mDrawableW, hPad + mDrawableH);
			mIndicator.layout(vPad + mDrawableW, hPad, vPad + mDrawableW
					+ indicatorW, hPad + mDrawableH);
		}

	}

	@Override
	public void setAppName(CharSequence name) {
		removeView(mTileName);
		mTileName.setText(name);
		mTileName.setTextSize(size.getTile_name_font_h());
		ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		addView(mTileName, lp);
	}

	public void setIndicator(CharSequence ind) {
		AXLog.d(TAG, "setIndicator ind = " + ind + ".");
		removeView(mIndicator);
		mIndicator.setText(ind);
		// mIndicator.setTextSize(mru.getPexil(R.dimen.tile_indicator_font_h));
		mIndicator.setTextSize(size.getTile_indicator_font_h());
		mIndicator.setGravity(Gravity.TOP);
		mIndicator.setVisibility(INVISIBLE);
		ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		addView(mIndicator, lp);
		AXLog.d(TAG, "setIndicator finished");
		// invalidate();
	}

	public void updateIndicator(CharSequence ind, int visibility) {
		AXLog.d(TAG, "updateIndicator ind = " + ind + "visibility = "
				+ visibility);
		LayoutParams lp = mIndicator.getLayoutParams();
		if (visibility == VISIBLE) {
			lp.width = mWidth / 2;
		} else {
			lp.width = LayoutParams.WRAP_CONTENT;
		}
		lp.height = LayoutParams.WRAP_CONTENT;
		AXLog.d(TAG, "lp.width =" + lp.width);

		mIndicator.setText(ind);
		// mIndicator.setTextSize(mru.getPexil(R.dimen.tile_indicator_font_h));
		mIndicator.setTextSize(size.getTile_indicator_font_h());
		mIndicator.setGravity(Gravity.TOP);
		mIndicator.setVisibility(visibility);

		mIndicator.setLayoutParams(lp);
		AXLog.d(TAG, "updateIndicator finish, text = " + mIndicator.getText());
	}

	@Override
	public void setAppIcon(Drawable icon) {
		mIcon.setImageDrawable(icon);
		LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		addView(mIcon, lp);
	}

	@Override
	public void setAppIcon(int resId) {
		mIcon.setImageResource(resId);
		LayoutParams lp = new LayoutParams(mDrawableW, mDrawableH);
		addView(mIcon, lp);
	}

	public void updateIndicator(Runnable r) {
		mHandler.post(r);
		mHandler.postDelayed(new Runnable() {
			public void run() {
				requestLayout();
				AXLog.d(TAG, "updateIndicator requestLayout");
			}

		}, REQUEST_LAYOUT_DELAY_TIME);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		AXLog.d(TAG, "onMeasure");
	}

	@Override
	public void requestLayout() {
		// TODO Auto-generated method stub
		super.requestLayout();
		AXLog.d(TAG, "requestLayout");
	}

	@Override
	public void draw(Canvas canvas) {
		setBackgroundColor(mConf.getAccentColor());
		super.draw(canvas);
	}

	@Override
	public void pauseBackgroundThread() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resumeBackgroundThread() {
		// TODO Auto-generated method stub
		
	}
	
	
}
