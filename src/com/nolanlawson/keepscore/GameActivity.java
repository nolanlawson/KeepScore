package com.nolanlawson.keepscore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.text.InputType;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.nolanlawson.keepscore.db.Game;
import com.nolanlawson.keepscore.db.GameDBHelper;
import com.nolanlawson.keepscore.db.PlayerScore;
import com.nolanlawson.keepscore.helper.ColorScheme;
import com.nolanlawson.keepscore.helper.CompatibilityHelper;
import com.nolanlawson.keepscore.helper.PlayerTextFormat;
import com.nolanlawson.keepscore.helper.PreferenceHelper;
import com.nolanlawson.keepscore.util.StopWatch;
import com.nolanlawson.keepscore.util.StringUtil;
import com.nolanlawson.keepscore.util.UtilLogger;
import com.nolanlawson.keepscore.widget.PlayerView;

public class GameActivity extends Activity {
	
	public static final String EXTRA_PLAYER_NAMES = "playerNames";
	public static final String EXTRA_GAME_ID = "gameId";
	public static final String EXTRA_GAME = "game";
	
	private static final int MAX_NUM_PLAYERS = 8;
	private static final long PERIODIC_SAVE_PERIOD = TimeUnit.SECONDS.toMillis(60);
	
	private static final UtilLogger log = new UtilLogger(GameActivity.class);
	
	private View rootLayout;
	
	private Game game;
	private List<PlayerScore> playerScores;
	private PowerManager.WakeLock wakeLock;
	
