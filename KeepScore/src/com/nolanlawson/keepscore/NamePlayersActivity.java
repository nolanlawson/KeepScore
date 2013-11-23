package com.nolanlawson.keepscore;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import com.nolanlawson.keepscore.helper.DialogHelper;
import com.nolanlawson.keepscore.helper.GameActivityHelper;
import com.nolanlawson.keepscore.helper.PlayerColor;
import com.nolanlawson.keepscore.helper.PlayerNameHelper;
import com.nolanlawson.keepscore.helper.PreferenceHelper;
import com.nolanlawson.keepscore.util.Callback;
import com.nolanlawson.keepscore.widget.PlayerColorView;

public class NamePlayersActivity extends Activity implements OnClickListener {

    public static final String EXTRA_NUM_PLAYERS = "numPlayers";

    private static final int[] PLAYER_VIEW_IDS = { R.id.player_and_color_1, R.id.player_and_color_2,
            R.id.player_and_color_3, R.id.player_and_color_4, R.id.player_and_color_5, R.id.player_and_color_6,
            R.id.player_and_color_7, R.id.player_and_color_8, R.id.player_and_color_9, R.id.player_and_color_10,
            R.id.player_and_color_11, R.id.player_and_color_12, R.id.player_and_color_13, R.id.player_and_color_14,
            R.id.player_and_color_15, R.id.player_and_color_16, R.id.player_and_color_17, R.id.player_and_color_18,
            R.id.player_and_color_19, R.id.player_and_color_20
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
        String[] playerColors = savedInstanceState.getStringArray(GameActivity.EXTRA_PLAYER_COLORS);
        for (int i = 0; i < numPlayers; i++) {
            playerEditTexts.get(i).setText(playerNames[i]);
            PlayerColor playerColor = PlayerColor.deserialize(playerColors[i]);
            PlayerColorView playerColorView = playerColorViews.get(i);
            playerColorView.setPlayerColor(playerColor);
        }

        if (savedInstanceState.getBoolean("colorChooserDialog")) {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    showColorChooserDialog(
                            playerColorViews.get(savedInstanceState.getInt("colorChooserDialogSquareImage")),
                            PlayerColor.deserialize(savedInstanceState.getString("colorChooserDialogChosenColor")));

                }
            });

        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(EXTRA_NUM_PLAYERS, numPlayers);
        outState.putStringArray(GameActivity.EXTRA_PLAYER_NAMES, getPlayerNames());
        outState.putStringArray(GameActivity.EXTRA_PLAYER_COLORS, getPlayerColors());
        boolean colorChooserDialogShowing = colorChooserDialog != null && colorChooserDialog.isShowing();
        outState.putBoolean("colorChooserDialog", colorChooserDialogShowing);
        if (colorChooserDialogShowing) {
            outState.putString("colorChooserDialogChosenColor", PlayerColor.serialize(colorChooserDialogChosenColor));
            outState.putInt("colorChooserDialogSquareImage", colorChooserDialogSquareImage);
        }
    }

    private void setUpWidgets() {

        okButton = (Button) findViewById(android.R.id.button1);
        
        for (int i = 0; i < PLAYER_VIEW_IDS.length; i++) {
            int id = PLAYER_VIEW_IDS[i];
            View view = findViewById(id);
            playerViews.add(view);
            playerEditTexts.add((AutoCompleteTextView) view.findViewById(R.id.player_name_edit_text));
            PlayerColor playerColor = PlayerColor.BUILT_INS[i % PlayerColor.BUILT_INS.length];
            PlayerColorView playerColorView = (PlayerColorView) view.findViewById(R.id.player_color_image);
            playerColorView.setVisibility(PreferenceHelper.getShowColors(this) ? View.VISIBLE : View.GONE);
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

            // get rid of any edit texts that don't fit given the number of
            // players
            playerViews.get(i).setVisibility(i >= numPlayers ? View.GONE : View.VISIBLE);

            // final edit text does "action done"
            if (i == numPlayers - 1) {
                playerEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
            }
        }
        okButton.setOnClickListener(this);

        fetchPlayerNameSuggestions();
    }

    private void fetchPlayerNameSuggestions() {

        // fetch the player name suggestions from the database, in the
        // background, to avoid UI slowdown

        new AsyncTask<Void, Void, List<String>>() {

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

        }.execute((Void) null);

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case android.R.id.button1:
                // ok button clicked

                String[] playerNames = getPlayerNames();
                String[] playerColors = getPlayerColors();

                GameActivityHelper.newGame(this, playerNames, playerColors);
                break;
            default:
                // color square clicked
                PlayerColorView playerColorView = (PlayerColorView) v;
                showColorChooserDialog(playerColorView, playerColorView.getPlayerColor());
                break;
        }
    }

    private void showColorChooserDialog(final PlayerColorView playerColorView, PlayerColor selectedColor) {

        colorChooserDialogChosenColor = selectedColor;
        colorChooserDialogSquareImage = playerColorViews.indexOf(playerColorView);

        colorChooserDialog = DialogHelper.showColorChooserDialog(this, selectedColor, new Callback<PlayerColor>() {
            @Override
            public void onCallback(PlayerColor playerColor) {
                // color changed
                colorChooserDialogChosenColor = playerColor;
            }
        }, new Runnable() {
            @Override
            public void run() {
                // color chosen
                playerColorView.setPlayerColor(colorChooserDialogChosenColor);
            }
        });
    }

    private String[] getPlayerNames() {
        String[] playerNames = new String[numPlayers];

        for (int i = 0; i < numPlayers; i++) {
            playerNames[i] = playerEditTexts.get(i).getText().toString();
        }
        return playerNames;
    }

    private String[] getPlayerColors() {
        String[] playerColors = new String[numPlayers];

        for (int i = 0; i < numPlayers; i++) {
            playerColors[i] = PlayerColor.serialize(playerColorViews.get(i).getPlayerColor());
        }
        return playerColors;
    }
}
