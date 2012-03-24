package com.nolanlawson.keepscore.helper;

import android.content.Context;

import com.nolanlawson.keepscore.R;

public enum ColorScheme {

	Light (android.R.color.background_light, android.R.color.primary_text_light_nodisable,
			R.color.blue_1, R.color.green, R.color.red, android.R.drawable.btn_default, R.color.light_blue_2,
			R.drawable.blue_border_shape_with_gradient),
	Dark  (android.R.color.background_dark, android.R.color.secondary_text_dark_nodisable,
			R.color.blue_4, R.color.green_4, R.color.red_4, R.drawable.btn_default_transparent, R.color.light_blue_3,
			R.drawable.blue_border_shape_no_gradient),
	Android  (R.color.android_color, android.R.color.primary_text_light_nodisable,
			R.color.blue_5, R.color.green_5, R.color.red, android.R.drawable.btn_default, R.color.light_blue_2,
			R.drawable.blue_border_shape_no_gradient),
	;
	
	private int backgroundColorResId;
	private int foregroundColorResId;
	private int positiveColorResId;
	private int greenPositiveColorResId;
	private int negativeColorResId;
	private int buttonBackgroundDrawableResId;
	private int dividerColorResId;
	private int borderDrawableResId;
	
	private ColorScheme(int backgroundColorResId, int foregroundColorResId,
			int positiveColorResId, int greenPositiveColorResId, int negativeColorResId,
			int buttonBackgroundDrawableResId, int dividerColorResId,
			int borderDrawableResId) {
		this.backgroundColorResId = backgroundColorResId;
		this.foregroundColorResId = foregroundColorResId;
		this.positiveColorResId = positiveColorResId;
		this.greenPositiveColorResId = greenPositiveColorResId;
		this.negativeColorResId = negativeColorResId;
		this.buttonBackgroundDrawableResId = buttonBackgroundDrawableResId;
		this.dividerColorResId = dividerColorResId;
		this.borderDrawableResId = borderDrawableResId;
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
		
		if (preference.equals(context.getString(R.string.pref_color_scheme_choice_light))) {
			return Light;
		} else if (preference.equals(context.getString(R.string.pref_color_scheme_choice_dark))) {
			return Dark;
		} else { // Android
			return Android;
		}
	}
}
