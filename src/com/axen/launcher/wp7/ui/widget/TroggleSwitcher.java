package com.axen.launcher.wp7.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class TroggleSwitcher extends SeekBar
	  implements Checkable, OnSeekBarChangeListener
	{
	
	
	public TroggleSwitcher(Context context) {
		this(context, null);
	}
	
	public TroggleSwitcher(Context context, AttributeSet attrs) {
		this(context, attrs,0);
	}
	  
	public TroggleSwitcher(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isChecked() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setChecked(boolean checked) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void toggle() {
		// TODO Auto-generated method stub
		
	}
	}
