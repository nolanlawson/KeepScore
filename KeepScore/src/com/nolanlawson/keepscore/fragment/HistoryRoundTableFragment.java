package com.nolanlawson.keepscore.fragment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.nolanlawson.keepscore.GameActivity;
import com.nolanlawson.keepscore.R;
import com.nolanlawson.keepscore.data.HistoryItem;
import com.nolanlawson.keepscore.db.Game;
import com.nolanlawson.keepscore.db.PlayerScore;

public class HistoryRoundTableFragment extends AbstractHistoryTableFragment {
    private Game game;

    private LayoutInflater inflater;
    
    private TableLayout byRoundTableLayout;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        this.inflater = inflater;
        
        game = getArguments().getParcelable(GameActivity.EXTRA_GAME);
        View rootView = inflater.inflate(R.layout.fragment_round_table, container, false);
        
        byRoundTableLayout = (TableLayout) rootView.findViewById(R.id.by_round_table);
        
        return rootView;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        createByRoundTableLayout(getActivity());
    }
    

    private void createByRoundTableLayout(Activity activity) {

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
        TableRow headerRow = new TableRow(activity);
        headerRow.addView(createListHeader(headerRow, " ", false, false));

        // add in all the section headers first, so they can be laid out across
        // as the first row

        for (PlayerScore playerScore : playerScores) {
            headerRow.addView(createDividerView(headerRow));
            headerRow.addView(createListHeader(headerRow, playerScore.toDisplayName(activity), true, false));
        }

        // add a column to the right with an epsilon sign (for the round total
        // sum)
        headerRow.addView(createDividerView(headerRow));
        headerRow.addView(createListHeader(headerRow, getString(R.string.CONSTANT_text_epsilon), false, true));

        byRoundTableLayout.addView(headerRow);

        List<HistoryItem> collatedHistoryItems = getCollatedHistoryItems(activity);

        for (int i = 0; i < collatedHistoryItems.size(); i += playerScores.size()) {

            int rowId = (i / playerScores.size());

            TableRow tableRow = new TableRow(activity);

            // add a column for the round number
            String roundName = (i == 0) ? "" : Integer.toString(rowId); // first
            // row is  just
            // the starting score
            tableRow.addView(createRowHeader(tableRow, roundName));

            // add in all the history items from this round
            int sum = 0;
            for (int j = i; j < i + playerScores.size(); j++) {
                HistoryItem historyItem = collatedHistoryItems.get(j);
                View historyItemAsView = createHistoryItemView(tableRow, historyItem, historyItemLayoutId, rowId, 
                        true, activity);
                tableRow.addView(createDividerView(tableRow));
                tableRow.addView(historyItemAsView);

                sum += historyItem == null ? 0 : historyItem.getDelta();
            }

            // add in the round total (sum)
            tableRow.addView(createDividerView(tableRow));
            if (i == 0) { // first row is just the starting score
                HistoryItem bogusHistoryItem = new HistoryItem(0, sum, true);
                tableRow.addView(createHistoryItemView(tableRow, bogusHistoryItem, historyItemLayoutId, rowId, 
                        false, activity));
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
    
    private List<HistoryItem> getCollatedHistoryItems(Activity activity) {

        // get all the iterators for the history items
        List<Iterator<HistoryItem>> playerHistoryItems = new ArrayList<Iterator<HistoryItem>>();
        for (PlayerScore playerScore : game.getPlayerScores()) {
            List<HistoryItem> historyItems = HistoryItem.createFromPlayerScore(playerScore, activity);
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
    protected LayoutInflater getInflater() {
        return inflater;
    }
}
