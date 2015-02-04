package com.axen.launcher.wp7.ui;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import com.axen.launcher.app.AppManager;
import com.axen.launcher.app.AppManager.AppItem;
import com.axen.launcher.app.TileItemInfo;
import com.axen.launcher.app.TileManager;
import com.axen.launcher.app.WhiteIconMatcher;
import com.axen.launcher.provider.WP7Launcher;
import com.axen.launcher.provider.WP7Launcher.ScreenShot;
import com.axen.launcher.wp7.main.R;
import com.axen.launcher.wp7.main.WP7App;
import com.axen.launcher.wp7.main.WP7Configuration;
import com.axen.launcher.wp7.ui.anim.AnimUtil;
import com.axen.launcher.wp7.ui.anim.Rotate3DAnimation;
import com.axen.launcher.wp7.ui.apputil.ClassifyApp;
import com.axen.launcher.wp7.ui.apputil.ClassifyFactory;
import com.axen.launcher.wp7.ui.apputil.ClassifyApp.AppClass;
import com.axen.launcher.wp7.ui.size.Size;
import com.axen.launcher.wp7.ui.statusbar.StatusbarController;
import com.axen.launcher.wp7.ui.widget.AppClassView;
import com.axen.launcher.wp7.ui.widget.AppItemView;
import com.axen.launcher.wp7.ui.widget.AppMenuDialog;
import com.axen.launcher.wp7.ui.widget.Tile;
import com.axen.launcher.wp7.ui.widget.WP7ProgessView;
import com.axen.utils.AXLog;
import com.axen.utils.ResourceUtil;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.LayoutInflater.Factory;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class LaucherActivity extends Activity implements OnClickListener,
		OnLongClickListener, DragController.DragListener,
		Animation.AnimationListener {

	private static final String TAG = "LaucherActivity";
	private WP7Configuration mConf = WP7Configuration.getInstance();
	private Size size = Size.get();
	private WorkSpace mWorkSpace = null;
	private DragLayer mDragLayer = null;
	private TileSpace mChildTile = null;
	private ButtonSpace mChildButton = null;
	private AppSpace mChildApp = null;
	private AppClassSpace mChildAppClass = null;
	private AppManager mAppManager = null;
	private TileManager mTileManager = null;
	private WindowManager mWindowManager = null;
	private StatusbarController mReceiver;
	private ContentResolver mResolver = null;

	private ClassifyApp mAppClasses = null;

	// private TextView mStatusBarTimer = null;

	private DragController mDragController = null;
	private Dialog mCustomDialog = null;
	private Dialog mOptionMenuDialog = null;
	private AppMenuDialog mAppContextMenu = null;

	private ResourceUtil mru = mConf.getRU();

	private static final int DIALOG_PROGRESS = 1; //
	private static final int DIALOG_APP_MENU = 2;

	private static final int REQUEST_CODE_APPCLASS = 0x1000; // 打开app item class
																// activity的请求代码

	private static final int LOADING_DELAY_MAX = 200; // if we waiting to long
														// to load, show
														// progress dialog.

	private static final int ASYNC_QUERY_TOKEN_NORMAL = 1;
	private static final int ASYNC_QUERY_TOKEN_UPDATE = 2;
	private static final int ASYNC_QUERY_TOKEN_FIRSTRUN = 3;

	private static final int ASYNC_DELETE_INIT_TYPE = 1;
	private static final int ASYNC_DELETE_RUNNING_TYPE = 2;
	private static final int ASYNC_SHOT_INSERT_COMPLETE = 3;
	private static final int ASYNC_SHOT_INSERT_FIRST_RUN = 4;

	// 第一次默认要插入的应用个数
	private static final int DEFAULT_PIN_NUMBER = 5;

	private static final String MARKET_URI = "market://details?id=";

	private static int sHideStatusBarTime = 3000; // 2s

	private static int sExitDragviewTime = 10000; // 10s

	private String mCurrentLanguage = "";

	private static final int MENU_SYSTEM_SETTINGS = Menu.FIRST + 1;
	private static final int MENU_LAUNCHER_SETTINGS = Menu.FIRST + 2;

	private Rotate3DAnimation mTileHideAnim = null;
	private Rotate3DAnimation mTileShowAnim = null;
	private Rotate3DAnimation mAppHideAnim = null;
	private Rotate3DAnimation mAppShowAnim = null;
	private Runnable mOnAnimationEnd = null;
	private static final int ANIM_DURATION = 150;
	private boolean mNeedShowAnim = false;

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
		}

	};

	private AsyncHandler mQueryHandler = null;

	private Runnable mShowProgressDialog = new Runnable() {
		public void run() {
			if (mCustomDialog == null) {
				mCustomDialog = new Dialog(LaucherActivity.this,
						R.style.windows_phone_dialog);
				mCustomDialog.getWindow().getDecorView().setBackgroundColor(mConf.getBackgroundColor());
				LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, 50);
				mCustomDialog.setContentView(new WP7ProgessView(
						LaucherActivity.this, mConf.getAccentColor(), mConf.getBackgroundColor()), lp);
				mCustomDialog.setCancelable(false);
				mCustomDialog.show();
			}
		}
	};

	private Runnable mHideStatusBar = new Runnable() {

		@Override
		public void run() {
			mDragLayer.hideItems();
		}

	};

	private Runnable mExitDragMode = new Runnable() {

		@Override
		public void run() {
			if (mChildTile.getState() == TileSpace.STATE_EDIT) {
				View dragView = mChildTile.getDragView();
				if (dragView != null) {
					dragView.setVisibility(View.VISIBLE);
					dragView.clearAnimation();
					mDragController.stopDrag();
				}
				mChildButton.setChildVisibility(R.id.id_button_arrow,
						View.VISIBLE);
				mChildTile.setState(TileSpace.STATE_NORMAL, true, null, 0);
			}
		}

	};

	private BroadcastReceiver mScreenOffReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
				// 我们要开始截图了，但是截图之前要删除上一次同类型的截图
				mQueryHandler
						.startDelete(
								ASYNC_DELETE_RUNNING_TYPE,
								null,
								WP7Launcher.ScreenShot.CONTENT_URI,
								WP7Launcher.ScreenShot._SHOT_TYPE + " = ?",
								new String[] { Integer
										.toString(WP7Launcher.ScreenShot.SHOT_TYPE_RUNNING) });
			} else if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {

				// 如果是因为屏幕亮面获得焦点，则不能播放动画
				mNeedShowAnim = false;
			}
		}

	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 先得到Statusbar 高度
		// getStatusBarHeight();
		setContentView(R.layout.main);

		mAppManager = AppManager.getInstance();
		mTileManager = TileManager.getInstance();

		mWorkSpace = (WorkSpace) findViewById(R.id.id_workspace);
		mDragLayer = (DragLayer) findViewById(R.id.id_drag_layer);
		// mStatusBarTimer = (TextView) findViewById(R.id.id_status_bar_time);
		mDragController = new DragController(this);
		mDragLayer.setDragController(mDragController);

		mDragLayer.setStatusBarClickListener(this);

		mWorkSpace.setOnClickListener(this);
		mWorkSpace.setOnLongClickListener(this);
		mWorkSpace.setDragController(mDragController);
		mWorkSpace.setDragLayer(mDragLayer);

		mAppClasses = ClassifyFactory.getDefault(getApplicationContext());

		mChildTile = mWorkSpace.mChildTile;
		mChildButton = mWorkSpace.mChildButton;
		mChildApp = mWorkSpace.mChildApp;
		mChildAppClass = mWorkSpace.mChildAppClass;
		mChildApp.setAppClassSpace(mChildAppClass);

		// mChildAppClass.setAppClasses(mAppClasses);
		// mChildApp.setAppClasses(mAppClasses);
		mWindowManager = getWindowManager();

		mDragController.setWindowManager(mWindowManager);
		mDragController.addDragListener(mChildTile);

		mResolver = getContentResolver();

		mQueryHandler = new AsyncHandler(getContentResolver());
		// 开始加载自信。
		mHandler.postDelayed(mShowProgressDialog, LOADING_DELAY_MAX);

		mReceiver = new StatusbarController(this, mDragLayer);
		registerReceiver(mReceiver, StatusbarController.getIntentFilter());

		/**
		 * mQueryHandler.startQuery(ASYNC_QUERY_TOKEN_NORMAL, null,
		 * WP7Launcher.TileInfo.CONTENT_URI, null, null, null,
		 * WP7Launcher.TileInfo.DEFAULT_SORT_ORDER);
		 */

		/**
		 * 我们要查询是不是第一次启动，如果是，则要向主屏添加5个应用。 在这里开始查询，然后在 onQueryComplete里看到结果
		 */

		mQueryHandler.startQuery(ASYNC_QUERY_TOKEN_FIRSTRUN, null,
				WP7Launcher.ScreenShot.CONTENT_URI, null,
				WP7Launcher.ScreenShot._SHOT_TYPE + " = ?",
				new String[] { Integer
						.toString(WP7Launcher.ScreenShot.SHOT_TYPE_INVALID) },
				WP7Launcher.ScreenShot.DEFAULT_SORT_ORDER);

		mCurrentLanguage = Locale.getDefault().getLanguage();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_PACKAGE_ADDED);
		filter.addAction(Intent.ACTION_PACKAGE_REMOVED);

		filter.addDataScheme("package");
		registerReceiver(mPackageReceiver, filter);

		filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		filter.addAction(Intent.ACTION_SCREEN_ON);
		registerReceiver(mScreenOffReceiver, filter);

		mTileHideAnim = new Rotate3DAnimation(0, 0, 0, -90, 0, 0,
				Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_SELF,
				0.5f);
		mTileHideAnim.setDuration(ANIM_DURATION);
		mTileHideAnim.setFillAfter(true);

		mTileShowAnim = new Rotate3DAnimation(0, 0, -90, 0, 0, 0,
				Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_SELF,
				0.5f);
		mTileShowAnim.setDuration(ANIM_DURATION);
		// mTileShowAnim.setStartOffset(100);
		mTileShowAnim.setFillAfter(true);
		mTileShowAnim.setFillBefore(true);

		mAppHideAnim = new Rotate3DAnimation(0, 0, 0, -90, 0, 5,
				Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_SELF,
				0.5f);
		mAppHideAnim.setDuration(ANIM_DURATION);
		mAppHideAnim.setFillAfter(true);

		mAppShowAnim = new Rotate3DAnimation(0, 0, -90, 0, 5, 0,
				Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_SELF,
				0.5f);
		mAppShowAnim.setDuration(ANIM_DURATION);
		// mAppShowAnim.setStartOffset(100);
		mAppShowAnim.setFillAfter(true);
		mAppShowAnim.setFillBefore(true);
	}

	@Override
	public boolean onLongClick(View v) {
		if (v instanceof Tile) {
			if (mDragController.isDragging()) {
				return true;
			} else {
				int[] loc = new int[2];
				mDragController.startDrag(v);
				// 隐藏箭头按钮
				mChildButton
						.setChildVisibility(R.id.id_button_arrow, View.GONE);
				AXLog.d(TAG, "onLongClick Tile");
				v.getLocationOnScreen(loc);
				mChildTile.setState(TileSpace.STATE_EDIT, true, (Tile) v,
						loc[1]);
				mDragController.setOnClickListener(this);
				mHandler.postDelayed(mExitDragMode, sExitDragviewTime);
			}
		} else if (v instanceof AppItemView) {

			int[] vLoc = new int[2];
			int itemH = v.getHeight();
			int menuH = 0;
			int padding = size.getApp_icon_space(); //mru.getPexil(R.dimen.app_icon_space);
			Window win = null;
			boolean dropAnim = false;
			dismissAppContextMenu();

			mAppContextMenu = createAppContextMenu(v);
			mAppContextMenu.setCanceledOnTouchOutside(true);
			win = mAppContextMenu.getWindow();
			WindowManager.LayoutParams params = win.getAttributes();
			v.getLocationOnScreen(vLoc);

			if (mAppManager.isSystemApp((ResolveInfo) v.getTag())) {
				menuH = size.getApp_context_menu_one_h(); //mru.getPexil(R.dimen.app_context_menu_one_h);
			} else {
				menuH = size.getApp_context_menu_three_h(); //mru.getPexil(R.dimen.app_context_menu_three_h);
			}

			params.width = LayoutParams.MATCH_PARENT;
			params.height = LayoutParams.WRAP_CONTENT;
			// 下面计算Menu显示的位置
			if (vLoc[1] + itemH + padding + menuH > mConf.getUsableHeight()) {
				// 如果在下面显示进出屏幕，则要显示在上面
				params.y = vLoc[1] - menuH - padding / 3;
			} else {
				params.y = vLoc[1] + itemH + padding;
				dropAnim = true;
			}

			// 因为Dialog是以屏幕中心为(0,0),所以要关掉屏幕高度的一半。
			params.y -= (mConf.getScreenH() - menuH) / 2;

			win.setAttributes(params);
			if (dropAnim) {
				win.setWindowAnimations(R.style.appContextMenuAnimDrop);
			} else {
				win.setWindowAnimations(R.style.appContextMenuAnimFloat);
			}

			// 在这里添加动画
			mAppContextMenu.show();
			if (mTileManager.isPinned((ResolveInfo) v.getTag())) {
				mAppContextMenu.setDisable(R.id.app_menu_pin_to_start, false);
			}

			if (mAppManager.isSystemApp((ResolveInfo) v.getTag())) {
				mAppContextMenu.setItemVisibility(
						R.id.app_menu_rate_and_review, View.GONE);
				mAppContextMenu.setItemVisibility(R.id.app_menu_uninstall,
						View.GONE);
			}

			// 延时，让变化更美观
			final View fv = v;
			mHandler.postDelayed(new Runnable() {
				public void run() {
					mChildApp.setMode(AppSpace.MODE_MENU, fv, true);
				}
			}, 300);

			mAppContextMenu.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					int mode = mWorkSpace.getAppMode();
					if (mode == AppSpace.MODE_NORMAL) {
						mChildApp.setMode(AppSpace.MODE_NORMAL, null, true);
					} else if (mode == AppSpace.MODE_SEARCH) {
						mChildApp.setMode(AppSpace.MODE_SEARCH, null, true);
					}
				}

			});
			AXLog.d(TAG, "onLongClick on App");

		} else {
			AXLog.d(TAG, "Long clicked on unknow view." + v);
		}
		return true;
	}

	@Override
	public void onClick(View v) {
		final View currentView = v;
		if (v.getId() == R.id.id_button_arrow) {
			AXLog.d(TAG, "onClick arrow");
			if (mWorkSpace.getCurrentScreen() == WorkSpace.SCREEN_APP) {
				mWorkSpace.scrollToScreen(WorkSpace.SCREEN_TILE);
				mWorkSpace.setCurrentScreen(WorkSpace.SCREEN_TILE);
			} else if (mWorkSpace.getCurrentScreen() == WorkSpace.SCREEN_TILE) {
				mWorkSpace.scrollToScreen(WorkSpace.SCREEN_APP);
				mWorkSpace.setCurrentScreen(WorkSpace.SCREEN_APP);
			}

		} else if (v.getId() == R.id.id_button_search) {
			AXLog.d(TAG, "onClick search");
			mWorkSpace.setAppSpaceMode(AppSpace.MODE_SEARCH);
		} else if (v.getId() == R.id.app_menu_pin_to_start) {

			if (mWorkSpace.getAppMode() == AppSpace.MODE_SEARCH) {
				mWorkSpace.setAppSpaceMode(AppSpace.MODE_NORMAL);
			}

			dismissAppContextMenu();
			Tile tile = mTileManager.pinToStart((ResolveInfo) v.getTag());
			if (tile != null) {
				mChildTile.addView(tile);
				mChildTile.requestLayout();
				// 还要翻到Tile页，然后滚动到末尾。
				mChildTile.scrollToBottom();

				mWorkSpace.scrollToScreen(WorkSpace.SCREEN_TILE);
			} else {
				AXLog.w(TAG, "created tile is null");
			}
			// mWorkSpace.scrollTo(mWorkSpace.getScrollX(), )

		} else if (v.getId() == R.id.app_menu_rate_and_review) {
			ResolveInfo ri = (ResolveInfo) v.getTag();
			Intent intentDownload = new Intent(Intent.ACTION_VIEW,
					Uri.parse(MARKET_URI + ri.activityInfo.packageName));
			dismissAppContextMenu();
			startActivity(intentDownload);
		} else if (v.getId() == R.id.app_menu_uninstall) {
			ResolveInfo ri = (ResolveInfo) v.getTag();
			Intent intent = new Intent(Intent.ACTION_DELETE, Uri.fromParts(
					"package", ri.activityInfo.packageName, null));
			dismissAppContextMenu();
			startActivity(intent);
		} else if (v.getId() == R.id.id_dragview_edit) {
			Tile tile = mChildTile.getDragView();
			if(tile == null) {
				return;
			}
			TileItemInfo tii = tile.getTii();
			mHandler.removeCallbacks(mExitDragMode);
			mExitDragMode.run();
			mTileManager.setEditTile(tii);
			Intent edit = new Intent();
			edit.setClass(this, TileEditActivity.class);
			startActivity(edit);
		}
		else if (v.getId() == R.id.id_dragview_unpin) {
			Tile t = mChildTile.getDragView();
			TileItemInfo tii = t.getTii();
			mTileManager.removeTile(tii);
			mDragController.stopDrag();
			mChildTile.removeView(t);
			mChildTile.setState(TileSpace.STATE_NORMAL, true, null, 0);
			mChildButton.setChildVisibility(R.id.id_button_arrow, View.VISIBLE);
			// requestLayout();

		} else if (v.getId() == R.id.id_status_bar) {
			mDragLayer.clearItemsAnim();
			mHandler.removeCallbacks(mHideStatusBar);
			if (mDragLayer.itemsShown()) {
				mDragLayer.hideItems();
			} else {
				mDragLayer.showItems();
				mHandler.postDelayed(mHideStatusBar, sHideStatusBarTime);
			}
		} else if (v instanceof AppItemView) {
			AXLog.d(TAG, "onClick button");
			mOnAnimationEnd = new Runnable() {
				public void run() {
					ResolveInfo ri = (ResolveInfo) currentView.getTag();
					Intent intent = new Intent(Intent.ACTION_MAIN);
					intent.addCategory(Intent.CATEGORY_LAUNCHER);
					intent.setClassName(ri.activityInfo.packageName,
							ri.activityInfo.name);
					startActivitySafely(intent, null);
					mHandler.postDelayed(new Runnable() {
						public void run() {
							mChildApp.clearChildAnimation();
						}
					}, 500);
				}
			};

			mChildApp.startEndAnim(mAppHideAnim, currentView, this, 0);

		} else if (v instanceof Tile) {
			if (mChildTile.getState() == TileSpace.STATE_NORMAL) {
				// 在这种模式下为启动程序
				mOnAnimationEnd = new Runnable() {
					public void run() {
						Intent intent = new Intent(Intent.ACTION_MAIN);
						TileItemInfo tii = ((Tile) currentView).getTii();
						intent.addCategory(Intent.CATEGORY_LAUNCHER);

						if (tii.isDefault) {
							intent.setClassName(tii.appDefaultPackage,
									tii.appDefaultActivity);
						} else {
							intent.setClassName(tii.appPackage, tii.appActivity);
						}
						startActivitySafely(intent, null);
						mHandler.postDelayed(new Runnable() {
							public void run() {
								mChildTile.clearChildAnimation();
							}
						}, 500);
					}
				};
				mChildTile.startEndAnim(mTileHideAnim, currentView, this, 0);
			} else if (mChildTile.getState() == TileSpace.STATE_EDIT) {
				int w = mChildTile.getTileW();
				int h = mChildTile.getTileH();

				int ew = mChildTile.getEditTileW();
				int eh = mChildTile.getEditTileH();
				mHandler.removeCallbacks(mExitDragMode);
				mHandler.postDelayed(mExitDragMode, sExitDragviewTime);

				LayoutParams lp = null;
				View dragView = mChildTile.getDragView();
				mChildTile.clearDragView();
				if (dragView != null) {
					lp = dragView.getLayoutParams();
					lp.width = ew;
					lp.height = eh;
					dragView.setLayoutParams(lp);
					dragView.clearAnimation();
					dragView.setVisibility(View.VISIBLE);
					mDragController.stopDrag();
				}

				lp = v.getLayoutParams();
				lp.height = h;
				lp.width = w;

				v.setLayoutParams(lp);
				// ((Tile) v).setState(TileSpace.STATE_NORMAL);
				int[] loc = new int[2];
				v.getLocationOnScreen(loc);
				v.clearAnimation();
				mChildTile.setState(TileSpace.STATE_EDIT, true, (Tile) v,
						loc[1]);
				// v.setVisibility(View.VISIBLE);

				// mChildTile.requestLayout();
				mChildTile.setOnLayoutFinish(new TileSpace.OnLayoutFinish() {

					@Override
					public void finish() {
						mDragController.startDrag(currentView);
						mDragController
								.setOnClickListener(LaucherActivity.this);
					}
				});

				AXLog.d(TAG, "onLongClick Tile");

			}
			AXLog.d(TAG, "onClick Tile");
		} else if (v instanceof AppClassView) {
			AXLog.d(TAG, "onClick AppClassView");
			Intent startIntent = new Intent(AppClassActivity.ACTION_START);
			startIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startIntent.setClass(this, AppClassActivity.class);
			startActivityForResult(startIntent, REQUEST_CODE_APPCLASS);
		} else {
			AXLog.d(TAG, "Click on unknow view =" + v);
		}

	}

	void startActivitySafely(Intent intent, Object tag) {
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		try {
			startActivity(intent);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(this, R.string.activity_not_found,
					Toast.LENGTH_SHORT).show();
			Log.e(TAG, "Unable to launch. tag=" + tag + " intent=" + intent, e);
		} catch (SecurityException e) {
			Toast.makeText(this, R.string.activity_not_found,
					Toast.LENGTH_SHORT).show();
			Log.e(TAG,
					"Launcher does not have the permission to launch "
							+ intent
							+ ". Make sure to create a MAIN intent-filter for the corresponding activity "
							+ "or use the exported attribute for this activity. "
							+ "tag=" + tag + " intent=" + intent, e);
		}
		// 只有在启动了别的应用回来时才会播放动画
		mNeedShowAnim = true;
	}

	@Override
	protected void onPause() {
		super.onPause();
		AXLog.d(TAG, "onPause");
		mReceiver.stopUpdate();
		if (mDragLayer.itemsShown()) {
			mDragLayer.hideItems();
		}
		mHandler.removeCallbacks(mHideStatusBar);
		// takeSnapshot();
		mChildTile.pauseBackgroundThread();
	}

	@Override
	protected void onResume() {
		super.onResume();
		AXLog.d(TAG, "show status = " + mConf.getShowStatusbar());

		//
		setFullScreen(mConf.getShowStatusbar());

		if (mConf.getAccentChangedAndReset()) {
			mWorkSpace.invalidate();
			// mChildAppClass.invalidate();
			mChildApp.updateAppItemTheme(mConf.getAccentColor(), mConf.getTextColor());
		}

		AXLog.d(TAG, "onResume");
		// 检查是不是要滚动
		int pos = mAppClasses.getScrollPos();
		if (mAppClasses.getNeedToScroll()) {
			AXLog.d(TAG, "onResume scrolling");
			mChildApp.scrollToY(pos);
			mAppClasses.setNeedToScroll(false);
		}
		mChildButton.updateSearchImage();
		mReceiver.startUpdate();
		// 完成后，要刷新一下status bar.
		sendBroadcast(new Intent(StatusbarController.ACTION_UPDATE_ALL));
		AXLog.d(TAG, "onResume pos = " + pos);

		mHandler.postDelayed(mHideStatusBar, sHideStatusBarTime * 3);
		mChildTile.resumeBackgroundThread();
		Tile t = mTileManager.getAddTile();
		if(t != null) {
			mTileManager.setAddTile(null);
			// 如果有要增加的Tile，则加
			mChildTile.addView(t);
			mChildTile.requestLayout();
		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		AXLog.d(TAG, "onWindowFocusChanged(" + hasFocus + ")");
		if (hasFocus && mNeedShowAnim) {
			if (mWorkSpace.getCurrentScreen() == WorkSpace.SCREEN_APP) {
				mChildApp.startEndAnim(mAppShowAnim, null, null, 150);
			} else if (mWorkSpace.getCurrentScreen() == WorkSpace.SCREEN_TILE) {
				mChildTile.startEndAnim(mTileShowAnim, null, null, 150);
			}
			mNeedShowAnim = false;
		}

		// mNeedShowAnim = !hasFocus; // 当它失去焦点的时候，下次再回来就要播放。
	}

	@Override
	public void onBackPressed() {
		AXLog.d(TAG, "onBackPressed");
		mHandler.removeCallbacks(mShowProgressDialog);
		mHandler.removeCallbacks(mExitDragMode);
		dismissProgessDialog();

		if (mWorkSpace.getAppMode() == AppSpace.MODE_SEARCH) {
			mWorkSpace.setAppSpaceMode(AppSpace.MODE_NORMAL);
		} else if (mWorkSpace.getCurrentScreen() == WorkSpace.SCREEN_APP) {
			// mWorkSpace.setCurrentScreen(WorkSpace.SCREEN_TILE);
			mChildApp.scrollToTop(true);
			mChildTile.scrollToTop();
			mWorkSpace.scrollToScreen(WorkSpace.SCREEN_TILE);
		} else {
			if (mChildTile.getState() == TileSpace.STATE_EDIT) {
				View dragView = mChildTile.getDragView();
				if (dragView != null) {
					dragView.setVisibility(View.VISIBLE);
					dragView.clearAnimation();
					mDragController.stopDrag();
				}
				mChildButton.setChildVisibility(R.id.id_button_arrow,
						View.VISIBLE);
				mChildTile.setState(TileSpace.STATE_NORMAL, true, null, 0);
			} else if (mChildTile.getState() == TileSpace.STATE_NORMAL) {
				mChildTile.scrollToTop();
			}
		}
	}

	private AppMenuDialog createAppContextMenu(View v) {
		AppMenuDialog menu = new AppMenuDialog(this, R.style.app_menu_dialog,
				v, this);
		menu.setOnClickListener(this);
		return menu;
	}

	private void dismissAppContextMenu() {
		if (mAppContextMenu != null && mAppContextMenu.isShowing()) {
			mAppContextMenu.dismiss();
		}
		mAppContextMenu = null;
	}

	private void dismissProgessDialog() {
		if (mCustomDialog != null) {
			if (mCustomDialog.isShowing()) {
				mCustomDialog.dismiss();
			}
			mCustomDialog = null;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		AXLog.d(TAG, "onActivityResult requestCode = " + requestCode
				+ " resultCode = " + resultCode);
		switch (requestCode) {
		case REQUEST_CODE_APPCLASS:
			if (resultCode == RESULT_OK) {
				int pos = data.getIntExtra(AppClassActivity.POSITION_EXTRA, 0);
				mChildApp.scrollToY(pos);
			}
			break;
		default:
			super.onActivityResult(requestCode, resultCode, data);
			break;
		}
	}

	@Override
	public void startActivity(Intent intent) {
		super.startActivity(intent);
		mNeedShowAnim = false;
	}

	@Override
	public void startActivityForResult(Intent intent, int requestCode) {
		super.startActivityForResult(intent, requestCode);
		mNeedShowAnim = false;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		/**
		 * 这里需要做的事情是： 1.更新所有的Tile的名称，因为语言已经改变了。 2.刷新所有的界面，因为语言改变显示也已经变了。
		 */
		if (newConfig.locale.getLanguage().equals(mCurrentLanguage)) {
			// 如果语言没有变，则不做任何事情。
			AXLog.d(TAG, "language not changed.");
			return;
		}

		// 如果语言改变了。
		mCurrentLanguage = newConfig.locale.getLanguage();

		mHandler.postDelayed(mShowProgressDialog, LOADING_DELAY_MAX);
		new Thread("ConfigurationChangeThread") {
			public void run() {
				List<AppItem> list = mAppManager.getAllLauncherAcitivities();
				mTileManager.updateAllTilesName(list);
				mTileManager.updateDatabaseSync(false);

				// 重新获得分类器
				mAppClasses = ClassifyFactory
						.getDefault(getApplicationContext());

				final Vector<TileItemInfo> tiles = mTileManager.getTiles();

				// 加载程序
				final Vector<AppClass> apps = mAppClasses.classify(mAppManager
						.getAllLauncherAcitivities());
				mHandler.removeCallbacks(mShowProgressDialog);
				mHandler.post(new Runnable() {
					public void run() {
						mChildTile.removeAllViews();
						for (TileItemInfo tii : tiles) {
							Tile t = mTileManager.from(tii);
							mChildTile.addView(t);
						}
						mChildAppClass.setAppClasses(apps);
						mChildApp.setAppClasses(apps);
						dismissProgessDialog();
						mChildTile.requestLayout();
					}
				});
			}
		}.start();
	}

	/**
	 * 保存屏幕截图，为锁屏用。
	 */
	private void takeSnapshot(OutputStream os) {
		View cv = getWindow().getDecorView();
		Bitmap b = Bitmap.createBitmap(mConf.getScreenW(), mConf.getScreenH(),
				Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(b);
		cv.draw(c);
		// String file = ICONS + "/" + fileName;
		try {
			b.compress(Bitmap.CompressFormat.PNG, 100, os);
			os.flush();
			os.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mReceiver != null) {
			unregisterReceiver(mReceiver);
			mReceiver = null;
		}

		if (mScreenOffReceiver != null) {
			unregisterReceiver(mScreenOffReceiver);
			mScreenOffReceiver = null;
		}

		if (mPackageReceiver != null) {
			unregisterReceiver(mPackageReceiver);
			mPackageReceiver = null;
		}

		dismissProgessDialog();
	}

	class AsyncHandler extends AsyncQueryHandler {

		public AsyncHandler(ContentResolver cr) {
			super(cr);
		}

		@Override
		protected void onDeleteComplete(int token, Object cookie, int result) {
			// super.onDeleteComplete(token, cookie, result);
			// 在这里先把数据放进去,然后在onInsertComplete再把文件放进去.
			int type = ScreenShot.SHOT_TYPE_INVALID;
			switch (token) {
			case ASYNC_DELETE_INIT_TYPE:
				// 刚起动手机时候的截屏
				type = ScreenShot.SHOT_TYPE_CREATE;
				break;
			case ASYNC_DELETE_RUNNING_TYPE:
				type = ScreenShot.SHOT_TYPE_RUNNING;
				break;
			}
			if (type != ScreenShot.SHOT_TYPE_INVALID) {
				ContentValues cv = new ContentValues();
				cv.put(ScreenShot._SHOT_TYPE, type);
				mQueryHandler.startInsert(ASYNC_SHOT_INSERT_COMPLETE, null,
						ScreenShot.CONTENT_URI, cv);
			}
		}

		@Override
		protected void onInsertComplete(int token, Object cookie, Uri uri) {
			// super.onInsertComplete(token, cookie, uri);
			if (token == ASYNC_SHOT_INSERT_COMPLETE) {
				try {
					OutputStream os = mResolver.openOutputStream(uri);
					if (os != null) {
						takeSnapshot(os);
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}

			} else if (token == ASYNC_SHOT_INSERT_FIRST_RUN) {
				// 如果是第一次运行，到这里就开始插入应用
				List<AppItem> list = mAppManager.getAllLauncherAcitivities();
				int count = 0;
				for (AppItem ai : list) {
					Integer icon = WhiteIconMatcher.matchIcon(ai.mRi);
					// 如果能找到有白图标的应用，则插入
					if (icon != null) {

						// 这里只是加入到数据库，所以不加Tile
						mTileManager.pinToStart(ai.mRi);
						count++;
					}

					if (count >= DEFAULT_PIN_NUMBER) {
						break;
					}
				}

				// 插入完成后，开始加载主界面
				mQueryHandler.startQuery(ASYNC_QUERY_TOKEN_NORMAL, null,
						WP7Launcher.TileInfo.CONTENT_URI, null, null, null,
						WP7Launcher.TileInfo.DEFAULT_SORT_ORDER);
			}
		}

		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {

			if (token == ASYNC_QUERY_TOKEN_NORMAL) {
				// mTileManager.loadTiles(cursor);

				// final TileSpace ts = mChildTile;
				// final TileManager tm = mTileManager;
				final Cursor c = cursor;

				new Thread("LoadTiles_Thread") {
					public void run() {
						mTileManager.loadTiles(c);
						final List<TileItemInfo> list = mTileManager.getTiles();

						// 加载程序
						final List<AppClass> apps = mAppClasses
								.classify(mAppManager
										.getAllLauncherAcitivities());
						mHandler.removeCallbacks(mShowProgressDialog);
						mHandler.post(new Runnable() {
							public void run() {
								mChildTile.removeAllViews();
								for (TileItemInfo tii : list) {
									Tile t = mTileManager.from(tii);
									mChildTile.addView(t);
								}
								mChildAppClass.setAppClasses(apps);
								mChildApp.setAppClasses(apps);
								dismissProgessDialog();
								mChildTile.requestLayout();

								// 我们要开始截图了，但是截图之前要删除上一次的截图
								mQueryHandler
										.startDelete(
												ASYNC_DELETE_INIT_TYPE,
												null,
												WP7Launcher.ScreenShot.CONTENT_URI,
												WP7Launcher.ScreenShot._SHOT_TYPE
														+ " = ?",
												new String[] { Integer
														.toString(WP7Launcher.ScreenShot.SHOT_TYPE_CREATE) });

								if (c != null) {
									c.close();
								}
							}
						});
					}
				}.start();
			} else if (token == ASYNC_QUERY_TOKEN_FIRSTRUN) {
				// 是不是第一次查询的结果到这里来
				if (cursor != null && cursor.getCount() > 0) {
					// 如果不是第一次运行，直接开始
					mQueryHandler.startQuery(ASYNC_QUERY_TOKEN_NORMAL, null,
							WP7Launcher.TileInfo.CONTENT_URI, null, null, null,
							WP7Launcher.TileInfo.DEFAULT_SORT_ORDER);
				} else {
					// 否则向数据插入一条shot_type是invlid的表示已经运行过了。
					ContentValues cv = new ContentValues();
					cv.put(ScreenShot._SHOT_TYPE, ScreenShot.SHOT_TYPE_INVALID);
					mQueryHandler.startInsert(ASYNC_SHOT_INSERT_FIRST_RUN,
							null, ScreenShot.CONTENT_URI, cv);
				}
			} else {
				AXLog.w(TAG, "onQueryComplete: Unknow token =" + token);
			}

		}

		@Override
		protected void onUpdateComplete(int token, Object cookie, int result) {
			super.onUpdateComplete(token, cookie, result);
		}

	}

	@Override
	public void onDragStart(int dragViewCenterX, int dragViewCenterH) {
		mHandler.removeCallbacks(mExitDragMode);
	}

	@Override
	public void onDragDrop(int dragViewCenterX, int dragViewCenterH) {
		mHandler.postDelayed(mExitDragMode, sExitDragviewTime);
	}

	@Override
	public void onDragOver(int x, int y) {
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intentSet = null;
		switch (item.getItemId()) {
		case R.id.id_menu_launcher_setting:
			intentSet = new Intent();
			intentSet.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			intentSet.setClass(getApplicationContext(), SettingsActivity.class);
			startActivity(intentSet);
			break;
		case R.id.id_menu_system_setting:

			intentSet = new Intent(Settings.ACTION_SETTINGS);
			startActivity(intentSet);
			break;
		case R.id.id_menu_add_special_tile:
			intentSet = new Intent();
			intentSet.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			intentSet.setClass(getApplicationContext(), AddSpecialTileActivity.class);
			startActivity(intentSet);
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.system_option_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	private BroadcastReceiver mPackageReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())) {
				// 新程序安装了，只要更新AppSpace
				new Thread("ReloadApp_Thread") {
					public void run() {
						// 加载程序
						final List<AppClass> apps = mAppClasses
								.classify(mAppManager
										.getAllLauncherAcitivities());
						mHandler.removeCallbacks(mShowProgressDialog);
						mHandler.post(new Runnable() {
							public void run() {
								mChildAppClass.setAppClasses(apps);
								mChildApp.setAppClasses(apps);
							}
						});
					}
				}.start();

			} else if (Intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction())) {
				String data = intent.getData().toString();
				boolean isReplace = intent.getBooleanExtra(
						Intent.EXTRA_REPLACING, false);

				if (!isReplace) {
					final String pn = data.substring(data.indexOf(":") + 1);
					new Thread("ReloadAllApp_Thread") {
						public void run() {
							// 删除 pn 对应的Tile然后重新刷新界面
							boolean del = mTileManager.removeTile(pn);
							if (del) {
								mQueryHandler
										.startQuery(
												ASYNC_QUERY_TOKEN_NORMAL,
												null,
												WP7Launcher.TileInfo.CONTENT_URI,
												null,
												null,
												null,
												WP7Launcher.TileInfo.DEFAULT_SORT_ORDER);
							} else {
								// 如果没有在Tile上，那么就要更新 AppSapce
								// 加载程序
								final List<AppClass> apps = mAppClasses
										.classify(mAppManager
												.getAllLauncherAcitivities());
								mHandler.removeCallbacks(mShowProgressDialog);
								mHandler.post(new Runnable() {
									public void run() {
										mChildAppClass.setAppClasses(apps);
										mChildApp.setAppClasses(apps);
									}
								});
							}
						}
					}.start();
				}

			}
		}

	};

	@Override
	public void onAnimationEnd(Animation animation) {
		if (mOnAnimationEnd != null) {
			mOnAnimationEnd.run();
			mOnAnimationEnd = null;
		}
	}

	@Override
	public void onAnimationRepeat(Animation animation) {

	}

	@Override
	public void onAnimationStart(Animation animation) {

	}

	public void getStatusBarHeight() {
		Rect frame = new Rect();
		getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
		int statusBarHeight = frame.top;
		AXLog.d(TAG, "statusBarHeight =" + statusBarHeight);
		mConf.setStatusBarHeight(statusBarHeight);
	}

	private void setFullScreen(boolean show) {
		mDragLayer.setStatusbar(show);
		if (show) {
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
		} else {
			final WindowManager.LayoutParams attrs = getWindow()
					.getAttributes();
			attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
			getWindow().setAttributes(attrs);
			getWindow().clearFlags(
					WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

		}
	}
}