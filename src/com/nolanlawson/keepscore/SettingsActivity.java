package com.nolanlawson.keepscore;

import com.nolanlawson.keepscore.helper.PreferenceHelper;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.text.TextUtils;
import android.widget.Toast;

public class SettingsActivity extends PreferenceActivity {

	private EditTextPreference button1Pref, button2Pref, button3Pref, button4Pref, updateDelayPref;
	private CheckBoxPreference useWakeLockPref;
	
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
		
		// update the preference's summary with whatever the value is, as it's changed
		for (EditTextPreference pref : new EditTextPreference[]{
				button1Pref, button2Pref, button3Pref, button4Pref}) {
			setDynamicSummary(pref);
		}
		
		// do a special check for the update delay value
		updateDelayPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if (TextUtils.isEmpty(newValue.toString()) 
						|| Integer.parseInt(newValue.toString()) < 1
						|| Integer.parseInt(newValue.toString()) > 600) {
					Toast.makeText(SettingsActivity.this, R.string.toast_valid_update_delay_values, Toast.LENGTH_LONG).show();
					return false;
				}
				PreferenceHelper.resetCache();
				return true;
			}
		});
		
		useWakeLockPref = (CheckBoxPreference) findPreferenceById(R.string.pref_use_wake_lock);
		
		
	}

	private Preference findPreferenceById(int resId) {
		return findPreference(getString(resId));
	}

	private void setDynamicSummary(EditTextPreference editTextPreference) {
		// set the summary to be whatever the value is, and change it if necessary
		
		editTextPreference.setSummary(editTextPreference.getText());
		
		editTextPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				
				if (TextUtils.isEmpty(newValue.toString()) || Integer.parseInt(newValue.toString()) == 0) {
					Toast.makeText(SettingsActivity.this, R.string.toast_no_zeroes, Toast.LENGTH_LONG).show();
					return false;
				}
				
				preference.setSummary((CharSequence)newValue);
				return true;
			}
		});
		
		
	}

	
	
}
