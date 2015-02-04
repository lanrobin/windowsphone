package com.axen.launcher.wp7.ui;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.axen.launcher.wp7.main.R;
import com.axen.launcher.wp7.main.WP7App;
import com.axen.launcher.wp7.main.WP7Configuration;
import com.axen.launcher.wp7.ui.anim.AnimUtil;
import com.axen.launcher.wp7.ui.size.Size;
import com.axen.launcher.wp7.ui.statusbar.StatusBarManager;
import com.axen.launcher.wp7.ui.statusbar.StatusbarController;
import com.axen.utils.ResourceUtil;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.media.AudioManager;
import android.os.BatteryManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * A ViewGroup that coordinated dragging across its dscendants
 */
public class DragLayer extends LinearLayout {

	private static final String TAG = "DragLayer";

	private WP7Configuration mConf = WP7Configuration.getInstance();

	DragController mDragController;

	private RelativeLayout mStatusBar = null;
	private WorkSpace mWorkSpace = null;
	private ResourceUtil mru = mConf.getRU();
	private Size size = Size.get();
	private Context mContext = null;

	private ImageView mSignalStrength = null;
	private ImageView mSingalType = null;
	private ImageView mDataDirection = null;
	private ImageView mBluetooth = null;
	private ImageView mGps = null;
	private ImageView mWifi = null;
	private ImageView mVibrate = null;
	private ImageView mBattery = null;
	private TextView mTimer = null;

	private AudioManager mAudioManager = null;

	private SimpleDateFormat mTimeFormat = new SimpleDateFormat("HH:mm");

	private int mStatusBarH = 0;

	private boolean mStatusBarShown = true;

	private OnClickListener mOnClickListener;

