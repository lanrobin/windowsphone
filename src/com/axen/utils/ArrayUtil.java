package com.axen.utils;

public class ArrayUtil {
	/**
	 * 把字符串数组转化为字符串，输出格式为
	 * [0]: strs[0]
	 * [1]: strs[1]
	 *   ...
	 * [n]: strs[n]
	 * @param strs 字符串数组对象
	 * @return 返回组装好了的数组
	 */
	public static String toString(String[] strs) {
		if(strs == null) {
			return null;
		}
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < strs.length; i++) {
			builder.append(String.format("[%d]: ", i));
			builder.append(strs[i]);
			builder.append("\n");
		}
		return builder.toString();
	}
	
	/**
	 * 把一个byte数组转换成十进制字符串。输出的格式为
	 * [12, 34, 56, ..., nn]
	 * @param arr
	 * @return
	 */
	public static String toString(byte[] arr) {
		if(arr == null) {
			return null;
		}
		
		StringBuilder builder = new StringBuilder("[");
		for(int i = 0; i < arr.length; i++) {
			builder.append(String.format("%d", i));
			if(i < arr.length - 1) {
				builder.append(", ");
			}
		}
		builder.append("]");
		return builder.toString();
	}
	
	/**
	 * 把一个byte数组转换成十进制字符串。输出的格式为
	 * [EF, D3, 3A, ..., FF]
	 * @param arr
	 * @return
	 */
	public static String toHexString(byte[] arr) {
		if(arr == null) {
			return null;
		}
		
		StringBuilder builder = new StringBuilder("[");
		for(int i = 0; i < arr.length; i++) {
			builder.append(String.format("%X", i));
			if(i < arr.length - 1) {
				builder.append(", ");
			}
		}
		builder.append("]");
		return builder.toString();
	}
}
