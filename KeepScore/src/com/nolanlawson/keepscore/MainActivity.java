package com.nolanlawson.keepscore;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.nolanlawson.keepscore.db.GameDBHelper;
import com.nolanlawson.keepscore.util.UtilLogger;

public class MainActivity extends Activity implements OnClickListener {

	private static UtilLogger log = new UtilLogger(MainActivity.class);
	
	private Button newGameButton, resumeGameButton, loadGameButton;
	
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
			dbHelper = new GameDBHelper(MainActivity.this);
			int gameCount = dbHelper.findGameCount();
			log.d("found game count: %d", gameCount);
			resumeGameButton.setEnabled(gameCount != 0);
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
			resumeMostRecentGame();
			break;
		case android.R.id.button3:
			Intent loadGameIntent = new Intent(this, LoadGameActivity.class);
			startActivity(loadGameIntent);
			break;
		}
	}

	private void resumeMostRecentGame() {
		// do in through an asynctask to avoid jank
		
		new AsyncTask<Void, Void, Integer>(){

			@Override
			protected Integer doInBackground(Void... params) {
				GameDBHelper dbHelper = null;
				try {
					dbHelper = new GameDBHelper(MainActivity.this);
					return dbHelper.findMostRecentGameId();
				} finally {
					if (dbHelper != null) {
						dbHelper.close();
					}
				}
			}

			@Override
			protected void onPostExecute(Integer mostRecentGameId) {
				super.onPostExecute(mostRecentGameId);
				Intent intent = new Intent(MainActivity.this, GameActivity.class);
				
				intent.putExtra(GameActivity.EXTRA_GAME_ID, mostRecentGameId);
				startActivity(intent);	
			}
		}.execute((Void)null);
	}
}