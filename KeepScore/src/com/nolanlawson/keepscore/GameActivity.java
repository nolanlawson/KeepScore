package com.nolanlawson.keepscore;

import java.util.ArrayList;
import java.util.Collections;
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
import android.view.ViewGroup.LayoutParams;
import android.view.ViewStub;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nolanlawson.keepscore.db.Game;
import com.nolanlawson.keepscore.db.GameDBHelper;
import com.nolanlawson.keepscore.db.PlayerScore;
import com.nolanlawson.keepscore.helper.ColorScheme;
import com.nolanlawson.keepscore.helper.CompatibilityHelper;
import com.nolanlawson.keepscore.helper.PlayerTextFormat;
import com.nolanlawson.keepscore.helper.PreferenceHelper;
import com.nolanlawson.keepscore.helper.VersionHelper;
import com.nolanlawson.keepscore.util.CollectionUtil;
import com.nolanlawson.keepscore.util.CollectionUtil.Function;
import com.nolanlawson.keepscore.util.Functions;
import com.nolanlawson.keepscore.util.StopWatch;
import com.nolanlawson.keepscore.util.StringUtil;
import com.nolanlawson.keepscore.util.UtilLogger;
import com.nolanlawson.keepscore.widget.PlayerView;

/**
 * Main activity when the user is playing a game.
 * @author nolan
 *
 */
public class GameActivity extends Activity {
	
	public static final String EXTRA_PLAYER_NAMES = "playerNames";
	public static final String EXTRA_GAME_ID = "gameId";
	public static final String EXTRA_GAME = "game";
	
	private static final int MAX_NUM_PLAYERS = 8;
	private static final long PERIODIC_SAVE_PERIOD = TimeUnit.SECONDS.toMillis(30);
	
	private static final UtilLogger log = new UtilLogger(GameActivity.class);
	
	private LinearLayout rootLayout, rowLayout2, rowLayout3, rowLayout4;
	private View rootPadding1, rootPadding2;
	private ViewStub roundTotalViewStub;
	private TextView roundTotalTextView;
	
	private Game game;
	private List<PlayerScore> playerScores;
	private PowerManager.WakeLock wakeLock;
	
