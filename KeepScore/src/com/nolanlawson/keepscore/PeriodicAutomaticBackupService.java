package com.nolanlawson.keepscore;

import java.util.List;

import android.app.IntentService;
import android.content.Intent;

import com.nolanlawson.keepscore.db.Game;
import com.nolanlawson.keepscore.db.GameDBHelper;
import com.nolanlawson.keepscore.helper.SdcardHelper;
import com.nolanlawson.keepscore.helper.SdcardHelper.Format;
import com.nolanlawson.keepscore.serialization.GamesBackup;
import com.nolanlawson.keepscore.serialization.GamesBackupSerializer;
import com.nolanlawson.keepscore.util.UtilLogger;

/**
 * Task to run occasionally in order to automatically back up saved games to an
 * XML file.
 * 
 * @author nolan
 * 
 */
public class PeriodicAutomaticBackupService extends IntentService {

    UtilLogger log = new UtilLogger(PeriodicAutomaticBackupService.class);
    
    /** action to use to call this receiver */
    public static final String INTENT_ACTION = "com.nolanlawson.keepscore.periodicbackup";
    
    /** random int */
    public static final int INTENT_REQUEST_CODE = 458010910;
    
    public static final String SERVICE_NAME = PeriodicAutomaticBackupService.class.getSimpleName();
    
    public PeriodicAutomaticBackupService() {
        super(SERVICE_NAME);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String filename = SdcardHelper.createBackupFilename(Format.GZIP);
        GameDBHelper dbHelper =  null;
        try {
            dbHelper = new GameDBHelper(this);
            List<Game> games = dbHelper.findAllGames();
            
            log.i("Beginning periodic automatic backup of %d saved KeepScore games...", games.size());
            
            GamesBackup gamesBackup = new GamesBackup();
            
            gamesBackup.setVersion(GamesBackup.CURRENT_BACKUP_VERSION);
            gamesBackup.setDateSaved(System.currentTimeMillis());
            gamesBackup.setGameCount(games.size());
            gamesBackup.setAutomatic(true);
            gamesBackup.setFilename(filename);
            gamesBackup.setGames(games);
            
            String xmlData = GamesBackupSerializer.serialize(gamesBackup);
            SdcardHelper.save(filename, Format.GZIP, xmlData);
            
            log.i("KeepScore backed up %d games to \"%s\".", games.size(), filename);
        } finally {
            if (dbHelper != null) {
                dbHelper.close();
            }
        }
    }

    
}
