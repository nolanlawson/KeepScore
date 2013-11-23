package com.nolanlawson.keepscore.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.nolanlawson.keepscore.R;

public class PreferenceHelper {

    // cache some of the frequently-accessed preferences for performance
    private static int updateDelay = -1;
    private static ColorScheme colorScheme = null;
    private static Boolean greenText = null;
    private static Boolean showRoundTotals = null;
    private static Boolean showColors = null;
    private static Integer plusButton = null;
    private static Integer minusButton = null;
    private static int[] popupButtons = null;
    private static int[] twoPlayerOnscreenButtons = null;
    private static Orientation orientation;

    public static void resetCache() {
        updateDelay = -1;
        colorScheme = null;
        greenText = null;
        showRoundTotals = null;
        showColors = null;
        plusButton = null;
        minusButton = null;
        popupButtons = null;
        twoPlayerOnscreenButtons = null;
        orientation = null;
    }
    
    public static Orientation getOrientation(Context context) {
        if (orientation == null) {
            String orientationStr = getStringPreference(
                    R.string.CONSTANT_pref_orientation, R.string.CONSTANT_pref_orientation_default, context);
            orientation = Orientation.fromString(orientationStr, context);
        }
        return orientation;
    }

    public static boolean getShowColors(Context context) {
        if (showColors == null) {
            showColors = getBooleanPreference(R.string.CONSTANT_pref_show_colors, 
                    R.string.CONSTANT_pref_show_colors_default, context);
        }
        return showColors;
    }

    public static int getPlusButtonValue(Context context) {
        
        if (plusButton == null) {
            plusButton = getIntPreference(R.string.CONSTANT_pref_plus_button,
                    R.string.CONSTANT_pref_plus_button_default, context);
        }
        return plusButton;
    }

    public static int getMinusButtonValue(Context context) {
        if (minusButton == null) {
            minusButton = getIntPreference(R.string.CONSTANT_pref_minus_button,
                    R.string.CONSTANT_pref_minus_button_default, context);
        }
        return minusButton;
    }

    public static int getPopupDeltaButtonValue(int index, Context context) {
        if (popupButtons == null) {
            popupButtons = new int[]{
                    getIntPreference(R.string.CONSTANT_pref_button_1, R.string.CONSTANT_pref_button_1_default, 
                            context),
                    getIntPreference(R.string.CONSTANT_pref_button_2, R.string.CONSTANT_pref_button_2_default,
                            context),
                    getIntPreference(R.string.CONSTANT_pref_button_3, R.string.CONSTANT_pref_button_3_default,
                            context),
                    getIntPreference(R.string.CONSTANT_pref_button_4, R.string.CONSTANT_pref_button_4_default,
                            context)    
                    
            };
        }
        return popupButtons[index];
    }

    public static int getTwoPlayerDeltaButtonValue(int index, Context context) {
        if (twoPlayerOnscreenButtons == null) {
            twoPlayerOnscreenButtons = new int[] {
                    getIntPreference(R.string.CONSTANT_pref_2p_button_1, R.string.CONSTANT_pref_2p_button_1_default,
                            context),
                    getIntPreference(R.string.CONSTANT_pref_2p_button_2, R.string.CONSTANT_pref_2p_button_2_default,
                            context),
                    getIntPreference(R.string.CONSTANT_pref_2p_button_3, R.string.CONSTANT_pref_2p_button_3_default,
                            context),
                    getIntPreference(R.string.CONSTANT_pref_2p_button_4, R.string.CONSTANT_pref_2p_button_4_default,
                            context)
            };
        }
        return twoPlayerOnscreenButtons[index];
    }

    public static boolean getShowRoundTotals(Context context) {
        if (showRoundTotals == null) {
            showRoundTotals = getBooleanPreference(R.string.CONSTANT_pref_show_round_totals,
                    R.string.CONSTANT_pref_show_round_totals_default, context);
        }
        return showRoundTotals;
    }

    public static ColorScheme getColorScheme(Context context) {
        if (colorScheme == null) {
            String pref = getStringPreference(R.string.CONSTANT_pref_color_scheme,
                    R.string.CONSTANT_pref_color_scheme_default, context);
            colorScheme = ColorScheme.findByPreference(pref, context);
        }
        return colorScheme;
    }

    public static int getUpdateDelay(Context context) {

        if (updateDelay == -1) {
            updateDelay = getIntPreference(R.string.CONSTANT_pref_update_delay,
                    R.string.CONSTANT_pref_update_delay_default, context);
        }
        return updateDelay;
    }

    public static boolean getGreenTextPreference(Context context) {

        if (greenText == null) {
            greenText = getBooleanPreference(R.string.CONSTANT_pref_green_text,
                    R.string.CONSTANT_pref_green_text_default, context);
        }
        return greenText;
    }

    public static void setBooleanPreference(int resId, boolean value, Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = sharedPrefs.edit();
        editor.putBoolean(context.getString(resId), value);
        editor.commit();
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
