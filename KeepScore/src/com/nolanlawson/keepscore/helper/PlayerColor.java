package com.nolanlawson.keepscore.helper;

import com.nolanlawson.keepscore.R;


/**
 * Colors applied to individual players, for the vast majority of board games that
 * have a unique color per player.
 * 
 * @author nolan
 *
 */
public enum PlayerColor {

    One      (R.drawable.player_color_selector_01, R.color.player_color_01, android.R.color.white),
    Two      (R.drawable.player_color_selector_02, R.color.player_color_02, android.R.color.white),
    Three    (R.drawable.player_color_selector_03, R.color.player_color_03, android.R.color.white),
    Four     (R.drawable.player_color_selector_04, R.color.player_color_04, android.R.color.white),
    Five     (R.drawable.player_color_selector_05, R.color.player_color_05, android.R.color.white),
    Six      (R.drawable.player_color_selector_06, R.color.player_color_06, android.R.color.white),
    Seven    (R.drawable.player_color_selector_07, R.color.player_color_07, android.R.color.white),
    Eight    (R.drawable.player_color_selector_08, R.color.player_color_08, android.R.color.white),
    Nine     (R.drawable.player_color_selector_09, R.color.player_color_09, android.R.color.white),
    Ten      (R.drawable.player_color_selector_10, R.color.player_color_10, android.R.color.black),
    Eleven   (R.drawable.player_color_selector_11, R.color.player_color_11, android.R.color.white),
    Twelve   (R.drawable.player_color_selector_12, R.color.player_color_12, android.R.color.white),
    Thirteen (R.drawable.player_color_selector_13, R.color.player_color_13, android.R.color.white),
    Fourteen (R.drawable.player_color_selector_14, R.color.player_color_14, android.R.color.white),
    Fifteen  (R.drawable.player_color_selector_15, R.color.player_color_15, android.R.color.white),
    Sixteen  (R.drawable.player_color_selector_16, R.color.player_color_16, android.R.color.white),
    ;
    
    private int selectorResId;
    private int backgroundColorResId;
    private int foregroundColorResId;
    
    private PlayerColor(int selectorResId, int backgroundColorResId, int foregroundColorResId) {
        this.selectorResId = selectorResId;
        this.backgroundColorResId = backgroundColorResId;
        this.foregroundColorResId = foregroundColorResId;
    }
    
    public int getSelectorResId() {
        return selectorResId;
    }
    public int getBackgroundColorResId() {
        return backgroundColorResId;
    }
    public int getForegroundColorResId() {
        return foregroundColorResId;
    }
    
}
