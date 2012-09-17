package com.nolanlawson.keepscore;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.nolanlawson.keepscore.data.EditablePlayerAdapter;
import com.nolanlawson.keepscore.db.Game;
import com.nolanlawson.keepscore.db.PlayerScore;
import com.nolanlawson.keepscore.helper.DialogHelper;
import com.nolanlawson.keepscore.helper.PreferenceHelper;
import com.nolanlawson.keepscore.util.Callback;
import com.nolanlawson.keepscore.util.StringUtil;
import com.nolanlawson.keepscore.widget.dragndrop.DragSortListView;

public class OrganizePlayersActivity extends ListActivity implements OnClickListener {

    public static final int MAX_NUM_PLAYERS = 8;
    public static final int MIN_NUM_PLAYERS = 2;
    
    public static final String EXTRA_PLAYER_SCORES = "playerScores";
    
    private EditablePlayerAdapter adapter;
    private Game game;
    
    private Button okButton, cancelButton, shuffleButton, addPlayerButton;
    
    private List<String> deletedPlayersToWarnAbout = new ArrayList<String>();
    
    
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
	adapter.setOnDeleteListener(new Callback<PlayerScore>(){

	    @Override
	    public void onCallback(PlayerScore playerScore) {
		// warn about deleted player scores if they actually have a history,
		// i.e. there's something the user might regret deleting
		if (playerScore.getHistory() != null && playerScore.getHistory().size() > 0) {
		    deletedPlayersToWarnAbout.add(playerScore.toDisplayName(OrganizePlayersActivity.this));
		}
	    }});
	
	((DragSortListView)getListView()).setDropListener(adapter);
	
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
	    onOkButtonClicked();
	    break;
	case android.R.id.button2:
	    randomizePlayers();
	    break;
	case android.R.id.button3:
	    setResult(RESULT_CANCELED);
	    finish();
	    break;
	case R.id.button_new_player:
	    showAddPlayerDialog();
	    break;
	}
	
    }
    

    private void onOkButtonClicked() {
	if (deletedPlayersToWarnAbout.isEmpty()) {
	    setDataResultAndExit();
	} else {
	    // warn the player just in case they don't want to delete these players
	    String deletePlayerText = getString(deletedPlayersToWarnAbout.size() == 1
		    ? R.string.text_confirm_delete_player
		    : R.string.text_confirm_delete_player_plural);
	    new AlertDialog.Builder(this)
	    	.setCancelable(false)
	    	.setTitle(R.string.title_confirm_delete)
	    	.setMessage(String.format(
	    		deletePlayerText,
	    		TextUtils.join(", ",deletedPlayersToWarnAbout)))
	    	.setNegativeButton(android.R.string.cancel, null)
	    	.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
		    
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
			setDataResultAndExit();
		    }
		})
		.show();
	    	
	}
	
    }


    private void setDataResultAndExit() {
	
	Intent data = new Intent();
	data.putParcelableArrayListExtra(EXTRA_PLAYER_SCORES, 
		new ArrayList<PlayerScore>(adapter.getItems()));
	setResult(RESULT_OK, data);
	finish();
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
