package com.nolanlawson.keepscore.data;

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
	
}
