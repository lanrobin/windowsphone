package com.axen.launcher.wp7.ui.widget;

import java.util.List;

import com.axen.launcher.wp7.main.WP7App;
import com.axen.launcher.wp7.main.WP7Configuration;
import com.axen.launcher.wp7.ui.apputil.ClassifyApp.AppClass;
import com.axen.utils.ResourceUtil;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class AppClassAdapter extends BaseAdapter {

	private List<AppClass> mList = null;
	private Context mContext = null;
	private OnClickListener mOnClickListener = null;
	private int mViewW = 0;
	private int mViewH = 0;
	private WP7Configuration mConf = WP7Configuration.getInstance();
	public AppClassAdapter(Context context, List<AppClass> list, OnClickListener l, int w, int h) {
		mList = list;
		mContext = context;
		mOnClickListener = l;
		mViewW = w;
		mViewH = h;
	}
	
	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ClassPositionView v = null;
		if(convertView == null){
			v = new ClassPositionView(mContext, mList.get(position),0xFFE0E0E0, mConf.getAccentColor(), mViewW, mViewH, false);
			v.setOnClickListener(mOnClickListener);
		}else {
			v = (ClassPositionView)convertView;
		}
		v.setAppClass(mList.get(position),0xFFE0E0E0, mConf.getAccentColor());
		v.invalidate();
		return v;
	}

}
