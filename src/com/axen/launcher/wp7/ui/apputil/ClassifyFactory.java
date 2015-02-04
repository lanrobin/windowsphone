package com.axen.launcher.wp7.ui.apputil;

import java.util.Locale;

import com.axen.utils.AXLog;

import android.content.Context;

public class ClassifyFactory {
	private static ClassifyApp sInstance = null;
	public static ClassifyApp getDefault(Context context){
		ClassifyApp app = null;
		Locale l = Locale.getDefault();
		String c = l.getCountry();
		if(context == null) {
			throw new NullPointerException("ClassifyFactory.getDefault arg is null.");
		}
		if(c.equals("CN")) { // 简单中文
			app = new SimplifyChineseClassify(context);
		}else if(c.equals("TW")) {  // 繁体中文
			app = new TranditionalChineseClassify(context);
		}
		/*else if(c.equals("JP")) {
		 * 暂时不支持日语，还有问题
			app = new JapaneseClassify(context);
		}*/
		else { // 默认的。
			app = new DefaultClassifyApp(context);
		}
		
		sInstance = app;
		return app;
	}
	
	public static ClassifyApp get() {
		return sInstance;
	}
}
