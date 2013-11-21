package com.nolanlawson.keepscore;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;

import com.nolanlawson.keepscore.helper.PlayerColor;
import com.nolanlawson.keepscore.helper.PlayerNameHelper;
import com.nolanlawson.keepscore.widget.PlayerColorView;

public class NamePlayersActivity extends Activity implements OnClickListener {
	
	public static final String EXTRA_NUM_PLAYERS = "numPlayers";
	
	private static final int[] PLAYER_VIEW_IDS = {
	    R.id.player_and_color_1,
	    R.id.player_and_color_2,
	    R.id.player_and_color_3,
	    R.id.player_and_color_4,
	    R.id.player_and_color_5,
	    R.id.player_and_color_6,
	    R.id.player_and_color_7,
	    R.id.player_and_color_8
	};
    
	private List<AutoCompleteTextView> playerEditTexts = new ArrayList<AutoCompleteTextView>();
	private List<PlayerColorView> playerColorViews = new ArrayList<PlayerColorView>();
	private List<View> playerViews = new ArrayList<View>();
	private Button okButton;
	
	private AlertDialog colorChooserDialog;
	private PlayerColor colorChooserDialogChosenColor;
	private int colorChooserDialogSquareImage;
	
	private int numPlayers;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		// prevents the soft keyboard from immediately popping up
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		numPlayers = getIntent().getIntExtra(EXTRA_NUM_PLAYERS, 0);
        
		int contentResId = R.layout.name_players;
		
        setContentView(contentResId);
        
