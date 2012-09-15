package com.nolanlawson.keepscore.helper;

import android.content.Context;
import android.widget.TextView;

public class ViewHelper {
	
	public static TextView createSimpleTextView(Context context, int resId, Object... args) {
		TextView textView = new TextView(context);
		textView.setTextColor(context.getResources().getColor(android.R.color.primary_text_light_nodisable));
		textView.setText(String.format(context.getString(resId), args));
		textView.setPadding(5, 0, 5, 0);
		return textView;
	}
}
