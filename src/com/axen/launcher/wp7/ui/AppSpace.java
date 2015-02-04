package com.axen.launcher.wp7.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import com.axen.launcher.app.AppManager;
import com.axen.launcher.app.AppManager.AppItem;
import com.axen.launcher.wp7.main.R;
import com.axen.launcher.wp7.main.WP7Configuration;
import com.axen.launcher.wp7.ui.anim.Rotate3DAnimation;
import com.axen.launcher.wp7.ui.apputil.ClassifyApp.AppClass;
import com.axen.launcher.wp7.ui.size.Size;
import com.axen.launcher.wp7.ui.widget.AppClassView;
import com.axen.launcher.wp7.ui.widget.AppItemView;
import com.axen.utils.AXLog;
import com.axen.utils.ResourceUtil;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.widget.Scroller;

public class AppSpace extends ViewGroup implements LoadedCallback,
		OnGestureListener, TextWatcher {

	private static final String TAG = "AppSpace";
	
	private WP7Configuration mConf = WP7Configuration.getInstance();
	private ResourceUtil mru = mConf.getRU();
	private Size size = Size.get();

	private WorkSpace mWorkspace = null;

	private Scroller mScroller;
	private GestureDetector mGesture = null;
	private OnClickListener mOnClickListener = null;
	private OnLongClickListener mOnLongClickListener = null;
	private int mLastMoveY = 0;

	private int mUpExceedMount = 0;
	private int mDownExceedMount = 0;
	private int mMoveOffset = 0;

	private AppManager mAppManager = null;
	private List<AppClass> mAppClasses = null;
	private View mCurrentView = null;
	private int mMode = MODE_NORMAL;
	private AppClassSpace mAppClasseSpace = null;

	private int mViewWidth = 0;
	private int mViewHeight = 0;
	private int mSearchModeItemLeftSpace = 0;
	private int mLastScrollY = 0;
	private int mMarginButton = 0;
	private int mAppIconH = 0;

	private int mTouchState = WorkSpace.TOUCH_EVENT_IDLE;

	private final static int MAX_EXCEED_MOVE_MOUNT = 100;

	public final static int MODE_NORMAL = 0;
	public final static int MODE_MENU = 1;
	public final static int MODE_SEARCH = 2;

	private int mContextMenuRightMovement = 0;

	private Handler mHandler = new Handler();

	private Interpolator mScrollInterplator = new Interpolator() {

		public float getInterpolation(float t) {
			// return (float) Math.sqrt(t);
			return (float) Math.pow(t, 1 / Math.E);
		}
	};

	public AppSpace(Context context, AttributeSet attrs) {
		super(context, attrs);
		// testView();
		mScroller = new Scroller(context, mScrollInterplator);

		mGesture = new GestureDetector(context, this);

		mMarginButton = size.getTop_margin_h(); //mru.getPexil(R.dimen.top_margin_h);
		mViewWidth = size.getApp_space_w(); //mru.getPexil(R.dimen.app_space_w);
		mAppIconH = size.getApp_icon_h(); //mru.getPexil(R.dimen.app_icon_h);
		mAppManager = AppManager.getInstance();

		mContextMenuRightMovement = size.getApp_context_menu_right_movement(); // mru.getPexil(R.dimen.app_context_menu_right_movement);

		mSearchModeItemLeftSpace = size.getSearch_left_space(); //mru.getPexil(R.dimen.search_left_space);

		// allAppView();
	}

	public AppSpace(Context context) {
		this(context, null);
	}
	
	

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		setBackgroundColor(mConf.getBackgroundColor());
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		AXLog.d(TAG, "widthMeasureSpec = " + widthMeasureSpec
				+ ", heightMeasureSpec = " + heightMeasureSpec);

		int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);

		int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

		View child = null;
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			child = getChildAt(i);
			child.measure(widthMeasureSpec, heightMeasureSpec);
		}

		setMeasuredDimension(mViewWidth, mViewHeight);
		AXLog.d(TAG, "widthSpecSize = " + widthSpecSize + ", heightSpecSize = "
				+ heightSpecSize + ", measuredW = " + mViewWidth);

		// setMeasuredDimension(measuredW, heightSpecSize);
		// super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		onAppLayout(changed, l, t, r, b);
	}
	
	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
		setBackgroundColor(mConf.getBackgroundColor());
	}

	private void onAppLayout(boolean changed, int l, int t, int r, int b) {
		// int top = 0; // mru.getPexil(R.dimen.top_margin_h);
		int left = 0; // mru.getPexil(R.dimen.app_left_space);
		// int w = mru.getPexil(R.dimen.app_icon_w);
		int space = size.getApp_icon_space(); //mru.getPexil(R.dimen.app_icon_space);
		int w = getWidth();
		// int h = mru.getPexil(R.dimen.app_icon_h);
		int top = 0;
		mViewHeight = mMarginButton;
		if (mMode == MODE_MENU) {
			left = mContextMenuRightMovement;
		}
		AXLog.d(TAG, "onAppLayout mode = " + mMode);
		int count = getChildCount();
		View child = null;
		AppClass ac = null;
		for (int i = 0; i < count; i++) {
			child = getChildAt(i);

			if (child.getVisibility() != VISIBLE) {
				continue;
			}

			if (child instanceof AppClassView) {
				ac = ((AppClassView) child).getAppClass();
				ac.screenOffset = top;
			}

			if (mMode == MODE_MENU) {
				if (child.equals(mCurrentView)) {
					left = 0;
				} else {
					left = mContextMenuRightMovement;
				}
			} else if (mMode == MODE_SEARCH) {
				left = mSearchModeItemLeftSpace;
			}
			child.layout(left, top, left + w, top + mAppIconH);

			top += mAppIconH + space;
		}

		mViewHeight += (top + mMarginButton);
	}

	public void setWorkspace(WorkSpace ws) {
		mWorkspace = ws;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		int action = ev.getAction();
		int y = (int) ev.getY();

		if (action == MotionEvent.ACTION_DOWN) {
			mLastMoveY = y;
			if (!mScroller.isFinished()) {
				mScroller.forceFinished(true);
			}
		}

		if (mWorkspace.getCurrentScreen() != WorkSpace.SCREEN_APP) {
			// 如果当前不是App space，则不管�?
			return false;
		}
		AXLog.d(TAG, "onInterceptTouchEvent mTouchState = " + mTouchState);
		return mTouchState == WorkSpace.TOUCH_EVENT_SCROLL_V;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		int action = ev.getAction();
		int y = (int) ev.getY();
		int x = (int) ev.getX();

		if (mWorkspace.getCurrentScreen() != WorkSpace.SCREEN_APP) {
			// 如果当前不是Tile space，则不管
			return false;
		} else if (mMode == MODE_MENU) {
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

			AXLog.d(TAG, "x = " + x + ", y = " + y + ", yDelta = " + yDelta);
			AXLog.d(TAG, "mDownExceedMount = " + mDownExceedMount
					+ ", mUpExceedMount = " + mUpExceedMount);
			AXLog.d(TAG, "mMoveOffset = " + mMoveOffset + ", mViewHeigth = "
					+ mViewHeight);
			if (yDelta > 0) {
				// 向下滑动
				if (mDownExceedMount <= 0) {
					// 还没有滚到头
					if (mViewHeight - mConf.getUsableHeight() > mMoveOffset) {
						// 还没滚到�?
						move = Math.min(mViewHeight - mMoveOffset, yDelta);
						mMoveOffset += move;
						scrollBy(0, move);
					} else {
						// 滚到头了，然后要记录回弹�?接着往下滚
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
					// 如果还没有滚到最大能滚的量，则继续滚�?
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
				mScroller.startScroll(getScrollX(), getScrollY(), getScrollX(),
						mUpExceedMount, (int) (Math.log(mUpExceedMount) * 60));
				invalidate();
				invalidate();
				mUpExceedMount = 0;
			}

			if (mDownExceedMount > 0) {
				// scrollBy(0, -mDownExceedMount);
				mScroller.startScroll(getScrollX(), getScrollY(), getScrollX(),
						-mDownExceedMount,
						(int) (Math.log(-mDownExceedMount) * 60));
				invalidate();
				mDownExceedMount = 0;
			}

			if (mScroller.isFinished()) {
				mTouchState = WorkSpace.TOUCH_EVENT_IDLE;
				mWorkspace.setScrollState(mTouchState);
			}
			// mScroller
			break;
		case MotionEvent.ACTION_CANCEL:
			break;
		default:
			break;
		}

		return mGesture.onTouchEvent(ev);
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
			//int slow = -(int) (velocityY * 0.25);
			int slow = -(int)(velocityY * 0.618);
			mScroller.fling(0, mMoveOffset, 0, slow, 0, 0, 0, mViewHeight
					- mConf.getUsableHeight());
			mMoveOffset = mScroller.getFinalY();
			if (slow <= 0) {
				mTouchState = WorkSpace.TOUCH_EVENT_IDLE;
				mWorkspace.setScrollState(mTouchState);
			}
			computeScroll();
		}
		return true;
	}

	@Override
	public void onLongPress(MotionEvent e) {

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
		for (int i = 0; i < getChildCount(); i++) {
			getChildAt(i).setOnClickListener(l);
		}
		mOnClickListener = l;
	}

	@Override
	public void scrollTo(int x, int y) {
		AXLog.d(TAG, "scrollTo( x= " + x + ", y = " + y + ")");
		super.scrollTo(x, y);
		mMoveOffset = getScrollY();

		/**
		 * 下面来检查是不是滚动到分类view,如果是，则要把分类space里也滚相应的位置。 如果是在SEARCH状态下，则不滚
		 */
		if (mMode != MODE_SEARCH) {
			mAppClasseSpace.rolling(mMoveOffset, mLastScrollY, mViewHeight);
		}
		mLastScrollY = mMoveOffset;
	}

	@Override
	public void setOnLongClickListener(OnLongClickListener l) {
		for (int i = 0; i < getChildCount(); i++) {
			getChildAt(i).setOnLongClickListener(l);
		}
		mOnLongClickListener = l;
	}

	/**
	 * 设置AppSpace的模式，只有在MODE_MENU模式下才需要设置v, 其它情况下设置为null.
	 * 
	 * @param mode
	 * @param v
	 * @param requestLayout
	 */
	public void setMode(int mode, View v, boolean requestLayout) {
		mMode = mode;
		mCurrentView = v;
		View child = null;
		if (mMode == MODE_SEARCH) {
			// 只有search模式下要滚到头
			scrollToTop(false);
		}
		for (int i = 0; i < getChildCount(); i++) {
			child = getChildAt(i);
			if (child instanceof AppClassView) {
				if (mMode == MODE_SEARCH) {
					// 在search模式下，所有的AppClass view 都要设置为GONE
					child.setVisibility(GONE);
				} else if (mMode == MODE_NORMAL) {
					child.setVisibility(VISIBLE);
				}
			}
		}

		if (requestLayout) {
			requestLayout();
		}
	}

	public void scrollToBottom() {
		int scrollY = mViewHeight - mConf.getUsableHeight();
		// scrollTo(getScrollX(), scrollY > 0? scrollY:getScrollY());
		if (scrollY > 0) {
			mScroller.startScroll(getScrollX(), getScrollY(), 0, scrollY,
					(int) (Math.log(scrollY) * 60));
			invalidate();
		}
	}

	public void scrollToTop(boolean useAnimation) {
		int scrollY = getScrollY();
		// scrollTo(getScrollX(), scrollY > 0? scrollY:getScrollY());
		if (scrollY > 0) {
			if (useAnimation) {
				mScroller.startScroll(getScrollX(), getScrollY(), 0, -scrollY,
						(int) (Math.log(scrollY) * 60));
			} else {
				scrollTo(getScrollX(), 0);
			}
			invalidate();
		}
	}

	public void setAppClasses(List<AppClass> acs) {
		removeAllViews();
		mAppClasses = acs;
		AppItemView v = null;
		AppClassView acv = null;
		AppClass ac = null;
		ResolveInfo ri = null;
		int count = 0;
		final Vector<Runnable> loadIcon = new Vector<Runnable>();
		final LayoutInflater inflater = LayoutInflater.from(getContext());
		final PackageManager pm = getContext().getPackageManager();
		for (int i = 0; i < mAppClasses.size(); i++) {
			ac = mAppClasses.get(i);
			if (ac.size() > 0) {
				acv = new AppClassView(getContext(), ac);
				acv.setOnLongClickListener(mOnLongClickListener);
				acv.setOnClickListener(mOnClickListener);
				addView(acv);
				count++;

				for (int j = 0; j < ac.size(); j++) {
					ri = ac.get(j);
					v = (AppItemView) inflater.inflate(R.layout.app_item_view,
							null);
					v.setImageBackgroundColor(mConf.getAccentColor());
					final AppItemView fv = v;
					final ResolveInfo fri = ri;
					loadIcon.add(new Runnable() {
						public void run() {
							final Drawable d = fri.loadIcon(pm);
							mHandler.post(new Runnable() {
								public void run() {
									fv.setImage(d);
								}
							});
							
						}
					});
					CharSequence cs = ri.loadLabel(pm);
					v.setText(cs);
					v.setTag(ri);
					v.setOnLongClickListener(mOnLongClickListener);
					v.setOnClickListener(mOnClickListener);
					addView(v);
					count++;
				}
			}
		}
		
		/*
		mViewHeight += (count * (mru.getPexil(R.dimen.app_icon_w) + mru
				.getPexil(R.dimen.app_icon_space))); */
		mViewHeight += (count * (size.getApp_icon_w() + size.getApp_icon_space()));

		// 为了避免ANR的问题，图标用线程加载。
		new Thread("LoadAppIcon") {
			public void run() {
				for (Runnable r : loadIcon) {
					r.run();
				}
				mHandler.post(new Runnable() {
					public void run() {
						
					}
				});
			}
		}.start();
	}

	public void setAppClassSpace(AppClassSpace mAppClass) {
		mAppClasseSpace = mAppClass;
	}

	public void scrollToY(int pos) {
		// 要滚动到下一个item，而不是让它顶着上一个
		/* pos += mru.getPexil(R.dimen.app_icon_w)
				+ mru.getPexil(R.dimen.app_icon_space); */
		pos += (size.getApp_icon_w() + size.getApp_icon_space());
		if (pos > mViewHeight - mConf.getUsableHeight()) {
			pos = mViewHeight - mConf.getUsableHeight();
		}
		int delta = pos - getScrollY();
		AXLog.d(TAG, "scrollToY(" + pos + ", " + delta + ")");
		mScroller.startScroll(getScrollX(), getScrollY(), 0, delta, 0);
		invalidate();
	}

	public boolean scrollable() {
		return false;
	}

	@Override
	public void afterTextChanged(Editable text) {
		ResolveInfo ri = null;
		AppItemView v = null;
		View child = null;
		SpannableStringBuilder ssb = null;
		boolean visible = false;

		if (mMode != MODE_SEARCH) {
			// 如果不是在search模式下，把所有View设置为可见不用管事。
			for (int i = 0; i < getChildCount(); i++) {
				child = getChildAt(i);
				child.setVisibility(VISIBLE);
				if (child instanceof AppItemView) {
					// 要消除名称的模式，否则颜色还不会变。
					((AppItemView) child).clearTextColor();
				}
			}
			return;
		}

		// 先滚回来。
		scrollToTop(false);
		AXLog.d(TAG, "Text:" + text.toString());
		List<AppItem> list = mAppManager.getMatched(
				mConf.getTextColor(),
				mConf.getAccentColor(), text);
		HashMap<ResolveInfo, SpannableStringBuilder> ris = new HashMap<ResolveInfo, SpannableStringBuilder>();
		for (AppItem ai : list) {
			ris.put(ai.mRi, ai.mName);
		}

		for (int i = 0; i < getChildCount(); i++) {
			visible = false;
			child = getChildAt(i);
			ri = (ResolveInfo) child.getTag();
			if (ri != null) {
				ssb = ris.get(ri);
				if (ssb != null && child instanceof AppItemView) {
					v = (AppItemView) child;
					v.setText(ssb);
					v.setVisibility(VISIBLE);
					visible = true;
				}
			}

			if (!visible) {
				child.setVisibility(GONE);
			}
		}

		requestLayout();
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {

	}

	@Override
	public void invalidate() {
		super.invalidate();

	}

	public void updateAppItemTheme(int color, int textColor) {
		View v = null;
		for (int i = 0; i < getChildCount(); i++) {
			v = getChildAt(i);
			if (v instanceof AppItemView) {
				((AppItemView) v).setImageBackgroundColor(color);
				((AppItemView) v).setTextColor(textColor);
			}
		}
	}

	public void startEndAnim(Rotate3DAnimation anim, View current,
			Animation.AnimationListener l, int totalDelay) {
		View v = null;
		int[] loc = new int[2];
		int animDelay = 250;
		int animVerticalDelay = 25;
		int index = 0;
		Rotate3DAnimation a = null;
		for (int i = getChildCount() - 1; i > 0; i--) {
			v = getChildAt(i);
			v.getLocationOnScreen(loc);
			if (loc[1] > mConf.getScreenH()) {
				// 如果在屏幕下面，下一个。
				continue;
			} else if (loc[1] < -mAppIconH) {
				// 如果在屏幕上面，则停止
				break;
			}

			try {
				a = anim.clone();
			} catch (CloneNotSupportedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (a == null) {
				continue;
			}

			if (v.equals(current)) {
				a.setStartOffset(animDelay);
				a.setAnimationListener(l);
			} else {
				a.setStartOffset(animVerticalDelay * index + totalDelay);
				index++;
			}
			v.startAnimation(a);
		}
	}

	public void clearChildAnimation() {
		for (int i = 0; i < getChildCount(); i++) {
			getChildAt(i).clearAnimation();
		}
	}

	public int getMode() {
		return mMode;
	}
	
	public void updateThemeColor() {
		
	}
}
