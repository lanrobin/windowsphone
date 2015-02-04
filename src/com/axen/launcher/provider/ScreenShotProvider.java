package com.axen.launcher.provider;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import com.axen.launcher.provider.WP7Launcher.ScreenShot;
import com.axen.utils.AXLog;
import com.axen.utils.Utils;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;

public class ScreenShotProvider extends ContentProvider {

	private DatabaseHelper mdb = null;

	// Uri工具类
	private static final UriMatcher sUriMatcher;
	private static final Map<String, String> sAppProjectionMap;

	private static final int SHOTS = 1;
	private static final int SHOT_ID = 2;

	private static final String TAG = "ScreenShotProvider";

	static {
		// Uri匹配工具类
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(WP7Launcher.ScreenShot.AUTHORITY, "screenshots",
				SHOTS);
		sUriMatcher.addURI(WP7Launcher.ScreenShot.AUTHORITY, "screenshots/#",
				SHOT_ID);

		sAppProjectionMap = new HashMap<String, String>();

		sAppProjectionMap.put(ScreenShot._ID, ScreenShot._ID);
		sAppProjectionMap.put(ScreenShot._COUNT, ScreenShot._COUNT);
		sAppProjectionMap.put(ScreenShot._SHOT_TYPE, ScreenShot._SHOT_TYPE);
		sAppProjectionMap.put(ScreenShot._DATA, ScreenShot._DATA);
	}

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		int count = 0;
		SQLiteDatabase db = mdb.getWritableDatabase();

		// delete file first.

		count = deleteFiles(db, ScreenShot.TABLE_NAME, uri, where, whereArgs);

		AXLog.d(TAG, "delete " + count + "files");
		count = 0;
		switch (sUriMatcher.match(uri)) {
		case SHOTS:
			count = db.delete(ScreenShot.TABLE_NAME, where, whereArgs);
			break;
		case SHOT_ID:
			String id = uri.getPathSegments().get(1);
			count = db.delete(ScreenShot.TABLE_NAME,
					ScreenShot._ID
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
		case SHOTS:
			type = ScreenShot.CONTENT_TYPE;
			break;
		case SHOT_ID:
			type = ScreenShot.CONTENT_ITEM_TYPE;
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		return type;
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		if (sUriMatcher.match(uri) != SHOTS) {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		String fileName = null;
		ContentValues values;
		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			values = new ContentValues();
		}

		fileName = values.getAsString(ScreenShot._DATA);
		if (TextUtils.isEmpty(fileName)) {
			fileName = Utils.getUUID();
			values.put(ScreenShot._DATA, fileName);
		}

		SQLiteDatabase db = mdb.getWritableDatabase();
		long rowId = db.insert(ScreenShot.TABLE_NAME, null, values);
		if (rowId > 0) {
			Uri noteUri = ContentUris.withAppendedId(ScreenShot.CONTENT_URI,
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
		qb.setTables(ScreenShot.TABLE_NAME);

		switch (sUriMatcher.match(uri)) {
		case SHOTS:
			qb.setProjectionMap(sAppProjectionMap);
			break;

		case SHOT_ID:
			qb.setProjectionMap(sAppProjectionMap);
			qb.appendWhere(ScreenShot._ID + "=" + uri.getPathSegments().get(1));
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		// If no sort order is specified use the default
		String orderBy;
		if (TextUtils.isEmpty(sortOrder)) {
			orderBy = ScreenShot.DEFAULT_SORT_ORDER;
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
		case SHOTS:
			count = db.update(ScreenShot.TABLE_NAME, values, where, whereArgs);
			break;

		case SHOT_ID:
			String noteId = uri.getPathSegments().get(1);
			count = db.update(ScreenShot.TABLE_NAME, values,
					ScreenShot._ID
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

	@Override
	public ParcelFileDescriptor openFile(Uri uri, String mode)
			throws FileNotFoundException {
		ParcelFileDescriptor pfd = null;
		String noteId = uri.getPathSegments().get(1);
		Cursor c = mdb.getReadableDatabase().query(ScreenShot.TABLE_NAME,
				new String[] { ScreenShot._DATA }, ScreenShot._ID + " = ?",
				new String[] { noteId }, null, null, null);
		
		if(c == null || c.getCount() < 1) {
			return null;
		}
		c.moveToNext();
		String fileName = c.getString(0);
		String path = getContext().getFilesDir().getAbsolutePath();
		fileName = path +"/" + fileName;
		int match = sUriMatcher.match(uri);
		if (match != SHOT_ID) {
			AXLog.w(TAG, "Try to open an unsupported uri for file. " + uri);
			return pfd;
		}
		// pfd = this.openFileHelper(uri, mode);
		int modeBits = ParcelFileDescriptor.MODE_CREATE | ParcelFileDescriptor.MODE_READ_WRITE;
		pfd = ParcelFileDescriptor.open(new File(fileName), modeBits);
		return pfd;
	}

	private int deleteFiles(SQLiteDatabase db, String table, Uri uri,
			String where, String[] whereArgs) {
		Cursor c = db.query(table, new String[] { ScreenShot._DATA }, where,
				whereArgs, null, null, null);
		String path = getContext().getFilesDir().getAbsolutePath();
		int i = 0;
		if (c == null || c.getCount() < 1) {
			return 0;
		}
		try {
			while (c.moveToNext()) {
				String fileName = c.getString(0);
				path = path +"/" + fileName;
				if (!TextUtils.isEmpty(path)) {
					new File(path).delete();
					i++;
				}
			}
		} catch (Throwable ex) {

		}
		return i;
	}
}
