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

    /** version without "automatic" - i.e., everything was manual */
    public static final int VERSION_ONE = 1;
    
    /** version where "automatic" was added, to distinguish automatic backups from manual backups */
    public static final int VERSION_TWO = 2;
    
    /** current version number; may change with time */
    public static final int CURRENT_BACKUP_VERSION = VERSION_TWO;
    
    private int gameCount;
    private int version;
    private long dateSaved;
    private boolean automatic;
    private List<Game> games;

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
