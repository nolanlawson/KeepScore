package com.nolanlawson.keepscore;

import java.util.ArrayList;
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

import com.nolanlawson.keepscore.helper.PlayerColor;
import com.nolanlawson.keepscore.helper.PlayerNameHelper;
import com.nolanlawson.keepscore.widget.SquareImage;

public class NamePlayersActivity extends Activity implements OnClickListener {
	
	public static final String EXTRA_NUM_PLAYERS = "numPlayers";
	
	private static final int[] PLAYER_VIEW_IDS = {
	    R.id.player_and_color_1,
	    R.id.player_and_color_2,
	    R.id.player_and_color_3,
	    R.id.player_and_color_4,
	    R.id.player_and_color_5,
	    R.id.player_and_color_6,
	    R.id.player_and_color_7,
	    R.id.player_and_color_8
	};
    
	private List<AutoCompleteTextView> playerEditTexts = new ArrayList<AutoCompleteTextView>();
	private List<SquareImage> playerColorImageViews = new ArrayList<SquareImage>();
	private List<View> playerViews = new ArrayList<View>();
	private Button okButton;
	
	private int numPlayers;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		// prevents the soft keyboard from immediately popping up
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		numPlayers = getIntent().getIntExtra(EXTRA_NUM_PLAYERS, 0);
        
		int contentResId = R.layout.name_players;
		
        setContentView(contentResId);
        
        setUpWidgets();
    }
	
	

	@Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        
        numPlayers = savedInstanceState.getInt(EXTRA_NUM_PLAYERS);
        
        String[] playerNames = savedInstanceState.getStringArray(GameActivity.EXTRA_PLAYER_NAMES);
        int[] playerColors = savedInstanceState.getIntArray(GameActivity.EXTRA_PLAYER_COLORS);
        for (int i = 0; i < numPlayers; i++) {
            playerEditTexts.get(i).setText(playerNames[i]);
            PlayerColor playerColor = PlayerColor.values()[playerColors[i]];
            SquareImage playerColorImageView = playerColorImageViews.get(i);
            playerColorImageView.setTag(playerColor);
            playerColorImageView.setImageResource(playerColor.getSelectorResId());
        }
        
    }



    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(EXTRA_NUM_PLAYERS, numPlayers);
        outState.putStringArray(GameActivity.EXTRA_PLAYER_NAMES, getPlayerNames());
        outState.putIntArray(GameActivity.EXTRA_PLAYER_COLORS, getPlayerColors());
    }



    private void setUpWidgets() {

		okButton = (Button) findViewById(android.R.id.button1);
		
		for (int i = 0; i < PLAYER_VIEW_IDS.length; i++) {
		    int id = PLAYER_VIEW_IDS[i];
		    View view = findViewById(id);
		    playerViews.add(view);
		    playerEditTexts.add((AutoCompleteTextView)view.findViewById(R.id.player_name_edit_text));
		    SquareImage squareImage = (SquareImage)view.findViewById(R.id.player_color_image);
		    PlayerColor playerColor = PlayerColor.values()[i];
		    squareImage.setImageResource(playerColor.getSelectorResId());
		    squareImage.setTag(playerColor);
		    playerColorImageViews.add(squareImage);
		}
		
		for (int i = 0; i < playerEditTexts.size(); i++) {
			AutoCompleteTextView playerEditText = playerEditTexts.get(i);
			if (playerEditText == null) {
				continue;
			}
			String hint = getString(R.string.text_player) + ' ' + (i + 1);
			playerEditText.setHint(hint);
			
			// get rid of any edit texts that don't fit given the number of players
			playerViews.get(i).setVisibility(i >= numPlayers ? View.GONE : View.VISIBLE);
			
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
				return PlayerNameHelper.getPlayerNameSuggestions(NamePlayersActivity.this);
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
	
	@Override
	public void onClick(View v) {
		// ok button clicked
		
		String[] playerNames = getPlayerNames();
		int[] playerColors = getPlayerColors();
		
		Intent intent = new Intent(this, GameActivity.class);
		
		intent.putExtra(GameActivity.EXTRA_PLAYER_NAMES, playerNames);
		intent.putExtra(GameActivity.EXTRA_PLAYER_COLORS, playerColors);
		
		startActivity(intent);
	}
	
	private String[] getPlayerNames() {
	    String[] playerNames = new String[numPlayers];
        
        for (int i = 0; i < numPlayers; i++) {
            playerNames[i] = playerEditTexts.get(i).getText().toString();
        }
        return playerNames;
	}
	
	private int[] getPlayerColors() {
       int[] playerColors = new int[numPlayers];
        
       for (int i = 0; i < numPlayers; i++) {
           playerColors[i] = ((PlayerColor)(playerColorImageViews.get(i).getTag())).ordinal();
       }
       return playerColors;
	}
}
