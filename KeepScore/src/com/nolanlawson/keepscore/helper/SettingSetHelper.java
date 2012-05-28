package com.nolanlawson.keepscore.helper;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.nolanlawson.keepscore.util.StringUtil;

/**
 * Helper for switching between different groups of settings. Useful for users
 * who want different increment amounts for scrabble, carcassonne, etc.
 * 
 * @author nolan
 * 
 */
public class SettingSetHelper {

	// reserved settings for figuring out what the current settings are
	public static final String META_SETTINGS = "meta_keepscore_settings";
	
	private static final String AVAILABLE_SETTINGS_SET = "available";
	
	public static boolean isValidSettingSetName(String name) {
		return !StringUtil.isEmptyOrWhitespace(name)
				&& name.length() <= 50 // come on, dude - too long
				&& !name.contains(","); // used for comma-separated list
	}

	/**
	 * Save the current setting set, overwriting anything with the previous
	 * name.
	 */
	public static void saveCurrentSettingSet(Context context, String settingSetName) {
		copySettings(getDefault(context), get(context, settingSetName));
		
		// save a new 'available' settings set to the meta settings
		Set<String> availableSet = getAvailableSettingSets(context);
		availableSet.add(settingSetName);
		Editor metaEditor = getMeta(context).edit();
		metaEditor.putString(AVAILABLE_SETTINGS_SET, TextUtils.join(",", availableSet)); // comma-separated
		metaEditor.commit();
	}

	/**
	 * Load a saved setting set into the current settings.
	 * 
	 * @param settingSetName
	 */
	public static void loadSettingSet(Context context, String settingSetName) {
		copySettings(get(context, settingSetName), getDefault(context));
	}
	
	public static Set<String> getAvailableSettingSets(Context context) {
		String available = getMeta(context).getString(AVAILABLE_SETTINGS_SET, "");
		return new HashSet<String>(StringUtil.split(available, ','));
	}
	
	public static Map<String, ?> getSettingsSet(Context context, String settingSetName) {
		return get(context, settingSetName).getAll();
	}
	

	public static void delete(Context context, String settingSetName) {
		Set<String> availableSets = getAvailableSettingSets(context);
		availableSets.remove(settingSetName);
		
		Editor editor = getMeta(context).edit();
		editor.putString(AVAILABLE_SETTINGS_SET, TextUtils.join(",", availableSets));
		editor.commit();
	}
	
	private static void copySettings(SharedPreferences source, SharedPreferences target) {
		Editor editor = target.edit();
		editor.clear();
		for (Entry<String, ?> entry : source.getAll().entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			
			if (value instanceof Boolean) {
				editor.putBoolean(key, (Boolean)value);
			} else if (value instanceof Float) {
				editor.putFloat(key, (Float)value);
			} else if (value instanceof Integer) {
				editor.putInt(key, (Integer)value);
			} else if (value instanceof Long) {
				editor.putLong(key, (Long)value);
			} else if (value instanceof String) {
				editor.putString(key, (String)value);
			}
		}
		editor.commit();
	}
	

	private static SharedPreferences getMeta(Context context) {
		return context.getSharedPreferences(META_SETTINGS, Context.MODE_PRIVATE);
	}
	
	private static SharedPreferences getDefault(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context);
	}
	
	private static SharedPreferences get(Context context, String settingsName) {
		return context.getSharedPreferences(settingsName, Context.MODE_PRIVATE);
	}
}
