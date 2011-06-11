package com.nolanlawson.keepscore;

import android.app.Activity;
import android.os.Bundle;
import android.widget.GridView;
import android.widget.ListView;

import com.nolanlawson.keepscore.data.HistoryAdapter;
import com.nolanlawson.keepscore.data.SeparatedListAdapter;
import com.nolanlawson.keepscore.db.Game;
import com.nolanlawson.keepscore.db.PlayerScore;
import com.nolanlawson.keepscore.util.UtilLogger;

/**
 * Activity for displaying the entire history of a game
 * @author nolan
 *
 */
public class HistoryActivity extends Activity {

	private static final UtilLogger log = new UtilLogger(HistoryActivity.class);
	
	public static final String EXTRA_GAME = "game";
	
	private ListView listView;
	
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
		
		listView = (ListView) findViewById(android.R.id.list);
		listView.setAdapter(adapter);

	}

	private void createAdapter() {
		
		adapter = new SeparatedListAdapter(this);
		
		for (PlayerScore playerScore : game.getPlayerScores()) {
			String header = playerScore.toDisplayName(this);
			HistoryAdapter subAdapter = HistoryAdapter.createFromPlayerScore(playerScore, this);
			adapter.addSection(header, subAdapter);
		}
		
	}
}
