package com.nolanlawson.keepscore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.HorizontalScrollView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.nolanlawson.keepscore.data.HistoryItem;
import com.nolanlawson.keepscore.db.Game;
import com.nolanlawson.keepscore.db.PlayerScore;
import com.nolanlawson.keepscore.util.CollectionUtil;
import com.nolanlawson.keepscore.util.IntegerUtil;
import com.nolanlawson.keepscore.util.UtilLogger;
import com.nolanlawson.keepscore.widget.chart.LineChartLine;
import com.nolanlawson.keepscore.widget.chart.LineChartView;

/**
 * Activity for displaying the entire history of a game
 * 
 * @author nolan
 * 
 */
public class HistoryActivity extends Activity implements
	OnCheckedChangeListener, OnClickListener {

    private static final UtilLogger log = new UtilLogger(HistoryActivity.class);

    // Public service announcement: You just lost the
    public static final String EXTRA_GAME = "game";
    private static final int MAX_COLUMNS_FOR_WIDE_LIST_LAYOUT = 4;
    private static final int MAX_COLUMNS_FOR_REGULAR_TALL_LIST_LAYOUT = 6;

    private RadioGroup radioGroup;
    private ToggleButton byChartButton, byRoundButton, byPlayerButton;
    private HorizontalScrollView byChartScrollView;
    private ScrollView byRoundScrollView, byPlayerScrollView;
    private TableLayout byRoundTableLayout, byPlayerTableLayout;
    private LineChartView lineChartView;
    private LayoutInflater inflater;

    private Game game;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);

	setContentView(R.layout.history);

	game = getIntent().getParcelableExtra(EXTRA_GAME);

	log.d("intent is %s", getIntent());
	log.d("game is %s", game);

	setUpWidgets();

	createByChartLayout();
	createByPlayerTableLayout();
	createByRoundTableLayout();
    }

    private void setUpWidgets() {
	radioGroup = (RadioGroup) findViewById(android.R.id.toggle);
	byChartButton = (ToggleButton) findViewById(R.id.by_chart_button);
	byRoundButton = (ToggleButton) findViewById(R.id.by_round_button);
	byPlayerButton = (ToggleButton) findViewById(R.id.by_player_button);

	radioGroup.setOnCheckedChangeListener(this);
	byChartButton.setOnClickListener(this);
	byRoundButton.setOnClickListener(this);
	byPlayerButton.setOnClickListener(this);

	byChartScrollView = (HorizontalScrollView) findViewById(R.id.by_chart_scroll_view);
	lineChartView = (LineChartView) findViewById(R.id.by_chart_view);

	byRoundTableLayout = (TableLayout) findViewById(R.id.by_round_table);
	byRoundScrollView = (ScrollView) findViewById(R.id.by_round_scroll_view);
	byPlayerTableLayout = (TableLayout) findViewById(R.id.by_player_table);
	byPlayerScrollView = (ScrollView) findViewById(R.id.by_player_scroll_view);

	inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    private void createByChartLayout() {

	List<LineChartLine> data = new ArrayList<LineChartLine>();
	
	for (PlayerScore playerScore : game.getPlayerScores()) {
	    List<Integer> dataPoints = new ArrayList<Integer>(playerScore.getHistory());
	    
	    // have to include the starting score as well
	    long startingScore = playerScore.getScore() - CollectionUtil.sum(playerScore.getHistory());
	    dataPoints.add(0, (int)startingScore);
	    
	    String displayName = playerScore.toDisplayName(this);
	    
	    LineChartLine line = new LineChartLine();
	    line.setDataPoints(dataPoints);
	    line.setLabel(displayName);
	    
	    data.add(line);
	}
	
	lineChartView.setData(data);
	
    }

    private void createByPlayerTableLayout() {

	// 'by player' table is a simple 2-column table with a vertical divider
	int counter = 0;
	List<PlayerScore> playerScores = game.getPlayerScores();
	for (int i = 0; i < playerScores.size(); i += 2) {
	    PlayerScore leftPlayer = playerScores.get(i);
	    PlayerScore rightPlayer = i + 1 < playerScores.size() ? playerScores
		    .get(i + 1) : null;

	    // create the header
	    TableRow headerRow = new TableRow(this);
	    headerRow.addView(createListHeader(headerRow,
		    leftPlayer.toDisplayName(this), true, false));
	    headerRow.addView(createDividerView(headerRow));
	    headerRow
		    .addView(createListHeader(
			    headerRow,
			    rightPlayer == null ? " " : rightPlayer
				    .toDisplayName(this), true, false));

	    byPlayerTableLayout.addView(headerRow);

	    // create the body
	    Iterator<HistoryItem> leftHistoryItems = HistoryItem
		    .createFromPlayerScore(leftPlayer, this).iterator();
	    Iterator<HistoryItem> rightHistoryItems = rightPlayer == null ? Collections
		    .<HistoryItem> emptyList().iterator() : HistoryItem
		    .createFromPlayerScore(rightPlayer, this).iterator();

	    while (leftHistoryItems.hasNext() || rightHistoryItems.hasNext()) {
		HistoryItem leftItem = leftHistoryItems.hasNext() ? leftHistoryItems
			.next() : null;
		HistoryItem rightItem = rightHistoryItems.hasNext() ? rightHistoryItems
			.next() : null;

		TableRow tableRow = new TableRow(this);
		tableRow.addView(createHistoryItemView(tableRow, leftItem,
			R.layout.history_item_wide, counter, true));
		tableRow.addView(createDividerView(tableRow));
		tableRow.addView(createHistoryItemView(tableRow, rightItem,
			R.layout.history_item_wide, counter, true));
		byPlayerTableLayout.addView(tableRow);
		counter++;
	    }
	}

    }

    private void createByRoundTableLayout() {

	// make all the columns that contain history information stretchable and
	// shrinkable,
	// i.e. not the "divider" or "row header" columns
	for (int i = 0; i < game.getPlayerScores().size(); i++) {
	    byRoundTableLayout.setColumnShrinkable((i * 2) + 2, true);
	    byRoundTableLayout.setColumnStretchable((i * 2) + 2, true);
	}

	// the 'by round' adapter simply needs each player name as a first
	// header row, and then after that you just go round-by-round
	// summing up the values and displaying the diff, e.g.:
	// p1, p2, p3, p4
	// 0, 0, 0, 0
	// +5, +3, -2, +10
	// 5, 3, 2, 10
	// etc.

	List<PlayerScore> playerScores = game.getPlayerScores();
	int historyItemLayoutId = playerScores.size() <= MAX_COLUMNS_FOR_WIDE_LIST_LAYOUT ? R.layout.history_item_wide
		: playerScores.size() <= MAX_COLUMNS_FOR_REGULAR_TALL_LIST_LAYOUT ? R.layout.history_item_tall
			: R.layout.history_item_extra_tall;

	// create the first row
	TableRow headerRow = new TableRow(this);
	headerRow.addView(createListHeader(headerRow, " ", false, false));

	// add in all the section headers first, so they can be laid out across
	// as the first row

	for (PlayerScore playerScore : playerScores) {
	    headerRow.addView(createDividerView(headerRow));
	    headerRow.addView(createListHeader(headerRow,
		    playerScore.toDisplayName(this), true, false));
	}

	// add a column to the right with an epsilon sign (for the round total
	// sum)
	headerRow.addView(createDividerView(headerRow));
	headerRow.addView(createListHeader(headerRow,
		getString(R.string.text_epsilon), false, true));

	byRoundTableLayout.addView(headerRow);

	List<HistoryItem> collatedHistoryItems = getCollatedHistoryItems();

	for (int i = 0; i < collatedHistoryItems.size(); i += playerScores.size()) {

	    int rowId = (i / playerScores.size());

	    TableRow tableRow = new TableRow(this);

	    // add a column for the round number
	    String roundName = (i == 0) ? "" : Integer.toString(rowId); // first
									// row
									// is
									// just
									// the
									// starting
									// score
	    tableRow.addView(createRowHeader(tableRow, roundName));

	    // add in all the history items from this round
	    int sum = 0;
	    for (int j = i; j < i + playerScores.size(); j++) {
		HistoryItem historyItem = collatedHistoryItems.get(j);
		View historyItemAsView = createHistoryItemView(tableRow,
			historyItem, historyItemLayoutId, rowId, true);
		tableRow.addView(createDividerView(tableRow));
		tableRow.addView(historyItemAsView);

		sum += historyItem == null ? 0 : historyItem.getDelta();
	    }

	    // add in the round total (sum)
	    tableRow.addView(createDividerView(tableRow));
	    if (i == 0) { // first row is just the starting score
		HistoryItem bogusHistoryItem = new HistoryItem(0, sum, true);
		tableRow.addView(createHistoryItemView(tableRow,
			bogusHistoryItem, historyItemLayoutId, rowId, false));
	    } else {
		tableRow.addView(createSumView(tableRow, historyItemLayoutId,
			rowId, sum));
	    }

	    byRoundTableLayout.addView(tableRow);
	}

    }

    private View createSumView(ViewGroup parent, int historyItemLayoutId,
	    int rowId, int sum) {
	// create a view that looks like a regular history item view, but is
	// actually just
	// the sum.

	// create a bogus history item
	View view = inflater.inflate(historyItemLayoutId, parent, false);

	// alternating colors for the background, from gray to white
	view.setBackgroundColor(getResources().getColor(
		rowId % 2 == 0 ? android.R.color.background_light
			: R.color.light_gray));

	TextView textView1 = (TextView) view.findViewById(android.R.id.text1);
	TextView textView2 = (TextView) view.findViewById(android.R.id.text2);

	textView1.setTextColor(getResources().getColor(
		android.R.color.primary_text_light_nodisable));
	textView1.setText(Integer.toString(sum));

	setDummyTextView(textView2);

	return view;
    }

    private List<HistoryItem> getCollatedHistoryItems() {

	// get all the iterators for the history items
	List<Iterator<HistoryItem>> playerHistoryItems = new ArrayList<Iterator<HistoryItem>>();
	for (PlayerScore playerScore : game.getPlayerScores()) {
	    List<HistoryItem> historyItems = HistoryItem.createFromPlayerScore(
		    playerScore, this);
	    playerHistoryItems.add(historyItems.iterator());
	}

	List<HistoryItem> collatedItems = new ArrayList<HistoryItem>();

	// collate
	while (!allIteratorsAreEmpty(playerHistoryItems)) {
	    for (Iterator<HistoryItem> iterator : playerHistoryItems) {
		if (iterator.hasNext()) {
		    collatedItems.add(iterator.next());
		} else {
		    // add an empty item
		    collatedItems.add(null);
		}
	    }
	}
	return collatedItems;
    }

    private boolean allIteratorsAreEmpty(List<Iterator<HistoryItem>> iterators) {
	for (Iterator<HistoryItem> iterator : iterators) {
	    if (iterator.hasNext()) {
		return false;
	    }
	}
	return true;
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
	// uncheck the other one
	for (ToggleButton button : new ToggleButton[] { byPlayerButton,
		byRoundButton, byChartButton }) {
	    button.setChecked(button.getId() == checkedId);
	}

    }

    @Override
    public void onClick(View view) {
	((RadioGroup) view.getParent()).check(view.getId());

	// switch between the tabs
	byRoundScrollView.setVisibility(view.getId() == R.id.by_round_button ? View.VISIBLE
			: View.GONE);
	byPlayerScrollView.setVisibility(view.getId() == R.id.by_player_button ? View.VISIBLE
			: View.GONE);
	byChartScrollView.setVisibility(view.getId() == R.id.by_chart_button ? View.VISIBLE
			: View.GONE);
    }

    private View createDividerView(ViewGroup parent) {
	return inflater.inflate(R.layout.column_divider, parent, false);
    }

    private View createListHeader(ViewGroup parent, CharSequence text,
	    boolean weightIsOne, boolean gravityCenter) {
	TextView view = (TextView) inflater.inflate(
		R.layout.history_column_header, parent, false);
	view.setText(text);
	if (gravityCenter) {
	    view.setGravity(Gravity.CENTER_HORIZONTAL);
	}

	return weightIsOne ? setLayoutWeightToOne(view) : view;
    }

    private View setLayoutWeightToOne(View view) {
	view.setLayoutParams(new TableRow.LayoutParams(0,
		LayoutParams.FILL_PARENT, 1.0F));
	return view;
    }

    private View createRowHeader(ViewGroup parent, CharSequence text) {
	View view = inflater
		.inflate(R.layout.history_row_header, parent, false);
	TextView textView = (TextView) view.findViewById(android.R.id.text1);
	textView.setText(text);
	return view;
    }

    public View createHistoryItemView(ViewGroup parent,
	    HistoryItem historyItem, int layoutResId, int rowId,
	    boolean weightIsOne) {

	View view = inflater.inflate(layoutResId, parent, false);

	// alternating colors for the background, from gray to white
	view.setBackgroundColor(getResources().getColor(
		rowId % 2 == 0 ? android.R.color.background_light
			: R.color.light_gray));

	TextView textView1 = (TextView) view.findViewById(android.R.id.text1);
	TextView textView2 = (TextView) view.findViewById(android.R.id.text2);

	if (historyItem == null) {
	    // null indicates to leave the text views empty
	    setDummyTextView(textView1);
	    setDummyTextView(textView2);
	    return weightIsOne ? setLayoutWeightToOne(view) : view;
	}

	textView2.setVisibility(View.VISIBLE);

	if (historyItem.isHideDelta()) {
	    setDummyTextView(textView1);
	    textView1.setVisibility(View.GONE); // set as gone to ensure that
						// the first line isn't too tall
						// when we use
						// history_item_tall.xml
	} else {
	    int delta = historyItem.getDelta();

	    SpannableString deltaSpannable = new SpannableString(
		    IntegerUtil.toStringWithSign(delta));

	    int colorResId = delta >= 0 ? R.color.green : R.color.red;
	    ForegroundColorSpan colorSpan = new ForegroundColorSpan(
		    getResources().getColor(colorResId));
	    deltaSpannable.setSpan(colorSpan, 0, deltaSpannable.length(),
		    Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

	    textView1.setVisibility(View.VISIBLE);
	    textView1.setText(deltaSpannable);
	}

	textView2.setText(Long.toString(historyItem.getRunningTotal()));

	return weightIsOne ? setLayoutWeightToOne(view) : view;
    }

    /**
     * For some reason, on Honeycomb tablets I have to set the text view to have
     * a dummy value and the visibility to INVISIBLE - I can't just set the text
     * to null or empty. If I don't, the text isn't wrapped correctly
     * vertically.
     * 
     * @param textView
     */
    public void setDummyTextView(TextView textView) {
	textView.setVisibility(View.INVISIBLE);
	textView.setText("0"); // dummy value
    }
}
