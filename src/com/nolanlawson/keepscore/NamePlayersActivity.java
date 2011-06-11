package com.nolanlawson.keepscore;

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
	
	private EditText playerOneEdit, playerTwoEdit, playerThreeEdit, playerFourEdit, playerFiveEdit, playerSixEdit;
	private Button okButton;
	
	private int numPlayers;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        

		// prevents the soft keyboard from immediately popping up
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		
        
        setContentView(R.layout.name_players);
        
        numPlayers = getIntent().getIntExtra(EXTRA_NUM_PLAYERS, 0);
        
        setUpWidgets();
    }

	private void setUpWidgets() {
		
		okButton = (Button) findViewById(android.R.id.button1);
		
		playerOneEdit = (EditText) findViewById(R.id.edit_player_1);
		playerTwoEdit = (EditText) findViewById(R.id.edit_player_2);
		playerThreeEdit = (EditText) findViewById(R.id.edit_player_3);
		playerFourEdit = (EditText) findViewById(R.id.edit_player_4);
		playerFiveEdit = (EditText) findViewById(R.id.edit_player_5);
		playerSixEdit = (EditText) findViewById(R.id.edit_player_6);
		
		// get rid of any edit texts that don't fit given the number of players
		
		playerThreeEdit.setVisibility(numPlayers >= 3 ? View.VISIBLE : View.GONE);
		playerFourEdit.setVisibility(numPlayers >= 4 ? View.VISIBLE : View.GONE);
		playerFiveEdit.setVisibility(numPlayers >= 5 ? View.VISIBLE : View.GONE);
		playerSixEdit.setVisibility(numPlayers >= 6 ? View.VISIBLE : View.GONE);
		
		okButton.setOnClickListener(this);
		
	}

	@Override
	public void onClick(View v) {
		// ok button clicked
		
		String[] playerNames = new String[numPlayers];
		
		EditText[] editTexts = new EditText[]{playerOneEdit, playerTwoEdit, playerThreeEdit, playerFourEdit,
				playerFiveEdit, playerSixEdit};
		
		for (int i = 0; i < numPlayers; i++) {
			playerNames[i] = editTexts[i].getText().toString();
		}
		
		Intent intent = new Intent(this, GameActivity.class);
		
		intent.putExtra(GameActivity.EXTRA_PLAYER_NAMES, playerNames);
		
		startActivity(intent);
	}
}
