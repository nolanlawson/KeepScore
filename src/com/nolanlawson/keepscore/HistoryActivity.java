package com.nolanlawson.keepscore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.widget.GridView;

import com.nolanlawson.keepscore.data.HistoryAdapter;
import com.nolanlawson.keepscore.data.HistoryItem;
import com.nolanlawson.keepscore.data.SeparatedListAdapter;
import com.nolanlawson.keepscore.db.Game;
import com.nolanlawson.keepscore.db.PlayerScore;
import com.nolanlawson.keepscore.helper.AdapterHelper;
import com.nolanlawson.keepscore.util.UtilLogger;

/**
 * Activity for displaying the entire history of a game
 * @author nolan
 *
 */
public class HistoryActivity extends Activity {

	private static final UtilLogger log = new UtilLogger(HistoryActivity.class);
	
	public static final String EXTRA_GAME = "game";
	
	private GridView gridView;
	
	private SeparatedListAdapter adapter;
	private Game game;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.history);
		
		game = getIntent().getParcelableExtra(EXTRA_GAME);
		
		log.d("intent is %s", getIntent());
		log.d("game is %s", game);
		
		createAdapter();
		
		gridView = (GridView) findViewById(android.R.id.list);
		gridView.setAdapter(adapter);

	}

	private void createAdapter() {
		
		adapter = new SeparatedListAdapter(this);
		
		List<String> sectionHeaders = new ArrayList<String>();
		List<List<HistoryItem>> sections = new ArrayList<List<HistoryItem>>();
		
		for (PlayerScore playerScore : game.getPlayerScores()) {
			String header = playerScore.toDisplayName(this);
			List<HistoryItem> section = HistoryItem.createFromPlayerScore(playerScore, this);
			sectionHeaders.add(header);
			sections.add(section);
		}
		// fit to the GridView; i.e. interleave so that everything shows up top-to-bottom
		// rather than left-to-right
		sections = AdapterHelper.createSectionsForTwoColumnGridView(sections);
		
		for (int i = 0; i < sections.size(); i++) {
			HistoryAdapter subAdapter = new HistoryAdapter(this, sections.get(i));
			String sectionHeader = i < sectionHeaders.size() ? sectionHeaders.get(i) : "";
			adapter.addSection(sectionHeader, subAdapter);
		}
	}
}
