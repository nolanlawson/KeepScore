package com.nolanlawson.keepscore.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.nolanlawson.keepscore.util.StringUtil;
import com.nolanlawson.keepscore.util.UtilLogger;

/**
 * TextView that cuts off additional lines of text if the text is getting cut off vertically.
 * @author nolan
 *
 */
public class AutofitTextView extends TextView {

	private static UtilLogger log = new UtilLogger(AutofitTextView.class);
	
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
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
	    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	    
	    int numLines = (getHeight() / getLineHeight());
	    
	    log.d("numLines is %s", numLines);
	    
	    if (numLines >= 2) {
	    
		    int newLineIndex = StringUtil.getNthIndexOf('\n', getText().toString(), numLines - 1);
		    
		    log.d("newLineIndex is %s", newLineIndex);
		    
		    if (newLineIndex != -1) {
		    	setText(getText().subSequence(0, newLineIndex));
		    }
		    
	    }
	}
	
}
