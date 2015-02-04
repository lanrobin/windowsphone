package com.axen.launcher.wp7.ui.widget;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import com.axen.launcher.app.AccentsManager;
import com.axen.launcher.wp7.main.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class AccentAdapter extends BaseAdapter {

	public static class AccentData {
		public static final String ACCENT_DATA_COLOR_INDEX = "AccentAdapter.AccentData.ACCENT_DATA_COLOR_ID";
		public int drawaleId;
		public int colorId;
		public int stringId;

		public AccentData(int did, int cid, int sid) {
			drawaleId = did;
			colorId = cid;
			stringId = sid;
		}
	}

	private Context mContext = null;
	private List<AccentData> mList = null;
	private LayoutInflater mInflater = null;
	private int mPanelSize = 0;
	private int mCurrentColorIndex = 0;

	public AccentAdapter(Context c, List<AccentData> list, int panelSize,
			int currentColorIndex) {
		mContext = c;
		mList = list;
		mInflater = LayoutInflater.from(mContext);
		mPanelSize = panelSize;
		mCurrentColorIndex = currentColorIndex;
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
		ImageView iv = null;
		TextView tv = null;

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.accent_color_item, null);
		}

		iv = (ImageView) convertView.findViewById(R.id.id_accent_color_image);
		tv = (TextView) convertView.findViewById(R.id.id_accent_color_text);
		AccentData ad = mList.get(position);
		//iv.setMinimumHeight(mPanelSize);
		// iv.setMinimumWidth(mPanelSize);
		Drawable drawable = mContext.getResources().getDrawable(ad.drawaleId);
		drawable.setBounds(0, 0, mPanelSize, mPanelSize);
		//  test saveDrawable(drawable, "/sdcard/color" + position + ".png");
		iv.setImageDrawable(drawable);
		// iv.invalidate();
		tv.setText(ad.stringId);
		if(position == mCurrentColorIndex) {
			tv.setTextColor(mContext.getResources().getColor(AccentsManager.COLORS[mCurrentColorIndex]));
		}
		convertView.setTag(ad);
		// convertView.requestLayout();
		return convertView;
	}
	
	private void saveDrawable(Drawable drawable, String fileName) {
		Bitmap bitmap = Bitmap
				.createBitmap(
						mPanelSize,
						mPanelSize,
						drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
								: Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);

		drawable.setBounds(0, 0, mPanelSize,
				mPanelSize);

		drawable.draw(canvas);

		OutputStream os = null;
		// String file = ICONS + "/" + fileName;
		new File(fileName).delete();
		try {
			os = new FileOutputStream(fileName);
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
			os.flush();
			os.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
