package com.axen.launcher.wp7.ui;

import com.axen.launcher.app.TileItemInfo;
import com.axen.launcher.app.TileManager;
import com.axen.launcher.wp7.main.R;
import com.axen.launcher.wp7.main.WP7Configuration;
import com.axen.launcher.wp7.ui.size.Size;
import com.axen.launcher.wp7.ui.widget.Tile;
import com.axen.utils.AXLog;
import com.axen.utils.ResourceUtil;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Checkable;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class TileEditActivity extends Activity implements OnClickListener {

	private static final String TAG = "TileEditActivity";

	private static final int REQUEST_IMAGE_CODE = 0x1304;

	private TileManager mTileManager = null;

	private ImageView mIconPreview = null;
	private View mChooseIcon = null;
	private View mHeader = null;
	private View mOK = null;
	private View mCancel = null;
	private EditText mTileName = null;
	private View mBackground = null;
	
	private View mHideName = null;
	private TextView mHideNameTitle = null;
	private TextView mHideNameContent = null;
	
	private View mLargeIcon = null;
	private TextView mLargeIconTitle = null;
	private TextView mLargeIconContent = null;

	private WP7Configuration mConf = WP7Configuration.getInstance();
	private ResourceUtil mru = mConf.getRU();
	private Size size = Size.get();

	private int mNarrowIconWidth = 0;
	private int mWideIconWidth = 0;

	private TileItemInfo mTileInfo = null;

	private Drawable mEdittedIcon = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_tile_activity);

		mBackground = findViewById(R.id.id_edit_tile_bg);
		mChooseIcon = findViewById(R.id.id_choose_tile_icon);
		mIconPreview = (ImageView) findViewById(R.id.id_tile_icon_preview);
		mHeader = findViewById(R.id.id_edit_tile_header);
		mOK = findViewById(R.id.id_edit_tile_ok);
		mCancel = findViewById(R.id.id_edit_tile_cancal);
		mTileName = (EditText) findViewById(R.id.id_edit_tile_name_editbox);
		
		mHideName = findViewById(R.id.id_edit_tile_hide_name);
		mHideNameTitle = (TextView)findViewById(R.id.id_edit_tile_hide_name_title);
		mHideNameContent = (TextView)findViewById(R.id.id_edit_tile_hide_name_name);
		
		mLargeIcon = findViewById(R.id.id_edit_tile_large_icon);
		mLargeIconTitle = (TextView)findViewById(R.id.id_edit_tile_large_icon_title);
		mLargeIconContent = (TextView)findViewById(R.id.id_edit_tile_large_icon_name_name);

		mNarrowIconWidth = size.getTile_icon_w(); //mru.getPexil(R.dimen.tile_icon_w);
		mWideIconWidth = size.getTile_w(); //mru.getPexil(R.dimen.tile_w);

		mHideName.setOnClickListener(this);
		mLargeIcon.setOnClickListener(this);
		mChooseIcon.setOnClickListener(this);
		mOK.setOnClickListener(this);
		mCancel.setOnClickListener(this);

		mTileManager = TileManager.getInstance();

		mTileInfo = mTileManager.getEditTile();
		mTileManager.setEditTile(null);

		if (mTileInfo == null) {
			AXLog.d(TAG, "mTileInfo is null, finish.");
			finish();
		}

		mTileName.setText(mTileInfo.tileName);
		mIconPreview.setImageDrawable(mTileInfo.getDrawable());
		if(mTileInfo.hideTileName()) {
			mHideNameContent.setText(R.string.string_on);
		}else {
			mHideNameContent.setText(R.string.string_off);
		}
		if(mTileInfo.fullSizeIcon()) {
			mLargeIconContent.setText(R.string.string_on);
		}else {
			mLargeIconContent.setText(R.string.string_off);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		setFullScreen(mConf.getShowStatusbar());
		mBackground.setBackgroundColor(mConf.getBackgroundColor());
		mHideNameTitle.setTextColor(mConf.getTextColor());
		mHideNameContent.setTextColor(mConf.getGrayTextColor());
		
		mLargeIconTitle.setTextColor(mConf.getTextColor());
		mLargeIconContent.setTextColor(mConf.getGrayTextColor());
		
		/*
		mChooseIcon.setBackgroundColor(mConf.getBackgroundColor());
		mChooseIcon.setBackgroundDrawable(mConf.getRectBack());
		mCancel.setBackgroundColor(mConf.getBackgroundColor());
		mCancel.setBackgroundDrawable(mConf.getRectBack());
		mOK.setBackgroundColor(mConf.getBackgroundColor());
		mOK.setBackgroundDrawable(mConf.getRectBack());
		*/
		mTileName.setBackgroundDrawable(mConf.getBackground());
		mTileName.setTextColor(mConf.getBackgroundColor());
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.id_edit_tile_hide_name:
			if(mTileInfo.hideTileName()) {
				mHideNameContent.setText(R.string.string_off);
				mTileInfo.clearFlag(TileItemInfo.FLAG_HIDE_TILE_NAME);
			}else {
				mHideNameContent.setText(R.string.string_on);
				mTileInfo.addFlag(TileItemInfo.FLAG_HIDE_TILE_NAME);
			}
			break;
		case R.id.id_edit_tile_large_icon:
			if(mTileInfo.fullSizeIcon()) {
				mLargeIconContent.setText(R.string.string_off);
				mTileInfo.clearFlag(TileItemInfo.FLAG_FULL_SIZE);
			}else {
				mLargeIconContent.setText(R.string.string_on);
				mTileInfo.addFlag(TileItemInfo.FLAG_FULL_SIZE);
			}
			break;
		case R.id.id_choose_tile_icon:
			// int w =
			// ((Checkable)mLargeTileIcon).isChecked()?mWideIconWidth:mNarrowIconWidth;
			// 一律按宽的
			int w = mWideIconWidth;
			AXLog.d(TAG, "crop w = " + w);
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("image/*");
			intent.putExtra("crop", "true");
			// aspectX aspectY 是宽高的比例
			intent.putExtra("aspectX", 1);
			intent.putExtra("aspectY", 1);
			// outputX outputY 是裁剪图片宽高
			intent.putExtra("outputX", w);
			intent.putExtra("outputY", w);
			intent.putExtra("return-data", true);
			startActivityForResult(intent, REQUEST_IMAGE_CODE);
			break;
		case R.id.id_edit_tile_ok:
			String tileName = mTileInfo.tileName;
			String newTileName = mTileName.getText().toString();
			if (!TextUtils.isEmpty(tileName) && tileName.equals(newTileName)) {
				// 名称变了
				mTileInfo.tileName = newTileName;
			}

			if (mEdittedIcon != null) {
				// 图标也变了
				mTileInfo.appDefaultIconDrawable = mEdittedIcon;
			}

			// 更新到数据库
			mTileManager.updateTile(mTileInfo, mEdittedIcon != null);

			Tile tile = mTileInfo.view;
			if (tile != null) {
				// 更新界面
				if (mEdittedIcon != null) {
					tile.setAppIcon(mTileInfo.appDefaultIconDrawable);
				}
				tile.requestLayout();
			}

			/** 有意让它没有break的。 */
		case R.id.id_edit_tile_cancal:
			finish();
			break;
		default:
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_IMAGE_CODE) {
			if (resultCode == RESULT_OK && data != null) {
				// Uri image = data.getData();
				Bundle extras = data.getExtras();
				if (extras != null) {
					Bitmap photo = extras.getParcelable("data");
					mEdittedIcon = new BitmapDrawable(photo);
					mIconPreview.setImageBitmap(photo);
				}
			}
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
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
