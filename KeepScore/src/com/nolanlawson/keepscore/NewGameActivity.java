package com.nolanlawson.keepscore;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class NewGameActivity extends Activity implements OnClickListener {

    public static final int TYPICAL_MAX_NUMBER_OF_PLAYERS = 8;
    
	private List<Button> numPlayersButtons = new ArrayList<Button>();
	private Button morePlayersButton;

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
		
		morePlayersButton = (Button) findViewById(R.id.button_more_players);
		morePlayersButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
	    if (v.getId() == R.id.button_more_players) {
	        showMorePlayersDialog();
	    } else {
    		for (int i = 0; i < numPlayersButtons.size(); i++) {
    			if (numPlayersButtons.get(i).getId() == v.getId()) {
    				// 0th button corresponds to 2 players, 1st to 3, etc.
    				startNamePlayersActivity(i + 2);
    				break;
    			}
    		}
	    }
	}

	private void showMorePlayersDialog() {
	    CharSequence[] items = new CharSequence[OrganizePlayersActivity.MAX_NUM_PLAYERS - TYPICAL_MAX_NUMBER_OF_PLAYERS];
	    for (int i = 0; i < items.length; i++) {
	        items[i] = Integer.toString(TYPICAL_MAX_NUMBER_OF_PLAYERS + 1 + i);
	    }
	    new AlertDialog.Builder(this)
	        .setCancelable(true)
	        .setTitle(R.string.text_num_players)
	        .setItems(items, new DialogInterface.OnClickListener() {
                
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startNamePlayersActivity(TYPICAL_MAX_NUMBER_OF_PLAYERS + 1 + which);
                }
            })
	        .show();
        
    }

    private void startNamePlayersActivity(int numPlayers) {
		Intent intent = new Intent(this, NamePlayersActivity.class);
		intent.putExtra(NamePlayersActivity.EXTRA_NUM_PLAYERS, numPlayers);
		startActivity(intent);
	}
}
