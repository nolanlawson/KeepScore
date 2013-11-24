package com.nolanlawson.keepscore.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nolanlawson.keepscore.GameActivity;
import com.nolanlawson.keepscore.R;
import com.nolanlawson.keepscore.db.Delta;
import com.nolanlawson.keepscore.db.Game;
import com.nolanlawson.keepscore.db.PlayerScore;
import com.nolanlawson.keepscore.util.CollectionUtil;
import com.nolanlawson.keepscore.widget.chart.LineChartLine;
import com.nolanlawson.keepscore.widget.chart.LineChartView;

public class HistoryRoundChartFragment extends AbstractHistoryChartFragment {
    private Game game;
    private LineChartView byRoundLineChartView;
    private View container;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        
        View rootView = inflater.inflate(R.layout.fragment_round_chart, container, false);
        
        this.container = rootView.findViewById(R.id.by_chart_scroll_view);
        byRoundLineChartView = (LineChartView) rootView.findViewById(R.id.by_chart_view);
        
        return rootView;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        game = getArguments().getParcelable(GameActivity.EXTRA_GAME);
        createByChartLayout(getActivity());
    }
    
    private void createByChartLayout(Activity activity) {

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
            line.setLabel(playerScore.toDisplayName(activity).toString());

            data.add(line);
        }

        byRoundLineChartView.setLineColors(createLineColors(game, activity));
        byRoundLineChartView.loadData(data);
    }
    
    @Override
    protected LineChartView getChart() {
        return byRoundLineChartView;
    }

    @Override
    protected View getContainer() {
        return container;
    }
    
}
