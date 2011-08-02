package com.nolanlawson.keepscore.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.nolanlawson.keepscore.R;

public class PreferenceHelper {

	private static int cachedUpdateDelay = -1;
	private static ColorScheme cachedColorScheme = null;
	
	public static void resetCache() {
		cachedUpdateDelay = -1;
		cachedColorScheme = null;
	}

	public static int getDeltaButtonValue(int index, Context context) {
		switch (index) {
			case 0:
				return getIntPreference(R.string.pref_button_1, R.string.pref_button_1_default, context);
			case 1:
				return getIntPreference(R.string.pref_button_2, R.string.pref_button_2_default, context);
			case 2:
				return getIntPreference(R.string.pref_button_3, R.string.pref_button_3_default, context);
			case 3:
			default:
				return getIntPreference(R.string.pref_button_4, R.string.pref_button_4_default, context);
		}		
	}
	
	public static ColorScheme getColorScheme(Context context) {
		if (cachedColorScheme == null) {
			String pref = getStringPreference(R.string.pref_color_scheme, R.string.pref_color_scheme_default, context);
			cachedColorScheme = ColorScheme.findByPreference(pref, context);
		}
		return cachedColorScheme;
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
	
	public static void setStringPreference(int resId, int valueResId, Context context) {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = sharedPrefs.edit();
		String value = context.getString(valueResId);
		editor.putString(context.getString(resId), value);
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
	
	
	private static String getStringPreference(int resId, int defaultValueResId, Context context) {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		String defaultValue = context.getString(defaultValueResId);
		return sharedPrefs.getString(context.getString(resId), defaultValue);
	}
	
}
