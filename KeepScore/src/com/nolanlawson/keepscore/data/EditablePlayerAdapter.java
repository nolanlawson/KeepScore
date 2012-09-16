package com.nolanlawson.keepscore.data;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.nolanlawson.keepscore.R;
import com.nolanlawson.keepscore.db.PlayerScore;
import com.nolanlawson.keepscore.widget.dragndrop.DropListener;
import com.nolanlawson.keepscore.widget.dragndrop.RemoveListener;

public class EditablePlayerAdapter extends ArrayAdapter<PlayerScore> implements
	DropListener {

    private List<PlayerScore> items;
    
    public EditablePlayerAdapter(Context context, List<PlayerScore> items) {
	super(context, R.layout.editable_player, items);
	this.items = items;
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
    public View getView(int position, View view, ViewGroup parent) {

	if (view == null) {
	    LayoutInflater inflater = (LayoutInflater) getContext()
		    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    view = inflater.inflate(R.layout.editable_player, parent, false);
	}

	final PlayerScore playerScore = getItem(position);
	TextView textView1 = (TextView) view.findViewById(android.R.id.text1);
	TextView textView2 = (TextView) view.findViewById(android.R.id.text2);
	

	textView1.setText(playerScore.toDisplayName(getContext()));
	textView2.setText('#' + Integer.toString(playerScore.getPlayerNumber() + 1));
	// add listener to the delete button
	Button deleteButton = (Button) view
		.findViewById(R.id.button_delete_player);
	deleteButton.setOnClickListener(new OnClickListener() {

	    @Override
	    public void onClick(View v) {
		// delete
		// TODO DELETE LOGIC
		remove(playerScore);
		notifyDataSetChanged();
	    }
	});
	Button editButton = (Button) view.findViewById(R.id.button_edit_player);
	editButton.setOnClickListener(new OnClickListener() {

	    @Override
	    public void onClick(View v) {
		// TODO edit logic

		notifyDataSetChanged();

	    }
	});

	return view;
    }

    @Override
    public int getItemViewType(int position) {
	return position == 0 ? 0 : 1; // first one is 'add', the rest are
				      // different
    }

    @Override
    public int getViewTypeCount() {
	return 2;// one for 'add', one for the others
    }

    public void onDrop(int from, int to) {
	PlayerScore temp = items.get(from);
	items.remove(from);
	items.add(to, temp);
	
	for (int i = 0; i < items.size(); i++) {
	    PlayerScore playerScore  = items.get(i);
	    playerScore.setPlayerNumber(i);
	}
	
	notifyDataSetChanged();
    }

}
