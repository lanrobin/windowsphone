package com.axen.launcher.wp7.ui;

import java.util.ArrayList;
import java.util.List;

import com.axen.launcher.app.TileItemInfo;
import com.axen.launcher.wp7.main.R;
import com.axen.launcher.wp7.ui.widget.Tile;
import com.axen.utils.AXLog;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;

public class DragController {

	private static final String TAG = "DragController";

	private static final int INVALID_COORDINATE = -1;

	/**
	 * 300毫秒内移动距离超过5像素的距离就算是在拖动屏幕
	 */
	private static final int DRAG_DISTINGUISH_INTERVAL = 300; //
	private static final int DRAG_DISTINGUISH_DISTANCE = 5; //

	private static final long VIBRATE_DURATION = 35;

	private Context mContext = null;

	/* MotionEvent.ACTION_DOWN 时候记录的 坐标 */
	private int mDownX = INVALID_COORDINATE;
	private int mDownY = INVALID_COORDINATE;

	/*
	 * 是不是有组件正在被拖动。
	 */
	private boolean mDragging = false;

	private long mDownTime = 0;

	/** Where the drag originated */
	// private DragSource mDragSource;

	/** The data associated with the object being dragged */
	private TileItemInfo mDragInfo;

	/** The view that moves around while you drag. */
	private DragView mDragView;

	/** Who can receive drop events */
	private ArrayList<DropTarget> mDropTargets = new ArrayList<DropTarget>();

	private List<DragListener> mListeners = new ArrayList<DragListener>();

	/** The window token used as the parent for the DragView. */
	private IBinder mWindowToken;

	private DragScroller mDragScroller;

	private View mScrollView;

	private View mOriginatorView = null;

	private int[] mCoordinatesTemp = new int[2];

	private InputMethodManager mInputMethodManager;

	private int mMotionDownX;

	private int mMotionDownY;

	private int mTouchOffsetX;

	private int mTouchOffsetY;

	private Vibrator mVibrator;

	private WindowManager mWindowManager = null;

	public DragController(Context context) {
		mContext = context;
	}

