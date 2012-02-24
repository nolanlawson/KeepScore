package com.nolanlawson.keepscore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import com.nolanlawson.keepscore.db.GameDBHelper;
import com.nolanlawson.keepscore.util.StringUtil;

public class NamePlayersActivity extends Activity implements OnClickListener {
	
	public static final String EXTRA_NUM_PLAYERS = "numPlayers";
	
	private List<AutoCompleteTextView> playerEditTexts = new ArrayList<AutoCompleteTextView>();
	private Button okButton;
	
	private int numPlayers;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		// prevents the soft keyboard from immediately popping up
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		numPlayers = getIntent().getIntExtra(EXTRA_NUM_PLAYERS, 0);
        
		int contentResId = numPlayers < 7 
				? R.layout.name_players_2_to_6 
				: R.layout.name_players_7_to_8;
		
        setContentView(contentResId);
        
        setUpWidgets();
    }

	private void setUpWidgets() {

		okButton = (Button) findViewById(android.R.id.button1);
		
		playerEditTexts.add((AutoCompleteTextView) findViewById(R.id.edit_player_1));
		playerEditTexts.add((AutoCompleteTextView) findViewById(R.id.edit_player_2));
		playerEditTexts.add((AutoCompleteTextView) findViewById(R.id.edit_player_3));
		playerEditTexts.add((AutoCompleteTextView) findViewById(R.id.edit_player_4));
		playerEditTexts.add((AutoCompleteTextView) findViewById(R.id.edit_player_5));
		playerEditTexts.add((AutoCompleteTextView) findViewById(R.id.edit_player_6));
		playerEditTexts.add((AutoCompleteTextView) findViewById(R.id.edit_player_7));
		playerEditTexts.add((AutoCompleteTextView) findViewById(R.id.edit_player_8));
		
		for (int i = 0; i < playerEditTexts.size(); i++) {
			AutoCompleteTextView playerEditText = playerEditTexts.get(i);
			if (playerEditText == null) {
				continue;
			}
			String hint = getString(R.string.text_player) + ' ' + (i + 1);
			playerEditText.setHint(hint);
			
			// get rid of any edit texts that don't fit given the number of players
			playerEditText.setVisibility(i >= numPlayers ? View.GONE : View.VISIBLE);
			
			// final edit text does "action done"
			if (i == numPlayers-1) {
				playerEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
			}
		}
		okButton.setOnClickListener(this);
		
		fetchPlayerNameSuggestions();
	}

	private void fetchPlayerNameSuggestions() {
		
		// fetch the player name suggestions from the database, in the background, to avoid UI slowdown
		
		new AsyncTask<Void, Void, List<String>>(){

			@Override
			protected List<String> doInBackground(Void... arg0) {
				return getPlayerNameSuggestions();
			}

			@Override
			protected void onPostExecute(List<String> result) {
				super.onPostExecute(result);
				
				for (AutoCompleteTextView playerEditText : playerEditTexts) {
					if (playerEditText == null) {
						continue;
					}
					ArrayAdapter<String> adapter = new ArrayAdapter<String>(NamePlayersActivity.this, 
							R.layout.simple_dropdown_small, result);
					playerEditText.setAdapter(adapter);
				}
			}
			
		}.execute((Void)null);
		
	}

	private List<String> getPlayerNameSuggestions() {
		// populate player name suggestions from previous games by grabbing the
		// names from the database
		GameDBHelper dbHelper = null;
		try {
			dbHelper = new GameDBHelper(this);
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

	@Override
	public void onClick(View v) {
		// ok button clicked
		
		String[] playerNames = new String[numPlayers];
		
		for (int i = 0; i < numPlayers; i++) {
			playerNames[i] = playerEditTexts.get(i).getText().toString();
		}
		
		Intent intent = new Intent(this, GameActivity.class);
		
		intent.putExtra(GameActivity.EXTRA_PLAYER_NAMES, playerNames);
		
		startActivity(intent);
	}
}
