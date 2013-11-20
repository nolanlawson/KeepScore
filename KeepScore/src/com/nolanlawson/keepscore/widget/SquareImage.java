package com.nolanlawson.keepscore.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.nolanlawson.keepscore.R;
import com.nolanlawson.keepscore.util.UtilLogger;

/**
 * Image view that scales its height to match its width or vice-versa.  By default, height is matched to width.
 * Can be changed using attribute "fixedOnHeight"
 * @author nolan
 *
 */
public class SquareImage extends ImageView {

    private static UtilLogger log = new UtilLogger(SquareImage.class);
    
    public static enum FixedAlong {
        width, height
    }

    private FixedAlong fixedAlong = FixedAlong.width;

    public SquareImage(Context context) {
        super(context);
    }

    public SquareImage(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public SquareImage(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }
    
    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs,
                    R.styleable.SquareImage);
            boolean fixedOnHeight = typedArray.getBoolean(R.styleable.SquareImage_fixedOnHeight, false);
            fixedAlong = fixedOnHeight ? FixedAlong.height : FixedAlong.width;
            typedArray.recycle();
        }        
    }
    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int squareDimen = (fixedAlong == FixedAlong.width) ? getMeasuredWidth() : getMeasuredHeight();

        log.d("dimensions are %s", squareDimen);
        
        setMeasuredDimension(squareDimen, squareDimen);
    }

}
