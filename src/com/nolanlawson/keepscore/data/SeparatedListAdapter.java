package com.nolanlawson.keepscore.data;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;

import com.nolanlawson.keepscore.R;



public class SeparatedListAdapter extends BaseAdapter {

	public final Map<SectionHeader,BaseAdapter> sections = new LinkedHashMap<SectionHeader,BaseAdapter>();
	public final TypeCheckingArrayAdapter<SectionHeader> headers;
	public final static int TYPE_SECTION_HEADER = 0;

	public SeparatedListAdapter(Context context) {
		headers = new TypeCheckingArrayAdapter<SectionHeader>(context, R.layout.list_header);
	}
	
	public BaseAdapter getSection(int position) {
		return sections.get(headers.getItem(position));
	}
	
	public void addSection(String sectionName, BaseAdapter adapter) {
		SectionHeader sectionHeader = new SectionHeader(sectionName);
		this.headers.add(sectionHeader);
		this.sections.put(sectionHeader, adapter);
	}

	public Object getItem(int position) {
		for(SectionHeader section : this.sections.keySet()) {
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
		for(SectionHeader section : this.sections.keySet()) {
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
			for(SectionHeader section : this.sections.keySet()) {
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
		for(SectionHeader section : this.sections.keySet()) {
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
	
	public Map<SectionHeader, BaseAdapter> getSections() {
		return sections;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	public static class SectionHeader {
		
		private static final AtomicInteger ID_COUNTER = new AtomicInteger(-1);
		
		private int id = ID_COUNTER.incrementAndGet();
		private String header;
		
		private SectionHeader(String header) {
			this.header = header;
		}
		public int getId() {
			return id;
		}
		public String getHeader() {
			return header;
		}
		@Override
		public int hashCode() {
			return id;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			SectionHeader other = (SectionHeader) obj;
			if (id != other.id)
				return false;
			return true;
		}
		@Override
		public String toString() {
			return header;
		}
	}
}
