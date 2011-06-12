package com.nolanlawson.keepscore;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.nolanlawson.keepscore.db.Game;
import com.nolanlawson.keepscore.db.GameDBHelper;

public class MainActivity extends Activity implements OnClickListener {

	private Button newGameButton, resumeGameButton, loadGameButton;
	private Game mostRecentGame;
	
	private Handler handler = new Handler(Looper.getMainLooper());
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        setUpWidgets();
    }
	
	@Override
	public void onResume() {
		super.onResume();
		
		fillInWidgets();
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
	    case R.id.menu_settings:
	    	Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
	    	startActivity(settingsIntent);
	    	break;
	    case R.id.menu_about:
	    	Intent aboutIntent = new Intent(MainActivity.this, AboutActivity.class);
	    	startActivity(aboutIntent);
	    	break;
	    }
	    return false;
	}

	private void fillInWidgets() {

		GameDBHelper dbHelper = null;
		try {
			dbHelper = new GameDBHelper(this);
			mostRecentGame = dbHelper.findMostRecentGame();
			
			resumeGameButton.setEnabled(mostRecentGame != null);
			
		} finally {
			if (dbHelper != null) {
				dbHelper.close();
			}
		}
	}

	private void setUpWidgets() {
		
		newGameButton = (Button) findViewById(android.R.id.button1);
		resumeGameButton = (Button) findViewById(android.R.id.button2);
		loadGameButton = (Button) findViewById(android.R.id.button3);
		
		for (Button button : new Button[]{newGameButton, resumeGameButton, loadGameButton}) {
			button.setOnClickListener(this);
		}
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case android.R.id.button1:
			Intent newGameIntent = new Intent(this, NewGameActivity.class);
			startActivity(newGameIntent);
			break;
		case android.R.id.button2:
			resumeGame();
			break;
		case android.R.id.button3:
			Intent loadGameIntent = new Intent(this, LoadGameActivity.class);
			startActivity(loadGameIntent);
			break;
		}
	}

	private void resumeGame() {
		// do in through the handler because it's sometimes janky loading the next activity
		
		handler.post(new Runnable() {
			
			@Override
			public void run() {
				Intent intent = new Intent(MainActivity.this, GameActivity.class);
				intent.putExtra(GameActivity.EXTRA_GAME_ID, mostRecentGame.getId());
				
				startActivity(intent);				
			}
		});
		

		
	}
}