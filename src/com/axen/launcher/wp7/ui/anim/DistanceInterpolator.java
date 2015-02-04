package com.axen.launcher.wp7.ui.anim;

import com.axen.utils.AXLog;

import android.view.animation.Interpolator;

public class DistanceInterpolator implements Interpolator {
	
	private static final String TAG = "DistanceInterpolator";

	private int mStart = 0;
	private int mEnd = 0;
	private int mCur = 0;
	private double mDelta = 0;
	private float r = 0.0f;

	public DistanceInterpolator(int start, int end, int cur) {
		mStart = start;
		mEnd = end;
		mCur = cur;
		mDelta = Math.abs(end - start);
		r = computeCurrent(mStart, mEnd, mCur);
	}

	@Override
	public float getInterpolation(float t) {
		return r;
	}

	public void updateCurrent(int cur) {
		mCur = cur;
		r = computeCurrent(mStart, mEnd, mCur);
		AXLog.d(TAG, "s = " + mStart +", e = " + mEnd +", c = " + mCur + ", r = " + r);
	}

	private float computeCurrent(int s, int e, int c) {
		float t = 1.0f;
		if (s == e) {
			t = 1.0f;
		}
		if (c < s) {
			t = 0.0f;
		} else if (c > e) {
			t = 1.0f;
		} else {
			t = (float)((c - s) / mDelta);
		}
		return t;
	}
}
