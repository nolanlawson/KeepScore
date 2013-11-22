package com.nolanlawson.keepscore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.FragmentTransaction;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.nolanlawson.keepscore.data.HistoryItem;
import com.nolanlawson.keepscore.db.Delta;
import com.nolanlawson.keepscore.db.Game;
import com.nolanlawson.keepscore.db.PlayerScore;
import com.nolanlawson.keepscore.helper.ColorScheme;
import com.nolanlawson.keepscore.helper.PreferenceHelper;
import com.nolanlawson.keepscore.util.CollectionUtil;
import com.nolanlawson.keepscore.util.CollectionUtil.Function;
import com.nolanlawson.keepscore.util.IntegerUtil;
import com.nolanlawson.keepscore.util.SparseArrays;
import com.nolanlawson.keepscore.util.UtilLogger;
import com.nolanlawson.keepscore.widget.chart.LineChartLine;
import com.nolanlawson.keepscore.widget.chart.LineChartView;

/**
 * Activity for displaying the entire history of a game
 * 
 * @author nolan
 * 
 */
public class HistoryActivity extends SherlockFragmentActivity implements ActionBar.TabListener {

    private static final UtilLogger log = new UtilLogger(HistoryActivity.class);

    // Public service announcement: You just lost the
    public static final String EXTRA_GAME = "game";
    private static final int MAX_COLUMNS_FOR_WIDE_LIST_LAYOUT = 4;
    private static final int MAX_COLUMNS_FOR_REGULAR_TALL_LIST_LAYOUT = 6;
    
    private static final long TIMELINE_ROUNDING_IN_MS = TimeUnit.SECONDS.toMillis(5);
    
    // valid scale values for the history item width when zooming in and out
    private static final List<Float> ZOOM_VALUES = Arrays.asList(
        0.1F, 0.2F, 0.3F, 0.4F, 0.5F, 0.75F, 1.0F, 1.5F, 2.0F, 2.5F, 3.0F);
    
    private AtomicInteger byRoundZoomIdx = new AtomicInteger(ZOOM_VALUES.indexOf(1.0F));
    private AtomicInteger timelineZoomIdx = new AtomicInteger(ZOOM_VALUES.indexOf(1.0F));
    
    private static final EnumSet<TabDef> CHART_TABS = EnumSet.of(TabDef.ChartByRound, TabDef.ChartByTime);

    private View byRoundChartContainer, timelineContainer, byRoundTableContainer, byPlayerTableContainer;
    private TableLayout byRoundTableLayout, byPlayerTableLayout;
    private LineChartView byRoundLineChartView, timelineChartView;
    private List<ActionBar.Tab> tabs;
    private LayoutInflater inflater;
    
    private Game game;
    
    private Handler handler = new Handler(Looper.getMainLooper());
    
    private static enum TabDef {
        
        ChartByTime(R.string.button_history_time_chart),
        ChartByRound(R.string.button_history_round_chart),
        TableByRound(R.string.button_history_round_table),
        TableByPlayer(R.string.button_history_player_table);
        
        private int titleResId;
        
        private TabDef(int titleResId) {
            this.titleResId = titleResId;
        }

        public int getTitleResId() {
            return titleResId;
        }
    }
    
    private static class TabInfo {
        private View contentView;
        private TabDef tabDefinition;
        
        private TabInfo(View contentView, TabDef tabDefinition) {
            this.contentView = contentView;
            this.tabDefinition = tabDefinition;
        }
        public View getContentView() {
            return contentView;
        }
        public TabDef getTabDefinition() {
            return tabDefinition;
        }
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.history);

        game = getIntent().getParcelableExtra(EXTRA_GAME);

        log.d("intent is %s", getIntent());
        log.d("game is %s", game);

        setUpWidgets();
        setUpActionBar();

