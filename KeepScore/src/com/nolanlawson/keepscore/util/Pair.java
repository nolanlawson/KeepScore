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
}
