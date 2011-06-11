package com.nolanlawson.keepscore.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class StringUtil {

	
	public static List<String> split(String str, char delimiter) {
		List<String> split = new LinkedList<String>();
		
		int index = str.indexOf(delimiter);
		int lastIndex = 0;
		while (index != -1) {
			
			split.add(str.substring(lastIndex, index));
			
			lastIndex = index + 1;
			
			index = str.indexOf(delimiter, lastIndex);
		}
		
		return new ArrayList<String>(split);
	}
	
	public static String nullToEmpty(String str) {
		return str == null ? "" : str;
	}
	
}
