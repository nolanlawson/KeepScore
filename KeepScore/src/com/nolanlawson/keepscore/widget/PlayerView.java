package com.nolanlawson.keepscore.widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewStub;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nolanlawson.keepscore.R;
import com.nolanlawson.keepscore.data.RecordedChange;
import com.nolanlawson.keepscore.data.RecordedChange.Type;
import com.nolanlawson.keepscore.db.Delta;
import com.nolanlawson.keepscore.db.PlayerScore;
import com.nolanlawson.keepscore.helper.ColorScheme;
import com.nolanlawson.keepscore.helper.DialogHelper;
import com.nolanlawson.keepscore.helper.DialogHelper.ResultListener;
import com.nolanlawson.keepscore.helper.PreferenceHelper;
import com.nolanlawson.keepscore.helper.VersionHelper;
import com.nolanlawson.keepscore.util.Callback;
import com.nolanlawson.keepscore.util.CollectionUtil;
import com.nolanlawson.keepscore.util.CollectionUtil.Function;
import com.nolanlawson.keepscore.util.Functions;
import com.nolanlawson.keepscore.util.IntegerUtil;
import com.nolanlawson.keepscore.util.SpannableUtil;
import com.nolanlawson.keepscore.util.StringUtil;
import com.nolanlawson.keepscore.util.UtilLogger;

/**
 * Main view that holds the widgets for a single player, e.g. the player name,
 * the player's score, and any + or - buttons.
 * 
 * @author nolan
 * 
 */
public class PlayerView implements OnClickListener, OnLongClickListener {

    private static final int MIN_NUM_HISTORY_CHARS = 3;
    private static final int ANIMATION_TIME = 1000;

    private static final UtilLogger log = new UtilLogger(PlayerView.class);

    private static final Function<Delta, Integer> DELTA_TO_LENGTH_WITH_SIGN = Functions.chain(
            Delta.GET_VALUE, Functions.INTEGER_TO_LENGTH_WITH_SIGN);
    
    private PlayerScore playerScore;
    
    // use atomic booleans because I'm paranoid and frankly don't understand
    // Android concurrency (and plus it seems to change from version to version
    // of Android, excuses, excuses, etc.)
    private AtomicBoolean shouldAutosave = new AtomicBoolean(false);
    private AtomicBoolean animationWasCanceled = new AtomicBoolean(false);
    private AtomicBoolean animationRunning = new AtomicBoolean(false);
    
    private int positiveTextColor;
    private int negativeTextColor;
    private int borderDrawableResId;
    private Drawable borderDrawable;

    private PlayerColorView playerColorView;
    private ImageView tagImageView;
    private View view, divider1, divider2, deltaButtonsViewStub;
    private AutoResizeTextView scoreTextView, nameTextView;
    private TextView historyTextView, badgeTextView;
    private LinearLayout badgeLinearLayout, onscreenDeltaButtonsLayout;
    private Button minusButton, plusButton, deltaButton1, deltaButton2, deltaButton3, deltaButton4;
    private Button[] deltaButtons;
    private Context context;
    private Handler handler;
    private boolean showOnscreenDeltaButtons;

    private AtomicLong lastIncremented = new AtomicLong(0);
    private HistoryUpdateRunnable historyUpdateRunnable;
    private final Object lock = new Object();
    
    private Runnable onChangeListener;
    private Runnable updateViewsRunnable;
    private Callback<RecordedChange> changeRecorder;

    public PlayerView(Context context, View view, PlayerScore playerScore, Handler handler,
            boolean showOnscreenDeltaButtons) {
        this.view = view;
        this.playerScore = playerScore;
        this.context = context;
        this.handler = handler;
        this.showOnscreenDeltaButtons = showOnscreenDeltaButtons;
        init();
    }

