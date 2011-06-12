package com.nolanlawson.keepscore;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.text.TextUtils;
import android.widget.Toast;

import com.nolanlawson.keepscore.helper.PreferenceHelper;
import com.nolanlawson.keepscore.util.IntegerUtil;

public class SettingsActivity extends PreferenceActivity {

	private EditTextPreference button1Pref, button2Pref, button3Pref, button4Pref, updateDelayPref, initialScorePref;
	private CheckBoxPreference useWakeLockPref;
	private Preference resetPref;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.settings);
		
		setUpPreferences();
	}
	
	private void setUpPreferences() {
		
		button1Pref = (EditTextPreference) findPreferenceById(R.string.pref_button_1);
		button2Pref = (EditTextPreference) findPreferenceById(R.string.pref_button_2);
		button3Pref = (EditTextPreference) findPreferenceById(R.string.pref_button_3);
		button4Pref = (EditTextPreference) findPreferenceById(R.string.pref_button_4);
		updateDelayPref = (EditTextPreference) findPreferenceById(R.string.pref_update_delay);
		initialScorePref = (EditTextPreference) findPreferenceById(R.string.pref_initial_score);
		resetPref = findPreferenceById(R.string.pref_reset);
		
		// update the preference's summary with whatever the value is, as it's changed
		for (EditTextPreference pref : new EditTextPreference[]{
				button1Pref, button2Pref, button3Pref, button4Pref}) {
			setDynamicSummary(pref);
		}
		
		// do a special check for the update delay value
		updateDelayPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if (!IntegerUtil.validInt(newValue.toString()) 
						|| Integer.parseInt(newValue.toString()) < 1
						|| Integer.parseInt(newValue.toString()) > 600) {
					Toast.makeText(SettingsActivity.this, R.string.toast_valid_update_delay_values, Toast.LENGTH_LONG).show();
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
					Toast.makeText(SettingsActivity.this, R.string.toast_valid_initial_score, Toast.LENGTH_LONG).show();
					return false;
				}
				preference.setSummary(newValue.toString());
				return true;
			}
		});
		
		// do a special popup for the reset preference
		resetPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				new AlertDialog.Builder(SettingsActivity.this)
					.setTitle(R.string.title_confirm)
					.setMessage(R.string.text_reset_confirm)
					.setCancelable(true)
					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							resetPreferences();
						}


					})
					.setNegativeButton(android.R.string.cancel, null)
					.show();
				return true;
			}
		});
		
		useWakeLockPref = (CheckBoxPreference) findPreferenceById(R.string.pref_use_wake_lock);
		
		
	}
	
	private void resetPreferences() {
		
		PreferenceHelper.setIntPreference(R.string.pref_button_1, R.string.pref_button_1_default, this);
		PreferenceHelper.setIntPreference(R.string.pref_button_2, R.string.pref_button_2_default, this);
		PreferenceHelper.setIntPreference(R.string.pref_button_3, R.string.pref_button_3_default, this);
		PreferenceHelper.setIntPreference(R.string.pref_button_4, R.string.pref_button_4_default, this);
		PreferenceHelper.setIntPreference(R.string.pref_initial_score, R.string.pref_initial_score_default, this);
		PreferenceHelper.setIntPreference(R.string.pref_update_delay, R.string.pref_update_delay_default, this);
		
		PreferenceHelper.setBooleanPreference(R.string.pref_use_wake_lock, R.string.pref_use_wake_lock_default, this);
		
		button1Pref.setSummary(R.string.pref_button_1_default);
		button1Pref.setText(getString(R.string.pref_button_1_default));
		
		button2Pref.setSummary(R.string.pref_button_2_default);
		button2Pref.setText(getString(R.string.pref_button_2_default));
		
		button3Pref.setSummary(R.string.pref_button_3_default);
		button3Pref.setText(getString(R.string.pref_button_3_default));
		
		button4Pref.setSummary(R.string.pref_button_4_default);
		button4Pref.setText(getString(R.string.pref_button_4_default));
		
		initialScorePref.setSummary(R.string.pref_initial_score_default);
		initialScorePref.setText(getString(R.string.pref_initial_score_default));
		
		updateDelayPref.setText(getString(R.string.pref_update_delay_default));
		
		useWakeLockPref.setChecked(Boolean.parseBoolean(getString(R.string.pref_use_wake_lock_default)));
		
		Toast.makeText(this, R.string.toast_settings_reset, Toast.LENGTH_SHORT).show();
	}
	
	private Preference findPreferenceById(int resId) {
		return findPreference(getString(resId));
	}

	private void setDynamicSummary(EditTextPreference editTextPreference) {
		// set the summary to be whatever the value is, and change it if necessary
		
		editTextPreference.setSummary(editTextPreference.getText());
		
		editTextPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				
				if (!IntegerUtil.validInt(newValue.toString()) || Integer.parseInt(newValue.toString()) == 0) {
					Toast.makeText(SettingsActivity.this, R.string.toast_no_zeroes, Toast.LENGTH_LONG).show();
					return false;
				}
				
				preference.setSummary((CharSequence)newValue);
				return true;
			}
		});
		
		
	}

	
	
}
