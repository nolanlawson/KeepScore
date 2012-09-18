package com.nolanlawson.keepscore.util;

import java.util.LinkedList;

/**
 * Stack that pushes items off the end (i.e. the bottom of the stack) if there are more items than the specified
 * max capacity.
 * @author nolan
 *
 */
public class DataExpiringStack<E> {

	private LinkedList<E> list;
	private int capacity;
	
	public DataExpiringStack(int capacity) {
		if (capacity <= 0) {
			throw new IllegalArgumentException("capacity must be greater than 0: " + capacity);
		}
		this.capacity = capacity;
		this.list = new LinkedList<E>();
	}

	
	public void pop(E object) {
		if (list.size() == capacity) {
			// expire from the beginning of the list
			list.removeFirst();
		}
		list.addLast(object);
	}
	
	public E poll() {
		if (isEmpty()) {
			return null;
		}
		return list.removeLast();
	}
	
	public int size() {
		return list.size();
	}
	
	public E peek() {
		if (isEmpty()) {
			return null;
		}
		return list.getLast();
	}
	
	public boolean isEmpty() {
		return list.isEmpty();
	}
	
	public void clear() {
	    list.clear();
	}


	@Override
	public String toString() {
		return "DataExpiringStack [capacity=" + capacity + ", list=" + list
				+ "]";
	}
	
}