    private void init() {

        // enable or disable onscreen delta buttons based on whether we have
        // enough room onscreen or not
        deltaButtonsViewStub = view.findViewById(R.id.onscreen_delta_buttons_stub);
        if (deltaButtonsViewStub != null) { // they're null in portrait mode
            int versionInt = VersionHelper.getVersionSdkIntCompat();
            if (versionInt > VersionHelper.VERSION_DONUT && versionInt < VersionHelper.VERSION_FROYO) {
                // in eclair, there's a bug where ViewStubs within ViewSubs do not
                // render correctly, so inflate the ViewStubs no matter what
                if (deltaButtonsViewStub instanceof ViewStub) {
                    deltaButtonsViewStub = ((ViewStub) deltaButtonsViewStub).inflate();
                }
    
            }
            deltaButtonsViewStub.setVisibility(showOnscreenDeltaButtons ? View.VISIBLE : View.GONE);
            onscreenDeltaButtonsLayout = (LinearLayout) view.findViewById(R.id.onscreen_delta_buttons_table_layout);
        }

        playerColorView = (PlayerColorView) view.findViewById(R.id.player_color_image);
        tagImageView = (ImageView) view.findViewById(R.id.image_name_tag);
        divider1 = view.findViewById(R.id.player_score_divider_1);
        divider2 = view.findViewById(R.id.player_score_divider_2);
        nameTextView = (AutoResizeTextView) view.findViewById(R.id.text_name);
        scoreTextView = (AutoResizeTextView) view.findViewById(R.id.text_score);
        scoreTextView.resizeText();
        historyTextView = (TextView) view.findViewById(R.id.text_history);
        badgeTextView = (TextView) view.findViewById(R.id.text_badge);
        badgeLinearLayout = (LinearLayout) view.findViewById(R.id.linear_layout_badge);

        minusButton = (Button) view.findViewById(R.id.button_minus);
        plusButton = (Button) view.findViewById(R.id.button_plus);
        deltaButton1 = (Button) view.findViewById(android.R.id.button1);
        deltaButton2 = (Button) view.findViewById(android.R.id.button2);
        deltaButton3 = (Button) view.findViewById(android.R.id.button3);
        deltaButton4 = (Button) view.findViewById(R.id.button4);
        deltaButtons = new Button[] { deltaButton1, deltaButton2, deltaButton3, deltaButton4 };
        for (Button deltaButton : deltaButtons) {
            if (deltaButton != null) {
                deltaButton.setOnClickListener(this);
            }
        }
        minusButton.setOnClickListener(this);
        minusButton.setOnLongClickListener(this);
        plusButton.setOnClickListener(this);
        plusButton.setOnLongClickListener(this);
        historyTextView.setOnClickListener(this);
        historyTextView.setOnLongClickListener(this);
        badgeLinearLayout.setOnClickListener(this);

        ColorScheme colorScheme = PreferenceHelper.getColorScheme(context);
        setNewColorScheme(colorScheme);

        updateViews();

        log.d("history is: %s", playerScore.getHistory());

    }

    public void setChangeRecorder(Callback<RecordedChange> changeRecorder) {
        this.changeRecorder = changeRecorder;
    }

    public LinearLayout getOnscreenDeltaButtonsLayout() {
        return onscreenDeltaButtonsLayout;
    }

    public ImageView getTagImageView() {
        return tagImageView;
    }

    public View getDeltaButtonsViewStub() {
        return deltaButtonsViewStub;
    }

    public View getView() {
        return view;
    }

    public AutoResizeTextView getNameTextView() {
        return nameTextView;
    }

    public TextView getBadgeTextView() {
        return badgeTextView;
    }

    public LinearLayout getBadgeLinearLayout() {
        return badgeLinearLayout;
    }

