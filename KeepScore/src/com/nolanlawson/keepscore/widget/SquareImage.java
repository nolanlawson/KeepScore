package com.nolanlawson.keepscore.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class SquareImage extends ImageView {

    public static enum FixedAlong {
        width, heightd
    }

    private FixedAlong fixedAlong = FixedAlong.width;

    public SquareImage(Context context) {
        super(context);
    }

    public SquareImage(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareImage(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    int squareDimen = 1;

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int square = (fixedAlong == FixedAlong.width) ? getMeasuredWidth() : getMeasuredHeight();

        if (square > squareDimen) {
            squareDimen = square;
        }

        setMeasuredDimension(squareDimen, squareDimen);
    }

}
