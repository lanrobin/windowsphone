package com.axen.launcher.wp7.ui;

import com.axen.launcher.app.TileItemInfo;
import com.axen.launcher.app.TileManager;
import com.axen.launcher.wp7.main.R;
import com.axen.launcher.wp7.main.WP7Configuration;
import com.axen.launcher.wp7.ui.widget.Tile;
import com.axen.utils.AXLog;
import com.axen.utils.ResourceUtil;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class AddSpecialTileActivity extends Activity implements OnClickListener {

	private static final String TAG = "AddSpecialTileActivity";

	private WP7Configuration mConf = WP7Configuration.getInstance();
	private ResourceUtil mru = mConf.getRU();

	private int mSelectTypeIndex = TILE_TYPE_INDEX_INVALID;
	private int mTileTypeInfo = TileItemInfo.TYPE_COMMON;

	private View mHideName = null;
	private TextView mHideNameTitle = null;
	private TextView mHideNameContent = null;

	private View mChooseApp = null;

	private View mTileType = null;
	private TextView mTileTypeTitle = null;
	private TextView mTileTypeContent = null;

	private View mOK = null;
	private View mCancel = null;
	private EditText mTileName = null;
	private View mBackground = null;
	private View mHeader = null;
	// private TextView mSpinnerView = null;
	private String[] mTypeNames = null;

	private PackageManager mPackageManager = null;
	private ComponentName mChoseApp = null;
	private TileManager mTileManager = null;
	private CharSequence mCustomTileName = null;

	private boolean mHideTileName = false;
	private static final int PICK_ACTIVITY_REQUEST_CODE = 0x1984;
	private static final int PICK_TILE_TYPE_REQUEST_CODE = 0x1985;

	public static final String PICK_TILE_TYPE_INDEX = "com.axen.launcher.wp7.ui.AddSpecialTileActivity.PICK_TILE_TYPE_INDEX";
	private static final int TILE_TYPE_INDEX_INVALID = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_special_tile_activity);

		mHideName = findViewById(R.id.id_add_special_tile_hide_name);
		mHideNameTitle = (TextView) findViewById(R.id.id_add_special_tile_hide_name_title);
		mHideNameContent = (TextView) findViewById(R.id.id_add_special_tile_hide_name_name);

		mTileType = findViewById(R.id.id_add_special_tile_type);
		mTileTypeTitle = (TextView) findViewById(R.id.id_add_special_tile_type_title);
		mTileTypeContent = (TextView) findViewById(R.id.id_add_special_tile_type_name);

		mChooseApp = findViewById(R.id.id_add_special_tile_choose_app);

		mOK = findViewById(R.id.id_add_special_tile_ok);
		mCancel = findViewById(R.id.id_add_special_tile_cancal);
		mTileName = (EditText) findViewById(R.id.id_add_special_tile_name_editbox);
		mBackground = findViewById(R.id.id_add_special_tile_background_holder);
		mHeader = findViewById(R.id.id_add_special_tile_header);

		mHideName.setOnClickListener(this);
		mChooseApp.setOnClickListener(this);
		mTileType.setOnClickListener(this);
		mOK.setOnClickListener(this);
		mCancel.setOnClickListener(this);

		mPackageManager = getPackageManager();
		mTileManager = TileManager.getInstance();

		mTypeNames = mru
				.getStringArray(R.array.string_arrays_special_tile_type);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		setFullScreen(mConf.getShowStatusbar());
		mBackground.setBackgroundColor(mConf.getBackgroundColor());
		mTileTypeTitle.setTextColor(mConf.getTextColor());
		mHideNameContent.setTextColor(mConf.getGrayTextColor());
		
		mHideNameTitle.setTextColor(mConf.getTextColor());
		mTileTypeContent.setTextColor(mConf.getGrayTextColor());
		mTileName.setBackgroundDrawable(mConf.getBackground());
		mTileName.setTextColor(mConf.getBackgroundColor());
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.id_add_special_tile_type:
			Intent intentType = new Intent();
			intentType.setClass(this, ChooseSpecialTileActivity.class);
			startActivityForResult(intentType, PICK_TILE_TYPE_REQUEST_CODE);
			break;
		case R.id.id_add_special_tile_hide_name:
			if (mHideTileName) {
				mHideNameContent.setText(R.string.string_off);
			} else {
				mHideNameContent.setText(R.string.string_on);
			}

			mHideTileName = !mHideTileName;
			break;
		case R.id.id_add_special_tile_choose_app:
			Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
			mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			Intent intent = new Intent(Intent.ACTION_PICK_ACTIVITY, null);
			intent.putExtra(Intent.EXTRA_INTENT, mainIntent);
			intent.putExtra(Intent.EXTRA_TITLE,
					getString(R.string.string_choose_app));
			startActivityForResult(intent, PICK_ACTIVITY_REQUEST_CODE);
			break;
		case R.id.id_add_special_tile_ok:
			String toastText = null;
			if (mSelectTypeIndex == TILE_TYPE_INDEX_INVALID) {
				// 如果还没选择类型，则提示要选择类型
				toastText = getString(R.string.string_choose_tile_type_to_continue);
			} else if (mChoseApp == null) {
				toastText = getString(R.string.string_choose_application_to_continue);
			} else {
				Integer drawableId = null;
				switch (mSelectTypeIndex) {
				case 0: // message
					mTileTypeInfo = TileItemInfo.TYPE_MESSAGE;
					drawableId = R.drawable.t_message;
					break;
				case 1: // phone
					mTileTypeInfo = TileItemInfo.TYPE_PHONE;
					drawableId = R.drawable.t_phone;
					break;
				case 2: // people
					mTileTypeInfo = TileItemInfo.TYPE_PEOPLE;
					break;
				case 3: // gallery
					mTileTypeInfo = TileItemInfo.TYPE_GALLERY;
					break;
				}

				Tile t = mTileManager.pinToStart(mChoseApp, drawableId,
						mTileTypeInfo, mCustomTileName);

				// 存放着，在LauncherActivity.onResume的时候增加
				mTileManager.setAddTile(t);

			}

			if (toastText != null) {
				// 如果有错，则提示。
				Toast.makeText(this, toastText, Toast.LENGTH_LONG).show();
				return;
			}

			/* 有意fall through */
		case R.id.id_add_special_tile_cancal:
			finish();
			break;
		default:
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == PICK_ACTIVITY_REQUEST_CODE) {
			if (resultCode == RESULT_OK && data != null) {
				mChoseApp = data.getComponent();
				if (mChoseApp != null) {
					try {
						mTileName
								.setText(mPackageManager
										.getApplicationLabel(mPackageManager
												.getApplicationInfo(mChoseApp
														.getPackageName(), 0)));
					} catch (NameNotFoundException e) {
						e.printStackTrace();
					}
				}
			}
			return;
		} else if (requestCode == PICK_TILE_TYPE_REQUEST_CODE) {
			if (resultCode == RESULT_OK && data != null) {
				mSelectTypeIndex = data.getIntExtra(PICK_TILE_TYPE_INDEX,
						TILE_TYPE_INDEX_INVALID);
				if (mSelectTypeIndex != TILE_TYPE_INDEX_INVALID
						&& mSelectTypeIndex < mTypeNames.length) {
					mTileTypeContent.setText(mTypeNames[mSelectTypeIndex]);
				}
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
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
