package com.nolanlawson.keepscore.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class PreferenceHelper {

	public static void setPreference(int resId, int value, Context context) {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = sharedPrefs.edit();
		editor.putInt(context.getString(resId), value);
		editor.commit();
	}
	
	public static int getPreference(int resId, int defaultValueResId, Context context) {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		int defaultValue = Integer.parseInt(context.getString(defaultValueResId));
		return sharedPrefs.getInt(context.getString(resId), defaultValue);
	}
	
}
