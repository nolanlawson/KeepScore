package com.nolanlawson.keepscore.util;

import java.util.ArrayList;
import java.util.List;

public class CollectionUtil {

	public static List<Integer> stringsToInts(List<String> list) {
		
		List<Integer> result = new ArrayList<Integer>();
		
		for (String str : list) {
			result.add(Integer.parseInt(str));
		}
		
		return result;
	}
	
	
	public static <T> List<T> reversedCopy(List<T> list) {
		List<T> result = new ArrayList<T>(list.size());
		
		for (int i = list.size() - 1; i >= 0; i--) {
			result.add(list.get(i));
		}
		
		return result;
	}
}
