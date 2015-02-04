package com.axen.launcher.wp7.ui;

import com.axen.launcher.app.AccentsManager;
import com.axen.launcher.wp7.main.R;
import com.axen.launcher.wp7.main.WP7Configuration;
import com.axen.utils.AXLog;
import com.axen.utils.ResourceUtil;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Checkable;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SettingsActivity extends Activity implements OnClickListener {

	private static final String TAG = "SettingsActivity";
	private WP7Configuration mConf = WP7Configuration.getInstance();
	private ResourceUtil mru = mConf.getRU();

	private LinearLayout mTheme = null;
	private TextView mAccentName = null;
	private View mHeader = null;
	private View mShowStatusbar = null;
	private View mBackgroundHolder = null;
	private TextView mAccentTitle = null;
	private TextView mSettingsTitle = null;
	private TextView mStatusbarTitle = null;
	private TextView mStatusbarContent = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_activity);

		mTheme = (LinearLayout) findViewById(R.id.id_settings_theme);

		mAccentName = (TextView) findViewById(R.id.id_settings_theme_accent_name);
		mAccentTitle = (TextView) findViewById(R.id.id_settings_theme_accent_title);
		mSettingsTitle = (TextView) findViewById(R.id.id_settings_title);

		mStatusbarTitle = (TextView) findViewById(R.id.id_settings_show_statusbar_title);
		mStatusbarContent = (TextView) findViewById(R.id.id_settings_show_statusbar_name);

		mHeader = findViewById(R.id.id_settings_header);
		mShowStatusbar = findViewById(R.id.id_settings_show_statusbar);

		mBackgroundHolder = findViewById(R.id.id_settings_background_holder);

		mAccentName
				.setText(AccentsManager.STRINGS[mConf.getAccentColorIndex()]);

		if (mConf.getShowStatusbar()) {
			mStatusbarContent.setText(R.string.string_on);
		} else {
			mStatusbarContent.setText(R.string.string_off);
		}

		mTheme.setOnClickListener(this);
		mShowStatusbar.setOnClickListener(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();

		mBackgroundHolder.setBackgroundColor(mConf.getBackgroundColor());
		setFullScreen(mConf.getShowStatusbar());
		mAccentName
				.setText(AccentsManager.STRINGS[mConf.getAccentColorIndex()]);
		mAccentName.setTextColor(mConf.getGrayTextColor());
		mAccentTitle.setTextColor(mConf.getTextColor());
		mSettingsTitle.setTextColor(mConf.getTextColor());
		mStatusbarTitle.setTextColor(mConf.getTextColor());
		mStatusbarContent.setTextColor(mConf.getGrayTextColor());
	}

	@Override
	public void onClick(View v) {
		Intent intentSet = null;
		switch (v.getId()) {
		case R.id.id_settings_theme:
			intentSet = new Intent();
			intentSet.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			intentSet.setClass(getApplicationContext(),
					SettingsThemeActivity.class);
			startActivity(intentSet);
			break;
		case R.id.id_settings_show_statusbar:
			boolean checked = mConf.getShowStatusbar();
			if (checked) {
				mStatusbarContent.setText(R.string.string_off);
			} else {
				mStatusbarContent.setText(R.string.string_on);
			}
			// 如果是true,表示要显示系统statusbar,所以退出全屏模式
			mConf.setShowStatusbar(!checked);
			setFullScreen(mConf.getShowStatusbar());
			break;
		default:
			AXLog.d(TAG, "Unknow View =" + v);
			break;
		}
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

		mHeader.setVisibility(show ? View.VISIBLE : View.GONE);
	}

}
