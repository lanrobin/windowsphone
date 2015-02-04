package com.axen.launcher.wp7.ui.widget;

import com.axen.launcher.app.TileItemInfo;
import com.axen.launcher.wp7.main.R;
import com.axen.launcher.wp7.main.WP7Configuration;
import com.axen.launcher.wp7.ui.size.Size;
import com.axen.utils.AXLog;
import com.axen.utils.ResourceUtil;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class CommonTile extends Tile {

	private static final String TAG = "CommonTile";

	private int mDrawableW = 0;
	private int mDrawableH = 0;
	private Drawable mIconDrawable = null;
	private WP7Configuration mConf = WP7Configuration.getInstance();
	private ResourceUtil mru = mConf.getRU();
	private Size size = Size.get();

	public CommonTile(Context context, TileItemInfo tii) {
		this(context, null, tii);
	}

	public CommonTile(Context context, AttributeSet attrs, TileItemInfo tii) {
		this(context, attrs, 0, tii);
	}

	public CommonTile(Context context, AttributeSet attrs, int defStyle,
			TileItemInfo tii) {
		super(context, attrs, defStyle);

		if (tii == null) {
			throw new NullPointerException(
					"CommonTile can not init with null TileItemInfo");
		}

		mInfo = tii;

		mAppName = new TextView(context, null, R.style.tile_name);
		// mAppName.setTextSize(getScaledValue(mAppName.getTextSize()));
		mIcon = new ImageView(context);

		mAppName.setBackgroundColor(Color.TRANSPARENT);

		setBackgroundColor(mConf.getAccentColor());

		// mAppName.setTypeface(Typeface.defaultFromStyle(R.style.tile_name));

		if (mInfo.isDefault || mInfo.isFixTile()) {
			mIconDrawable = mInfo.appDefaultIconDrawable;
		} else {
			mIconDrawable = mInfo.appIconDrawable;
		}

		// mDrawableW = mIconDrawable.getIntrinsicWidth();
		// mDrawableH = mIconDrawable.getIntrinsicHeight();
		mDrawableW = size.getTile_icon_w(); //mru.getPexil(R.dimen.tile_icon_w);
		mDrawableH = size.getTile_icon_h(); //mru.getPexil(R.dimen.tile_icon_h);

		// 如果是宽Tile,则要占用一行
		if (mInfo.wideTile) {
			int space = size.getTiles_space(); //mru.getPexil(R.dimen.tiles_space);
			mWidth = 2 * mWidth + space;
		}

		mAppName.setTextSize(size.getTile_name_font_h());
		setAppName(mInfo.tileName);
		// setAppIcon(R.drawable.app_ie);
		setAppIcon(mIconDrawable);
	}

	private TextView mAppName = null;
	private ImageView mIcon = null;

	private TileItemInfo mInfo = null;

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		if (mInfo == null) {
			Object obj = getTag();
			if (obj instanceof TileItemInfo) {
				mInfo = (TileItemInfo) obj;
			}
		}
		if (mInfo == null || mInfo.type == TileItemInfo.TYPE_INVALID) {
			AXLog.w(TAG, "mInfo is null or type == TYPE_INVALID");
			return;
		}

		View v = null;
		for (int i = 0; i < getChildCount(); i++) {
			v = getChildAt(i);
			v.measure(widthMeasureSpec, heightMeasureSpec);
		}

		// super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		setMeasuredDimension(mWidth, mHeight);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// int tvHeigth = mAppName.getMeasuredHeight();
		// mIcon.getDrawa
		// int iconWidth = mIcon.getMeasuredHeight();
		// int iconHeigth = mIcon.getMeasuredWidth();
		// int iconWidth = mIcon.getI;
		// int iconHeigth = getScaledValue(72);
		int vPad = getScaledValue(r - l - mDrawableW);
		vPad /= 2;
		int hPad = getScaledValue(b - t - mDrawableH);
		hPad /= 2;

		// 如果是全tile显示
		if (mInfo.fullSizeIcon()) {
			mIcon.layout(0, 0, mWidth, mHeight);
		} else {
			mIcon.layout(vPad, hPad, vPad + mDrawableW, hPad + mDrawableH);
		}
		
		mAppName.layout(
				getScaledValue(mAppNameLeftMargin),
				getScaledValue(mHeight - mAppNameH - mTileNameBottomMargin),
				getScaledValue(mWidth), getScaledValue(mHeight
						- mTileNameBottomMargin));

		if (mInfo.hideTileName()) {
			mAppName.setVisibility(INVISIBLE);
		} else {
			mAppName.setVisibility(VISIBLE);
		}

	}

	@Override
	public boolean isWideTile() {
		return mInfo.wideTile;
	}

	@Override
	public void setAppName(CharSequence name) {
		removeView(mAppName);
		mAppName.setTextSize(size.getTile_name_font_h());
		mAppName.setText(name);
		ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		addView(mAppName, lp);
	}

	@Override
	public void setAppIcon(Drawable icon) {
		removeView(mIcon);
		mIcon.setImageDrawable(icon);
		LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		addView(mIcon, lp);
	}

	@Override
	public void setAppIcon(int resId) {
		removeView(mIcon);
		mIcon.setImageResource(resId);
		LayoutParams lp = new LayoutParams(mDrawableW, mDrawableH);
		addView(mIcon, lp);
	}

	@Override
	public TileItemInfo getTii() {
		return mInfo;
	}

	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
		setBackgroundColor(mConf.getAccentColor());
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
