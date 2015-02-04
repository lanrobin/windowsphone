package com.axen.utils;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;

public class ImageUtil {

	public static  Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {

		int width = bm.getWidth();

		int height = bm.getHeight();

		float scaleWidth = ((float) newWidth) / width;

		float scaleHeight = ((float) newHeight) / height;

		// create a matrix for the manipulation

		Matrix matrix = new Matrix();

		// resize the bit map

		matrix.postScale(scaleWidth, scaleHeight);

		// recreate the new Bitmap

		Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height,
				matrix, false);

		return resizedBitmap;

	}
	/**
	 * 
	 * @param bm
	 * @param num 必须是完成平方数，1，4，9
	 * @return
	 */
	public static Bitmap[] splitBitmap(Bitmap bm, int num) {
		if(num < 2) {
			return new Bitmap[] {bm};
		}
		
		if(bm == null) {
			throw new IllegalArgumentException("bm is null");
		}
		
		int sqrt = (int)Math.sqrt(num);
		int w = bm.getWidth();
		int h = bm.getHeight();
		
		int cw = w/sqrt;
		int ch = h/sqrt;
		Bitmap[] v = new Bitmap[num];
		for(int i = 0; i < sqrt; i ++) {
			for(int j = 0; j < sqrt; j ++) {
				v[i * sqrt + j] = Bitmap.createBitmap(bm, j * cw, i * ch, cw, ch);
			}
		}
		return v;
	}
	
	private static boolean isFullSquareNum(int num) {
		if(num < 1) {
			return false;
		}
		int sqrt = (int)Math.sqrt(num);
		for(int i = sqrt - 1; i < sqrt + 1; i ++) {
			if(i * i == num) {
				return true;
			}
		}
		return false;
	}
}
