package com.axen.launcher.wp7.ui.statusbar;

import java.util.List;

import com.axen.launcher.wp7.main.R;
import com.axen.launcher.wp7.main.WP7Configuration;
import com.axen.launcher.wp7.ui.DragLayer;
import com.axen.utils.AXLog;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.GpsStatus;
import android.location.LocationManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

public class StatusbarController extends BroadcastReceiver {

	public static final String ACTION_UPDATE_TIMER = "com.axen.launcher.wp7.ui.statusbar.ACTION_UPDATE_TIMER";

	public static final String ACTION_UPDATE_ALL = "com.axen.launcher.wp7.ui.statusbar.ACTION_UPDATE";
	private static final String TAG = "StatusbarController";
	
	private static final int PSL_STATUS = 
			PhoneStateListener.LISTEN_DATA_ACTIVITY
			| PhoneStateListener.LISTEN_DATA_CONNECTION_STATE
			| PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
			| PhoneStateListener.LISTEN_SERVICE_STATE;

	private DragLayer mHost = null;
	private Context mContext = null;

	private boolean mWifiEnabled;

	private boolean mWifiConnected;

	private WifiManager mWifiManager = null;
	private LocationManager mGpsManager = null;
	private TelephonyManager mTelManager = null;
	private WP7Configuration mConf = WP7Configuration.getInstance();

	private GpsStatus.Listener mGpsListener = new GpsStatus.Listener() {

		private int[] icons = {R.drawable.n_gps, R.drawable.n_gps_white};
		
		@Override
		public void onGpsStatusChanged(int event) {
			if (GpsStatus.GPS_EVENT_STARTED == event) {
				mHost.updateGpsIcon(icons[mConf.getBackgroundColorMode()]);
			} else if (GpsStatus.GPS_EVENT_STOPPED == event) {
				mHost.updateGpsIcon(R.drawable.transparent_24_24);
			}
		}

	};

	private PhoneStateListener mPhoneListener = new PhoneStateListener() {

		@Override
		public void onDataActivity(int direction) {
			super.onDataActivity(direction);
			int icon = R.drawable.transparent_24_24;
			switch (direction) {
			case TelephonyManager.DATA_ACTIVITY_IN:
				icon = R.drawable.n_data_direct_down;
				break;
			case TelephonyManager.DATA_ACTIVITY_OUT:
				icon = R.drawable.n_data_direct_out;
				break;
			case TelephonyManager.DATA_ACTIVITY_INOUT:
				icon = R.drawable.n_data_direct_both;
				break;
			}
			mHost.updateDataDirection(icon);
		}

		@Override
		public void onDataConnectionStateChanged(int state, int networkType) {
			super.onDataConnectionStateChanged(state, networkType);
			int icon = getNetworkTypeIcon(networkType);
			mHost.updateNetworkType(icon);
		}

		@Override
		public void onSignalStrengthsChanged(SignalStrength signalStrength) {
			super.onSignalStrengthsChanged(signalStrength);
			int level = getSignalLevel(signalStrength);
			int icon = StatusBarManager.getSignalStrengthIcon(mConf.getBackgroundColorMode(), level);
			mHost.updateSignalStrength(icon);
		}

	};

	private int mWifiLevel = 0;

	private int mWifiRssi = -200;

	private String mWifiSsid;

	private boolean mStopUpdate = false;;

