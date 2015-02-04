package com.axen.launcher.wp7.ui.apputil;

import java.util.Vector;

import com.axen.launcher.app.AppManager.AppItem;

import android.content.Context;
import android.content.pm.ResolveInfo;

public class DefaultClassifyApp extends AbstractClassifyApp {

	
	// 先初始化
	static {
		sAppClasses.clear();
		sAppClasses
				.add(new AppClass(AppClass.CLASS_BASIC, AppClass.CLASS_BASIC));
		for (char c = 'a'; c <= 'z'; c++) {
			sAppClasses.add(new AppClass(String.valueOf(c), String.valueOf(c)));
		}
		sAppClasses.add(new AppClass(AppClass.CLASS_UNKOWN,
				AppClass.CLASS_UNKOWN));
	}

	DefaultClassifyApp(Context context) {
		super(context);
	}

	@Override
	public Vector<AppClass> classify(Vector<AppItem> apps) {

		// 先清空
		clearClassified();
		ResolveInfo ri = null;
		for ( AppItem i : apps) {
			ri = i.mRi;
			char c = getAppNameFirstChar(ri);
			if (isDigital(c)) {
				sAppClasses.get(0).add(ri);
			} else if (isAlphaBeta(c)) {
				c = Character.toLowerCase(c);
				sAppClasses.get(c - 'a' + 1).add(ri);
			} else {
				sAppClasses.get(sAppClasses.size() - 1).add(ri);
			}
		}

		return sAppClasses;
	}

	@Override
	public int getMaxNameLen() {
		// 默认是11个字符
		return 30;
	}
}
