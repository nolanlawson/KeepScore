package com.nolanlawson.keepscore.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.nolanlawson.keepscore.util.UtilLogger;

public class SquareImage extends ImageView {

    private static UtilLogger log = new UtilLogger(SquareImage.class);
    
    public static enum FixedAlong {
        width, height
    }

    private FixedAlong fixedAlong = FixedAlong.height;

    public SquareImage(Context context) {
        super(context);
    }

    public SquareImage(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareImage(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int squareDimen = (fixedAlong == FixedAlong.width) ? getMeasuredWidth() : getMeasuredHeight();

        log.d("dimensions are %s", squareDimen);
        
        setMeasuredDimension(squareDimen, squareDimen);
    }

}
