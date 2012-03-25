package com.nolanlawson.keepscore.data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.SectionIndexer;

import com.nolanlawson.keepscore.R;

/**
 * ListAdapter with headers for each section.  Probably stolen from Jeff Sharkey or somewhere; can't recall.
 * @author nolan
 *
 */
public class SeparatedListAdapter extends BaseAdapter implements SectionIndexer {
	
	public final static int TYPE_SECTION_HEADER = 0;
	
	public Map<String,BaseAdapter> sections = new LinkedHashMap<String,BaseAdapter>();
	public TypeCheckingArrayAdapter<String> headers;

	private SectionIndexer sectionIndexer;

	public SeparatedListAdapter(Context context) {
		headers = new TypeCheckingArrayAdapter<String>(context, R.layout.list_header);
	}
	
	public BaseAdapter getSection(int position) {
		return sections.get(headers.getItem(position));
	}
	
	public String getSectionName(int position) {
		return headers.getItem(position);
	}
	
	public void addSection(String section, BaseAdapter adapter) {
		this.headers.add(section);
		this.sections.put(section, adapter);
	}
	
	public void addSectionToFront(String section, BaseAdapter adapter) {
		this.headers.insert(section, 0);
		Map<String,BaseAdapter> newSections = new LinkedHashMap<String,BaseAdapter>();
		newSections.put(section, adapter);
		newSections.putAll(sections);
		sections = newSections;
	}

	public Object getItem(int position) {
		for(String section : this.sections.keySet()) {
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
		for(String section : this.sections.keySet()) {
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
		for(String section : this.sections.keySet()) {
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
	
	public Map<String, BaseAdapter> getSectionsMap() {
		return sections;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void removeSection(String section) {
		headers.remove(section);
		sections.remove(section);
	}
	
	@Override
	public int getPositionForSection(int section) {
		return getSectionIndexer().getPositionForSection(section);
	}

	@Override
	public int getSectionForPosition(int position) {
		return getSectionIndexer().getSectionForPosition(position);
	}

	@Override
	public Object[] getSections() {
		return getSectionIndexer().getSections();
	}
	
	private SectionIndexer getSectionIndexer() {
		if (sectionIndexer == null) {
			sectionIndexer = createSectionIndexer();
		}
		return sectionIndexer;
	}
	
	private SectionIndexer createSectionIndexer() {
		
		List<String> sectionNames = new ArrayList<String>();
		final List<Integer> sectionsToPositions = new ArrayList<Integer>();
		final List<Integer> positionsToSections = new ArrayList<Integer>();
		
		int runningCount = 0;
		for (Entry<String,BaseAdapter> entry : sections.entrySet()) {
			String section = entry.getKey();
			BaseAdapter subAdapter = entry.getValue();
			
			sectionNames.add(section);
			sectionsToPositions.add(runningCount);
			
			int size = subAdapter.getCount() + 1;
			for (int i = 0; i < size; i++) {
				positionsToSections.add(sectionNames.size() - 1);
			}
			runningCount += size;
		}
		
		final Object[] sectionNamesArray = sectionNames.toArray();
		
		return new SectionIndexer() {
			
			@Override
			public Object[] getSections() {
				return sectionNamesArray;
			}
			
			@Override
			public int getSectionForPosition(int position) {
				return positionsToSections.get(position);
			}
			
			@Override
			public int getPositionForSection(int section) {
				return sectionsToPositions.get(section);
			}
		};
	}
		
	
	public void refreshSections() {
		sectionIndexer = null;
		getSections();
	}
}