    public AutoResizeTextView getScoreTextView() {
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

    public View getDivider1() {
        return divider1;
    }

    public View getDivider2() {
        return divider2;
    }

    public Button getDeltaButton1() {
        return deltaButton1;
    }

    public Button getDeltaButton2() {
        return deltaButton2;
    }

    public Button getDeltaButton3() {
        return deltaButton3;
    }

    public Button getDeltaButton4() {
        return deltaButton4;
    }

    public void setOnChangeListener(Runnable onChangeListener) {
        this.onChangeListener = onChangeListener;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_minus:
                increment(PreferenceHelper.getMinusButtonValue(context));
                break;
            case R.id.button_plus:
                increment(PreferenceHelper.getPlusButtonValue(context));
                break;
            case R.id.text_history:
                // do nothing - just let it flash the background, so that the
                // user
                // knows this text view is long-clickable
                break;
            case android.R.id.button1: // 2p-mode button #1
                increment(PreferenceHelper.getTwoPlayerDeltaButtonValue(0, context));
                break;
            case android.R.id.button2: // 2p-mode button #2
                increment(PreferenceHelper.getTwoPlayerDeltaButtonValue(1, context));
                break;
            case android.R.id.button3: // 2p-mode button #3
                increment(PreferenceHelper.getTwoPlayerDeltaButtonValue(2, context));
                break;
            case R.id.button4: // 2p-mode button #4
                increment(PreferenceHelper.getTwoPlayerDeltaButtonValue(3, context));
                break;
            case R.id.linear_layout_badge: // badge linear layout
                confirmHistory();
                break;
        }
    }

    private void increment(final int delta) {
        new IncrementDeltaAsyncTask(delta).execute(((Void) null));
    }

    private void incrementInBackground(int value) {
        log.d("incrementInBackground()");
        
        long currentTime = System.currentTimeMillis();
        long lastIncrementedTime = lastIncremented.getAndSet(currentTime);

        if (currentTime - lastIncrementedTime > getUpdateDelayInMs() || playerScore.getHistory().isEmpty()) {
            log.d("it's been awhile");
            Delta delta = new Delta(currentTime, value);
            // if it's been awhile since the last time we incremented
            changeRecorder.onCallback(new RecordedChange(playerScore.getPlayerNumber(), Type.AddNew, delta));
            playerScore.getHistory().add(delta);
        } else {
            log.d("it hasn't been awhile");
            // else just update the most recent history item
            int lastIndex = playerScore.getHistory().size() - 1;
            int newValue = playerScore.getHistory().get(lastIndex).getValue() + value;
            if (newValue == 0) { // don't add "0" to the list; just delete the
                // last history item
                Delta deletedDelta = playerScore.getHistory().remove(lastIndex);
                changeRecorder.onCallback(new RecordedChange(playerScore.getPlayerNumber(), Type.DeleteLastZero,
                        deletedDelta));
                lastIncremented.set(0); // reset the lastIncremented time so we
                // don't update the
                // previous value later
            } else {
                playerScore.getHistory().set(lastIndex, new Delta(currentTime, newValue));
                changeRecorder.onCallback(new RecordedChange(playerScore.getPlayerNumber(), Type.ModifyLast, 
                        new Delta(currentTime, value)));
            }
        }

        playerScore.setScore(playerScore.getScore() + value);

        shouldAutosave.set(true);

        // this runnable updates the history after 10 seconds and makes the
        // blibbet disappear
        createDelayedHistoryUpdateTask();

        // this runnable updates the history text view and the total score text
        // view
        handler.post(getUpdateViewsRunnable());
    }

