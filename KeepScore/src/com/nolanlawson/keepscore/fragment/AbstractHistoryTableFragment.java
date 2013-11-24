package com.nolanlawson.keepscore.fragment;

import android.app.Activity;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.TableRow;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.nolanlawson.keepscore.R;
import com.nolanlawson.keepscore.data.HistoryItem;
import com.nolanlawson.keepscore.helper.ColorScheme;
import com.nolanlawson.keepscore.helper.PreferenceHelper;
import com.nolanlawson.keepscore.util.IntegerUtil;

public abstract class AbstractHistoryTableFragment extends SherlockFragment {
    
    protected static final int MAX_COLUMNS_FOR_WIDE_LIST_LAYOUT = 4;
    protected static final int MAX_COLUMNS_FOR_REGULAR_TALL_LIST_LAYOUT = 6;

    protected abstract LayoutInflater getInflater();
    
    protected View createDividerView(ViewGroup parent) {
        return getInflater().inflate(R.layout.column_divider, parent, false);
    }

    protected View createListHeader(ViewGroup parent, CharSequence text, boolean weightIsOne, boolean gravityCenter) {
        TextView view = (TextView) getInflater().inflate(R.layout.history_column_header, parent, false);
        view.setText(text);
        if (gravityCenter) {
            view.setGravity(Gravity.CENTER_HORIZONTAL);
        }

        return weightIsOne ? setLayoutWeightToOne(view) : view;
    }

    protected View setLayoutWeightToOne(View view) {
        view.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.MATCH_PARENT, 1.0F));
        return view;
    }

    protected View createRowHeader(ViewGroup parent, CharSequence text) {
        View view = getInflater().inflate(R.layout.history_row_header, parent, false);
        TextView textView = (TextView) view.findViewById(android.R.id.text1);
        textView.setText(text);
        return view;
    }

    protected View createHistoryItemView(ViewGroup parent, HistoryItem historyItem, int layoutResId, int rowId,
            boolean weightIsOne, Activity activity) {

        View view = getInflater().inflate(layoutResId, parent, false);

        // alternating colors for the background, from gray to white
        view.setBackgroundColor(getResources().getColor(
                rowId % 2 == 0 ? android.R.color.background_light : R.color.light_gray));

        TextView textView1 = (TextView) view.findViewById(android.R.id.text1);
        TextView textView2 = (TextView) view.findViewById(android.R.id.text2);

        if (historyItem == null) {
            // null indicates to leave the text views empty
            setDummyTextView(textView1);
            setDummyTextView(textView2);
            return weightIsOne ? setLayoutWeightToOne(view) : view;
        }

        textView2.setVisibility(View.VISIBLE);

        if (historyItem.isHideDelta()) {
            setDummyTextView(textView1);
            textView1.setVisibility(View.GONE); // set as gone to ensure that
            // the first line isn't too tall
            // when we use
            // history_item_tall.xml
        } else {
            int delta = historyItem.getDelta();

            SpannableString deltaSpannable = new SpannableString(IntegerUtil.toCharSequenceWithSign(delta));

            int colorResId = delta >= 0 
                    ? (PreferenceHelper.getGreenTextPreference(activity) 
                            ? ColorScheme.Light.getGreenPositiveColorResId() // green
                            : ColorScheme.Light.getPositiveColorResId()) // blue
                    : ColorScheme.Light.getNegativeColorResId(); // red
            ForegroundColorSpan colorSpan = new ForegroundColorSpan(getResources().getColor(colorResId));
            deltaSpannable.setSpan(colorSpan, 0, deltaSpannable.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

            textView1.setVisibility(View.VISIBLE);
            textView1.setText(deltaSpannable);
        }

        textView2.setText(Long.toString(historyItem.getRunningTotal()));

        return weightIsOne ? setLayoutWeightToOne(view) : view;
    }

    /**
     * For some reason, on Honeycomb tablets I have to set the text view to have
     * a dummy value and the visibility to INVISIBLE - I can't just set the text
     * to null or empty. If I don't, the text isn't wrapped correctly
     * vertically.
     * 
     * @param textView
     */
    protected void setDummyTextView(TextView textView) {
        textView.setVisibility(View.INVISIBLE);
        textView.setText("0"); // dummy value
    }

}
