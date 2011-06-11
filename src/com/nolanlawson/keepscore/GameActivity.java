package com.nolanlawson.keepscore;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;

import com.nolanlawson.keepscore.db.Game;
import com.nolanlawson.keepscore.db.PlayerScore;
import com.nolanlawson.keepscore.widget.PlayerView;

public class GameActivity extends Activity {
	
	public static final String EXTRA_PLAYER_NAMES = "playerNames";
	
	private Game game;
	private List<PlayerScore> playerScores;
	private int numPlayers;
	
	private List<PlayerView> playerViews;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        createGame();
        
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        
        setContentView(R.layout.game_3_to_4);
        
        
        
        setUpWidgets();
    }

	private void createGame() {

        String[] playerNames = getIntent().getStringArrayExtra(EXTRA_PLAYER_NAMES);
        
        game = new Game();
        playerScores = new ArrayList<PlayerScore>();
        
        
        game.setDateStarted(System.currentTimeMillis());
        game.setPlayerScores(playerScores);
        
        for (int i = 0; i < playerNames.length; i++) {
        	
        	PlayerScore playerScore = new PlayerScore();
        	
        	playerScore.setName(playerNames[i]);
        	playerScore.setPlayerNumber(i);
        	playerScore.setHistory(new ArrayList<Integer>());
        	
        	playerScores.add(playerScore);
        }
        
        numPlayers = playerNames.length;
        
	}

	private void setUpWidgets() {

		playerViews = new ArrayList<PlayerView>();
		
		for (int i = 0; i < numPlayers; i++) {
			
			PlayerScore playerScore = playerScores.get(i);
			
			int resId = getPlayerViewResId(i);
			
			View view = findViewById(resId);
			
			PlayerView playerView = new PlayerView(this, view, playerScore);
			
	    	String playerName = !TextUtils.isEmpty(playerScore.getName()) 
	    			? playerScore.getName() 
	    			: (getString(R.string.text_player) + " " + (i + 1));
	    	playerView.getName().setText(playerName);
	    	
			playerViews.add(playerView);
			
		}

    			
		
	}

	private int getPlayerViewResId(int playerNumber) {
		switch (playerNumber) {
		case 0:
			return R.id.player_1;
		case 1:
			return R.id.player_2;
		case 2:
			return R.id.player_3;
		case 3:
		default:
			return R.id.player_4;	
		}
	}
}
