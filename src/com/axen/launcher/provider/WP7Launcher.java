package com.axen.launcher.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public class WP7Launcher {
	private WP7Launcher() {
	};

	/**
	 * Tile和应用Icon是存放在目录下的。
	 */
	public static final String APP_ICON_DIR = "app_icons";

	public static final String SCHEMA = "content://";

	public static final class TileInfo implements BaseColumns {
		public static final String AUTHORITY = "axen-tileinfo";
		public static final String TABLE_NAME = "tile_info";

		public static final String PATH_TILES = "tiles";

		public static final String DEFAULT_SORT_ORDER = "_y DESC";// 按行排序

		/**
		 * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
		 */
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.axen.tileinfo";

		/**
		 * The MIME type of a {@link #CONTENT_URI} sub-directory of a single
		 * note.
		 */
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.axen.tileinfo";

		/**
		 * The content:// style URL for this table
		 */
		public static final Uri CONTENT_URI = Uri.parse(SCHEMA + AUTHORITY
				+ "/"+ PATH_TILES);

		/**
		 * The content URI base for a single note. Callers must append a numeric
		 * note id to this Uri to retrieve a note
		 */
		public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEMA
				+ AUTHORITY + "/" +PATH_TILES);

		/**
		 * The content URI match pattern for a single note, specified by its ID.
		 * Use this to match incoming URIs or to construct an Intent.
		 */
		public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEMA
				+ AUTHORITY + "/"+ PATH_TILES + "/#");

		/**
		 * 显示在Tile上的应用名字，如果是系统默认的Tile，则名字不能改 比如 Phone Message之类的。
		 */
		public static final String _TILE_NAME = "_tile_name";
		public static final String _X = "_x";
		public static final String _Y = "_y";

		/** 是不是系统默认的Tile, 系统默认的Tile是不能更改图标的。 */
		public static final String _DEFAULT = "_default";

		/**
		 * 关联的应用程序的名称，如果是系统默认的Tile,可能用户会用来关系第 三方应用，这样的话就要存储地三方应用的名称
		 */
		public static final String _APP_NAME = "_app_name";

		/**
		 * 关联应用的包名
		 */
		public static final String _APP_DEFAULT_PACKAGE = "_default_package";

		/**
		 * 关联应用的Activity名字
		 */
		public static final String _APP_DEFAULT_ACTIVITY = "_default_activity";

		/**
		 * 关联应用的Icon目录名字
		 */
		public static final String _APP_DEFAULT_ICON = "_default_icon";

		/**
		 * 关联第三方应用的包名
		 */
		public static final String _APP_PACKAGE = "_package";

		/**
		 * 关联第三方应用的Activity
		 */
		public static final String _APP_ACTIVITY = "_activity";

		/**
		 * 关联第三方应用的_icon
		 */
		public static final String _APP_ICON = "_icon";

		/**
		 * 是不是使用第三方的应用启动, 0表示用，1表示不用。
		 */
		public static final String _USE_DEFAULT = "_use_default";

		/**
		 * 应用程序打开的次数
		 */
		public static final String _LAUNCH_TIMES = "_launch_times";

		/**
		 * Tile 是不是一个宽Tile, 如果是，则要占用一行显示，其它Tile就不能在这上面显示了。
		 */
		public static final String _WIDE_TILE = "_wide_tile";

		/**
		 * Tile 的flag,如果能不能移动，能不能删除之类，这个在上层应用具体实现。
		 */
		public static final String _FLAGS = "_flags";

		/**
		 * Tile 信息是不是要显示
		 */
		public static final String _ENABLED = "_enabled";

		/**
		 * Tile的各类，可能是appwidget或是app
		 */
		public static final String _TYPE = "_type";
	}

	public static final class AppInfo implements BaseColumns {

		public static final String AUTHORITY = "axen-appinfo";
		
		public static final String TABLE_NAME = "app_info";

		public static final String PATH_APPS = "/apps";
		public static final String PATH_APP_ID = "/apps/";

		public static final String DEFAULT_SORT_ORDER = "_name DESC";

		/**
		 * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
		 */
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.axen.appinfo";

		/**
		 * The MIME type of a {@link #CONTENT_URI} sub-directory of a single
		 * note.
		 */
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.axen.appinfo";

		/**
		 * The content:// style URL for this table
		 */
		public static final Uri CONTENT_URI = Uri.parse(SCHEMA + AUTHORITY
				+ PATH_APPS);

		/**
		 * The content URI base for a single note. Callers must append a numeric
		 * note id to this Uri to retrieve a note
		 */
		public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEMA
				+ AUTHORITY + PATH_APP_ID);

		/**
		 * App 的flag,如果能不能移动，能不能删除之类，这个在上层应用具体实现。
		 */
		public static final String _FLAGS = "_flags";
		public static final String _PACKAGE = "_package_name";

		/**
		 * 关联第三方应用的Activity
		 */
		public static final String _ACTIVITY = "_activity";

		public static final String _APP_NAME = "_name";

		/**
		 * 这个应该是不是已经pin到Start上了。
		 */
		public static final String _PINNED = "_pinned";
		/**
		 * 应用程序打开的次数
		 */
		public static final String _LAUNCH_TIMES = "_launch_times";

	}
	
	public static final class ScreenShot implements BaseColumns {
	public static final String AUTHORITY = "axen-screenshot";
		
		public static final String TABLE_NAME = "screenshot";

		public static final String SHOTS = "/screenshots";
		public static final String SHOT_ID = "/screenshots/";
		
		/** homescreen 刚起动的时候截屏的 */
		public static final int SHOT_TYPE_CREATE     = 0;  

		/** homescreen收到关屏消息时候截屏的 */
		public static final int SHOT_TYPE_RUNNING     = 1;  
		public static final int SHOT_TYPE_INVALID     = -1;
		/**
		 * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
		 */
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.axen.screenshot";

		/**
		 * The MIME type of a {@link #CONTENT_URI} sub-directory of a single
		 * note.
		 */
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.axen.screenshot";

		/**
		 * The content:// style URL for this table
		 */
		public static final Uri CONTENT_URI = Uri.parse(SCHEMA + AUTHORITY
				+ SHOTS);

		/**
		 * The content URI base for a single shot. Callers must append a numeric
		 * note id to this Uri to retrieve a note
		 */
		public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEMA
				+ AUTHORITY + SHOT_ID);

		/**
		 * 只有两个字段，一个是截屏的类型，一个是截屏的图片存储的路径。
		 */
		public static final String _SHOT_TYPE = "_type";
		public static final String _DATA = "_data";

		public static final String DEFAULT_SORT_ORDER = "_type DESC";
	}
}
