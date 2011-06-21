package com.nolanlawson.keepscore.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Handler;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nolanlawson.keepscore.R;
import com.nolanlawson.keepscore.db.PlayerScore;
import com.nolanlawson.keepscore.helper.DialogHelper;
import com.nolanlawson.keepscore.helper.DialogHelper.ResultListener;
import com.nolanlawson.keepscore.helper.PreferenceHelper;
import com.nolanlawson.keepscore.util.CollectionUtil;
import com.nolanlawson.keepscore.util.CollectionUtil.Function;
import com.nolanlawson.keepscore.util.IntegerUtil;
import com.nolanlawson.keepscore.util.SpannableUtil;
import com.nolanlawson.keepscore.util.StringUtil;
import com.nolanlawson.keepscore.util.UtilLogger;

public class PlayerView implements OnClickListener, OnLongClickListener {
	
	private static final UtilLogger log = new UtilLogger(PlayerView.class);
	
	private PlayerScore playerScore;
	private AtomicBoolean shouldAutosave = new AtomicBoolean(false);
	
	private View view;
	private TextView nameTextView, scoreTextView, historyTextView, badgeTextView;
	private LinearLayout badgeLinearLayout;
	private Button minusButton, plusButton;
	private Context context;
	private Handler handler;
	
	private AtomicLong lastIncremented = new AtomicLong(0);
	private HistoryUpdateRunnable historyUpdateRunnable;
	private final Object lock = new Object();
	
	public PlayerView(Context context, View view, PlayerScore playerScore, Handler handler) {
		this.view = view;
		this.playerScore = playerScore;
		this.context = context;
		this.handler = handler;
		init();
	}

	private void init() {

		nameTextView = (TextView) view.findViewById(R.id.text_name);
		scoreTextView = (TextView) view.findViewById(R.id.text_score);
		historyTextView = (TextView) view.findViewById(R.id.text_history);
		badgeTextView = (TextView) view.findViewById(R.id.text_badge);
		
		badgeLinearLayout = (LinearLayout) view.findViewById(R.id.linear_layout_badge);
		
		minusButton = (Button) view.findViewById(R.id.button_minus);
		plusButton = (Button) view.findViewById(R.id.button_plus);
		
		minusButton.setOnClickListener(this);
		minusButton.setOnLongClickListener(this);
		plusButton.setOnClickListener(this);
		plusButton.setOnLongClickListener(this);
		nameTextView.setOnClickListener(this);
		nameTextView.setOnLongClickListener(this);
		historyTextView.setOnClickListener(this);
		historyTextView.setOnLongClickListener(this);

		updateViews();
    	
    	log.d("history is: %s", playerScore.getHistory());
		
	}

	public View getView() {
		return view;
	}

	public TextView getNameTextView() {
		return nameTextView;
	}
	
	public TextView getBadgeTextView() {
		return badgeTextView;
	}