    public void updateViews() {
        log.d("updateViews()");
        
        long currentTime = System.currentTimeMillis();

        if (borderDrawable == null) {
            borderDrawable = context.getResources().getDrawable(borderDrawableResId);
        }
        view.setBackgroundDrawable(borderDrawable);

        CharSequence playerName = playerScore.toDisplayName(context);
        nameTextView.setText(playerName);
        
        playerColorView.setPlayerColor(playerScore.getPlayerColor());
        playerColorView.setVisibility(PreferenceHelper.getShowColors(context) ? View.VISIBLE : View.GONE);

        scoreTextView.setText(Long.toString(playerScore.getScore()));
        scoreTextView.resizeText();

        if (currentTime < (lastIncremented.get() + getUpdateDelayInMs()) && !playerScore.getHistory().isEmpty()) { 
            log.d("showing badge");
            // still
            // modifiable
            // show badge (blibbet)
            makeBadgeVisible();
            Delta lastDelta = playerScore.getHistory().get(playerScore.getHistory().size() - 1);
            badgeTextView.setText(IntegerUtil.toCharSequenceWithSign(lastDelta.getValue()));
            badgeLinearLayout
                    .setBackgroundResource(lastDelta.getValue() >= 0 ? getPositiveBadge() : R.drawable.badge_red_fade_out);

            // update history text view now rather than later
            setHistoryTextLazily(playerScore.getHistory(), currentTime);
        } else {
            log.d("hiding badge");
            // hide badge (blibbet)

            // update history text view later
            final Spannable newText = fromHistory(playerScore.getHistory(), currentTime);
            final Integer newHash = historyHash(playerScore.getHistory(), currentTime);
            Runnable updateHistoryRunnable = new Runnable() {

                @Override
                public void run() {
                    historyTextView.setText(newText);
                    historyTextView.setTag(newHash);

                }
            };
            fadeOutBadge(updateHistoryRunnable);
        }

        // set values for delta buttons
        if (this.showOnscreenDeltaButtons) {
            for (int i = 0; i < deltaButtons.length; i++) {
                Button button = deltaButtons[i];
                button.setText(IntegerUtil.toCharSequenceWithSign(PreferenceHelper.getTwoPlayerDeltaButtonValue(i, context)));
            }
        }
    }

    private int getPositiveBadge() {
        if (PreferenceHelper.getGreenTextPreference(context)) {
            return R.drawable.badge_green_fade_out;
        } else {
            return R.drawable.badge_blue_fade_out;
        }
    }

    private int getPositiveTextColor(ColorScheme colorScheme) {
        if (PreferenceHelper.getGreenTextPreference(context)) {
            return colorScheme.getGreenPositiveColorResId();
        } else {
            return colorScheme.getPositiveColorResId();
        }
    }

    private void makeBadgeVisible() {
        synchronized (lock) {
            // show the badge, canceling the "fade out" animation if necessary
            TransitionDrawable transitionDrawable = (TransitionDrawable) badgeLinearLayout.getBackground();
            transitionDrawable.resetTransition();
            log.d("reset transition");
            if (badgeTextView.getAnimation() != null) {
                log.d("cleared animation");
                badgeTextView.clearAnimation();
                animationWasCanceled.set(true);
            }
            badgeTextView.setVisibility(View.VISIBLE);
            badgeLinearLayout.setVisibility(View.VISIBLE);
        }
    }

