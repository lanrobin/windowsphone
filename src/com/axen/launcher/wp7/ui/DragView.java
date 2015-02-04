package com.axen.launcher.wp7.ui;

import com.axen.launcher.app.TileItemInfo;
import com.axen.launcher.wp7.main.R;
import com.axen.launcher.wp7.main.WP7App;
import com.axen.launcher.wp7.main.WP7Configuration;
import com.axen.launcher.wp7.ui.size.Size;
import com.axen.launcher.wp7.ui.statusbar.StatusBarManager;
import com.axen.utils.AXLog;
import com.axen.utils.ResourceUtil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.IBinder;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

// import android.view.WindowManagerImpl;

public class DragView extends ViewGroup {

	private static final String TAG = "DragView";

	private static final int ON_CLICK_TIME = 500; // 如果在0。5秒内完成Down和Up的动作，并且中间过程中没有移动，则表示ONCLICK事件。

	private static final int RECT_NONE = -1;
	private static final int RECT_UNPIN = 0;
	private static final int RECT_EDIT = 1;
	private ImageView mUnpin = null;
	private ImageView mEdit = null;
	private ImageView mTileSnap = null;

	private int mTileWidth = 0;
	private int mTileHeight = 0;
	private int mButtonWidth = 0;
	private int mButtonHeight = 0;
	private int mDragViewWidth = 0;
	private int mDragViewHeight = 0;

	private WP7Configuration mConf = WP7Configuration.getInstance();
	private ResourceUtil mru = mConf.getRU();
	private Size size = Size.get();
	private int mRegistrationX;
	private int mRegistrationY;

	private int mOnScreenCenterX;
	private int mOnScreenCenterY;
	private WindowManager.LayoutParams mLayoutParams;
	private WindowManager mWindowManager;

	private boolean mIsMoving = false;

	private long mDownTime = 0; // 记录Down事件的时间，用来检查是不是满足OnClick的条件。
	private Rect mRectUnpin = new Rect();
	private Rect mRectEdit = new Rect();
	private int touchViewRect = RECT_NONE; // 记录是按在Unpin（RECT_UNPIN）上还edit(RECT_EDIT)上，如果RECT_NONE则什么也没按到。

	private TileItemInfo mTileInfo = null;

	public DragView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		mDragViewWidth = size.getDrag_view_w(); //mru.getPexil(R.dimen.drag_view_w);
		mDragViewHeight = size.getDrag_view_h(); //mru.getPexil(R.dimen.drag_view_h);
		mButtonWidth = size.getButton_w(); //mru.getPexil(R.dimen.button_w);
		mButtonHeight = size.getButton_h(); //mru.getPexil(R.dimen.button_h);