	public LinearLayout getBadgeLinearLayout() {
		return badgeLinearLayout;
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
		case R.id.text_history:
			// do nothing - just let it flash the background, so that the user
			// knows this text view is long-clickable
			break;
		}
	}

	private void increment(int delta) {

		long currentTime = System.currentTimeMillis();
		
		long lastIncrementedTime = lastIncremented.getAndSet(currentTime);
		
		if (currentTime - lastIncrementedTime > getUpdateDelayInMs() 
				|| playerScore.getHistory().isEmpty()) {
			
			// if it's been awhile since the last time we incremented
			synchronized (lock) {
				playerScore.getHistory().add(delta);
			}
		} else {
			// else just update the most recent history item
			synchronized (lock) {
				int lastIndex = playerScore.getHistory().size() - 1;
				int newValue = playerScore.getHistory().get(lastIndex) + delta;
				if (newValue == 0) { // don't add "0" to the list; just delete the last history item
					playerScore.getHistory().remove(lastIndex);
					lastIncremented.set(0); // reset the lastIncremented time so we don't update the
					                        // previous value later
				} else {
					playerScore.getHistory().set(lastIndex, newValue);
				}
			}
		}
		
		synchronized (lock) {
			playerScore.setScore(playerScore.getScore() + delta);
		}
		
		// now update the history text view and the total score text view
		updateViews();
		
		shouldAutosave.set(true);
		createDelayedHistoryUpdateTask();
	}
	
	private void updateViews() {

		long currentTime = System.currentTimeMillis();

    	String playerName = playerScore.toDisplayName(context);
    	nameTextView.setText(playerName);
		
		scoreTextView.setText(Long.toString(playerScore.getScore()));
		historyTextView.setText(fromHistory(playerScore.getHistory(), currentTime));
		
		if (currentTime < (lastIncremented.get() + getUpdateDelayInMs()) && !playerScore.getHistory().isEmpty()) { // still modifiable
			// show badge (blibbet)
			badgeLinearLayout.setVisibility(View.VISIBLE);
			Integer lastDelta = playerScore.getHistory().get(playerScore.getHistory().size() - 1);
			badgeTextView.setText(IntegerUtil.toStringWithSign(lastDelta));
			badgeLinearLayout.setBackgroundResource(lastDelta >= 0 ? R.drawable.circle_shape_green : R.drawable.circle_shape_red);
			
		} else {
			// hide badge (blibbet)
			badgeLinearLayout.setVisibility(View.INVISIBLE);
		}
	}

	/**
	 * Add green color for positive entries and red color for negative entries, and convert ints to strings.
	 * @param currentTime 
	 */
	private Spannable fromHistory(List<Integer> history, long currentTime) {
				
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

		// if the most recent history entry is still modifiable, then make it bold
		if (currentTime < (lastIncremented.get() + getUpdateDelayInMs())) { // still modifiable
			// set a bold span for the first entry
			Spannable firstSpannable = spannables.get(0);
			StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
			SpannableUtil.setWholeSpan(firstSpannable, boldSpan);
		}
		
		Spannable result = new SpannableString(
				StringUtil.joinSpannables("\n",CollectionUtil.toArray(spannables, Spannable.class)));
		
		return result;
		
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
				SpannableUtil.setWholeSpan(spannable, colorSpan);
				
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
		case R.id.text_history:
			return showHistoryContextDialog();
		}
		
		
		return false;
	}

	private boolean showHistoryContextDialog() {
		
		List<Integer> history = playerScore.getHistory();
		
		if (history == null || history.isEmpty()) {
			return false; // don't do anything if the history is empty
		}
		
		// don't show "confirm" unless the last history entry is still modifiable
		final boolean showConfirm = 
			System.currentTimeMillis() < (lastIncremented.get() + getUpdateDelayInMs());
		
		CharSequence[] items = showConfirm
				? new CharSequence[]{
						context.getString(R.string.text_confirm),
						context.getString(R.string.text_undo_last)}
				: new CharSequence[]{context.getString(R.string.text_undo_last)};
		
		new AlertDialog.Builder(context)
			.setCancelable(true)
			.setItems(items, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (showConfirm && which == 0) {
						// confirm
						confirmHistory();
					} else {
						// undo last
						undoLast();
					}
					dialog.dismiss();
				}
			})
			.show();
			
		return true;
		
	}

	protected void undoLast() {

		synchronized (lock) {
			List<Integer> history = playerScore.getHistory();
			// undo the last history items
			if (history != null && !history.isEmpty()) {
				Integer removed = history.remove((int)(history.size() - 1));
				playerScore.setScore(playerScore.getScore() - removed);
			}
		}
		lastIncremented.set(0); // reset lastIncremented
		shouldAutosave.set(true);
		updateViews();
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
					
					updateViews();
					
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
						
						updateViews();
						
					}
				}
				
			}
		}, context);
	}

	public void reset(Context context) {
		synchronized (lock) {
			playerScore.setScore(PreferenceHelper.getIntPreference(
					R.string.pref_initial_score, R.string.pref_initial_score_default, context));
			playerScore.setHistory(new ArrayList<Integer>());
		}
		lastIncremented.set(0);
		shouldAutosave.set(true);
		
		updateViews();
		
	}
	
	public void confirmHistory() {
		lastIncremented.set(0); // reset so that the history views will un-bold
		handler.removeCallbacks(getHistoryUpdateRunnable()); // remove pending runnables
		updateViews();
	}

	public void cancelPendingUpdates() {
		getHistoryUpdateRunnable().setCanceled(true);
	}
	
	/**
	 * creates a runnable to be run in after the update delay has completed to un-bold
	 * any bolded history entries
	 */
	private void createDelayedHistoryUpdateTask() {
		Runnable runnable = getHistoryUpdateRunnable();
		handler.removeCallbacks(runnable);
		handler.postDelayed(runnable, getUpdateDelayInMs());
	}
	
	private long getUpdateDelayInMs() {
		return PreferenceHelper.getUpdateDelay(context) * 1000L;
	}
	
	private HistoryUpdateRunnable getHistoryUpdateRunnable() {
		
		if (historyUpdateRunnable == null) {
			historyUpdateRunnable = new HistoryUpdateRunnable();
		}
		return historyUpdateRunnable;
	}	
	
	private class HistoryUpdateRunnable implements Runnable {

		private boolean canceled;
		
		@Override
		public void run() {
			
			if (canceled) {
				return;
			}
			long currentTime = System.currentTimeMillis();
			if (currentTime >= (lastIncremented.get() + getUpdateDelayInMs())
					&& !playerScore.getHistory().isEmpty()) { 
				// not modifiable anymore, need to unbold the last history item
				updateViews();
			}
		}
		
		public void setCanceled(boolean canceled) {
			this.canceled = true;
		}
	}
}
