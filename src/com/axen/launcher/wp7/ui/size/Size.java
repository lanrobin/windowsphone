package com.axen.launcher.wp7.ui.size;

import com.axen.utils.AXLog;

import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

public abstract class Size {
	
	private static final int W_240 = 240;
	private static final int W_320 = 320;
	private static final int W_480 = 480;
	private static final int W_540 = 540;
	private static final int W_600 = 600;
	private static final int W_640 = 640;
	private static final int W_720 = 720;
	private static final int W_800 = 800;
	
	public static Size _S;
	private static final String TAG = "Size";
	
	public static final void init(Context c) {
		if(c == null) {
			throw new NullPointerException("Size init context == null");
		}
		
		if(_S != null) {
			AXLog.d(TAG, "All ready initialized.");
			return;
		}
		
		Display display = ((WindowManager) c
				.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

		Point outSize = new Point();
		//display.getSize(outSize);
		outSize.x = display.getWidth();
		outSize.y = display.getHeight();
		switch(outSize.x) {
		case W_240:
			_S = new Size240(outSize);
			break;
		case W_320:
			_S = new Size320(outSize);
			break;
		case W_480:
			_S = new Size480(outSize);
			break;
		case W_540:
			_S = new Size540(outSize);
			break;
		case W_600:
			_S = new Size600(outSize);
			break;
		case W_640:
			_S = new Size640(outSize);
			break;
		case W_720:
			_S = new Size720(outSize);
			break;
		case W_800:
			_S = new Size800(outSize);
			break;
		default:
			_S = new SizeScale(outSize);
			break;
		}
	}
	
	public static final Size get(){
		return _S;
	}

	public abstract int getScreen_w();

	public abstract int getTile_space_w();

	public abstract int getWorkspace_w();

	public abstract int getButton_space_w();

	public abstract int getApp_space_w();

	public abstract int getTop_margin_h();

	public abstract int getTile_left_space();

	public abstract int getTiles_space();

	public abstract int getTile_w();

	public abstract int getTile_h();

	public abstract int getTile_icon_w();

	public abstract int getTile_icon_h();

	public abstract int getTile_edit_w();

	public abstract int getTile_edit_h();

	public abstract int getTile_name_font_h();

	public abstract int getTile_name_h();

	public abstract int getTile_name_left_margin();

	public abstract int getTile_indicator_font_h();

	public abstract int getDrag_view_w();

	public abstract int getDrag_view_h();

	public abstract int getStatus_bar_h();

	public abstract int getStatus_bar_font_h();

	public abstract int getStatus_item_narrow_w();

	public abstract int getStatus_item_wide_w();

	public abstract int getStatus_item_padding();

	public abstract int getButton_w();

	public abstract int getButton_h();

	public abstract int getInactive_tile_w();

	public abstract int getInactive_tile_h();

	public abstract int getWide_tile_w();

	public abstract int getButtons_space();

	public abstract int getApp_icon_w();

	public abstract int getApp_icon_h();

	public abstract int getApp_icon_space();

	public abstract int getApp_name_font_h();

	public abstract int getApp_class_view_w();

	public abstract int getApp_class_view_h();

	public abstract int getApp_class_view_space();

	public abstract int getApp_class_font_h();

	public abstract int getApp_left_space();

	public abstract int getApp_context_menu_one_h();

	public abstract int getApp_context_menu_three_h();

	public abstract int getApp_context_menu_right_movement();

	public abstract int getSearch_left_space();

	public abstract int getSearch_font_h();

	public abstract int getSearch_box_h();

	public abstract int getSearch_box_w();

	public abstract int getSearch_mode_app_icon_space();

	public abstract int getSearch_icon_w();

	public abstract int getSearch_icon_h();

	public abstract int getSearch_box_app_icon_space();

	public abstract int getApp_context_menu_item_left_padding();

	public abstract int getApp_context_menu_item_top_padding();

	public abstract int getApp_context_menu_item_bottom_padding();

	public abstract int getApp_context_menu_item_text_size();

	public abstract int getApp_context_menu_top_padding();

	public abstract int getApp_context_menu_bottom_padding();

	public abstract int getSettings_left_space();

	public abstract int getSettings_right_space();

	public abstract int getSettings_title_text_size();

	public abstract int getSettings_title_top_padding();

	public abstract int getSettings_title_item_space();

	public abstract int getSettings_item_space();

	public abstract int getSettings_group_item_space();

	public abstract int getSettings_editbox_h();

	public abstract int getSettings_accent_color_panel_size();

	public abstract int getSettings_accents_chooser_size();

	public abstract int getSettings_accents_chooser_space();

	public abstract int getSettings_accents_chooser_text_size();

	public abstract int getSetings_normal_text_size();

	public abstract int getTile_name_bottom_margin();

	public abstract int getDrag_view_wide_w();;

}
