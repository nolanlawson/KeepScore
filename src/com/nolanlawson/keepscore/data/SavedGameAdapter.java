package com.nolanlawson.keepscore.data;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.nolanlawson.keepscore.R;
import com.nolanlawson.keepscore.db.Game;
import com.nolanlawson.keepscore.db.PlayerScore;
import com.nolanlawson.keepscore.util.CollectionUtil;
import com.nolanlawson.keepscore.util.UtilLogger;
import com.nolanlawson.keepscore.util.CollectionUtil.Function;

public class SavedGameAdapter extends ArrayAdapter<Game> {

	private static UtilLogger log = new UtilLogger(SavedGameAdapter.class);
	
	private static final String DATE_FORMAT = "MMM dd hh:mmaa";
	
	public SavedGameAdapter(Context context, List<Game> values) {
		super(context, R.layout.saved_game_item, values);
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		
		// view wrapper optimization per Romain Guy
		final Context context = parent.getContext();
		ViewWrapper viewWrapper;
		if (view == null) {
			LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = vi.inflate(R.layout.saved_game_item, parent, false);
			viewWrapper = new ViewWrapper(view);
			view.setTag(viewWrapper);
		} else {
			viewWrapper = (ViewWrapper)view.getTag();
		}
		
		TextView nameTextView = viewWrapper.getNameTextView();
		TextView numPlayersTextView = viewWrapper.getNumPlayersTextView();
		TextView playersTextView = viewWrapper.getPlayersTextView();
		TextView startedTextView = viewWrapper.getStartedTextView();
		TextView savedTextView = viewWrapper.getSavedTextView();
		
		Game game = getItem(position);
		
		nameTextView.setText(game.getName());
		nameTextView.setVisibility(TextUtils.isEmpty(game.getName()) ? View.GONE : View.VISIBLE);
		
		numPlayersTextView.setText(game.getPlayerScores().size() + " " + context.getString(R.string.text_players));
		
		String players = TextUtils.join(", ", CollectionUtil.transform(game.getPlayerScores(), 
				new Function<PlayerScore,String>(){

			@Override
			public String apply(PlayerScore playerScore) {
				return playerScore.toDisplayName(context) + ": "+ playerScore.getScore();
			}
		}));
		
		playersTextView.setText(players);
		
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT);

		String started = context.getString(R.string.text_started_colon);
		startedTextView.setText(started + " " + simpleDateFormat.format(new Date(game.getDateStarted())));
		
		String saved = context.getString(game.isAutosaved() ? R.string.text_autosaved_colon : R.string.text_saved_colon);
		savedTextView.setText(saved + " " + simpleDateFormat.format(new Date(game.getDateSaved())));
		
		log.d("saved long is: %s", game.getDateSaved());
		log.d("started long is: %s", game.getDateStarted());
		
		return view;
	}
	
	private static class ViewWrapper {
		
		private View view;
		private TextView nameTextView, numPlayersTextView, playersTextView, startedTextView, savedTextView;
		
		public ViewWrapper(View view) {
			this.view = view;
		}
		
		public TextView getNameTextView() {
			if (nameTextView == null) {
				nameTextView = (TextView) view.findViewById(R.id.text_game_name);
			}
			return nameTextView;
		}
		public TextView getNumPlayersTextView() {
			if (numPlayersTextView == null) {
				numPlayersTextView = (TextView) view.findViewById(R.id.text_num_players);
			}
			return numPlayersTextView;
		}
		public TextView getPlayersTextView() {
			if (playersTextView == null) {
				playersTextView = (TextView) view.findViewById(R.id.text_player_names);
			}
			return playersTextView;
		}
		public TextView getStartedTextView() {
			if (startedTextView == null) {
				startedTextView = (TextView) view.findViewById(R.id.text_date_started);
			}
			return startedTextView;
		}
		public TextView getSavedTextView() {
			if (savedTextView == null) {
				savedTextView = (TextView) view.findViewById(R.id.text_date_saved);
			}
			return savedTextView;
		}
		
	}
	
	
	
}
