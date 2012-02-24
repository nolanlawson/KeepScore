package com.nolanlawson.keepscore.helper;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.nolanlawson.keepscore.R;
import com.nolanlawson.keepscore.SettingsActivity;
import com.nolanlawson.keepscore.util.IntegerUtil;

/**
 * Utilities for building up the delta dialog.
 * @author nolan
 *
 */
public class DialogHelper {

	public static interface ResultListener<T>{
		
		public void onResult(T result);
		
	}
	
	public static void showAdditionalDeltasDialog(boolean positive, final ResultListener<Integer> resultListener, 
			final Context context) {

		LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = layoutInflater.inflate(R.layout.delta_popup, null);
		
		prepareDeltaView(view, positive, context);
		
		final EditText editText = (EditText) view.findViewById(android.R.id.edit);
		
		new AlertDialog.Builder(context)
			.setCancelable(true)
			.setTitle(positive ? R.string.tile_add : R.string.tile_subtract)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
					if (resultListener != null) {
						int result = TextUtils.isEmpty(editText.getText()) 
								? 0 
								: Integer.parseInt(editText.getText().toString());
						
						resultListener.onResult(result);
					}
					
					dialog.dismiss();
					
				}
			})
			.setNeutralButton(R.string.button_customize, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					Intent intent = new Intent(context, SettingsActivity.class);
					context.startActivity(intent);
					
				}
			})
			.setNegativeButton(android.R.string.cancel, null)
			.setView(view)
			.show();
		
	}

	private static void prepareDeltaView(View view, boolean positive, Context context) {
		// set the buttons based on the preferences
		
		int button1Value = PreferenceHelper.getPopupDeltaButtonValue(0, context);
		int button2Value = PreferenceHelper.getPopupDeltaButtonValue(1, context);
		int button3Value = PreferenceHelper.getPopupDeltaButtonValue(2, context);
		int button4Value = PreferenceHelper.getPopupDeltaButtonValue(3, context);
		
		if (!positive) {
			button1Value *= -1;
			button2Value *= -1;
			button3Value *= -1;
			button4Value *= -1;
		}
		
		Button button1 = (Button) view.findViewById(android.R.id.button1);
		Button button2 = (Button) view.findViewById(android.R.id.button2);
		Button button3 = (Button) view.findViewById(android.R.id.button3);
		Button button4 = (Button) view.findViewById(R.id.button4);
		EditText editText = (EditText) view.findViewById(android.R.id.edit);
		
		button1.setText(IntegerUtil.toStringWithSign(button1Value));
		button2.setText(IntegerUtil.toStringWithSign(button2Value));
		button3.setText(IntegerUtil.toStringWithSign(button3Value));
		button4.setText(IntegerUtil.toStringWithSign(button4Value));
		
		button1.setOnClickListener(incrementingOnClickListener(editText, button1Value));
		button2.setOnClickListener(incrementingOnClickListener(editText, button2Value));
		button3.setOnClickListener(incrementingOnClickListener(editText, button3Value));
		button4.setOnClickListener(incrementingOnClickListener(editText, button4Value));
		
	}
	
	private static OnClickListener incrementingOnClickListener(final EditText editText, final int delta) {
		
		return new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				int editTextValue = TextUtils.isEmpty(editText.getText()) 
						? 0 
						: Integer.parseInt(editText.getText().toString());
				editText.setText(Integer.toString(editTextValue + delta));				
			}
		};

		
	}
	
}
