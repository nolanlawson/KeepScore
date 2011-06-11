package com.nolanlawson.keepscore;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.nolanlawson.keepscore.db.Game;
import com.nolanlawson.keepscore.db.GameDBHelper;

public class MainActivity extends Activity implements OnClickListener {

	private Button newGameButton, resumeGameButton, loadGameButton;
	private Game mostRecentGame;
	
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
			Intent intent = new Intent(this, NewGameActivity.class);
			startActivity(intent);
			break;
		case android.R.id.button2:
			resumeGame();
			break;
		case android.R.id.button3:
			break;
		}
	}

	private void resumeGame() {
		Intent intent = new Intent(this, GameActivity.class);
		intent.putExtra(GameActivity.EXTRA_GAME_ID, mostRecentGame.getId());
		
		startActivity(intent);
		
	}
}