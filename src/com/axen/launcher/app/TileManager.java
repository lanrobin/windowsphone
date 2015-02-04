package com.axen.launcher.app;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.HashMap;
import java.util.Vector;

import com.axen.launcher.app.AppManager.AppItem;
import com.axen.launcher.provider.WP7Launcher;
import com.axen.launcher.provider.WP7Launcher.TileInfo;
import com.axen.launcher.system.CallManager;
import com.axen.launcher.system.MessageManager;
import com.axen.launcher.wp7.ui.TileSpace;
import com.axen.launcher.wp7.ui.widget.CallTile;
import com.axen.launcher.wp7.ui.widget.CommonTile;
import com.axen.launcher.wp7.ui.widget.ContactsTile;
import com.axen.launcher.wp7.ui.widget.GalleryTile;
import com.axen.launcher.wp7.ui.widget.MessageTile;
import com.axen.launcher.wp7.ui.widget.Tile;
import com.axen.utils.AXLog;
import com.axen.utils.Utils;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;

public class TileManager {

	private static final String TAG = "TileManager";

	// private static final String ICONS = "icons";

	private static final int DEFAULT_TILE_NUMBER = 12; // 随便设置的

	/**
	 * 最大能加入的Tile.免费版本最多能加入10个。
	 */
	private static final int MAX_ALLOW_TILE = Integer.MAX_VALUE;

	private Context mContext = null;

	private Vector<TileItemInfo> mTiles = null;
	// private boolean[] mOccupiedSlots = null;
	private CellInfo[] mAllSlots = null;
	private PackageManager mPM = null;
	private AppManager mAM = null;
	private ContentResolver mCR = null;
	private int mRowNumber = 0;

	// 这个只是在编辑的时候才用
	private TileItemInfo mStoreForEdit = null;

	public void setEditTile(TileItemInfo tii) {
		mStoreForEdit = tii;
	}

	public TileItemInfo getEditTile() {
		return mStoreForEdit;
	}

	// 这个是在增加special tile的时候用的
	private Tile mStoreTile = null;

	public void setAddTile(Tile tile) {
		mStoreTile = tile;
	}

	public Tile getAddTile() {
		return mStoreTile;
	}

	/**
	 * 这两个CellInfo只是用来标识是不是移动到最头和最尾了。
	 */
	public static final CellInfo HEAD = new CellInfo();
	public static final CellInfo REAR = new CellInfo();

	public static final class CellInfo {
		public boolean occupied;
		public int x;
		public int y;
		public TileItemInfo tii;
	};

	private TileManager() {
		mTiles = new Vector<TileItemInfo>();
		initCells(DEFAULT_TILE_NUMBER * 2);
	}

	public void init(Context context) {
		mContext = context;
		mPM = mContext.getPackageManager();
		mAM = AppManager.getInstance();
		mCR = mContext.getContentResolver();
		// mContext.getDir(ICONS, Context.MODE_PRIVATE);
	}

	private void initCells(int size) {
		mAllSlots = new CellInfo[size];
		CellInfo info = null;
		for (int i = 0; i < mAllSlots.length; i++) {
			// mOccupiedSlots[i] = false;
			mAllSlots[i] = new CellInfo();
			info = mAllSlots[i];
			info.occupied = false;
			info.x = i % 2;
			info.y = i / 2;
		}
	}

	private static final class Holder {
		public static final TileManager _INSTANCE = new TileManager();
	}

	public static TileManager getInstance() {
		return Holder._INSTANCE;
	}

	public boolean isPinned(ResolveInfo ri) {
		boolean ret = false;
		for (TileItemInfo tii : mTiles) {
			if (tii.isRepresent(ri)) {
				ret = true;
				break;
			}
		}
		return ret;
	}

	public Tile pinToStart(ComponentName cn, Integer drawableId,
			int type, CharSequence tileName) {
		TileItemInfo tii = CnToTii(cn, drawableId, type, tileName);
		return pinToStart(tii, type);
	}

