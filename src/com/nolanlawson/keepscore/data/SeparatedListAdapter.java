package com.nolanlawson.keepscore.data;

import java.util.LinkedHashMap;
import java.util.Map;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;

import com.nolanlawson.keepscore.R;

/**
 * ListAdapter with headers for each section.  Probably stolen from Jeff Sharkey or somewhere; can't recall.
 * @author nolan
 *
 */
public class SeparatedListAdapter extends BaseAdapter {

	public final Map<String,BaseAdapter> sections = new LinkedHashMap<String,BaseAdapter>();
	public final TypeCheckingArrayAdapter<String> headers;
	public final static int TYPE_SECTION_HEADER = 0;

	public SeparatedListAdapter(Context context) {
		headers = new TypeCheckingArrayAdapter<String>(context, R.layout.list_header);
	}
	
	public BaseAdapter getSection(int position) {
		return sections.get(headers.getItem(position));
	}
	
	public void addSection(String section, BaseAdapter adapter) {
		this.headers.add(section);
		this.sections.put(section, adapter);
	}

	public Object getItem(int position) {
		for(Object section : this.sections.keySet()) {
			Adapter adapter = sections.get(section);
			int size = adapter.getCount() + 1;

			// check if position inside this section
			if(position == 0) return section;
			if(position < size) return adapter.getItem(position - 1);

			// otherwise jump into next section
			position -= size;
		}
		return null;
	}

	public int getCount() {
		// total together all sections, plus one for each section header
		int total = 0;
		for(Adapter adapter : this.sections.values())
			total += adapter.getCount() + 1;
		return total;
	}

	public int getViewTypeCount() {
		// assume that headers count as one, then total all sections
		int total = 1;
		for(Adapter adapter : this.sections.values())
			total += adapter.getViewTypeCount();
		return total;
	}

	public int getItemViewType(int position) {
		int type = 1;
		for(Object section : this.sections.keySet()) {
			Adapter adapter = sections.get(section);
			int size = adapter.getCount() + 1;

			// check if position inside this section
			if(position == 0) return TYPE_SECTION_HEADER;
			if(position < size) return type + adapter.getItemViewType(position - 1);

			// otherwise jump into next section
			position -= size;
			type += adapter.getViewTypeCount();
		}
		return -1;
	}

	public boolean areAllItemsSelectable() {
		return false;
	}

	public boolean isEnabled(int position) {

		if (getItemViewType(position) == TYPE_SECTION_HEADER) {
			return false;
		} else {
			for(Object section : this.sections.keySet()) {
				BaseAdapter adapter = sections.get(section);
				int size = adapter.getCount() + 1;

				// check if position inside this section
				if(position < size) return adapter.isEnabled(position - 1);

				// otherwise jump into next section
				position -= size;
			}
			return false;
		}

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		int sectionnum = 0;
		for(Object section : this.sections.keySet()) {
			Adapter adapter = sections.get(section);
			int size = adapter.getCount() + 1;

			// check if position inside this section
			if(position == 0) return headers.getView(sectionnum, convertView, parent);
			if(position < size) return adapter.getView(position - 1, convertView, parent);

			// otherwise jump into next section
			position -= size;
			sectionnum++;
		}
		return null;
	}
	
	public Map<String, BaseAdapter> getSections() {
		return sections;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

}
