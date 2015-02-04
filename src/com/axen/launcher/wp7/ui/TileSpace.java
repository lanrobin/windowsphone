package com.axen.launcher.wp7.ui;

import java.util.Random;

import com.axen.launcher.app.TileItemInfo;
import com.axen.launcher.app.TileManager;
import com.axen.launcher.wp7.main.R;
import com.axen.launcher.wp7.main.WP7App;
import com.axen.launcher.wp7.main.WP7Configuration;
import com.axen.launcher.wp7.ui.anim.AnimUtil;
import com.axen.launcher.wp7.ui.anim.Rotate3DAnimation;
import com.axen.launcher.wp7.ui.size.Size;
import com.axen.launcher.wp7.ui.widget.CommonTile;
import com.axen.launcher.wp7.ui.widget.Tile;
import com.axen.utils.AXLog;
import com.axen.utils.ResourceUtil;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.Interpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Scroller;

public class TileSpace extends ViewGroup implements LoadedCallback,
		GestureDetector.OnGestureListener, DragController.DragListener {

	private static final String TAG = "TileSpace";

	private int mViewWidth = 0;
	private int mViewHeight = 0;
	private int mTileWidth = 0;
	private int mTileHeight = 0;
	private int mTileSpace = 0;
	private int mTileLeftSpace = 0;
	private int mTileEditLeftSpace = 0;
	private int mShrinkMountX = 0;
	private int mShrinkMountY = 0;
	private int mTopMargin = 0;
	private int mTileEditWidth = 0;
	private int mTileEditHeight = 0;
	private int mTileRow = 0;
	private int mEditTileCenterY = 0;

	private Tile mDragView = null;

	private WP7Configuration mConf = WP7Configuration.getInstance();
	private ResourceUtil mru = mConf.getRU();
	private Size size = Size.get();

	private WorkSpace mWorkspace = null;

	private TileManager mTM = TileManager.getInstance();

	private int mLastMoveY = 0;

	private int mUpExceedMount = 0;
	private int mDownExceedMount = 0;
	private int mMoveOffset = 0;

	private boolean mArrowJumped = false;

	private int mTouchState = WorkSpace.TOUCH_EVENT_IDLE;

	private Scroller mScroller;
	private GestureDetector mGesture = null;

	private View.OnClickListener mClickListener = null;
	private View.OnLongClickListener mLongClickListener = null;

	private final static int MAX_EXCEED_MOVE_MOUNT = 100;

	public static final int STATE_NORMAL = 0;
	public static final int STATE_EDIT = 1;

	private int mState = STATE_NORMAL;

	private Interpolator mScrollInterplator = new Interpolator() {

		public float getInterpolation(float t) {
			// return (float) Math.sqrt(t);
			return (float) Math.pow(t, 1 / Math.E);
		}
	};

	public static interface OnLayoutFinish {
		void finish();
	}

	private OnLayoutFinish mOnLayoutFinish = null;

	public TileSpace(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		mScroller = new Scroller(context, mScrollInterplator);

		mGesture = new GestureDetector(context, this);

		setState(STATE_NORMAL, false, null, 0);

		// mViewHeigth = 2 * mTopMargin;

		/**
		 * 测试用。 testTiles();
		 */

	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		setBackgroundColor(mConf.getBackgroundColor());
		// this.setDrawingCacheBackgroundColor(mConf.getBackgroundColor());
		// this.setDrawingCacheEnabled(false);
		// this.setAnimationCacheEnabled(false);
	}

	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
		setBackgroundColor(mConf.getBackgroundColor());
	}

	public TileSpace(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public TileSpace(Context context) {
		this(context, null);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		for (int i = 0; i < getChildCount(); i++) {
			getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
		}

		/**
		 * int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec); int
		 * widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
		 * 
		 * int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec); int
		 * heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
		 */
		mViewHeight = 2 * mTopMargin + (mTM.getMaxY() + 1)
				* (mTileHeight + mTileSpace) - mTileSpace;

		setMeasuredDimension(mViewWidth, mViewHeight);
		/**
		 * AXLog.d(TAG, "mViewHeight = " + mViewHeight + ", mViewWidth = " +
		 * mViewWidth);
		 */
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		AXLog.d(TAG, "onLayout");
		if (mState == STATE_NORMAL) {
			onTileLayout(changed, l, t, r, b);
		} else if (mState == STATE_EDIT) {
			onEditTileLayout(changed, l, t, r, b);
		}
	}

	// 开始排列Tile
	private void onTileLayout(boolean changed, int l, int t, int r, int b) {
		int w = 0;
		int h = 0;
		int top = mTopMargin;
		int space = mTileSpace;
		int left = mTileLeftSpace;

		if (!mConf.getShowStatusbar()) {
			top -= mConf.getStatusBarHeight();
		}

		mLastMoveY = mTM.getMaxY();
		// mShrinkMountY = (mTileHeight - mTileEditHeight) * mLastMoveY / 2;

		int count = getChildCount();
		// top -= (h + space);
		mTileRow = 0;
		View child = null;
		Tile tile = null;
		TileItemInfo tii = null;
		for (int i = 0; i < count; i++) {
			child = getChildAt(i);
			if (!(child instanceof Tile)) {
				AXLog.w(TAG, "Unsupported child.");
				throw new IllegalArgumentException(
						"Unsupported child, must be child of Tile.");
			}

			left = mTileLeftSpace;

			tile = (Tile) child;
			tii = tile.getTii();
			// 计算宽度和高度
			w = mTileWidth;
			h = mTileHeight;

			// 如果是宽的 tile,则是两个tile 加上中间间隔的宽度
			if (tii.wideTile) {
				w = 2 * w + space;
			}

			Rect rect = tii.rect;
			rect.left = left + (w + space) * tii.x;
			rect.top = top + (h + space) * tii.y;
			rect.right = left + (w + space) * tii.x + w;
			rect.bottom = top + (h + space) * tii.y + h;
			child.layout(rect.left, rect.top, rect.right, rect.bottom);
			child.requestLayout();
			/**
			 * // 第一个 if (i % 2 == 0) {
			 * 
			 * child.layout(left, top, left + w, top + h); } else {
			 * child.layout(left + w + space, top, left + 2 * w + space, top +
			 * h); }
			 */
		}

		// 只调用一次，下次调用再设置
		if (mOnLayoutFinish != null) {
			mOnLayoutFinish.finish();
			mOnLayoutFinish = null;
		}
	}

	// 开始排列Tile
	private void onEditTileLayout(boolean changed, int l, int t, int r, int b) {
		// 在这里模式下，以被drag的view为中心
		int centerY = getScrollY() + mEditTileCenterY + mTileHeight / 2;
		TileItemInfo tii = null;
		TileItemInfo tiiDrag = mDragView.getTii();
		Tile tile = null;
		Rect rect = null;
		int top = 0;
		int left = mTileLeftSpace + mShrinkMountX;
		int minTop = mViewHeight;
		for (int i = 0; i < getChildCount(); i++) {
			tile = (Tile) getChildAt(i);
			tii = tile.getTii();
			rect = tii.rect;
			if (tii.y < tiiDrag.y) {

				top = centerY - (mTileEditHeight + mTileSpace)
						* (tiiDrag.y - tii.y) - mTileEditHeight / 2;
				rect.left = left + (mTileEditWidth + mTileSpace) * tii.x;
				rect.top = top;
				rect.right = left + (mTileEditWidth + mTileSpace) * tii.x
						+ mTileEditWidth;
				rect.bottom = top + mTileEditHeight;

			} else if (tii.y > tiiDrag.y) {
				top = centerY + (mTileEditHeight + mTileSpace)
						* (tii.y - tiiDrag.y) - mTileEditHeight / 2;
				rect.left = left + (mTileEditWidth + mTileSpace) * tii.x;
				rect.top = top;
				rect.right = left + (mTileEditWidth + mTileSpace) * tii.x
						+ mTileEditWidth;
				rect.bottom = top + mTileEditHeight;
			} else {
				// 如果与自己是同一行，
				if (!tile.equals(mDragView)) {
					top = centerY - mTileEditHeight / 2;
					rect.left = left + (mTileEditWidth + mTileSpace) * tii.x;
					rect.top = top;
					rect.right = left + (mTileEditWidth + mTileSpace) * tii.x
							+ mTileEditWidth;
					rect.bottom = top + mTileEditHeight;
				} else {
					top = centerY - mTileHeight / 2;
					rect.left = left + (mTileWidth + mTileSpace) * tiiDrag.x;
					rect.top = top;
					rect.right = left + (mTileWidth + mTileSpace) * tiiDrag.x
							+ mTileWidth;
					rect.bottom = top + mTileHeight;
					// 如果是自己，隐藏
					tile.setVisibility(GONE);
					top = centerY - mTileEditHeight / 2; // 恢复成正常的
				}
			}
			if (minTop > top) {
				minTop = top;
			}

			if (tii.wideTile) {
				rect.right += (mTileWidth + mTileSpace);
			}

			tile.layout(rect.left, rect.top, rect.right, rect.bottom);
		}
		mTopMargin = minTop;
		// 只调用一次，下次调用再设置
		if (mOnLayoutFinish != null) {
			mOnLayoutFinish.finish();
			mOnLayoutFinish = null;
		}
	}

	@Override
	public void addView(View child) {
		if (child instanceof Tile) {
			super.addView(child);
			child.setOnClickListener(mClickListener);
			child.setOnLongClickListener(mLongClickListener);
		} else {
			AXLog.d(TAG, "Only view of Tile can be added.");
		}
	}

	@Override
	public void requestLayout() {
		super.requestLayout();
	}

	public void setWorkspace(WorkSpace ws) {
		mWorkspace = ws;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		int action = ev.getAction();
		int y = (int) ev.getY();

		if (action == MotionEvent.ACTION_DOWN) {
			mLastMoveY = y;
		}

		if (mWorkspace.getCurrentScreen() != WorkSpace.SCREEN_TILE) {
			// 如果当前不是Tile space，则不管。
			return false;
		}

		// return super.onInterceptTouchEvent(ev);
		// AXLog.d(TAG, "onInterceptTouchEvent mTouchState = " + mTouchState);
		return mTouchState == WorkSpace.TOUCH_EVENT_SCROLL_V;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		int action = ev.getAction();
		int y = (int) ev.getY();
		int x = (int) ev.getX();

		if (mWorkspace.getCurrentScreen() != WorkSpace.SCREEN_TILE) {
			// 如果当前不是Tile space，则不管。
			return false;
		}

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			mLastMoveY = y;
			break;
		case MotionEvent.ACTION_MOVE:

			int yDelta = mLastMoveY - y;
			int move = 0;
			mLastMoveY = y;
			/*
			 * AXLog.d(TAG, "x = " + x + ", y = " + y + ", yDelta = " + yDelta);
			 * AXLog.d(TAG, "mDownExceedMount = " + mDownExceedMount +
			 * ", mUpExceedMount = " + mUpExceedMount); AXLog.d(TAG,
			 * "mMoveOffset = " + mMoveOffset + ", mViewHeigth = " +
			 * mViewHeight);
			 */
			if (yDelta > 0) {
				// 向下滑动
				if (mDownExceedMount <= 0) {
					// 还没有滚到头
					if (mViewHeight - mConf.getUsableHeight() > mMoveOffset) {
						// 还没滚到头
						move = Math.min(mViewHeight - mMoveOffset, yDelta);
						mMoveOffset += move;
						scrollBy(0, move);
					} else {
						// 开始跳动的动画
						// this.setVisibility(GONE);
						// mWorkspace.mChildButton.startArrorJumpAnim();
						// 滚到头了，然后要记录回弹量,接着往下滚
						if (mDownExceedMount <= MAX_EXCEED_MOVE_MOUNT) {
							move = yDelta / 2;
							move = Math.min(MAX_EXCEED_MOVE_MOUNT
									- mDownExceedMount, move);
							mDownExceedMount += move;
							scrollBy(0, move);
						}

						// 肯定已经滚到最大的数了
						mMoveOffset = mViewHeight - mConf.getUsableHeight();
					}
				} else if (mDownExceedMount > 0) {
					// 如果还没有滚到最大能滚的量，则继续滚。
					if (mDownExceedMount <= MAX_EXCEED_MOVE_MOUNT) {
						move = yDelta / 2;
						move = Math.min(MAX_EXCEED_MOVE_MOUNT
								- mDownExceedMount, move);
						mDownExceedMount += move;
						scrollBy(0, move);
					}
				}
			} else if (yDelta < 0) {
				// 向上
				if (mUpExceedMount <= 0) {
					if (mMoveOffset > 0) {
						move = Math.min(mMoveOffset, -yDelta);
						mMoveOffset -= move;
						scrollBy(0, -move);
					} else {
						if (mUpExceedMount <= MAX_EXCEED_MOVE_MOUNT) {
							move = -yDelta / 2;
							move = Math.min(MAX_EXCEED_MOVE_MOUNT
									- mUpExceedMount, move);
							mUpExceedMount += move;
							scrollBy(0, -move);
						}
						mMoveOffset = 0;
					}
				} else {
					if (mUpExceedMount <= MAX_EXCEED_MOVE_MOUNT) {
						move = -yDelta / 2;
						move = Math.min(MAX_EXCEED_MOVE_MOUNT - mUpExceedMount,
								move);
						mUpExceedMount += move;
						scrollBy(0, -move);
					}
				}
			}

			break;
		case MotionEvent.ACTION_UP:
			if (mUpExceedMount > 0) {
				// scrollBy(0, mUpExceedMount);
				mScroller.startScroll(getScrollX(), getScrollY(), 0,
						mUpExceedMount, 800);
				// invalidate();
				mUpExceedMount = 0;
			}

			if (mDownExceedMount > 0) {
				// scrollBy(0, -mDownExceedMount);
				mScroller.startScroll(getScrollX(), getScrollY(), 0,
						-mDownExceedMount, 800);
				// invalidate();
				mDownExceedMount = 0;
			}
			mTouchState = WorkSpace.TOUCH_EVENT_IDLE;
			mWorkspace.setScrollState(mTouchState);
			break;
		case MotionEvent.ACTION_CANCEL:
			break;
		default:
			break;
		}

		return mGesture.onTouchEvent(ev) && mState != STATE_EDIT;
	}

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			scrollTo(0, mScroller.getCurrY());
			postInvalidate();
		}
	}

	@Override
	public boolean onDown(MotionEvent e) {
		if (!mScroller.isFinished()) {
			mScroller.forceFinished(true);
		}
		return true;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		if (mDownExceedMount == 0 && mUpExceedMount == 0) {
			// int slow = (int) (velocityY * 0.75);
			int slow = -(int) (velocityY * 0.618);
			mScroller.fling(0, mMoveOffset, 0, slow, 0, 0, 0, mViewHeight
					- mConf.getUsableHeight());
			mMoveOffset = mScroller.getFinalY();
			computeScroll();
		}
		return true;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
		AXLog.d(TAG, "onLongPress");

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

	public void setTouchState(int state) {
		mTouchState = state;
	}

	@Override
	public void setOnClickListener(OnClickListener l) {
		mClickListener = l;
		for (int i = 0; i < getChildCount(); i++) {
			getChildAt(i).setOnClickListener(l);
		}
	}

	@Override
	public void setOnLongClickListener(OnLongClickListener l) {
		mLongClickListener = l;
		for (int i = 0; i < getChildCount(); i++) {
			getChildAt(i).setOnLongClickListener(l);
		}
	}

	@Override
	public void scrollBy(int x, int y) {
		AXLog.d(TAG, "scroll x:" + x + ", y:" + y);
		super.scrollBy(x, y);
	}

	public void setState(int state, boolean requstLayout, Tile dragView,
			int centerY) {
		mState = state;
		mEditTileCenterY = centerY;
		if (mState == STATE_NORMAL) {
			stopAnimation();
		} else if (mState == STATE_EDIT) {
			startAnimation(dragView);
		} else {
			AXLog.w(TAG, "Unkown state = " + state);
		}

		mViewWidth = size.getTile_space_w(); //mru.getPexil(R.dimen.tile_space_w);
		mTileWidth = size.getTile_w(); //mru.getPexil(R.dimen.tile_w);
		mTileHeight = size.getTile_h(); //mru.getPexil(R.dimen.tile_h);
		mTileSpace = size.getTiles_space(); //mru.getPexil(R.dimen.tiles_space);
		mTopMargin = size.getTop_margin_h(); //mru.getPexil(R.dimen.top_margin_h);
		mTileEditWidth = size.getTile_edit_w(); //mru.getPexil(R.dimen.tile_edit_w);
		mTileEditHeight = size.getTile_edit_h(); //mru.getPexil(R.dimen.tile_edit_h);
		mTileLeftSpace = size.getTile_left_space(); //mru.getPexil(R.dimen.tile_left_space);
		mShrinkMountX = mTileWidth - mTileEditWidth;

		mDragView = dragView;

		setState(state);

		if (mDragView != null) {
			((Tile) mDragView).setState(TileSpace.STATE_NORMAL);
		}
		if (requstLayout) {
			requestLayout();
		}
	}

	private void startAnimation(View dragView) {
		View v = null;
		Random r = new Random();
		Animation anim = null; // AnimUtil.getEditTileAnim(getContext(),
								// R.anim.tile_shake_and_dim);
		AnimationSet set = null;
		// Animation a = null;
		long offset = 0;
		for (int i = 0; i < getChildCount(); i++) {
			set = (AnimationSet) AnimUtil.getEditTileAnim(getContext(),
					R.anim.tile_shake_and_dim);
			set.setRepeatCount(Integer.MAX_VALUE);
			set.setRepeatMode(Animation.RESTART);
			offset = r.nextLong() % 1500;
			for (Animation child : set.getAnimations()) {
				if (child instanceof AnimationSet) {
					child.setStartOffset(offset);
				}
			}
			// anim.setStartOffset();
			v = getChildAt(i);
			if (true) {
				int[] loc = new int[2];
				v.getLocationOnScreen(loc);
				AXLog.d(TAG, "view isShown = " + v.isShown());
				AXLog.d(TAG, "y = " + ((Tile) v).getTii().y);
				AXLog.d(TAG, "loc[0] = " + loc[0] + ", loc[1] =" + loc[1]);
			}
			if (!v.equals(dragView)) {
				v.startAnimation(set);
			}
		}
	}

	private void stopAnimation() {
		Animation a = null;
		View v = null;
		for (int i = 0; i < getChildCount(); i++) {
			v = getChildAt(i);
			// a = v.getAnimation();
			// v.setAnimation(null);
			v.clearAnimation();
			// if(a != null) {
			// a.cancel();
			// }
		}
	}

	public Tile getDragView() {
		return mDragView;
	}

	public void clearDragView() {
		mDragView = null;
	}

	private void setState(int state) {
		for (int i = 0; i < getChildCount(); i++) {
			Tile v = (Tile) getChildAt(i);
			if (v == mDragView) {
				continue;
			}
			v.setState(state);
		}
	}

	public int getState() {
		return mState;
	}

	public int getTileW() {
		return mTileWidth;
	}

	public int getTileH() {
		return mTileHeight;
	}

	public int getEditTileW() {
		return mTileEditWidth;
	}

	public int getEditTileH() {
		return mTileEditHeight;
	}

	public void setOnLayoutFinish(OnLayoutFinish l) {
		mOnLayoutFinish = l;
	}

	@Override
	public void onDragDrop(int x, int y) {
		AXLog.d(TAG, "onDragDrop x = " + x + ", y = " + y);
		if (mDragView == null) {
			AXLog.d(TAG, "mDragView is null.");
			return;
		}
		TileItemInfo tii = mDragView.getTii();
		TileManager.CellInfo info = mTM.getCellInfo(x, y + mMoveOffset,
				mTopMargin, mTileEditLeftSpace, mTileSpace, mTileEditWidth,
				mTileEditHeight, tii.wideTile);

		int oldX = tii.x;
		int oldY = tii.y;
		if (info.equals(TileManager.HEAD)) {
			AXLog.d(TAG, "Drag to the head.");
			tii.x = TileManager.HEAD.x;
			tii.y = 0;
		} else if (info.equals(TileManager.REAR)) {
			AXLog.d(TAG, "Drag to the tail.");
			tii.x = TileManager.REAR.x;
			tii.y = mTM.getMaxY() + 1;
		} else {
			AXLog.d(TAG, "Drag to x = " + info.x + ", y = " + info.y);
			if (tii.equals(info.tii)) {
				// 如果是自己，不做任何变化
				return;
			} else {
				tii.x = info.x;
				tii.y = info.y;
			}
		}

		mTM.updateTii(tii, oldX, oldY);

		// 更新 mEditTileCenterY
		mEditTileCenterY = y;
		requestLayout();
	}

	@Override
	public void onDragOver(int x, int y) {
		AXLog.d(TAG, "onDragOver x = " + x + ", y = " + y);
	}

	private View getDropView(int x, int y) {
		View v = null;
		Rect rect = new Rect();
		for (int i = 0; i < getChildCount(); i++) {
			v = getChildAt(i);
			v.getHitRect(rect);
			y += mMoveOffset;

			if (v.equals(mDragView)) {
				v = null;
			}
		}
		return v;
	}

	@Override
	public void scrollTo(int x, int y) {
		super.scrollTo(x, y);
		if (getScrollY() < mViewHeight - mConf.getUsableHeight()) {
			mArrowJumped = false;
		} else {
			if (!mArrowJumped) {
				mArrowJumped = true;
				mWorkspace.mChildButton.startArrorJumpAnim();
			}
		}
		mMoveOffset = getScrollY();
	}

	public void scrollToBottom() {
		int scrollY = mViewHeight - mConf.getUsableHeight() - getScrollY()
				+ mTopMargin;
		// scrollTo(getScrollX(), scrollY > 0? scrollY:getScrollY());
		if (scrollY > 0) {
			mScroller.startScroll(getScrollX(), getScrollY(), getScrollX(),
					scrollY, (int) (Math.log(scrollY) * 60));
			invalidate();
		}
	}

	public void scrollToTop() {
		int scrollY = getScrollY();
		// scrollTo(getScrollX(), scrollY > 0? scrollY:getScrollY());
		if (scrollY > 0) {
			mScroller.startScroll(getScrollX(), getScrollY(), getScrollX(),
					-scrollY, (int) (Math.log(scrollY) * 60));
			invalidate();
		}
	}

	@Override
	public void onDragStart(int dragViewCenterX, int dragViewCenterH) {
		// TODO Auto-generated method stub

	}

	public void startEndAnim(Rotate3DAnimation anim, View current,
			Animation.AnimationListener l, int totalDelay) {
		Tile t = null;
		TileItemInfo tii = null;
		Rotate3DAnimation a = null;
		int animDelay = 150;
		for (int i = 0; i < getChildCount(); i++) {
			try {
				a = anim.clone();
			} catch (CloneNotSupportedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (a == null) {
				continue;
			}
			t = (Tile) getChildAt(i);
			tii = t.getTii();

			if (t.equals(current)) {
				a.setStartOffset(animDelay * 2);
				a.setAnimationListener(l);
				t.startAnimation(a);
			} else {
				a.setStartOffset((1 - tii.x) * animDelay + totalDelay);
				t.startAnimation(a);
			}
		}
	}

	public void clearChildAnimation() {
		for (int i = 0; i < getChildCount(); i++) {
			getChildAt(i).clearAnimation();
		}
	}

	public void pauseBackgroundThread() {
		for (int i = 0; i < getChildCount(); i++) {
			((Tile) getChildAt(i)).pauseBackgroundThread();
		}
	}

	public void resumeBackgroundThread() {
		for (int i = 0; i < getChildCount(); i++) {
			((Tile) getChildAt(i)).resumeBackgroundThread();
		}
	}
}
