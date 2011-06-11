package com.nolanlawson.keepscore.util;

import com.nolanlawson.keepscore.util.CollectionUtil.Function;
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
	
}

