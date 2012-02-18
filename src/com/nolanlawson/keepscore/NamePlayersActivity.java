package com.nolanlawson.keepscore;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import com.nolanlawson.keepscore.helper.PreferenceHelper;

public class NamePlayersActivity extends Activity implements OnClickListener {
	
	public static final String EXTRA_NUM_PLAYERS = "numPlayers";
	
	private String[] playerHistory = new String[] {};
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

		// get player name history to populate autocomplete
		playerHistory = PreferenceHelper.getPlayerHistory(this);
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
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_players, playerHistory);
			playerEditText.setAdapter(adapter);
			if (i == numPlayers-1) {
				playerEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
			}
		}
		okButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// ok button clicked
		
		String[] playerNames = new String[numPlayers];
		
		for (int i = 0; i < numPlayers; i++) {
			playerNames[i] = playerEditTexts.get(i).getText().toString();
			PreferenceHelper.setPlayerHistory(this, playerNames[i]);
		}
		
		Intent intent = new Intent(this, GameActivity.class);
		
		intent.putExtra(GameActivity.EXTRA_PLAYER_NAMES, playerNames);
		
		startActivity(intent);
	}
}
