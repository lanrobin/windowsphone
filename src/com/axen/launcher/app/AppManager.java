package com.axen.launcher.app;

import java.util.ArrayList;
import java.util.Vector;
import java.util.List;

import com.axen.utils.AXLog;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;

public class AppManager {

	private static final String TAG = "AppManager";

	public static class AppItem {
		public ResolveInfo mRi = null;
		public SpannableStringBuilder mName = null;

		public AppItem(ResolveInfo ri, SpannableStringBuilder name) {
			mRi = ri;
			mName = name;
		}
	}

	private Vector<AppItem> mApps = new Vector<AppItem>();
	private PackageManager mPackageManager = null;

	public Vector<AppItem> getAllLauncherAcitivities() {
		if (mContext == null || mPackageManager == null) {
			return null;
		}
		mApps.clear();
		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		// mainIntent.addCategory(Intent.CATEGORY_DEFAULT);
		// 符合上面条件的全部查出来,并且排序
		List<ResolveInfo> list = mPackageManager.queryIntentActivities(
				mainIntent, 0);
		for (ResolveInfo ri : list) {
			mApps.add(new AppItem(ri, new SpannableStringBuilder(ri
					.loadLabel(mPackageManager))));
		}
		return mApps;
	}

	private Context mContext;

	private AppManager() {
		// mApps = new ArrayList<ResolveInfo>();
	}

	private static final class Holder {
		public static final AppManager _INSTANCE = new AppManager();
	}

	public static AppManager getInstance() {
		return Holder._INSTANCE;
	}

	public void init(Context context) {
		mContext = context;
		mPackageManager = mContext.getPackageManager();
	}

	public boolean isSystemApp(ResolveInfo ri) {
		return isSystemApp(ri.activityInfo.packageName);
	}
	
	public boolean isSystemApp(String packageName) {
		boolean ret = false;
		try {
			ApplicationInfo ai = mPackageManager.getApplicationInfo(packageName, 0);
			if (ai != null) {
				ret = ai.sourceDir.startsWith("/system/");
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return ret;
	}
	
	public List<AppItem> getMatched(int unmatchColor, int matchColor, CharSequence cs) {
		if(TextUtils.isEmpty(cs)) {
			// 如果是空，全部都匹配。
			return mApps;
		}
		List<AppItem> list = new ArrayList<AppItem>();
		SpannableStringBuilder ssb = null;
		for(AppItem ai: mApps) {
			ssb = getMatchIngnoreCase(unmatchColor, matchColor, ai.mName.toString(), cs);
			
			if(ssb != null) {
				list.add(new AppItem(ai.mRi, ssb));
			}
		}
		return list;
	}
	
	private SpannableStringBuilder getMatchIngnoreCase(int unmatchColor, int matchColor,
			String target, CharSequence t) {
		String lowCaseTarget = target.toLowerCase();
		String text = t.toString().toLowerCase();
		String[] ss = lowCaseTarget.split(" "); // 以空格拆分
		int indexOfMatch = -1;
		int unMatchPrefixLen = 0; // 没有匹配之前的长度
		for (int i = 0; i < ss.length; i++) {
			if (ss[i].startsWith(text)) {
				indexOfMatch = i; // 找到了
				break;
			}
			unMatchPrefixLen += (ss[i].length() + 1);
		}
		
		if(indexOfMatch == -1) {
			
			// 如果没有找到，返回null
			return null;
		}
		AXLog.d(TAG, t + " matched " + target + " at index = " + unMatchPrefixLen);
		SpannableStringBuilder ssb = new SpannableStringBuilder(target);

		// 先设置前面没有匹配的
		ssb.setSpan(new ForegroundColorSpan(unmatchColor), 0, unMatchPrefixLen,
				Spannable.SPAN_INCLUSIVE_INCLUSIVE);

		// 再设置匹配了的
		ssb.setSpan(new ForegroundColorSpan(matchColor), unMatchPrefixLen,
				unMatchPrefixLen + t.length(),
				Spannable.SPAN_EXCLUSIVE_INCLUSIVE);

		// 再设置最后没有匹配的
		ssb.setSpan(new ForegroundColorSpan(unmatchColor),
				unMatchPrefixLen + t.length(), target.length(),
				Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
		return ssb;
	}

	/**
	 * 匹配文字，并标颜色
	 * 
	 * @param unmatchColor
	 *            没有匹配的颜色
	 * @param matchColor
	 *            匹配的颜色
	 * @param target
	 *            目标字符
	 * @param t
	 *            匹配字
	 * @return 已经匹配好了的字符，如果不匹配返回 null
	 */
	private SpannableStringBuilder getMatch(int unmatchColor, int matchColor,
			String target, CharSequence t) {
		String[] ss = target.split(" "); // 以空格拆分
		int indexOfMatch = -1;
		int unMatchPrefixLen = 0; // 没有匹配之前的长度
		for (int i = 0; i < ss.length; i++) {
			if (ss[i].startsWith(t.toString())) {
				indexOfMatch = i; // 找到了
				break;
			}
			unMatchPrefixLen += (ss[i].length() + 1);
		}
		
		if(indexOfMatch == -1) {
			
			// 如果没有找到，返回null
			return null;
		}
		AXLog.d(TAG, t + " matched " + target + " at index = " + unMatchPrefixLen);
		SpannableStringBuilder ssb = new SpannableStringBuilder(target);

		// 先设置前面没有匹配的
		ssb.setSpan(new ForegroundColorSpan(unmatchColor), 0, unMatchPrefixLen,
				Spannable.SPAN_INCLUSIVE_INCLUSIVE);

		// 再设置匹配了的
		ssb.setSpan(new ForegroundColorSpan(matchColor), unMatchPrefixLen,
				unMatchPrefixLen + t.length(),
				Spannable.SPAN_EXCLUSIVE_INCLUSIVE);

		// 再设置最后没有匹配的
		ssb.setSpan(new ForegroundColorSpan(unmatchColor),
				unMatchPrefixLen + t.length(), target.length(),
				Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
		return ssb;
	}
}
