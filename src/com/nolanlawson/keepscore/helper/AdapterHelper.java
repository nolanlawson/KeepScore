package com.nolanlawson.keepscore.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class AdapterHelper {

	/**
	 * Create sections for a SeparatedListAdapter that is designed to be put into a two-column grid view.
	 * Basically, the section elements have to be collated to allow for the fact that GridView lists
	 * its elements in left-to-right order, whereas we want them to be in top-to-bottom order.
	 * @param <T>
	 * @param sectionHeaders
	 * @param sections
	 * @return
	 */
	public static <T> List<List<T>> createSectionsForTwoColumnGridView(List<List<T>> sections) {
		List<List<T>> result = new ArrayList<List<T>>();
		
		for (int i = 0; i < sections.size(); i+= 2) {
			
			Iterator<T> leftIterator = sections.get(i).iterator();
			Iterator<T> rightIterator = i + 1 < sections.size()
					? sections.get(i + 1).iterator() 
					: Collections.<T>emptyList().iterator(); // empty for odd numbers
					
			// left header necessarily has no items, because it's immediately followed to the right
			// by the right header
			result.add(Collections.<T>emptyList());

			List<T> rightList = new ArrayList<T>();
			
			// walk through and interleave the two iterators
			while (leftIterator.hasNext() || rightIterator.hasNext()) {
				rightList.add(leftIterator.hasNext() ? leftIterator.next() : null);
				rightList.add(rightIterator.hasNext() ? rightIterator.next() : null);
			}
			result.add(rightList);
		}
		return result;
	}
}
