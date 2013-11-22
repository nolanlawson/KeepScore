package com.nolanlawson.keepscore.data;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nolanlawson.keepscore.R;
import com.nolanlawson.keepscore.util.Callback;

/**
 * Simple adapter to show some text with a delete button to the right.  Used for managing the
 * 'setting sets'.
 * @author nolan
 *
 */
public class TextWithDeleteAdapter extends ArrayAdapter<String> {

	private OnDeleteListener onDeleteListener;
	private Callback<Integer> onClickListener;
	
	public TextWithDeleteAdapter(Context context, List<String> items) {
		super(context, R.layout.text_with_delete, items);
	}
	
	public void setOnDeleteListener(OnDeleteListener onDeleteListener) {
		this.onDeleteListener = onDeleteListener;
	}
    public void setOnItemClickedListener(Callback<Integer> onClickListener) {
        this.onClickListener = onClickListener;
    }
    

	@Override
	public boolean areAllItemsEnabled() {
		return true;
	}

	@Override
	public boolean isEnabled(int position) {
		return true;
	}

	@Override
	public View getView(final int position, View view, ViewGroup parent) {
		
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		view = inflater.inflate(R.layout.text_with_delete, parent, false);
		
		final String textItem = getItem(position);
		TextView textView = (TextView) view.findViewById(android.R.id.text1);
		RelativeLayout relativeLayout = (RelativeLayout) view.findViewById(R.id.root_relative_layout);
		

		textView.setText(textItem);
		// add listener to the delete button
		ImageButton button = (ImageButton) view.findViewById(android.R.id.button1);
		button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//delete button clicked
				onDeleteListener.onDelete(textItem);
			}
		});
		
		relativeLayout.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                onClickListener.onCallback(position);
                
            }
        });
		
		return view;
	}

	@Override
	public int getItemViewType(int position) {
		return 0;
	}

	@Override
	public int getViewTypeCount() {
		return 1;
	}
	
	/**
	 * Listener for 'delete' button press events.
	 * @author nolan
	 *
	 */
	public static interface OnDeleteListener {
		/**
		 * Called when the X button is clicked.  Call adapter.remove(text) and adapter.notifyDataSetChanged()
		 * to notify the adapter if the item should truly be deleted.
		 * @param text
		 */
		void onDelete(String text);
	}
}

