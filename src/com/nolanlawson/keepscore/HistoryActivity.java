package com.nolanlawson.keepscore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.GridView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.ToggleButton;

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
public class HistoryActivity extends Activity implements OnCheckedChangeListener, OnClickListener {

	private static final UtilLogger log = new UtilLogger(HistoryActivity.class);
	
	private static final int BY_PLAYER_NUM_COLUMNS = 2;
	
	// Public service announcement: You just lost the
	public static final String EXTRA_GAME = "game";
	
	private RadioGroup radioGroup;
	private ToggleButton byRoundButton, byPlayerButton;
	private GridView byRoundGridView, byPlayerGridView;
	
	private SeparatedListAdapter byPlayerAdapter, byRoundAdapter;
	private Game game;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.history);
		
		game = getIntent().getParcelableExtra(EXTRA_GAME);
		
		log.d("intent is %s", getIntent());
		log.d("game is %s", game);
		
		setUpWidgets();
		
		createByPlayerAdapter();
		createByRoundAdapter();
		byRoundGridView.setAdapter(byRoundAdapter);
		byRoundGridView.setNumColumns(game.getPlayerScores().size());
		byPlayerGridView.setAdapter(byPlayerAdapter);
		// due to issue 3830 (http://code.google.com/p/android/issues/detail?id=3830), gridView does not properly
		// disable highlighting by trackball.  So I have to manually set the selector to transparent, which is non-optimal
		// but works OK
		byRoundGridView.setSelector(android.R.color.transparent);
		byPlayerGridView.setSelector(android.R.color.transparent);
		

	}

	private void setUpWidgets() {
		radioGroup = (RadioGroup) findViewById(android.R.id.toggle);
		byRoundButton = (ToggleButton) findViewById(R.id.by_round_button);
		byPlayerButton = (ToggleButton) findViewById(R.id.by_player_button);
		
		radioGroup.setOnCheckedChangeListener(this);
		byRoundButton.setOnClickListener(this);
		byPlayerButton.setOnClickListener(this);
		
		byRoundGridView = (GridView) findViewById(R.id.by_round_list);
		byPlayerGridView = (GridView) findViewById(R.id.by_player_list);
		
	}

	private void createByPlayerAdapter() {
		
		byPlayerAdapter = new SeparatedListAdapter(this);
		
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
			HistoryAdapter subAdapter = HistoryAdapter.create(this, sections.get(i), BY_PLAYER_NUM_COLUMNS);
			String sectionHeader = i < sectionHeaders.size() ? sectionHeaders.get(i) : "";
			byPlayerAdapter.addSection(sectionHeader, subAdapter);
		}
	}
	
	private void createByRoundAdapter() {
		byRoundAdapter = new SeparatedListAdapter(this);
		// the 'by round' adapter simply needs each player name as a first header row, and then after that you just go round-by-round
		// summing up the values and displaying the diff, e.g.:
		// p1,  p2,  p3,  p4
		// 0,   0,   0,   0
		// +5,  +3,  -2,  +10
		// 5,   3,   2,   10
		// etc.
		
		// add in all the section headers first, so they can be laid out across as the first row
		List<PlayerScore> playerScores = game.getPlayerScores();
		for (int i = 0; i < playerScores.size(); i++) {
			PlayerScore playerScore = playerScores.get(i);
			
			List<HistoryItem> historyItems;
			if (i < game.getPlayerScores().size() - 1) {
				historyItems = Collections.emptyList();
			} else {
				// the final header needs to hold all the items, because it's left-to-right
				// so we need to collate all the items
				historyItems = getCollatedHistoryItems();
				
			}
			HistoryAdapter section = HistoryAdapter.create(this, historyItems, playerScores.size());
			byRoundAdapter.addSection(playerScore.toDisplayName(this), section);
		}
		
	}

	private List<HistoryItem> getCollatedHistoryItems() {
		
		// get all the iterators for the history items
		List<Iterator<HistoryItem>> playerHistoryItems = new ArrayList<Iterator<HistoryItem>>();
		for (PlayerScore playerScore : game.getPlayerScores()) {
			List<HistoryItem> historyItems = HistoryItem.createFromPlayerScore(playerScore, this);
			playerHistoryItems.add(historyItems.iterator());
		}
		
		List<HistoryItem> collatedItems = new ArrayList<HistoryItem>();
		
		// collate
		while (!allIteratorsAreEmpty(playerHistoryItems)) {
			for (Iterator<HistoryItem> iterator : playerHistoryItems) {
				if (iterator.hasNext()) {
					collatedItems.add(iterator.next());
				} else {
					// add an empty item
					collatedItems.add(null);
				}
			}
		}
		return collatedItems;
	}

	private boolean allIteratorsAreEmpty(List<Iterator<HistoryItem>> iterators) {
		for (Iterator<HistoryItem> iterator : iterators) {
			if (iterator.hasNext()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
		// uncheck the other one
		for (ToggleButton button : new ToggleButton[]{byPlayerButton, byRoundButton}) {
            button.setChecked(button.getId() == checkedId);
        }
		
	}
	
	@Override
	public void onClick(View view) {
	    ((RadioGroup)view.getParent()).check(view.getId());
	    
	    // switch from round view to player view or vice versa
	    boolean isByRound = (view.getId() == R.id.by_round_button);
	    
	    byRoundGridView.setVisibility(isByRound ? View.VISIBLE : View.GONE);
	    byPlayerGridView.setVisibility(isByRound ? View.GONE : View.VISIBLE);
	    
	}
}
