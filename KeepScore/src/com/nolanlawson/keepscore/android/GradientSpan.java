package com.nolanlawson.keepscore.android;

import com.nolanlawson.keepscore.helper.VersionHelper;

import android.graphics.LinearGradient;
import android.graphics.Paint.FontMetrics;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.os.Parcel;
import android.text.ParcelableSpan;
import android.text.TextPaint;
import android.text.style.CharacterStyle;
import android.text.style.UpdateAppearance;

/**
 * Like the ForegroundColorSpan, but draws a gradient from top (startColor) to bottom (endColor).
 * @author nolan
 *
 */
public class GradientSpan extends CharacterStyle implements UpdateAppearance, ParcelableSpan {

    private static final int ID = 234710600; // random

    private final int mStartColor;
    private final int mEndColor;

    public GradientSpan(int startColor, int endColor) {
	mStartColor = startColor;
	mEndColor = endColor;
    }

    public GradientSpan(Parcel src) {
	mStartColor = src.readInt();
	mEndColor = src.readInt();
    }

    public int getSpanTypeId() {
	return ID;
    }

    public int describeContents() {
	return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
	dest.writeInt(mStartColor);
	dest.writeInt(mEndColor);
    }

    public int getStartColor() {
	return mStartColor;
    }
    
    public int getEndColor() {
	return mEndColor;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
	// can't figure out what the good y2 value should be.  A high enough
	// number seems to enusre it doesn't repeat, though
	float bottom = ds.getFontSpacing();
	// lineargradient is backwards post-honeyomb
	int[] gradient = VersionHelper.getVersionSdkIntCompat() >= VersionHelper.VERSION_HONEYCOMB
		? new int[]{mEndColor, mStartColor}
		: new int[]{mStartColor, mEndColor};
	Shader shader = new LinearGradient(0, 0, 0, bottom,
	            gradient, null, TileMode.MIRROR);
	ds.setShader(shader);
    }
    
}
