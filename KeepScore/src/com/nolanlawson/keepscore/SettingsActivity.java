package com.nolanlawson.keepscore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.text.InputType;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ListAdapter;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.nolanlawson.keepscore.data.SimpleTwoLineAdapter;
import com.nolanlawson.keepscore.data.TextWithDeleteAdapter;
import com.nolanlawson.keepscore.data.TextWithDeleteAdapter.OnDeleteListener;
import com.nolanlawson.keepscore.helper.PackageHelper;
import com.nolanlawson.keepscore.helper.PreferenceHelper;
import com.nolanlawson.keepscore.helper.SettingSetHelper;
import com.nolanlawson.keepscore.helper.ToastHelper;
import com.nolanlawson.keepscore.util.IntegerUtil;

public class SettingsActivity extends SherlockPreferenceActivity implements OnPreferenceChangeListener,
        OnPreferenceClickListener {

    public static final String COLOR_SCHEME_CHANGED = "colorSchemeChanged";

    public static final String EXTRA_SCROLL_TO_CONFIGURATIONS = null;

    private EditTextPreference button1Pref, button2Pref, button3Pref, button4Pref, twoPlayerButton1Pref,
            twoPlayerButton2Pref, twoPlayerButton3Pref, twoPlayerButton4Pref, updateDelayPref, initialScorePref;
    private CheckBoxPreference greenTextPref, showRoundTotalsPref, showInitialMessagePref, disableHighlightTagPref;
    private Preference resetPref, aboutPref, saveSettingsPref, loadSettingsPref;
    private ListPreference colorSchemePref;

    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings);

        setUpPreferences();
        
        // home button goes back
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // go back on pressing home in the action bar
        switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            return true;
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (getIntent() != null && getIntent().getBooleanExtra(EXTRA_SCROLL_TO_CONFIGURATIONS, false)) {
            // scroll to the 'configurations' section after onResume() finishes

            handler.post(new Runnable() {

                @Override
                public void run() {
                    int position = getPreferenceCategoryPosition(R.string.pref_cat_config);
                    getListView().setSelection(position); // 4 is the config
                                                          // section
                }
            });
        }
    }

    private void setUpPreferences() {

        button1Pref = (EditTextPreference) findPreferenceById(R.string.CONSTANT_pref_button_1);
        button2Pref = (EditTextPreference) findPreferenceById(R.string.CONSTANT_pref_button_2);
        button3Pref = (EditTextPreference) findPreferenceById(R.string.CONSTANT_pref_button_3);
        button4Pref = (EditTextPreference) findPreferenceById(R.string.CONSTANT_pref_button_4);
        twoPlayerButton1Pref = (EditTextPreference) findPreferenceById(R.string.CONSTANT_pref_2p_button_1);
        twoPlayerButton2Pref = (EditTextPreference) findPreferenceById(R.string.CONSTANT_pref_2p_button_2);
        twoPlayerButton3Pref = (EditTextPreference) findPreferenceById(R.string.CONSTANT_pref_2p_button_3);
        twoPlayerButton4Pref = (EditTextPreference) findPreferenceById(R.string.CONSTANT_pref_2p_button_4);
        greenTextPref = (CheckBoxPreference) findPreferenceById(R.string.CONSTANT_pref_green_text);
        showRoundTotalsPref = (CheckBoxPreference) findPreferenceById(R.string.CONSTANT_pref_show_round_totals);
        showInitialMessagePref = (CheckBoxPreference) findPreferenceById(R.string.CONSTANT_pref_initial_message);
        disableHighlightTagPref = (CheckBoxPreference) findPreferenceById(R.string.CONSTANT_pref_disable_highlight_tag);
        
        updateDelayPref = (EditTextPreference) findPreferenceById(R.string.CONSTANT_pref_update_delay);
        initialScorePref = (EditTextPreference) findPreferenceById(R.string.CONSTANT_pref_initial_score);
        resetPref = findPreferenceById(R.string.CONSTANT_pref_reset);
        aboutPref = findPreferenceById(R.string.CONSTANT_pref_about);
        colorSchemePref = (ListPreference) findPreferenceById(R.string.CONSTANT_pref_color_scheme);
        loadSettingsPref = findPreferenceById(R.string.CONSTANT_pref_load_settings);
        saveSettingsPref = findPreferenceById(R.string.CONSTANT_pref_save_settings);

        // update the preference's summary with whatever the value is, as it's
        // changed
        for (EditTextPreference pref : new EditTextPreference[] { button1Pref, button2Pref, button3Pref, button4Pref,
                twoPlayerButton1Pref, twoPlayerButton2Pref, twoPlayerButton3Pref, twoPlayerButton4Pref }) {
            setDynamicSummary(pref);
        }

        // do a special check for the update delay value
        updateDelayPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (!IntegerUtil.validInt(newValue.toString()) || Integer.parseInt(newValue.toString()) < 1
                        || Integer.parseInt(newValue.toString()) > 600) {
                    ToastHelper.showLong(SettingsActivity.this, R.string.toast_valid_update_delay_values);
                    return false;
                }
                PreferenceHelper.resetCache();
                return true;
            }
        });

        // do another special check for the initial score value

        initialScorePref.setSummary(initialScorePref.getText());
        initialScorePref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                if (!IntegerUtil.validInt(newValue.toString())) {
                    ToastHelper.showLong(SettingsActivity.this, R.string.toast_valid_initial_score);
                    return false;
                }
                preference.setSummary(newValue.toString());
                return true;
            }
        });

        // show the version number in the "about" summary text
        String version = String.format(getString(R.string.text_version_number), PackageHelper.getVersionName(this));
        aboutPref.setSummary(version);
        // do a special popup for the reset preference
        resetPref.setOnPreferenceClickListener(this);
        loadSettingsPref.setOnPreferenceClickListener(this);
        saveSettingsPref.setOnPreferenceClickListener(this);

        setDynamicColorSchemeSummary(colorSchemePref);

        // go to the about activity if the about pref is pressed
        aboutPref.setOnPreferenceClickListener(this);

        greenTextPref.setOnPreferenceChangeListener(this);
        showRoundTotalsPref.setOnPreferenceChangeListener(this);
        disableHighlightTagPref.setOnPreferenceChangeListener(this);
        showInitialMessagePref.setOnPreferenceChangeListener(this);
    }

    private void resetPreferences() {

        // delete all preferences
        Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.clear();
        editor.commit();
        PreferenceHelper.resetCache();

        ToastHelper.showShort(this, R.string.toast_settings_reset);

        // reload activity
        startActivity(getIntent());
        finish();
    }

    private Preference findPreferenceById(int resId) {
        return findPreference(getString(resId));
    }

    private void setDynamicSummary(EditTextPreference editTextPreference) {
        // set the summary to be whatever the value is, and change it if
        // necessary

        editTextPreference.setSummary(editTextPreference.getText());

        editTextPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            public boolean onPreferenceChange(Preference preference, Object newValue) {

                if (!IntegerUtil.validInt(newValue.toString()) || Integer.parseInt(newValue.toString()) == 0) {
                    ToastHelper.showLong(SettingsActivity.this, R.string.toast_no_zeroes);
                    return false;
                }

                preference.setSummary((CharSequence) newValue);
                return true;
            }
        });
    }

    private void setDynamicColorSchemeSummary(ListPreference preference) {
        // set the summary to be whatever the value is, and change it if
        // necessary

        CharSequence entryValue = preference.getValue();
        int idx = Arrays.asList(preference.getEntryValues()).indexOf(entryValue);
        CharSequence entry = preference.getEntries()[idx];
        preference.setSummary(entry);
        preference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            public boolean onPreferenceChange(Preference preference, Object newValue) {

                CharSequence entryValue = (CharSequence) newValue;
                int idx = Arrays.asList(((ListPreference) preference).getEntryValues()).indexOf(entryValue);
                CharSequence entry = ((ListPreference) preference).getEntries()[idx];

                preference.setSummary(entry);

                PreferenceHelper.resetCache();
                return true;
            }
        });
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        PreferenceHelper.resetCache(); // ensure that the changes get reflected
        return true;
    }

    private void saveSettings() {

        final EditText editText = createSettingSetEditText();

        AlertDialog dialog = new AlertDialog.Builder(this).setCancelable(true)
                .setTitle(R.string.title_save_setting_set).setView(editText)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        String name = editText.getText().toString();
                        if (!SettingSetHelper.isValidSettingSetName(name)) {
                            ToastHelper.showShort(SettingsActivity.this, R.string.toast_invalid_setting_set_name);
                            return;
                        }

                        SettingSetHelper.saveCurrentSettingSet(SettingsActivity.this, name);

                        ToastHelper.showShort(SettingsActivity.this, R.string.toast_saved_setting_set_name, name);
                    }
                }).create();

        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        dialog.show();

    }

    private EditText createSettingSetEditText() {
        EditText editText = new EditText(this);

        editText.setSingleLine();
        editText.setHint(R.string.text_enter_saved_settings_name);
        editText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        return editText;
    }

    private void loadSettings() {
        Set<String> settingSets = SettingSetHelper.getAvailableSettingSets(this);
        if (settingSets.isEmpty()) {
            ToastHelper.showShort(this, R.string.toast_no_saved_settings);
            return;
        }

        final ListAdapter availableSetAdapter = createAvailableSettingSetsAdapter();

        new AlertDialog.Builder(this).setCancelable(true).setTitle(R.string.pref_load_settings_name)
                .setNegativeButton(android.R.string.cancel, null)
                .setAdapter(availableSetAdapter, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showSavedSettingSetDialog((String) availableSetAdapter.getItem(which));
                        dialog.dismiss();
                    }
                }).show();

    }

    private void showSavedSettingSetDialog(final String settingSet) {

        new AlertDialog.Builder(this).setCancelable(true)
                .setTitle(String.format(getString(R.string.title_load_setting_set), settingSet))
                .setAdapter(createSettingSetContentsAdapter(settingSet), null)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        loadSettingSet(settingSet);
                    }
                }).show();

    }

    private void loadSettingSet(String settingSet) {

        SettingSetHelper.loadSettingSet(SettingsActivity.this, settingSet);
        PreferenceHelper.resetCache();
        ToastHelper.showShort(SettingsActivity.this, R.string.toast_loaded_setting_set, settingSet);

        // reload activity
        startActivity(getIntent());
        finish();

    }

    private ListAdapter createSettingSetContentsAdapter(String settingSet) {
        Map<String, ?> settings = getSettingsToDisplay(settingSet);
        SimpleTwoLineAdapter adapter = SimpleTwoLineAdapter.create(this, settings.entrySet(), false);
        return adapter;
    }

    private ListAdapter createAvailableSettingSetsAdapter() {
        List<String> availableSettingSets = new ArrayList<String>(SettingSetHelper.getAvailableSettingSets(this));
        Collections.sort(availableSettingSets);
        final TextWithDeleteAdapter adapter = new TextWithDeleteAdapter(this, availableSettingSets);

        adapter.setOnDeleteListener(new OnDeleteListener() {
            @Override
            public void onDelete(final String settingSetName) {
                onDeleteSettingSet(adapter, settingSetName);
            }
        });
        return adapter;
    }

    private void onDeleteSettingSet(final TextWithDeleteAdapter adapter, final String settingSetName) {
        // called when the "X" button on a saved setting set is clicked
        new AlertDialog.Builder(this).setCancelable(true).setTitle(R.string.title_confirm_delete)
                .setMessage(String.format(getString(R.string.text_delete_saved_settings), settingSetName))
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SettingSetHelper.delete(SettingsActivity.this, settingSetName);
                        adapter.remove(settingSetName);
                        adapter.notifyDataSetChanged();
                        dialog.dismiss();
                    }
                }).show();
    }

    /**
     * Grab a map of all settings, using user-friendly display names for the
     * settings
     * 
     * @param settingSet
     * @return
     */
    private Map<String, ?> getSettingsToDisplay(String settingSet) {

        Map<String, ?> inputMap = SettingSetHelper.getSettingsSet(this, settingSet);

        Map<String, Object> outputMap = new LinkedHashMap<String, Object>();

        // walk through all the settings in this activity - it's a hack, but it
        // works
        for (int i = 0; i < getListView().getAdapter().getCount(); i++) {
            Object obj = getListView().getAdapter().getItem(i);
            if (!(obj instanceof Preference)) {
                continue;
            }

            Preference pref = (Preference) obj;

            if (pref instanceof PreferenceCategory || !pref.isPersistent()) { // fake
                                                                              // pref,
                                                                              // like
                                                                              // 'about'
                                                                              // or
                                                                              // 'reset'
                continue;
            }

            Object value = inputMap.get(pref.getKey());
            value = value != null ? value : Boolean.FALSE; // Android seems to
                                                           // store false
                                                           // booleans as null

            outputMap.put(pref.getTitle().toString(), value);
        }

        return outputMap;
    }

    @Override
    public boolean onPreferenceClick(Preference pref) {
        if (pref.getKey().equals(getString(R.string.CONSTANT_pref_reset))) {
            resetPrefs();
        } else if (pref.getKey().equals(getString(R.string.CONSTANT_pref_about))) {
            Intent intent = new Intent(SettingsActivity.this, AboutActivity.class);
            startActivity(intent);
        } else if (pref.getKey().equals(getString(R.string.CONSTANT_pref_save_settings))) {
            saveSettings();
        } else if (pref.getKey().equals(getString(R.string.CONSTANT_pref_load_settings))) {
            loadSettings();
        }

        return true;
    }

    private void resetPrefs() {
        new AlertDialog.Builder(SettingsActivity.this).setTitle(R.string.title_confirm)
                .setMessage(R.string.text_reset_confirm).setCancelable(true)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        resetPreferences();
                    }

                }).setNegativeButton(android.R.string.cancel, null).show();

    }

    private int getPreferenceCategoryPosition(int titleId) {
        PreferenceScreen screen = getPreferenceScreen();
        String titleToFind = getString(titleId);
        for (int i = 0; i < screen.getPreferenceCount(); i++) {

            Preference preference = screen.getPreference(i);

            if (!(preference instanceof PreferenceCategory)) {
                continue;
            }
            if (titleToFind.equals(((PreferenceCategory) preference).getTitle())) {
                return i;
            }
        }
        return -1;
    }
}
