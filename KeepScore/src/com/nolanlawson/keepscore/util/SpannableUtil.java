package com.nolanlawson.keepscore.util;

import android.text.Spannable;
import android.text.Spanned;

public class SpannableUtil {

	/**
	 * Set a span over the entire spannable.
	 * @param spannable
	 * @param what
	 */
	public static void setWholeSpan(Spannable spannable, Object what) {
		spannable.setSpan(what, 0, spannable.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
	}
}
