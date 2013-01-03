package com.nolanlawson.keepscore.helper;

import android.content.Context;

import com.nolanlawson.keepscore.R;

/**
 * Defines a color scheme that can be chosen by the user from the Settings.
 * 
 * 
 * @author nolan
 *
 */
public enum ColorScheme {

    Light(
            R.color.card_background, R.color.card_text_color, R.color.blue_1, R.color.green,
            R.color.red, R.drawable.btn_default_holo_light, R.color.card_divider, R.drawable.card_shape,
            R.drawable.bubbly_background_1, android.R.color.primary_text_dark_nodisable),
    Dark(
            android.R.color.background_dark, android.R.color.secondary_text_dark_nodisable, R.color.blue_4,
            R.color.green_4, R.color.red_4, R.drawable.btn_default_transparent, R.color.light_blue_3,
            R.drawable.blue_border_shape_no_gradient, android.R.color.transparent,
            android.R.color.secondary_text_dark_nodisable),
    Android(
            R.color.android_color,
            android.R.color.primary_text_light_nodisable, R.color.blue_5, R.color.green_5, R.color.red,
            android.R.drawable.btn_default, R.color.light_blue_2, R.drawable.blue_border_shape_no_gradient,
            android.R.color.transparent, android.R.color.primary_text_light_nodisable),
    ClassicLight(
            android.R.color.background_light, android.R.color.primary_text_light_nodisable, R.color.blue_1,
            R.color.green, R.color.red, android.R.drawable.btn_default, R.color.light_blue_2,
            R.drawable.blue_border_shape_with_gradient, android.R.color.transparent,
            android.R.color.primary_text_light_nodisable),	
	;
	
    private int backgroundColorResId;
    private int foregroundColorResId;
    private int positiveColorResId;
    private int greenPositiveColorResId;
    private int negativeColorResId;
    private int buttonBackgroundDrawableResId;
    private int dividerColorResId;
    private int borderDrawableResId;
    private int playerNameBackgroundDrawableResId;
    private int playerNameTextColorResId;

    private ColorScheme(int backgroundColorResId, int foregroundColorResId, int positiveColorResId,
            int greenPositiveColorResId, int negativeColorResId, int buttonBackgroundDrawableResId,
            int dividerColorResId, int borderDrawableResId, int playerNameBackgroundDrawableResId,
            int playerNameTextColorResId) {
        this.backgroundColorResId = backgroundColorResId;
        this.foregroundColorResId = foregroundColorResId;
        this.positiveColorResId = positiveColorResId;
        this.greenPositiveColorResId = greenPositiveColorResId;
        this.negativeColorResId = negativeColorResId;
        this.buttonBackgroundDrawableResId = buttonBackgroundDrawableResId;
        this.dividerColorResId = dividerColorResId;
        this.borderDrawableResId = borderDrawableResId;
        this.playerNameBackgroundDrawableResId = playerNameBackgroundDrawableResId;
        this.playerNameTextColorResId = playerNameTextColorResId;
    }

    public int getPlayerNameTextColorResId() {
        return playerNameTextColorResId;
    }
    
    public int getPlayerNameBackgroundDrawableResId() {
        return playerNameBackgroundDrawableResId;
    }
    
    public int getGreenPositiveColorResId() {
        return greenPositiveColorResId;
    }

    public int getBorderDrawableResId() {
        return borderDrawableResId;
    }

    public int getBackgroundColorResId() {
        return backgroundColorResId;
    }

    public int getForegroundColorResId() {
        return foregroundColorResId;
    }

    public int getPositiveColorResId() {
        return positiveColorResId;
    }

    public int getNegativeColorResId() {
        return negativeColorResId;
    }

    public int getButtonBackgroundDrawableResId() {
        return buttonBackgroundDrawableResId;
    }

    public int getDividerColorResId() {
        return dividerColorResId;
    }

    public static ColorScheme findByPreference(String preference, Context context) {

        if (preference.equals(context.getString(R.string.CONSTANT_pref_color_scheme_choice_light))) {
            return Light;
        } else if (preference.equals(context.getString(R.string.CONSTANT_pref_color_scheme_choice_dark))) {
            return Dark;
        } else if (preference.equals(context.getString(R.string.CONSTANT_pref_color_scheme_choice_android))) { // Android
            return Android;
        } else { // ClassicLight
            return ClassicLight;
        }
    }
}
