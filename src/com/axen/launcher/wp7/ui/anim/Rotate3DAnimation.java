package com.axen.launcher.wp7.ui.anim;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class Rotate3DAnimation extends Animation {

	private float mFromXDegree = 0.0f;
	private float mToXDegree = 0.0f;
	private float mFromYDegree = 0.0f;
	private float mToYDegree = 0.0f;
	private float mFromZDegree = 0.0f;
	private float mToZDegree = 0.0f;
	private int mPivotXType = ABSOLUTE;
	private float mPivotXValue = 0.0f;
	private int mPivotYType = ABSOLUTE;
	private float mPivotYValue = 0.0f;

	private Camera mCamera;
	private float mPivotX;
	private float mPivotY;
	private static final String TAG = "Rotate3DAnimation";
	
	private View mAttachView = null;

	public Rotate3DAnimation(float fromXDegree, float toXDegree,
			float fromYDegree, float toYDegree, float fromZDegree,
			float toZDegree, int pivotXType, float pivotXValue, int pivotYType,
			float pivotYValue) {
		mFromXDegree = fromXDegree;
		mToXDegree = toXDegree;
		mFromYDegree = fromYDegree;
		mToYDegree = toYDegree;
		mFromZDegree = fromZDegree;
		mToZDegree = toZDegree;
		mPivotXType = pivotXType;
		mPivotXValue = pivotXValue;
		mPivotYType = pivotYType;
		mPivotYValue = pivotYValue;
		mAttachView = null;
	}

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		// super.applyTransformation(interpolatedTime, t);
		float xDegree = mFromXDegree + (mToXDegree - mFromXDegree)
				* interpolatedTime;
		float yDegree = mFromYDegree + (mToYDegree - mFromYDegree)
				* interpolatedTime;
		float zDegree = mFromZDegree + (mToZDegree - mFromZDegree)
				* interpolatedTime;

		final Matrix matrix = t.getMatrix();
		mCamera.save();
		// 这里很重要哦。
		//mCamera.translate(mPivotX, mPivotY, 0);
		mCamera.rotateX(xDegree);
		mCamera.rotateY(yDegree);
		mCamera.rotateZ(zDegree);
		// mCamera.translate(-mPivotX, -mPivotY, 0);
		mCamera.getMatrix(matrix);
		mCamera.restore();

		matrix.preTranslate(-mPivotX, -mPivotY);
		matrix.postTranslate(mPivotX, mPivotY);
	}

	@Override
	public void initialize(int width, int height, int parentWidth,
			int parentHeight) {
		super.initialize(width, height, parentWidth, parentHeight);
		mCamera = new Camera();
		mPivotX = resolveSize(mPivotXType, mPivotXValue, width, parentWidth);
		mPivotY = resolveSize(mPivotYType, mPivotYValue, height, parentHeight);
	}

	@Override
	public Rotate3DAnimation clone()  throws CloneNotSupportedException {
		Rotate3DAnimation r = (Rotate3DAnimation)super.clone();
		r.mFromXDegree = mFromXDegree;
		r.mToXDegree = mToXDegree;
		r.mFromYDegree = mFromYDegree;
		r.mToYDegree = mToYDegree;
		r.mFromZDegree = mFromZDegree;
		r.mToZDegree = mToZDegree;
		r.mPivotXType = mPivotXType;
		r.mPivotXValue = mPivotXValue;
		r.mPivotYType = mPivotYType;
		r.mPivotYValue = mPivotYValue;
		r.mPivotX = mPivotX;
		r.mPivotY = mPivotY;
		r.mAttachView = null;
		return r;
	}
	
	public void setAttachView(View v) {
		mAttachView = v;
	}
	
	public View getAttachView() {
		return mAttachView;
	}
}
