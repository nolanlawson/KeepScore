package com.nolanlawson.keepscore.widget;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.nolanlawson.keepscore.R;
import com.nolanlawson.keepscore.db.PlayerScore;
import com.nolanlawson.keepscore.util.CollectionUtil;
import com.nolanlawson.keepscore.util.StringUtil;
import com.nolanlawson.keepscore.util.UtilLogger;
import com.nolanlawson.keepscore.util.CollectionUtil.Function;

public class PlayerView implements OnClickListener {

	public static final long LAST_INCREMENTED_WAIT_TIME = 10000;
	
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
		plusButton.setOnClickListener(this);
		

    	String playerName = !TextUtils.isEmpty(playerScore.getName()) 
    			? playerScore.getName() 
    			: (context.getString(R.string.text_player) + " " + (playerScore.getPlayerNumber() + 1));
    	nameTextView.setText(playerName);
    	
    	scoreTextView.setText(Long.toString(playerScore.getScore()));
   		historyTextView.setText(fromHistory(playerScore.getHistory()));
    	
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
		}
		
		shouldAutosave.set(true);
		
	}

	private void increment(int delta) {

		long currentTime = System.currentTimeMillis();
		
		long lastIncrementedTime = lastIncremented.getAndSet(currentTime);
		
		if (currentTime - lastIncrementedTime > LAST_INCREMENTED_WAIT_TIME
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
				if (obj >= 0) {
					return obj.toString().length() + 1; // +1 for the '+' char
				}
				return obj.toString().length();
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
				// add '+' to nonnegative values				
				String str = value >= 0 ? ("+" + value) : Integer.toString(value);
				log.d("max length is %s, str is '%s'", maxChars, str);
				str = StringUtil.padLeft(str, ' ', maxChars);
				Spannable spannable = new SpannableString(str);
				spannable.setSpan(colorSpan, 0, spannable.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
				
				return spannable;
			}
			
		};
	}
}
