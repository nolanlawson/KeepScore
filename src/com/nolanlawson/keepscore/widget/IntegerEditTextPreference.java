package com.nolanlawson.keepscore.widget;

import android.content.Context;
import android.preference.EditTextPreference;
import android.text.method.DigitsKeyListener;
import android.util.AttributeSet;

/**
 * EditTextPreference that only allows inputting integer numbers.
 * @author nlawson
 *
 */
public class IntegerEditTextPreference extends EditTextPreference {

	public IntegerEditTextPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setUpEditText();
	}

	public IntegerEditTextPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		setUpEditText();
	}

	public IntegerEditTextPreference(Context context) {
		super(context);
		setUpEditText();
	}	
	
	private void setUpEditText() {
		getEditText().setKeyListener(DigitsKeyListener.getInstance(true, false));
	}
}
