package com.nolanlawson.keepscore.widget;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.nolanlawson.keepscore.R;
import com.nolanlawson.keepscore.db.PlayerScore;
import com.nolanlawson.keepscore.helper.DialogHelper;
import com.nolanlawson.keepscore.helper.PreferenceHelper;
import com.nolanlawson.keepscore.helper.DialogHelper.ResultListener;
import com.nolanlawson.keepscore.util.CollectionUtil;
import com.nolanlawson.keepscore.util.IntegerUtil;
import com.nolanlawson.keepscore.util.StringUtil;
import com.nolanlawson.keepscore.util.UtilLogger;
import com.nolanlawson.keepscore.util.CollectionUtil.Function;

public class PlayerView implements OnClickListener, OnLongClickListener {
	
	private static final UtilLogger log = new UtilLogger(PlayerView.class);
	
	private PlayerScore playerScore;
	private AtomicBoolean shouldAutosave = new AtomicBoolean(false);
	
	private View view;
	private TextView nameTextView, scoreTextView, historyTextView;
	private Button minusButton, plusButton;
	private Context context;
	
	private AtomicLong lastIncremented = new AtomicLong(0);
	private final Object lock = new Object();
	
	public PlayerView(Context context, View view, PlayerScore playerScore) {
		this.view = view;
		this.playerScore = playerScore;
		this.context = context;
		init();
	}

	private void init() {

		nameTextView = (TextView) view.findViewById(R.id.text_name);
		scoreTextView = (TextView) view.findViewById(R.id.text_score);
		historyTextView = (TextView) view.findViewById(R.id.text_history);
		
		minusButton = (Button) view.findViewById(R.id.button_minus);
		plusButton = (Button) view.findViewById(R.id.button_plus);
		
		minusButton.setOnClickListener(this);
		minusButton.setOnLongClickListener(this);
		plusButton.setOnClickListener(this);
		plusButton.setOnLongClickListener(this);
		nameTextView.setOnClickListener(this);
		nameTextView.setOnLongClickListener(this);

		updateTextViews();
    	
    	log.d("history is: %s", playerScore.getHistory());
		
	}

	public View getView() {
		return view;
	}

	public TextView getNameTextView() {
		return nameTextView;
	}

	public TextView getScoreTextView() {
		return scoreTextView;
	}

	public TextView getHistoryTextView() {
		return historyTextView;
	}

	public Button getMinusButton() {
		return minusButton;
	}

	public Button getPlusButton() {
		return plusButton;
	}

	public AtomicBoolean getShouldAutosave() {
		return shouldAutosave;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_minus:
			increment(-1);
			break;
		case R.id.button_plus:
			increment(1);
			break;
		case R.id.text_name:
			break;
		}
	}

	private void increment(int delta) {

		long currentTime = System.currentTimeMillis();
		
		long lastIncrementedTime = lastIncremented.getAndSet(currentTime);
		
		long updateDelay = PreferenceHelper.getUpdateDelay(context) * 1000; // convert ms to s
		
		if (currentTime - lastIncrementedTime > updateDelay
				&& !(
						!playerScore.getHistory().isEmpty() 
						&& playerScore.getHistory().get(playerScore.getHistory().size() - 1) == 0)) {
			// if it's been awhile since the last time we incremented, OR if the last "update" is set to zero
			// somehow (because the user pressed +1 followed by -1, for instance), then
			// add a new history item
			synchronized (lock) {
				playerScore.getHistory().add(delta);
			}
		} else {
			// else just update the most recent history item
			synchronized (lock) {
				int lastIndex = playerScore.getHistory().size() - 1;
				int newValue = playerScore.getHistory().get(lastIndex) + delta;
				playerScore.getHistory().set(lastIndex, newValue);
			}
		}
		
		synchronized (lock) {
			playerScore.setScore(playerScore.getScore() + delta);
		}
		
		// now update the history text view and the total score text view
		updateTextViews();
		
		shouldAutosave.set(true);
	}
	
	private void updateTextViews() {


    	String playerName = playerScore.toDisplayName(context);
    	nameTextView.setText(playerName);
		
		scoreTextView.setText(Long.toString(playerScore.getScore()));
		historyTextView.setText(fromHistory(playerScore.getHistory()));
		
	}

	/**
	 * Add green color for positive entries and red color for negative entries, and convert ints to strings.
	 */
	private CharSequence fromHistory(List<Integer> history) {
				
		if (history == null || history.isEmpty()) {
			return new SpannableString("");
		}
		
		
		history = CollectionUtil.reversedCopy(history);
		
		// if e.g. there is a double-digit delta (e.g. "+10"), then all other strings need to be padded
		// so that they line up correctly
		int maxChars = CollectionUtil.maxValue(history, new Function<Integer, Integer>(){

			@Override
			public Integer apply(Integer obj) {
				return IntegerUtil.toStringWithSign(obj).length();
			}});
		
		List<Spannable> spannables = CollectionUtil.transform(history, historyToSpan(maxChars));
		
		return StringUtil.joinSpannables("\n",CollectionUtil.toArray(spannables, Spannable.class));
	}
	
	private Function<Integer,Spannable> historyToSpan(final int maxChars) {
		return new Function<Integer, Spannable>() {

			@Override
			public Spannable apply(Integer value) {
				int colorResId = (value >= 0) ? R.color.green : R.color.red;
				ForegroundColorSpan colorSpan = new ForegroundColorSpan(
						context.getResources().getColor(colorResId));
				String str = IntegerUtil.toStringWithSign(value);
				log.d("max length is %s, str is '%s'", maxChars, str);
				str = StringUtil.padLeft(str, ' ', maxChars);
				Spannable spannable = new SpannableString(str);
				spannable.setSpan(colorSpan, 0, spannable.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
				
				return spannable;
			}
			
		};
	}

	@Override
	public boolean onLongClick(View view) {
		// on long click, start up the additional delta values popup
		
		switch (view.getId()) {
		case R.id.button_plus:
			showAdditionalDeltasPopup(true);
			return true;
		case R.id.button_minus:
			showAdditionalDeltasPopup(false);
			return true;
		case R.id.text_name:
			showChangeNameDialog();
			return true;
		}
		
		
		return false;
	}

	private void showChangeNameDialog() {
		
		final EditText editText = new EditText(context);
		editText.setHint(context.getString(R.string.text_player) + " " + (playerScore.getPlayerNumber() + 1));
		editText.setText(StringUtil.nullToEmpty(playerScore.getName()));
		editText.setSingleLine();
		editText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
		new AlertDialog.Builder(context)
			.setTitle(R.string.tile_change_name)
			.setView(editText)
			.setCancelable(true)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
					String newName = StringUtil.nullToEmpty(editText.getText().toString());
					
					playerScore.setName(newName.trim());
					
					updateTextViews();
					
					shouldAutosave.set(true);
					dialog.dismiss();
					
				}
			})
			.setNegativeButton(android.R.string.cancel, null)
			.show();
		
	}

	private void showAdditionalDeltasPopup(boolean positive) {
		DialogHelper.showAdditionalDeltasDialog(positive, new ResultListener<Integer>() {
			
			@Override
			public void onResult(Integer delta) {
				
				if (delta != 0) {
					
					// add the value to the player's score, considering it as its "own" history item
					// regardless of the time since the last delta
					lastIncremented.set(0);
					synchronized (lock) {
						playerScore.setScore(playerScore.getScore() + delta);
						playerScore.getHistory().add(delta);
						
						updateTextViews();
						
					}
				}
				
			}
		}, context);
	}
}
