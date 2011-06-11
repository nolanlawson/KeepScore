package com.nolanlawson.keepscore.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class CollectionUtil {

	public static <E,T extends Comparable<T>> T maxValue(List<E> list, Function<E,T> function) {
		
		T max = null;
		
		for (E obj : list) {
			T value = function.apply(obj);
			if (max == null || value.compareTo(max) > 0) {
				max = value;
			}
		}
		
		return max;
		
	}
	
	public static <E,T,K> Function<E, K> compose(final Function<E, T> function1, final Function<T, K> function2) {
		return new Function<E,K>(){

			@Override
			public K apply(E obj) {
				return function2.apply(function1.apply(obj));
			}
		};
	}
	
	public static <T> Function<T, String> toStringFunction() {
		return new Function<T, String>(){

			@Override
			public String apply(T obj) {
				return obj.toString();
			}
		};
	}
	
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
	
	public static <T> List<T> filter(List<T> list, Predicate<T> predicate) {
		
		List<T> filtered = new ArrayList<T>();
		
		for (T object : list) {
			if (predicate.apply(object)) {
				filtered.add(object);
			}
		}
		
		return filtered;
		
	}
	
	public static <E,T> List<T> transform(List<E> list, Function<E,T> function) {
		List<T> result = new ArrayList<T>();
		
		for (E object : list) {
			result.add(function.apply(object));
		}
		
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T[] toArray(List<T> list, Class<T> clazz) {
		return list.toArray((T[])Array.newInstance(clazz, list.size()));
	}
	
	public static interface Function<E,T> {
		
		public T apply(E obj);
	}
	
	public static interface Predicate<T> {
		
		public boolean apply(T obj);
	}
}
