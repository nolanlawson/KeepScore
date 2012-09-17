package com.nolanlawson.keepscore.data;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.nolanlawson.keepscore.R;
import com.nolanlawson.keepscore.db.Game;
import com.nolanlawson.keepscore.db.PlayerScore;
import com.nolanlawson.keepscore.util.CollectionUtil;
import com.nolanlawson.keepscore.util.CollectionUtil.Function;
import com.nolanlawson.keepscore.util.Functions;
import com.nolanlawson.keepscore.util.UtilLogger;

public class SavedGameAdapter extends ArrayAdapter<Game> {

	private static UtilLogger log = new UtilLogger(SavedGameAdapter.class);
	
	private Set<Game> checked = new HashSet<Game>();
	private Runnable onCheckChangedRunnable;
	
	public SavedGameAdapter(Context context, List<Game> values) {
		super(context, R.layout.saved_game_item, values);
	}
	
	public Set<Game> getChecked() {
		return checked;
	}
	
	public void setOnCheckChangedRunnable(Runnable onCheckChangedRunnable) {
		this.onCheckChangedRunnable = onCheckChangedRunnable;
	}

	public void setChecked(Set<Game> checked) {
		this.checked = checked;
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
		
		TextView titleTextView = viewWrapper.getTitleTextView();
		TextView numPlayersTextView = viewWrapper.getNumPlayersTextView();
		TextView subtitleTextView = viewWrapper.getSubtitleTextView();
		TextView savedTextView = viewWrapper.getSavedTextView();
		CheckBox checkBox = viewWrapper.getCheckBox();
		
		final Game game = getItem(position);
		
		String gameTitle;
		if (!TextUtils.isEmpty(game.getName())) {
			gameTitle = game.getName();
		} else {
			// Player 1, Player 2, Player3 etc.
			gameTitle = TextUtils.join(", ", CollectionUtil.transform(game.getPlayerScores(), 
					new Function<PlayerScore,String>(){

						@Override
						public String apply(PlayerScore playerScore) {
							return playerScore.toDisplayName(context);
						}
					}
			));
		}
		
		titleTextView.setText(gameTitle);
		
		numPlayersTextView.setText(Integer.toString(game.getPlayerScores().size()));
		
		int numRounds = CollectionUtil.max(game.getPlayerScores(), Functions.PLAYER_SCORE_TO_HISTORY_SIZE);
		int roundsResId = numRounds == 1 ? R.string.text_format_rounds_singular : R.string.text_format_rounds;
		String rounds = String.format(context.getString(roundsResId), numRounds);
		
		subtitleTextView.setText(rounds);
		
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getContext().getString(R.string.date_format));

		savedTextView.setText(simpleDateFormat.format(new Date(game.getDateSaved())));
		
		checkBox.setOnCheckedChangeListener(null);
		checkBox.setChecked(checked.contains(game));
		checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					checked.add(game);
				} else {
					checked.remove(game);
				}
				if (onCheckChangedRunnable != null) {
					onCheckChangedRunnable.run();
				}
			}
		});
		
		log.d("saved long is: %s", game.getDateSaved());
		log.d("started long is: %s", game.getDateStarted());
		
		return view;
	}
	
	private static class ViewWrapper {
		
		private View view;
		private TextView titleTextView, numPlayersTextView, subtitleTextView, savedTextView;
		private CheckBox checkBox;
		
		public ViewWrapper(View view) {
			this.view = view;
		}
		
		public TextView getTitleTextView() {
			if (titleTextView == null) {
				titleTextView = (TextView) view.findViewById(R.id.text_game_title);
			}
			return titleTextView;
		}
		public TextView getNumPlayersTextView() {
			if (numPlayersTextView == null) {
				numPlayersTextView = (TextView) view.findViewById(R.id.text_num_players);
			}
			return numPlayersTextView;
		}
		public TextView getSubtitleTextView() {
			if (subtitleTextView == null) {
				subtitleTextView = (TextView) view.findViewById(R.id.text_game_subtitle);
			}
			return subtitleTextView;
		}
		public TextView getSavedTextView() {
			if (savedTextView == null) {
				savedTextView = (TextView) view.findViewById(R.id.text_date_saved);
			}
			return savedTextView;
		}
		public CheckBox getCheckBox() {
			if (checkBox == null) {
				checkBox = (CheckBox) view.findViewById(android.R.id.checkbox);
			}
			return checkBox;
		}
	}
}
