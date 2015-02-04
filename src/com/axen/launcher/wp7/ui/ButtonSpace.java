package com.axen.launcher.wp7.ui;

import com.axen.launcher.wp7.main.R;
import com.axen.launcher.wp7.main.WP7App;
import com.axen.launcher.wp7.main.WP7Configuration;
import com.axen.launcher.wp7.ui.anim.AnimUtil;
import com.axen.launcher.wp7.ui.size.Size;
import com.axen.utils.AXLog;
import com.axen.utils.ResourceUtil;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

public class ButtonSpace extends ViewGroup {

	private static final String TAG = "ButtonSpace";

	private ImageView mArrowImage = null;
	private ImageView mSearchImage = null;
	private WP7Configuration mConf = WP7Configuration.getInstance();
	private ResourceUtil mru = mConf.getRU();
	private Size size = Size.get();
	private int mButtonW = 0;

	public static final int INDEX_SEARCH = 0;
	public static final int INDEX_ARROW_LEFT = 1;
	public static final int INDEX_ARROW_RIGHT = 2;

	private static int[][] images = {
			{ R.drawable.button_search, R.drawable.button_arrow_left,
					R.drawable.button_arrow_right },
			{ R.drawable.button_search_white,
					R.drawable.button_arrow_left_white,
					R.drawable.button_arrow_right_white } };

	public ButtonSpace(Context context) {
		this(context, null);
	}

	public ButtonSpace(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ButtonSpace(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
		setBackgroundColor(mConf.getBackgroundColor());
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mArrowImage = (ImageView) findViewById(R.id.id_button_arrow);
		mSearchImage = (ImageView) findViewById(R.id.id_button_search);
		setBackgroundColor(mConf.getBackgroundColor());
		mArrowImage
				.setImageResource(images[mConf.getBackgroundColorMode()][INDEX_ARROW_RIGHT]);
		mSearchImage
				.setImageResource(images[mConf.getBackgroundColorMode()][INDEX_SEARCH]);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

		int measuredW = size.getButton_space_w(); //mru.getPexil(R.dimen.button_space_w);
		setMeasuredDimension(measuredW, heightSpecSize);

		View child = null;
		int count = getChildCount();
		AXLog.d(TAG, "onMeasure: count = " + count);
		for (int i = 0; i < count; i++) {
			child = getChildAt(i);
			AXLog.d(TAG, "onMeasure: child.id = " + child.getId());

			child.measure(widthMeasureSpec, heightMeasureSpec);
		}

		setMeasuredDimension(measuredW, heightSpecSize);
		// super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {

		int top = size.getTop_margin_h(); //mru.getPexil(R.dimen.top_margin_h);
		int space = size.getButtons_space(); //mru.getPexil(R.dimen.buttons_space);
		int w = size.getButton_w(); //mru.getPexil(R.dimen.button_w);
		int h = size.getButton_h(); //mru.getPexil(R.dimen.button_h);
		mButtonW = w;

		if (!mConf.getShowStatusbar()) {
			top -= mConf.getStatusBarHeight();
		}

		View child = findViewById(R.id.id_button_arrow);

		int left = (r - l - w) / 2;
		child.layout(left, top, left + w, top + h);

		top = top + space + w;

		child = findViewById(R.id.id_button_search);
		child.layout(left, top, left + w, top + h);

	}

	public void setAlpha(float alpha) {
		if (alpha <= 0.0f) {
			alpha = 0.0f;
		} else if (alpha > 1.0f) {
			alpha = 1.0f;
		}
		if (mSearchImage != null) {
			// mSearchButton.get
			// mSearchImage.getBackground().setAlpha((int)(alpha * 255));
			mSearchImage.getDrawable().setAlpha((int) (alpha * 255));
		} else {
			AXLog.w(TAG, "mSearchButton is null");
		}

		if (alpha >= 1.0f) {
			mSearchImage.setEnabled(true);
		} else {
			mSearchImage.setEnabled(false);
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		AXLog.d(TAG,
				"Intercept:action =" + ev.getAction() + ", x = " + ev.getX()
						+ ", y = " + ev.getY());
		return super.onInterceptTouchEvent(ev);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		AXLog.d(TAG,
				"onTouchEvent:action =" + ev.getAction() + ", x = " + ev.getX()
						+ ", y = " + ev.getY());
		return super.onTouchEvent(ev);
	}

	public void setChildVisibility(int resId, int visibility) {
		View v = findViewById(resId);
		if (v != null) {
			v.setVisibility(visibility);
		}
	}

	public void startArrorJumpAnim() {
		/**
		 * int[] loc = new int[2]; mArrowImage.getLocationInWindow(loc);
		 * AnimationSet set = AnimUtil.getArrowButtonJumpAnim(loc[0],
		 * WP7App.getScreenWidth() - mButtonW + 1, loc[1]);
		 */
		int x = mArrowImage.getLeft();
		// int y = mArrowImage.getTop();
		int bsW = getWidth();
		AnimationSet set = AnimUtil.getArrowButtonJumpAnim(0, bsW - mButtonW
				- x, 0);
		mArrowImage.startAnimation(set);
		// mArrowImage.startAnimation(anim2);
	}
	
	public void updateSearchImage() {
		mSearchImage.setImageResource(images[mConf.getBackgroundColorMode()][0]);
	}

	public void startArrowRotateAnim(Animation anim) {
		mArrowImage.clearAnimation();
		mArrowImage.startAnimation(anim);
	}

	public void clearArrowRotateAnim() {
		mArrowImage.clearAnimation();
	}

	@Override
	public void setOnClickListener(OnClickListener l) {
		mArrowImage.setOnClickListener(l);
		mSearchImage.setOnClickListener(l);
	}

	public void updateArrowImage(int index) {
		mArrowImage
				.setImageResource(images[mConf.getBackgroundColorMode()][index]);
	}
}
