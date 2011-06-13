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
import com.nolanlawson.keepscore.util.IntegerUtil;

public class HistoryAdapter extends ArrayAdapter<HistoryItem> {

	public HistoryAdapter(Context context, List<HistoryItem> items) {
		super(context, R.layout.simple_small_list_item, items);
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {

		// view wrapper optimization per Romain Guy
		final Context context = parent.getContext();
		ViewWrapper viewWrapper;
		if (view == null) {
			LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = vi.inflate(R.layout.simple_small_list_item, parent, false);
			viewWrapper = new ViewWrapper(view);
			view.setTag(viewWrapper);
		} else {
			viewWrapper = (ViewWrapper)view.getTag();
		}
		
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
		} else {
			int delta = item.getDelta();
			
			SpannableString deltaSpannable = new SpannableString(IntegerUtil.toStringWithSign(delta));
			
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
	private void setDummyTextView(TextView textView) {
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
}
