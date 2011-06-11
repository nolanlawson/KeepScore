package com.nolanlawson.keepscore.db;

import java.util.Comparator;
import java.util.List;

public class PlayerScore {

	private int id = -1;
	private String name;
	private long score;
	private int playerNumber;
	private List<Integer> history;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public long getScore() {
		return score;
	}
	public void setScore(long score) {
		this.score = score;
	}
	public int getPlayerNumber() {
		return playerNumber;
	}
	public void setPlayerNumber(int playerNumber) {
		this.playerNumber = playerNumber;
	}
	public List<Integer> getHistory() {
		return history;
	}
	public void setHistory(List<Integer> history) {
		this.history = history;
	}
	
	@Override
	public String toString() {
		return "PlayerScore [history=" + history + ", id=" + id + ", name="
				+ name + ", playerNumber=" + playerNumber + ", score=" + score
				+ "]";
	}
	public static Comparator<PlayerScore> sortByPlayerNumber() {
		return new Comparator<PlayerScore>() {

			@Override
			public int compare(PlayerScore left, PlayerScore right) {
				return left.getPlayerNumber() - right.getPlayerNumber();
			}
		};
	}
}
