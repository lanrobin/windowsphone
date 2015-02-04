package com.axen.launcher.wp7.ui;

import java.util.List;

import com.axen.launcher.wp7.main.R;
import com.axen.launcher.wp7.main.WP7App;
import com.axen.launcher.wp7.main.WP7Configuration;
import com.axen.launcher.wp7.ui.apputil.ClassifyApp;
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
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.widget.EditText;
import android.widget.Scroller;

public class AppClassSpace extends ViewGroup {

	private static final String TAG = "AppClassSpace";
	private int mAppItemH = 0;
	private int mAppItemW = 0;
	private int mAppItemSpace = 0;
	private int mAppItemLeftSpace = 0;
	private int mSpaceW = 0;
	private int mSpaceH = 0;

	private int mSearchBoxH = 0;
	private int mSearchBoxW = 0;
	private int mSearchBoxLeftspace = 0;
	
	private WP7Configuration mConf = WP7Configuration.getInstance();
	private ResourceUtil mru = mConf.getRU();
	private Size size = Size.get();
	private List<AppClass> mAppClasses;
	private Scroller mScroller;

	private EditText mSearchBox = null;
	private View mSearchBoxWrap = null;
	
	private TextWatcher mTextWatcher = null;

	private Interpolator mScrollInterplator = new Interpolator() {

		public float getInterpolation(float t) {
			// return (float) Math.sqrt(t);
			// return (float) Math.pow(t, 1 / Math.E);
			return t;
		}
	};
	private int mMode = AppSpace.MODE_NORMAL;

	public AppClassSpace(Context context) {
		this(context, null);
	}

	public AppClassSpace(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public AppClassSpace(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mAppItemH = size.getApp_icon_h(); //mru.getPexil(R.dimen.app_icon_h);
		mAppItemW = size.getApp_icon_w(); //mru.getPexil(R.dimen.app_icon_w);
		mAppItemSpace = size.getApp_icon_space(); //mru.getPexil(R.dimen.app_icon_space);
		mAppItemLeftSpace = size.getApp_left_space(); //mru.getPexil(R.dimen.app_left_space);

		mSearchBoxH = size.getSearch_box_h(); //mru.getPexil(R.dimen.search_box_h);
		mSearchBoxLeftspace = size.getSearch_left_space(); //mru.getPexil(R.dimen.search_left_space);
		mSearchBoxW = mConf.getScreenW() - 2 * mSearchBoxLeftspace;
		mSpaceW = size.getApp_space_w(); // mru.getPexil(R.dimen.app_space_w);
		mScroller = new Scroller(context, mScrollInterplator);
		
		mSearchBoxWrap = LayoutInflater.from(context).inflate(R.layout.search_input_box, null);
		mSearchBox = (EditText)mSearchBoxWrap.findViewById(R.id.id_search_editbox);
		addView(mSearchBoxWrap);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		setBackgroundColor(mConf.getBackgroundColor());
		mSearchBox.setTextColor(mConf.getBackgroundColor());
	}

	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
		setBackgroundColor(mConf.getBackgroundColor());
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if (mMode == AppSpace.MODE_SEARCH) {
			onSearchLayout(changed, l, t, r, b);
		} else {
			onNormalLayout(changed, l, t, r, b);
		}
	}

	protected void onNormalLayout(boolean changed, int l, int t, int r, int b) {
		int top = mAppItemH + mAppItemSpace;
		View v = null;
		for (int i = 0; i < getChildCount(); i++) {
			v = getChildAt(i);
			if (v.getVisibility() != GONE) {
				v.layout(0, top, mSpaceW, top + mAppItemH);
				top += (mAppItemH + mAppItemSpace);
			}
		}
		mSpaceH = top;
	}

	protected void onSearchLayout(boolean changed, int l, int t, int r, int b) {
		int top = ((mAppItemH + mAppItemSpace) - mSearchBoxH) / 2;
		/**
		 * 在Search模式下，我们只要layout Searchbox
		 */
		if (mSearchBoxWrap.getVisibility() != VISIBLE) {
			// mSearchBox.setText("Search");
			mSearchBoxWrap.setVisibility(VISIBLE);
		}

		mSearchBoxWrap.layout(mSearchBoxLeftspace, top, mSearchBoxLeftspace
				+ mSearchBoxW, top + mSearchBoxH);
		mSpaceH = (mAppItemH + mAppItemSpace);
		// mSearchBox.requestFocus();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		View v = null;
		for (int i = 0; i < getChildCount(); i++) {
			v = getChildAt(i);
			v.measure(widthMeasureSpec, heightMeasureSpec);
		}
		this.setMeasuredDimension(mSpaceW, mAppItemH + mAppItemSpace);
	}