    private void fadeOutBadge(final Runnable onAnimationComplete) {
        synchronized (lock) {

            if (!animationRunning.get() // animation is already running, so shouldn't
                    // start a new one
                    && lastIncremented.get() != 0 // counter was reset, in which
                    // case it would be
                    // unintuitive for the badge
                    // to fade
                    && badgeTextView.getVisibility() == View.VISIBLE) {
                // animation isn't already showing, and the badge is visible
                animationRunning.set(true);
                animationWasCanceled.set(false);

                badgeLinearLayout.setVisibility(View.VISIBLE);
                // show an animation for the badge with the textview and the
                // background linearlayout fading out
                Animation fadeOutAnimation = AnimationUtils.loadAnimation(context, android.R.anim.fade_out);
                fadeOutAnimation.setDuration(ANIMATION_TIME);
                fadeOutAnimation.setAnimationListener(new AnimationListener() {

                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        synchronized (lock) {
                            log.d("onAnimationEnd, setting visiblity to invisible");
                            if (!animationWasCanceled.get()) {
                                badgeTextView.setVisibility(View.INVISIBLE);
                            }
                            // necessary to update again to set the history text
                            // view correctly
                            onAnimationComplete.run();
                            animationRunning.set(false);
                        }
                    }
                });
                badgeTextView.setAnimation(fadeOutAnimation);
                fadeOutAnimation.start();
                TransitionDrawable transitionDrawable = (TransitionDrawable) badgeLinearLayout.getBackground();
                transitionDrawable.setCrossFadeEnabled(true);
                transitionDrawable.startTransition(ANIMATION_TIME);
            } else {
                // just don't show it - the animation might already be showing,
                // or maybe the badge is
                // already invisible
                badgeLinearLayout.setVisibility(View.INVISIBLE);
                badgeTextView.setVisibility(View.INVISIBLE);

                // this ensures that the history text view gets updated
                // properly, even if the user
                // exits the activity while the animation is in progress (e.g.
                // by going to the Settings)
                onAnimationComplete.run();
            }
        }
    }

    private void setHistoryTextLazily(List<Delta> history, long currentTime) {
        Integer hash = historyHash(history, currentTime);
        if (!hash.equals(historyTextView.getTag())) {
            // need to redraw the history
            historyTextView.setText(fromHistory(history, currentTime));
            historyTextView.setTag(hash);
        }
    }

    /**
     * Do a quick hash of the showable history, as an optimization to check to
     * see if we need to redraw the history or not
     * 
     * @param history
     * @param currentTime
     * @return
     */
    private int historyHash(List<Delta> history, long currentTime) {
        List<Delta> historyToShow = historyToShow(history, currentTime);
        // hash code implementation copied from java.util.Arrays (just the values, not the timestamps)
        if (historyToShow.isEmpty()) {
            return 0;
        }
        
        int result = 1;
        for (Delta delta : historyToShow) {
            result = 31 * result + delta.getValue();
        }
        return result;
    }

    /**
     * Part of the history that can be shown in the little window, i.e.
     * everything but the badge
     * 
     * @param history
     * @param currentTime
     * @return
     */
    private List<Delta> historyToShow(List<Delta> history, long currentTime) {

        boolean stillModifiable = currentTime < (lastIncremented.get() + getUpdateDelayInMs());

        if (history == null || history.isEmpty() || (stillModifiable && history.size() <= 1)) {
            return Collections.emptyList();
        }

        if (stillModifiable) {
            // last element is shown as the badge, so don't show it here
            return history.subList(0, history.size() - 1);
        }
        return history;
    }

    /**
     * Add green color for positive entries and red color for negative entries,
     * and convert ints to strings.
     * 
     * @param currentTime
     */
    private Spannable fromHistory(List<Delta> history, long currentTime) {

        history = historyToShow(history, currentTime);

        if (history.isEmpty()) {
            return null;
        }

        history = CollectionUtil.reversedCopy(history);

        // if e.g. there is a double-digit delta (e.g. "+10"), then all other
        // strings need to be padded
        // so that they line up correctly
        // but ensure there's always at least enough space for 3 chars (e.g.
        // '+10'), because
        // I think it looks nicer and more consistent with most games
        int maxChars = Math.max(MIN_NUM_HISTORY_CHARS,
                CollectionUtil.maxValue(history, DELTA_TO_LENGTH_WITH_SIGN));

        List<Spannable> spannables = CollectionUtil.transform(history, historyToSpan(maxChars));

        Spannable result = new SpannableString(StringUtil.joinSpannables("\n",
                CollectionUtil.toArray(spannables, Spannable.class)));

        return result;

    }

    // I don't expect most delta strings to exceed 4 characters, e.g. "+100"
    private SparseArray<Function<Delta, Spannable>> historyToSpanLookup = 
            new SparseArray<CollectionUtil.Function<Delta,Spannable>>(4);
    
    private Function<Delta, Spannable> historyToSpan(final int maxChars) {
        
        Function<Delta, Spannable> function = historyToSpanLookup.get(maxChars);
        if (function == null) {
            function = new Function<Delta, Spannable>() {

                @Override
                public Spannable apply(Delta delta) {
                    int value = delta.getValue();
                    int colorResId = (value >= 0) ? positiveTextColor : negativeTextColor;
                    ForegroundColorSpan colorSpan = new ForegroundColorSpan(context.getResources().getColor(colorResId));
                    CharSequence str = IntegerUtil.toCharSequenceWithSign(value);
                    str = StringUtil.padLeft(str, ' ', maxChars);
                    Spannable spannable = new SpannableString(str);
                    SpannableUtil.setWholeSpan(spannable, colorSpan);

                    return spannable;
                }
            };
            historyToSpanLookup.put(maxChars, function);
        }
        return function;
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
            case R.id.text_history:
                return showHistoryContextDialog();
        }

        return false;
    }

    private boolean showHistoryContextDialog() {

        final List<CharSequence> items = new ArrayList<CharSequence>();

        // don't show "confirm" unless the last history entry is still
        // modifiable
        boolean showConfirm = System.currentTimeMillis() < (lastIncremented.get() + getUpdateDelayInMs());

        if (showConfirm) {
            items.add(context.getString(R.string.text_confirm));
        }
        if (playerScore.getHistory() != null && !playerScore.getHistory().isEmpty()) {
            // can't undo last if there's no history
            items.add(context.getString(R.string.text_undo_last));
        }
        items.add(context.getString(R.string.text_add_zero));

        new AlertDialog.Builder(context).setCancelable(true)
                .setItems(items.toArray(new CharSequence[items.size()]), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (items.get(which).equals(context.getString(R.string.text_confirm))) {
                            // confirm
                            confirmHistory();
                        } else if (items.get(which).equals(context.getString(R.string.text_undo_last))) {
                            // undo last
                            deleteLast();
                        } else {
                            // add zero
                            addZero();
                        }
                        dialog.dismiss();
                    }
                }).show();

        return true;

    }

    private void addZero() {
        // add zero, i.e. just add a history entry with zero in it
        // this is designed for people playing a game like hearts, where there
        // may be a round
        // with no points for a particular player
        synchronized (lock) {
            Delta delta = new Delta(System.currentTimeMillis(), 0);
            changeRecorder.onCallback(new RecordedChange(playerScore.getPlayerNumber(), Type.AddNew, 
                    delta));
            playerScore.getHistory().add(delta);
        }

        lastIncremented.set(0); // reset last incremented
        shouldAutosave.set(true);
        updateViews();

    }

    private void deleteLast() {

        synchronized (lock) {
            List<Delta> history = playerScore.getHistory();
            // undo the last history items
            if (history != null && !history.isEmpty()) {
                Delta removed = history.remove((int) (history.size() - 1));
                playerScore.setScore(playerScore.getScore() - removed.getValue());
                changeRecorder.onCallback(new RecordedChange(playerScore.getPlayerNumber(), Type.DeleteLast, removed));
            }
        }
        lastIncremented.set(0); // reset lastIncremented
        shouldAutosave.set(true);
        updateViews();
    }

    private void showAdditionalDeltasPopup(final boolean positive) {
        DialogHelper.showAdditionalDeltasDialog(positive, new ResultListener<Integer>() {

            @Override
            public void onResult(Integer delta) {

                if (delta != 0) {
                    // add the value to the player's score, while still
                    // considering it a
                    // "modifiable" history item
                    increment(positive ? delta : -delta);
                } else {
                    addZero();
                }

            }
        }, context);
    }

    public void reset(Context context) {
        synchronized (lock) {
            playerScore.setScore(PreferenceHelper.getIntPreference(R.string.CONSTANT_pref_initial_score,
                    R.string.CONSTANT_pref_initial_score_default, context));
            playerScore.setHistory(new ArrayList<Delta>());
        }
        lastIncremented.set(0);
        shouldAutosave.set(true);

        updateViews();

    }

    public void confirmHistory() {
        lastIncremented.set(0); // reset so that the history views will refresh
        handler.removeCallbacks(getHistoryUpdateRunnable()); // remove pending
        // runnables
        updateViews();
    }

    public void cancelPendingUpdates() {
        getHistoryUpdateRunnable().setCanceled(true);
    }

    /**
     * creates a runnable to be run in after the update delay has completed to
     * move from the "badge" to the history entries
     */
    private void createDelayedHistoryUpdateTask() {
        Runnable runnable = getHistoryUpdateRunnable();
        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable, getUpdateDelayInMs());
    }

    private long getUpdateDelayInMs() {
        return PreferenceHelper.getUpdateDelay(context) * 1000L;
    }

    private Runnable getUpdateViewsRunnable() {
        if (updateViewsRunnable == null) {
            updateViewsRunnable = new Runnable() {

                @Override
                public void run() {
                    updateViews();

                    playerScore.setLastUpdate(System.currentTimeMillis());

                    // this runnable updates the round total, if there is one
                    if (onChangeListener != null) {
                        onChangeListener.run();
                    }

                }
            };
        }
        return updateViewsRunnable;
    }

    private HistoryUpdateRunnable getHistoryUpdateRunnable() {

        if (historyUpdateRunnable == null) {
            historyUpdateRunnable = new HistoryUpdateRunnable();
        }
        return historyUpdateRunnable;
    }

    public void setNewColorScheme(ColorScheme colorScheme) {
        positiveTextColor = getPositiveTextColor(colorScheme);
        negativeTextColor = colorScheme.getNegativeColorResId();
        borderDrawableResId = colorScheme.getBorderDrawableResId();
        borderDrawable = null;
    }

    public void revertChange(RecordedChange recordedChange) {
        synchronized (lock) {

            switch (recordedChange.getType()) {
                case AddNew:
                    playerScore.getHistory().remove(playerScore.getHistory().size() - 1);
                    playerScore.setScore(playerScore.getScore() - recordedChange.getDelta().getValue());
                    break;
                case DeleteLast:
                case DeleteLastZero:
                    playerScore.getHistory().add(recordedChange.getDelta());
                    playerScore.setScore(playerScore.getScore() + recordedChange.getDelta().getValue());
                    break;
                case ModifyLast:
                default:
                    int lastIdx = playerScore.getHistory().size() - 1;
                    Delta lastDelta = playerScore.getHistory().get(lastIdx);
                    lastDelta.setValue(lastDelta.getValue() - recordedChange.getDelta().getValue());
                    playerScore.setScore(playerScore.getScore() - recordedChange.getDelta().getValue());
                    break;
            }
        }
    }

    public void reexecuteChange(RecordedChange recordedChange) {
        synchronized (lock) {

            switch (recordedChange.getType()) {
                case AddNew:
                    playerScore.getHistory().add(recordedChange.getDelta());
                    playerScore.setScore(playerScore.getScore() + recordedChange.getDelta().getValue());
                    break;
                case DeleteLast:
                case DeleteLastZero:
                    playerScore.getHistory().remove(playerScore.getHistory().size() - 1);
                    playerScore.setScore(playerScore.getScore() - recordedChange.getDelta().getValue());
                    break;
                case ModifyLast:
                default:
                    int lastIdx = playerScore.getHistory().size() - 1;
                    Delta lastDelta = playerScore.getHistory().get(lastIdx);
                    lastDelta.setValue(lastDelta.getValue() + recordedChange.getDelta().getValue());
                    playerScore.setScore(playerScore.getScore() + recordedChange.getDelta().getValue());
                    break;
            }
        }
    }

    private class HistoryUpdateRunnable implements Runnable {

        private boolean canceled;

        @Override
        public void run() {

            if (canceled) {
                return;
            }
            long currentTime = System.currentTimeMillis();
            if (currentTime >= (lastIncremented.get() + getUpdateDelayInMs()) && !playerScore.getHistory().isEmpty()) {
                // not modifiable anymore, need to unbold the last history item
                updateViews();
            }
        }

        public void setCanceled(boolean canceled) {
            this.canceled = true;
        }
    }

    private class IncrementDeltaAsyncTask extends AsyncTask<Void, Void, Void> {

        private int delta;

        public IncrementDeltaAsyncTask(int delta) {
            this.delta = delta;
        }

        @Override
        protected Void doInBackground(Void... params) {
            incrementInBackground(delta);
            return null;
        }

    }

    public void resetLastIncremented() {
        lastIncremented.set(0);
    }
}