	public boolean isDragging() {
		return mDragging;
	}
	
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		AXLog.d(TAG, "action = " + ev.getAction());
		AXLog.d(TAG, "[x,y] = [" + ev.getX() + ", " + ev.getY() + "]");
		AXLog.d(TAG, "raw[x, y] = [" + ev.getRawX() + "," + ev.getRawY() + "]");
		boolean inDragView = false;
		final int screenX = (int) ev.getX();
		final int screenY = (int) ev.getY();
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (mDragView != null) {
				inDragView = mDragView.pointInView(screenX, screenY);
			}
			mMotionDownX = screenX;
			mMotionDownY = screenY;
			break;
		case MotionEvent.ACTION_MOVE:
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			break;
		default:
			AXLog.d(TAG, "Unhandled action " + ev.getAction()
					+ " in onInterceptTouchEvent.");
			break;
		}
		/*
		 * 如果正在拖动，则我们自己处理
		 */
		return mDragging && inDragView;
	}

	public boolean onTouchEvent(MotionEvent ev) {

		/*
		 * 如果不在拖动，我们则不做任何处理。
		 */
		if (!mDragging) {
			return false;
		}
		final int action = ev.getAction();

		if (action == MotionEvent.ACTION_DOWN) {
			// recordScreenSize();
		}

		final int screenX = (int) ev.getX();// clamp((int)ev.getRawX(), 0,
											// mDisplayMetrics.widthPixels);
		final int screenY = (int) ev.getY();// clamp((int)ev.getRawY(), 0,
											// mDisplayMetrics.heightPixels);

		// 我们要监控所有的事件
		mDragView.move(ev);

		switch (action) {
		case MotionEvent.ACTION_MOVE:
			// mDragView.move((int)ev.getRawX(), (int)ev.getRawY());
			if (mDragView != null && mListeners != null) {
				invokeDragOver(mDragView.getCenterX(), mDragView.getCenterY());
			}
			break;

		case MotionEvent.ACTION_DOWN:
			// Remember location of down touch
			mMotionDownX = screenX;
			mMotionDownY = screenY;
			if (mDragView != null && mListeners != null) {
				invokeDragStart(mDragView.getCenterX(), mDragView.getCenterY());
				invokeDragOver(mDragView.getCenterX(), mDragView.getCenterY());
			}
			// mLastDropTarget = null;
			break;

		case MotionEvent.ACTION_CANCEL:
			break;
		case MotionEvent.ACTION_UP:
			if (mDragView != null) {
				mDragView.stopMove();
				invokeDragDrop(mDragView.getCenterX(), mDragView.getCenterY());
			}
			break;
		}
		return true;
	}

	public boolean dispatchUnhandledMove(View focused, int direction) {
		// TODO Auto-generated method stub
		return false;
	}

	public void setDragScoller(DragScroller scroller) {
		mDragScroller = scroller;
	}

	public void setWindowToken(IBinder token) {
		mWindowToken = token;
	}

	/**
	 * Sets the drag listner which will be notified when a drag starts or ends.
	 */
	public void addDragListener(DragListener l) {
		mListeners.add(l);
	}

	/**
	 * Remove a previously installed drag listener.
	 */
	public void removeDragListener(DragListener l) {
		mListeners.remove(l);
	}
	
	private void invokeDragStart(int x, int y) {
		for(DragListener l: mListeners) {
			l.onDragStart(x, y);
		}
	}
	
	private void invokeDragOver(int x, int y) {
		for(DragListener l: mListeners) {
			l.onDragOver(x, y);
		}
	}
	
	private void invokeDragDrop(int x, int y) {
		for(DragListener l: mListeners) {
			l.onDragDrop(x, y);
		}
	}

	/**
	 * Add a DropTarget to the list of potential places to receive drop events.
	 */
	public void addDropTarget(DropTarget target) {
		mDropTargets.add(target);
	}

	/**
	 * Don't send drop events to <em>target</em> any more.
	 */
	public void removeDropTarget(DropTarget target) {
		mDropTargets.remove(target);
	}

	/**
	 * Set which view scrolls for touch events near the edge of the screen.
	 */
	public void setScrollView(View v) {
		mScrollView = v;
	}

	/**
	 * Clamp val to be &gt;= min and &lt; max.
	 */
	private static int clamp(int val, int min, int max) {
		if (val < min) {
			return min;
		} else if (val >= max) {
			return max - 1;
		} else {
			return val;
		}
	}

	/**
	 * Interface to receive notifications when a drag starts or stops
	 */
	interface DragListener {
		public void onDragStart(int dragViewCenterX, int dragViewCenterH);
		public void onDragDrop(int dragViewCenterX, int dragViewCenterH);
		public void onDragOver(int x, int y);
	}

	public void startDrag(View v) {
		mOriginatorView = v;
		Bitmap b = getViewBitmap(v);

		if (b == null) {
			// out of memory?
			return;
		}
		int[] loc = mCoordinatesTemp;
		v.getLocationOnScreen(loc);
		int screenX = loc[0];
		int screenY = loc[1];
		startDrag(b, screenX, screenY, ((Tile)v).getTii());
	}

	public void stopDrag() {
		// stopMove();
		if (mWindowManager != null) {
			mWindowManager.removeView(mDragView);
			if (mDragView != null) {
				mDragView.stopMove();
			}
			mDragView = null;
		}
		mDragging = false;
	}

	/**
	 * Draw the view into a bitmap.
	 */
	private Bitmap getViewBitmap(View v) {
		v.clearFocus();
		v.setPressed(false);

		boolean willNotCache = v.willNotCacheDrawing();
		// v.buildDrawingCache();
		v.setWillNotCacheDrawing(false);

		// Reset the drawing cache background color to fully transparent
		// for the duration of this operation
		int color = v.getDrawingCacheBackgroundColor();
		v.setDrawingCacheBackgroundColor(0);

		if (color != 0) {
			v.destroyDrawingCache();
		}
		v.buildDrawingCache();
		Bitmap cacheBitmap = v.getDrawingCache();
		if (cacheBitmap == null) {
			Log.e(TAG, "failed getViewBitmap(" + v + ")",
					new RuntimeException());
			return null;
		}

		Bitmap bitmap = Bitmap.createBitmap(cacheBitmap);

		// Restore the view
		v.destroyDrawingCache();
		v.setWillNotCacheDrawing(willNotCache);
		v.setDrawingCacheBackgroundColor(color);

		return bitmap;
	}

	/**
	 * Starts a drag.
	 * 
	 * @param b
	 *            The bitmap to display as the drag image. It will be re-scaled
	 *            to the enlarged size.
	 * @param screenX
	 *            The x position on screen of the left-top of the bitmap.
	 * @param screenY
	 *            The y position on screen of the left-top of the bitmap.
	 * @param textureLeft
	 *            The left edge of the region inside b to use.
	 * @param textureTop
	 *            The top edge of the region inside b to use.
	 * @param textureWidth
	 *            The width of the region inside b to use.
	 * @param textureHeight
	 *            The height of the region inside b to use.
	 * @param source
	 *            An object representing where the drag originated
	 * @param dragInfo
	 *            The data associated with the object that is being dragged
	 * @param dragAction
	 *            The drag action: either {@link #DRAG_ACTION_MOVE} or
	 *            {@link #DRAG_ACTION_COPY}
	 */
	public void startDrag(Bitmap b, int screenX, int screenY, TileItemInfo dragInfo) {

		// Hide soft keyboard, if visible
		if (mInputMethodManager == null) {
			mInputMethodManager = (InputMethodManager) mContext
					.getSystemService(Context.INPUT_METHOD_SERVICE);
		}
		mInputMethodManager.hideSoftInputFromWindow(mWindowToken, 0);


		// int registrationX = ((int)mMotionDownX) - screenX;
		// int registrationY = ((int)mMotionDownY) - screenY;

		mTouchOffsetX = mMotionDownX - screenX;
		mTouchOffsetY = mMotionDownY - screenY;

		mDragging = true;
		mDragInfo = dragInfo;

		if (mVibrator == null) {
			mVibrator = (Vibrator) mContext
					.getSystemService(Context.VIBRATOR_SERVICE);
		}
		mVibrator.vibrate(VIBRATE_DURATION);

		final LayoutInflater inflater = LayoutInflater.from(mContext);

		DragView dragView = mDragView = (DragView) inflater.inflate(
				R.layout.drag_view, null);
		dragView.setTileItemInfo(mDragInfo);
		mDragView.setImageView(b, b.getWidth(), b.getHeight());
		dragView.setWindowManager(mWindowManager);
		dragView.show(mWindowToken, (int) mMotionDownX, (int) mMotionDownY);
	}

	public void setWindowManager(WindowManager wm) {
		mWindowManager = wm;
	}

	public void setOnClickListener(OnClickListener l) {
		if (mDragView != null) {
			mDragView.setOnClickListener(l);
		}
	}
}
