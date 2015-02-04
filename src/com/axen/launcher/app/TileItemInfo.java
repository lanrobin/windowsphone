package com.axen.launcher.app;

import com.axen.launcher.wp7.ui.widget.Tile;

import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

public class TileItemInfo {
	public String appActivity;
	public String appDefaultActivity;
	public String appDefaultIcon;
	public Drawable appDefaultIconDrawable;
	public String appDefaultPackage;
	public String appIcon;
	public Drawable appIconDrawable;
	public String appName;
	public String appPackage;
	public boolean isDefault;
	public boolean enabled;
	public int flags;
	public int launchTimes;
	public String tileName;
	public int type;
	public boolean useDefault;
	public boolean wideTile;
	public int x;
	public int y;
	public int _id;
	
	public Rect rect = new Rect();

	/**
	 * 这就view就是用这个TileItemInfo创造出来的View, 所以通过 view.getTag()就可以把这个TileItemInfo
	 * 获得。
	 */
	public Tile view;

	/**
	 * 这个Tile不能被删除，一般是广告或是最后一个Tile.
	 */
	public static final int FLAG_UNMOVABLE = 0x01;

	/**
	 * 这个Tile是不能被移动的，广告必须是每个大Tile
	 */
	public static final int FLAG_UNREMOVABLE = 0x02;

	/**
	 * 这是一个宽的Tile,为1x2的。
	 */
	public static final int FLAG_WIDE_TILE = 0x04;

	/**
	 * 这是不是一个固定的Tile,如phone, message这类的Tile 的图标只能用系统给出的，不能换成第三方程序带的。
	 */
	public static final int FLAG_FIX_TILE = 0x08;

	public static final int FLAG_SYSTEM_APP = 0x10;
	
	// 这种Tile是就图片占满整个tile
	public static final int FLAG_FULL_SIZE = 0x20;

	// 这种Tile不显示Tile的名字。
	public static final int FLAG_HIDE_TILE_NAME = 0x40;
	
	// 这种Tile的名称编辑过的，所以只能以这个名称显示
	public static final int FLAG_EDITTED_TILE_NAME = 0x80;
	
	/**
	 * Tile的类型，为APP type或是widget type.
	 */
	public static final int TYPE_INVALID = 0;
	public static final int TYPE_COMMON = 1;
	public static final int TYPE_WIDGET = 2;
	public static final int TYPE_MESSAGE = 3;
	public static final int TYPE_PHONE = 4;
	public static final int TYPE_GALLERY = 5;
	public static final int TYPE_PEOPLE = 6;
	
	

	public boolean isFixTile() {
		return (flags & FLAG_FIX_TILE) != 0;
	}

	public boolean isRepresent(ResolveInfo ri) {
		boolean ret = false;
		if (ri == null || ri.activityInfo == null) {
			return false;
		}
		ActivityInfo ai = ri.activityInfo;
		if (useDefault) {
			ret = ai.packageName.equals(appDefaultPackage)
					&& ai.name.equals(appDefaultActivity);
		} else {
			ret = ai.packageName.equals(appPackage)
					&& ai.name.equals(appActivity);
		}
		return ret;
	}
	
	public String getCurrentActivity() {
		if(useDefault) {
			return appDefaultActivity;
		}else {
			return appActivity;
		}
	}

	public Drawable getDrawable() {
		if(useDefault) {
			return appDefaultIconDrawable;
		}else {
			return appIconDrawable;
		}
	}
	
	public boolean fullSizeIcon() {
		return (flags & TileItemInfo.FLAG_FULL_SIZE) != 0;
	}
	
	public boolean hideTileName() {
		return (flags & TileItemInfo.FLAG_HIDE_TILE_NAME) != 0;
	}
	
	public void addFlag(int bit) {
		flags |= bit;
	}
	
	public void clearFlag(int bit) {
		flags &= (~bit);
	}
}