	@Override
	public void onReceive(Context context, Intent intent) {
		if (context == null || intent == null || mHost == null) {
			AXLog.d(TAG, "context = " + context + ", intent = " + intent
					+ ", mHost = " + mHost);
			return;
		} else {
			AXLog.d(TAG, "action = " + intent.getAction());
		}
		
		if(!mConf.getShowStatusbar()) {
			AXLog.d(TAG, "Statusbar don't show, so return.");
			return;
		}
		
		if(mStopUpdate) {
			AXLog.d(TAG, "statusbar updating has been stopped.");
			return ;
		}

		String action = intent.getAction();

		/**
		 * 因为系统变为震动没有通知，所以只有自己去查。
		 */
		mHost.updateVibrate();

		if (ACTION_UPDATE_TIMER.endsWith(action)) {
			mHost.updateTimer();
		} else if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
			mHost.updateBattery(intent);
		} else if (action.equals(WifiManager.RSSI_CHANGED_ACTION)
				|| action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)
				|| action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
			updateWifiIcon(intent);
		} else if (ACTION_UPDATE_ALL.equals(action)) {
			updateAllIcon();
		} else if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(action)) {
			int mode = Settings.System.getInt(mContext.getContentResolver(),
					Settings.System.AIRPLANE_MODE_ON, 0);
			if (mode == 1) {
				mHost.updateSignalStrength(R.drawable.n_plane_on);
			}
		} else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
			int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
					BluetoothAdapter.STATE_OFF);
			if (state == BluetoothAdapter.STATE_ON) {
				mHost.updateBluetooth(R.drawable.n_bluetooth);
			} else {
				mHost.updateBluetooth(R.drawable.transparent_24_24);
			}
		}
	}

	private void updateWifiIcon(Intent intent) {
		final String action = intent.getAction();
		if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
			mWifiEnabled = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
					WifiManager.WIFI_STATE_UNKNOWN) == WifiManager.WIFI_STATE_ENABLED;

		} else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
			final NetworkInfo networkInfo = (NetworkInfo) intent
					.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
			boolean wasConnected = mWifiConnected;
			mWifiConnected = networkInfo != null && networkInfo.isConnected();
			// If we just connected, grab the inintial signal strength and ssid
			if (mWifiConnected && !wasConnected) {
				// try getting it out of the intent first
				// "wifiInfo" is avalaible in api level 14, but here we don't
				// care.
				WifiInfo info = (WifiInfo) intent
						.getParcelableExtra("wifiInfo");
				if (info == null) {
					info = mWifiManager.getConnectionInfo();
				}
				if (info != null) {
					mWifiSsid = huntForSsid(info);
				} else {
					mWifiSsid = null;
				}
			} else if (!mWifiConnected) {
				mWifiSsid = null;
			}
			// Apparently the wifi level is not stable at this point even if
			// we've just connected to
			// the network; we need to wait for an RSSI_CHANGED_ACTION for that.
			// So let's just set
			// it to 0 for now
			mWifiLevel = 0;
			mWifiRssi = -200;
		} else if (action.equals(WifiManager.RSSI_CHANGED_ACTION)) {
			if (mWifiConnected) {
				mWifiRssi = intent
						.getIntExtra(WifiManager.EXTRA_NEW_RSSI, -200);
				mWifiLevel = WifiManager.calculateSignalLevel(mWifiRssi,
						StatusBarManager.getWifiLevels());
			}
		}

		// 更新图标
		int icon = R.drawable.transparent_24_24;
		if (mWifiEnabled) {
			icon = StatusBarManager.getWifiIcon(mConf.getBackgroundColorMode(), mWifiLevel);
		}

		mHost.updateWifiIcon(icon);
	}

	private void updateAllIcon() {
		// sim 卡不在
		if (mTelManager.getSimState() == TelephonyManager.SIM_STATE_ABSENT) {
			mHost.updateSignalStrength(StatusBarManager.getSignalStrengthIcon(mConf.getBackgroundColorMode(), 5));
		}

		int mode = Settings.System.getInt(mContext.getContentResolver(),
				Settings.System.AIRPLANE_MODE_ON, 0);
		if (mode == 1) {
			mHost.updateSignalStrength(StatusBarManager.getSignalStrengthIcon(mConf.getBackgroundColorMode(), 6));
		}

		BluetoothAdapter dapter = BluetoothAdapter.getDefaultAdapter();
		if (dapter != null && dapter.getState() == BluetoothAdapter.STATE_ON) {
			mHost.updateBluetooth(StatusBarManager.getBluetooth(mConf.getBackgroundColorMode(), 0));
		} else {
			mHost.updateBluetooth(StatusBarManager.getBluetooth(mConf.getBackgroundColorMode(), 1));
		}

		int icon = getNetworkTypeIcon(mTelManager.getNetworkType());

		mHost.updateNetworkType(icon);
		
		// wifi
		WifiInfo info = mWifiManager.getConnectionInfo();
		if(info != null && mWifiManager.isWifiEnabled()) {
			mWifiSsid = huntForSsid(info);
			mWifiRssi = info.getRssi();
			mWifiLevel = WifiManager.calculateSignalLevel(mWifiRssi,
					StatusBarManager.getWifiLevels());
			icon = StatusBarManager.getWifiIcon(mConf.getBackgroundColorMode(), mWifiLevel);
			mHost.updateWifiIcon(icon);
		}else {
			mHost.updateWifiIcon(R.drawable.transparent_24_24);
		}
		
		mHost.updateTimer();
	}

	private int getNetworkTypeIcon(int type) {
		/* g e 3g u h 4g no*/
		int iconIndex = 0;
		switch (type) {
		case TelephonyManager.NETWORK_TYPE_CDMA:
		case TelephonyManager.NETWORK_TYPE_GPRS:
		case TelephonyManager.NETWORK_TYPE_IDEN:
			// icon = R.drawable.n_signal_type_g;
			iconIndex = 0;
			break;
		case TelephonyManager.NETWORK_TYPE_EDGE:
		case TelephonyManager.NETWORK_TYPE_1xRTT:
			// icon = R.drawable.n_signal_type_e;
			iconIndex = 1;
			break;
		case TelephonyManager.NETWORK_TYPE_HSPA:
		case TelephonyManager.NETWORK_TYPE_UMTS:
		case TelephonyManager.NETWORK_TYPE_EVDO_0:
		case TelephonyManager.NETWORK_TYPE_EVDO_A:
		case 0x0C:   // 2.2 don't support TelephonyManager.NETWORK_TYPE_EVDO_B:
			//icon = R.drawable.n_signal_type_3g;
			iconIndex = 2;
			break;
		case 15: /* NETWORK_TYPE_HSPAP */
		case TelephonyManager.NETWORK_TYPE_HSDPA:
			//icon = R.drawable.n_signal_type_h;
			iconIndex = 4;
			break;
		case TelephonyManager.NETWORK_TYPE_HSUPA:
			//icon = R.drawable.n_signal_type_u;
			iconIndex = 1;
			break;
		case 13: /* NETWORK_TYPE_LTE */
			// icon = R.drawable.n_signal_type_4g;
			iconIndex = 5;
			break;
		case TelephonyManager.NETWORK_TYPE_UNKNOWN:
		default:
			// icon = R.drawable.transparent_24_24;
			iconIndex = 6;
			break;
		}
		return StatusBarManager.getSinalType(mConf.getBackgroundColorMode(), iconIndex);
	}

	/**
	 * 返回 从 0...4
	 * 
	 * @param ss
	 * @return
	 */
	private int getSignalLevel(SignalStrength ss) {
		int level = 0;
		if (ss.isGsm()) {
			int asu = ss.getGsmSignalStrength();
			if (asu <= 2 || asu == 99)
				level = 0;
			else if (asu >= 12)
				level = 4;
			else if (asu >= 8)
				level = 3;
			else if (asu >= 5)
				level = 2;
			else
				level = 1;
		} else {
			final int cdmaDbm = ss.getCdmaDbm();
			final int cdmaEcio = ss.getCdmaEcio();
			int levelDbm;
			int levelEcio;

			if (cdmaDbm >= -75)
				levelDbm = 1;
			else if (cdmaDbm >= -85)
				levelDbm = 2;
			else if (cdmaDbm >= -95)
				levelDbm = 3;
			else if (cdmaDbm >= -100)
				levelDbm = 4;
			else
				levelDbm = 0;

			// Ec/Io are in dB*10
			if (cdmaEcio >= -90)
				levelEcio = 4;
			else if (cdmaEcio >= -110)
				levelEcio = 3;
			else if (cdmaEcio >= -130)
				levelEcio = 2;
			else if (cdmaEcio >= -150)
				levelEcio = 1;
			else
				levelEcio = 0;

			level = (levelDbm < levelEcio) ? levelDbm : levelEcio;
		}
		return level;
	}

	private String huntForSsid(WifiInfo info) {
		String ssid = info.getSSID();
		if (ssid != null) {
			return ssid;
		}
		// OK, it's not in the connectionInfo; we have to go hunting for it
		List<WifiConfiguration> networks = mWifiManager.getConfiguredNetworks();
		for (WifiConfiguration net : networks) {
			if (net.networkId == info.getNetworkId()) {
				return net.SSID;
			}
		}
		return null;
	}
	
	/**
	 * 为了节约电，当LauncherAcitivty在屏幕关闭的时候不需要更新。
	 * 只有当屏幕开的时候才需要。
	 */
	public void stopUpdate() {
		mStopUpdate  = true;
		mTelManager.listen(mPhoneListener, PhoneStateListener.LISTEN_NONE);
	}

	
	public void startUpdate() {
		mStopUpdate = false;
		mTelManager.listen(mPhoneListener, PSL_STATUS);
	}
	
	public StatusbarController(Context context, DragLayer host) {
		if (host == null || context == null) {
			throw new NullPointerException(
					" new StatusbarController with null.");
		}
		mHost = host;
		mContext = context;

		mWifiManager = (WifiManager) mContext
				.getSystemService(Context.WIFI_SERVICE);
		mGpsManager = (LocationManager) mContext
				.getSystemService(Context.LOCATION_SERVICE);
		// mGpsManager.getGpsStatus(null);
		mGpsManager.addGpsStatusListener(mGpsListener);

		mTelManager = (TelephonyManager) mContext
				.getSystemService(Context.TELEPHONY_SERVICE);
		mTelManager.listen(mPhoneListener,PSL_STATUS);
	}

	/**
	 * 获得自己想要支持的Action filter.
	 * 
	 * @return
	 */
	public static IntentFilter getIntentFilter() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_UPDATE_TIMER);
		filter.addAction(ACTION_UPDATE_ALL);
		filter.addAction(Intent.ACTION_BATTERY_CHANGED);
		filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
		filter.addAction(WifiManager.RSSI_CHANGED_ACTION);
		filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		return filter;
	}
}
