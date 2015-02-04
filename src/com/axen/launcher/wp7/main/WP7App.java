package com.axen.launcher.wp7.main;

import com.axen.launcher.app.AppManager;
import com.axen.launcher.app.TileManager;
import com.axen.launcher.system.CallManager;
import com.axen.launcher.system.ContactsManager;
import com.axen.launcher.system.ImageManager;
import com.axen.launcher.system.MessageManager;
import com.axen.launcher.wp7.ui.size.Size;

import android.app.Application;

public class WP7App extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		
		/*
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
				.detectDiskReads().detectDiskWrites().detectNetwork() // 这里可以替换为detectAll()
																		// 就包括了磁盘读写和网络I/O
				.penaltyLog() // 打印logcat，当然也可以定位到dropbox，通过文件保存相应的log
				.build());
		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
				.detectLeakedSqlLiteObjects() // 探测SQLite数据库操作
				.penaltyLog() // 打印logcat
				.penaltyDeath().build());
				*/

		WP7Configuration.getInstance().init(this);
		TileManager.getInstance().init(this);
		AppManager.getInstance().init(this);
		CallManager.getInstance().init(this);
		MessageManager.getInstance().init(this);
		ContactsManager.getInstance().init(this);
		ImageManager.getInstance().init(this);
		Size.init(this);
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
	}
}