	private List<PlayerView> playerViews;
	private Handler handler = new Handler(Looper.getMainLooper());
	private boolean paused = true;
	private GameDBHelper dbHelper;
	private boolean savedGameBeforeExit;
	
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        createGame();
        
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        
		PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, getPackageName());
        
        setContentView(R.layout.game);
        setUpWidgets();
    }
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		log.d("onDestroy()");
	}

	@Override
	protected void onPause() {
		super.onPause();
		log.d("onPause()");
		
		if (wakeLock.isHeld()) {
			log.d("Releasing wakelock");
			wakeLock.release();
		}		
		
		paused = true;
		
		if (shouldAutosave()) {
			saveGame(game, false, null);
		}
		
		if (dbHelper != null) {
			dbHelper.close();
			dbHelper = null;
		}
		
		if (savedGameBeforeExit) { // if nothing was changed in the game, don't show this message
			Toast.makeText(this, R.string.toast_game_saved, Toast.LENGTH_SHORT).show();
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
		
		startPeriodicSave();
		
		updateRoundTotalViewText();
		
		setOrUpdateColorScheme();
		
		setPlayerViewTextSizes();
		
		paused = false;
		savedGameBeforeExit = false;
	}
	
	private GameDBHelper getDbHelper() {
		if (dbHelper == null) {
			dbHelper = new GameDBHelper(this);
		}
		return dbHelper;
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
	    case R.id.menu_settings:
	    	Intent settingsIntent = new Intent(GameActivity.this, SettingsActivity.class);
	    	startActivity(settingsIntent);
	    	break;
	    case R.id.menu_randomize:
	    	showRandomizePlayersDialog();
	    	break;
	    case R.id.menu_reset_scores:
	    	showResetScoresDialog();
	    	break;
	    case R.id.menu_add_player:
	    	showAddPlayerDialog();
	    	break;
	    case R.id.menu_copy:
	    	cloneGame();
	    	break;
	    }
	    return false;
	}

	private void showRandomizePlayersDialog() {
		new AlertDialog.Builder(this)
			.setCancelable(true)
			.setTitle(R.string.text_confirm)
			.setMessage(R.string.text_players_will_be_randomized)
			.setNegativeButton(android.R.string.cancel, null)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					randomizePlayers();
				}
			})
			.show();
		
	}

	private void randomizePlayers() {
		for (PlayerView playerView : playerViews) {
			playerView.cancelPendingUpdates(); // get rid of any lingering score badges
		}
		
		Collections.shuffle(playerScores);
		for (int i = 0; i < playerScores.size(); i++) {
			playerScores.get(i).setPlayerNumber(i);
		}
		
		setUpWidgets();
		
		saveGame(game, true, null);
		Toast.makeText(this, R.string.toast_players_randomized, Toast.LENGTH_SHORT).show();
		
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
			
			playerView.setNewColorScheme(colorScheme);
			
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
					button.setTextColor(getResources().getColor(colorScheme.getForegroundColorResId()));
				}
			}
			
			playerView.updateViews();
		}
		if (roundTotalTextView != null) {
			roundTotalTextView.setTextColor(getResources().getColor(colorScheme.getForegroundColorResId()));
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
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					for (PlayerView playerView : playerViews) {
						playerView.reset(GameActivity.this);
					}
					updateRoundTotalViewText();
				}
			})
			.setNegativeButton(android.R.string.cancel, null)
			.show();

	}

	private void cloneGame() {
		
		saveGame(game, true, null);
		for (PlayerView playerView : playerViews) {
			playerView.cancelPendingUpdates();
		}
		
		game = game.makeCleanCopy();
		playerScores = game.getPlayerScores();
		
		setUpWidgets();
		
		saveGame(game, true, null);
		Toast.makeText(this, R.string.toast_game_copied, Toast.LENGTH_SHORT).show();
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
		
		game = getDbHelper().findGameById(gameId);
		playerScores = game.getPlayerScores();
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

	private synchronized void saveGame(final Game gameToSave, boolean inBackground, final Runnable onFinished) {

		for (PlayerView playerView : playerViews) {
			playerView.getShouldAutosave().set(false);
		}
		
		if (inBackground) {
			// do in the background to avoid jankiness
			new AsyncTask<Void, Void, Void>() {
	
				@Override
				protected Void doInBackground(Void... params) {
					
					saveGame(gameToSave);
					
					return null;
				}
	
				@Override
				protected void onPostExecute(Void result) {
					super.onPostExecute(result);
					if (onFinished != null) {
						onFinished.run();
					}
				}
				
				
			}.execute((Void)null);
		} else {
			// do in foreground to ensure the game gets saved before the activity finishes
			saveGame(gameToSave);
			if (onFinished != null) {
				onFinished.run();
			}
		}
	}	
	
	private synchronized void saveGame(Game gameToSave) {
		StopWatch stopWatch = new StopWatch("saveGame()");
		
		getDbHelper().saveGame(gameToSave);
		log.d("saved game: %s", gameToSave);
		savedGameBeforeExit = true;
		
		stopWatch.log(log);		
	}

	private void setUpWidgets() {

		rootLayout = (LinearLayout) findViewById(R.id.game_root_layout);
		rowLayout2 = (LinearLayout) findViewById(R.id.game_row_2);
		rowLayout3 = (LinearLayout) findViewById(R.id.game_row_3);
		rowLayout4 = (LinearLayout) findViewById(R.id.game_row_4);
		
		// set which rows are visible based on how many players there are
		rowLayout2.setVisibility(playerScores.size() > 2 ? View.VISIBLE : View.GONE);
		rowLayout3.setVisibility(playerScores.size() > 4 ? View.VISIBLE : View.GONE);
		rowLayout4.setVisibility(playerScores.size() > 6 ? View.VISIBLE : View.GONE);
		
		// add top and bottom spacing on the two-player game.  it looks nicer
		rootPadding1 = findViewById(R.id.game_root_padding_1);
		rootPadding2 = findViewById(R.id.game_root_padding_2);
		rootPadding1.setVisibility(playerScores.size() <= 2 ? View.VISIBLE : View.GONE);
		rootPadding2.setVisibility(playerScores.size() <= 2 ? View.VISIBLE : View.GONE);
		
		// inflate the round total view stub if we're in Eclair (due to an Eclair bug), or
		// if the round totals are enabled
		try {
			roundTotalViewStub = (ViewStub) findViewById(R.id.round_totals);
			int versionInt = VersionHelper.getVersionSdkIntCompat();
			if (versionInt > VersionHelper.VERSION_DONUT &&
					versionInt < VersionHelper.VERSION_FROYO) {
				roundTotalTextView = (TextView) roundTotalViewStub.inflate();
			}
		} catch (ClassCastException ignore) {
			// view stub already inflated
		}
		
		playerViews = new ArrayList<PlayerView>();
		
		// only show the onscreen delta buttons if space allows
		boolean showOnscreenDeltaButtons = playerScores.size() <= 
				getResources().getInteger(R.integer.max_players_for_onscreen_delta_buttons);
		
		for (int i = 0; i < playerScores.size(); i++) {
			
			PlayerScore playerScore = playerScores.get(i);
			int resId = getPlayerViewResId(i);
			View view = getPlayerScoreView(resId);
			
			PlayerView playerView = new PlayerView(this, view, playerScore, handler, showOnscreenDeltaButtons);
			
			playerView.setOnChangeListener(new Runnable(){

				@Override
				public void run() {
					updateRoundTotalViewText();
				}
			});
			
			// set to autosave if the player names are filled in.  This feels intuitive to me.  There's no point
			// in saving an empty game, but if the player names are included, the game feels non-empty and therefore
			// worth saving.  This only applies for newly created games.
			if (game.getId() == -1 && !TextUtils.isEmpty(playerScore.getName())) {
				playerView.getShouldAutosave().set(true);
			}
	    	
			playerViews.add(playerView);
		}
		
		if (playerScores.size() == 3) {
			// hide the "fourth" player
			getPlayerScoreView(R.id.player_4).setVisibility(View.INVISIBLE);
		} else if (playerScores.size() == 5) {
			// hide the "sixth" player
			getPlayerScoreView(R.id.player_6).setVisibility(View.INVISIBLE);
		} else if (playerScores.size() == 7) {
			// hide the "eighth" player
			getPlayerScoreView(R.id.player_8).setVisibility(View.INVISIBLE);
		}
	}
	
	private void updateRoundTotalViewText() {
		
		boolean showRoundTotal = PreferenceHelper.getShowRoundTotals(this);
		
		if (!showRoundTotal) {
			if (roundTotalTextView != null) {
				roundTotalTextView.setVisibility(View.GONE);
			}
			return;
		}
		
		final int round = CollectionUtil.max(playerScores, Functions.PLAYER_SCORE_TO_HISTORY_SIZE);
		
		int roundTotal = round == 0 ? 0 : CollectionUtil.sum(playerScores, new Function<PlayerScore, Integer>(){

			@Override
			public Integer apply(PlayerScore obj) {
				return obj.getHistory().size() >= round ? obj.getHistory().get(round - 1) : 0;
			}
		});
		
		String text = String.format(getString(R.string.round_total), Math.max(round, 1), roundTotal);
		
		if (roundTotalTextView == null) {
			roundTotalTextView = (TextView) roundTotalViewStub.inflate();
		}
		roundTotalTextView.setVisibility(View.VISIBLE);
		roundTotalTextView.setText(text);
	}
	
	/**
	 * sometimes the text gets cut off in the 6 or 8 player view, 
	 * so make the text smaller
	 */
	private void setPlayerViewTextSizes() {
		
		PlayerTextFormat textFormat = PlayerTextFormat.forNumPlayers(playerScores.size());
		
		for (PlayerView playerView : playerViews) {
			setPlayerViewTextSizes(playerView, textFormat);
		}
	}
	
	private void setPlayerViewTextSizes(PlayerView playerView, PlayerTextFormat textFormat) {
		playerView.getNameTextView().setTextSize(TypedValue.COMPLEX_UNIT_PX,
				getResources().getDimensionPixelSize(textFormat.getPlayerNameTextSize()));
		playerView.getBadgeTextView().setTextSize(TypedValue.COMPLEX_UNIT_PX,
				getResources().getDimensionPixelSize(textFormat.getBadgeTextSize()));	
		playerView.getScoreTextView().setTextSize(TypedValue.COMPLEX_UNIT_PX,
				getResources().getDimensionPixelSize(textFormat.getPlayerScoreTextSize()));
		
		Button plusButton = playerView.getPlusButton();
		Button minusButton = playerView.getMinusButton();
		
		// if the round totals are showing, we have a little less space to work with
		int plusMinusButtonHeight = PreferenceHelper.getShowRoundTotals(this)
				? textFormat.getPlusMinusButtonHeightWithRoundTotals()
				: textFormat.getPlusMinusButtonHeight();
		
		// in some cases I manually define it to just be 'fill parent'
		if (plusMinusButtonHeight != LinearLayout.LayoutParams.FILL_PARENT) {
			plusMinusButtonHeight = getResources().getDimensionPixelSize(plusMinusButtonHeight);
		}
				
		for (Button button : new Button[]{plusButton, minusButton}) {
			button.setTextSize(TypedValue.COMPLEX_UNIT_PX,
					getResources().getDimensionPixelSize(textFormat.getPlusMinusTextSize()));
			button.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, 
					plusMinusButtonHeight));
		}
		
		for (Button button : new Button[] {playerView.getDeltaButton1(), 
				playerView.getDeltaButton2(), 
				playerView.getDeltaButton3(),
				playerView.getDeltaButton4()}) {
			if (button != null) {
				button.setTextSize(TypedValue.COMPLEX_UNIT_PX,
						getResources().getDimensionPixelSize(textFormat.getOnscreenDeltaButtonTextSize()));
			}
		}
		if (playerView.getOnscreenDeltaButtonsLayout() != null) {
			playerView.getOnscreenDeltaButtonsLayout().setLayoutParams(new RelativeLayout.LayoutParams(
					LayoutParams.FILL_PARENT, getResources().getDimensionPixelSize(
							textFormat.getOnscreenDeltaButtonHeight())));
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
		
	}

	private View getPlayerScoreView(int resId) {
		// either get the view, or inflate from the ViewStub
		View view = findViewById(resId);
		if (view instanceof ViewStub) {
			return ((ViewStub)view).inflate();
		}
		return view;
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
