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
 * Like the ForegroundColorSpan, but draws a gradient from top (startColor) to
 * bottom (endColor).
 * 
 * @author nolan
 * 
 */
public class GradientSpan extends CharacterStyle implements UpdateAppearance, ParcelableSpan {

    private static final int ID = 234710600; // random

    private final int mStartColor;
    private final int mEndColor;
    private final float mStartY;
    private final float mEndY;

    public GradientSpan(int startColor, int endColor, float startY, float endY) {
        mStartColor = startColor;
        mEndColor = endColor;
        mStartY = startY;
        mEndY = endY;
    }

    public GradientSpan(Parcel src) {
        mStartColor = src.readInt();
        mEndColor = src.readInt();
        mStartY = src.readFloat();
        mEndY = src.readFloat();
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
        dest.writeFloat(mStartY);
        dest.writeFloat(mEndY);
    }

    public int getStartColor() {
        return mStartColor;
    }

    public int getEndColor() {
        return mEndColor;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        Shader shader = new LinearGradient(0, mStartY, 0, mEndY, 
                new int[] { mStartColor, mEndColor }, null,
                TileMode.CLAMP);
        ds.setShader(shader);
    }

}
