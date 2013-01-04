package com.nolanlawson.keepscore.util;

import java.util.List;

import android.text.TextUtils;

import com.nolanlawson.keepscore.util.CollectionUtil.Function;

/**
 * Write CSV-style files, as defined by http://en.wikipedia.org/wiki/Comma-separated_values
 * @author nolan
 *
 */
public class CSVUtil {

    private static final Function<String, String> CONVERT_VALUE = new Function<String, String>() {

        @Override
        public String apply(String obj) {
            return convertValue(obj);
        }
    };
    
    /**
     * Convert a list of values into a valid CSV line.
     * @param values
     * @return
     */
    public static String convertToLine(List<String> values) {
        return TextUtils.join(",", CollectionUtil.transform(values, CONVERT_VALUE)) + "\r\n";
    }
    
    private static String convertValue(Object obj) {
        if (obj == null) {
            return "";
        }
        String str = String.valueOf(obj);
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        
        return '"' + escape(str) + '"';
    }
    
    private static String escape(String input) {
        
        return input.replace("\"", "\"\"").replace("\r", "");
        
    }
    
}
