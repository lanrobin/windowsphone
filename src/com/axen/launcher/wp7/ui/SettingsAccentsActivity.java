package com.axen.launcher.wp7.ui;

import java.util.ArrayList;
import java.util.List;

import com.axen.launcher.app.AccentsManager;
import com.axen.launcher.wp7.main.R;
import com.axen.launcher.wp7.main.WP7Configuration;
import com.axen.launcher.wp7.ui.size.Size;
import com.axen.launcher.wp7.ui.widget.AccentAdapter;
import com.axen.launcher.wp7.ui.widget.AccentAdapter.AccentData;
import com.axen.utils.ResourceUtil;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class SettingsAccentsActivity extends Activity implements OnItemClickListener {

	private WP7Configuration mConf = WP7Configuration.getInstance();
	private ResourceUtil mru = mConf.getRU();
	private Size size = Size.get();
	private ListView mListView = null;
	private int mChooserPanelSize = 0;
	
	private List<AccentData> mDatas = new ArrayList<AccentData>();
	private AccentAdapter mAdapter = null;

	private View mHeader = null;;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_accents_activity);
		
		mHeader = findViewById(R.id.id_settins_accents_header);
		
		mChooserPanelSize = size.getSettings_accents_chooser_size(); //mru.getPexil(R.dimen.settings_accents_chooser_size);
		
		mDatas.clear();
		
		for(int i = 0; i < AccentsManager.COLORS.length; i ++) {
			mDatas.add(new AccentData(AccentsManager.DRAWABLES[i], AccentsManager.COLORS[i], AccentsManager.STRINGS[i]));
		}
		
		mAdapter = new AccentAdapter(this, mDatas, mChooserPanelSize, mConf.getAccentColorIndex());
		
		mListView = (ListView)findViewById(R.id.id_accent_listview);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		setFullScreen(mConf.getShowStatusbar());
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
		AccentData ad = (AccentData)view.getTag();
		if(ad != null) {
			Intent intent = new Intent();
			intent.putExtra(AccentData.ACCENT_DATA_COLOR_INDEX, position);
			setResult(RESULT_OK, intent);
		}
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
		
		mHeader .setVisibility(show?View.VISIBLE:View.GONE);
	}
}
