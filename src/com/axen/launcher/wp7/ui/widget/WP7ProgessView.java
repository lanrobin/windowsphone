package com.axen.launcher.wp7.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class WP7ProgessView extends View {

	private int mColor = Color.RED;
	private int mBackgroundColor = Color.BLACK;
	private Paint mPainter = null;

	// private Handler mHandler = new Handler();

	private static final int RECT_NUMBER = 5;
	private static int[] sPos = new int[RECT_NUMBER];
	private static int[] sVisible = new int[RECT_NUMBER];

	private static final int RECT_WIDTH = 5; // px

	private static final float SLOW_RANGE = 0.30f; // 有30%的空间是慢的

	private static final int UPDATE_PROGRESS_INTERVAL = 25; // ms

	private static final int STANDARD_SCREEN_W = 480; // px

	private UpdateProgessThread mThread = null;

	public WP7ProgessView(Context context) {
		this(context, null);
	}

	public WP7ProgessView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public WP7ProgessView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public WP7ProgessView(Context context, int color, int bgcolor) {
		this(context, null, 0);
		mColor = color;
		mBackgroundColor = bgcolor;
		mPainter = new Paint();
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

	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
		canvas.drawColor(mBackgroundColor);

		for (int i = 0; i < RECT_NUMBER; i++) {
			if (sVisible[i] == View.VISIBLE) {
				drawRect(canvas, sPos[i]);
			}
		}
	}

	private void drawRect(Canvas canvas, int pos) {
		int m = getHeight() / 2;
		int w = RECT_WIDTH / 2;
		canvas.drawRect(new RectF(pos - w, m - w, pos + w, m + w), mPainter);
		// canvas.drawCircle(pos - w, m - w, 3, mPainter);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		mThread = new UpdateProgessThread("WP7ProgessView", this);
		mThread.start();
	}

	@Override
	protected void onDetachedFromWindow() {
		mThread.stopping();
		mThread = null;
		super.onDetachedFromWindow();
	}

	@Override
	protected void onWindowVisibilityChanged(int visibility) {
		// TODO Auto-generated method stub
		super.onWindowVisibilityChanged(visibility);
	}

	private static final class UpdateProgessThread extends Thread {
		private WP7ProgessView host = null;
		// private int mShowNumber = 0;
		// private int mNewRect = 0;
		// private int mStopIndex = 0;
		private int mInvisibleCount = 0; // 是不是重新开始一次循环。
		private int mPosToShowNext = 0; // 显示下一个方块坐标
		private int mIndexToShowNext = 0; // 检查显示下一个方块的方块坐标

		private long mSleepTime = 0;

		private boolean running = true;

		public UpdateProgessThread(String name, WP7ProgessView v) {
			super(name);
			host = v;
			for (int i = 0; i < RECT_NUMBER; i++) {

				// 让所有的小方块不显示
				sPos[i] = -RECT_WIDTH;
				sVisible[i] = View.GONE;
			}

			// 至少先让第一个可见
			sVisible[0] = View.VISIBLE;
		}

		public void run() {
			int w = host.getWidth();
			int slowRange = 0;
			int fastRange1 = 0;

			while (running) {
				if (w <= 0) {
					w = host.getWidth();
					slowRange = (int) (w * SLOW_RANGE);
					fastRange1 = (w - slowRange) / 2;
				}
				if (w > 0 && mSleepTime <= 0) {
					mSleepTime = (long) (UPDATE_PROGRESS_INTERVAL
							* STANDARD_SCREEN_W / w);
				}
				// int fastRange2 = fastRange1;

				if (mPosToShowNext == 0) {
					/**
					 * 现在我们来决定五个小方框的出现时机,研究了Windows Phone 7的progress
					 * dialog，它的效果是
					 * 至少能让五个小方块在同时出现在慢区域。所以在第一个方块走出慢区域的时候，第五个必然要进入慢区域.
					 * 算法是五个方块都要在慢区，所以他们的间隔是 slowRange/6, 快区是慢区的4/3倍，所以显示间隔就是
					 * slowRange * (1/6) * (4/3)。
					 */
					mPosToShowNext = (int) (slowRange * 2.0 / 9);
				}

				if (mIndexToShowNext < RECT_NUMBER - 1) {
					/**
					 * 如果指定的方块跑过了显示下一个方块的点，则显示下一个方块。
					 */
					if (sPos[mIndexToShowNext] > mPosToShowNext) {
						mIndexToShowNext++;
						sVisible[mIndexToShowNext] = View.VISIBLE;
					}
				}

				try {

					// 更新界面的时间间隔。
					Thread.sleep(mSleepTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				for (int i = 0; i < RECT_NUMBER; i++) {
					if (sVisible[i] != View.VISIBLE) {
						// 如果这个点是看不见的，则不更新了。
						continue;
					}
					if (sPos[i] <= fastRange1
							|| sPos[i] >= (fastRange1 + slowRange)) {
						// 让它移动快点。
						sPos[i] += (int)(1.5 *RECT_WIDTH);
					} else if ((sPos[i] - fastRange1) < slowRange) {
						// 让它移动慢点。
						sPos[i] += RECT_WIDTH;
					}
					// 如果已经移出边界，则让它看不见
					if (sPos[i] > w) {
						sVisible[i] = View.GONE;
						sPos[i] = -RECT_WIDTH;
					}
				}

				mInvisibleCount = 0;

				for (int i = 0; i < RECT_NUMBER; i++) {
					if (sVisible[i] != View.VISIBLE) {
						mInvisibleCount++;
					}
				}
				
				host.postInvalidate();

				// 如果需要重新开始，则初始化一些变量。
				if (mInvisibleCount >= RECT_NUMBER) {
					mIndexToShowNext = 0;
					sVisible[0] = View.VISIBLE;
					try {

						// 更新界面的时间间隔。
						Thread.sleep(300);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

			}
		}

		public void stopping() {
			running = false;
			// stop();
		}
	}

}
