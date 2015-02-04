package com.axen.launcher.provider;

import com.axen.utils.AXLog;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "appinfo.db";

	/*
	 * 当前数据库版本。
	 */
	private static final int DATABASE_VERSION = 1;
	private static final String TAG = "DatabaseHelper";

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		createTileTable(db);
		createAppTable(db);
		createScreenShotTable(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		AXLog.d(TAG, "oldVersion = " + oldVersion + ", newVersion = "
				+ newVersion);
	}

	// create Tile info table.
	private void createTileTable(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + WP7Launcher.TileInfo.TABLE_NAME + " ("
				+ WP7Launcher.TileInfo._ID + " INTEGER PRIMARY KEY,"
				+ WP7Launcher.TileInfo._TILE_NAME + " TEXT," + WP7Launcher.TileInfo._X
				+ " INTEGER," + WP7Launcher.TileInfo._Y + " INTEGER,"
				+ WP7Launcher.TileInfo._DEFAULT + " INTEGER,"
				+ WP7Launcher.TileInfo._APP_NAME + " TEXT,"
				+ WP7Launcher.TileInfo._APP_DEFAULT_PACKAGE + " TEXT,"
				+ WP7Launcher.TileInfo._APP_DEFAULT_ACTIVITY + " TEXT,"
				+ WP7Launcher.TileInfo._APP_DEFAULT_ICON + " TEXT,"
				+ WP7Launcher.TileInfo._APP_PACKAGE + " TEXT,"
				+ WP7Launcher.TileInfo._APP_ACTIVITY + " TEXT,"
				+ WP7Launcher.TileInfo._APP_ICON + " TEXT,"
				+ WP7Launcher.TileInfo._USE_DEFAULT + " INTEGER,"
				+ WP7Launcher.TileInfo._LAUNCH_TIMES + " INTEGER,"
				+ WP7Launcher.TileInfo._WIDE_TILE + " INTEGER,"
				+ WP7Launcher.TileInfo._FLAGS + " INTEGER,"
				+ WP7Launcher.TileInfo._TYPE + " INTEGER,"
				+ WP7Launcher.TileInfo._ENABLED + " INTEGER" + ");");
	}
	
	private void createAppTable(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + WP7Launcher.AppInfo.TABLE_NAME + " ("
				+ WP7Launcher.AppInfo._ID + " INTEGER PRIMARY KEY,"
				+ WP7Launcher.AppInfo._COUNT + " INTEGER,"
				+ WP7Launcher.AppInfo._PACKAGE + " TEXT,"
				+ WP7Launcher.AppInfo._ACTIVITY + " TEXT,"
				+ WP7Launcher.AppInfo._LAUNCH_TIMES + " INTEGER,"
				+ WP7Launcher.AppInfo._FLAGS + " INTEGER,"
				+ WP7Launcher.AppInfo._PINNED + " INTEGER" + ");");
	}
	
	private void createScreenShotTable(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + WP7Launcher.ScreenShot.TABLE_NAME + " ("
				+ WP7Launcher.ScreenShot._ID + " INTEGER PRIMARY KEY,"
				+ WP7Launcher.ScreenShot._COUNT + " INTEGER,"
				+ WP7Launcher.ScreenShot._SHOT_TYPE + " INTEGER,"
				+ WP7Launcher.ScreenShot._DATA + " TEXT" + ");");
	}
}
