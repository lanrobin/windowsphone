package com.axen.launcher.wp7.ui.anim;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;

public class StatusbarAnimationListener implements AnimationListener {
	
	private View mHost = null;
	private int msv = View.GONE;
	private int mev = View.GONE;
	
	public StatusbarAnimationListener(View host, int startVisibility, int endVisibility) {
		mHost = host;
		msv = startVisibility;
		mev = endVisibility;
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		mHost.setVisibility(mev);
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAnimationStart(Animation animation) {
		if(mHost != null) {
			mHost.setVisibility(msv);
		}
	}

}