	public Tile pinToStart(ResolveInfo ri) {

		// 如果到了最大的数目，则直接返回null;
		if (mTiles.size() >= MAX_ALLOW_TILE) {
			return null;
		}

		Integer id = WhiteIconMatcher.matchIcon(ri);
		TileItemInfo tii = RiToTii(ri, id);

		return pinToStart(tii, TileItemInfo.TYPE_INVALID);
	}

	private Tile pinToStart(TileItemInfo tii, int type) {
		// 先看右边，如果右边都占用，则左边不管如何不能再用了。
		// 或者是宽tile,则肯定要另一行了。
		if (mAllSlots[2 * mRowNumber + 1].occupied || tii.wideTile) {
			// 看看最后一行的右边位置是不是被占了，如果没占则新起一行
			mRowNumber++;
			tii.x = 0;
		} else if (!mAllSlots[2 * mRowNumber].occupied) {
			// 看看最后一行的右边位置是不是被占了，如果没占则直接用
			tii.x = 0;
		} else {
			tii.x = 1;
		}

		tii.y = mRowNumber;

		mTiles.add(tii);

		// 如果mOccupiedSlots不够用了，则新分配
		if (mAllSlots.length < (mTiles.size() - 1) * 2) {
			// CellInfo[] tmp = new CellInfo[mAllSlots.length +
			// DEFAULT_TILE_NUMBER];
			CellInfo[] tmp = mAllSlots;
			initCells(mAllSlots.length + DEFAULT_TILE_NUMBER);

			for (int i = 0; i < tmp.length; i++) {
				mAllSlots[i] = tmp[i];
			}
			// System.arraycopy(mAllSlots, 0, tmp, 0, mAllSlots.length);
			// mAllSlots = tmp;
		}

		mAllSlots[2 * tii.y + tii.x].occupied = true;
		mAllSlots[2 * tii.y + tii.x].tii = tii;

		// 如果是wide tile，两块都占用了。
		if (tii.wideTile) {
			mAllSlots[2 * tii.y + 1].occupied = true;
			mAllSlots[2 * tii.y + 1].tii = tii;
		}

		// CommonTile tile = new CommonTile(mContext, tii);
		Tile tile = null;
		if (type == TileItemInfo.TYPE_INVALID) {
			tii.type = TileItemInfo.TYPE_COMMON;
			tile = from(tii);
		} else {
			tile = from(tii, type);
		}
		
		saveTii(tii);
		
		tii.view = tile;
		return tile;
	}

	private TileItemInfo RiToTii(ResolveInfo ri, Integer id) {
		TileItemInfo tii = new TileItemInfo();
		tii.appDefaultActivity = ri.activityInfo.name;
		tii.appDefaultPackage = ri.activityInfo.packageName;
		tii.appDefaultIcon = Utils.getUUID(); // 我们分配一个新的文件名来存放图标文件
		if (id != null) {
			tii.appDefaultIconDrawable = mContext.getResources()
					.getDrawable(id);
		} else {
			tii.appDefaultIconDrawable = ri.loadIcon(mPM);
		}
		tii.enabled = true;
		tii.isDefault = true;
		tii.useDefault = true;
		tii.tileName = WhiteIconMatcher.shortenName(ri.loadLabel(mPM)
				.toString());
		if (mAM.isSystemApp(ri)) {
			tii.flags |= TileItemInfo.FLAG_SYSTEM_APP;
		}

		// wide tile
		int type = WhiteIconMatcher.getTileType(ri.activityInfo.name);
		if (type == TileItemInfo.TYPE_GALLERY) {
			tii.wideTile = true;
		}

		tii.type = type;

		return tii;
	}

