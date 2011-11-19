package com.nolanlawson.keepscore;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.GridView;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.RadioGroup.OnCheckedChangeListener;

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
	private GridView byPlayerGridView;
	private ScrollView byRoundScrollView;
	private TableLayout byRoundTableLayout;
	
	private SeparatedListAdapter byPlayerAdapter;
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
		createByRoundTableLayout();

		byPlayerGridView.setAdapter(byPlayerAdapter);
		// due to issue 3830 (http://code.google.com/p/android/issues/detail?id=3830), gridView does not properly
		// disable highlighting by trackball.  So I have to manually set the selector to transparent, which is non-optimal
		// but works OK
		byPlayerGridView.setSelector(android.R.color.transparent);
		

	}

	private void setUpWidgets() {
		radioGroup = (RadioGroup) findViewById(android.R.id.toggle);
		byRoundButton = (ToggleButton) findViewById(R.id.by_round_button);
		byPlayerButton = (ToggleButton) findViewById(R.id.by_player_button);
		
		radioGroup.setOnCheckedChangeListener(this);
		byRoundButton.setOnClickListener(this);
		byPlayerButton.setOnClickListener(this);
		
		byRoundTableLayout = (TableLayout) findViewById(R.id.by_round_list);
		byRoundScrollView = (ScrollView) findViewById(R.id.by_round_scroll_view);
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
	
	private void createByRoundTableLayout() {
		
		// make all the columns that contain history information stretchable and shrinkable,
		// i.e. not the "divider" or "row header" columns
		for (int i = 0; i < game.getPlayerScores().size(); i++) {
			byRoundTableLayout.setColumnShrinkable((i * 2) + 2, true);
			byRoundTableLayout.setColumnStretchable((i * 2) + 2, true);
		}
		
		// the 'by round' adapter simply needs each player name as a first header row, and then after that you just go round-by-round
		// summing up the values and displaying the diff, e.g.:
		// p1,  p2,  p3,  p4
		// 0,   0,   0,   0
		// +5,  +3,  -2,  +10
		// 5,   3,   2,   10
		// etc.
		
		List<PlayerScore> playerScores = game.getPlayerScores();
		int numColumns = playerScores.size() + 1; // +1 for the round number column
		
		// create the first row
		TableRow headerRow = new TableRow(this);
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		TextView emptyView = (TextView) inflater.inflate(R.layout.list_header, headerRow, false);
		emptyView.setText(" ");
		headerRow.addView(emptyView);
		
		// add in all the section headers first, so they can be laid out across as the first row
		
		for (PlayerScore playerScore : playerScores) {
			TextView playerNameView = (TextView) inflater.inflate(R.layout.list_header, headerRow, false);
			playerNameView.setText(playerScore.toDisplayName(this));
			headerRow.addView(createDividerView(headerRow));
			headerRow.addView(playerNameView);
		}
		byRoundTableLayout.addView(headerRow);
		
		List<HistoryItem> collatedHistoryItems = getCollatedHistoryItems();
		
		for (int i = 0; i < collatedHistoryItems.size(); i += playerScores.size()) {
			
			int rowId = (i / playerScores.size());
			
			TableRow tableRow = new TableRow(this);
			
			// add a column for the round number
			View roundView = inflater.inflate(R.layout.row_header, tableRow, false);
			TextView roundTextView = (TextView) roundView.findViewById(android.R.id.text1);
			String roundName = (i == 0) ? "" : Integer.toString(rowId); // first row is just the starting score
			roundTextView.setText(roundName);
			
			tableRow.addView(roundView);
			
			// add in all the history items from this round
			for (int j = i; j < i + playerScores.size(); j++) {
				HistoryItem historyItem = collatedHistoryItems.get(j);
				View historyItemAsView = HistoryAdapter.createView(this, historyItem, numColumns - 1, rowId);
				tableRow.addView(createDividerView(tableRow));
				tableRow.addView(historyItemAsView);
			}
			byRoundTableLayout.addView(tableRow);
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
	    
	    byRoundScrollView.setVisibility(isByRound ? View.VISIBLE : View.GONE);
	    byPlayerGridView.setVisibility(isByRound ? View.GONE : View.VISIBLE);
	    
	}
	
	private View createDividerView(ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		return inflater.inflate(R.layout.column_divider, parent, false);
	}
}
