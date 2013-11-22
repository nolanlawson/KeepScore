package com.nolanlawson.keepscore.helper;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;

import com.nolanlawson.keepscore.R;

/**
 * Colors applied to individual players, for the vast majority of board games
 * that have a unique color per player.
 * 
 * @author nolan
 * 
 */
public abstract class PlayerColor {

    public abstract Drawable toBackgroundDrawable(Context context);
    public abstract int toColor(Context context);
    
    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public static class BuiltInPlayerColor extends PlayerColor {

        private int backgroundResId;
        private int colorResId;
        private int ordinal;

        private BuiltInPlayerColor(int backgroundResId, int colorResId, int ordinal) {
            this.backgroundResId = backgroundResId;
            this.colorResId = colorResId;
            this.ordinal = ordinal;
        }

        public Drawable toBackgroundDrawable(Context context) {
            return context.getResources().getDrawable(backgroundResId);
        }
        
        public int toColor(Context context) {
            return context.getResources().getColor(colorResId);
        }
        
        public int getOrdinal() {
            return ordinal;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + backgroundResId;
            result = prime * result + ordinal;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof BuiltInPlayerColor && this.getOrdinal() == ((BuiltInPlayerColor)obj).getOrdinal();
        }

        @Override
        public String toString() {
            return "BuiltInPlayerColor [backgroundResId=" + backgroundResId + ", ordinal=" + ordinal + "]";
        }
    }

    public static final BuiltInPlayerColor[] BUILT_INS = {
            new BuiltInPlayerColor(R.drawable.player_color_selector_01, R.color.player_color_01, 0),
            new BuiltInPlayerColor(R.drawable.player_color_selector_02, R.color.player_color_02, 1),
            new BuiltInPlayerColor(R.drawable.player_color_selector_03, R.color.player_color_03, 2),
            new BuiltInPlayerColor(R.drawable.player_color_selector_04, R.color.player_color_04, 3),
            new BuiltInPlayerColor(R.drawable.player_color_selector_05, R.color.player_color_05, 4),
            new BuiltInPlayerColor(R.drawable.player_color_selector_06, R.color.player_color_06, 5),
            new BuiltInPlayerColor(R.drawable.player_color_selector_07, R.color.player_color_07, 6),
            new BuiltInPlayerColor(R.drawable.player_color_selector_08, R.color.player_color_08, 7),
            new BuiltInPlayerColor(R.drawable.player_color_selector_09, R.color.player_color_09, 8),
            new BuiltInPlayerColor(R.drawable.player_color_selector_10, R.color.player_color_10, 9),
            new BuiltInPlayerColor(R.drawable.player_color_selector_11, R.color.player_color_11, 10),
            new BuiltInPlayerColor(R.drawable.player_color_selector_12, R.color.player_color_12, 11),
            new BuiltInPlayerColor(R.drawable.player_color_selector_13, R.color.player_color_13, 12),
            new BuiltInPlayerColor(R.drawable.player_color_selector_14, R.color.player_color_14, 13),
            new BuiltInPlayerColor(R.drawable.player_color_selector_15, R.color.player_color_15, 14),
            new BuiltInPlayerColor(R.drawable.player_color_selector_16, R.color.player_color_16, 15)
    };

    public static class CustomPlayerColor extends PlayerColor {

        private int color;

        /**
         * Create a color from a normal Android-style int definition like
         * #ff000000 (black)
         * 
         * @param color
         */
        public CustomPlayerColor(int color) {
            this.color = color;
        }

        @Override
        public Drawable toBackgroundDrawable(Context context) {
            // build up a nice dynamic state list drawable with rounded corners
            // as an aside, the selected state is not necessary, because it's never shown in the 4x4 grid
            
            GradientDrawable normal = (GradientDrawable)context.getResources().getDrawable(
                    R.drawable.player_color_template).mutate();
            GradientDrawable pressed = (GradientDrawable)context.getResources().getDrawable(
                    R.drawable.player_color_template_pressed).mutate();
            
            normal.setColor(color);
            pressed.setColor(color);
            
            StateListDrawable states = new StateListDrawable();
            states.addState(new int[]{ -android.R.attr.state_pressed}, normal);
            states.addState(new int[]{ android.R.attr.state_pressed}, pressed);
            
            return states;
        }
        
        public int getColor() {
            return color;
        }
        
        public int toColor(Context context) {
            return color;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + color;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            CustomPlayerColor other = (CustomPlayerColor) obj;
            if (color != other.color)
                return false;
            return true;
        }
        
        
    }
    
    public static String serialize(PlayerColor playerColor) {
        if (playerColor instanceof BuiltInPlayerColor) {
            return Integer.toString(((BuiltInPlayerColor)playerColor).getOrdinal());
        } else { // custom
            return new StringBuilder("#")
                    .append(Integer.toHexString(((CustomPlayerColor)playerColor).getColor()))
                    .toString();
        }
    }
    
    public static PlayerColor deserialize(String str) {
        if (str.startsWith("#")) {
            return new CustomPlayerColor(Color.parseColor(str));
        } else {
            return BUILT_INS[Integer.parseInt(str)];
        }
    }
}
