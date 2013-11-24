package com.nolanlawson.keepscore.fragment;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.nolanlawson.keepscore.GameActivity;
import com.nolanlawson.keepscore.R;
import com.nolanlawson.keepscore.data.HistoryItem;
import com.nolanlawson.keepscore.db.Game;
import com.nolanlawson.keepscore.db.PlayerScore;

public class HIstoryPlayerTableFragment extends AbstractHistoryTableFragment {
    private Game game;
    
    private TableLayout byPlayerTableLayout;
    
    private LayoutInflater inflater;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        this.inflater = inflater;
        
        View rootView = inflater.inflate(R.layout.fragment_player_table, container, false);
        
        byPlayerTableLayout = (TableLayout) rootView.findViewById(R.id.by_player_table);
        
        return rootView;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        game = getArguments().getParcelable(GameActivity.EXTRA_GAME);
        createByPlayerTableLayout(getActivity());
    }

    private void createByPlayerTableLayout(Activity activity) {

        // 'by player' table is a simple 2-column table with a vertical divider
        int counter = 0;
        List<PlayerScore> playerScores = game.getPlayerScores();
        for (int i = 0; i < playerScores.size(); i += 2) {
            PlayerScore leftPlayer = playerScores.get(i);
            PlayerScore rightPlayer = i + 1 < playerScores.size() ? playerScores.get(i + 1) : null;

            // create the header
            TableRow headerRow = new TableRow(activity);
            headerRow.addView(createListHeader(headerRow, leftPlayer.toDisplayName(activity), true, false));
            headerRow.addView(createDividerView(headerRow));
            headerRow.addView(createListHeader(headerRow, rightPlayer == null ? " " : rightPlayer.toDisplayName(activity),
                    true, false));

            byPlayerTableLayout.addView(headerRow);

            // create the body
            Iterator<HistoryItem> leftHistoryItems = HistoryItem.createFromPlayerScore(leftPlayer, activity).iterator();
            Iterator<HistoryItem> rightHistoryItems = rightPlayer == null ? Collections.<HistoryItem> emptyList()
                    .iterator() : HistoryItem.createFromPlayerScore(rightPlayer, activity).iterator();

            while (leftHistoryItems.hasNext() || rightHistoryItems.hasNext()) {
                HistoryItem leftItem = leftHistoryItems.hasNext() ? leftHistoryItems.next() : null;
                HistoryItem rightItem = rightHistoryItems.hasNext() ? rightHistoryItems.next() : null;

                TableRow tableRow = new TableRow(activity);
                tableRow.addView(createHistoryItemView(tableRow, leftItem, R.layout.history_item_wide, counter, true, activity));
                tableRow.addView(createDividerView(tableRow));
                tableRow.addView(createHistoryItemView(tableRow, rightItem, R.layout.history_item_wide, counter, true, activity));
                byPlayerTableLayout.addView(tableRow);
                counter++;
            }
        }
    }    
    
    @Override
    protected LayoutInflater getInflater() {
        return inflater;
    }
}
