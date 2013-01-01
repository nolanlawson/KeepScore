package com.nolanlawson.keepscore;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.nolanlawson.keepscore.data.GamesBackupSummaryAdapter;
import com.nolanlawson.keepscore.data.LoadGamesBackupResult;
import com.nolanlawson.keepscore.data.SavedGameAdapter;
import com.nolanlawson.keepscore.data.SeparatedListAdapter;
import com.nolanlawson.keepscore.data.TimePeriod;
import com.nolanlawson.keepscore.db.Game;
import com.nolanlawson.keepscore.db.GameDBHelper;
import com.nolanlawson.keepscore.db.PlayerScore;
import com.nolanlawson.keepscore.helper.PreferenceHelper;
import com.nolanlawson.keepscore.helper.SdcardHelper;
import com.nolanlawson.keepscore.helper.SdcardHelper.Format;
import com.nolanlawson.keepscore.helper.ToastHelper;
import com.nolanlawson.keepscore.helper.VersionHelper;
import com.nolanlawson.keepscore.serialization.GamesBackup;
import com.nolanlawson.keepscore.serialization.GamesBackupSerializer;
import com.nolanlawson.keepscore.serialization.GamesBackupSummary;
import com.nolanlawson.keepscore.util.CollectionUtil;
import com.nolanlawson.keepscore.util.CollectionUtil.Predicate;
import com.nolanlawson.keepscore.util.Callback;
import com.nolanlawson.keepscore.util.StringUtil;
import com.nolanlawson.keepscore.util.UtilLogger;
import com.nolanlawson.keepscore.widget.CustomFastScrollView;

public class MainActivity extends SherlockListActivity implements OnClickListener, OnItemLongClickListener {

    private static UtilLogger log = new UtilLogger(MainActivity.class);

    private SeparatedListAdapter<SavedGameAdapter> adapter;
    private CustomFastScrollView fastScrollView;
    private LinearLayout buttonRow;
    private Button newGameButton, selectAllButton, deselectAllButton;
    private View spacerView;

    private Integer lastPosition;
    private Set<Game> lastChecked;

    private boolean userRespondedToOpenBackupDialog;
    private boolean selectedMode;
    
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setListAdapter(adapter);

        setContentView(R.layout.main);

        setUpWidgets();

        getSupportActionBar().setHomeButtonEnabled(false);
        
