package com.axen.launcher.system;

import android.content.Context;

/**
 * 所有Manager的父类
 * @author lanhuanze
 *
 */
public abstract class Manager {

	/**
	 * 子类在初化完成的时候，一定要将本标志设置成true,否则会导致
	 * {@link #checkState()} 抛出IllegalStateException异常。
	 */
	protected boolean mbInitialized =false;
	
	/**
	 * 初始化函数。
	 * @param context
	 */
	public abstract void init(Context context);
	
	protected boolean checkState() {
		if(!mbInitialized) {
			throw new IllegalStateException("Initialization needed before use.");
		}
		return mbInitialized;
	}
	
	public abstract void query();
}
