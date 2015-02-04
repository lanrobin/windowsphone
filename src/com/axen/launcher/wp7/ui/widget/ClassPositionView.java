package com.axen.launcher.wp7.ui.widget;

import com.axen.launcher.wp7.main.R;
import com.axen.launcher.wp7.main.WP7App;
import com.axen.launcher.wp7.main.WP7Configuration;
import com.axen.launcher.wp7.ui.apputil.ClassifyApp.AppClass;
import com.axen.utils.ResourceUtil;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.View;

public class ClassPositionView extends View {

	private AppClass mAC = null;
	private WP7Configuration mConf = WP7Configuration.getInstance();
	private ResourceUtil mru = mConf.getRU();

	private int mColor = Color.RED;
	private int mBackgroundColor = Color.TRANSPARENT;
	private Paint mPainter = null;

	private int mViewW = 0;
	private int mViewH = 0;

	private int mStrokeSize = 0;

	private boolean mDrawBorder = false;
	
	private float mTextSize = 0;
	
	public ClassPositionView(Context context, AppClass ac, int color, int bgColor,
			int w, int h, boolean drawBorder) {
		super(context);
		mAC = ac;
		// mViewW = mru.getPexil(R.dimen.app_icon_w);
		// mViewH = mru.getPexil(R.dimen.app_icon_h);
		mViewW = w;
		mViewH = h;

		mStrokeSize = mViewW / 10; // 估算的

		mDrawBorder = drawBorder;
		
		mTextSize = mru.getDimen(R.dimen.app_class_font_h); // 估算

		if (ac.size() > 0) { // 如果这是一个有效的项，则设置正常的颜色
			mBackgroundColor = bgColor;
			mColor = color;
		} else {
			mBackgroundColor = 0xFF202020;
			mColor = Color.GRAY;
		}
		mPainter = new Paint(Paint.FAKE_BOLD_TEXT_FLAG |
		Paint.ANTI_ALIAS_FLAG);
		mPainter = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPainter.setAntiAlias(true);
		mPainter.setColor(mColor);
		mPainter.setStyle(Paint.Style.FILL);
		setBackgroundColor(mBackgroundColor);
	}

	public void setColor(int color) {
		mColor = color;
		mPainter.setColor(mColor);
	}

	public int getColor() {
		return mColor;
	}
	
	private void updateColor(int color) {
		if (mAC.size() > 0) { // 如果这是一个有效的项，则设置正常的颜色
			// mBackgroundColor = bgColor;
			mColor = color;
		} else {
			mBackgroundColor = 0xFF202020;
			mColor = Color.GRAY;
		}
	}
	
	@Override
	public void draw(Canvas canvas) {
		// updateColor(WP7App.getThemeColor());
		canvas.drawColor(mBackgroundColor);
		super.draw(canvas);
		Rect r = new Rect(0, 0, mViewW, mViewH);

		int textCx = mViewW / 10;
		int textCy = mViewH - 2 * mStrokeSize - 1;
		mPainter.setColor(mColor);
		mPainter.setStrokeWidth(mStrokeSize);
		mPainter.setStyle(Paint.Style.STROKE);
		if (mAC.showInfo.length() > 1) {
			mPainter.setTextSize(mTextSize * 2 / 3);
			textCx -= mStrokeSize / 2;
		} else {
			mPainter.setTextSize(mTextSize);
		}
		if (mDrawBorder) {
			canvas.drawRect(r, mPainter);
		}
		/*
		 * canvas.drawLine(r.left, r.top, r.right, r.top, mPainter);
		 * canvas.drawLine(r.left, r.bottom, r.right, r.bottom, mPainter);
		 * canvas.drawLine(r.left, r.top, r.left, r.bottom, mPainter);
		 * canvas.drawLine(r.right, r.top, r.right, r.bottom, mPainter);
		 */
		if (!mAC.showInfo.equals(AppClass.CLASS_UNKOWN)) {
			mPainter.setStyle(Paint.Style.FILL);
			canvas.drawText(mAC.showInfo, 0, mAC.showInfo.length(), textCx,
					textCy, mPainter);
			mPainter.setStyle(Paint.Style.STROKE);
		} else {
			drawBall(canvas);
		}
	}

	private void drawBall(Canvas canvas) {
		mPainter.setStrokeWidth(mStrokeSize / 2);
		canvas.drawCircle(mViewW * 2 / 5, mViewH * 3 / 5, mViewH / 4, mPainter);
		canvas.drawLine(mViewW * 3 / 20, mViewH * 3 / 5, mViewW * 13 / 20,
				mViewH * 3 / 5, mPainter);
		canvas.drawLine(mViewW * 2 / 5, mViewH * 7 / 20, mViewW * 2 / 5,
				mViewH * 17 / 20, mPainter);
		float sx = mViewW * 3 / 20;
		float tx = mViewW * 13 / 20;
		float d = (float) ((mViewH / 4) * (1 - Math.sqrt(3) / 2));
		sx += d;
		tx -= d;
		canvas.drawLine(sx, mViewH * 29 / 40, tx, mViewH * 29 / 40, mPainter);
		canvas.drawLine(sx, mViewH * 19 / 40, tx, mViewH * 19 / 40, mPainter);
		RectF f = new RectF(mViewW * 11 / 40, mViewH * 17 / 20,
				mViewW * 21 / 40, mViewH * 7 / 20);
		canvas.drawOval(f, mPainter);
		mPainter.setStrokeWidth(mStrokeSize);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		setMeasuredDimension(mViewW, mViewH);
	}

	public void setAppClass(AppClass appClass, int color, int bgColor) {
		mAC = appClass;
		if (mAC.size() > 0) { // 如果这是一个有效的项，则设置正常的颜色
			mBackgroundColor = bgColor;
			mColor = color;
		} else {
			mBackgroundColor = 0xFF202020;
			mColor = Color.GRAY;
		}
		mPainter.setColor(mColor);
		setBackgroundColor(mBackgroundColor);
	}

	public AppClass getAppClass() {
		return mAC;
	}
}
