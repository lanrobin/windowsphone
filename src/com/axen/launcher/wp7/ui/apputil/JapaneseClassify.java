package com.axen.launcher.wp7.ui.apputil;

import java.util.Vector;

import com.axen.launcher.app.AppManager.AppItem;
import com.axen.launcher.wp7.ui.apputil.ClassifyApp.AppClass;

import android.content.Context;
import android.content.pm.ResolveInfo;

public class JapaneseClassify extends AbstractClassifyApp {

	private final static char[] HEADERS = { 'ア', 'イ', 'ウ', 'エ', 'オ', 'カ', 'キ',
			'ク', 'ケ', 'コ', 'サ', 'シ', 'ス', 'セ', 'ソ', 'タ', 'チ', 'ツ', 'テ', 'ト',
			'ナ', 'ニ', 'ヌ', 'ネ', 'ノ', 'ハ', 'ヒ', 'フ', 'ヘ', 'ホ', 'マ', 'ミ', 'ム',
			'メ', 'モ', 'ヤ', 'イ', 'ユ', 'エ', 'ヨ', 'ラ', 'リ', 'ル', 'レ', 'ロ', 'ワ',
			'イ', 'ウ', 'エ', 'ヲ', 'ン' }; // 共51个元素
	
	private final static int GROUP_ELEM_NUM = 5;  // 每一个分为一个组
	private final static int GROUP_NUMBER = 10;  // 一共有10组，最后一个归入最后一组
	private final static int INVALID_GROUP = -1;
	
	// 先初始化
	static {
		sAppClasses.clear();
		for (int i = 0; i < GROUP_NUMBER; i++) {
			sAppClasses.add(new AppClass("" + HEADERS[i * GROUP_ELEM_NUM], "" + HEADERS[i * GROUP_ELEM_NUM]));
		}
		sAppClasses.add(new AppClass(AppClass.CLASS_UNKOWN,
				AppClass.CLASS_UNKOWN));
		sAppClasses
				.add(new AppClass(AppClass.CLASS_BASIC, AppClass.CLASS_BASIC));
		for (char c = 'a'; c <= 'z'; c++) {
			sAppClasses.add(new AppClass(String.valueOf(c), String.valueOf(c)));
		}
	}

	public JapaneseClassify(Context context) {
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
				sAppClasses.get(GROUP_NUMBER + 1).add(ri);
			} else if (isAlphaBeta(c)) {
				c = Character.toLowerCase(c);
				sAppClasses.get(c - 'a' + 2 + GROUP_NUMBER).add(ri);
			} else {
				int index = getGroup(c);
				if (index != INVALID_GROUP) {
					// 找到，加入到对应的组。
					sAppClasses.get(index).add(ri);
				} else {
					// 没有找到，加入到未知组
					sAppClasses.get(GROUP_NUMBER).add(ri);
				}
			}
		}
		return sAppClasses;
	}
	
	private int getGroup(char c) {
		int index = 0;
		for(; index < HEADERS.length; index ++) {
			if(HEADERS[index] == c) {
				break;
			}
		}
		if(index >= HEADERS.length) {
			// 没找到
			return INVALID_GROUP;
		}
 		index /= GROUP_ELEM_NUM;
 		if(index >= GROUP_NUMBER) {
 			// 最后一元素匹配的要归入最后一组。
 			index = GROUP_NUMBER -1;
 		}
		return index;
	}
	
	@Override
	public int getMaxNameLen() {
		// 默认是11个字符
		return 30;
	}
}
