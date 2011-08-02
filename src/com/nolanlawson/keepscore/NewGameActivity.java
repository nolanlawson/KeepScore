package com.nolanlawson.keepscore;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class NewGameActivity extends Activity implements OnClickListener {

	private List<Button> numPlayersButtons = new ArrayList<Button>();

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_game);
        
        setUpWidgets();
    }

	private void setUpWidgets() {
		
		numPlayersButtons.add((Button) findViewById(R.id.button_2_players));
		numPlayersButtons.add((Button) findViewById(R.id.button_3_players));
		numPlayersButtons.add((Button) findViewById(R.id.button_4_players));
		numPlayersButtons.add((Button) findViewById(R.id.button_5_players));
		numPlayersButtons.add((Button) findViewById(R.id.button_6_players));
		numPlayersButtons.add((Button) findViewById(R.id.button_7_players));
		numPlayersButtons.add((Button) findViewById(R.id.button_8_players));
		
		for (Button button : numPlayersButtons) {
			button.setOnClickListener(this);
		}
	}

	@Override
	public void onClick(View v) {
		for (int i = 0; i < numPlayersButtons.size(); i++) {
			if (numPlayersButtons.get(i).getId() == v.getId()) {
				// 0th button corresponds to 2 players, 1st to 3, etc.
				startNamePlayersActivity(i + 2);
				break;
			}
		}
	}

	private void startNamePlayersActivity(int numPlayers) {
		Intent intent = new Intent(this, NamePlayersActivity.class);
		intent.putExtra(NamePlayersActivity.EXTRA_NUM_PLAYERS, numPlayers);
		startActivity(intent);
	}
}
