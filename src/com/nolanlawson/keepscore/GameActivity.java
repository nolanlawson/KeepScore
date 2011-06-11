package com.nolanlawson.keepscore;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

public class GameActivity extends Activity {
	
	public static final String EXTRA_PLAYER_NAMES = "playerNames";
	
	private int numPlayers;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        numPlayers = getIntent().getIntExtra(NamePlayersActivity.EXTRA_NUM_PLAYERS, 0);
        
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        
        setContentView(R.layout.game_3_to_4);
        
        
        
        setUpWidgets();
    }

	private void setUpWidgets() {
		// TODO Auto-generated method stub
		
	}
}
