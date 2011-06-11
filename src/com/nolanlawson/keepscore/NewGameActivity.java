package com.nolanlawson.keepscore;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class NewGameActivity extends Activity implements OnClickListener {

	Button twoPlayersButton, threePlayersButton, fourPlayersButton, fivePlayersButton, sixPlayersButton;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_game);
        
        setUpWidgets();
    }

	private void setUpWidgets() {
		
		twoPlayersButton = (Button) findViewById(R.id.button_2_players);
		threePlayersButton = (Button) findViewById(R.id.button_3_players);
		fourPlayersButton = (Button) findViewById(R.id.button_4_players);
		fivePlayersButton = (Button) findViewById(R.id.button_5_players);
		sixPlayersButton = (Button) findViewById(R.id.button_6_players);
		
		for (Button button : new Button[]{twoPlayersButton, threePlayersButton, 
				fourPlayersButton, fivePlayersButton, sixPlayersButton}) {
			button.setOnClickListener(this);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_2_players:
			startNamePlayersActivity(2);
			break;
		case R.id.button_3_players:
			startNamePlayersActivity(3);
			break;
		case R.id.button_4_players:
			startNamePlayersActivity(4);
			break;
		case R.id.button_5_players:
			startNamePlayersActivity(5);
			break;
		case R.id.button_6_players:
			startNamePlayersActivity(6);
			break;	
		}
	}

	private void startNamePlayersActivity(int numPlayers) {
		Intent intent = new Intent(this, NamePlayersActivity.class);
		intent.putExtra(NamePlayersActivity.EXTRA_NUM_PLAYERS, numPlayers);
		
		startActivity(intent);
		
	}

	
}
