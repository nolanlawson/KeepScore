package com.nolanlawson.keepscore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.nolanlawson.keepscore.data.RecordedChange;
import com.nolanlawson.keepscore.data.RecordedChange.Type;
import com.nolanlawson.keepscore.db.Game;
import com.nolanlawson.keepscore.db.GameDBHelper;
import com.nolanlawson.keepscore.db.PlayerScore;
import com.nolanlawson.keepscore.helper.ColorScheme;
import com.nolanlawson.keepscore.helper.CompatibilityHelper;
import com.nolanlawson.keepscore.helper.PlayerTextFormat;
import com.nolanlawson.keepscore.helper.PreferenceHelper;
import com.nolanlawson.keepscore.helper.VersionHelper;
import com.nolanlawson.keepscore.util.Callback;
import com.nolanlawson.keepscore.util.CollectionUtil;
import com.nolanlawson.keepscore.util.CollectionUtil.Function;
import com.nolanlawson.keepscore.util.DataExpiringStack;
import com.nolanlawson.keepscore.util.Functions;
import com.nolanlawson.keepscore.util.Pair;
import com.nolanlawson.keepscore.util.StopWatch;
import com.nolanlawson.keepscore.util.UtilLogger;
import com.nolanlawson.keepscore.widget.PlayerView;

/**
 * Main activity when the user is playing a game.
 * 
 * @author nolan
 * 
 */
public class GameActivity extends SherlockActivity {

    public static final String EXTRA_PLAYER_NAMES = "playerNames";
    public static final String EXTRA_PLAYER_COLORS = "playerColors";
    public static final String EXTRA_GAME_ID = "gameId";
    public static final String EXTRA_GAME = "game";

    public static final int REQUEST_CODE_ADD_EDIT_PLAYERS = 2;

    private static final long PERIODIC_SAVE_PERIOD = TimeUnit.SECONDS.toMillis(30);

    // how many changes to keep in memory?
    private static final int UNDO_STACK_SIZE = 500;

    @SuppressWarnings("unchecked")
    private static final Set<Pair<Type, Type>> ACCEPTABLE_UNDO_TRANSITIONS = new HashSet<Pair<Type, Type>>(
            Arrays.asList(Pair.create(Type.ModifyLast, Type.ModifyLast), Pair.create(Type.ModifyLast, Type.AddNew),
                    Pair.create(Type.DeleteLastZero, Type.AddNew), Pair.create(Type.DeleteLastZero, Type.ModifyLast),
                    Pair.create(Type.ModifyLast, Type.DeleteLastZero),
                    Pair.create(Type.DeleteLastZero, Type.DeleteLastZero)));
    @SuppressWarnings("unchecked")
    private static final Set<Pair<Type, Type>> ACCEPTABLE_REDO_TRANSITIONS = new HashSet<Pair<Type, Type>>(
            Arrays.asList(Pair.create(Type.ModifyLast, Type.ModifyLast), Pair.create(Type.AddNew, Type.ModifyLast),
                    Pair.create(Type.AddNew, Type.DeleteLastZero), Pair.create(Type.DeleteLastZero, Type.ModifyLast),
                    Pair.create(Type.ModifyLast, Type.DeleteLastZero),
                    Pair.create(Type.DeleteLastZero, Type.DeleteLastZero)));

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