        createTimelineLayout();
        createByChartLayout();
        createByPlayerTableLayout();
        createByRoundTableLayout();
    }

    private void setUpActionBar() {

        tabs = new ArrayList<ActionBar.Tab>();
        
        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // home button goes back
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        View[] tabContentViews = {timelineContainer, byRoundChartContainer, byRoundTableContainer, byPlayerTableContainer};
        
        for (int i = 0; i < TabDef.values().length; i++) {
            TabDef tab = TabDef.values()[i];
            createTab(tab, tabContentViews[i], i == 0);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // go back on pressing home in the action bar
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.menu_zoom_in:
                changeZoom(1);
                return true;
            case R.id.menu_zoom_out:
                changeZoom(-1);
                return true;
        }
        return false;
    }
    

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.history_menu, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        
        // only show zoom in/zoom out if the graph is visible
        MenuItem zoomInMenuItem = menu.findItem(R.id.menu_zoom_in);
        MenuItem zoomOutMenuItem = menu.findItem(R.id.menu_zoom_out);
        
        TabInfo currentTabInfo = (TabInfo)getCurrentTab().getTag();
        
        boolean chartVisible = CHART_TABS.contains(currentTabInfo.getTabDefinition());
        
        boolean isTimeline = currentTabInfo.getTabDefinition() == TabDef.ChartByTime;
        
        AtomicInteger zoomIdx = isTimeline ? timelineZoomIdx : byRoundZoomIdx;
        boolean atMin = zoomIdx.get() == 0;
        boolean atMax = zoomIdx.get() == ZOOM_VALUES.size() - 1;
        
        zoomInMenuItem.setEnabled(chartVisible && !atMax);
        zoomInMenuItem.setVisible(chartVisible);
        zoomOutMenuItem.setEnabled(chartVisible && !atMin);
        zoomOutMenuItem.setVisible(chartVisible);
        
        //
        // set the icons to be grayed out if disabled.  It looks prettier that way.  See
        // http://stackoverflow.com/questions/9642990/is-it-possible-to-grey-out-not-just-disable-a-menuitem-in-android
        // for details.
        //
        Drawable zoomInIcon = getResources().getDrawable(R.drawable.action_zoom_in);
        Drawable zoomOutIcon = getResources().getDrawable(R.drawable.action_zoom_out);
        
        if (atMin) {
            zoomOutIcon.mutate().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
        } else if (atMax) {
            zoomInIcon.mutate().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
        }
        
        zoomInMenuItem.setIcon(zoomInIcon);
        zoomOutMenuItem.setIcon(zoomOutIcon);
        
        return true;
    }
    
    private void changeZoom(int delta) {
        
        ActionBar.Tab currentTab = getCurrentTab();
        TabInfo tabInfo = (TabInfo) currentTab.getTag();
        
        boolean isTimeline = tabInfo.getTabDefinition() == TabDef.ChartByTime;
        AtomicInteger zoomIdx = isTimeline ? timelineZoomIdx : byRoundZoomIdx;
        View container = isTimeline ? timelineContainer : byRoundChartContainer;
        LineChartView chart = isTimeline ? timelineChartView : byRoundLineChartView;
        
        // change the zoom on the graph, i.e. update the width of the individual history items
        float zoomValue = ZOOM_VALUES.get(Math.min(ZOOM_VALUES.size() - 1, Math.max(0, zoomIdx.addAndGet(delta))));
        
        chart.setZoomLevel(zoomValue);
        chart.requestLayout();
        chart.invalidate();
        container.invalidate();
        
        supportInvalidateOptionsMenu();
    }

    private void createTab(TabDef tabDefinition, View contentView, boolean selected) {
        ActionBar.Tab tab = getSupportActionBar().newTab();
        tab.setText(getString(tabDefinition.getTitleResId()));
        tab.setTabListener(this);
        
        tab.setTag(new TabInfo(contentView, tabDefinition));
        getSupportActionBar().addTab(tab, selected);
        tabs.add(tab);
    }

    private void setUpWidgets() {

        timelineContainer = findViewById(R.id.timeline_scroll_view);
        byRoundChartContainer = findViewById(R.id.by_chart_scroll_view);
        byRoundTableContainer = findViewById(R.id.by_round_scroll_view);
        byPlayerTableContainer = findViewById(R.id.by_player_scroll_view);
        
        timelineChartView = (LineChartView) findViewById(R.id.timeline_view);
        byRoundLineChartView = (LineChartView) findViewById(R.id.by_chart_view);
        byRoundTableLayout = (TableLayout) findViewById(R.id.by_round_table);
        byPlayerTableLayout = (TableLayout) findViewById(R.id.by_player_table);

        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    private void createTimelineLayout() {
        
        SparseArray<SparseArray<Long>> smoothedData = smoothData(game);
        
        List<LineChartLine> data = new ArrayList<LineChartLine>();
        for (PlayerScore playerScore : game.getPlayerScores()) {
            data.add(new LineChartLine(playerScore.toDisplayName(this).toString(), new ArrayList<Integer>()));
        }
        
        long[] lastPlayerScores = new long[game.getPlayerScores().size()];
        
        for (int i = 0; i < smoothedData.size(); i++) {
            int timeSinceStart = smoothedData.keyAt(i);
            SparseArray<Long> scores = smoothedData.get(timeSinceStart);
            
            for (int playerIdx = 0; playerIdx < game.getPlayerScores().size(); playerIdx++){
                Long scoreObj = scores.get(playerIdx);
                // just give the player a zero-delta (i.e. previous score) for this "round" if no changes
                long score = scoreObj == null ? lastPlayerScores[playerIdx] : scoreObj;
                
                List<Integer> dataPoints = data.get(playerIdx).getDataPoints();
                dataPoints.add((int)score);
                
                lastPlayerScores[playerIdx] = score;
            }
        }
        
        timelineChartView.setLineColors(createLineColors());
        timelineChartView.loadData(data);
    }
    
    private SparseArray<SparseArray<Long>> smoothData(Game game) {
        
        long startTime = Math.round(Math.floor(game.getDateStarted() * 1.0 / TIMELINE_ROUNDING_IN_MS));
        
        // first, plot all players' deltas with their timestamps on the same timeline (x axis), rounded to
        // the nearest ten seconds
        SparseArray<SparseArray<Long>> timeline = new SparseArray<SparseArray<Long>>();
        for (int i = 0; i < game.getPlayerScores().size(); i++) {
            PlayerScore playerScore = game.getPlayerScores().get(i);
            
            // have to include the starting score as well
            long startingScore = playerScore.getScore() - CollectionUtil.sum(CollectionUtil.transform(
                    playerScore.getHistory(), Delta.GET_VALUE));
            
            timeline.put(0, SparseArrays.create(i, startingScore));
            
            long runningTally = startingScore;
            for (Delta delta : playerScore.getHistory()) {
                long roundedTimestamp = Math.round(Math.floor(delta.getTimestamp() * 1.0 / TIMELINE_ROUNDING_IN_MS));
                runningTally += delta.getValue();
                
                int timeSinceStart = (int)(roundedTimestamp - startTime);
                SparseArray<Long> existingScoresAtThisTime = timeline.get(timeSinceStart);
                if (existingScoresAtThisTime == null) {
                    timeline.put(timeSinceStart, SparseArrays.create(i, runningTally));
                } else {
                    // If the same player updated his score twice within the same rounded span,
                    // then just add the two values together
                    existingScoresAtThisTime.put(i, runningTally);
                }
            }
        }
        return timeline;
    }

    private void createByChartLayout() {

        List<LineChartLine> data = new ArrayList<LineChartLine>();

        for (PlayerScore playerScore : game.getPlayerScores()) {
            List<Integer> dataPoints = new ArrayList<Integer>();

            // have to include the starting score as well
            long runningTally = playerScore.getScore() - CollectionUtil.sum(CollectionUtil.transform(
                    playerScore.getHistory(), Delta.GET_VALUE));
            dataPoints.add((int) runningTally);

            for (Delta delta : playerScore.getHistory()) {
                runningTally += delta.getValue();
                dataPoints.add((int) runningTally);
            }

            LineChartLine line = new LineChartLine();
            line.setDataPoints(dataPoints);
            line.setLabel(playerScore.toDisplayName(this).toString());

            data.add(line);
        }

        byRoundLineChartView.setLineColors(createLineColors());
        byRoundLineChartView.loadData(data);
    }

    private List<Integer> createLineColors() {
        
        return CollectionUtil.transform(game.getPlayerScores(), new Function<PlayerScore, Integer>(){

            @Override
            public Integer apply(PlayerScore obj) {
                return obj.getPlayerColor().toChartColor(HistoryActivity.this);
            }
        });
    }

    private void createByPlayerTableLayout() {

        // 'by player' table is a simple 2-column table with a vertical divider
        int counter = 0;
        List<PlayerScore> playerScores = game.getPlayerScores();
        for (int i = 0; i < playerScores.size(); i += 2) {
            PlayerScore leftPlayer = playerScores.get(i);
            PlayerScore rightPlayer = i + 1 < playerScores.size() ? playerScores.get(i + 1) : null;

            // create the header
            TableRow headerRow = new TableRow(this);
            headerRow.addView(createListHeader(headerRow, leftPlayer.toDisplayName(this), true, false));
            headerRow.addView(createDividerView(headerRow));
            headerRow.addView(createListHeader(headerRow, rightPlayer == null ? " " : rightPlayer.toDisplayName(this),
                    true, false));

            byPlayerTableLayout.addView(headerRow);

            // create the body
            Iterator<HistoryItem> leftHistoryItems = HistoryItem.createFromPlayerScore(leftPlayer, this).iterator();
            Iterator<HistoryItem> rightHistoryItems = rightPlayer == null ? Collections.<HistoryItem> emptyList()
                    .iterator() : HistoryItem.createFromPlayerScore(rightPlayer, this).iterator();

            while (leftHistoryItems.hasNext() || rightHistoryItems.hasNext()) {
                HistoryItem leftItem = leftHistoryItems.hasNext() ? leftHistoryItems.next() : null;
                HistoryItem rightItem = rightHistoryItems.hasNext() ? rightHistoryItems.next() : null;

                TableRow tableRow = new TableRow(this);
                tableRow.addView(createHistoryItemView(tableRow, leftItem, R.layout.history_item_wide, counter, true));
                tableRow.addView(createDividerView(tableRow));
                tableRow.addView(createHistoryItemView(tableRow, rightItem, R.layout.history_item_wide, counter, true));
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
            headerRow.addView(createListHeader(headerRow, playerScore.toDisplayName(this), true, false));
        }

        // add a column to the right with an epsilon sign (for the round total
        // sum)
        headerRow.addView(createDividerView(headerRow));
        headerRow.addView(createListHeader(headerRow, getString(R.string.CONSTANT_text_epsilon), false, true));

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
                View historyItemAsView = createHistoryItemView(tableRow, historyItem, historyItemLayoutId, rowId, true);
                tableRow.addView(createDividerView(tableRow));
                tableRow.addView(historyItemAsView);

                sum += historyItem == null ? 0 : historyItem.getDelta();
            }

            // add in the round total (sum)
            tableRow.addView(createDividerView(tableRow));
            if (i == 0) { // first row is just the starting score
                HistoryItem bogusHistoryItem = new HistoryItem(0, sum, true);
                tableRow.addView(createHistoryItemView(tableRow, bogusHistoryItem, historyItemLayoutId, rowId, false));
            } else {
                tableRow.addView(createSumView(tableRow, historyItemLayoutId, rowId, sum));
            }

            byRoundTableLayout.addView(tableRow);
        }

    }

    private View createSumView(ViewGroup parent, int historyItemLayoutId, int rowId, int sum) {
        // create a view that looks like a regular history item view, but is
        // actually just
        // the sum.

        // create a bogus history item
        View view = inflater.inflate(historyItemLayoutId, parent, false);

        // alternating colors for the background, from gray to white
        view.setBackgroundColor(getResources().getColor(
                rowId % 2 == 0 ? android.R.color.background_light : R.color.light_gray));

        TextView textView1 = (TextView) view.findViewById(android.R.id.text1);
        TextView textView2 = (TextView) view.findViewById(android.R.id.text2);

        textView1.setTextColor(getResources().getColor(android.R.color.primary_text_light_nodisable));
        textView1.setText(Integer.toString(sum));

        setDummyTextView(textView2);

        return view;
    }

    private List<HistoryItem> getCollatedHistoryItems() {

        // get all the iterators for the history items
        List<Iterator<HistoryItem>> playerHistoryItems = new ArrayList<Iterator<HistoryItem>>();
        for (PlayerScore playerScore : game.getPlayerScores()) {
            List<HistoryItem> historyItems = HistoryItem.createFromPlayerScore(playerScore, this);
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

    private View createDividerView(ViewGroup parent) {
        return inflater.inflate(R.layout.column_divider, parent, false);
    }

    private View createListHeader(ViewGroup parent, CharSequence text, boolean weightIsOne, boolean gravityCenter) {
        TextView view = (TextView) inflater.inflate(R.layout.history_column_header, parent, false);
        view.setText(text);
        if (gravityCenter) {
            view.setGravity(Gravity.CENTER_HORIZONTAL);
        }

        return weightIsOne ? setLayoutWeightToOne(view) : view;
    }

    private View setLayoutWeightToOne(View view) {
        view.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.MATCH_PARENT, 1.0F));
        return view;
    }

    private View createRowHeader(ViewGroup parent, CharSequence text) {
        View view = inflater.inflate(R.layout.history_row_header, parent, false);
        TextView textView = (TextView) view.findViewById(android.R.id.text1);
        textView.setText(text);
        return view;
    }

    public View createHistoryItemView(ViewGroup parent, HistoryItem historyItem, int layoutResId, int rowId,
            boolean weightIsOne) {

        View view = inflater.inflate(layoutResId, parent, false);

        // alternating colors for the background, from gray to white
        view.setBackgroundColor(getResources().getColor(
                rowId % 2 == 0 ? android.R.color.background_light : R.color.light_gray));

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

            SpannableString deltaSpannable = new SpannableString(IntegerUtil.toCharSequenceWithSign(delta));

            int colorResId = delta >= 0 
                    ? (PreferenceHelper.getGreenTextPreference(this) 
                            ? ColorScheme.Light.getGreenPositiveColorResId() // green
                            : ColorScheme.Light.getPositiveColorResId()) // blue
                    : ColorScheme.Light.getNegativeColorResId(); // red
            ForegroundColorSpan colorSpan = new ForegroundColorSpan(getResources().getColor(colorResId));
            deltaSpannable.setSpan(colorSpan, 0, deltaSpannable.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

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

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        switchToTab(tab);
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
        // do nothing
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
        switchToTab(tab);
    }

    private void switchToTab(ActionBar.Tab tab) {

        final TabInfo tabInfo = (TabInfo)tab.getTag();
        // do in the background to avoid jank
        handler.post(new Runnable() {
            
            @Override
            public void run() {
                
                // switch between the tabs
                for (ActionBar.Tab otherTab : tabs) {
                    TabInfo otherTabInfo = (TabInfo) otherTab.getTag();
                    boolean selected = tabInfo.getTabDefinition().ordinal() == otherTabInfo.getTabDefinition().ordinal();
                    otherTabInfo.getContentView().setVisibility(selected ? View.VISIBLE : View.GONE);
                }
                supportInvalidateOptionsMenu();
            }
        });
    }
    
    private ActionBar.Tab getCurrentTab() {
        for (ActionBar.Tab tab :tabs) {
            if (((TabInfo)tab.getTag()).getContentView().getVisibility() == View.VISIBLE) {
                return tab;
            }
        }
        return null; // shouldn't happen
    }
}
