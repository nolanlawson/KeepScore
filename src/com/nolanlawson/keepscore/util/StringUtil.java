package com.nolanlawson.keepscore.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.TextUtils;

import com.nolanlawson.keepscore.util.CollectionUtil.Function;

public class StringUtil {

	
	public static List<String> split(String str, char delimiter) {
		
		if (TextUtils.isEmpty(str)) {
			return Collections.emptyList();
		}
		
		List<String> split = new LinkedList<String>();
		
		int index = str.indexOf(delimiter);
		int lastIndex = 0;
		while (index != -1) {
						
			split.add(str.substring(lastIndex, index));
			
			lastIndex = index + 1;
			
			index = str.indexOf(delimiter, lastIndex);
		}
		
		split.add(str.substring(lastIndex,str.length())); // add the final string after the last delimiter
		
		return new ArrayList<String>(split);
	}
	
    /**
     * Returns a CharSequence concatenating the specified CharSequences using the specified delimiter,
     * retaining their spans if any.
     * 
     * This is mostly borrowed from TextUtils.concat();
     */
    public static CharSequence joinSpannables(String delimiter, CharSequence... text) {
        if (text.length == 0) {
            return "";
        }

        if (text.length == 1) {
            return text[0];
        }

        boolean spanned = false;
        for (int i = 0; i < text.length; i++) {
            if (text[i] instanceof  Spanned) {
                spanned = true;
                break;
            }
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length; i++) {
        	if (i > 0) {
        		sb.append(delimiter);
        	}
            sb.append(text[i]);
        }

        if (!spanned) {
            return sb.toString();
        }

        SpannableString ss = new SpannableString(sb);
        int off = 0;
        for (int i = 0; i < text.length; i++) {
            int len = text[i].length();

            if (text[i] instanceof  Spanned) {
                TextUtils.copySpansFrom((Spanned) text[i], 0, len, Object.class,
                        ss, off);
            }

            off += len + delimiter.length();
        }

        return new SpannedString(ss);
    }
	
	public static String nullToEmpty(CharSequence str) {
		return str == null ? "" : str.toString();
	}
	
	/**
	 * find the nth index of a char c in a string.  returns -1 if there aren't that many chars in the string
	 * @param c
	 * @param n
	 * @return
	 */
	public static int getNthIndexOf(char c, String str, int n) {
		
		if (n < 1) {
			throw new IllegalArgumentException("n must be >= 1: " + n);
		}
		
		int index = str.indexOf(c);
		int count = 0;
		while (index != -1 && (++count) < n) {
			index = str.indexOf(c, index + 1);
		}
		
		return index;
	}
	
	/**
	 * Pad a string on the left until it reaches the designated height
	 * @param str
	 * @param padding
	 * @param upTo
	 * @return
	 */
	public static String padLeft(String str, char padding, int upTo) {
		
		StringBuilder stringBuilder = new StringBuilder(str);
		
		while (stringBuilder.length() < upTo) {
			stringBuilder.insert(0, padding);
		}
		
		return stringBuilder.toString();
		
	}
	
	public static Function<String, Integer> lengthFunction() {
		return new Function<String, Integer>() {

			@Override
			public Integer apply(String obj) {
				return obj.length();
			}
		};
	}

	public static String emptyToNull(CharSequence name) {
		return TextUtils.isEmpty(name) ? null : name.toString();
	}
}
