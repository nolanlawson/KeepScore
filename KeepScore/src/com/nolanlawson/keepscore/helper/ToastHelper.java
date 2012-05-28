package com.nolanlawson.keepscore.helper;

import android.content.Context;
import android.widget.Toast;

/**
 * Helper class for avoiding some boilerplate with the Toasts.
 * @author nolan
 *
 */
public class ToastHelper {

	public static void showLong(Context context, int resId, Object... args) {
		show(context, resId, Toast.LENGTH_LONG, args);
	}
	
	public static void showShort(Context context, int resId, Object... args) {
		show(context, resId, Toast.LENGTH_SHORT, args);
	}
	
	private static void show(Context context, int resId, int length, Object... args) {
		
		
		if (args.length == 0) {
			Toast.makeText(context, resId, length).show();
		} else {
			String message = context.getString(resId);
			message = String.format(message, args);
			Toast.makeText(context, message, length).show();
		}
	}
	
}
