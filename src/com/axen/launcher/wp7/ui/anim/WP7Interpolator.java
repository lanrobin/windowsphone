package com.axen.launcher.wp7.ui.anim;

import android.view.animation.Interpolator;

public class WP7Interpolator implements Interpolator {

	@Override
	public float getInterpolation(float t) {
		return (float) Math.pow(t, 1 / Math.E);
	}

	public WP7Interpolator() {
		
	}
	/**
	public static Interpolator getInstance() {
		return Holder._INSTANCE;
	}
	
	private static class Holder {
		public static final WP7Interpolator _INSTANCE = new WP7Interpolator();
	}
	*/
}
