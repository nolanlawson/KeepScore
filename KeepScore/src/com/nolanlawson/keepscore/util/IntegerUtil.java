package com.nolanlawson.keepscore.util;

import android.text.TextUtils;

public class IntegerUtil {

    /**
     * calls toString on the int, but also adds a "+" if it's nonnegative
     * 
     * @param i
     * @return
     */
    public static CharSequence toCharSequenceWithSign(int i) {
        return i >= 0 ? new StringBuilder("+").append(i) : Integer.toString(i);
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

    public static int parseIntOrZero(CharSequence charSequence) {
        if (!TextUtils.isEmpty(charSequence)) {
            try {
                return Integer.parseInt(charSequence.toString());
            } catch (NumberFormatException ignore) {
            }
        }
        return 0;
    }

}
