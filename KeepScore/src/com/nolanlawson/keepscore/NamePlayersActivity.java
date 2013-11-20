package com.nolanlawson.keepscore;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
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

import com.nolanlawson.keepscore.helper.PlayerColor;
import com.nolanlawson.keepscore.helper.PlayerNameHelper;
import com.nolanlawson.keepscore.widget.SquareImage;

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
	private List<SquareImage> playerColorImageViews = new ArrayList<SquareImage>();
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
            SquareImage playerColorImageView = playerColorImageViews.get(i);
            playerColorImageView.setTag(playerColor);
            playerColorImageView.setImageResource(playerColor.getSelectorResId());
        }
        
        if (savedInstanceState.getBoolean("colorChooserDialog")) {
            runOnUiThread(new Runnable() {
                
                @Override
                public void run() {
                    showColorChooserDialog(
                            playerColorImageViews.get(savedInstanceState.getInt("colorChooserDialogSquareImage")), 
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
		    SquareImage squareImage = (SquareImage)view.findViewById(R.id.player_color_image);
		    PlayerColor playerColor = PlayerColor.values()[i];
		    squareImage.setImageResource(playerColor.getSelectorResId());
		    squareImage.setTag(playerColor);
		    squareImage.setOnClickListener(this);
		    playerColorImageViews.add(squareImage);
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
        	    showColorChooserDialog(((SquareImage)v), (PlayerColor)v.getTag());
        	    break;
	    }
	}
	
	private void showColorChooserDialog(final SquareImage squareImage, PlayerColor selectedColor) {
	    
	    final View view = createColorChooserView(selectedColor);
	    
	    colorChooserDialogChosenColor = selectedColor;
	    colorChooserDialogSquareImage = playerColorImageViews.indexOf(squareImage);
	    colorChooserDialog = new AlertDialog.Builder(this)
	        .setCancelable(true)
	        .setNegativeButton(android.R.string.cancel, null)
	        .setTitle(R.string.title_choose_color)
	        .setView(view)
	        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    
                    PlayerColor selectedPlayerColor = (PlayerColor)view.getTag();
                    squareImage.setTag(selectedPlayerColor);
                    squareImage.setImageResource(selectedPlayerColor.getSelectorResId());
                }
            })
	        .show();
	}
	
	private View createColorChooserView(final PlayerColor selectedColor) {
	    
	    LayoutInflater inflater = (LayoutInflater)getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
	    final View view = inflater.inflate(R.layout.color_chooser_dialog, null, false);
	    
	    final List<SquareImage> squareImages = getSquareImages(view);
        
	    for (int i = 0; i < squareImages.size(); i++) {
	        SquareImage squareImage = squareImages.get(i);

            PlayerColor playerColor = PlayerColor.values()[i];
            squareImage.setSelected(playerColor == selectedColor);
            squareImage.setTag(playerColor);
            if (playerColor == selectedColor) {
                view.setTag(selectedColor); // remember which one is selected
            }
            squareImage.setImageResource(playerColor.getSelectorResId());
            squareImage.setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    PlayerColor playerColor = (PlayerColor)(v.getTag());
                    colorChooserDialogChosenColor = playerColor;
                    for (SquareImage otherSquareImage : squareImages) {
                        otherSquareImage.setSelected(otherSquareImage.getTag() == playerColor);
                    }
                    view.setTag(playerColor); // remember which one is selected
                }
            });
        }
        return view;
	}
	
	private List<SquareImage> getSquareImages(View view) {
	    
	    int[] rows = new int[]{R.id.row_1, R.id.row_2, R.id.row_3, R.id.row_4};
	    int[] columns = new int[]{R.id.column_1, R.id.column_2, R.id.column_3, R.id.column_4};
	    List<SquareImage> result = new ArrayList<SquareImage>();
        
	    if (getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
	        // when in portrait mode, rows are rows and columns are columns
	        for (int rowId : rows) {
	            View row = view.findViewById(rowId);
	            for (int columnId : columns) {
	                result.add((SquareImage)(row.findViewById(columnId)));
	            }
	        }	        
	    } else {
	        // when in landscape mode, rows are columns and columns are rows and hamburgers eat people
            for (int columnId : columns) {
                
                for (int rowId : rows) {
                    View row = view.findViewById(rowId);
                    result.add((SquareImage)(row.findViewById(columnId)));
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
           playerColors[i] = ((PlayerColor)(playerColorImageViews.get(i).getTag())).ordinal();
       }
       return playerColors;
	}
}
