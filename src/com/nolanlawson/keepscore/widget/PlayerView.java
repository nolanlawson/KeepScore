package com.nolanlawson.keepscore.widget;

import java.util.concurrent.atomic.AtomicLong;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.nolanlawson.keepscore.R;
import com.nolanlawson.keepscore.db.PlayerScore;
import com.nolanlawson.keepscore.util.CollectionUtil;

public class PlayerView implements OnClickListener {

	public static final long LAST_INCREMENTED_WAIT_TIME = 10000;
	
	private PlayerScore playerScore;
	
	private View view;
	private TextView name, score, history;
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

		name = (TextView) view.findViewById(R.id.text_name);
		score = (TextView) view.findViewById(R.id.text_score);
		history = (TextView) view.findViewById(R.id.text_history);
		
		minusButton = (Button) view.findViewById(R.id.button_minus);
		plusButton = (Button) view.findViewById(R.id.button_plus);
		
		minusButton.setOnClickListener(this);
		plusButton.setOnClickListener(this);
		
	}

	public View getView() {
		return view;
	}

	public TextView getName() {
		return name;
	}

	public TextView getScore() {
		return score;
	}

	public TextView getHistory() {
		return history;
	}

	public Button getMinusButton() {
		return minusButton;
	}

	public Button getPlusButton() {
		return plusButton;
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
		
		score.setText(Long.toString(playerScore.getScore()));
		
		history.setText(TextUtils.join("\n", CollectionUtil.reversedCopy(playerScore.getHistory())));
		
		
		
	}
}
