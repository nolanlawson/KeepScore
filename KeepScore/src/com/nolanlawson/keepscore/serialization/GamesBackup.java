package com.nolanlawson.keepscore.serialization;

import java.util.List;

import com.nolanlawson.keepscore.db.Game;

/**
 * Representation of a saved XML file of games
 * 
 * @author nolan
 * 
 */
public class GamesBackup {
    
    private int gameCount;
    private int version;
    private long dateSaved;
    private boolean automatic;
    private String filename;
    private List<Game> games;

    
    public String getFilename() {
        return filename;
    }
    public void setFilename(String filename) {
        this.filename = filename;
    }
    public boolean isAutomatic() {
        return automatic;
    }

    public void setAutomatic(boolean automatic) {
        this.automatic = automatic;
    }

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
