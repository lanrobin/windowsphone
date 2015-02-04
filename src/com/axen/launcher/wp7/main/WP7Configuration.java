package com.axen.launcher.wp7.main;

import com.axen.launcher.app.AccentsManager;
import com.axen.utils.ResourceUtil;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.WindowManager;

public class WP7Configuration {

	private Context mContext = null;

	public static final int BACKGROUND_COLOR_BLACK = 0;
	public static final int BACKGROUND_COLOR_WHITE = 1;

	private WP7Configuration() {

	}

	private static class Holder {
		public static final WP7Configuration _INSTANCE = new WP7Configuration();
	}

	public static WP7Configuration getInstance() {
		return Holder._INSTANCE;
	}

	public void init(Context c) {
		if (c == null) {
			throw new NullPointerException(
					"WP7Configuration.init with null pointer.");
		}
		mContext = c;
		Display display = ((WindowManager) mContext
				.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

		mScreenH = display.getHeight();
		mScreenW = display.getWidth();
		mru = new ResourceUtil(mContext);

		mStatusbarH = (int) Math.ceil(25 * mContext.getResources()
				.getDisplayMetrics().density);

		mThemeColor = mru.getColor(R.color.accent_color_teal);
		mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		mThemeColor = mPrefs.getInt(THEME_COLOR, mThemeColor);
		mShowStatusbar = mPrefs.getBoolean(SHOW_STATUSBAR, false);

		mBackGroundColorMode = mPrefs.getInt(BACKGROUND_COLOR_MODE,
				mBackGroundColorMode);

		// 可能会是升级的问题,所以要这样处理。
		if (mBackGroundColorMode != BACKGROUND_COLOR_BLACK
				&& mBackGroundColorMode != BACKGROUND_COLOR_WHITE) {
			mBackGroundColorMode = BACKGROUND_COLOR_BLACK;
		}
		
		// 测试用
		// mBackGroundColorMode = BACKGROUND_COLOR_WHITE;

		setBackgroundColorMode(mBackGroundColorMode);

		mThemeColorIndex = AccentsManager.getIndexFromColor(mContext,
				mThemeColor);
	}

	public int getScreenW() {
		return mScreenW;
	}

	public int getScreenH() {
		return mScreenH;
	}

	public int getAccentColor() {
		return mThemeColor;
	}

	public int getStatusBarHeight() {
		return mStatusbarH;
	}

	public void setStatusBarHeight(int h) {
		mStatusbarH = h;
	}

	public void setThemeColorIndex(int index) {
		if (mThemeColorIndex != index) {
			mThemeColorIndex = index;
			mThemeChanged = true;
		}
		mThemeColor = mru.getColor(AccentsManager.COLORS[index]);
		
		SharedPreferences.Editor e = mPrefs.edit();
		e.putInt(THEME_COLOR, mThemeColor);
		e.commit();
	}

	public int getAccentColorIndex() {
		return mThemeColorIndex;
	}

	public ResourceUtil getRU() {
		return mru;
	}

	public boolean getShowStatusbar() {
		return mShowStatusbar;
	}

	// 设置是否显示statusbar只有在Settings里能改变。
	public void setShowStatusbar(boolean show) {
		mShowStatusbar = show;
		SharedPreferences.Editor e = mPrefs.edit();
		e.putBoolean(SHOW_STATUSBAR, mShowStatusbar);
		e.commit();
	}

	public int getUsableHeight() {
		if (mShowStatusbar) {
			return mScreenH - mStatusbarH;
		} else {
			return mScreenH;
		}
	}

	public int getBackgroundColor() {
		return mBackgroundColor;
	}

	public void setBackgroundColorMode(int mode) {

		if (BACKGROUND_COLOR_BLACK == mode) {
			mBackgroundColor = mru.getColor(R.color.black_background_color);
			mDisabledTextColor = mru
					.getColor(R.color.black_background_disabled_text_color);
			mNormalTextColor = mru
					.getColor(R.color.black_background_text_color);
			mGrayTextColor = mru
					.getColor(R.color.black_background_gray_text_color);
			mMenuBackGroundColor = mru
					.getColor(R.color.black_background_menu_background_color);
			mMenuTextColor = mru.getColor(R.color.black_background_text_color);
			
			mBackground = mru.getDrawable(R.drawable.wp7_edittext_background_black);
			mBorder = mru.getDrawable(R.drawable.wp7_button_background_black);
		} else if (BACKGROUND_COLOR_WHITE == mode) {
			mBackgroundColor = mru.getColor(R.color.white_background_color);
			mDisabledTextColor = mru
					.getColor(R.color.white_background_disabled_text_color);
			mNormalTextColor = mru
					.getColor(R.color.white_background_text_color);
			mGrayTextColor = mru
					.getColor(R.color.white_background_gray_text_color);
			mMenuBackGroundColor = mru
					.getColor(R.color.white_background_menu_background_color);
			mMenuTextColor = mru.getColor(R.color.white_background_text_color);
			mBackground = mru.getDrawable(R.drawable.wp7_edittext_background_white);
			mBorder = mru.getDrawable(R.drawable.wp7_button_background_white);
		} else {
			throw new IllegalArgumentException("setBackgroundColorMode mode = "
					+ mode);
		}
		mBackGroundColorMode = mode;
		mThemeChanged = true;
		SharedPreferences.Editor e = mPrefs.edit();
		e.putInt(BACKGROUND_COLOR_MODE, mBackGroundColorMode);
		e.commit();
	}

	public boolean isBackgroundColorChanged() {
		return mBackgroundChanged;
	}

	public boolean getAccentChangedAndReset() {
		boolean ret = mThemeChanged;
		if(mThemeChanged) {
			mThemeChanged = false;
		}
		return ret;
	}

	private int mThemeColor = 0;
	private int mScreenW = 0;
	private int mScreenH = 0;
	private int mThemeColorIndex = 0;
	private int mBackgroundColor = 0;
	private int mStatusbarH = 0;
	private ResourceUtil mru = null;
	private boolean mShowStatusbar = false;
	private boolean mThemeChanged = false;
	private boolean mBackgroundChanged = false;
	private SharedPreferences mPrefs = null;
	private int mBackGroundColorMode = BACKGROUND_COLOR_BLACK;
	private int mDisabledTextColor = 0;
	private int mNormalTextColor = 0;
	private int mGrayTextColor = 0;
	private int mMenuBackGroundColor = 0;
	private int mMenuTextColor = 0;
	private Drawable mBackground = null;
	private Drawable mBorder = null;

	private static final String THEME_COLOR = "preference_theme_color";
	private static final String BACKGROUND_COLOR_MODE = "preference_background_color";
	private static final String SHOW_STATUSBAR = "preference_statusbar";

	public int getDisabledTextColor() {
		return mDisabledTextColor;
	}

	public int getTextColor() {
		return mNormalTextColor;
	}

	public int getGrayTextColor() {
		return mGrayTextColor;
	}

	public int getMenuBackGroundColor() {
		return mMenuBackGroundColor;
	}

	public int getMenuTextColor() {
		return mMenuTextColor;
	}
	
	public int getBackgroundColorMode() {
		return mBackGroundColorMode;
	}
	
	public Drawable getBackground() {
		return mBackground;
	}
	
	public Drawable getRectBack() {
		return mBorder;
	}
}
