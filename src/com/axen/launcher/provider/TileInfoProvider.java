package com.axen.launcher.provider;

import java.util.HashMap;
import java.util.Map;

import com.axen.launcher.provider.WP7Launcher.TileInfo;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class TileInfoProvider extends ContentProvider {

	private DatabaseHelper mdb = null;

	// Uri工具类
	private static final UriMatcher sUriMatcher;
	private static final Map<String, String> sTileProjectionMap;

	private static final int TILES = 1;
	private static final int TILE_ID = 2;

	static {
		// Uri匹配工具类
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(WP7Launcher.TileInfo.AUTHORITY,
				WP7Launcher.TileInfo.PATH_TILES, TILES);
		sUriMatcher.addURI(WP7Launcher.TileInfo.AUTHORITY,
				WP7Launcher.TileInfo.PATH_TILES + "/#", TILE_ID);

		sTileProjectionMap = new HashMap<String, String>();
		sTileProjectionMap.put(TileInfo._ID, TileInfo._ID);
		sTileProjectionMap.put(TileInfo._COUNT, TileInfo._COUNT);
		sTileProjectionMap.put(TileInfo._TILE_NAME, TileInfo._TILE_NAME);
		sTileProjectionMap.put(TileInfo._X, TileInfo._X);
		sTileProjectionMap.put(TileInfo._Y, TileInfo._Y);
		sTileProjectionMap.put(TileInfo._DEFAULT, TileInfo._DEFAULT);
		sTileProjectionMap.put(TileInfo._APP_PACKAGE, TileInfo._APP_PACKAGE);
		sTileProjectionMap.put(TileInfo._APP_NAME, TileInfo._APP_NAME);
		sTileProjectionMap.put(TileInfo._APP_ICON, TileInfo._APP_ICON);
		sTileProjectionMap.put(TileInfo._APP_ACTIVITY, TileInfo._APP_ACTIVITY);
		sTileProjectionMap.put(TileInfo._APP_DEFAULT_ACTIVITY,
				TileInfo._APP_DEFAULT_ACTIVITY);
		sTileProjectionMap.put(TileInfo._APP_DEFAULT_ICON,
				TileInfo._APP_DEFAULT_ICON);
		sTileProjectionMap.put(TileInfo._APP_DEFAULT_PACKAGE,
				TileInfo._APP_DEFAULT_PACKAGE);
		sTileProjectionMap.put(TileInfo._ENABLED, TileInfo._ENABLED);
		sTileProjectionMap.put(TileInfo._FLAGS, TileInfo._FLAGS);
		sTileProjectionMap.put(TileInfo._LAUNCH_TIMES, TileInfo._LAUNCH_TIMES);
		sTileProjectionMap.put(TileInfo._TILE_NAME, TileInfo._TILE_NAME);
		sTileProjectionMap.put(TileInfo._USE_DEFAULT, TileInfo._USE_DEFAULT);
		sTileProjectionMap.put(TileInfo._WIDE_TILE, TileInfo._WIDE_TILE);
		sTileProjectionMap.put(TileInfo._TYPE, TileInfo._TYPE);

	}

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		int count = 0;
		SQLiteDatabase db = mdb.getWritableDatabase();
		switch (sUriMatcher.match(uri)) {
		case TILES:
			count = db.delete(TileInfo.TABLE_NAME, where, whereArgs);
			break;
		case TILE_ID:
			String id = uri.getPathSegments().get(1);
			count = db.delete(TileInfo.TABLE_NAME,
					TileInfo._ID
							+ "="
							+ id
							+ (!TextUtils.isEmpty(where) ? " AND (" + where
									+ ')' : ""), whereArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		if (count > 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		}
		return count;
	}

	@Override
	public String getType(Uri uri) {
		String type = null;
		switch (sUriMatcher.match(uri)) {
		case TILES:
			type = TileInfo.CONTENT_TYPE;
			break;
		case TILE_ID:
			type = TileInfo.CONTENT_ITEM_TYPE;
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		return type;
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		if (sUriMatcher.match(uri) != TILES) {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		ContentValues values;
		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			values = new ContentValues();
		}

		SQLiteDatabase db = mdb.getWritableDatabase();
		long rowId = db.insert(TileInfo.TABLE_NAME, TileInfo._FLAGS, values);
		if (rowId > 0) {
			Uri noteUri = ContentUris.withAppendedId(TileInfo.CONTENT_URI,
					rowId);
			getContext().getContentResolver().notifyChange(noteUri, null);
			return noteUri;
		}

		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public boolean onCreate() {
		mdb = new DatabaseHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(TileInfo.TABLE_NAME);

		switch (sUriMatcher.match(uri)) {
		case TILES:
			qb.setProjectionMap(sTileProjectionMap);
			break;

		case TILE_ID:
			qb.setProjectionMap(sTileProjectionMap);
			qb.appendWhere(TileInfo._ID + "=" + uri.getPathSegments().get(1));
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		// If no sort order is specified use the default
		String orderBy;
		if (TextUtils.isEmpty(sortOrder)) {
			orderBy = TileInfo.DEFAULT_SORT_ORDER;
		} else {
			orderBy = sortOrder;
		}

		// Get the database and run the query
		SQLiteDatabase db = mdb.getReadableDatabase();
		Cursor c = qb.query(db, projection, selection, selectionArgs, null,
				null, orderBy);

		// Tell the cursor what uri to watch, so it knows when its source data
		// changes
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String where,
			String[] whereArgs) {
		SQLiteDatabase db = mdb.getWritableDatabase();
		int count;
		switch (sUriMatcher.match(uri)) {
		case TILES:
			count = db.update(TileInfo.TABLE_NAME, values, where, whereArgs);
			break;

		case TILE_ID:
			String noteId = uri.getPathSegments().get(1);
			count = db.update(TileInfo.TABLE_NAME, values,
					TileInfo._ID
							+ "="
							+ noteId
							+ (!TextUtils.isEmpty(where) ? " AND (" + where
									+ ')' : ""), whereArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		if (count > 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		}
		return count;
	}

}
