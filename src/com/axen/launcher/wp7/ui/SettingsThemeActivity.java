package com.axen.launcher.wp7.ui;

import com.axen.launcher.app.AccentsManager;
import com.axen.launcher.wp7.main.R;
import com.axen.launcher.wp7.main.WP7App;
import com.axen.launcher.wp7.main.WP7Configuration;
import com.axen.launcher.wp7.ui.size.Size;
import com.axen.launcher.wp7.ui.widget.AccentAdapter.AccentData;
import com.axen.utils.AXLog;
import com.axen.utils.ResourceUtil;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.TextAppearanceSpan;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class SettingsThemeActivity extends Activity implements OnClickListener {

	private TextView mThemeSummary = null;

	private TextView mAccentColor = null;
	private TextView mTitle = null;
	private TextView mThemeTitle = null;
	private TextView mBGTitle = null;
	private TextView mBGContent = null;
	private TextView mAccentsTitle = null;
	
	private View mBackground = null;

	private String mSummaryPrefix = null;
	private String mSummaryMiddle = null;
	private String mSummarySuffix = null;

	private static final String ACCENT_PANEL = "[panel]";

	private WP7Configuration mConf = WP7Configuration.getInstance();
	private ResourceUtil mru = mConf.getRU();
	private Size size = Size.get();

	private int mAccentPanelSize = 0;
	
	private static final int REQUEST_CODE_ACCENT_COLOR = 0x1000;

	private static final String TAG = "SettingsThemeActivity";
	
	private int mDrawableId = 0;
	private int mColorStringIndex = 0;
	
	private View mHeader = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting_theme_activity);

		mThemeSummary = (TextView) findViewById(R.id.id_settings_theme_summary);
		mBackground = findViewById(R.id.id_settings_theme_background_holder);
		mAccentColor = (TextView) findViewById(R.id.id_settings_theme_accent_color);
		mHeader = findViewById(R.id.id_settings_theme_header);
		mTitle = (TextView)findViewById(R.id.id_settings_theme_title);
		mThemeTitle = (TextView)findViewById(R.id.id_settings_theme_theme);
		mBGTitle = (TextView)findViewById(R.id.id_settings_theme_bg_title);
		mBGContent = (TextView)findViewById(R.id.id_settings_theme_background);
		mAccentsTitle = (TextView)findViewById(R.id.id_settings_theme_accents_title);

		mSummaryPrefix = getString(R.string.settings_theme_summary_prefix);
		mSummaryMiddle = getString(R.string.settings_theme_summary_middle);
		mSummarySuffix = getString(R.string.settings_theme_summary_suffix);

		mAccentPanelSize = size.getSettings_accent_color_panel_size(); //mru.getPexil(R.dimen.settings_accent_color_panel_size);
		
		
		mColorStringIndex = mConf.getAccentColorIndex();
		mDrawableId = AccentsManager.DRAWABLES[mColorStringIndex];
		mAccentColor.setOnClickListener(this);
		mBGContent.setOnClickListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		setFullScreen(mConf.getShowStatusbar());
		reflash();
	}
	
	private void reflash() {
		
		
		String dark = getString(R.string.string_settings_theme_background_dark);
		String light = getString(R.string.string_settings_theme_background_light);
		
		if(mConf.getBackgroundColorMode() == WP7Configuration.BACKGROUND_COLOR_BLACK) {
			mBGContent.setText(dark);
		}else if(mConf.getBackgroundColorMode() == WP7Configuration.BACKGROUND_COLOR_WHITE) {
			mBGContent.setText(light);
		}
		
		mBackground.setBackgroundColor(mConf.getBackgroundColor());
		mTitle.setTextColor(mConf.getTextColor());
		mThemeTitle.setTextColor(mConf.getTextColor());
		mBGTitle.setTextColor(mConf.getTextColor());
		mAccentsTitle.setTextColor(mConf.getTextColor());
		
		int normalColor = mConf.getTextColor();
		int accentColor = mConf.getAccentColor();
		int blackColor = mConf.getGrayTextColor();
		int pLen = mSummaryPrefix.length();
		int mLen = mSummaryMiddle.length();
		int sLen = mSummarySuffix.length();
		SpannableStringBuilder ssb = new SpannableStringBuilder(mSummaryPrefix
				+ mSummaryMiddle + mSummarySuffix);
		// 先设置前面没有匹配的
		ssb.setSpan(new ForegroundColorSpan(normalColor), 0, pLen,
				Spannable.SPAN_INCLUSIVE_INCLUSIVE);

		// 再设置匹配了的
		ssb.setSpan(new ForegroundColorSpan(accentColor), pLen, pLen + mLen,
				Spannable.SPAN_EXCLUSIVE_INCLUSIVE);

		// 再设置最后没有匹配的
		ssb.setSpan(new ForegroundColorSpan(normalColor), pLen + mLen, pLen
				+ mLen + sLen, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
		mThemeSummary.setText(ssb);
		String colorName = getString(AccentsManager.STRINGS[mColorStringIndex]);
		// int color = 0xFF1081DD;
		Drawable drawable = getResources().getDrawable(mDrawableId);
		drawable.setBounds(0, 0, mAccentPanelSize, mAccentPanelSize);
		SpannableString spannable = new SpannableString(ACCENT_PANEL
				+ colorName);
		// 要让图片替代指定的文字就要用ImageSpan
		ImageSpan span = new ImageSpan(drawable, ImageSpan.ALIGN_BASELINE);
		// 开始替换，注意第2和第3个参数表示从哪里开始替换到哪里替换结束（start和end）
		// 最后一个参数类似数学中的集合,[5,12)表示从5到12，包括5但不包括12
		spannable.setSpan(span, 0, ACCENT_PANEL.length(),
				Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
		spannable.setSpan( new TextAppearanceSpan(this, android.R.style.TextAppearance_Medium),
				ACCENT_PANEL.length(),
				colorName.length() + ACCENT_PANEL.length(),
				Spannable.SPAN_INCLUSIVE_INCLUSIVE);
		spannable.setSpan(new ForegroundColorSpan(blackColor), ACCENT_PANEL.length(), colorName.length() + ACCENT_PANEL.length(),
				Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
		mAccentColor.setText(spannable);
	}

	@Override
	public void onClick(View view) {
		switch(view.getId()) {
		case R.id.id_settings_theme_accent_color:
			Intent intent = new Intent();
			intent.putExtra(AccentData.ACCENT_DATA_COLOR_INDEX, mConf.getAccentColorIndex());
			intent.setClass(this, SettingsAccentsActivity.class);
			// startActivity(intent);
			this.startActivityForResult(intent, REQUEST_CODE_ACCENT_COLOR);
			break;
		case R.id.id_settings_theme_background:
			String bg = mBGContent.getText().toString();
			String dark = getString(R.string.string_settings_theme_background_dark);
			String light = getString(R.string.string_settings_theme_background_light);
			if(dark.equals(bg)) {
				// 当前是dark
				mBGContent.setText(light);
				mConf.setBackgroundColorMode(WP7Configuration.BACKGROUND_COLOR_WHITE);
			}else if(light.equals(bg)) {
				mBGContent.setText(dark);
				mConf.setBackgroundColorMode(WP7Configuration.BACKGROUND_COLOR_BLACK);
			}else {
				AXLog.d(TAG, "Unkown string = " + bg);
			}
			
			reflash();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(REQUEST_CODE_ACCENT_COLOR == requestCode && resultCode == RESULT_OK) {
			int index = data.getIntExtra(AccentData.ACCENT_DATA_COLOR_INDEX, mConf.getAccentColorIndex());
			mDrawableId = AccentsManager.DRAWABLES[index];
			mColorStringIndex = index;
			// mColorStringId = data.getIntExtra(AccentData.ACCENT_DATA_STRING_ID, mColorStringId);
			mConf.setThemeColorIndex(index);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	private void setFullScreen(boolean show) {
		if (show) {
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
		} else {
			final WindowManager.LayoutParams attrs = getWindow()
					.getAttributes();
			attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
			getWindow().setAttributes(attrs);
			getWindow().clearFlags(
					WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

		}
		mHeader.setVisibility(show?View.VISIBLE:View.GONE);
	}

}
