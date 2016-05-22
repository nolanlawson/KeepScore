package com.nolanlawson.keepscore.widget;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.SpannedString;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.widget.TextView;

import com.nolanlawson.keepscore.android.TopDownGradientSpan;
import com.nolanlawson.keepscore.util.StringUtil;

/**
 * TextView that cuts off additional lines of text if the text is getting cut
 * off vertically.
 * 
 * @author nolan
 * 
 */
public class AutofitTextView extends TextView {

    private static final int START_ALPHA = 255;
    private static final int END_ALPHA = 32;

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

        int height = getHeight() - getCompoundPaddingBottom() - getCompoundPaddingTop();
        int maxNumLines = (height / getLineHeight());

        if (maxNumLines >= 2) {

            String oldText = getText().toString();
            int cutoffIndex = StringUtil.getNthIndexOf('\n', oldText, maxNumLines);

            if (cutoffIndex != -1) { // cut off the text

                int startOfLastLine = StringUtil.getNthIndexOf('\n', oldText, maxNumLines - 1);

                // make the last line semi-transparent, to indicate that there
                // are more results left
                // (similar to the ellipsize effect in normal text views, but
                // vertical)

                // get the original color (blue or red)
                ForegroundColorSpan[] foregroundColorSpans = ((Spanned) getText()).getSpans(startOfLastLine + 1,
                        cutoffIndex, ForegroundColorSpan.class);

                // make an alpha-ized gradient out of the original color
                int originalColor = foregroundColorSpans[0].getForegroundColor();
                int startColor = (START_ALPHA << 24) | (0x00FFFFFF & originalColor);
                int endColor = (END_ALPHA << 24) | (0x00FFFFFF & originalColor);

                int numLines = StringUtil.count(getText().subSequence(0, cutoffIndex).toString(), "\n");
                float startY = (numLines * getLineHeight());
                float endY = startY + getLineHeight();
                
                // build up a new spannable
                SpannableStringBuilder builder = new SpannableStringBuilder()
                        .append(getText().subSequence(0, startOfLastLine))
                        .append(getText().subSequence(startOfLastLine, cutoffIndex).toString());
                builder.setSpan(new TopDownGradientSpan(startColor, endColor, startY, endY), 
                	startOfLastLine, cutoffIndex, 0);
                setText(builder);

            }
        }
    }
}
