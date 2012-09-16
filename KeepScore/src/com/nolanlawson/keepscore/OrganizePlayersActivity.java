package com.nolanlawson.keepscore;

import java.util.ArrayList;
import java.util.Collections;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.nolanlawson.keepscore.data.EditablePlayerAdapter;
import com.nolanlawson.keepscore.db.Game;
import com.nolanlawson.keepscore.db.PlayerScore;
import com.nolanlawson.keepscore.helper.DialogHelper;
import com.nolanlawson.keepscore.helper.PreferenceHelper;
import com.nolanlawson.keepscore.util.Callback;
import com.nolanlawson.keepscore.util.StringUtil;
import com.nolanlawson.keepscore.widget.PlayerView;
import com.nolanlawson.keepscore.widget.dragndrop.DragNDropListView;

public class OrganizePlayersActivity extends ListActivity implements OnClickListener {

    public static final int RESULT_OK = 0;
    public static final int RESULT_CANCEL = 1;
    
    private static final int MAX_NUM_PLAYERS = 8;
    
    private EditablePlayerAdapter adapter;
    private Game game;
    
    private Button okButton, cancelButton, shuffleButton, addPlayerButton;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	
	game = getIntent().getParcelableExtra(GameActivity.EXTRA_GAME);
	
	adapter = new EditablePlayerAdapter(this, game.getPlayerScores());
	
	setListAdapter(adapter);
	setContentView(R.layout.organize_players_dialog);
	
	setUpWidgets();
	setAddNewPlayerEnabled();
	adapter.setOnChangeListener(new Runnable() {
	    
	    @Override
	    public void run() {
		setAddNewPlayerEnabled();
	    }
	});
	
	((DragNDropListView)getListView()).setDropListener(adapter);
	
    }


    private void setAddNewPlayerEnabled() {
	addPlayerButton.setEnabled(adapter.getCount() < MAX_NUM_PLAYERS);
    }


    private void setUpWidgets() {
	
	okButton = (Button) findViewById(android.R.id.button1);
	cancelButton = (Button) findViewById(android.R.id.button3);
	shuffleButton = (Button) findViewById(android.R.id.button2);
	addPlayerButton = (Button) findViewById(R.id.button_new_player);
	
	for (Button button : new Button[]{okButton, cancelButton, shuffleButton, addPlayerButton}) {
	    button.setOnClickListener(this);
	}
    }


    @Override
    public void onClick(View view) {
	
	switch (view.getId()) {
	case android.R.id.button1:
	    setResult(RESULT_OK);
	    finish();
	    break;
	case android.R.id.button2:
	    randomizePlayers();
	    break;
	case android.R.id.button3:
	    setResult(RESULT_CANCEL);
	    finish();
	    break;
	case R.id.button_new_player:
	    showAddPlayerDialog();
	    break;
	}
	
    }
    

    private void showAddPlayerDialog() {
	
	DialogHelper.showPlayerNameDialog(this, R.string.title_add_player, 
		"", adapter.getCount(), new Callback<String>() {
		    
		    @Override
		    public void onCallback(String input) {
			addNewPlayer(input.trim());
		    }
		});
    }
    

    private void addNewPlayer(CharSequence name) {
	PlayerScore playerScore = new PlayerScore();

	playerScore.setId(-1);
	playerScore.setName(StringUtil.nullToEmpty(name));
	playerScore.setPlayerNumber(adapter.getCount());
	playerScore.setScore(PreferenceHelper.getIntPreference(
		R.string.pref_initial_score,
		R.string.pref_initial_score_default, this));
	playerScore.setHistory(new ArrayList<Integer>());

	adapter.add(playerScore);
	adapter.notifyDataSetChanged();
    }
    
    
    
    private void randomizePlayers() {
	adapter.shuffle();
    }

}
