package com.nolanlawson.keepscore.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.widget.TextView;

import com.nolanlawson.keepscore.util.StringUtil;

/**
 * TextView that cuts off additional lines of text if the text is getting cut
 * off vertically.
 * 
 * @author nolan
 * 
 */
public class AutofitTextView extends TextView {

    private boolean isOverflowing = false;

    public AutofitTextView(Context context, AttributeSet attrs, int defStyle) {
	super(context, attrs, defStyle);
	init();
    }

    public AutofitTextView(Context context, AttributeSet attrs) {
	super(context, attrs);
	init();
    }

    public AutofitTextView(Context context) {
	super(context);
	init();
    }

    private void init() {
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	super.onMeasure(widthMeasureSpec, heightMeasureSpec);

	int height = getHeight() - getPaddingBottom() - getPaddingTop();
	int numLines = (height / getLineHeight());

	if (numLines >= 2) {
	    String oldText = getText().toString();
	    int newLineIndex = StringUtil.getNthIndexOf('\n', oldText, numLines);

	    if (newLineIndex != -1) {
		setText(getText().subSequence(0, newLineIndex));
	    }
	    isOverflowing = newLineIndex < oldText.length();
	} else {
	    isOverflowing = false;
	}
    }

    @Override
    protected void onDraw(Canvas canvas) {
	super.onDraw(canvas);

	if (isOverflowing) {

	    // add a manual vertical "marquee" effect to demonstrate that there
	    // are more numbers below the bottom one

	    Rect bounds = new Rect();
	    canvas.getClipBounds(bounds);

	    int halfTextSize = Math.round(getTextSize() / 2);

	    int numLines = StringUtil.count(getText().toString(), "\n") - 1;
	    
	    // make the rectangle cover up the bottom half of the last text line
	    
	    int rectTop = Math.max(bounds.top,
		    bounds.top + (numLines * getLineHeight()) - (getLineHeight() / 2));

	    Rect rect = new Rect(0, rectTop, bounds.right, bounds.bottom);

	    Paint paint = new Paint();
	    paint.setColor(Color.WHITE);
	    paint.setStyle(Style.FILL);
	    paint.setAlpha(128);

	    canvas.drawRect(rect, paint);
	}
    }
}
