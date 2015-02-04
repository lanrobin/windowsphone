package com.axen.launcher.wp7.ui.apputil;

import java.util.Vector;

import com.axen.launcher.app.AppManager.AppItem;
import com.axen.launcher.wp7.ui.apputil.ClassifyApp.AppClass;

import android.content.Context;
import android.content.pm.ResolveInfo;

public class SimplifyChineseClassify extends AbstractClassifyApp {

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
		System.loadLibrary("Pinyin");
	}

	SimplifyChineseClassify(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Vector<AppClass> classify(Vector<AppItem> apps) {
		clearClassified();
		ResolveInfo ri = null;
		for (AppItem i : apps) {
			ri = i.mRi;
			char c = getAppNameFirstChar(ri);
			if (isDigital(c)) {
				sAppClasses.get(0).add(ri);
			} else if (isAlphaBeta(c)) {
				c = Character.toLowerCase(c);
				sAppClasses.get(c - 'a' + 1).add(ri);
			} else {
				short cs = (short)c;
				cs = getFirstPinyin(cs);
				c = (char)cs;
				if (isAlphaBeta(c)) {
					c = Character.toLowerCase(c);
					sAppClasses.get(c - 'a' + 1).add(ri);
				} else {
					sAppClasses.get(sAppClasses.size() - 1).add(ri);
				}
			}
		}
		return sAppClasses;
	}

	private short getFirstPinyin(short c) {
		return nativeFirstPinyin(c);
	}

	private native short nativeFirstPinyin(short c);
	
	@Override
	public int getMaxNameLen() {
		// 默认是11个字符
		return 30;
	}
}