	/**
	 * Used to create a new DragLayer from XML.
	 * 
	 * @param context
	 *            The application's context.
	 * @param attrs
	 *            The attribtues set containing the Workspace's customization
	 *            values.
	 */
	public DragLayer(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		mStatusBarH = size.getStatus_bar_h(); //mru.getPexil(R.dimen.status_bar_h);
		init();
	}

	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
		setBackgroundColor(mConf.getBackgroundColor());
	}

	private void init() {
		Intent timerIntent = new Intent(StatusbarController.ACTION_UPDATE_TIMER);
		PendingIntent timerPending = PendingIntent.getBroadcast(mContext, 0,
				timerIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager alarm = (AlarmManager) mContext
				.getSystemService(Context.ALARM_SERVICE);
		alarm.setRepeating(AlarmManager.RTC, System.currentTimeMillis(),
				30 * 1000, timerPending);

		mAudioManager = (AudioManager) mContext
				.getSystemService(Context.AUDIO_SERVICE);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mStatusBar = (RelativeLayout) findViewById(R.id.id_status_bar);
		mWorkSpace = (WorkSpace) findViewById(R.id.id_workspace);

		mSignalStrength = (ImageView) findViewById(R.id.id_status_bar_signal_strength);
		mSingalType = (ImageView) findViewById(R.id.id_status_bar_signal_type);
		mDataDirection = (ImageView) findViewById(R.id.id_status_bar_data_direction);
		mBluetooth = (ImageView) findViewById(R.id.id_status_bar_bluetooth);
		mGps = (ImageView) findViewById(R.id.id_status_bar_gps);
		mWifi = (ImageView) findViewById(R.id.id_status_bar_wifi);
		mVibrate = (ImageView) findViewById(R.id.id_status_bar_vibrate);
		mBattery = (ImageView) findViewById(R.id.id_status_bar_battery);
		mTimer = (TextView) findViewById(R.id.id_status_bar_time);
		
		ViewGroup.LayoutParams lp = mStatusBar.getLayoutParams();
		lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
		lp.height = size.getStatus_bar_h();
		mStatusBar.setLayoutParams(lp);
		
		mStatusBar.bringToFront();
		// mTimer.setText(text)

		clearItemsAnim();
		// 初始完成后，要刷新一下status bar.
		mContext.sendBroadcast(new Intent(StatusbarController.ACTION_UPDATE_ALL));
		setBackgroundColor(mConf.getBackgroundColor());
	}

	public void setDragController(DragController controller) {
		mDragController = controller;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return mDragController.onInterceptTouchEvent(ev);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		return mDragController.onTouchEvent(ev);
	}

	@Override
	public boolean dispatchUnhandledMove(View focused, int direction) {
		return mDragController.dispatchUnhandledMove(focused, direction);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		mWorkSpace.layout(l, 0, r, b);
		mStatusBar.layout(l, 0, r, mConf.getStatusBarHeight());
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		for (int i = 0; i < getChildCount(); i++) {
			getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
		}
	}

	public void updateTimer() {
		mTimer.setTextColor(mConf.getTextColor());
		mTimer.setTextSize(size.getStatus_bar_font_h());
		mTimer.setHeight(size.getStatus_bar_h());
		mTimer.setText(mTimeFormat.format(new Date()));
	}

	public void updateBattery(Intent intent) {
		int level = intent.getIntExtra("level", 0);
		int scale = intent.getIntExtra("scale", 100);
		int status = intent.getIntExtra("status", 0);
		boolean charging = (status == BatteryManager.BATTERY_STATUS_CHARGING); // 看是不是在充电
		int icon = StatusBarManager.getBatteryIcon(charging,
				mConf.getBackgroundColorMode(), level, scale);

		if (mBattery != null) {
			mBattery.setImageResource(icon);
			mBattery.invalidate();
		}
	}

	public void updateVibrate() {
		if (mVibrate != null) {
			boolean vibrate = mAudioManager
					.shouldVibrate(AudioManager.VIBRATE_TYPE_NOTIFICATION);
			if (!vibrate) {
				vibrate = mAudioManager
						.shouldVibrate(AudioManager.VIBRATE_TYPE_RINGER);
			}

			if (vibrate) {
				mVibrate.setImageResource(StatusBarManager.getVibrate(
						mConf.getBackgroundColorMode(), 0));
			} else {
				mVibrate.setImageResource(R.drawable.transparent_24_24);
			}
			mVibrate.invalidate();
		}
	}

	public void updateWifiIcon(int icon) {
		if (mWifi != null) {
			mWifi.setImageResource(icon);
			mWifi.invalidate();
		}
	}

	public void updateGpsIcon(int nGps) {
		if (mGps != null) {
			mGps.setImageResource(nGps);
			mGps.invalidate();
		}
	}

	public void updateSignalStrength(int icon) {
		if (mSignalStrength != null) {
			mSignalStrength.setImageResource(icon);
			mSignalStrength.invalidate();
		}
	}

	public void updateBluetooth(int icon) {
		if (mBluetooth != null) {
			mBluetooth.setImageResource(icon);
			mBluetooth.invalidate();
		}
	}

	public void updateNetworkType(int icon) {
		if (mSingalType != null) {
			mSingalType.setImageResource(icon);
			mSingalType.invalidate();
		}
	}

	public void updateDataDirection(int icon) {
		if (mDataDirection != null) {
			mDataDirection.setImageResource(icon);
			mDataDirection.invalidate();
		}
	}

	public void clearItemsAnim() {
		mSignalStrength.clearAnimation();
		mSingalType.clearAnimation();
		mDataDirection.clearAnimation();
		mBluetooth.clearAnimation();
		mGps.clearAnimation();
		mWifi.clearAnimation();
		mVibrate.clearAnimation();
		// mBattery.clearAnimation();
		mTimer.clearAnimation();
	}

	public void showItems() {
		int interval = 80;
		long startOffset = 0;
		mSignalStrength.startAnimation(AnimUtil.getStatusBarShowAnim(
				mSignalStrength, startOffset));
		startOffset += interval;
		mSingalType.startAnimation(AnimUtil.getStatusBarShowAnim(mSingalType,
				startOffset));
		startOffset += interval;
		mDataDirection.startAnimation(AnimUtil.getStatusBarShowAnim(
				mDataDirection, startOffset));
		startOffset += interval;
		mBluetooth.startAnimation(AnimUtil.getStatusBarShowAnim(mBluetooth,
				startOffset));
		startOffset += interval;
		mGps.startAnimation(AnimUtil.getStatusBarShowAnim(mGps, startOffset));
		startOffset += interval;
		mWifi.startAnimation(AnimUtil.getStatusBarShowAnim(mWifi, startOffset));
		startOffset += interval;
		// mBattery.startAnimation(AnimUtil.getStatusBarShowAnim(mBattery,
		// startOffset));
		// startOffset += interval;
		mVibrate.startAnimation(AnimUtil.getStatusBarShowAnim(mVibrate,
				startOffset));

		mStatusBarShown = true;
	}

	public void hideItems() {
		int interval = -80;
		long startOffset = 700;
		mSignalStrength.startAnimation(AnimUtil.getStatusBarHideAnim(
				mSignalStrength, startOffset));
		startOffset += interval;
		mSingalType.startAnimation(AnimUtil.getStatusBarHideAnim(mSingalType,
				startOffset));
		startOffset += interval;
		mDataDirection.startAnimation(AnimUtil.getStatusBarHideAnim(
				mDataDirection, startOffset));
		startOffset += interval;
		mBluetooth.startAnimation(AnimUtil.getStatusBarHideAnim(mBluetooth,
				startOffset));
		startOffset += interval;
		mGps.startAnimation(AnimUtil.getStatusBarHideAnim(mGps, startOffset));
		startOffset += interval;
		mWifi.startAnimation(AnimUtil.getStatusBarHideAnim(mWifi, startOffset));
		startOffset += interval;
		// mBattery.startAnimation(AnimUtil.getStatusBarHideAnim(mBattery,
		// startOffset));
		// startOffset += interval;
		mVibrate.startAnimation(AnimUtil.getStatusBarHideAnim(mVibrate,
				startOffset));

		mStatusBarShown = false;
	}

	public boolean itemsShown() {
		return mStatusBarShown;
	}

	public void setStatusBarClickListener(OnClickListener l) {
		mOnClickListener = l;
		if (mStatusBar != null) {
			mStatusBar.setOnClickListener(l);
		}
	}

	public void setStatusbar(boolean show) {
		if (show) {
			mStatusBar.setVisibility(VISIBLE);
		} else {
			mStatusBar.setVisibility(GONE);
		}
	}
}
