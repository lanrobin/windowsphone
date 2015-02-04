package com.axen.launcher.wp7.ui.apputil;

import java.util.Vector;

import com.axen.launcher.app.AppManager.AppItem;

import android.content.Context;
import android.content.pm.ResolveInfo;

public class TranditionalChineseClassify extends AbstractClassifyApp {

	private static final int MAX_STROKE = 31;

	// 先初始化
	static {
		sAppClasses.clear();
		for (int i = 1; i <= MAX_STROKE; i++) {
			sAppClasses.add(new AppClass("" + i + "劃", "" + i + "劃"));
		}
		sAppClasses.add(new AppClass(AppClass.CLASS_UNKOWN,
				AppClass.CLASS_UNKOWN));
		sAppClasses
				.add(new AppClass(AppClass.CLASS_BASIC, AppClass.CLASS_BASIC));
		for (char c = 'a'; c <= 'z'; c++) {
			sAppClasses.add(new AppClass(String.valueOf(c), String.valueOf(c)));
		}
		System.loadLibrary("Stroke");
	}

	TranditionalChineseClassify(Context context) {
		super(context);
	}

	@Override
	public Vector<AppClass> classify(Vector<AppItem> apps) {
		clearClassified();
		ResolveInfo ri = null;
		for (AppItem i : apps) {
			ri = i.mRi;
			char c = getAppNameFirstChar(ri);
			if (isDigital(c)) {
				// 第32项
				sAppClasses.get(MAX_STROKE + 1).add(ri);
			} else if (isAlphaBeta(c)) {
				c = Character.toLowerCase(c);
				sAppClasses.get(c - 'a' + 2 + MAX_STROKE).add(ri);
			} else {
				short cs = (short)c;
				cs = getStrokeNumber(cs);
				if (cs > 0) {
					if(cs > MAX_STROKE) {
						cs = MAX_STROKE;
					}
					int index = cs - 1;
					sAppClasses.get(index).add(ri);
				} else {
					sAppClasses.get(MAX_STROKE).add(ri);
				}
			}
		}
		return sAppClasses;
	}

	private short getStrokeNumber(short c) {
		return nativeStrokeNumber(c);
	}

	private native short nativeStrokeNumber(short c);

	@Override
	public int getMaxNameLen() {
		// 默认是11个字符
		return 30;
	}
}