	private List<PlayerView> playerViews;
	private Handler handler = new Handler(Looper.getMainLooper());
	private boolean paused = true;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        createGame();
        
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        
		PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, getPackageName());
        
        setContentView(getContentViewResId());
        setUpWidgets();
    }

	private int getContentViewResId() {
		switch (playerScores.size()) {
		case 2:
			return R.layout.game_2;
		case 3:
		case 4:
			return R.layout.game_3_to_4;
		case 5:
		case 6:
			return R.layout.game_5_to_6;
		case 7:
		case 8:
		default:
			return R.layout.game_7_to_8;
		}
	}
	
	

	@Override
	protected void onPause() {
		super.onPause();
		
		if (wakeLock.isHeld()) {
			log.d("Releasing wakelock");
			wakeLock.release();
		}		
		
		paused = true;
		
		if (shouldAutosave()) {
			saveGame(game, true);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		boolean useWakeLock = PreferenceHelper.getBooleanPreference(
				R.string.pref_use_wake_lock, R.string.pref_use_wake_lock_default, this);
		if (useWakeLock && !wakeLock.isHeld()) {
			log.d("Acquiring wakelock");
			wakeLock.acquire();
		}
		
		setOrUpdateColorScheme();
		
		startPeriodicSave();
		
		paused = false;
	}

	private void startPeriodicSave() {
		handler.postDelayed(new Runnable(){

			@Override
			public void run() {
				if (shouldAutosave()) {
					saveGame(game, true, new Runnable() {
						
						@Override
						public void run() {
							if (!paused) {
								startPeriodicSave();
							}
						}
					});
				} else {
					log.d("no need to do periodic save");
					if (!paused) {
						startPeriodicSave();
					}
				}
			}
		}, PERIODIC_SAVE_PERIOD);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.game_menu, menu);
	    
	    return true;
	}
	
	
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		MenuItem historyItem = menu.findItem(R.id.menu_reset_scores);
		historyItem.setEnabled(!isAtDefault());
		
		MenuItem addPlayerItem = menu.findItem(R.id.menu_add_player);
		addPlayerItem.setEnabled(playerScores.size() < MAX_NUM_PLAYERS);
		
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
	    switch (item.getItemId()) {
	    case R.id.menu_history:
	    	Intent historyIntent = new Intent(this, HistoryActivity.class);
	    	historyIntent.putExtra(HistoryActivity.EXTRA_GAME, game);
	    	startActivity(historyIntent);
	    	break;
	    case R.id.menu_save:
	    	saveGame(game, false);
	    	break;
	    case R.id.menu_settings:
	    	Intent settingsIntent = new Intent(GameActivity.this, SettingsActivity.class);
	    	startActivity(settingsIntent);
	    	break;
	    case R.id.menu_home:
	    	Intent homeIntent = new Intent(GameActivity.this, MainActivity.class);
	    	homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	    	startActivity(homeIntent);
	    	break;
	    case R.id.menu_reset_scores:
	    	showResetScoresDialog();
	    	break;
	    case R.id.menu_add_player:
	    	showAddPlayerDialog();
	    	break;
	    }
	    return false;
	}

	private void showAddPlayerDialog() {
		final EditText editText = new EditText(this);
		editText.setHint(getString(R.string.text_player) + " " + (playerScores.size() + 1));
		editText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
		editText.setSingleLine();
		new AlertDialog.Builder(this)
			.setTitle(R.string.title_add_player)
			.setView(editText)
			.setCancelable(true)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
					dialog.dismiss();
					addNewPlayer(editText.getText());
					
				}
			})
			.setNegativeButton(android.R.string.cancel, null)
			.show();
		
	}

	private void setOrUpdateColorScheme() {
		
		ColorScheme colorScheme = PreferenceHelper.getColorScheme(this);
		
		int foregroundColor = getResources().getColor(colorScheme.getForegroundColorResId());
		int backgroundColor = getResources().getColor(colorScheme.getBackgroundColorResId());
		int dividerColor = getResources().getColor(colorScheme.getDividerColorResId());

		rootLayout.setBackgroundColor(backgroundColor);
		for (PlayerView playerView : playerViews) {
			playerView.getNameTextView().setTextColor(foregroundColor);
			playerView.getScoreTextView().setTextColor(foregroundColor);
			
			playerView.setPositiveTextColor(colorScheme.getPositiveColorResId());
			playerView.setNegativeTextColor(colorScheme.getNegativeColorResId());
			
			playerView.getDivider1().setBackgroundColor(dividerColor); 
			if (playerView.getDivider2() != null) {
				playerView.getDivider2().setBackgroundColor(dividerColor);
			}
			
			for (Button button : new Button[]{
					playerView.getPlusButton(), 
					playerView.getMinusButton(), 
					playerView.getDeltaButton1(),
					playerView.getDeltaButton2(),
					playerView.getDeltaButton3(),
					playerView.getDeltaButton4(),
					}) {
				if (button != null) {
					button.setBackgroundDrawable(getResources().getDrawable(
							colorScheme.getButtonBackgroundDrawableResId()));
				}
			}
			
			playerView.updateViews();
		}
	}
	
	private void addNewPlayer(CharSequence name) {
		PlayerScore playerScore = new PlayerScore();
		
		playerScore.setId(-1);
		playerScore.setName(StringUtil.nullToEmpty(name));
		playerScore.setPlayerNumber(playerScores.size());
		playerScore.setScore(PreferenceHelper.getIntPreference(
				R.string.pref_initial_score, R.string.pref_initial_score_default, this));
		playerScore.setHistory(new ArrayList<Integer>());
		
		playerScores.add(playerScore);
		
		Runnable onFinished = new Runnable() {

			@Override
			public void run() {
				log.d("game to parcel is: %s", game);
				
				// start a new activity so that the layout can refresh correctly
				// TODO: don't start a new activity; just refresh the layout
				
				Intent intent = new Intent(GameActivity.this, GameActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.putExtra(EXTRA_GAME, game);
				
				startActivity(intent);

				CompatibilityHelper.overridePendingTransition(
						GameActivity.this, android.R.anim.fade_in, android.R.anim.fade_out);
			}
			
		};
		
		saveGame(game, true, onFinished); // automatically save the game
	}

	private boolean isAtDefault() {
		for (PlayerScore playerScore : playerScores) {
			if (!playerScore.isAtDefault(this)) {
				return false;
			}
		}
		return true;
	}
	
	private void showResetScoresDialog() {
		
		new AlertDialog.Builder(this)
			.setCancelable(true)
			.setTitle(R.string.title_confirm_reset)
			.setMessage(R.string.text_reset_scores_confirm)
			.setPositiveButton(R.string.button_overwite, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					for (PlayerView playerView : playerViews) {
						playerView.reset(GameActivity.this);
					}
				}
			})
			.setNeutralButton(R.string.button_new_game_with_same, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					startNewGameWithSameSettings();
				}
			})
			.setNegativeButton(android.R.string.cancel, null)
			.show();

	}

	protected void startNewGameWithSameSettings() {
		
		saveGame(game, true);
		for (PlayerView playerView : playerViews) {
			playerView.cancelPendingUpdates();
		}
		
		game = (Game)game.clone();
		
		playerScores = game.getPlayerScores();

		// reset everything except the player names
		game.setId(-1);
		game.setDateStarted(System.currentTimeMillis());
		game.setDateSaved(0);
		game.setName(null);
		
		for (PlayerScore playerScore : playerScores) {
			playerScore.setId(-1);
			playerScore.setHistory(new ArrayList<Integer>());
			playerScore.setScore(PreferenceHelper.getIntPreference(
					R.string.pref_initial_score, R.string.pref_initial_score_default, this));
		}		
		
		setUpWidgets();
	}

	private boolean shouldAutosave() {
		// only autosave if the user has changed SOMETHING, i.e. the scores aren't all just zero
		
		for (PlayerView playerView : playerViews) {
			if (playerView.getShouldAutosave().get()) {
				return true;
			}
		}
		
		return false;
	}
	
	private void createGame() {

		if (getIntent().hasExtra(EXTRA_PLAYER_NAMES)) {
			// starting a new game
			createNewGame();
		} else if (getIntent().hasExtra(EXTRA_GAME_ID)) {
			// loading an existing game
			createExistingGameFromId();
		} else {
			// game object parceled into intent
			game = getIntent().getParcelableExtra(EXTRA_GAME);
			playerScores = game.getPlayerScores();
			log.d("unparceled game is: %s", game);
		}
		
		log.d("loaded game: %s", game);
		log.d("loaded playerScores: %s", playerScores);
	}

	private void createExistingGameFromId() {
		int gameId = getIntent().getIntExtra(EXTRA_GAME_ID, 0);
		
		GameDBHelper dbHelper = null;
		try {
			dbHelper = new GameDBHelper(this);
			game = dbHelper.findGameById(gameId);
			playerScores = game.getPlayerScores();
		} finally {
			if (dbHelper != null) {
				dbHelper.close();
			}
		}
	}

	private void createNewGame() {

        String[] playerNames = getIntent().getStringArrayExtra(EXTRA_PLAYER_NAMES);
        
        game = new Game();
        
        playerScores = new ArrayList<PlayerScore>();
        
        
        game.setDateStarted(System.currentTimeMillis());
        game.setPlayerScores(playerScores);
        
        for (int i = 0; i < playerNames.length; i++) {
        	
        	PlayerScore playerScore = new PlayerScore();
        	
        	playerScore.setName(playerNames[i]);
        	playerScore.setPlayerNumber(i);
        	playerScore.setHistory(new ArrayList<Integer>());
        	playerScore.setScore(PreferenceHelper.getIntPreference(
        			R.string.pref_initial_score, R.string.pref_initial_score_default, GameActivity.this));
        	
        	playerScores.add(playerScore);
        }
        
		log.d("created new game: %s", game);
		log.d("created new playerScores: %s", playerScores);
	}
	
	private void saveGame(Game gameToSave, boolean autosaved) {
		saveGame(gameToSave, autosaved, null);
	}

	private synchronized void saveGame(Game gameToSave, final boolean autosaved, 
			final Runnable onFinished) {

		StopWatch stopWatch = new StopWatch("clone game");
		
		
		for (PlayerView playerView : playerViews) {
			playerView.getShouldAutosave().set(false);
			// update the views just in case anything bolded needs to be unbolded
			// also, to remove any pending delayed runnables
			if (!autosaved) {
				playerView.confirmHistory();
			}
		}
		final Game clonedGame = (Game) gameToSave.clone();
		stopWatch.log(log);
		
		// do in the background to avoid jankiness
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				
				StopWatch stopWatch = new StopWatch("save in background");
				
				GameDBHelper dbHelper = null;
				try {
					dbHelper = new GameDBHelper(GameActivity.this);
					dbHelper.saveGame(clonedGame, autosaved);
					log.d("saved game: %s", clonedGame);
				} finally {
					if (dbHelper != null) {
						dbHelper.close();
					}
				}
				
				stopWatch.log(log);
				
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				if (!autosaved) {
					Toast.makeText(GameActivity.this, R.string.toast_saved, Toast.LENGTH_SHORT).show();
				}
				if (onFinished != null) {
					onFinished.run();
				}
			}
			
			
		}.execute((Void)null);
	}	
	
	private void setUpWidgets() {

		rootLayout = findViewById(R.id.game_root_layout);
		
		playerViews = new ArrayList<PlayerView>();
		
		PlayerTextFormat textFormat = PlayerTextFormat.forNumPlayers(playerScores.size());
		
		for (int i = 0; i < playerScores.size(); i++) {
			
			PlayerScore playerScore = playerScores.get(i);
			int resId = getPlayerViewResId(i);
			View view = findViewById(resId);
			
			PlayerView playerView = new PlayerView(this, view, playerScore, handler);
			
			// set to autosave if the player names are filled in.  This feels intuitive to me.  There's no point
			// in saving an empty game, but if the player names are included, the game feels non-empty and therefore
			// worth saving.  This only applies for newly created games.
			if (game.getId() == -1 && !TextUtils.isEmpty(playerScore.getName())) {
				playerView.getShouldAutosave().set(true);
			}
			
			// sometimes the text gets cut off in the 6 or 8 player view, 
			// so make the text smaller
			
			playerView.getNameTextView().setTextSize(TypedValue.COMPLEX_UNIT_PX,
					getResources().getDimensionPixelSize(textFormat.getPlayerNameTextSize()));
			playerView.getBadgeTextView().setTextSize(TypedValue.COMPLEX_UNIT_PX,
					getResources().getDimensionPixelSize(textFormat.getBadgeTextSize()));	
			playerView.getScoreTextView().setTextSize(TypedValue.COMPLEX_UNIT_PX,
					getResources().getDimensionPixelSize(textFormat.getPlayerScoreTextSize()));
			playerView.getPlusButton().setTextSize(TypedValue.COMPLEX_UNIT_PX,
					getResources().getDimensionPixelSize(textFormat.getPlusMinusTextSize()));
			playerView.getMinusButton().setTextSize(TypedValue.COMPLEX_UNIT_PX,
					getResources().getDimensionPixelSize(textFormat.getPlusMinusTextSize()));
			for (View plusMinusButtonMargin : playerView.getPlusMinusButtonMargins()) {
				plusMinusButtonMargin.setLayoutParams(
						new LinearLayout.LayoutParams(0, 0, textFormat.getPlusMinusButtonMargin()));
			}
			
			playerView.getBadgeTextView().setPadding(
					getResources().getDimensionPixelSize(textFormat.getBadgePaddingLeftRight()), // left
					getResources().getDimensionPixelSize(textFormat.getBadgePaddingTopBottom()), // top
					getResources().getDimensionPixelSize(textFormat.getBadgePaddingLeftRight()), // right
					getResources().getDimensionPixelSize(textFormat.getBadgePaddingTopBottom()) // bottom
					);
			
			// the offset is from the top right corner only
			playerView.getBadgeLinearLayout().setPadding(
					0, 
					getResources().getDimensionPixelSize(textFormat.getBadgeOffset()), 
					getResources().getDimensionPixelSize(textFormat.getBadgeOffset()), 
					0);
	    	
			playerViews.add(playerView);
		}
		
		if (playerScores.size() == 3) {
			// hide the "fourth" player
			findViewById(R.id.player_4).setVisibility(View.INVISIBLE);
		} else if (playerScores.size() == 5) {
			// hide the "sixth" player
			findViewById(R.id.player_6).setVisibility(View.INVISIBLE);
		} else if (playerScores.size() == 7) {
			// hide the "eighth" player
			findViewById(R.id.player_8).setVisibility(View.INVISIBLE);
		}
	}

	private int getPlayerViewResId(int playerNumber) {
		switch (playerNumber) {
		case 0:
			return R.id.player_1;
		case 1:
			return R.id.player_2;
		case 2:
			return R.id.player_3;
		case 3:
			return R.id.player_4;	
		case 4:
			return R.id.player_5;
		case 5: 
			return R.id.player_6;
		case 6:
			return R.id.player_7;
		case 7:
		default:
			return R.id.player_8;
			
		}
	}
}
