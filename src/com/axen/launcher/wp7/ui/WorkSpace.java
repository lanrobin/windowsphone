package com.axen.launcher.wp7.ui;

import com.axen.launcher.wp7.main.R;
import com.axen.launcher.wp7.main.WP7App;
import com.axen.launcher.wp7.main.WP7Configuration;
import com.axen.launcher.wp7.ui.anim.AnimUtil;
import com.axen.launcher.wp7.ui.anim.DistanceInterpolator;
import com.axen.launcher.wp7.ui.anim.WP7Interpolator;
import com.axen.launcher.wp7.ui.size.Size;
import com.axen.utils.AXLog;
import com.axen.utils.ResourceUtil;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.Scroller;

public class WorkSpace extends ViewGroup implements
		GestureDetector.OnGestureListener {

	private static final String TAG = "WorkSpace";
	private DragController mDragController = null;
	private Scroller mScroller = null;
	private GestureDetector mGesture = null;
	private Context mContext = null;
	private DragLayer mDragLayer = null;
	
	private OnScrollFinish mScrollFinished = null;
	private int mHitX = 0;
	private int mHitY = 0;
	private Interpolator mScrollInterplator = new WP7Interpolator();
			/**new Interpolator() {

		public float getInterpolation(float t) {
			// return (float) Math.sqrt(t);
			return (float) Math.pow(t, 1 / Math.E);
		}
	};*/

	private DistanceInterpolator mArrowRotateAnim = null;

	private int mMaximumVelocity = 0;
	private int mTouchSlop = 0;
	private int mCurrentScreen = SCREEN_TILE;
	private int mTouchState = TOUCH_EVENT_IDLE;
	private int mLastMoveX = 0;
	private int mLastMoveY = 0;
	private int mMoveOffset = 0;
	private int mTileViewWidth = 0;
	private int mButtonViewWidth = 0;
	private int mAppViewWidth = 0;
	private int mWorkspaceWidth = 0;
	
	private int mAppSpaceNormalW = 0;
	private int mAppSpaceSearchW = 0;
	
	private int mStatusBarH = 0;
	private int mAppItemH = 0;
	private int mAppItemSpaceH = 0;
	
	private int mLastScrollX = 0;

	private boolean mArrowAnimSet = false; // arrowbutton动画是不是已经开始了。

	TileSpace mChildTile = null;
	ButtonSpace mChildButton = null;
	AppSpace mChildApp = null;
	AppClassSpace mChildAppClass = null;
	
	private WP7Configuration mConf = WP7Configuration.getInstance();
	private ResourceUtil mru = mConf.getRU();
	private Size size = Size.get();
	
	private int mAppSpaceMode = AppSpace.MODE_NORMAL;
	/**
	 * The velocity at which a fling gesture will cause us to snap to the next
	 * screen
	 */
	public static final int SNAP_VELOCITY = 600;

	public static final int TOUCH_EVENT_IDLE = 0;
	public static final int TOUCH_EVENT_DETECT = 1;
	public static final int TOUCH_EVENT_SCROLL_H = 2;
	public static final int TOUCH_EVENT_SCROLL_V = 3;

	public static final int SCREEN_TILE = 0;
	public static final int SCREEN_APP = 1;
	
	public static interface OnScrollFinish{
		public void scrollFinish();
	}

	
	
	public WorkSpace(Context context) {
		this(context, null);
	}

	public WorkSpace(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public WorkSpace(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		initWorkspace(context);
	}
	
	private void initWorkspace(Context context) {
		mScroller = new Scroller(context, mScrollInterplator);
		final ViewConfiguration configuration = ViewConfiguration.get(context);
		mTouchSlop = configuration.getScaledTouchSlop();
		mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();

		mGesture = new GestureDetector(context, this);
		
		mStatusBarH = size.getStatus_bar_h(); //mru.getPexil(R.dimen.status_bar_h);
		mAppItemH = size.getApp_icon_h(); //mru.getPexil(R.dimen.app_icon_h);
		mAppItemSpaceH = size.getApp_icon_space(); //mru.getPexil(R.dimen.app_icon_space);
		mAppSpaceNormalW = size.getApp_space_w(); //mru.getPexil(R.dimen.app_space_w);
		mAppSpaceSearchW = mConf.getUsableHeight();
		mTileViewWidth = size.getTile_space_w(); //mru.getPexil(R.dimen.tile_space_w);
		mAppViewWidth = mTileViewWidth;  // 它们一样宽的
		mButtonViewWidth = size.getButton_space_w(); //mru.getPexil(R.dimen.button_space_w);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		mChildTile = (TileSpace) findViewById(R.id.id_tile_space);
		mChildButton = (ButtonSpace) findViewById(R.id.id_button_space);
		mChildApp = (AppSpace) findViewById(R.id.id_app_space);
		mChildAppClass = (AppClassSpace)findViewById(R.id.id_app_class_space);

		mChildTile.setWorkspace(this);
		// mChildTile.setTouchSlop(mTouchSlop);
		mChildApp.setWorkspace(this);
		// mChildApp.setTouchSlop(mTouchSlop);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		AXLog.d(TAG, "widthMeasureSpec = " + widthMeasureSpec
				+ ", heightMeasureSpec = " + heightMeasureSpec);

		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		final int width = MeasureSpec.getSize(widthMeasureSpec);
		final int widthMode = MeasureSpec.getMode(widthMeasureSpec);

		final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int measuredHeight = MeasureSpec.getSize(heightMeasureSpec);

		View child = null;
		int count = getChildCount();
		AXLog.d(TAG, "onMeasure: count = " + count);
		for (int i = 0; i < count; i++) {
			child = getChildAt(i);
			AXLog.d(TAG, "onMeasure: child.id = " + child.getId());

			// child.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
			child.measure(widthMeasureSpec, heightMeasureSpec);
		}

		mWorkspaceWidth = mAppViewWidth + mButtonViewWidth + mAppViewWidth;

		setMeasuredDimension(mWorkspaceWidth, measuredHeight);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {

		AXLog.d(TAG, "onLayout: changed=" + changed + ", l=" + l + ", t="
				+ ",r=" + r + ",b=" + b);
		if(mAppSpaceMode == AppSpace.MODE_SEARCH) {
			onSearchLayout(changed, l, t, r, b);
		}else {
			onNormalLayout(changed, l, t, r, b);
		}
	}
	
	
	
	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
		setBackgroundColor(mConf.getBackgroundColor());
	}

	private void onSearchLayout(boolean changed, int l, int t, int r, int b) {

		AXLog.d(TAG, "onSearchLayout");
		
		int childLeft = 0;
		int childWidth = 0;

		/*
		 * 首先是Tile这一屏的宽度
		 * 目前来说只有四个children, 一个是TileSpace,一个ButtonSpace 一个是AppSpace，还有一个AppClassSpace。
		 * 但是对于在Search模式下，ButtonSpace是不可见的，AppSpace和AppClassSpace是全屏的宽度
		 */
		View child = mChildTile;
		childWidth = mTileViewWidth;
		child.layout(childLeft, t, childWidth, b);
		childLeft += childWidth;

		/*
		childWidth = mButtonViewWidth;
		AXLog.d(TAG, "onLayout 1: childLeft = " + childLeft);
		child = mChildButton;
		child.layout(childLeft, t, childLeft + childWidth, b);
		childLeft += childWidth;
		*/
		// 设置不可见
		mChildButton.setVisibility(GONE);
		
		// 我们重新设置宽度，与屏幕一样宽
		childWidth = mAppSpaceSearchW;
		
		int top = 0;
		if(mConf.getShowStatusbar()) {
			top = mConf.getStatusBarHeight();
		}
		child = mChildAppClass;
		
		child.layout(childLeft, top, childLeft + childWidth, top + mAppItemH + mAppItemSpaceH);
		
		top += (mAppItemH + mAppItemSpaceH);
		
		AXLog.d(TAG, "onLayout 2: childLeft = " + childLeft);
		child = mChildApp;
		child.layout(childLeft, top, childLeft + childWidth, b);

		AXLog.d(TAG,
				"onLayout 3: childLeft = "
						+ (childLeft + child.getMeasuredWidth()));

		if (mChildButton != null && mCurrentScreen == SCREEN_TILE) {
			mChildButton.setAlpha(0.0f);
		}
	}
	
	private void onNormalLayout(boolean changed, int l, int t, int r, int b) {

		AXLog.d(TAG, "onNormalLayout");
		
		int childLeft = 0;
		int childWidth = 0;

		/*
		 * 首先是Tile这一屏的宽度
		 * 目前来说只有四个children, 一个是TileSpace,一个ButtonSpace 一个是AppSpace，还有一个AppClassSpace。
		 */
		View child = mChildTile;
		childWidth = mTileViewWidth;
		child.layout(childLeft, t, childWidth, b);
		childLeft += childWidth;

		childWidth = mButtonViewWidth;
		AXLog.d(TAG, "onLayout 1: childLeft = " + childLeft);
		child = mChildButton;
		child.layout(childLeft, t, childLeft + childWidth, b);
		childLeft += childWidth;
		
		// 我们要重新排列
		childWidth = mAppSpaceNormalW;
		
		int top = 0;
		if(mConf.getShowStatusbar()) {
			top = mStatusBarH;
		}
		child = mChildAppClass;
		
		child.layout(childLeft, top, childLeft + childWidth, top + mAppItemH + mAppItemSpaceH);
		
		top += (mAppItemH + mAppItemSpaceH);
		
		AXLog.d(TAG, "onLayout 2: childLeft = " + childLeft);
		child = mChildApp;
		child.layout(childLeft, top, childLeft + childWidth, b);

		AXLog.d(TAG,
				"onLayout 3: childLeft = "
						+ (childLeft + child.getMeasuredWidth()));

		if (mChildButton != null && mCurrentScreen == SCREEN_TILE) {
			mChildButton.setAlpha(0.0f);
		}
	}

	void setCurrentScreen(int screen) {
		if(mDragLayer != null) {
			// 如果是app space加载广告
			//mDragLayer.loadAd(screen == SCREEN_APP);
		}
		mCurrentScreen = screen;
	}

	public void setDragController(DragController dc) {
		mDragController = dc;
	}

	public void setDragLayer(DragLayer dl) {
		mDragLayer = dl;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		// return super.dispatchTouchEvent(ev);
		// 如果是上下滚动，那么就转发给子view
		super.dispatchTouchEvent(ev);

		return true;
		// return false;
	}

	@Override
	public boolean dispatchUnhandledMove(View focused, int direction) {
		// TODO Auto-generated method stub
		return super.dispatchUnhandledMove(focused, direction);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		int action = ev.getAction();
		int x = (int) ev.getX();
		int y = (int) ev.getY();

		// 如果是编辑状态则不能左右拖动
		if (mChildTile.getState() != TileSpace.STATE_NORMAL || mChildApp.getMode() != AppSpace.MODE_NORMAL) {
			return false;
		}

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			mLastMoveX = x;
			mLastMoveY = y;
			// 先置成探测状态。
			if (mTouchState == TOUCH_EVENT_IDLE && mScroller.isFinished()) {
				mTouchState = TOUCH_EVENT_DETECT;
			}
			break;
		case MotionEvent.ACTION_MOVE:
			final int xDiff = x - mLastMoveX;
			final int yDiff = y - mLastMoveY;

			AXLog.d(TAG, " mTouchSlop = " + mTouchSlop + ", xDiff = " + xDiff
					+ ", yDiff = " + yDiff);
			/*
			 * 滚动分两种情况，在第一屏的时候只能往右滚，在第二屏的时候只能往左滚
			 */
			if (mCurrentScreen == SCREEN_TILE) {

				// 如果上下滚，则不能左右滚了。
				if (Math.abs(yDiff) > mTouchSlop) {
					mTouchState = TOUCH_EVENT_SCROLL_V;
					mChildTile.setTouchState(mTouchState);
					AXLog.d(TAG, "TOUCH_EVENT_SCROLL_V set.");
				}

				// 只能往右滚
				else if (xDiff < 0 && Math.abs(xDiff) > mTouchSlop) {
					mTouchState = TOUCH_EVENT_SCROLL_H;
					AXLog.d(TAG, "TOUCH_EVENT_SCROLL_H set.");
				} else {
					AXLog.d(TAG, "mCurrentScreen = " + mCurrentScreen
							+ ",xDiff = " + xDiff);
				}
			} else if (mCurrentScreen == SCREEN_APP) {
				// 如果上下滚，则不能左右滚了。
				if (Math.abs(yDiff) > mTouchSlop) {
					mTouchState = TOUCH_EVENT_SCROLL_V;
					mChildApp.setTouchState(mTouchState);
				} else if (xDiff > 0 && Math.abs(xDiff) > mTouchSlop) {
					mTouchState = TOUCH_EVENT_SCROLL_H;
				} else {
					AXLog.d(TAG, "mCurrentScreen = " + mCurrentScreen
							+ ",xDiff = " + xDiff);
				}
			} else {
				AXLog.d(TAG, "Invalid screen = " + mCurrentScreen);
			}
			break;
		case MotionEvent.ACTION_UP:
			mTouchState = TOUCH_EVENT_IDLE;
			AXLog.d(TAG, "TOUCH_EVENT_IDLE set 1.");
			break;
		case MotionEvent.ACTION_CANCEL:
			break;
		default:
			break;
		}

		AXLog.d(TAG, "onInterceptTouchEvent mTouchState = " + mTouchState
				+ ", mCurrentScreen = " + mCurrentScreen);
		// 如果是探测状态或是左右滚动状态，则劫持
		return (mTouchState == TOUCH_EVENT_SCROLL_H);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		final int action = ev.getAction();
		int x = (int) ev.getX();
		int y = (int) ev.getY();

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			if (!mScroller.isFinished()) {
				mScroller.forceFinished(true);
				mMoveOffset = mScroller.getFinalX();
			}
			mLastMoveX = x;
			mLastMoveY = y;
			break;
		case MotionEvent.ACTION_MOVE:
			if (ev.getPointerCount() == 1) {
				int deltaX = x - mLastMoveX;
				int move = 0;
				mLastMoveX = x;
				mLastMoveY = y;
				if (mCurrentScreen == SCREEN_TILE) {
					// 最大能移动mAppViewWidth的距离
					if (deltaX < 0) {
						// 手往左滑动，所以移动的距离是增加的。
						// 移动的距离是两都较小的值,因为最多只能移动mTileViewWidth这么宽
						move = Math.min(-deltaX, mTileViewWidth - mMoveOffset);
						// scrollBy(move, 0);
						scrollTo(getScrollX() + move, 0);
						// mMoveOffset += move;
					} else {
						// 手往右滑动，所以移动的距离是减少的。
						// 最多只能移动mMoveOffset这么长
						move = Math.min(deltaX, mMoveOffset);
						// scrollBy(-move, 0);
						scrollTo(getScrollX() - move, 0);
						// mMoveOffset -= move;
					}

				} else if (mCurrentScreen == SCREEN_APP) {
					// 最大能移动mAppViewWidth的距离
					if (deltaX > 0) {
						// 手往右滑动，所以移动的距离是减少的。
						// 移动的距离是两都较小的值,往左最多能移动 mAppViewWidth的距离
						move = Math.min(deltaX, mMoveOffset);
						// scrollBy(-move, 0);
						scrollTo(getScrollX() - move, 0);
						// mMoveOffset -= move;
					} else {
						// 手往左滑动，所以移动的距离是增加的。
						move = Math.min(-deltaX, mAppViewWidth - mMoveOffset);
						// scrollBy(move, 0);
						scrollTo(getScrollX() + move, 0);
						// mMoveOffset += move;
					}
				} else {
					AXLog.w(TAG, "Invalid screen = " + mCurrentScreen);
				}

			}
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			int lastScroll = 0;
			// 这时候要来判断应该是停留在哪个screen上了。
			// mMoveOffset 的最大值是mTileViewWidth，最小值是0
			// if(mMoveOffset)
			if (mMoveOffset <= mTileViewWidth / 2) {
				// mMoveOffset超过了mTileViewWidth的半，则要滚动到app screen
				//mCurrentScreen = SCREEN_TILE;
				setCurrentScreen(SCREEN_TILE);
				lastScroll = -mMoveOffset;
				mMoveOffset = 0;
			} else {
				//mCurrentScreen = SCREEN_APP;
				setCurrentScreen(SCREEN_APP);
				lastScroll = mTileViewWidth - mMoveOffset;
				mMoveOffset = mTileViewWidth;
			}
			// scrollBy(lastScroll, 0);
			mScroller.startScroll(getScrollX(), getScrollY(), lastScroll,
					getScrollY(), Math.abs(lastScroll) * 3);
			invalidate();
			mTouchState = TOUCH_EVENT_IDLE;
			AXLog.d(TAG, "TOUCH_EVENT_IDLE set.");
			break;
		default:
			break;
		}

		return (mTouchState == TOUCH_EVENT_SCROLL_H);
		// return mGesture.onTouchEvent(ev);
	}

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			postInvalidate();
		}
	}

	@Override
	public boolean onDown(MotionEvent e) {
		// return (mTouchState == TOUCH_EVENT_SCROLL_H);
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void scrollTo(int x, int y) {
		super.scrollTo(x, y);
		mMoveOffset = getScrollX();

		float a = mMoveOffset;
		a = a / mTileViewWidth;
		mChildButton.setAlpha(a);

		int velocityX = mMoveOffset - mLastScrollX;
		AXLog.d(TAG, "velocityX = " + velocityX);
		if (!mArrowAnimSet) {

			mArrowAnimSet = true;
			if (velocityX < 0
					&& mMoveOffset <= mChildButton.getWidth()) {
				// 从左向右
				if (mArrowRotateAnim == null) {
					mArrowRotateAnim = new DistanceInterpolator(0,
							mChildButton.getWidth(), mMoveOffset);
				}
				mChildButton.startArrowRotateAnim(AnimUtil.getArrowRotateAnim(
						false, mArrowRotateAnim));
			} else if (velocityX > 0 && mMoveOffset >= mTileViewWidth - mChildButton.getWidth()) {
				if (mArrowRotateAnim == null) {
					mArrowRotateAnim = new DistanceInterpolator(mTileViewWidth
							- mChildButton.getWidth(), mTileViewWidth,
							mMoveOffset);
				}
				mChildButton.startArrowRotateAnim(AnimUtil.getArrowRotateAnim(true,
						mArrowRotateAnim));
			}else {
				mArrowAnimSet = false;
			}
		}
		
		if (mArrowRotateAnim != null && mArrowAnimSet) {
			mArrowRotateAnim.updateCurrent(getScrollX());
		}

		if (mMoveOffset >= mTileViewWidth) {
			mArrowAnimSet = false;
			mChildButton.clearArrowRotateAnim();
			mChildButton.updateArrowImage(ButtonSpace.INDEX_ARROW_LEFT);
			mArrowRotateAnim = null;
		} else if (mMoveOffset <= 0) {
			mArrowAnimSet = false;
			mChildButton.clearArrowRotateAnim();
			mChildButton.updateArrowImage(ButtonSpace.INDEX_ARROW_RIGHT);
			mArrowRotateAnim = null;
		}

		// 用来判断滚动的方向
		mLastScrollX = mMoveOffset;
		
		if(getScrollX() <= mHitX) {
			if(mScrollFinished != null) {
				mScrollFinished.scrollFinish();
				mScrollFinished = null;
			}
		}
	}

	public void setScrollState(int state) {
		if (state < TOUCH_EVENT_IDLE || state > TOUCH_EVENT_SCROLL_V) {
			AXLog.w(TAG, "Unknow state = " + state);
		}
		mTouchState = state;
	}

	public int getScrollState() {
		return mTouchState;
	}

	public int getTouchSlop() {
		return mTouchSlop;
	}

	@Override
	public void setOnClickListener(OnClickListener l) {
		mChildTile.setOnClickListener(l);
		mChildButton.setOnClickListener(l);
		mChildApp.setOnClickListener(l);
	}

	@Override
	public void setOnLongClickListener(OnLongClickListener l) {
		mChildTile.setOnLongClickListener(l);
		// mChildButton.setOnClickListener(l);
		mChildApp.setOnLongClickListener(l);
	}

	public int getCurrentScreen() {
		return mCurrentScreen;
	}

	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		computeScroll();
		mDragController.setWindowToken(getWindowToken());
	}

	public void scrollToScreen(int screen) {
		if (screen == SCREEN_APP) {
			mScroller.startScroll(getScrollX(), getScrollY(), mTileViewWidth,
					getScrollY(), (int) Math.abs(mTileViewWidth * 1.5));
		} else if (screen == SCREEN_TILE) {
			mScroller.startScroll(getScrollX(), getScrollY(), -mTileViewWidth,
					getScrollY(), (int) Math.abs(mTileViewWidth * 1.5));
		}
		mCurrentScreen = screen;
		invalidate();
	}
	
	public void setOnScrollFinishListener(OnScrollFinish l, int hitX) {
		mHitX = hitX;
		// mHitY = hitY;
		mScrollFinished = l;
	}
	
	public void setAppSpaceMode(int mode) {
		if(mode == AppSpace.MODE_SEARCH) {
			mChildButton.setVisibility(GONE);
			mChildApp.setMode(mode, null, false);
			mChildAppClass.setMode(mode, false);
			mChildAppClass.setTextWatcher(mChildApp);
		}else {
			mChildButton.setVisibility(VISIBLE);
			mChildApp.setMode(mode, null, false);
			mChildAppClass.setMode(mode, false);
		}
		mAppSpaceMode  = mode;
		
		requestLayout();
	}

	public int getAppMode() {
		return mAppSpaceMode;
	}
}
