package com.nolanlawson.keepscore.fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nolanlawson.keepscore.GameActivity;
import com.nolanlawson.keepscore.R;
import com.nolanlawson.keepscore.db.Delta;
import com.nolanlawson.keepscore.db.Game;
import com.nolanlawson.keepscore.db.PlayerScore;
import com.nolanlawson.keepscore.util.CollectionUtil;
import com.nolanlawson.keepscore.util.SparseArrays;
import com.nolanlawson.keepscore.util.TimeUtil;
import com.nolanlawson.keepscore.util.UtilLogger;
import com.nolanlawson.keepscore.widget.chart.LineChartLine;
import com.nolanlawson.keepscore.widget.chart.LineChartView;

public class HistoryTimelineFragment extends AbstractHistoryChartFragment {
    
    private static UtilLogger log = new UtilLogger(HistoryTimelineFragment.class);

    private static final long TIMELINE_ROUNDING_IN_MS = TimeUnit.SECONDS.toMillis(5);
    
    private LineChartView timelineChartView;
    private View container;
    private Game game;
   
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        
        View rootView = inflater.inflate(R.layout.fragment_timeline, container, false);
        
        this.container = rootView.findViewById(R.id.timeline_scroll_view);
        timelineChartView = (LineChartView) rootView.findViewById(R.id.timeline_view);
        
        return rootView;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        game = getArguments().getParcelable(GameActivity.EXTRA_GAME);
        
        createTimelineLayout(getActivity());
    }
    

    private void createTimelineLayout(Activity activity) {
        
        List<String> xAxisLabels = new ArrayList<String>();
        SparseArray<SparseArray<Long>> smoothedData = smoothData(game);
        
        List<LineChartLine> data = new ArrayList<LineChartLine>();
        for (PlayerScore playerScore : game.getPlayerScores()) {
            data.add(new LineChartLine(playerScore.toDisplayName(activity).toString(), new ArrayList<Integer>()));
        }
        
        long[] lastPlayerScores = new long[game.getPlayerScores().size()];
        
        for (int i = 0; i < smoothedData.size(); i++) {
            int timeSinceStart = smoothedData.keyAt(i);
            xAxisLabels.add(TimeUtil.formatSeconds(timeSinceStart));
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
        
        timelineChartView.setLineColors(createLineColors(game, activity));
        timelineChartView.setxAxisLabels(xAxisLabels);
        log.d("x labels are %s", xAxisLabels);
        timelineChartView.loadData(data);
    }
    

    private SparseArray<SparseArray<Long>> smoothData(Game game) {
        
        long roundedStartTimeInMs = Math.round(Math.floor(
                game.getDateStarted() * 1.0 / TIMELINE_ROUNDING_IN_MS)) * TIMELINE_ROUNDING_IN_MS;
        
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
                runningTally += delta.getValue();
                
                long timeSinceStartInMs = delta.getTimestamp() - roundedStartTimeInMs;
                int roundedTimeSinceStartInSecs = (int)TimeUnit.MILLISECONDS.toSeconds(Math.round(Math.floor(
                        timeSinceStartInMs * 1.0 / TIMELINE_ROUNDING_IN_MS)) * TIMELINE_ROUNDING_IN_MS);
                
                if (roundedTimeSinceStartInSecs == 0) {
                    // just in case someone was actually fast enough to log the first score in <5 seconds, bump
                    // it up to the first mark instead
                    roundedTimeSinceStartInSecs = (int)TimeUnit.MILLISECONDS.toSeconds(TIMELINE_ROUNDING_IN_MS);
                }
                
                log.d("roundedStartTimeInMs: %s, timeSinceStartInMs: %s, roundedTimeSinceStartInSecs: %s",
                        roundedStartTimeInMs, timeSinceStartInMs, roundedTimeSinceStartInSecs);
                
                SparseArray<Long> existingScoresAtThisTime = timeline.get(roundedTimeSinceStartInSecs);
                if (existingScoresAtThisTime == null) {
                    timeline.put(roundedTimeSinceStartInSecs, SparseArrays.create(i, runningTally));
                } else {
                    // If the same player updated his score twice within the same rounded span,
                    // then just add the two values together
                    existingScoresAtThisTime.put(i, runningTally);
                }
            }
        }
        return timeline;
    }

    @Override
    protected LineChartView getChart() {
        return timelineChartView;
    }

    @Override
    protected View getContainer() {
        return container;
    }
    
}