	private TileItemInfo CnToTii(ComponentName cn, Integer drawableId,
			int type, CharSequence tileName) {
		TileItemInfo tii = new TileItemInfo();
		tii.appDefaultActivity = cn.getClassName();
		tii.appDefaultPackage = cn.getPackageName();
		tii.appDefaultIcon = Utils.getUUID(); // 我们分配一个新的文件名来存放图标文件
		if (drawableId != null) {
			tii.appDefaultIconDrawable = mContext.getResources().getDrawable(
					drawableId);
		}
		tii.enabled = true;
		tii.isDefault = true;
		tii.useDefault = true;
		try {
			if (TextUtils.isEmpty(tileName)) {
				tii.tileName = WhiteIconMatcher.shortenName(mPM
						.getApplicationLabel(
								mPM.getApplicationInfo(cn.getPackageName(), 0))
						.toString());
			} else {
				tii.tileName = tileName.toString();
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		if (mAM.isSystemApp(cn.getPackageName())) {
			tii.flags |= TileItemInfo.FLAG_SYSTEM_APP;
		}

		// wide tile
		if (type == TileItemInfo.TYPE_GALLERY) {
			tii.wideTile = true;
		}

		tii.type = type;

		return tii;
	}

	private int saveTii(TileItemInfo t) {
		final TileItemInfo tii = t;
		final ContentValues cv = new ContentValues();
		cv.put(TileInfo._APP_ACTIVITY, tii.appActivity);
		cv.put(TileInfo._APP_DEFAULT_ACTIVITY, tii.appDefaultActivity);
		cv.put(TileInfo._APP_DEFAULT_ICON, tii.appDefaultIcon);
		cv.put(TileInfo._APP_DEFAULT_PACKAGE, tii.appDefaultPackage);
		cv.put(TileInfo._APP_ICON, tii.appIcon);
		cv.put(TileInfo._APP_NAME, tii.appName);
		cv.put(TileInfo._APP_PACKAGE, tii.appPackage);
		cv.put(TileInfo._DEFAULT, tii.isDefault ? 1 : 0);
		cv.put(TileInfo._ENABLED, tii.enabled ? 1 : 0);

		cv.put(TileInfo._FLAGS, tii.flags);
		cv.put(TileInfo._LAUNCH_TIMES, tii.launchTimes);
		cv.put(TileInfo._TILE_NAME, tii.tileName);
		cv.put(TileInfo._USE_DEFAULT, tii.useDefault ? 1 : 0);
		cv.put(TileInfo._TYPE, tii.type);
		cv.put(TileInfo._WIDE_TILE, tii.wideTile ? 1 : 0);
		cv.put(TileInfo._X, tii.x);
		cv.put(TileInfo._Y, tii.y);

		new Thread("saveTii_Thread") {
			public void run() {
				if (tii.appDefaultIconDrawable != null) {
					if (TextUtils.isEmpty(tii.appDefaultIcon)) {
						tii.appDefaultIcon = Utils.getUUID();
						cv.put(TileInfo._APP_DEFAULT_ICON, tii.appDefaultIcon);

					}

					saveDrawable(tii.appDefaultIconDrawable, tii.appDefaultIcon);
				}

				if (tii.appIconDrawable != null) {
					if (TextUtils.isEmpty(tii.appIcon)) {
						tii.appIcon = Utils.getUUID();
						cv.put(TileInfo._APP_ICON, tii.appIcon);
					}
					saveDrawable(tii.appIconDrawable, tii.appIcon);
				}

				Uri uri = mCR.insert(WP7Launcher.TileInfo.CONTENT_URI, cv);
				tii._id = Integer.parseInt(uri.getPathSegments().get(1));
			}
		}.start();

		return 1;
	}

	public void loadTiles(Cursor c) {
		/*
		 * ContentResolver cr = mContext.getContentResolver(); Cursor c =
		 * cr.query(WP7Launcher.TileInfo.CONTENT_URI, null, null, null,
		 * WP7Launcher.TileInfo.DEFAULT_SORT_ORDER);
		 */
		if (c == null || c.getCount() < 1) {
			AXLog.d(TAG, "loadTiles cursor is null or count = 0");
		}

		mTiles.clear();

		try {
			int aa = c.getColumnIndexOrThrow(TileInfo._APP_ACTIVITY);
			int ada = c.getColumnIndexOrThrow(TileInfo._APP_DEFAULT_ACTIVITY);
			int adi = c.getColumnIndexOrThrow(TileInfo._APP_DEFAULT_ICON);
			int adp = c.getColumnIndexOrThrow(TileInfo._APP_DEFAULT_PACKAGE);
			int ai = c.getColumnIndexOrThrow(TileInfo._APP_ICON);
			int an = c.getColumnIndexOrThrow(TileInfo._APP_NAME);
			int ap = c.getColumnIndexOrThrow(TileInfo._APP_PACKAGE);
			int d = c.getColumnIndexOrThrow(TileInfo._DEFAULT);
			int e = c.getColumnIndexOrThrow(TileInfo._ENABLED);
			int f = c.getColumnIndexOrThrow(TileInfo._FLAGS);
			int lt = c.getColumnIndexOrThrow(TileInfo._LAUNCH_TIMES);
			int tn = c.getColumnIndexOrThrow(TileInfo._TILE_NAME);
			int t = c.getColumnIndexOrThrow(TileInfo._TYPE);
			int ud = c.getColumnIndexOrThrow(TileInfo._USE_DEFAULT);
			int wt = c.getColumnIndexOrThrow(TileInfo._WIDE_TILE);
			int x = c.getColumnIndexOrThrow(TileInfo._X);
			int y = c.getColumnIndexOrThrow(TileInfo._Y);
			int id = c.getColumnIndexOrThrow(TileInfo._ID);

			TileItemInfo tii = null;

			while (c.moveToNext()) {
				tii = new TileItemInfo();

				tii.appActivity = c.getString(aa);
				tii.appDefaultActivity = c.getString(ada);
				tii.appDefaultIcon = c.getString(adi);
				tii.appDefaultPackage = c.getString(adp);
				tii.appIcon = c.getString(ai);
				tii.appName = c.getString(an);
				tii.appPackage = c.getString(ap);
				tii.isDefault = c.getInt(d) != 0;
				tii.enabled = c.getInt(e) != 0;
				tii.flags = c.getInt(f);
				tii.launchTimes = c.getInt(lt);
				tii.tileName = c.getString(tn);
				tii.type = c.getInt(t);
				tii.useDefault = c.getInt(ud) != 0;
				tii.wideTile = c.getInt(wt) != 0;
				tii.x = c.getInt(x);
				tii.y = c.getInt(y);
				tii._id = c.getInt(id);

				if (!TextUtils.isEmpty(tii.appDefaultIcon)) {
					tii.appDefaultIconDrawable = getDrawable(tii.appDefaultIcon);
				}

				if (!TextUtils.isEmpty(tii.appIcon)) {
					tii.appIconDrawable = getDrawable(tii.appIcon);
				}

				mTiles.add(tii);
			}

		} finally {
			c.close();
			c = null;
		}

		// 更新
		updateOccupation();
	}

	// 更新占用情况
	private void updateOccupation() {
		int maxY = 0;
		// 最多就是一列
		if (mAllSlots.length < mTiles.size() * 2) {
			// mAllSlots = new CellInfo[mTiles.size() * 2];
			initCells(mTiles.size() * 2);
		}

		for (int i = 0; i < mAllSlots.length; i++) {
			mAllSlots[i].occupied = false;
			mAllSlots[i].x = i % 2;
			mAllSlots[i].y = i / 2;
			mAllSlots[i].tii = null;
		}

		for (TileItemInfo tii : mTiles) {
			if (maxY < tii.y) {
				maxY = tii.y;
			}
			// 只要有Tile就是被占用了
			mAllSlots[tii.y * 2 + tii.x].occupied = true;
			mAllSlots[tii.y * 2 + tii.x].tii = tii;
			// 只果是wide tile，一行都被占用
			// wide tile的(x,y)总是(0,y)
			if (tii.wideTile) {
				mAllSlots[tii.y * 2 + 1].occupied = true;
				mAllSlots[tii.y * 2 + 1].tii = tii;
			}
		}

		// 更新最大的行
		mRowNumber = maxY;
	}

	private void saveDrawable(Drawable drawable, String fileName) {
		Bitmap bitmap = Bitmap
				.createBitmap(
						drawable.getIntrinsicWidth(),
						drawable.getIntrinsicHeight(),
						drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
								: Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);

		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
				drawable.getIntrinsicHeight());

		drawable.draw(canvas);

		OutputStream os = null;
		// String file = ICONS + "/" + fileName;
		mContext.deleteFile(fileName);
		try {
			os = mContext.openFileOutput(fileName, Context.MODE_PRIVATE);
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
			os.flush();
			os.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void deleteFile(String fileName) {
		// String file = ICONS + "/" + fileName;
		mContext.deleteFile(fileName);
	}

	private Drawable getDrawable(String fileName) {
		FileInputStream fis = null;
		// String file = ICONS + "/" + fileName;
		try {
			fis = mContext.openFileInput(fileName);
			byte[] data = new byte[fis.available()];
			fis.read(data);
			Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
			Drawable d = new BitmapDrawable(mContext.getResources(), bmp);
			// bmp.recycle();
			return d;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Vector<TileItemInfo> getTiles() {
		return mTiles;
	}

	public Tile from(TileItemInfo tii) {
		Integer type = WhiteIconMatcher.getTileType(tii
				.getCurrentActivity());
		if(type == TileItemInfo.TYPE_INVALID || type == TileItemInfo.TYPE_COMMON) {
			type = tii.type;
		}
		return from(tii, type);
	}

	private Tile from(TileItemInfo tii, int type) {
		if (type != TileItemInfo.TYPE_COMMON && !tii.isFixTile()) {
			// 如果是特定的类别，但是却没有设置，是我们不能编辑的，并且要更新到数据库中
			tii.flags |= TileItemInfo.FLAG_FIX_TILE;
			// 更新到数据库
			updateTile(tii, false);
		}
		Tile tile = null;
		if (type == TileItemInfo.TYPE_COMMON || type == TileItemInfo.TYPE_INVALID) {
			tile = new CommonTile(mContext, tii);
		} else if (type == TileItemInfo.TYPE_PEOPLE) {
			tile = new ContactsTile(mContext, tii);
		} else if (type == TileItemInfo.TYPE_MESSAGE) {
			tile = new MessageTile(mContext, MessageManager.getInstance(), tii);
		} else if (type == TileItemInfo.TYPE_PHONE) {
			tile = new CallTile(mContext, CallManager.getInstance(), tii);
		} else if (type == TileItemInfo.TYPE_GALLERY) {
			tile = new GalleryTile(mContext, tii);
		}

		tii.view = tile;
		return tile;
	}

	public int getMaxY() {
		return mRowNumber;
	}

	public void removeTile(TileItemInfo tii) {
		if (tii == null) {
			throw new NullPointerException("removeTile's param is null.");
		}
		final TileItemInfo ft = tii;
		// 从mTiles中移除
		mTiles.remove(tii);

		new Thread("deleteTileThread") {
			public void run() {
				deleteDatabase(ft);
			}
		}.start();

		// 查检所有的Tile是不是要往上移动。
		boolean needToMoveUp = false;
		if (tii.wideTile) {
			// 如果是一个宽的tile被移除了，则肯定要向上移动。
			needToMoveUp = true;
		} else if (!mAllSlots[tii.y * 2 + (1 - tii.x)].occupied) {
			// 如果被与移除的Tile同一行没有Tile，则要向上移动，
			needToMoveUp = true;
		}

		if (needToMoveUp) {
			// mOccupiedSlots[tii.y * 2] = false;
			// mOccupiedSlots[tii.y * 2 + 1] = false;
			for (TileItemInfo t : mTiles) {
				if (t.y > tii.y) {
					t.y--;
				}
			}
			// 更新数据库
			updateDatabase(true);
		}
		// 更新点位
		updateOccupation();

	}

	private void updateDatabase(boolean needToUpdateIcon) {
		final Vector<TileItemInfo> ls = mTiles;
		final boolean ui = needToUpdateIcon;
		new Thread("updateTileDatabaseThread") {
			public void run() {
				for (TileItemInfo tii : ls) {
					updateTile(tii, ui);
				}
			}
		}.start();
	}

	/**
	 * 同步更新数据库
	 * 
	 * @param needToUpdateIcon
	 */
	public void updateDatabaseSync(boolean needToUpdateIcon) {
		for (TileItemInfo tii : mTiles) {
			updateTile(tii, needToUpdateIcon);
		}
	}

	public int updateTile(TileItemInfo tii, boolean updateIcon) {
		ContentValues cv = new ContentValues();
		cv.put(TileInfo._ID, tii._id);
		cv.put(TileInfo._APP_ACTIVITY, tii.appActivity);
		cv.put(TileInfo._APP_DEFAULT_ACTIVITY, tii.appDefaultActivity);
		cv.put(TileInfo._APP_DEFAULT_ICON, tii.appDefaultIcon);
		cv.put(TileInfo._APP_DEFAULT_PACKAGE, tii.appDefaultPackage);
		cv.put(TileInfo._APP_ICON, tii.appIcon);
		cv.put(TileInfo._APP_NAME, tii.appName);
		cv.put(TileInfo._APP_PACKAGE, tii.appPackage);
		cv.put(TileInfo._DEFAULT, tii.isDefault ? 1 : 0);
		cv.put(TileInfo._ENABLED, tii.enabled ? 1 : 0);

		cv.put(TileInfo._FLAGS, tii.flags);
		cv.put(TileInfo._LAUNCH_TIMES, tii.launchTimes);
		cv.put(TileInfo._TILE_NAME, tii.tileName);
		cv.put(TileInfo._USE_DEFAULT, tii.useDefault ? 1 : 0);
		cv.put(TileInfo._TYPE, tii.type);
		cv.put(TileInfo._WIDE_TILE, tii.wideTile ? 1 : 0);
		cv.put(TileInfo._X, tii.x);
		cv.put(TileInfo._Y, tii.y);
		if (updateIcon) {
			if (tii.appDefaultIconDrawable != null) {
				if (TextUtils.isEmpty(tii.appDefaultIcon)) {
					tii.appDefaultIcon = Utils.getUUID();
					cv.put(TileInfo._APP_DEFAULT_ICON, tii.appDefaultIcon);

				} else {
					mContext.deleteFile(tii.appDefaultIcon);
				}

				saveDrawable(tii.appDefaultIconDrawable, tii.appDefaultIcon);
			}

			if (tii.appIconDrawable != null) {
				if (TextUtils.isEmpty(tii.appIcon)) {
					tii.appIcon = Utils.getUUID();
					cv.put(TileInfo._APP_ICON, tii.appIcon);
				} else {
					mContext.deleteFile(tii.appIcon);
				}
				saveDrawable(tii.appIconDrawable, tii.appIcon);
			}

		}
		return mCR.update(WP7Launcher.TileInfo.CONTENT_URI, cv, "_id = "
				+ tii._id, null);
	}

	private int deleteDatabase(TileItemInfo tii) {
		return mCR.delete(WP7Launcher.TileInfo.CONTENT_URI, "_id = " + tii._id,
				null);
	}

	public CellInfo getCellInfo(int x, int y, int topMargin, int leftMargin,
			int tileSpace, int tileW, int tileH, boolean wide) {
		CellInfo info = null;
		int yPos = topMargin + tileH / 2;
		int i = 0;

		// 如果是到了最头上了或是第1行的上半部分，则直接返回头
		if ((y < topMargin + tileH / 2) || mTiles.size() <= 1) {
			// 看看在块，左还是右，如果是宽的，肯定在左边
			if (x > (leftMargin + tileW + tileSpace / 2)) {
				HEAD.x = 1;
			} else {
				HEAD.x = 0;
			}
			info = HEAD;
		} else {

			for (i = 0; i < mAllSlots.length; i += 2) {

				if (yPos > y) { // Y 找到了，下面找x
					if (x > (leftMargin + tileW + tileSpace / 2)) {
						// 是右边的
						info = mAllSlots[i + 1];
					} else {
						info = mAllSlots[i];
					}
					break;
				}
				yPos += (tileH + tileSpace);
			}

			if (info == null && i >= mAllSlots.length) {
				if (x > (leftMargin + tileW + tileSpace / 2)) {
					REAR.x = 1;
				} else {
					REAR.x = 0;
				}
				info = REAR;
			}
		}

		// 如果是宽的，则一定要占用一行
		if (wide) {
			info.x = 0;
		}

		return info;
	}

	/**
	 * 
	 * @param tii
	 *            更新后的tii
	 * @param oldX
	 *            老的x
	 * @param oldY
	 */
	public void updateTii(TileItemInfo tii, int oldX, int oldY) {
		CellInfo info = mAllSlots[tii.y * 2 + tii.x];
		int curY = tii.y;
		int curX = tii.x;
		boolean needToMoveUp = false; // 是不是上移。
		boolean needToMoveDown = false; // 是不是要下移。
		if (info.occupied) {
			// 如果要插入的位置被占用，则要挪位置
			// 或者是它是一个宽的tile,而它的邻位被占用了。
			needToMoveUp = true;
		} else if (mAllSlots[tii.y * 2 + (1 - tii.x)].occupied && tii.wideTile) {
			needToMoveUp = true;
		}

		if (!mAllSlots[oldY * 2 + (1 - oldX)].occupied || tii.wideTile) {
			// 如果被移走的tile邻位是空的或者是一个宽tile，则要往下移。
			needToMoveDown = true;
		}

		for (TileItemInfo t : mTiles) {
			// 如果不是自己，并且y不比自己小，则要上移一行。
			if (needToMoveUp) {
				if (!tii.equals(t) && t.y >= curY) {
					t.y++;
				}
			}

			if (needToMoveDown) {
				if (t.y > oldY) {
					t.y--;
				}
			}
		}

		updateOccupation();
		updateDatabase(false);
	}

	public void updateAllTilesName(List<AppItem> list) {
		if (list == null || list.size() < 1) {
			AXLog.w(TAG, "updateAllTilesName, list is null or 0 size");
			return;
		}
		HashMap<String, String> map = new HashMap<String, String>();
		for (AppItem item : list) {
			map.put(item.mRi.activityInfo.packageName, item.mRi.loadLabel(mPM)
					.toString());
			AXLog.w(TAG, "packageName = " + item.mRi.activityInfo.packageName
					+ ", Label = " + item.mRi.loadLabel(mPM));
		}
		for (TileItemInfo t : mTiles) {
			AXLog.w(TAG,
					"1: packageName = "
							+ (t.useDefault ? map.get(t.appDefaultPackage)
									: map.get(t.appPackage)) + "t.tileName = "
							+ t.tileName);
			t.tileName = t.useDefault ? map.get(t.appDefaultPackage) : map
					.get(t.appPackage);
			AXLog.w(TAG,
					"2 :packageName = "
							+ (t.useDefault ? map.get(t.appDefaultPackage)
									: map.get(t.appPackage)) + "t.tileName = "
							+ t.tileName);
		}
	}

	/**
	 * 从TileManager中删除一个项，如果没有则返回 false, 如果有，返回true;
	 * 
	 * @param packageName
	 * @return
	 */
	public boolean removeTile(String packageName) {
		if (TextUtils.isEmpty(packageName)) {
			return false;
		}
		TileItemInfo tiiDel = null;
		int delCount = 0;
		for (TileItemInfo tii : mTiles) {
			if (tii.useDefault) {
				if (packageName.equalsIgnoreCase(tii.appDefaultPackage)) {
					tiiDel = tii;
					break;
				}
			} else {
				if (packageName.equalsIgnoreCase(tii.appPackage)) {
					tiiDel = tii;
					break;
				}
			}
		}

		if (tiiDel != null) {
			delCount = deleteDatabase(tiiDel);
		}

		return delCount > 0;
	}
}
