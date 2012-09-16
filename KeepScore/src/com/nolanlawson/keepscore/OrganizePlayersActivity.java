package com.nolanlawson.keepscore;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.nolanlawson.keepscore.data.EditablePlayerAdapter;
import com.nolanlawson.keepscore.db.Game;
import com.nolanlawson.keepscore.widget.dragndrop.DragNDropListView;

public class OrganizePlayersActivity extends ListActivity implements OnClickListener {

    public static final int RESULT_OK = 0;
    public static final int RESULT_CANCEL = 1;
    
    private EditablePlayerAdapter adapter;
    private Game game;
    
    private Button okButton, cancelButton, shuffleButton;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	
	game = getIntent().getParcelableExtra(GameActivity.EXTRA_GAME);
	
	adapter = new EditablePlayerAdapter(this, game.getPlayerScores());
	
	setListAdapter(adapter);
	setContentView(R.layout.organize_players_dialog);
	
	setUpWidgets();
	
	((DragNDropListView)getListView()).setDropListener(adapter);
	
    }


    private void setUpWidgets() {
	
	okButton = (Button) findViewById(android.R.id.button1);
	cancelButton = (Button) findViewById(android.R.id.button3);
	shuffleButton = (Button) findViewById(android.R.id.button2);
	
	for (Button button : new Button[]{okButton, cancelButton, shuffleButton}) {
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
	    break;
	case android.R.id.button3:
	    setResult(RESULT_CANCEL);
	    finish();
	    break;
	}
	
    }

    
    
}
