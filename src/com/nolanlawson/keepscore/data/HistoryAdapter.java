package com.nolanlawson.keepscore.data;

import java.util.List;

import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.nolanlawson.keepscore.R;
import com.nolanlawson.keepscore.util.CollectionUtil;
import com.nolanlawson.keepscore.util.IntegerUtil;
import com.nolanlawson.keepscore.util.StringUtil;
import com.nolanlawson.keepscore.util.CollectionUtil.Function;

public class HistoryAdapter extends ArrayAdapter<HistoryItem> {

	private static final int MAX_COLUMNS_FOR_WIDE_LIST_LAYOUT = 4;
	
	private int maxNumDigits;
	private int layoutResId;
	private int numColumns;
	
	private HistoryAdapter(Context context, List<HistoryItem> items, int layoutResId, int numColumns) {
		super(context, layoutResId, items);
		this.layoutResId = layoutResId;
		this.numColumns = numColumns;
		init(items);
	}
	
	public static HistoryAdapter create(Context context, List<HistoryItem> items, int numColumns) {
		int layoutResId = getLayoutResIdForAdapter(numColumns);
		return new HistoryAdapter(context, items, layoutResId, numColumns);
	}
	
	

	private void init(List<HistoryItem> items) {
		// ensure that the delta textviews all line up correctly
		maxNumDigits = CollectionUtil.maxValue(items, new Function<HistoryItem, Integer>() {

			@Override
			public Integer apply(HistoryItem obj) {
				if (obj != null && !obj.isHideDelta()) {
					return IntegerUtil.toStringWithSign(obj.getDelta()).length();
				}
				return 0;
			}
		}, 0);
		
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {

		// view wrapper optimization per Romain Guy
		final Context context = parent.getContext();
		ViewWrapper viewWrapper;
		if (view == null) {
			LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = vi.inflate(layoutResId, parent, false);
			viewWrapper = new ViewWrapper(view);
			view.setTag(viewWrapper);
		} else {
			viewWrapper = (ViewWrapper)view.getTag();
		}
		
		// alternating colors for the background, from gray to white
		view.setBackgroundColor(context.getResources().getColor(
				(position / numColumns) % 2 == 0 ? android.R.color.background_light : R.color.light_gray));
		
		TextView textView1 = viewWrapper.getTextView1();
		TextView textView2 = viewWrapper.getTextView2();
		
		HistoryItem item;
		try {
			item = getItem(position);
		} catch (IndexOutOfBoundsException ignore) {
			setDummyTextView(textView1);
			setDummyTextView(textView2);
			return view;
		}
		
		
		if (item == null) {
			// null indicates to leave the text views empty
			setDummyTextView(textView1);
			setDummyTextView(textView2);
			return view;			
		}
		
		textView2.setVisibility(View.VISIBLE);
		
		if (item.isHideDelta()) {
			setDummyTextView(textView1);
			textView1.setVisibility(View.GONE); // set as gone to ensure that the first line isn't too tall when we use history_item_tall.xml
		} else {
			int delta = item.getDelta();
			
			SpannableString deltaSpannable = new SpannableString(StringUtil.padLeft(IntegerUtil.toStringWithSign(delta), ' ', maxNumDigits));
			
			int colorResId = delta >= 0 ? R.color.green : R.color.red;
			ForegroundColorSpan colorSpan = new ForegroundColorSpan(context.getResources().getColor(colorResId));
			deltaSpannable.setSpan(colorSpan, 0, deltaSpannable.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
			
			textView1.setVisibility(View.VISIBLE);
			textView1.setText(deltaSpannable);
		}
		
		textView2.setText(Long.toString(item.getRunningTotal()));
		
		return view;
	}
	
	
	
	@Override
	public boolean areAllItemsEnabled() {
		return false; // nothing enabled
	}

	@Override
	public boolean isEnabled(int position) {
		return false; // nothing enabled
	}
	
	/**
	 * For some reason, on Honeycomb tablets I have to set the text view to have a dummy value and the visibility to INVISIBLE - I can't just 
	 * set the text to null or empty.  If I don't, the text isn't wrapped correctly vertically.
	 * @param textView
	 */
	private static void setDummyTextView(TextView textView) {
		textView.setVisibility(View.INVISIBLE);
		textView.setText("0"); // dummy value
	}

	private static class ViewWrapper {
		private View view;
		private TextView textView1, textView2;
		
		public ViewWrapper(View view) {
			this.view = view;
		}
		
		public TextView getTextView1() {
			if (textView1 == null) {
				textView1 = (TextView) view.findViewById(android.R.id.text1);
			}
			return textView1;
		}
		
		public TextView getTextView2() {
			if (textView2 == null) {
				textView2 = (TextView) view.findViewById(android.R.id.text2);
			}
			return textView2;
		}
	}	
	
	
	private static int getLayoutResIdForAdapter(int numColumns) {
		// if there are more than 4 columns, the text gets squished together.  So I use a different layout depending on how many
		// columns there are
		return numColumns <= MAX_COLUMNS_FOR_WIDE_LIST_LAYOUT ? R.layout.history_item_wide : R.layout.history_item_tall;
	}
	
	public static View createView(Context context, HistoryItem historyItem, int numColumns, int rowId) {
		
		int layoutResId = getLayoutResIdForAdapter(numColumns);
		
		LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = vi.inflate(layoutResId, null, false);
		
		// alternating colors for the background, from gray to white
		view.setBackgroundColor(context.getResources().getColor(
				rowId % 2 == 0 ? android.R.color.background_light : R.color.light_gray));
		
		TextView textView1 = (TextView) view.findViewById(android.R.id.text1);
		TextView textView2 = (TextView) view.findViewById(android.R.id.text2);
		
		if (historyItem == null) {
			// null indicates to leave the text views empty
			setDummyTextView(textView1);
			setDummyTextView(textView2);
			return view;			
		}
		
		textView2.setVisibility(View.VISIBLE);
		
		if (historyItem.isHideDelta()) {
			setDummyTextView(textView1);
			textView1.setVisibility(View.GONE); // set as gone to ensure that the first line isn't too tall when we use history_item_tall.xml
		} else {
			int delta = historyItem.getDelta();
			
			SpannableString deltaSpannable = new SpannableString(IntegerUtil.toStringWithSign(delta));
			
			int colorResId = delta >= 0 ? R.color.green : R.color.red;
			ForegroundColorSpan colorSpan = new ForegroundColorSpan(context.getResources().getColor(colorResId));
			deltaSpannable.setSpan(colorSpan, 0, deltaSpannable.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
			
			textView1.setVisibility(View.VISIBLE);
			textView1.setText(deltaSpannable);
		}
		
		textView2.setText(Long.toString(historyItem.getRunningTotal()));
		
		return view;		
	}
}
