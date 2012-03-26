package com.nolanlawson.keepscore.data;

import java.util.ArrayList;
import java.util.Collection;
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
public class SeparatedListAdapter<T extends BaseAdapter> extends BaseAdapter implements SectionIndexer {
	
	private static final int MIN_NUM_SECTIONS_FOR_SECTION_OVERLAYS = 2;
	private static final int MIN_NUM_ITEMS_FOR_SECTION_OVERLAYS = 10;
	
	public static final int TYPE_SECTION_HEADER = 0;
	
	public Map<String,T> sections = new LinkedHashMap<String,T>();
	public TypeCheckingArrayAdapter<String> headers;

	private SectionIndexer sectionIndexer;

	public SeparatedListAdapter(Context context) {
		headers = new TypeCheckingArrayAdapter<String>(context, R.layout.list_header);
	}
	
	public T getSection(int position) {
		return sections.get(headers.getItem(position));
	}
	
	public String getSectionName(int position) {
		return headers.getItem(position);
	}
	
	public Collection<T> getSubAdapters() {
		return sections.values();
	}
	
	public void addSection(String section, T adapter) {
		this.headers.add(section);
		this.sections.put(section, adapter);
	}
	
	public void addSectionToFront(String section, T adapter) {
		this.headers.insert(section, 0);
		Map<String,T> newSections = new LinkedHashMap<String,T>();
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
				T adapter = sections.get(section);
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
	
	public Map<String, T> getSectionsMap() {
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
		
		if (!enoughToShowOverlays()) {
			return createEmptySectionIndexer();
		}
		
		List<String> sectionNames = new ArrayList<String>();
		final List<Integer> sectionsToPositions = new ArrayList<Integer>();
		final List<Integer> positionsToSections = new ArrayList<Integer>();
		
		int runningCount = 0;
		for (Entry<String,T> entry : sections.entrySet()) {
			String section = entry.getKey();
			T subAdapter = entry.getValue();
			
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
		
	
	private SectionIndexer createEmptySectionIndexer() {
		final Object[] empty = {};
		return new SectionIndexer() {
			
			@Override
			public Object[] getSections() {
				return empty;
			}
			
			@Override
			public int getSectionForPosition(int position) {
				return 0;
			}
			
			@Override
			public int getPositionForSection(int section) {
				return 0;
			}
		};
	}

	public void refreshSections() {
		sectionIndexer = null;
		getSections();
	}
	
	private boolean enoughToShowOverlays() {
		int numHeaders = headers.getCount();
		return numHeaders >= MIN_NUM_SECTIONS_FOR_SECTION_OVERLAYS && 
				(getCount() - numHeaders) >= MIN_NUM_ITEMS_FOR_SECTION_OVERLAYS;
	}
}
