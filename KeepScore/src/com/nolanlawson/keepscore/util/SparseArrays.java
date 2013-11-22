package com.nolanlawson.keepscore.util;

import android.util.SparseArray;

/**
 * Inspired by Google Guava, some helper functions for creating SparseArrays.
 * @author nolan
 *
 */
public class SparseArrays {

    public static <E> SparseArray<E> create() {
        return new SparseArray<E>();
    }
    
    public static <E> SparseArray<E> create(int key, E value) {
        SparseArray<E> result = new SparseArray<E>();
        
        result.put(key, value);
        return result;
    }
}
