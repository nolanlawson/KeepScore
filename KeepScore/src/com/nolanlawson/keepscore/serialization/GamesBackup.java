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

    /** Version without "automatic" - i.e., everything was manual */
    public static final int VERSION_ONE = 1;
    
    /** Version where "automatic" was added, to distinguish automatic backups from manual backups */
    public static final int VERSION_TWO = 2;
    
    /** 
     * Version where "backupFilename" was added, for cases where the filename could not easily be determined
     * (e.g. from Gmail attachments)
     */
    public static final int VERSION_THREE = 3;
    
    /** current version number; may change with time */
    public static final int CURRENT_BACKUP_VERSION = VERSION_THREE;
    
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
