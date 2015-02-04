package com.axen.launcher.wp7.ui;

import com.axen.launcher.wp7.main.R;
import com.axen.launcher.wp7.main.WP7Configuration;
import com.axen.launcher.wp7.ui.size.Size;
import com.axen.utils.ResourceUtil;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ChooseSpecialTileActivity extends Activity implements
		OnItemClickListener {
	private WP7Configuration mConf = WP7Configuration.getInstance();
	private ResourceUtil mru = mConf.getRU();
	private Size size = Size.get();
	private ListView mListView = null;
	private int mChooserPanelSize = 0;
	private String[] mTypeArrays = null;
	private ArrayAdapter mAdapter = null;

	private View mHeader = null;
	private View mBG = null;
	private TextView mTitle = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.choose_special_tile_activity);

		mHeader = findViewById(R.id.id_choose_special_tile_header);
		mBG = findViewById(R.id.id_choose_special_tile_background);
		mTitle = (TextView) findViewById(R.id.id_choose_special_tile_title);
		mTypeArrays = mru
				.getStringArray(R.array.string_arrays_special_tile_type);
		
		mListView = (ListView) findViewById(R.id.id_choose_special_tile_listview);
		if (mConf.getBackgroundColorMode() == WP7Configuration.BACKGROUND_COLOR_BLACK) {
			mAdapter = new ArrayAdapter(this, R.layout.spinner_item_white,
					mTypeArrays);
		} else if (mConf.getBackgroundColorMode() == WP7Configuration.BACKGROUND_COLOR_WHITE) {
			mAdapter = new ArrayAdapter(this, R.layout.spinner_item,
					mTypeArrays);
		}
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
		mTitle.setTextSize(size.getSetings_normal_text_size());
	}

	@Override
	protected void onResume() {
		super.onResume();
		setFullScreen(mConf.getShowStatusbar());
		mBG.setBackgroundColor(mConf.getBackgroundColor());
		mTitle.setTextColor(mConf.getTextColor());
		View v = null;
		for(int i = 0; i <  mListView.getChildCount(); i ++) {
			v = mListView.getChildAt(i);
			if(v instanceof TextView) {
				((TextView) v).setTextColor(mConf.getGrayTextColor());
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view,
			int position, long id) {
		Intent intent = new Intent();
		intent.putExtra(AddSpecialTileActivity.PICK_TILE_TYPE_INDEX, position);
		setResult(RESULT_OK, intent);
		finish();
	}

	private void setFullScreen(boolean show) {
		if (show) {
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
		} else {
			final WindowManager.LayoutParams attrs = getWindow()
					.getAttributes();
			attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
			getWindow().setAttributes(attrs);
			getWindow().clearFlags(
					WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

		}

		mHeader.setVisibility(show ? View.VISIBLE : View.GONE);
	}
	
}
