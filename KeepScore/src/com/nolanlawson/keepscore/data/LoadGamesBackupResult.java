package com.nolanlawson.keepscore.data;

import java.util.List;

import com.nolanlawson.keepscore.db.Game;

/**
 * Representation of the result of loading a GamesBackup.
 * @author nolan
 *
 */
public class LoadGamesBackupResult {

	private String filename;
	private int numFound;
	private int numDuplicates;
	private List<Game> loadedGames;
	
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public int getNumFound() {
		return numFound;
	}
	public void setNumFound(int numFound) {
		this.numFound = numFound;
	}
	public List<Game> getLoadedGames() {
		return loadedGames;
	}
	public void setLoadedGames(List<Game> loadedGames) {
		this.loadedGames = loadedGames;
	}
	public int getNumDuplicates() {
		return numDuplicates;
	}
	public void setNumDuplicates(int numDuplicates) {
		this.numDuplicates = numDuplicates;
	}
}
