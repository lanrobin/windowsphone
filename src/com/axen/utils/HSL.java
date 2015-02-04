package com.axen.utils;

import android.graphics.Color;

public class HSL {

	/** 色调 */
	private float h = 0;
	/** 饱和度 */
	private float s = 0;
	/** 深度 */
	private float l = 0;

	private int alpha = 0;

	public HSL() {
	}

	public HSL(float h, float s, float l) {
		setH(h);
		setS(s);
		setL(l);
	}

	public float getH() {
		return h;
	}

	public void setH(float h) {
		if (h < 0) {
			this.h = 0;
		} else if (h > 360) {
			this.h = 360;
		} else {
			this.h = h;
		}
	}

	public float getS() {
		return s;
	}

	public void setS(float s) {
		if (s < 0) {
			this.s = 0;
		} else if (s > 255) {
			this.s = 255;
		} else {
			this.s = s;
		}
	}

	public float getL() {
		return l;
	}

	public void setL(float l) {
		if (l < 0) {
			this.l = 0;
		} else if (l > 255) {
			this.l = 255;
		} else {
			this.l = l;
		}
	}

	public int getAlpha() {
		return alpha;
	}

	public void setAlpha(int alpha) {
		this.alpha = alpha;
	}

	public String toString() {
		return "HSL {" + h + ", " + s + ", " + l + "}";
	}

	public static HSL fromARGB(int color) {

		int alpha = ((color >> 24) & 0xFF);
		int red = ((color >> 16) & 0xFF);
		int green = ((color >> 8) & 0xFF);
		int blue = (color & 0xFF);

		int[] v = new int[3];
		rgb2hsl(red, green, blue, v);
		HSL hsl = new HSL(v[0], v[1], v[2]);
		hsl.setAlpha(alpha);
		return hsl;
	}

	public static int HSL2ARGB(HSL hsl) {
		double num4 = 0.0;
		double num5 = 0.0;
		double num6 = 0.0;
		double num = hsl.getH() % 360.0;
		double num2 = hsl.getS() / 100.0;
		double num3 = hsl.getL() / 100.0;
		if (num2 == 0.0) {
			num4 = num3;
			num5 = num3;
			num6 = num3;
		} else {
			double d = num / 60.0;
			int num11 = (int) Math.floor(d);
			double num10 = d - num11;
			double num7 = num3 * (1.0 - num2);
			double num8 = num3 * (1.0 - (num2 * num10));
			double num9 = num3 * (1.0 - (num2 * (1.0 - num10)));
			switch (num11) {
			case 0:
				num4 = num3;
				num5 = num9;
				num6 = num7;
				break;
			case 1:
				num4 = num8;
				num5 = num3;
				num6 = num7;
				break;
			case 2:
				num4 = num7;
				num5 = num3;
				num6 = num9;
				break;
			case 3:
				num4 = num7;
				num5 = num8;
				num6 = num3;
				break;
			case 4:
				num4 = num9;
				num5 = num7;
				num6 = num3;
				break;
			case 5:
				num4 = num3;
				num5 = num7;
				num6 = num8;
				break;
			}
		}
		return Color.argb(hsl.getAlpha(),(int) (num4 * 255.0), (int) (num5 * 255.0),
				(int) (num6 * 255.0));
	}

	public static float RGBFromHue(float a, float b, float h) {
		if (h < 0) {
			h += 360;
		}
		if (h >= 360) {
			h -= 360;
		}
		if (h < 60) {
			return a + ((b - a) * h) / 60;
		}
		if (h < 180) {
			return b;
		}

		if (h < 240) {
			return a + ((b - a) * (240 - h)) / 60;
		}
		return a;
	}

	private static void rgb2hsl(int r, int g, int b, int hsl[]) {

		float var_R = (r / 255.0f);
		float var_G = (g / 255.0f);
		float var_B = (b / 255.0f);

		float var_Min; // Min. value of RGB
		float var_Max; // Max. value of RGB
		float del_Max; // Delta RGB value

		if (var_R > var_G) {
			var_Min = var_G;
			var_Max = var_R;
		} else {
			var_Min = var_R;
			var_Max = var_G;
		}

		if (var_B > var_Max)
			var_Max = var_B;
		if (var_B < var_Min)
			var_Min = var_B;

		del_Max = var_Max - var_Min;

		float H = 0, S, L;
		L = (var_Max + var_Min) / 2f;

		if (del_Max == 0) {
			H = 0;
			S = 0;
		} // gray
		else { // Chroma
			if (L < 0.5)
				S = del_Max / (var_Max + var_Min);
			else
				S = del_Max / (2 - var_Max - var_Min);

			float del_R = (((var_Max - var_R) / 6f) + (del_Max / 2f)) / del_Max;
			float del_G = (((var_Max - var_G) / 6f) + (del_Max / 2f)) / del_Max;
			float del_B = (((var_Max - var_B) / 6f) + (del_Max / 2f)) / del_Max;

			if (var_R == var_Max)
				H = del_B - del_G;
			else if (var_G == var_Max)
				H = (1 / 3f) + del_R - del_B;
			else if (var_B == var_Max)
				H = (2 / 3f) + del_G - del_R;
			if (H < 0)
				H += 1;
			if (H > 1)
				H -= 1;
		}
		hsl[0] = (int) (360 * H);
		hsl[1] = (int) (S * 100);
		hsl[2] = (int) (L * 100);
	}

}
