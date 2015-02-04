package com.axen.launcher.wp7.ui.size;

import android.graphics.Point;

public class SizeScale extends Size {
	private double mScale = 1.0;
	private static final double BASE_W = 480.0;
	SizeScale(Point p) {
		screen_w = p.x;
		mScale = p.x/BASE_W;
		
	}
	
	private int screen_w = 480;
	private int tile_space_w = 386;
	private int workspace_w = 866;
	private int button_space_w = 94;
	private int app_space_w = 386;
	private int top_margin_h = 94;
	private int tile_left_space = 28;
	private int tiles_space = 12;
	private int tile_w = 172;
	private int tile_h = 172;
	private int tile_icon_w = 72;
	private int tile_icon_h = 72;
	private int tile_edit_w = 132;
	private int tile_edit_h = 132;
	private int tile_name_font_h = 20;
	private int tile_name_h = 36;
	private int tile_name_left_margin = 6;
	private int tile_indicator_font_h = 42;
	private int drag_view_w = 214;
	private int drag_view_h = 214;
	private int status_bar_h = 26;
	private int status_bar_font_h = 14;
	private int status_item_narrow_w = 24;
	private int status_item_wide_w = 40;
	private int status_item_padding = 3;
	private int button_w = 42;
	private int button_h = 42;
	private int inactive_tile_w = 130;
	private int inactive_tile_h = 130;

	private int wide_tile_w = 356;
	private int buttons_space = 30;
	private int app_icon_w = 60;
	private int app_icon_h = 60;
	private int app_icon_space = 12;
	private int app_name_font_h = 22;
	private int app_class_view_w = 100;
	private int app_class_view_h = 100;
	private int app_class_view_space = 10;
	private int app_class_font_h = 26;
	private int app_left_space = 98;
	private int app_context_menu_one_h = 89;
	private int app_context_menu_three_h = 219;
	private int app_context_menu_right_movement = 10;
	private int search_left_space = 24;
	private int search_font_h = 36;
	private int search_box_h = 44;
	private int search_box_w = 432;
	private int search_mode_app_icon_space = 24;
	private int search_icon_w = 60;
	private int search_icon_h = 60;
	private int search_box_app_icon_space = 12;
	private int app_context_menu_item_left_padding = 20;
	private int app_context_menu_item_top_padding = 12;
	private int app_context_menu_item_bottom_padding = 12;
	private int app_context_menu_item_text_size = 20;
	private int app_context_menu_top_padding = 12;
	private int app_context_menu_bottom_padding = 12;