        loadBackupFileIfApplicable(getIntent());
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        
        loadBackupFileIfApplicable(intent);
    }


    @Override
    protected void onPause() {
        super.onPause();

        // save which items were checked and where we are in the list
        lastChecked = new HashSet<Game>();
        for (SavedGameAdapter subAdapter : adapter.getSubAdapters()) {
            lastChecked.addAll(subAdapter.getChecked());
        }
        lastPosition = getListView().getFirstVisiblePosition();
    }

    @Override
    public void onResume() {
        super.onResume();

        List<Game> games = getAllGames();
        Collections.sort(games, Game.byRecentlySaved());
        log.d("loaded games %s", games);

        SortedMap<TimePeriod, List<Game>> organizedGames = organizeGamesByTimePeriod(games);

        adapter = new SeparatedListAdapter<SavedGameAdapter>(this);
        for (Entry<TimePeriod, List<Game>> entry : organizedGames.entrySet()) {
            TimePeriod timePeriod = entry.getKey();
            List<Game> gamesSection = entry.getValue();
            SavedGameAdapter subAdapter = new SavedGameAdapter(this, gamesSection);
            if (lastChecked != null) {
                // reload the checked items from when the user last quit
                subAdapter.setChecked(lastChecked);
            }
            subAdapter.setOnCheckChangedRunnable(new Runnable() {

                @Override
                public void run() {
                    showOrHideButtonRow();
                }
            });
            adapter.addSection(getString(timePeriod.getTitleResId()), subAdapter);
        }
        setListAdapter(adapter);

        if (lastPosition != null) {
            // scroll to the user's last position when they quit
            getListView().setSelection(lastPosition);
        }
        lastPosition = null;
        lastChecked = null;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        MenuItem deleteSelectedMenuItem = menu.findItem(R.id.menu_delete_selected);
        MenuItem shareSelectedMenuItem = menu.findItem(R.id.menu_share_selected);
        
        MenuItem loadBackupMenuItem = menu.findItem(R.id.menu_load_backup);
        MenuItem saveBackupMenuItem = menu.findItem(R.id.menu_save_backup);
        MenuItem shareMenuItem = menu.findItem(R.id.menu_share);
        
        MenuItem settingsMenuItem = menu.findItem(R.id.menu_settings);
        MenuItem aboutMenuItem = menu.findItem(R.id.menu_about);
        
        //
        // if the row of buttons at the bottom (select all, deselect all) is showing, offer
        // specific actions up at the top.
        //
        
        MenuItem[] selectedModeMenuItems = new MenuItem[]{deleteSelectedMenuItem, shareSelectedMenuItem};
        MenuItem[] normalModeMenuItems = new MenuItem[]{loadBackupMenuItem, saveBackupMenuItem, shareMenuItem,
                settingsMenuItem, aboutMenuItem};

        for (MenuItem menuItem : selectedModeMenuItems) {
            menuItem.setEnabled(selectedMode);
            menuItem.setVisible(selectedMode);
        }
        for (MenuItem menuItem : normalModeMenuItems) {
            menuItem.setEnabled(!selectedMode);
            menuItem.setVisible(!selectedMode);
        }
        
        if (!selectedMode) { // normal mode
                
            //
            // hide certain menu items if we're in less than Froyo, because
            // the XML Transformer stuff is only available in Froyo and above
            //
            MenuItem[] eclairIncompatibleMenuItems = new MenuItem[]{
                    loadBackupMenuItem, saveBackupMenuItem, shareMenuItem};
            boolean postFroyo = VersionHelper.getVersionSdkIntCompat() >= VersionHelper.VERSION_FROYO;
    
            for (MenuItem menuItem : eclairIncompatibleMenuItems) {
                menuItem.setVisible(postFroyo);
                menuItem.setEnabled(postFroyo);
            }
            
            //
            // if pre-Honeycomb and icon is not in the action bar (i.e. we're in
            // portrait mode), then use the dark Gingerbread-style theme instead.
            // otherwise, the icons don't show up at all on a black background.
            //
            boolean preHoneycomb = VersionHelper.getVersionSdkIntCompat() < VersionHelper.VERSION_HONEYCOMB;
    
            if (preHoneycomb) {
    
                boolean portrait = Configuration.ORIENTATION_PORTRAIT == getResources().getConfiguration().orientation;
    
                settingsMenuItem.setIcon(portrait ? R.drawable.ic_menu_preferences : R.drawable.action_settings);
                aboutMenuItem.setIcon(portrait ? R.drawable.ic_menu_info_details : R.drawable.action_about);
                shareMenuItem.setIcon(portrait ? R.drawable.ic_menu_share : R.drawable.action_share);
            }
        }

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
        case R.id.menu_settings:
            Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(settingsIntent);
            break;
        case R.id.menu_about:
            Intent aboutIntent = new Intent(MainActivity.this, AboutActivity.class);
            startActivity(aboutIntent);
            break;
        case R.id.menu_save_backup:
            showSaveBackupDialog();
            break;
        case R.id.menu_load_backup:
            showLoadBackupDialog();
            break;
        case R.id.menu_share:
            showShareDialog(getAllGameIds());
            break;
        case R.id.menu_delete_selected:
            showDeleteSelectedDialog();
            break;
        case R.id.menu_share_selected:
            showShareDialog(getSelectedGameIds());
            break;
        }
        return false;
    }

    private void showShareDialog(final List<Integer> gameIds) {
        
        new AlertDialog.Builder(this)
                .setCancelable(true)
                .setTitle(R.string.title_confirm)
                .setMessage(getResources().getQuantityString(R.plurals.text_share_with_friend, 
                        gameIds.size(), gameIds.size()))
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    
                    public void onClick(DialogInterface dialog, int which) {
                        // save the backup first, then send it as an email attachment, because
                        // most of the time, the file cannot be read by another app unless it's
                        // on the SD card
                        // Also, we have to use XML because Gmail blocks zipped files for security.
                        saveBackup(Format.XML, gameIds, new Callback<String>() {
                            
                            public void onCallback(String filename) {
                                sendAsAttachment(filename, gameIds.size());
                            }
                        });
                    }
                })
                .show();
    }

    private void sendAsAttachment(String filename, int gameCount) {
        
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("application/x-gzip");
        
        intent.putExtra(Intent.EXTRA_SUBJECT, getResources().getQuantityString(
                R.plurals.text_share_mail_subject, gameCount, gameCount));
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.text_share_mail_body));
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(SdcardHelper.getBackupFile(filename)));
        
        List<ResolveInfo> resolveInfos = getPackageManager().queryIntentActivities(intent, 0);
        
        if (resolveInfos.isEmpty()) {
            ToastHelper.showLong(this, R.string.toast_share_error_no_app);
        } else {
            startActivity(intent);
        }
    }
    
    private void loadBackupFileIfApplicable(Intent intent) {
        
        // if the user opened up a games-20xxxxxxxxx.xml.gz file from a file browser, open it here
        if (intent == null || intent.getData() == null) {
            return;
        }
        
        if (userRespondedToOpenBackupDialog) {
            // user already clicked on the dialog, so don't show it again.
            // I do this because if the screen orientation changes, I want to re-show the dialog, but
            // ONLY if the user hasn't accepted it already.
            return;
        }
        userRespondedToOpenBackupDialog = false;
        
        log.i("Received intent: %s", intent);
        log.i("Received data: %s", intent.getData());
        
        GamesBackupSummary summary;
        try {
            summary = GamesBackupSerializer.readGamesBackupSummary(intent.getData(), Format.XML, getContentResolver());
        } catch (Exception e) {
            log.e(e, "Unexpected error loading %s", intent.getData());
            ToastHelper.showLong(this, R.string.toast_error_with_backup);
            return;
        }
        
        final GamesBackupSummary finalSummary = summary;
        
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        
        GamesBackupSummaryAdapter adapter =  new GamesBackupSummaryAdapter(this, displayMetrics, 
                new ArrayList<GamesBackupSummary>(Collections.singleton(finalSummary)));

        DialogInterface.OnClickListener onOk = new DialogInterface.OnClickListener() {
            
            public void onClick(DialogInterface dialog, int which) {
                userRespondedToOpenBackupDialog = true;
                loadBackup(finalSummary);
            }
        };
        DialogInterface.OnClickListener onCancel = new DialogInterface.OnClickListener() {
            
            public void onClick(DialogInterface dialog, int which) {
                userRespondedToOpenBackupDialog = true;
            }
        };
        
        new AlertDialog.Builder(this)
                .setCancelable(true)
                .setTitle(R.string.title_choose_backup)
                .setNegativeButton(android.R.string.cancel, onCancel)
                .setPositiveButton(android.R.string.ok, onOk)
                .setAdapter(adapter, onOk)
                .show();
        
    }
    
    private void setUpWidgets() {

        newGameButton = (Button) findViewById(R.id.button_new_game);

        getListView().setOnItemLongClickListener(this);
        fastScrollView = (CustomFastScrollView) findViewById(R.id.fast_scroll_view);

        buttonRow = (LinearLayout) findViewById(R.id.layout_button_row);
        selectAllButton = (Button) findViewById(R.id.button_select_all);
        deselectAllButton = (Button) findViewById(R.id.button_deselect_all);

        for (Button button : new Button[] { selectAllButton, deselectAllButton, newGameButton }) {
            button.setOnClickListener(this);
        }

        spacerView = findViewById(R.id.view_spacer);
    }

    private void showSaveBackupDialog() {

        if (adapter.isEmpty()) {
            ToastHelper.showShort(this, R.string.toast_no_saved_games);
            return;
        }

        final List<Integer> gameIds = getAllGameIds();

        String message = String.format(getString(gameIds.size() == 1 ? R.string.text_save_backup
                : R.string.text_save_backup_plural), gameIds.size());

        new AlertDialog.Builder(this).setCancelable(true).setTitle(R.string.menu_save_backup).setMessage(message)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        saveBackup(Format.GZIP, gameIds, new Callback<String>() {

                            public void onCallback(String filename) {
                                String message = String.format(getString(gameIds.size() == 1 
                                        ? R.string.text_save_backup_succeeded
                                        : R.string.text_save_backup_succeeded_plural), gameIds.size(), filename);
                                new AlertDialog.Builder(MainActivity.this)
                                        .setCancelable(true)
                                        .setTitle(R.string.title_success)
                                        .setMessage(message)
                                        .setPositiveButton(android.R.string.ok, null)
                                        .show();
                                
                            }
                        });
                    }
                }).show();

    }

    private void saveBackup(final Format format, 
            final List<Integer> gameIds, final Callback<String> onSuccessWithFilename) {

        if (!SdcardHelper.isAvailable()) {
            ToastHelper.showLong(this, R.string.toast_no_sdcard);
            return;
        }
        
        final String filename = SdcardHelper.createBackupFilename(format);

        final ProgressDialog progressDialog = new ProgressDialog(this);

        progressDialog.setCancelable(false);
        progressDialog.setTitle(R.string.text_saving);
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax(gameIds.size());
        progressDialog.show();

        new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... params) {
                
                List<Game> games = new ArrayList<Game>();
                GameDBHelper dbHelper = null;
                try {
                    dbHelper = new GameDBHelper(MainActivity.this);
                    for (Integer gameId : gameIds) {
                        
                        Game game = dbHelper.findGameById(gameId);
                        games.add(game);
                        publishProgress((Void) null);
                    }
                } finally {
                    if (dbHelper != null) {
                        dbHelper.close();
                    }
                }

                GamesBackup gamesBackup = new GamesBackup();
                gamesBackup.setVersion(GamesBackup.CURRENT_BACKUP_VERSION);
                gamesBackup.setDateSaved(System.currentTimeMillis());
                gamesBackup.setAutomatic(false);
                gamesBackup.setGameCount(games.size());
                gamesBackup.setGames(games);
                gamesBackup.setFilename(filename);
                String xmlData = GamesBackupSerializer.serialize(gamesBackup);

                return SdcardHelper.save(filename, format, xmlData);
            }

            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);
                if (result) {
                    onSuccessWithFilename.onCallback(filename);
                } else {
                    ToastHelper.showLong(MainActivity.this, R.string.toast_save_backup_failed);
                }
                progressDialog.dismiss();
            }

            @Override
            protected void onProgressUpdate(Void... values) {
                super.onProgressUpdate(values);
                progressDialog.incrementProgressBy(1);
            }

        }.execute((Void) null);

    }

    private void showLoadBackupDialog() {

        if (!SdcardHelper.isAvailable()) {
            ToastHelper.showLong(this, R.string.toast_no_sdcard);
            return;
        }

        final List<String> backups = SdcardHelper.list();
        
        if (backups.isEmpty()) {
            ToastHelper.showShort(this, R.string.toast_no_backups);
            return;
        }
        
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(R.string.text_loading_generic);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setIndeterminate(false);
        progressDialog.setMax(backups.size());
        progressDialog.show();
        
        // show progress dialog to avoid jankiness
        new AsyncTask<Void, Void, List<GamesBackupSummary>>(){

            @Override
            protected List<GamesBackupSummary> doInBackground(Void... params) {
                List<GamesBackupSummary> summaries = new ArrayList<GamesBackupSummary>();

                // fetch the summaries only, so that we don't have to read the entire XML file for each one
                for (String backup : backups) {
                    File file = SdcardHelper.getBackupFile(backup);
                    Uri uri = Uri.fromFile(file);
                    
                    Format format = file.getName().endsWith(".gz") ? Format.GZIP : Format.XML;
                    
                    GamesBackupSummary summary = GamesBackupSerializer.readGamesBackupSummary(
                            uri, format, getContentResolver());
                    summaries.add(summary);
                    
                    publishProgress((Void)null);
                }

                // show most recent ones first
                Collections.sort(summaries, new Comparator<GamesBackupSummary>(){

                    public int compare(GamesBackupSummary lhs, GamesBackupSummary rhs) {
                        return Long.valueOf(rhs.getDateSaved()).compareTo(lhs.getDateSaved());
                    }
                });
                
                return summaries;
            }

            @Override
            protected void onProgressUpdate(Void... values) {
                super.onProgressUpdate(values);
                progressDialog.incrementProgressBy(1);
            }

            @Override
            protected void onPostExecute(List<GamesBackupSummary> result) {
                super.onPostExecute(result);
                progressDialog.dismiss();
                showLoadBackupDialogFinished(result);
            }
            
        }.execute((Void)null);
    }

    private void showLoadBackupDialogFinished(List<GamesBackupSummary> summaries) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        
        final GamesBackupSummaryAdapter adapter =  new GamesBackupSummaryAdapter(MainActivity.this,
                displayMetrics, summaries);

        new AlertDialog.Builder(MainActivity.this)
                .setCancelable(true)
                .setTitle(R.string.title_choose_backup)
                .setNegativeButton(android.R.string.cancel, null)
                .setAdapter(adapter, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        GamesBackupSummary summary = adapter.getItem(which);
                        loadBackup(summary);
                    }
                }).show();        
    }

    private void loadBackup(final GamesBackupSummary summary) {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(R.string.text_loading);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setIndeterminate(false);
        progressDialog.setMax(summary.getGameCount());
        progressDialog.show();

        new AsyncTask<Void, Void, LoadGamesBackupResult>() {

            @Override
            protected LoadGamesBackupResult doInBackground(Void... params) {
                return loadBackupInBackground(summary.getFilename(), new Runnable() {

                    @Override
                    public void run() {
                        publishProgress((Void) null);
                    }
                });
            }

            @Override
            protected void onProgressUpdate(Void... values) {
                super.onProgressUpdate(values);
                progressDialog.incrementProgressBy(1);
            }

            @Override
            protected void onPostExecute(LoadGamesBackupResult result) {
                super.onPostExecute(result);
                onReceiveLoadGamesBackupResult(result);
                progressDialog.dismiss();
            }

        }.execute((Void) null);
    }

    private void onReceiveLoadGamesBackupResult(LoadGamesBackupResult result) {

        if (result == null) {
            // failed to load the backup for some reason
            ToastHelper.showLong(this, R.string.toast_error_with_backup);
            return;
        }

        // load the new games into the existing adapter
        for (Game game : result.getLoadedGames()) {
            onNewGameCreated(game);
        }

        // create a nice summary message

        String message = String.format(getString(R.string.text_load_backup), result.getFilename(),
                result.getNumFound(), result.getLoadedGames().size(), result.getNumDuplicates());

        new AlertDialog.Builder(this).setCancelable(true).setTitle(R.string.title_success).setMessage(message)
                .setPositiveButton(android.R.string.ok, null).show();
    }

    private LoadGamesBackupResult loadBackupInBackground(String filename, Runnable onProgress) {

        // use the start date as a unique identifier; it's a
        // millisecond-timestamp, so it should work

        GamesBackup gamesBackup;
        try {
            Uri uri = Uri.fromFile(SdcardHelper.getBackupFile(filename));
            Format format = filename.endsWith(".gz") ? Format.GZIP : Format.XML;
            
            String xmlData = SdcardHelper.open(uri, format, getContentResolver());

            gamesBackup = GamesBackupSerializer.deserialize(xmlData);
        } catch (Exception e) {
            log.e(e, "unexpected");
            return null;
        }
        List<Game> loadedGames = new ArrayList<Game>();
        int numFound = 0, numDuplicates = 0;
        GameDBHelper dbHelper = null;
        try {
            dbHelper = new GameDBHelper(this);
            for (Game game : gamesBackup.getGames()) {
                numFound++;
                if (dbHelper.existsByDateStarted(game.getDateStarted())) {
                    numDuplicates++;
                } else {
                    dbHelper.saveGame(game, false); // don't update 'dateSaved'
                    // value - keep original
                    loadedGames.add(game);
                }
                onProgress.run();
            }
        } finally {
            if (dbHelper != null) {
                dbHelper.close();
            }
        }

        LoadGamesBackupResult result = new LoadGamesBackupResult();
        result.setLoadedGames(loadedGames);
        result.setNumDuplicates(numDuplicates);
        result.setNumFound(numFound);
        result.setFilename(filename);

        return result;
    }

    private List<Integer> getSelectedGameIds() {
        final Set<Integer> ids = new HashSet<Integer>();
        for (SavedGameAdapter subAdapter : adapter.getSectionsMap().values()) {
            for (Game game : subAdapter.getChecked()) {
                ids.add(game.getId());
            }
        }
        return new ArrayList<Integer>(ids);
    }
    
    private List<Integer> getAllGameIds() {
        List<Integer> ids = new ArrayList<Integer>();
        for (int i = 0; i < adapter.getCount(); i++) {
            Object item = adapter.getItem(i);
            if (item instanceof Game) { // else it's a subsection title
                ids.add(((Game)item).getId());
            }
        }
        return ids;
    }
    
    private List<Game> getAllGames() {
        GameDBHelper dbHelper = null;
        try {
            dbHelper = new GameDBHelper(this);
            return dbHelper.findAllGames();
        } finally {
            if (dbHelper != null) {
                dbHelper.close();
            }
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Game game = (Game) adapter.getItem(position);

        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(GameActivity.EXTRA_GAME_ID, game.getId());

        startActivity(intent);

    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapter, View view, int position, long id) {

        showOptionsMenu((Game) (this.adapter.getItem(position)));

        return true;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
        case R.id.button_new_game:
            Intent newGameIntent = new Intent(this, NewGameActivity.class);
            startActivity(newGameIntent);
            break;
        case R.id.button_select_all: // select all
            selectAll();
            break;
        case R.id.button_deselect_all: // deselect all
            deselectAll();
            break;
        }
    }

    private void selectAll() {
        for (SavedGameAdapter subAdapter : adapter.getSectionsMap().values()) {
            for (int i = 0; i < subAdapter.getCount(); i++) {
                Game game = subAdapter.getItem(i);
                subAdapter.getChecked().add(game);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void deselectAll() {
        for (SavedGameAdapter subAdapter : adapter.getSectionsMap().values()) {
            subAdapter.getChecked().clear();
        }

        adapter.notifyDataSetChanged();
        selectedMode = false;
        hideButtonRow();
        supportInvalidateOptionsMenu();
    }

    private void showDeleteSelectedDialog() {
        final Set<Game> games = new HashSet<Game>();
        for (SavedGameAdapter subAdapter : adapter.getSectionsMap().values()) {
            games.addAll(subAdapter.getChecked());
        }
        String message = games.size() == 1 ? getString(R.string.text_game_will_be_deleted) : String.format(
                getString(R.string.text_games_will_be_deleted), games.size());

        new AlertDialog.Builder(this).setCancelable(true).setTitle(R.string.title_confirm_delete).setMessage(message)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteGames(games);
                    }
                }).show();
    }

    private void deleteGames(final Set<Game> games) {

        // do in background to avoid jankiness
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {

                GameDBHelper dbHelper = null;
                try {
                    dbHelper = new GameDBHelper(MainActivity.this);
                    dbHelper.deleteGames(games);

                } finally {
                    if (dbHelper != null) {
                        dbHelper.close();
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                int toast = games.size() == 1 ? R.string.toast_deleted : R.string.toast_multiple_deleted;
                Toast.makeText(MainActivity.this, toast, Toast.LENGTH_SHORT).show();
                for (Game game : games) {
                    onGameDeleted(game);
                }
                // clear from the selected sets
            }

        }.execute((Void) null);
    }

    private void showOptionsMenu(final Game game) {

        String editTitle = getString(TextUtils.isEmpty(game.getName()) ? R.string.title_name_game
                : R.string.title_edit_game_name);

        CharSequence[] options = new CharSequence[] { getString(R.string.text_delete), getString(R.string.text_copy),
                getString(R.string.menu_rematch), getString(R.string.menu_history), editTitle,
                getString(R.string.menu_share)};

        new AlertDialog.Builder(this).setCancelable(true).setItems(options, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                switch (which) {
                case 0: // delete
                    showDeleteDialog(game);
                    break;
                case 1: // copy
                    copyGame(game, false);
                    break;
                case 2: // rematch
                    copyGame(game, true);
                    break;
                case 3: // history
                    showHistory(game);
                    break;
                case 4: // edit name
                    showEditGameNameDialog(game);
                    break;
                case 5: // share
                    showShareDialog(Collections.singletonList(game.getId()));
                    break;
                }
            }
        }).show();

    }

    private void copyGame(Game game, final boolean resetScores) {

        final Game newGame = game.makeCleanCopy();

        if (resetScores) {
            for (PlayerScore playerScore : newGame.getPlayerScores()) {
                playerScore.setScore(PreferenceHelper.getIntPreference(R.string.CONSTANT_pref_initial_score,
                        R.string.CONSTANT_pref_initial_score_default, this));
                playerScore.setHistory(new ArrayList<Integer>());
            }
        }

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                GameDBHelper dbHelper = null;
                try {
                    dbHelper = new GameDBHelper(MainActivity.this);
                    dbHelper.saveGame(newGame);
                } finally {
                    if (dbHelper != null) {
                        dbHelper.close();
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                onNewGameCreated(newGame);
                ToastHelper.showShort(MainActivity.this, resetScores ? R.string.toast_rematch_created
                        : R.string.toast_game_copied);
            }

        }.execute((Void) null);
    }

    private void onNewGameCreated(Game newGame) {

        // if the appropriate section doesn't exist, need to create it
        TimePeriod timePeriodForThisGame = getTimePeriod(new Date(), newGame);
        String sectionForThisGame = getString(timePeriodForThisGame.getTitleResId());

        if (adapter.getCount() == 0 || !adapter.getSectionsMap().keySet().contains(sectionForThisGame)) {
            SavedGameAdapter subAdapter = new SavedGameAdapter(MainActivity.this, new ArrayList<Game>(
                    Collections.singleton(newGame)));
            subAdapter.setOnCheckChangedRunnable(new Runnable() {

                @Override
                public void run() {
                    showOrHideButtonRow();
                }
            });
            Map<String, Integer> sectionsToOrder = new HashMap<String, Integer>();
            for (TimePeriod timePeriod : TimePeriod.values()) {
                sectionsToOrder.put(getString(timePeriod.getTitleResId()), timePeriod.ordinal());
            }
            int index = 0;
            for (int i = 0; i < adapter.getSectionHeaders().getCount(); i++) {
                String section = adapter.getSectionHeaders().getItem(i);

                if (sectionsToOrder.get(sectionForThisGame) < sectionsToOrder.get(section)) {
                    break;
                }
                index++;
            }

            adapter.insertSection(sectionForThisGame, index, subAdapter);
        } else { // just insert it into the proper section
            SavedGameAdapter subAdapter = adapter.getSectionsMap().get(sectionForThisGame);
            subAdapter.add(newGame);
            subAdapter.sort(Game.byRecentlySaved());
        }
        adapter.notifyDataSetChanged();
        adapter.refreshSections();
        fastScrollView.listItemsChanged();

    }

    private void showOrHideButtonRow() {
        
        // the row should be shown if any items are selected
        selectedMode = CollectionUtil.any(adapter.getSectionsMap().values(), new Predicate<SavedGameAdapter>() {

            @Override
            public boolean apply(SavedGameAdapter obj) {
                return !obj.getChecked().isEmpty();
            }
        });

        if (selectedMode) {
            showButtonRow();
        } else {
            hideButtonRow();
        }
        
        // redraw the options at the top
        supportInvalidateOptionsMenu();
    }

    private void showButtonRow() {
        if (buttonRow.getVisibility() == View.VISIBLE) {
            // already visible; don't show slide animation
            return;
        }

        handler.post(new Runnable() {

            @Override
            public void run() {
                startShowButtonRowAnimation();
            }
        });

    }

    private void hideButtonRow() {
        if (buttonRow.getVisibility() == View.GONE) {
            return; // nothing to do
        }

        handler.post(new Runnable() {

            @Override
            public void run() {
                startHideButtonRowAnimation();
            }
        });

    }

    private void startHideButtonRowAnimation() {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_top_to_bottom);
        animation.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                buttonRow.setVisibility(View.GONE);
            }
        });
        spacerView.setVisibility(View.GONE);
        buttonRow.setAnimation(animation);
        buttonRow.startAnimation(animation);
    }

    private void startShowButtonRowAnimation() {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_bottom_to_top);
        animation.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // propertly shrink the scroll view
                spacerView.setVisibility(View.VISIBLE);
            }
        });
        buttonRow.setAnimation(animation);
        buttonRow.setVisibility(View.VISIBLE);

        buttonRow.startAnimation(animation);
    }

    private void showHistory(Game game) {

        Intent intent = new Intent(this, HistoryActivity.class);
        intent.putExtra(HistoryActivity.EXTRA_GAME, game);

        startActivity(intent);

    }

    private void showEditGameNameDialog(final Game game) {

        final EditText editText = new EditText(this);
        editText.setHint(R.string.hint_game_name);
        editText.setText(StringUtil.nullToEmpty(game.getName()));
        editText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        editText.setSingleLine();
        new AlertDialog.Builder(this).setTitle(R.string.title_edit_game_name).setView(editText).setCancelable(true)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(final DialogInterface dialog, int which) {

                        final String newName = StringUtil.nullToEmpty(editText.getText().toString());

                        // update database in the background to avoid
                        // jankiness
                        new AsyncTask<Void, Void, Void>() {

                            @Override
                            protected Void doInBackground(Void... params) {
                                GameDBHelper dbHelper = null;
                                try {
                                    dbHelper = new GameDBHelper(MainActivity.this);
                                    dbHelper.updateGameName(game, newName);
                                } finally {
                                    if (dbHelper != null) {
                                        dbHelper.close();
                                    }
                                }
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void result) {
                                super.onPostExecute(result);

                                game.setName(newName.trim());
                                adapter.notifyDataSetChanged();

                                dialog.dismiss();
                            }

                        }.execute((Void) null);

                    }
                }).setNegativeButton(android.R.string.cancel, null).show();

    }

    private void showDeleteDialog(final Game game) {
        new AlertDialog.Builder(this).setCancelable(true).setTitle(R.string.title_confirm_delete)
                .setMessage(R.string.text_game_will_be_deleted)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        deleteGames(Collections.singleton(game));
                    }
                }).setNegativeButton(android.R.string.cancel, null).show();
    }

    private void onGameDeleted(Game game) {
        // delete the game from the adapter

        for (Entry<String, SavedGameAdapter> entry : new HashMap<String, SavedGameAdapter>(adapter.getSectionsMap())
                .entrySet()) {
            SavedGameAdapter subAdapter = (SavedGameAdapter) entry.getValue();
            if (subAdapter.getCount() == 1 && subAdapter.getItem(0).equals(game)) {
                // special case where there's only one item left - don't want
                // the adapter to be left empty
                // So delete the entire section
                adapter.removeSection(entry.getKey());
            } else {
                subAdapter.remove(game);
            }
            subAdapter.getChecked().remove(game); // remove from the checked
            // list
        }

        adapter.notifyDataSetChanged();
        adapter.refreshSections();
        fastScrollView.listItemsChanged();

        showOrHideButtonRow();
    }

    private SortedMap<TimePeriod, List<Game>> organizeGamesByTimePeriod(List<Game> games) {
        SortedMap<TimePeriod, List<Game>> result = new TreeMap<TimePeriod, List<Game>>();

        Iterator<TimePeriod> timePeriodIterator = Arrays.asList(TimePeriod.values()).iterator();
        TimePeriod timePeriod = timePeriodIterator.next();
        Date date = new Date();
        for (Game game : games) {
            // time periods are sorted from newest to oldest, just like the
            // games. So we can just walk through
            // them in order
            while (!timePeriodMatches(date, timePeriod, game)) {
                timePeriod = timePeriodIterator.next();
            }
            List<Game> existing = result.get(timePeriod);
            if (existing == null) {
                result.put(timePeriod, new ArrayList<Game>(Collections.singleton(game)));
            } else {
                existing.add(game);
            }
        }
        return result;
    }

    private TimePeriod getTimePeriod(Date date, Game game) {
        for (TimePeriod timePeriod : TimePeriod.values()) {
            if (timePeriodMatches(date, timePeriod, game)) {
                return timePeriod;
            }
        }
        return TimePeriod.Older;
    }

    /**
     * Return true if the game occurred within this time period.
     * 
     * @param date
     * @param timePeriod
     * @param currentGame
     * @return
     */
    private boolean timePeriodMatches(Date date, TimePeriod timePeriod, Game currentGame) {
        Date start = timePeriod.getStartDateFunction().apply(date);
        Date end = timePeriod.getEndDateFunction().apply(date);

        return currentGame.getDateSaved() < end.getTime() && currentGame.getDateSaved() >= start.getTime();
    }

}