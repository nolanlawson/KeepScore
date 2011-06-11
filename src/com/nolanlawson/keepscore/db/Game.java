package com.nolanlawson.keepscore.db;

import java.util.List;

public class Game {

	private int id = -1;
	private long dateStarted;
	private long dateSaved;
	private String name;
	private boolean autosaved;
	private List<PlayerScore> playerScores;
	
	
	
	public boolean isAutosaved() {
		return autosaved;
	}
	public void setAutosaved(boolean autosaved) {
		this.autosaved = autosaved;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public long getDateStarted() {
		return dateStarted;
	}
	public void setDateStarted(long dateStarted) {
		this.dateStarted = dateStarted;
	}
	public long getDateSaved() {
		return dateSaved;
	}
	public void setDateSaved(long dateSaved) {
		this.dateSaved = dateSaved;
	}
	public List<PlayerScore> getPlayerScores() {
		return playerScores;
	}
	public void setPlayerScores(List<PlayerScore> playerScores) {
		this.playerScores = playerScores;
	}
	@Override
	public String toString() {
		return "Game [autosaved=" + autosaved + ", dateSaved=" + dateSaved
				+ ", dateStarted=" + dateStarted + ", id=" + id + ", name="
				+ name + ", playerScores=" + (playerScores != null ? playerScores.size() : 0) + "]";
	}
	
}
