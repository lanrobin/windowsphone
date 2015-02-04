package com.axen.launcher.app;

import android.content.Context;
import android.content.res.Resources;

import com.axen.launcher.wp7.main.R;

public class AccentsManager {
	public static int [] COLORS = {
			R.color.accent_color_nokia_blue,
			R.color.accent_color_magenta,
			R.color.accent_color_purple,
			R.color.accent_color_teal,
			R.color.accent_color_lime,
			R.color.accent_color_brown,
			R.color.accent_color_pink,
			R.color.accent_color_mango,
			R.color.accent_color_blue,
			R.color.accent_color_red,
			R.color.accent_color_green
		};
	public static int [] DRAWABLES ={
			R.drawable.accenter_color_nokia_blue,
			R.drawable.accenter_color_magenta,
			R.drawable.accenter_color_purple,
			R.drawable.accenter_color_teal,
			R.drawable.accenter_color_lime,
			R.drawable.accenter_color_brown,
			R.drawable.accenter_color_pink,
			R.drawable.accenter_color_mango,
			R.drawable.accenter_color_blue,
			R.drawable.accenter_color_red,
			R.drawable.accenter_color_green
	};
	
	public static int [] STRINGS = {
		R.string.string_accent_color_nokia_blue,
		R.string.string_accent_color_magenta,
		R.string.string_accent_color_purple,
		R.string.string_accent_color_teal,
		R.string.string_accent_color_lime,
		R.string.string_accent_color_brown,
		R.string.string_accent_color_pink,
		R.string.string_accent_color_mango,
		R.string.string_accent_color_blue,
		R.string.string_accent_color_red,
		R.string.string_accent_color_green
	};
	
	public static int getIdFromColor(Context c, int color) {
		int id = -1;
		Resources res = c.getResources();
		for(int i = 0; i < COLORS.length; i ++) {
			if(res.getColor(COLORS[i]) == color) {
				id = COLORS[i];
				break;
			}
		}
		return id;
	}
	
	public static int getIndexFromColor(Context c, int color) {
		int id = -1;
		Resources res = c.getResources();
		for(int i = 0; i < COLORS.length; i ++) {
			if(res.getColor(COLORS[i]) == color) {
				id = i;
				break;
			}
		}
		return id;
	}
}
