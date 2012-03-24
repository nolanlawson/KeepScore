package com.nolanlawson.keepscore.util;

/**
 * As the old joke goes:
 * 
 * Q) Why is Java such a manly language?
 * 
 * A) Because it forces every developer to grow a Pair.
 * 
 * @author nolan
 *
 * @param <E>
 * @param <T>
 */
public class Pair<E,T> {

	private E first;
	private T second;
	
	public E getFirst() {
		return first;
	}
	public T getSecond() {
		return second;
	}
	
	public static <E,T> Pair<E,T> create(E first, T second) {
		Pair<E,T> result = new Pair<E,T>();
		result.first = first;
		result.second = second;
		return result;
	}
}