    private DataExpiringStack<RecordedChange> undoStack = new DataExpiringStack<RecordedChange>(UNDO_STACK_SIZE);
    private DataExpiringStack<RecordedChange> redoStack = new DataExpiringStack<RecordedChange>(UNDO_STACK_SIZE);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createGame();

        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, getPackageName());

        setContentView(R.layout.game);
        setUpWidgets();
        scheduleAutomaticBackup();
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

        if (savedGameBeforeExit) { // if nothing was changed in the game, don't
            // show this message
            Toast.makeText(this, R.string.toast_game_saved, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        boolean useWakeLock = PreferenceHelper.getBooleanPreference(R.string.CONSTANT_pref_use_wake_lock,
                R.string.CONSTANT_pref_use_wake_lock_default, this);
        if (useWakeLock && !wakeLock.isHeld()) {
            log.d("Acquiring wakelock");
            wakeLock.acquire();
        }

        startPeriodicSave();

        updateRoundTotalViewText();

        setOrUpdateColorScheme();
        
        updateHighlightedPlayer();

        setPlayerViewTextSizes();

        paused = false;
        savedGameBeforeExit = false;

        getSupportActionBar().hide();
    }

    private void scheduleAutomaticBackup() {
        // schedule a backup to run around 4AM the next day, to ensure that this
        // game is saved as an XML backup when the user is probably done modifying it

        Intent intent = new Intent(this, PeriodicAutomaticBackupService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 
                PeriodicAutomaticBackupService.INTENT_REQUEST_CODE, intent, 
                PendingIntent.FLAG_CANCEL_CURRENT);
        
        AlarmManager alarmManager = (AlarmManager) getSystemService(Activity.ALARM_SERVICE);

        Calendar dateCal = Calendar.getInstance();
        // make it tomorrow (+1 day)
        dateCal.setTime(new Date(System.currentTimeMillis() + (1000 * 60 * 60 * 24)));
        // Set it to 4AM
        dateCal.set(Calendar.HOUR_OF_DAY, 4);
        dateCal.set(Calendar.MINUTE, 0);
        dateCal.set(Calendar.SECOND, 0);
        dateCal.set(Calendar.MILLISECOND, 0);

        log.d("Setting automatic backup for %s", dateCal);
        
        // Create an offset from the current time in which the alarm will go off.
        alarmManager.set(AlarmManager.RTC_WAKEUP, dateCal.getTimeInMillis(), pendingIntent);
    }

    private GameDBHelper getDbHelper() {
        if (dbHelper == null) {
            dbHelper = new GameDBHelper(this);
        }
        return dbHelper;
    }

    private void startPeriodicSave() {
        handler.postDelayed(new Runnable() {

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
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.game_menu, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // show/hide undo/redo menu items
        MenuItem undoMenuItem = menu.findItem(R.id.menu_undo);
        MenuItem redoMenuItem = menu.findItem(R.id.menu_redo);

        boolean showUndo = !undoStack.isEmpty();
        boolean showRedo = !redoStack.isEmpty();

        undoMenuItem.setEnabled(showUndo);
        redoMenuItem.setEnabled(showRedo);

        return true;
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
        case R.id.menu_add_edit_players:
            startOrganizePlayersActivity();
            break;
        case R.id.menu_rematch:
            showRematchDialogue();
            break;
        case R.id.menu_undo:
            undoOrRedo(true);
            break;
        case R.id.menu_redo:
            undoOrRedo(false);
            break;
        }
        return false;
    }

    private void undoOrRedo(boolean undo) {

        DataExpiringStack<RecordedChange> stackToPoll = undo ? undoStack : redoStack;
        DataExpiringStack<RecordedChange> stackToPop = !undo ? undoStack : redoStack;

        RecordedChange recordedChange = null;
        int lastPlayerNumber = -1;
        RecordedChange.Type lastType = null;
        while ((recordedChange = stackToPoll.peek()) != null
                && (lastPlayerNumber == -1 || lastPlayerNumber == recordedChange.getPlayerNumber())
                // only apply multiple changes to the same PlayerScore
                && (lastType == null || isAcceptableUndoOrRedoTransition(undo, lastType, recordedChange.getType()))) {

            recordedChange = stackToPoll.poll();
            PlayerView playerView = playerViews.get(recordedChange.getPlayerNumber());
            if (undo) {
                playerView.revertChange(recordedChange);
            } else {
                playerView.reexecuteChange(recordedChange);
            }
            stackToPop.pop(recordedChange);

            lastPlayerNumber = recordedChange.getPlayerNumber();
            lastType = recordedChange.getType();
        }

        if (lastPlayerNumber != -1) {
            PlayerView playerView = playerViews.get(lastPlayerNumber);
            playerView.resetLastIncremented(); // keeps the badge from showing
            playerView.updateViews();
        }
    }

    private boolean isAcceptableUndoOrRedoTransition(boolean undo, Type lastType, Type type) {
        // consider "modify last" to be part of a larger task that is being
        // undone
        // so e.g. when undoing, MMMMMA is acceptable, whereas with redoing,
        // AMMMMM is acceptable

        if (undo) {
            return ACCEPTABLE_UNDO_TRANSITIONS.contains(Pair.create(lastType, type));
        } else { // redo
            return ACCEPTABLE_REDO_TRANSITIONS.contains(Pair.create(lastType, type));
        }
    }

    private void startOrganizePlayersActivity() {

        Intent intent = new Intent(this, OrganizePlayersActivity.class);
        intent.putExtra(EXTRA_GAME, game);
        startActivityForResult(intent, REQUEST_CODE_ADD_EDIT_PLAYERS);
    }

    private void showRematchDialogue() {
        new AlertDialog.Builder(this).setCancelable(true).setTitle(R.string.text_confirm)
                .setMessage(R.string.text_confirm_rematch).setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        createRematchGame();
                    }
                }).show();

    }

    private void setOrUpdateColorScheme() {

        ColorScheme colorScheme = PreferenceHelper.getColorScheme(this);

        int foregroundColor = getResources().getColor(colorScheme.getForegroundColorResId());
        int backgroundColor = getResources().getColor(colorScheme.getBackgroundColorResId());
        int dividerColor = getResources().getColor(colorScheme.getDividerColorResId());
        
        rootLayout.setBackgroundColor(backgroundColor);
        for (PlayerView playerView : playerViews) {
            playerView.getNameTextView().setTextColor(foregroundColor);
            playerView.getNameTextView().setTypeface(colorScheme.getPlayerNameTypeface());
            playerView.getScoreTextView().setTextColor(foregroundColor);

            playerView.setNewColorScheme(colorScheme);

            playerView.getDivider1().setBackgroundColor(dividerColor);
            if (playerView.getDivider2() != null) {
                playerView.getDivider2().setBackgroundColor(dividerColor);
            }
            
            for (Button button : new Button[] { playerView.getPlusButton(), playerView.getMinusButton(),
                    playerView.getDeltaButton1(), playerView.getDeltaButton2(), playerView.getDeltaButton3(),
                    playerView.getDeltaButton4(), }) {
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

    private void createRematchGame() {

        saveGame(game, true, null); // save the original game

        for (PlayerView playerView : playerViews) {
            playerView.cancelPendingUpdates();
        }

        game = game.makeCleanCopy();
        playerScores = game.getPlayerScores();

        setUpWidgets();

        for (PlayerView playerView : playerViews) {
            playerView.reset(GameActivity.this);
        }
        saveGame(game, true, null); // save the new game

        updateRoundTotalViewText();
        undoStack.clear();
        redoStack.clear();

        Toast.makeText(this, R.string.toast_rematch_created, Toast.LENGTH_SHORT).show();
    }
    
    private boolean gameWasSaved() {
        return game != null && game.getId() != -1;
    }

    private boolean shouldAutosave() {
        // only autosave if the user has changed SOMETHING, i.e. the scores
        // aren't all just zero

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
            playerScore.setScore(PreferenceHelper.getIntPreference(R.string.CONSTANT_pref_initial_score,
                    R.string.CONSTANT_pref_initial_score_default, GameActivity.this));

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

            }.execute((Void) null);
        } else {
            // do in foreground to ensure the game gets saved before the
            // activity finishes
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

        // add top and bottom spacing on the two-player game. it looks nicer
        rootPadding1 = findViewById(R.id.game_root_padding_1);
        rootPadding2 = findViewById(R.id.game_root_padding_2);
        rootPadding1.setVisibility(playerScores.size() <= 2 ? View.VISIBLE : View.GONE);
        rootPadding2.setVisibility(playerScores.size() <= 2 ? View.VISIBLE : View.GONE);

        // inflate the round total view stub if we're in Eclair (due to an
        // Eclair bug), or
        // if the round totals are enabled
        try {
            roundTotalViewStub = (ViewStub) findViewById(R.id.round_totals);
            int versionInt = VersionHelper.getVersionSdkIntCompat();
            if (versionInt > VersionHelper.VERSION_DONUT && versionInt < VersionHelper.VERSION_FROYO) {
                roundTotalTextView = (TextView) roundTotalViewStub.inflate();
            }
        } catch (ClassCastException ignore) {
            // view stub already inflated
        }

        playerViews = new ArrayList<PlayerView>();

        // only show the onscreen delta buttons if space allows
        boolean showOnscreenDeltaButtons = playerScores.size() <= getResources().getInteger(
                R.integer.max_players_for_onscreen_delta_buttons);

        for (int i = 0; i < playerScores.size(); i++) {

            PlayerScore playerScore = playerScores.get(i);
            int resId = getPlayerViewResId(i);
            View view = getPlayerScoreView(resId);

            PlayerView playerView = new PlayerView(this, view, playerScore, handler, showOnscreenDeltaButtons);

            playerView.setChangeRecorder(new Callback<RecordedChange>() {

                @Override
                public void onCallback(RecordedChange recordedChange) {
                    undoStack.pop(recordedChange);
                    redoStack.clear();
                }
            });
            playerView.setOnChangeListener(new Runnable() {

                @Override
                public void run() {
                    updateRoundTotalViewText();
                    updateHighlightedPlayer();
                }
            });

            // set to autosave if the player names are filled in. This feels
            // intuitive to me. There's no point
            // in saving an empty game, but if the player names are included,
            // the game feels non-empty and therefore
            // worth saving. This only applies for newly created games.
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

    private void updateHighlightedPlayer() {
        // highlight the most recently changed player.  This helps with round-based games
        // (where it's important to know that each player's round has been tallied)
        // or games where the scoring has to happen in a particular order (e.g. Cribbage)
        
        long maxLastUpdate = 0;
        int maxLastUpdateIdx = -1;
        
        for (int i = 0; i < playerScores.size(); i++) {
            PlayerScore playerScore = playerScores.get(i);
            
            log.i("playerScore lastUpdate is %s", playerScore.getLastUpdate());
            
            if (playerScore.getLastUpdate() > maxLastUpdate) {
                maxLastUpdate = playerScore.getLastUpdate();
                maxLastUpdateIdx = i;
            }
        }
        
        log.d("updating highlighted player score to idx %s", maxLastUpdateIdx);
        // if none of the player scores are above 0, then this is a game from an older version
        // of KeepScore where we didn't track the lastUpdate, so we don't highlight anything
        
        boolean disableTagIcon = PreferenceHelper.getBooleanPreference(
                R.string.CONSTANT_pref_disable_highlight_tag, 
                R.string.CONSTANT_pref_disable_highlight_tag_default, this);
        
        for (int i = 0; i < playerViews.size(); i++) {
            PlayerView playerView = playerViews.get(i);
            boolean highlighted = (!disableTagIcon && (i == maxLastUpdateIdx));
            
            // highlight or un-highlight by showing or hiding the bullet
            ImageView tagImageView = playerView.getTagImageView();
            tagImageView.setVisibility(highlighted ? View.VISIBLE : View.INVISIBLE);
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

        int roundTotal = round == 0 ? 0 : CollectionUtil.sum(playerScores, new Function<PlayerScore, Integer>() {

            @Override
            public Integer apply(PlayerScore obj) {
                return obj.getHistory().size() >= round ? obj.getHistory().get(round - 1) : 0;
            }
        });

        String text = String.format(getString(R.string.text_round_total), Math.max(round, 1), roundTotal);

        if (roundTotalTextView == null) {
            roundTotalTextView = (TextView) roundTotalViewStub.inflate();
        }
        roundTotalTextView.setVisibility(View.VISIBLE);
        roundTotalTextView.setText(text);
    }

    /**
     * sometimes the text gets cut off in the 6 or 8 player view, so make the
     * text smaller
     */
    private void setPlayerViewTextSizes() {

        PlayerTextFormat textFormat = PlayerTextFormat.forNumPlayers(playerScores.size());

        for (PlayerView playerView : playerViews) {
            setPlayerViewTextSizes(playerView, textFormat);
        }
    }

    private void setPlayerViewTextSizes(PlayerView playerView, PlayerTextFormat textFormat) {
        playerView.getBadgeTextView().setTextSize(TypedValue.COMPLEX_UNIT_PX,
                getResources().getDimensionPixelSize(textFormat.getBadgeTextSize()));
        playerView.getScoreTextView().setMaxTextSize(
                getResources().getDimensionPixelSize(textFormat.getPlayerScoreTextSize()));
        playerView.getNameTextView().setMaxTextSize(
                getResources().getDimensionPixelSize(textFormat.getPlayerNameTextSize()));   
        playerView.getScoreTextView().resizeText();
        playerView.getNameTextView().resizeText();

        Button plusButton = playerView.getPlusButton();
        Button minusButton = playerView.getMinusButton();

        // if the round totals are showing, we have a little less space to work
        // with
        int plusMinusButtonHeight = PreferenceHelper.getShowRoundTotals(this) ? textFormat
                .getPlusMinusButtonHeightWithRoundTotals() : textFormat.getPlusMinusButtonHeight();

        // in some cases I manually define it to just be 'fill parent'
        if (plusMinusButtonHeight != LinearLayout.LayoutParams.MATCH_PARENT) {
            plusMinusButtonHeight = getResources().getDimensionPixelSize(plusMinusButtonHeight);
        }

        for (Button button : new Button[] { plusButton, minusButton }) {
            button.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    getResources().getDimensionPixelSize(textFormat.getPlusMinusTextSize()));
            button.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, plusMinusButtonHeight));
        }

        for (Button button : new Button[] { playerView.getDeltaButton1(), playerView.getDeltaButton2(),
                playerView.getDeltaButton3(), playerView.getDeltaButton4() }) {
            if (button != null) {
                button.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                        getResources().getDimensionPixelSize(textFormat.getOnscreenDeltaButtonTextSize()));
            }
        }
        if (playerView.getOnscreenDeltaButtonsLayout() != null) {
            playerView.getOnscreenDeltaButtonsLayout().setLayoutParams(
                    new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, getResources().getDimensionPixelSize(
                            textFormat.getOnscreenDeltaButtonHeight())));
        }

        playerView.getBadgeTextView().setPadding(
                getResources().getDimensionPixelSize(textFormat.getBadgePaddingLeftRight()), // left
                getResources().getDimensionPixelSize(textFormat.getBadgePaddingTopBottom()), // top
                getResources().getDimensionPixelSize(textFormat.getBadgePaddingLeftRight()), // right
                getResources().getDimensionPixelSize(textFormat.getBadgePaddingTopBottom()) // bottom
                );

        // the offset is from the top right corner only
        playerView.getBadgeLinearLayout().setPadding(0,
                getResources().getDimensionPixelSize(textFormat.getBadgeOffset()),
                getResources().getDimensionPixelSize(textFormat.getBadgeOffset()), 0);

    }

    private View getPlayerScoreView(int resId) {
        // either get the view, or inflate from the ViewStub
        View view = findViewById(resId);
        if (view instanceof ViewStub) {
            return ((ViewStub) view).inflate();
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_ADD_EDIT_PLAYERS && resultCode == RESULT_OK) {
            final List<PlayerScore> newPlayerScores = data
                    .getParcelableArrayListExtra(OrganizePlayersActivity.EXTRA_PLAYER_SCORES);
            handler.post(new Runnable() {

                @Override
                public void run() {
                    saveWithNewPlayerScores(newPlayerScores);
                }
            });
        }
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {

        boolean backButtonPressed = keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0;
        if (backButtonPressed && (gameWasSaved() || shouldAutosave())) {
            // back button pressed
            // if the game has been modified at all, skip showing the "enter player names"
            // screen and just go directly to the main screen
        
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void saveWithNewPlayerScores(final List<PlayerScore> newPlayerScores) {

        // delete the game and recreate it with the new data

        // do in the background to avoid jank
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {

                // delete the old game before starting new one
                getDbHelper().deleteGame(game);
                // after this, because the id is not -1, only UPDATEs will be
                // performed,
                // so the delete is clean even if the background saver keeps
                // running

                final Game newGame = (Game) game.clone();
                newGame.setId(-1);
                newGame.setPlayerScores(newPlayerScores);
                for (PlayerScore playerScore : newGame.getPlayerScores()) {
                    playerScore.setId(-1);
                }

                Runnable onFinished = new Runnable() {

                    @Override
                    public void run() {
                        log.d("game to parcel is: %s", game);

                        // start a new activity so that the layout can refresh
                        // correctly
                        // TODO: don't start a new activity; just refresh the
                        // layout

                        Intent intent = new Intent(GameActivity.this, GameActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra(EXTRA_GAME, newGame);

                        startActivity(intent);

                        CompatibilityHelper.overridePendingTransition(GameActivity.this, android.R.anim.fade_in,
                                android.R.anim.fade_out);
                    }

                };
                saveGame(newGame, true, onFinished); // automatically save the
                                                     // game

                return null;
            }
        }.execute((Void) null);

    }

}
