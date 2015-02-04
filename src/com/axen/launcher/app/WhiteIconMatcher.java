package com.axen.launcher.app;

import java.util.Map;
import java.util.HashMap;

import com.axen.launcher.wp7.main.R;
import com.axen.launcher.wp7.ui.apputil.ClassifyApp;
import com.axen.launcher.wp7.ui.apputil.ClassifyFactory;

import android.content.pm.ResolveInfo;
import android.text.TextUtils;

public class WhiteIconMatcher {
	
	public static Integer matchIcon(ResolveInfo ri) {
		String name = ri.activityInfo.name;
		Integer icon = getStrickPackageIcon(name);
		return icon;
	}

	public static String shortenName(String name) {
		ClassifyApp ca = ClassifyFactory.get();
		int len  = 10;
		if (!TextUtils.isEmpty(name)) {
			if(ca != null) {
				len = ca.getMaxNameLen();
			}
			if (name.length() > len) {
				name = name.substring(0, len) + " ...";
			}
		}
		return name;
	}

	private static Map<String, Integer> SMATCHER = new HashMap<String, Integer>();
	private static Map<String, Integer> TYPEMATCHER = new HashMap<String, Integer>();

	static {
	
		/** SAMSUNG */
		SMATCHER.put("com.android.settings.Settings", R.drawable.t_settings);
		SMATCHER.put("com.android.mms.ui.ConversationComposer", R.drawable.t_message);
		SMATCHER.put("com.sec.android.app.contacts.DialerEntryActivity", R.drawable.t_phone);
		SMATCHER.put("com.sec.android.app.camera.Camera", R.drawable.t_camera);
		SMATCHER.put("com.android.browser.BrowserActivity", R.drawable.t_browser);
		SMATCHER.put("com.google.android.maps.MapsActivity", R.drawable.t_map);
		SMATCHER.put("com.android.vending.AssetBrowserActivity", R.drawable.t_market);
		SMATCHER.put("com.google.android.gm.ConversationListActivityGmail", R.drawable.t_gmail);
		SMATCHER.put("com.sec.android.app.videoplayer.activity.VideoList", R.drawable.t_camera);
		SMATCHER.put("com.sec.android.app.calculator.Calculator", R.drawable.t_calculator);
		SMATCHER.put("com.sec.android.app.clockpackage.ClockPackage", R.drawable.t_alarm);
		SMATCHER.put("com.android.music.list.activity.MpMainTabActivity", R.drawable.t_music);
		
		/** SonyEricsson */
		SMATCHER.put("com.sonyericsson.android.camera3d.CameraActivity", R.drawable.t_camera);
		SMATCHER.put("com.android.deskclock.DeskClock", R.drawable.t_alarm);
		SMATCHER.put("com.sonyericsson.android.camera.CameraActivity", R.drawable.t_camera);
		SMATCHER.put("com.sonyericsson.conversations.ui.ConversationListActivity", R.drawable.t_message);
		SMATCHER.put("com.android.deskclock.AlarmClock", R.drawable.t_alarm);
		SMATCHER.put("com.google.android.googlequicksearchbox.SearchActivity", R.drawable.t_googlesearch);
		SMATCHER.put("com.sonyericsson.music.PlayerActivity", R.drawable.t_music);
		SMATCHER.put("com.android.calculator2.Calculator", R.drawable.t_calculator);
		SMATCHER.put("com.sonyericsson.android.socialphonebook.DialerEntryActivity", R.drawable.t_phone);
		SMATCHER.put("com.sonyericsson.android.camera3d.viewer.Browser", R.drawable.t_music);
		SMATCHER.put("com.android.deskclock.AlarmClock", R.drawable.t_alarm);
		// SMATCHER.put("com.sonyericsson.gallery.Gallery", R.drawable.t_music);
		
		/** HTC **/
		SMATCHER.put("com.android.mms.ui.ConversationList", R.drawable.t_message);
		SMATCHER.put("com.htc.android.worldclock.WorldClockTabControl", R.drawable.t_alarm);
		SMATCHER.put("com.htc.album.AlbumMain.ActivityMainCarousel", R.drawable.t_music);
		SMATCHER.put("com.android.camera.CameraEntry", R.drawable.t_camera);
		SMATCHER.put("com.android.camera.CamcorderEntry", R.drawable.t_camera);
		SMATCHER.put("com.android.settings.framework.activity.HtcSettings", R.drawable.t_settings);
		SMATCHER.put("com.htc.music.HtcMusic", R.drawable.t_music);
		SMATCHER.put("com.android.htcdialer.Dialer", R.drawable.t_phone);
		SMATCHER.put("com.htc.connectedMedia.ConnectedMedias", R.drawable.t_music);
		
		/** MOTO */
		SMATCHER.put("com.motorola.blur.conversations.ui.ConversationList", R.drawable.t_message);
		SMATCHER.put("com.android.contacts.DialtactsActivity", R.drawable.t_phone);
		SMATCHER.put("com.motorola.blur.alarmclock.AlarmClock", R.drawable.t_alarm);
		SMATCHER.put("com.motorola.Camera.Camera", R.drawable.t_camera);
		SMATCHER.put("com.motorola.blur.messaging.MessagingActivity", R.drawable.t_message);
		SMATCHER.put("com.motorola.Camera.Camcorder", R.drawable.t_camera);
		
		/** ANDROID Emulator */
		SMATCHER.put("com.android.music.MusicBrowserActivity", R.drawable.t_music);
		// SMATCHER.put("com.android.camera.GalleryPicker", R.drawable.t_music);
		SMATCHER.put("com.android.mms.ui.ConversationList", R.drawable.t_message);
		SMATCHER.put("com.android.contacts.DialtactsActivity", R.drawable.t_phone);
		SMATCHER.put("com.android.camera.Camera", R.drawable.t_camera);
		SMATCHER.put("com.android.quicksearchbox.SearchActivity", R.drawable.t_googlesearch);
		
		/**
		
		
		SMACTHER.put("com.android.deskclock.AlarmClock", R.drawable.t_alarm);
		SMACTHER.put("com.android.mms", R.drawable.t_message);
		SMACTHER.put("com.android.mms", R.drawable.t_message);
		SMACTHER.put("com.android.mms", R.drawable.t_message);
		**/
		
		/**
		 * 
		 */
		
		/** ����� */
		SMATCHER.put("com.facebook.orca.auth.StartScreenActivity", R.drawable.t_facebook_messenger);
		SMATCHER.put("com.google.android.youtube.app.froyo.phone.HomeActivity",
				R.drawable.t_youtube);
		SMATCHER.put("com.google.android.apps.plus.phone.HomeActivity", R.drawable.t_googleplus);
		SMATCHER.put("com.pandora.android.Main", R.drawable.t_pandora);
		SMATCHER.put("com.skype.rover.Main", R.drawable.t_skype);
		SMATCHER.put("com.whatsapp.Main", R.drawable.t_what_s_up_messager);
		SMATCHER.put("com.twitter.android.StartActivity", R.drawable.t_twitter);
		SMATCHER.put("com.facebook.katana.LoginActivity", R.drawable.t_facebook);
		SMATCHER.put("com.google.android.youtube.HomeActivity", R.drawable.t_youtube);
	}
	
