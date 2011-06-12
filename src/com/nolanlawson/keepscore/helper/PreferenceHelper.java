package com.nolanlawson.keepscore.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.nolanlawson.keepscore.R;

public class PreferenceHelper {

	private static int cachedUpdateDelay = -1;
	
	public static void resetCache() {
		cachedUpdateDelay = -1;
	}
	
	public static int getUpdateDelay(Context context) {
		
		if (cachedUpdateDelay == -1) {
			cachedUpdateDelay = getIntPreference(R.string.pref_update_delay, R.string.pref_update_delay_default, context);
		}
		
		return cachedUpdateDelay;
		
	}
	
	public static void setBooleanPreference(int resId, int valueResId, Context context) {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = sharedPrefs.edit();
		boolean value = Boolean.parseBoolean(context.getString(valueResId));
		editor.putBoolean(context.getString(resId), value);
		editor.commit();
	}
	
	public static void setIntPreference(int resId, int valueResId, Context context) {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = sharedPrefs.edit();
		editor.putString(context.getString(resId), context.getString(valueResId));
		editor.commit();
	}
	
	public static int getIntPreference(int resId, int defaultValueResId, Context context) {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		String defaultValue = context.getString(defaultValueResId);
		return Integer.parseInt(sharedPrefs.getString(context.getString(resId), defaultValue));
	}
	
	public static boolean getBooleanPreference(int resId, int defaultValueResId, Context context) {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		boolean defaultValue = Boolean.parseBoolean(context.getString(defaultValueResId));
		return sharedPrefs.getBoolean(context.getString(resId), defaultValue);		
	}
	
	
	
}
