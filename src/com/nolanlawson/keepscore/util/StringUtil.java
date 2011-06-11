package com.nolanlawson.keepscore.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.text.TextUtils;

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
	
	public static String nullToEmpty(String str) {
		return str == null ? "" : str;
	}
	
}
