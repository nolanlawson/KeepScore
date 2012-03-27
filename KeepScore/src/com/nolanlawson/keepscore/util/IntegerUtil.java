package com.nolanlawson.keepscore.util;

import android.text.TextUtils;

public class IntegerUtil {
	
	/**
	 * calls toString on the int, but also adds a "+" if it's nonnegative
	 * @param i
	 * @return
	 */
	public static String toStringWithSign(int i) {
		return i >= 0 ? "+" + i : Integer.toString(i);
	}
	
	public static boolean validInt(CharSequence integer) {
		if (!TextUtils.isEmpty(integer)) {
			try {
				Integer.parseInt(integer.toString());
				return true;
			} catch (NumberFormatException ex) {
			}
		}
		return false;
	}
	
}

