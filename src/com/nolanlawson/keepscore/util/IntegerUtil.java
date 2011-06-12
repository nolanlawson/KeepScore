package com.nolanlawson.keepscore.util;

import android.text.TextUtils;

import com.nolanlawson.keepscore.util.CollectionUtil.Predicate;

public class IntegerUtil {

	public static Predicate<Integer> isNonZero() {
		return new Predicate<Integer>(){

			@Override
			public boolean apply(Integer obj) {
				return obj != 0;
			}
		};
	}
	
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

