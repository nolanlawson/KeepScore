package com.nolanlawson.keepscore.widget;

import android.content.Context;
import android.util.AttributeSet;

import com.nolanlawson.keepscore.helper.PlayerColor;

/**
 * Abstraction of the little square that shows a player's color.
 * @author nolan
 *
 */
public class PlayerColorView extends SquareImage {

    private PlayerColor playerColor = PlayerColor.BUILT_INS[0]; // default

    public PlayerColorView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public PlayerColorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PlayerColorView(Context context) {
        super(context);
    }

    public PlayerColor getPlayerColor() {
        return playerColor;
    }

    public void setPlayerColor(PlayerColor playerColor) {
        this.playerColor = playerColor;
        setImageDrawable(playerColor.toBackgroundDrawable(getContext()));
    }
}
