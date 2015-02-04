package com.axen.launcher.wp7.ui.anim;

import android.content.Context;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;

public class AnimUtil {
	public static AnimationSet getAppMenuOtherMove(int pivotY, int curY,
			int fromX, int toX) {
		AnimationSet set = new AnimationSet(false);
		int delta = curY - pivotY;
		int moveY = (int) (delta * 0.1);
		TranslateAnimation anim = new TranslateAnimation(fromX, toX, curY, curY
				+ moveY);
		anim.setFillAfter(true);
		anim.setDuration(100);
		anim.setStartOffset(0);
		set.addAnimation(anim);
		return set;
	}

	public static AnimationSet getTileDimAnimations() {
		return null;
	}

	public static AnimationSet getArrowButtonJumpAnim(int fromX, int toX, int y) {
		AnimationSet set = new AnimationSet(false);
		TranslateAnimation anim1 = new TranslateAnimation(fromX, toX, y, y);
		anim1.setFillAfter(true);
		anim1.setDuration(100);
		anim1.setStartOffset(0);
		anim1.setInterpolator(new DecelerateInterpolator(1.0f));
		set.addAnimation(anim1);

		TranslateAnimation anim2 = new TranslateAnimation(toX, fromX, y, y);
		anim2.setFillAfter(false);
		anim2.setDuration(100);
		anim2.setStartOffset(100);
		anim2.setInterpolator(new AccelerateInterpolator(1.0f));
		set.addAnimation(anim2);
		return set;
	}

	public static Animation getArrowRotateAnim(boolean reverse,
			DistanceInterpolator interpolator) {
		RotateAnimation ra = null;
		if (!reverse) {
			ra = new RotateAnimation(180, 0, Animation.RELATIVE_TO_SELF, 0.5f,
					Animation.RELATIVE_TO_SELF, 0.5f);
			ra.setFillAfter(true);
			ra.setFillBefore(false);
			ra.setInterpolator(interpolator);

		} else {
			ra = new RotateAnimation(0, -180, Animation.RELATIVE_TO_SELF, 0.5f,
					Animation.RELATIVE_TO_SELF, 0.5f);
			ra.setFillAfter(true);
			ra.setFillBefore(false);
			ra.setInterpolator(interpolator);
		}
		ra.setDuration(500);
		ra.setStartOffset(0);
		return ra;
	}

	public static Animation getEditTileAnim(Context context, int resId) {
		return AnimationUtils.loadAnimation(context, resId);
	}
	
	public static Animation getStatusBarShowAnim(View host, long startOffset) {
		TranslateAnimation ta = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0,
				Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0);
		ta.setAnimationListener(new StatusbarAnimationListener(host, View.GONE, View.VISIBLE));
		ta.setInterpolator(new WP7Interpolator());
		ta.setStartOffset(startOffset);
		ta.setDuration(150);
		// ta.setFillAfter(true);
		return ta;
	}
	
	public static Animation getStatusBarHideAnim(View host, long startOffset) {
		TranslateAnimation ta = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0,
				Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, -1.0f);
		ta.setAnimationListener(new StatusbarAnimationListener(host, View.VISIBLE, View.GONE));
		ta.setInterpolator(new WP7Interpolator());
		ta.setStartOffset(startOffset);
		ta.setDuration(150);
		ta.setFillAfter(true);
		return ta;
	}
}