	public void setAppClasses(List<AppClass> acs) {
		removeAllViews();
		mAppClasses = acs;
		AppClass ac = null;
		for (int i = 0; i < mAppClasses.size(); i++) {
			ac = mAppClasses.get(i);
			if (ac.size() > 0) {
				addView(new AppClassView(getContext(), ac));
			}
		}
		if(mSearchBoxWrap != null) {
			addView(mSearchBoxWrap);
		}
		requestLayout();
	}

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			scrollTo(0, mScroller.getCurrY());
			postInvalidate();
		}
	}

	/**
	 * 根据AppSpace的滚动量来计算自己的滚动量
	 * 
	 * @param moveOffset
	 * @param lastScrollY
	 * @param viewHeigt
	 */
	public void rolling(int moveOffset, int lastScrollY, int viewHeigt) {
		int delta = 0;
		AppClass ac = null;
		AppClass lastAC = null;
		int countScroll = 0; // 根据滚过的个数来算出来的长度。
		int offset = getScrollY();
		int count = 0;
		int itemH = mAppItemH + mAppItemSpace;

		// moveOffset -= viewHeigt;
		/**
		 * 从最小的appclass开始计算
		 */
		for (int i = 0; i < mAppClasses.size(); i++) {
			ac = mAppClasses.get(i);
			if (ac.size() > 0) {
				// 如果是有效的class
				if (ac.screenOffset > moveOffset) {
					// 只到找到一个class所在的位置比moveOffset小,停止
					break;
				}
				lastAC = ac;
				count++; // 记录已经过了几个有效的ac
			}
		}

		if (lastAC == null) {
			// 在第一个和第二个之间，什么也不用管。
			return;
		} else if (moveOffset > lastAC.screenOffset + itemH) {
			// 如果已经滚超过了这个AC的长度，则
			delta = itemH;
		} else {
			delta = moveOffset - lastAC.screenOffset;
		}
		AXLog.d(TAG, "lastAC is null = " + (lastAC == null));
		AXLog.d(TAG, "count = " + count);
		AXLog.d(TAG, "delta = " + delta);
		AXLog.d(TAG, "offset = " + offset);
		AXLog.d(TAG, "moveOffset = " + moveOffset);
		AXLog.d(TAG, "lastAC.screenOffset = " + lastAC.screenOffset);
		AXLog.d(TAG, "-----------------");
		countScroll = (count - 1) * (mAppItemH + mAppItemSpace);
		// 滚动的量为: 应当滚动的量-已经滚动的量
		delta = countScroll + delta - offset;
		// mScroller.startScroll(getScrollX(), offset, getScrollX(), delta);

		scrollBy(getScrollX(), delta);
		invalidate();

	}

	public void setMode(int mode, boolean requestLayout) {
		mMode = mode;
		View v = null;
		scrollToTop();
		mSearchBox.setText("");
		if (mMode == AppSpace.MODE_SEARCH) {
			for (int i = 0; i < getChildCount(); i++) {
				v = getChildAt(i);
				if (v instanceof EditText) {
					v.setVisibility(VISIBLE);
				} else {
					v.setVisibility(GONE);
				}
			}
		} else {
			for (int i = 0; i < getChildCount(); i++) {
				v = getChildAt(i);
				if (v instanceof EditText) {
					v.setVisibility(GONE);
				} else {
					v.setVisibility(VISIBLE);
				}
			}
		}
		if (requestLayout) {
			requestLayout();
		}
	}
	
	public void scrollToTop() {
		// int delta = getScrollY();
		scrollTo(getScrollX(), 0);
	}
	
	public void setTextWatcher(TextWatcher tw) {
		mTextWatcher = tw;
		if(mSearchBox != null && mTextWatcher != null) {
			mSearchBox.addTextChangedListener(mTextWatcher);
		}
	}
}
