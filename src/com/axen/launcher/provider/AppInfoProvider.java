package com.axen.launcher.provider;

import java.util.HashMap;
import java.util.Map;

import com.axen.launcher.provider.WP7Launcher.AppInfo;

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

public class AppInfoProvider extends ContentProvider {

	private DatabaseHelper mdb = null;

	// Uri工具类
	private static final UriMatcher sUriMatcher;
	private static final Map<String, String> sAppProjectionMap;

	private static final int APPS = 1;
	private static final int APP_ID = 2;

	static {
		// Uri匹配工具类
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(WP7Launcher.AppInfo.AUTHORITY,"apps", APPS);
		sUriMatcher.addURI(WP7Launcher.AppInfo.AUTHORITY,
				"apps/#", APP_ID);

		sAppProjectionMap = new HashMap<String, String>();

		sAppProjectionMap.put(AppInfo._ID, AppInfo._ID);
		sAppProjectionMap.put(AppInfo._ACTIVITY, AppInfo._ACTIVITY);
		sAppProjectionMap.put(AppInfo._FLAGS, AppInfo._FLAGS);
		sAppProjectionMap.put(AppInfo._LAUNCH_TIMES, AppInfo._LAUNCH_TIMES);
		sAppProjectionMap.put(AppInfo._PACKAGE, AppInfo._PACKAGE);
		sAppProjectionMap.put(AppInfo._PINNED, AppInfo._PINNED);

	}

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		int count = 0;
		SQLiteDatabase db = mdb.getWritableDatabase();
		switch (sUriMatcher.match(uri)) {
		case APPS:
			count = db.delete(AppInfo.TABLE_NAME, where, whereArgs);
			break;
		case APP_ID:
			String id = uri.getPathSegments().get(1);
			count = db.delete(AppInfo.TABLE_NAME,
					AppInfo._ID
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
		case APPS:
			type = AppInfo.CONTENT_TYPE;
			break;
		case APP_ID:
			type = AppInfo.CONTENT_ITEM_TYPE;
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		return type;
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		if (sUriMatcher.match(uri) != APPS) {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		ContentValues values;
		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			values = new ContentValues();
		}

		SQLiteDatabase db = mdb.getWritableDatabase();
		long rowId = db.insert(AppInfo.TABLE_NAME, null, values);
		if (rowId > 0) {
			Uri noteUri = ContentUris.withAppendedId(AppInfo.CONTENT_URI,
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
		qb.setTables(AppInfo.TABLE_NAME);

		switch (sUriMatcher.match(uri)) {
		case APPS:
			qb.setProjectionMap(sAppProjectionMap);
			break;

		case APP_ID:
			qb.setProjectionMap(sAppProjectionMap);
			qb.appendWhere(AppInfo._ID + "=" + uri.getPathSegments().get(1));
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		// If no sort order is specified use the default
		String orderBy;
		if (TextUtils.isEmpty(sortOrder)) {
			orderBy = AppInfo.DEFAULT_SORT_ORDER;
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
		case APPS:
			count = db.update(AppInfo.TABLE_NAME, values, where, whereArgs);
			break;

		case APP_ID:
			String noteId = uri.getPathSegments().get(1);
			count = db.update(AppInfo.TABLE_NAME, values,
					AppInfo._ID
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
