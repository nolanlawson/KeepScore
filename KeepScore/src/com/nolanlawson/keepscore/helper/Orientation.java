package com.nolanlawson.keepscore.helper;

import android.content.Context;

import com.nolanlawson.keepscore.R;


/**
 * A user's preferred orientation in the settings.  In the future we may also add something like "Sensor" or "Automatic"
 * @author nolan
 *
 */
public enum Orientation {

    Landsdcape(R.string.CONSTANT_pref_orientation_choice_landscape),
    Portrait(R.string.CONSTANT_pref_orientation_choice_portrait),
    ;
    
    private int prefResId;
    
    private Orientation(int prefResId) {
        this.prefResId = prefResId;
    }
    
    public int getPrefResId() {
        return prefResId;
    }
    
    public static Orientation fromString(String str, Context context) {
        for (Orientation orientation : values()) {
            if (context.getString(orientation.getPrefResId()).equals(str)) {
                return orientation;
            }
        }
        return null;
    }
}
