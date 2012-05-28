package com.nolanlawson.keepscore.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.nolanlawson.keepscore.R;

/**
 * Simple adapter for data objects with only two lines of text to show.
 * 
 * Useful for simple maps like Map<String, Foo>
 * @author nolan
 *
 */
public class SimpleTwoLineAdapter extends ArrayAdapter<Entry<?, ?>> {

	private SimpleTwoLineAdapter(Context context,
			Collection<? extends Entry<?, ?>> objects) {
		super(context, R.layout.spinner_dropdown_two_lines, new ArrayList<Entry<?,?>>(objects));
	}
	
	public static SimpleTwoLineAdapter create(Context context,
			Collection<? extends Entry<?, ?>> objects,
					boolean sortKeys) {
		
		List<Entry<?, ?>> sortedItems = new ArrayList<Entry<?,?>>(objects);
		
		if (sortKeys) {
			Collections.sort(sortedItems, new Comparator<Entry<?,?>>() {

				@Override
				public int compare(Entry<?, ?> lhs, Entry<?, ?> rhs) {
					return lhs.getKey().toString().compareTo(rhs.getKey().toString());
				}
			});
		}
		
		return new SimpleTwoLineAdapter(context, sortedItems);
		
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		if (view == null) {
			view = LayoutInflater.from(getContext()).inflate(R.layout.spinner_dropdown_two_lines, null, false);
		}
		
		TextView text1 = (TextView) view.findViewById(android.R.id.text1);
		TextView text2 = (TextView) view.findViewById(android.R.id.text2);
		
		Entry<?, ?> entry = getItem(position);
		text1.setText(String.valueOf(entry.getKey()));
		text2.setText(String.valueOf(entry.getValue()));
		
		return view;
	}
	
	

}