	private int settings_left_space = 30;
	private int settings_right_space = 30;
	private int settings_title_text_size = 42;
	private int settings_title_top_padding = 10;
	private int settings_title_item_space = 15;
	private int settings_item_space = 40;
	private int settings_group_item_space = 5;
	private int settings_editbox_h = 44;
	private int settings_accent_color_panel_size = 24;
	private int settings_accents_chooser_size = 40;
	private int settings_accents_chooser_space = 22;
	private int settings_accents_chooser_text_size = 24;
	private int setings_normal_text_size = 18;
	private int tile_name_bottom_margin = 12;
	private int drag_view_wide_w = 398;
	public int getScreen_w() {
		return (int)(mScale * screen_w);
	}
	public int getTile_space_w() {
		return (int)(mScale * tile_space_w);
	}
	public int getWorkspace_w() {
		return (int)(mScale * workspace_w);
	}
	public int getButton_space_w() {
		return (int)(mScale * button_space_w);
	}
	public int getApp_space_w() {
		return (int)(mScale * app_space_w);
	}
	public int getTop_margin_h() {
		return (int)(mScale * top_margin_h);
	}
	public int getTile_left_space() {
		return (int)(mScale * tile_left_space);
	}
	public int getTiles_space() {
		return (int)(mScale * tiles_space);
	}
	public int getTile_w() {
		return (int)(mScale * tile_w);
	}
	public int getTile_h() {
		return (int)(mScale * tile_h);
	}
	public int getTile_icon_w() {
		return (int)(mScale * tile_icon_w);
	}
	public int getTile_icon_h() {
		return (int)(mScale * tile_icon_h);
	}
	public int getTile_edit_w() {
		return (int)(mScale * tile_edit_w);
	}
	public int getTile_edit_h() {
		return (int)(mScale * tile_edit_h);
	}
	public int getTile_name_font_h() {
		return (int)(mScale * tile_name_font_h);
	}
	public int getTile_name_h() {
		return (int)(mScale * tile_name_h);
	}
	public int getTile_name_left_margin() {
		return (int)(mScale * tile_name_left_margin);
	}
	public int getTile_indicator_font_h() {
		return (int)(mScale * tile_indicator_font_h);
	}
	public int getDrag_view_w() {
		return (int)(mScale * drag_view_w);
	}
	public int getDrag_view_h() {
		return (int)(mScale * drag_view_h);
	}
	public int getStatus_bar_h() {
		return (int)(mScale * status_bar_h);
	}
	public int getStatus_bar_font_h() {
		return (int)(mScale * status_bar_font_h);
	}
	public int getStatus_item_narrow_w() {
		return (int)(mScale * status_item_narrow_w);
	}
	public int getStatus_item_wide_w() {
		return (int)(mScale * status_item_wide_w);
	}
	public int getStatus_item_padding() {
		return (int)(mScale * status_item_padding);
	}
	public int getButton_w() {
		return (int)(mScale * button_w);
	}
	public int getButton_h() {
		return (int)(mScale * button_h);
	}
	public int getInactive_tile_w() {
		return (int)(mScale * inactive_tile_w);
	}
	public int getInactive_tile_h() {
		return (int)(mScale * inactive_tile_h);
	}
	public int getWide_tile_w() {
		return (int)(mScale * wide_tile_w);
	}
	public int getButtons_space() {
		return (int)(mScale * buttons_space);
	}
	public int getApp_icon_w() {
		return (int)(mScale * app_icon_w);
	}
	public int getApp_icon_h() {
		return (int)(mScale * app_icon_h);
	}
	public int getApp_icon_space() {
		return (int)(mScale * app_icon_space);
	}
	public int getApp_name_font_h() {
		return (int)(mScale * app_name_font_h);
	}
	public int getApp_class_view_w() {
		return (int)(mScale * app_class_view_w);
	}
	public int getApp_class_view_h() {
		return (int)(mScale * app_class_view_h);
	}
	public int getApp_class_view_space() {
		return (int)(mScale * app_class_view_space);
	}
	public int getApp_class_font_h() {
		return (int)(mScale * app_class_font_h);
	}
	public int getApp_left_space() {
		return (int)(mScale * app_left_space);
	}
	public int getApp_context_menu_one_h() {
		return (int)(mScale * app_context_menu_one_h);
	}
	public int getApp_context_menu_three_h() {
		return (int)(mScale * app_context_menu_three_h);
	}
	public int getApp_context_menu_right_movement() {
		return (int)(mScale * app_context_menu_right_movement);
	}
	public int getSearch_left_space() {
		return (int)(mScale * search_left_space);
	}
	public int getSearch_font_h() {
		return (int)(mScale * search_font_h);
	}
	public int getSearch_box_h() {
		return (int)(mScale * search_box_h);
	}
	public int getSearch_box_w() {
		return (int)(mScale * search_box_w);
	}
	public int getSearch_mode_app_icon_space() {
		return (int)(mScale * search_mode_app_icon_space);
	}
	public int getSearch_icon_w() {
		return (int)(mScale * search_icon_w);
	}
	public int getSearch_icon_h() {
		return (int)(mScale * search_icon_h);
	}
	public int getSearch_box_app_icon_space() {
		return (int)(mScale * search_box_app_icon_space);
	}
	public int getApp_context_menu_item_left_padding() {
		return (int)(mScale * app_context_menu_item_left_padding);
	}
	public int getApp_context_menu_item_top_padding() {
		return (int)(mScale * app_context_menu_item_top_padding);
	}
	public int getApp_context_menu_item_bottom_padding() {
		return (int)(mScale * app_context_menu_item_bottom_padding);
	}
	public int getApp_context_menu_item_text_size() {
		return (int)(mScale * app_context_menu_item_text_size);
	}
	public int getApp_context_menu_top_padding() {
		return (int)(mScale * app_context_menu_top_padding);
	}
	public int getApp_context_menu_bottom_padding() {
		return (int)(mScale * app_context_menu_bottom_padding);
	}
	public int getSettings_left_space() {
		return (int)(mScale * settings_left_space);
	}
	public int getSettings_right_space() {
		return (int)(mScale * settings_right_space);
	}
	public int getSettings_title_text_size() {
		return (int)(mScale * settings_title_text_size);
	}
	public int getSettings_title_top_padding() {
		return (int)(mScale * settings_title_top_padding);
	}
	public int getSettings_title_item_space() {
		return (int)(mScale * settings_title_item_space);
	}
	public int getSettings_item_space() {
		return (int)(mScale * settings_item_space);
	}
	public int getSettings_group_item_space() {
		return (int)(mScale * settings_group_item_space);
	}
	public int getSettings_editbox_h() {
		return (int)(mScale * settings_editbox_h);
	}
	public int getSettings_accent_color_panel_size() {
		return (int)(mScale * settings_accent_color_panel_size);
	}
	public int getSettings_accents_chooser_size() {
		return (int)(mScale * settings_accents_chooser_size);
	}
	public int getSettings_accents_chooser_space() {
		return (int)(mScale * settings_accents_chooser_space);
	}
	public int getSettings_accents_chooser_text_size() {
		return (int)(mScale * settings_accents_chooser_text_size);
	}
	public int getSetings_normal_text_size() {
		return (int)(mScale * setings_normal_text_size);
	}
	public int getTile_name_bottom_margin() {
		return (int)(mScale * tile_name_bottom_margin);
	}
	public int getDrag_view_wide_w() {
		return (int)(mScale * drag_view_wide_w);
	}
}
