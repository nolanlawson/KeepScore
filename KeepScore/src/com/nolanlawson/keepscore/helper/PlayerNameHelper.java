package com.nolanlawson.keepscore.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;

import com.nolanlawson.keepscore.db.GameDBHelper;
import com.nolanlawson.keepscore.util.StringUtil;

public class PlayerNameHelper {

	public static List<String> getPlayerNameSuggestions(Context context) {
		// populate player name suggestions from previous games by grabbing the
		// names from the database
		GameDBHelper dbHelper = null;
		try {
			dbHelper = new GameDBHelper(context);
			List<String> suggestions = dbHelper.findDistinctPlayerNames();
			
			List<String> filteredSuggestions = new ArrayList<String>();
			
			// filter out null/empty/whitespace names
			for (String suggestion : suggestions) {
				if (StringUtil.isEmptyOrWhitespace(suggestion)) {
					continue;
				}
				filteredSuggestions.add(suggestion.trim());
			}
			
			// sort, case insensitive
			Collections.sort(filteredSuggestions, String.CASE_INSENSITIVE_ORDER);
			
			return filteredSuggestions;
		} finally {
			if (dbHelper != null) {
				dbHelper.close();
			}
		}
	}
	
}
