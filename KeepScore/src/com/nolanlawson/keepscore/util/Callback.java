package com.nolanlawson.keepscore.util;

/**
 * Simple callback object.  Makes code prettier.
 * @author nolan
 *
 */
public interface Callback<E> {

    public void onCallback(E input);
}
