package com.nolanlawson.keepscore;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class NamePlayersActivity extends Activity implements OnClickListener {
	
	public static final String EXTRA_NUM_PLAYERS = "numPlayers";
	
	private List<EditText> playerEditTexts = new ArrayList<EditText>();
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
		
		playerEditTexts.add((EditText) findViewById(R.id.edit_player_1));
		playerEditTexts.add((EditText) findViewById(R.id.edit_player_2));
		playerEditTexts.add((EditText) findViewById(R.id.edit_player_3));
		playerEditTexts.add((EditText) findViewById(R.id.edit_player_4));
		playerEditTexts.add((EditText) findViewById(R.id.edit_player_5));
		playerEditTexts.add((EditText) findViewById(R.id.edit_player_6));
		playerEditTexts.add((EditText) findViewById(R.id.edit_player_7));
		playerEditTexts.add((EditText) findViewById(R.id.edit_player_8));
		
		for (int i = 0; i < playerEditTexts.size(); i++) {
			EditText playerEditText = playerEditTexts.get(i);
			if (playerEditText == null) {
				continue;
			}
			String hint = getString(R.string.text_player) + ' ' + (i + 1);
			playerEditText.setHint(hint);
			
			// get rid of any edit texts that don't fit given the number of players
			playerEditText.setVisibility(i >= numPlayers ? View.GONE : View.VISIBLE);
		}
		okButton.setOnClickListener(this);
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
