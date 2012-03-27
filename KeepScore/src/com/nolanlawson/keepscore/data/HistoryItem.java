package com.nolanlawson.keepscore.data;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.nolanlawson.keepscore.db.PlayerScore;

/**
 * Simple class for showing the full history of a user's score
 * @author nolan
 *
 */
public class HistoryItem {

	private int delta;
	private long runningTotal;
	private boolean hideDelta;
	
	public HistoryItem(int delta, long runningTotal, boolean hideDelta) {
		this.delta = delta;
		this.runningTotal = runningTotal;
		this.hideDelta = hideDelta;
	}
	public boolean isHideDelta() {
		return hideDelta;
	}
	public void setHideDelta(boolean hideDelta) {
		this.hideDelta = hideDelta;
	}
	public int getDelta() {
		return delta;
	}
	public void setDelta(int delta) {
		this.delta = delta;
	}
	public long getRunningTotal() {
		return runningTotal;
	}
	public void setRunningTotal(long runningTotal) {
		this.runningTotal = runningTotal;
	}
	
	/**
	 * Create a list of displayable history items given a PlayerScore.  Entries should be listed from past to future.
	 * @param playerScore
	 * @return
	 */
	public static List<HistoryItem> createFromPlayerScore(PlayerScore playerScore, Context context) {
		
		List<Integer> history = playerScore.getHistory();
		long runningScore = getStartingScore(playerScore);
		
		List<HistoryItem> historyItems = new ArrayList<HistoryItem>();
		
		// add an initial one to just show the starting score
		historyItems.add(new HistoryItem(0, runningScore, true));
		
		for (Integer historyDelta : history) {
			runningScore += historyDelta;
			
			historyItems.add(new HistoryItem(historyDelta, runningScore, false));
		}
		
		return historyItems;
		
	}
	private static long getStartingScore(PlayerScore playerScore) {
		// figure out what the starting score was by just subtracting everything
		long result = playerScore.getScore();
		for (Integer delta : playerScore.getHistory()) {
			result -= delta;
		}
		return result;
	}	
	
}
