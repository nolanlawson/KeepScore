package com.nolanlawson.keepscore.android;

import android.graphics.LinearGradient;
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
public class TopDownGradientSpan extends CharacterStyle implements UpdateAppearance, ParcelableSpan {

    private static final int ID = 234710600; // random

    private final int mStartColor;
    private final int mEndColor;
    private final float mStartY;
    private final float mEndY;

    public TopDownGradientSpan(int startColor, int endColor, float startY, float endY) {
        mStartColor = startColor;
        mEndColor = endColor;
        mStartY = startY;
        mEndY = endY;
    }

    public TopDownGradientSpan(Parcel src) {
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
    
    public float getStartY() {
        return mStartY;
    }
    
    public float getEndY() {
        return mEndY;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        Shader shader = new LinearGradient(0, mStartY, 0, mEndY, 
                new int[] { mStartColor, mEndColor }, null,
                TileMode.CLAMP);
        ds.setShader(shader);
    }

}