        setUpWidgets();
    }
	
	

	@Override
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        
        numPlayers = savedInstanceState.getInt(EXTRA_NUM_PLAYERS);
        
        String[] playerNames = savedInstanceState.getStringArray(GameActivity.EXTRA_PLAYER_NAMES);
        int[] playerColors = savedInstanceState.getIntArray(GameActivity.EXTRA_PLAYER_COLORS);
        for (int i = 0; i < numPlayers; i++) {
            playerEditTexts.get(i).setText(playerNames[i]);
            PlayerColor playerColor = PlayerColor.values()[playerColors[i]];
            PlayerColorView playerColorView = playerColorViews.get(i);
            playerColorView.setPlayerColor(playerColor);
        }
        
        if (savedInstanceState.getBoolean("colorChooserDialog")) {
            runOnUiThread(new Runnable() {
                
                @Override
                public void run() {
                    showColorChooserDialog(
                            playerColorViews.get(savedInstanceState.getInt("colorChooserDialogSquareImage")), 
                            PlayerColor.values()[savedInstanceState.getInt("colorChooserDialogChosenColor")]);
                    
                }
            });
            
        }
        
    }



    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(EXTRA_NUM_PLAYERS, numPlayers);
        outState.putStringArray(GameActivity.EXTRA_PLAYER_NAMES, getPlayerNames());
        outState.putIntArray(GameActivity.EXTRA_PLAYER_COLORS, getPlayerColors());
        boolean colorChooserDialogShowing = colorChooserDialog != null && colorChooserDialog.isShowing();
        outState.putBoolean("colorChooserDialog", colorChooserDialogShowing);
        if (colorChooserDialogShowing) {
            outState.putInt("colorChooserDialogChosenColor", colorChooserDialogChosenColor.ordinal());
            outState.putInt("colorChooserDialogSquareImage", colorChooserDialogSquareImage);
        }
    }



    private void setUpWidgets() {

		okButton = (Button) findViewById(android.R.id.button1);
		
		for (int i = 0; i < PLAYER_VIEW_IDS.length; i++) {
		    int id = PLAYER_VIEW_IDS[i];
		    View view = findViewById(id);
		    playerViews.add(view);
		    playerEditTexts.add((AutoCompleteTextView)view.findViewById(R.id.player_name_edit_text));
		    PlayerColor playerColor = PlayerColor.values()[i];
		    PlayerColorView playerColorView = (PlayerColorView)view.findViewById(R.id.player_color_image);;
		    playerColorView.setPlayerColor(playerColor);
		    playerColorView.setOnClickListener(this);
		    playerColorViews.add(playerColorView);
		}
		
		for (int i = 0; i < playerEditTexts.size(); i++) {
			AutoCompleteTextView playerEditText = playerEditTexts.get(i);
			if (playerEditText == null) {
				continue;
			}
			String hint = getString(R.string.text_player) + ' ' + (i + 1);
			playerEditText.setHint(hint);
			
			// get rid of any edit texts that don't fit given the number of players
			playerViews.get(i).setVisibility(i >= numPlayers ? View.GONE : View.VISIBLE);
			
			// final edit text does "action done"
			if (i == numPlayers-1) {
				playerEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
			}
		}
		okButton.setOnClickListener(this);
		
		fetchPlayerNameSuggestions();
	}

	private void fetchPlayerNameSuggestions() {
		
		// fetch the player name suggestions from the database, in the background, to avoid UI slowdown
		
		new AsyncTask<Void, Void, List<String>>(){

			@Override
			protected List<String> doInBackground(Void... arg0) {
				return PlayerNameHelper.getPlayerNameSuggestions(NamePlayersActivity.this);
			}

			@Override
			protected void onPostExecute(List<String> result) {
				super.onPostExecute(result);
				
				for (AutoCompleteTextView playerEditText : playerEditTexts) {
					if (playerEditText == null) {
						continue;
					}
					ArrayAdapter<String> adapter = new ArrayAdapter<String>(NamePlayersActivity.this, 
							R.layout.simple_dropdown_small, result);
					playerEditText.setAdapter(adapter);
				}
			}
			
		}.execute((Void)null);
		
	}
	
	@Override
	public void onClick(View v) {
	    
	    switch (v.getId()) {
	        case android.R.id.button1:
        		// ok button clicked
        		
        		String[] playerNames = getPlayerNames();
        		int[] playerColors = getPlayerColors();
        		
        		Intent intent = new Intent(this, GameActivity.class);
        		
        		intent.putExtra(GameActivity.EXTRA_PLAYER_NAMES, playerNames);
        		intent.putExtra(GameActivity.EXTRA_PLAYER_COLORS, playerColors);
        		
        		startActivity(intent);
        		break;
        	default:
        	    // color square clicked
        	    PlayerColorView playerColorView = (PlayerColorView)v;
        	    showColorChooserDialog(playerColorView, playerColorView.getPlayerColor());
        	    break;
	    }
	}
	
	private void showColorChooserDialog(final PlayerColorView playerColorView, PlayerColor selectedColor) {
	    
	    final View view = createColorChooserView(selectedColor);
	    
	    colorChooserDialogChosenColor = selectedColor;
	    colorChooserDialogSquareImage = playerColorViews.indexOf(playerColorView);
	    colorChooserDialog = new AlertDialog.Builder(this)
	        .setCancelable(true)
	        .setNegativeButton(android.R.string.cancel, null)
	        .setTitle(R.string.title_choose_color)
	        .setView(view)
	        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    
                    PlayerColor selectedPlayerColor = (PlayerColor)view.getTag();
                    playerColorView.setPlayerColor(selectedPlayerColor);
                }
            })
	        .show();
	}
	
	private View createColorChooserView(final PlayerColor selectedColor) {
	    
	    LayoutInflater inflater = (LayoutInflater)getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
	    final View view = inflater.inflate(R.layout.color_chooser_dialog, null, false);
	    
	    final List<PlayerColorView> playerColorViews = getSquareImages(view);
        
	    for (int i = 0; i < playerColorViews.size(); i++) {
	        PlayerColorView playerColorView = playerColorViews.get(i);

            PlayerColor playerColor = PlayerColor.values()[i];
            playerColorView.setSelected(playerColor == selectedColor);
            playerColorView.setPlayerColor(playerColor);
            if (playerColor == selectedColor) {
                view.setTag(selectedColor); // remember which one is selected
            }
            playerColorView.setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    PlayerColor playerColor = ((PlayerColorView)v).getPlayerColor();
                    colorChooserDialogChosenColor = playerColor;
                    for (PlayerColorView otherSquareImage : playerColorViews) {
                        otherSquareImage.setSelected(otherSquareImage.getPlayerColor() == playerColor);
                    }
                    view.setTag(playerColor); // remember which one is selected
                }
            });
        }
        return view;
	}
	
	private List<PlayerColorView> getSquareImages(View view) {
	    
	    int[] rows = new int[]{R.id.row_1, R.id.row_2, R.id.row_3, R.id.row_4};
	    int[] columns = new int[]{R.id.column_1, R.id.column_2, R.id.column_3, R.id.column_4};
	    List<PlayerColorView> result = new ArrayList<PlayerColorView>();
        
	    LinearLayout allRows = (LinearLayout)(view.findViewById(R.id.all_rows));
	    boolean portraitOrdering = allRows.getOrientation() == LinearLayout.VERTICAL;
	    
	    if (portraitOrdering) {
	        // when in portrait mode, rows are rows and columns are columns
	        for (int rowId : rows) {
	            View row = view.findViewById(rowId);
	            for (int columnId : columns) {
	                result.add((PlayerColorView)(row.findViewById(columnId)));
	            }
	        }	        
	    } else {
	        // when in landscape mode, rows are columns and columns are rows and hamburgers eat people
            for (int columnId : columns) {
                
                for (int rowId : rows) {
                    View row = view.findViewById(rowId);
                    result.add((PlayerColorView)(row.findViewById(columnId)));
                }
            }
	    }
	    return result;
    }



    private String[] getPlayerNames() {
	    String[] playerNames = new String[numPlayers];
        
        for (int i = 0; i < numPlayers; i++) {
            playerNames[i] = playerEditTexts.get(i).getText().toString();
        }
        return playerNames;
	}
	
	private int[] getPlayerColors() {
       int[] playerColors = new int[numPlayers];
        
       for (int i = 0; i < numPlayers; i++) {
           playerColors[i] = playerColorViews.get(i).getPlayerColor().ordinal();
       }
       return playerColors;
	}
}
