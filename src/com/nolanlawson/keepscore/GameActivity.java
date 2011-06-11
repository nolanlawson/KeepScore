package com.nolanlawson.keepscore;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.nolanlawson.keepscore.db.Game;
import com.nolanlawson.keepscore.db.GameDBHelper;
import com.nolanlawson.keepscore.db.PlayerScore;
import com.nolanlawson.keepscore.util.UtilLogger;
import com.nolanlawson.keepscore.widget.PlayerView;

public class GameActivity extends Activity {
	
	public static final String EXTRA_PLAYER_NAMES = "playerNames";
	
	private static final UtilLogger log = new UtilLogger(GameActivity.class);
	
	private Game game;
	private List<PlayerScore> playerScores;
	private int numPlayers;
	private PowerManager.WakeLock wakeLock;
	
	private List<PlayerView> playerViews;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        createGame();
        
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        
        setContentView(getContentViewResId());
        
        
        
        setUpWidgets();
        
		PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, getPackageName());
    }

	private int getContentViewResId() {
		switch (numPlayers) {
		case 2:
			return R.layout.game_2;
		case 3:
		case 4:
			return R.layout.game_3_to_4;
		case 5:
		case 6:
		default:
			return R.layout.game_5_to_6;
		}
	}
	
	

	@Override
	protected void onPause() {
		super.onPause();
		
		if (wakeLock.isHeld()) {
			log.d("Releasing wakelock");
			wakeLock.release();
		}		
		
		saveGame(true);
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		if (!wakeLock.isHeld()) {
			log.d("Acquiring wakelock");
			wakeLock.acquire();
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main_menu, menu);
	    
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
	    switch (item.getItemId()) {
	    case R.id.menu_save:
	    	saveGame(false);
	    	break;
	    }
	    return false;
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

	private void saveGame(final boolean autosaved) {
		
		// do in the background to avoid jankiness
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				
				GameDBHelper dbHelper = null;
				try {
					dbHelper = new GameDBHelper(GameActivity.this);
					dbHelper.saveGame(game, autosaved);
				} finally {
					if (dbHelper != null) {
						dbHelper.close();
					}
				}
				
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				int resId = autosaved ? R.string.toast_saved_automatically : R.string.toast_saved;
				Toast.makeText(GameActivity.this, resId, Toast.LENGTH_SHORT).show();
			}
			
			
		}.execute((Void)null);
		
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
		
		if (numPlayers == 3) {
			// hide the "fourth" player
			findViewById(R.id.player_4).setVisibility(View.INVISIBLE);
		} else if (numPlayers == 5) {
			// hide the "sixth" player
			findViewById(R.id.player_6).setVisibility(View.INVISIBLE);
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
			return R.id.player_4;	
		case 4:
			return R.id.player_5;
		case 5: 
		default:
			return R.id.player_6;
		}
	}
}