		mRegistrationX = mDragViewWidth / 2;
		mRegistrationY = mDragViewHeight / 2;
	}

	public DragView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public DragView(Context context) {
		this(context, null);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mUnpin = (ImageView) findViewById(R.id.id_dragview_unpin);
		mEdit = (ImageView) findViewById(R.id.id_dragview_edit);
		mTileSnap = (ImageView) findViewById(R.id.id_dragview_tile);
		
		mUnpin.setImageResource(StatusBarManager.getEditTileIcon(mConf.getBackgroundColorMode(), 1));
		mEdit.setImageResource(StatusBarManager.getEditTileIcon(mConf.getBackgroundColorMode(), 0));
	}

	/**
	 * Create a window containing this view and show it.
	 * 
	 * @param windowToken
	 *            obtained from v.getWindowToken() from one of your views
	 * @param touchX
	 *            the x coordinate the user touched in screen coordinates
	 * @param touchY
	 *            the y coordinate the user touched in screen coordinates
	 */
	public void show(IBinder windowToken, int touchX, int touchY) {
		WindowManager.LayoutParams lp;
		int pixelFormat;

		pixelFormat = PixelFormat.TRANSLUCENT;

		lp = new WindowManager.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT, touchX - mRegistrationX,
				touchY - mRegistrationY,
				WindowManager.LayoutParams.TYPE_APPLICATION_SUB_PANEL,
				WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
						| WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
						| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
						| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
				/* | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM */,
				pixelFormat);
		// lp.token = mStatusBarView.getWindowToken();
		lp.gravity = Gravity.LEFT | Gravity.TOP;
		lp.token = windowToken;
		lp.setTitle("DragView");
		mLayoutParams = lp;

		mOnScreenCenterX = touchX;
		mOnScreenCenterY = touchY;

		mWindowManager.addView(this, lp);

		// mAnimationScale = 1.0f/mScale;
		// mTween.start(true);
	}

	public void setImageView(Bitmap b, int w, int h) {
		if (mTileSnap != null) {
			mTileSnap.setImageBitmap(b);
			// mDragViewWidth = w;
			// mDragViewHeight = h;
			mTileWidth = w;
			mTileHeight = h;
			requestLayout();
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int marginL = (r - l - mTileWidth) / 2;
		int marginT = (b - t - mTileHeight) / 2;
		mTileSnap.layout(l + marginL, t + marginT, r - marginL, b - marginT);
		mUnpin.layout(r - mButtonWidth, t, r, t + mButtonHeight);
		mUnpin.bringToFront();
		mEdit.layout(l, t, l + mButtonWidth, t + mButtonHeight);
		mEdit.bringToFront();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		View child = null;
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			child = getChildAt(i);
			child.measure(widthMeasureSpec, heightMeasureSpec);
		}
		setMeasuredDimension(mDragViewWidth, mDragViewHeight);
	}

	public void setWindowManager(WindowManager wm) {
		mWindowManager = wm;
	}

	/**
	 * Move the window containing this view.
	 * 
	 * @param touchX
	 *            the x coordinate the user touched in screen coordinates
	 * @param touchY
	 *            the y coordinate the user touched in screen coordinates
	 */
	void move(int touchX, int touchY) {
		if (pointInView(touchX, touchY)) {
			if (!mIsMoving) {
				mIsMoving = true;
				mUnpin.setVisibility(View.GONE);
				mEdit.setVisibility(View.GONE);
			}
			WindowManager.LayoutParams lp = mLayoutParams;
			lp.x = touchX - mRegistrationX;
			lp.y = touchY - mRegistrationY;
			mWindowManager.updateViewLayout(this, lp);
			mOnScreenCenterY = touchY;
			mOnScreenCenterX = touchX;
		}

	}

	boolean move(MotionEvent ev) {
		int action = ev.getAction();
		int touchx = (int) ev.getX();
		int touchy = (int) ev.getY();
		boolean handled = pointInView(touchx, touchy);
		if (handled) {
			if (checkMotionEvent(ev)) {
				move(touchx, touchy);
			}
		}
		return handled;
	}

	/**
	 * 只要是点击在DragView上，DragLayer就是劫持事件。
	 * 
	 * @param touchX
	 * @param touchY
	 * @return
	 */
	public boolean pointInView(int touchX, int touchY) {
		Rect rect = new Rect();
		getHitRect(rect);
		
		int deltaX = (int)(mButtonWidth * 0.5);
		int deltaY = (int)(mButtonHeight * 0.5);
		inflateRect(rect, deltaX, deltaY);
		
		
		int[] pt = new int[2];
		getLocationOnScreen(pt);
		int x = touchX - pt[0];
		int y = touchY - pt[1];
		return rect.contains(x, y);

	}

	/**
	 * 检查是不是可以产生OnClick事情，因为系统不会为我们检查。
	 * 
	 * @param ev
	 * @return
	 */
	public boolean checkMotionEvent(MotionEvent ev) {
		int action = ev.getAction();
		int touchx = (int) ev.getX();
		int touchy = (int) ev.getY();
		int[] pt = new int[2];
		getLocationOnScreen(pt);
		int x = touchx - pt[0];
		int y = touchy - pt[1];

		boolean moving = false;
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			mUnpin.getHitRect(mRectUnpin);

			mEdit.getHitRect(mRectEdit);
			
			// 我们把它们的区域增加1.2倍，让用户更容易选择
			int deltaX = (int)(mButtonWidth * 0.5);
			int deltaY = (int)(mButtonHeight * 0.5);
			
			inflateRect(mRectUnpin, deltaX, deltaY);
			inflateRect(mRectEdit, deltaX, deltaY);
			
			AXLog.d(TAG, "DOWN mRectUnpin =" + mRectUnpin.toShortString());
			AXLog.d(TAG, "DOWN mRectEdit =" + mRectEdit.toShortString());
			AXLog.d(TAG, "DOWN (x, y) = (" + x + "," + y + ")");
			// 记录Down的时间
			mDownTime = System.currentTimeMillis();

			// 记录所在的RECT
			if (mRectUnpin.contains(x, y)) {
				touchViewRect = RECT_UNPIN;
			} else if (mRectEdit.contains(x, y)) {
				touchViewRect = RECT_EDIT;
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (touchViewRect == RECT_UNPIN) {
				// 如果已经移出这个view了，则不算OnClick事件了。
				if (!mRectUnpin.contains(x, y)) {
					touchViewRect = RECT_NONE;
					moving = true;
				}
			} else if (touchViewRect == RECT_EDIT) {
				if (!mRectEdit.contains(x, y)) {
					touchViewRect = RECT_NONE;
					moving = true;
				}
			} else {
				moving = true;
			}
			break;
		case MotionEvent.ACTION_CANCEL:
			touchViewRect = RECT_NONE;
			// moving = true;
			break;
		case MotionEvent.ACTION_UP:
			AXLog.d(TAG, "UP mRectUnpin =" + mRectUnpin.toShortString());
			AXLog.d(TAG, "UP mRectEdit =" + mRectEdit.toShortString());
			AXLog.d(TAG, "UP (x, y) = (" + x + "," + y + ")");
			AXLog.d(TAG, "UP time " + (System.currentTimeMillis() - mDownTime));
			// 如果在这过程中，始终没有移出这个view,则要检查是不是可以算作OnClick.
			if (touchViewRect == RECT_UNPIN) {
				if (mRectUnpin.contains(x, y)) {
					if (System.currentTimeMillis() - mDownTime < ON_CLICK_TIME) {
						// 算是OnClick事件
						AXLog.d(TAG, "ClickOn Unpin");
						mUnpin.performClick();
					}
				}
			} else if (touchViewRect == RECT_EDIT) {
				if (mRectEdit.contains(x, y)) {
					if (System.currentTimeMillis() - mDownTime < ON_CLICK_TIME) {
						// 算是OnClick事件
						AXLog.d(TAG, "ClickOn Edit");
						if (mEdit != null && mEdit.isEnabled()) {
							mEdit.performClick();
						}
					}
				}
			}

			touchViewRect = RECT_NONE;
			break;
		}
		return moving;
	}

	void stopMove() {
		if (mIsMoving) {
			mIsMoving = false;
			mUnpin.setVisibility(View.VISIBLE);
			if (mEdit.isEnabled()) {
				mEdit.setVisibility(View.VISIBLE);
			}
		}
	}

	void remove() {
		mWindowManager.removeView(this);
	}

	public int getCenterX() {
		return mOnScreenCenterX;
	}

	public int getCenterY() {
		return mOnScreenCenterY;
	}

	public void setOnClickListener(OnClickListener l) {
		if (mUnpin != null) {
			mUnpin.setOnClickListener(l);
		}

		if (mEdit != null) {
			mEdit.setOnClickListener(l);
		}

	}

	public void setTileItemInfo(TileItemInfo tii) {
		mTileInfo = tii;
		if (mTileInfo != null) {
			if (mTileInfo.wideTile) {
				mDragViewWidth = size.getDrag_view_wide_w(); //mru.getPexil(R.dimen.drag_view_wide_w);
			}

			// 如果是不能编辑的Tile则
			if (mTileInfo.isFixTile()) {
				if (mEdit != null) {
					mEdit.setVisibility(GONE);
					mEdit.setEnabled(false);
				}
			}
		}

	}
	
	private void inflateRect(Rect r, int x, int y) {
		r.left -= x;
		r.right += x;
		r.top -= y;
		r.bottom += y;
	}
}
