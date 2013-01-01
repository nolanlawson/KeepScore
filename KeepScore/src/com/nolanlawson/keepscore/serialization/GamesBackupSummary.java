package com.nolanlawson.keepscore.serialization;


/**
 * Representation of a saved XML file of games, summarized.
 * @author nolan
 *
 */
public class GamesBackupSummary {

        private int gameCount;
        private long dateSaved;
        private String filename;
        private boolean automatic;
        
        public boolean isAutomatic() {
            return automatic;
        }
        public void setAutomatic(boolean automatic) {
            this.automatic = automatic;
        }
        public String getFilename() {
            return filename;
        }
        public void setFilename(String filename) {
            this.filename = filename;
        }
        public int getGameCount() {
                return gameCount;
        }
        public void setGameCount(int gameCount) {
                this.gameCount = gameCount;
        }
        public long getDateSaved() {
                return dateSaved;
        }
        public void setDateSaved(long dateSaved) {
                this.dateSaved = dateSaved;
        }
}
