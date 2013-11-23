package com.nolanlawson.keepscore.util;

import java.util.Comparator;

import com.nolanlawson.keepscore.util.CollectionUtil.Function;

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
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((first == null) ? 0 : first.hashCode());
        result = prime * result + ((second == null) ? 0 : second.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Pair<?,?> other = (Pair<?,?>) obj;
        if (first == null) {
            if (other.first != null)
                return false;
        } else if (!first.equals(other.first))
            return false;
        if (second == null) {
            if (other.second != null)
                return false;
        } else if (!second.equals(other.second))
            return false;
        return true;
    }
    
    public static <E extends Comparable<E>,T> Comparator<Pair<E,T>> byFirst() {
        return new Comparator<Pair<E,T>>() {

            @Override
            public int compare(Pair<E, T> lhs, Pair<E, T> rhs) {
                return lhs.getFirst().compareTo(rhs.getFirst());
            }
        };
    }
    public static <E,T extends Comparable<T>> Comparator<Pair<E,T>> bySecond() {
        return new Comparator<Pair<E,T>>() {

            @Override
            public int compare(Pair<E, T> lhs, Pair<E, T> rhs) {
                return lhs.getSecond().compareTo(rhs.getSecond());
            }
        };
    }
    public static <E, T> Function<Pair<E,T>,E> getFirstFunction() {
        return new Function<Pair<E,T>,E>() {

            @Override
            public E apply(Pair<E, T> obj) {
                return obj.getFirst();
            };
        };
    }

    public static <E, T> Function<Pair<E,T>,T> getSecondFunction() {
        return new Function<Pair<E,T>,T>() {

            @Override
            public T apply(Pair<E, T> obj) {
                return obj.getSecond();
            };
        };
    }

}
