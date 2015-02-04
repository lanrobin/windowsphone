package com.axen.launcher.wp7.ui.statusbar;

import com.axen.launcher.wp7.main.R;

public class StatusBarManager {

	private static final int[][] VIBRATE = {
			{ R.drawable.n_vibrate, R.drawable.transparent_24_24 },
			{ R.drawable.n_vibrate_white, R.drawable.transparent_24_24 } };

	private static final int[][] BLUETOOTH = {
			{ R.drawable.n_bluetooth, R.drawable.transparent_24_24 },
			{ R.drawable.n_bluetooth_white, R.drawable.transparent_24_24 } };

	private static final int[][] BATTERY = {
			{ R.drawable.n_battery_0, R.drawable.n_battery_1,
					R.drawable.n_battery_2, R.drawable.n_battery_3,
					R.drawable.n_battery_4 },
			{ R.drawable.n_battery_0_white, R.drawable.n_battery_1_white,
					R.drawable.n_battery_2_white, R.drawable.n_battery_3_white,
					R.drawable.n_battery_4_white } };

	private static final int[][] BATTERY_CHARGING = {
			{ R.drawable.n_battery_charging0, R.drawable.n_battery_charging1,
					R.drawable.n_battery_charging2,
					R.drawable.n_battery_charging3,
					R.drawable.n_battery_charging4 },
			{ R.drawable.n_battery_charging0_white,
					R.drawable.n_battery_charging1_white,
					R.drawable.n_battery_charging2_white,
					R.drawable.n_battery_charging3_white,
					R.drawable.n_battery_charging4_white } };

	private static final int[][] SIGNAL_STRENGTH = {
			{
					R.drawable.transparent_24_24, // 如果是0，则为空
					R.drawable.n_signal_strength_1,
					R.drawable.n_signal_strength_2,
					R.drawable.n_signal_strength_3,
					R.drawable.n_signal_strength_4,
					R.drawable.n_signal_strength_no_sim, R.drawable.n_plane_on },
			{
					R.drawable.transparent_24_24, // 如果是0，则为空
					R.drawable.n_signal_strength_1_white,
					R.drawable.n_signal_strength_2_white,
					R.drawable.n_signal_strength_3_white,
					R.drawable.n_signal_strength_4_white,
					R.drawable.n_signal_strength_no_sim_white,
					R.drawable.n_plane_on_white } };

	private static final int[][] SIGNAL_TYPE = {
			{ R.drawable.n_signal_type_g, R.drawable.n_signal_type_e,
					R.drawable.n_signal_type_3g, R.drawable.n_signal_type_u,
					R.drawable.n_signal_type_h, R.drawable.n_signal_type_4g,
					R.drawable.transparent_24_24 },
			{ R.drawable.n_signal_type_g_white,
					R.drawable.n_signal_type_e_white,
					R.drawable.n_signal_type_3g_white,
					R.drawable.n_signal_type_u_white,
					R.drawable.n_signal_type_h_white,
					R.drawable.n_signal_type_4g_white,
					R.drawable.transparent_24_24 } };

	private static final int[][] WIFI = {
			{ R.drawable.n_wifi_0, R.drawable.n_wifi_1, R.drawable.n_wifi_2,
					R.drawable.n_wifi_3, R.drawable.n_wifi_4 },
			{ R.drawable.n_wifi_0_white, R.drawable.n_wifi_1_white,
					R.drawable.n_wifi_2_white, R.drawable.n_wifi_3_white,
					R.drawable.n_wifi_4_white } };

	private static final int[][] EDIT_TILE = {
			{ R.drawable.edit, R.drawable.unpin },
			{ R.drawable.edit_white, R.drawable.unpin_white } };

	public static int getBatteryIcon(boolean charging, int mode, int cur,
			int max) {
		int[][] icons = BATTERY;
		if (cur > max) {
			cur = max;
		}
		if (charging) {
			icons = BATTERY_CHARGING;
		}
		int scale = max / icons[0].length;

		scale = cur / scale;
		if (scale >= icons[0].length) {
			scale = icons[0].length - 1;
		}

		return icons[mode][scale];
	}

	public static int getWifiLevels() {
		return WIFI[0].length;
	}

	public static int getWifiIcon(int mode, int mWifiLevel) {
		return WIFI[mode][mWifiLevel];
	}

	public static int getSignalStrengthMaxScale() {
		return SIGNAL_STRENGTH[0].length;
	}

	public static int getSignalStrengthIcon(int mode, int level) {
		if (level > SIGNAL_STRENGTH[0].length - 1) {
			level = SIGNAL_STRENGTH[0].length - 1;
		} else if (level < 0) {
			level = 0;
		}
		return SIGNAL_STRENGTH[mode][level];
	}

	public static int getSinalType(int mode, int type) {
		return SIGNAL_TYPE[mode][type];
	}

	public static int getVibrate(int mode, int vir) {
		return VIBRATE[mode][vir];
	}

	public static int getBluetooth(int mode, int bt) {
		return BLUETOOTH[mode][bt];
	}

	public static int getEditTileIcon(int mode, int index) {
		return EDIT_TILE[mode][index];
	}
}
