package com.nolanlawson.keepscore.serialization;

import java.util.List;

import com.nolanlawson.keepscore.db.Game;

/**
 * Representation of a saved XML file of games
 * @author nolan
 *
 */
public class GamesBackup {

	public static final int CURRENT_BACKUP_VERSION = 1; // current version number; may change with time
	
	private int gameCount;
	private int version;
	private long dateSaved;
	private List<Game> games;
	
	public int getGameCount() {
		return gameCount;
	}
	public void setGameCount(int gameCount) {
		this.gameCount = gameCount;
	}
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	public List<Game> getGames() {
		return games;
	}
	public void setGames(List<Game> games) {
		this.games = games;
	}
	public long getDateSaved() {
		return dateSaved;
	}
	public void setDateSaved(long dateSaved) {
		this.dateSaved = dateSaved;
	}
}