	static {
		
		/* SAMSUNG */
		TYPEMATCHER.put("com.android.mms.ui.ConversationComposer", TileItemInfo.TYPE_MESSAGE);
		TYPEMATCHER.put("com.sec.android.app.contacts.DialerEntryActivity", TileItemInfo.TYPE_PHONE);
		TYPEMATCHER.put("com.sec.android.app.contacts.ContactsEntryActivity", TileItemInfo.TYPE_PEOPLE);
		TYPEMATCHER.put("com.cooliris.media.Gallery", TileItemInfo.TYPE_GALLERY);
		
		/* Sony Ericsson */
		TYPEMATCHER.put("com.sonyericsson.conversations.ui.ConversationListActivity", TileItemInfo.TYPE_MESSAGE);
		TYPEMATCHER.put("com.sonyericsson.android.socialphonebook.DialerEntryActivity", TileItemInfo.TYPE_PHONE);
		TYPEMATCHER.put("com.sonyericsson.android.socialphonebook.LaunchActivity", TileItemInfo.TYPE_PEOPLE);
		TYPEMATCHER.put("com.sonyericsson.gallery.Gallery", TileItemInfo.TYPE_GALLERY);
		TYPEMATCHER.put("com.sonyericsson.android.camera3d.viewer.Browser", TileItemInfo.TYPE_GALLERY);
		
		/* HTC */
		TYPEMATCHER.put("com.android.mms.ui.ConversationList", TileItemInfo.TYPE_MESSAGE);
		TYPEMATCHER.put("com.android.htcdialer.Dialer", TileItemInfo.TYPE_PHONE);
		TYPEMATCHER.put("com.android.htccontacts.BrowseLayerCarouselActivity", TileItemInfo.TYPE_PEOPLE);
		TYPEMATCHER.put("com.htc.album.AlbumMain.ActivityMainCarousel", TileItemInfo.TYPE_GALLERY);
		
		/* MOTO */
		TYPEMATCHER.put("com.motorola.blur.conversations.ui.ConversationList", TileItemInfo.TYPE_MESSAGE);
		TYPEMATCHER.put("com.android.contacts.DialtactsActivity", TileItemInfo.TYPE_PHONE);
		TYPEMATCHER.put("com.android.contacts.DialtactsContactsEntryActivity", TileItemInfo.TYPE_PEOPLE);
		TYPEMATCHER.put("com.motorola.cgallery.Dashboard", TileItemInfo.TYPE_GALLERY);
		
		/* Google Emulator */
		TYPEMATCHER.put("com.android.mms.ui.ConversationList", TileItemInfo.TYPE_MESSAGE);
		TYPEMATCHER.put("com.android.contacts.DialtactsActivity", TileItemInfo.TYPE_PHONE);
		TYPEMATCHER.put("com.android.contacts.DialtactsContactsEntryActivity", TileItemInfo.TYPE_PEOPLE);
		TYPEMATCHER.put("com.android.camera.GalleryPicker", TileItemInfo.TYPE_GALLERY);
		
		/* 
		TYPEMACTHER.put("com.android.contacts.DialtactsContactsEntryActivity", SPECIAL_TILE);
		TYPEMACTHER.put("", SPECIAL_TILE);
		TYPEMACTHER.put("", SPECIAL_TILE);
		TYPEMACTHER.put("", SPECIAL_TILE);
		*/
		
	};
	private static Integer getStrickPackageIcon(String pn) {
		Integer icon = SMATCHER.get(pn);
		return icon;
	}
	
	public static Integer getTileType(String activityName) {
		if(TextUtils.isEmpty(activityName)) {
			return TileItemInfo.TYPE_INVALID;
		}
		Integer lookup = TYPEMATCHER.get(activityName);
		if(lookup != null) {
			return lookup;
		}
		return TileItemInfo.TYPE_COMMON;
	}
}
