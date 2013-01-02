package com.nolanlawson.keepscore.helper;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import au.com.bytecode.opencsv.CSVWriter;

import com.nolanlawson.keepscore.R;
import com.nolanlawson.keepscore.db.Game;
import com.nolanlawson.keepscore.db.PlayerScore;
import com.nolanlawson.keepscore.util.CollectionUtil;
import com.nolanlawson.keepscore.util.CollectionUtil.Function;
import com.nolanlawson.keepscore.util.Functions;
import com.nolanlawson.keepscore.util.UtilLogger;

public class SdcardHelper {

    private static final int BUFFER = 0x1000; // 4K

    private static final String ROOT_DIR = "keepscore";
    
    // apparently this is a good format for Excel/LibreOffice
    private static final DateFormat CSV_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    /**
     * 
     * Format to save the backups file in.
     * 
     * Gzip is used to save space.
     * 
     * XML is used because Gmail doesn't let you open zipped files from within the app.
     * @author nolan
     *
     */
    public static enum Format {
        XML, GZIP;
    }
    
    /**
     * 
     * Location on the SD card under "/sdcard/keepscore" to save the backup file.
     * 
     * The "backups" folder is used for backups that the user saves either manually or automatically and
     * wants to retrieve later.
     * 
     * The "shares" folder is just used for backup files that the user wants to send to someone else.  So in
     * principle, it's temporary storage.
     * 
     * Ditto for the "spreadsheets" folder.
     * 
     * @author nolan
     *
     */
    public static enum Location {
        Backups("backups"), 
        Shares("shares"),
        Spreadsheets("spreadsheets")
        ;
        
        private String directoryName;
        
        private Location(String directoryName) {
            this.directoryName = directoryName;
        }
        
        public String getDirectoryName() {
            return directoryName;
        }
    }    

    private static UtilLogger log = new UtilLogger(SdcardHelper.class);

    public static boolean isAvailable() {
        return Environment.getExternalStorageDirectory() != null 
                && Environment.getExternalStorageDirectory().exists();
    }

    public static File getDirectory(Location location) {
        File rootDir = getOrCreateDir(Environment.getExternalStorageDirectory(), ROOT_DIR);
        return getOrCreateDir(rootDir, location.getDirectoryName());
    }
    
    public static boolean backupExists(String filename, Location location) {
        File file = new File(getDirectory(location), filename);
        return file.exists();
    }

    public static List<String> list(Location location) {
        return Arrays.asList(getDirectory(location).list());
    }

    public static String open(Uri uri, Format format, ContentResolver contentResolver) {
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = null;

        try {
            InputStream fileInputStream = contentResolver.openInputStream(uri);
            if (format == Format.GZIP) { // new, gzipped format
                fileInputStream = new GZIPInputStream(fileInputStream);
            }
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
            bufferedReader = new BufferedReader(inputStreamReader, BUFFER);

            while (bufferedReader.ready()) {
                stringBuilder.append(bufferedReader.readLine());
            }
        } catch (IOException ex) {
            log.e(ex, "couldn't read file");
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    log.e(e, "couldn't close buffered reader");
                }
            }
        }
        return stringBuilder.toString();
    }

    /**
     * Write  CSV file with games on the X axis and players (and basic game data) on the Y axis.
     * 
     * @param filename
     * @param games
     * @return
     */
    public static void saveSpreadsheet(String filename, List<Game> games, final Context context) {

        // get all the unique player names so we can put them on the X axis
        SortedSet<String> playerNames = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        
        // special value for players whose names weren't specified
        for (Game game : games) {
            for (PlayerScore playerScore : game.getPlayerScores()) {
                playerNames.add(playerScore.toDisplayName(context));
            }
        }
        
        // sort by start date descending
        List<Game> sortedGames = new ArrayList<Game>(games);
        Collections.sort(sortedGames, new Comparator<Game>() {

            @Override
            public int compare(Game lhs, Game rhs) {
                return Long.valueOf(rhs.getDateStarted()).compareTo(lhs.getDateStarted());
            }
        });
        
        File file = new File(getDirectory(Location.Spreadsheets), filename);
        
        CSVWriter writer = null;
        try {
            writer = new CSVWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), 
                    "UTF-8")), ',');
            
            // write the column names
            List<String> columnNames = new ArrayList<String>(Arrays.asList(context.getResources().getStringArray(
                    R.array.share_spreadsheet_column_names)));

            columnNames.addAll(playerNames);
            writer.writeNext(columnNames.toArray(new String[columnNames.size()]));
            
            // write each game as a line in the CSV
            for (Game game : sortedGames) {
                List<String> entries = new ArrayList<String>();
                
                // date started
                entries.add(CSV_DATE_FORMAT.format(new Date(game.getDateStarted())));
                // date saved
                entries.add(CSV_DATE_FORMAT.format(new Date(game.getDateSaved())));
                // play time, using duration format HH:MM:SS
                long duration = (game.getDateSaved() - game.getDateStarted()) / 1000;
                entries.add(String.format("%02d:%02d:%02d", duration / 3600, (duration % 3600) / 60, duration % 60));
                // num players
                entries.add(Integer.toString(game.getPlayerScores().size()));
                // num rounds
                entries.add(Integer.toString(
                        CollectionUtil.max(game.getPlayerScores(), Functions.PLAYER_SCORE_TO_HISTORY_SIZE)));
                // game name
                entries.add(game.getName());
                
                Function<PlayerScore,String> playerScoreToName = new Function<PlayerScore, String>(){

                    @Override
                    public String apply(PlayerScore obj) {
                        return obj.toDisplayName(context);
                    }
                };
                
                // player(s) with max
                entries.add(TextUtils.join(", ", CollectionUtil.transform(
                        CollectionUtil.maxWithTies(game.getPlayerScores(), Functions.PLAYER_SCORE_TO_SCORE),
                        playerScoreToName)));
                // player(s) with min
                entries.add(TextUtils.join(", ", CollectionUtil.transform(
                        CollectionUtil.minWithTies(game.getPlayerScores(), Functions.PLAYER_SCORE_TO_SCORE),
                        playerScoreToName)));
                
                // rest of columns are just all the player names, so add blank for irrelevant players
                // or the score for the actual players
                Map<String, Long> playerScoreLookup = new HashMap<String, Long>();
                for (PlayerScore playerScore : game.getPlayerScores()) {
                    playerScoreLookup.put(playerScore.toDisplayName(context), playerScore.getScore());
                }

                for (String playerName : playerNames) {
                    Long score = playerScoreLookup.get(playerName);
                    entries.add(score != null ? Long.toString(score) : null);
                }
                
                writer.writeNext(entries.toArray(new String[entries.size()]));
            }
        } catch (IOException e) {
            log.e(e, "unexpected error");
            throw new RuntimeException(e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    log.e(e, "unexpected error");
                    throw new RuntimeException(e);
                }
            }
        }
    }
    
    /**
     * Save the xml in zipped format.
     * 
     * @param filename
     * @param xmlData
     * @return
     */
    public static boolean save(String filename, Format format, Location location, String xmlData) {
        File newFile = new File(getDirectory(location), filename);
        try {
            if (!newFile.exists()) {
                newFile.createNewFile();
            }
        } catch (IOException ex) {
            log.e(ex, "couldn't create new file");
            return false;
        }

        OutputStream out = null;
        Writer writer = null;
        try {

            // specifying BUFFER gets rid of an annoying warning message in the logs
            out = new BufferedOutputStream(new FileOutputStream(newFile, true), BUFFER);
            if (format == Format.GZIP) {
                out = new GZIPOutputStream(out);
            }
            writer = new OutputStreamWriter(out, "UTF-8");
            writer.write(xmlData);
        } catch (FileNotFoundException ex) {
            log.e(ex, "unexpected exception");
            return false;
        } catch (UnsupportedEncodingException ex) {
            log.e(ex, "unexpected exception");
            return false;
        } catch (IOException ex) {
            log.e(ex, "unexpected exception");
            return false;
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException ex) {
                log.e(ex, "unexpected exception");
                return false;                
            }
        }
        return true;
    }
    
    public static String createSpreadsheetFilename() {
        return createFilename("spreadsheet-", ".csv");
    }
    
    public static String createBackupFilename(Format format) {
        return createFilename("games-", (format == Format.GZIP ? ".xml.gz" : ".xml"));
    }
    
    /**
     * Create a simple filename from the current date.
     * @param prefix
     * @param suffix
     * @return
     */
    private static String createFilename(String prefix, String suffix) {
        Date date = new Date();
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);

        DecimalFormat twoDigitDecimalFormat = new DecimalFormat("00");
        DecimalFormat fourDigitDecimalFormat = new DecimalFormat("0000");

        String year = fourDigitDecimalFormat.format(calendar.get(Calendar.YEAR));
        String month = twoDigitDecimalFormat.format(calendar.get(Calendar.MONTH) + 1);
        String day = twoDigitDecimalFormat.format(calendar.get(Calendar.DAY_OF_MONTH));
        String hour = twoDigitDecimalFormat.format(calendar.get(Calendar.HOUR_OF_DAY));
        String minute = twoDigitDecimalFormat.format(calendar.get(Calendar.MINUTE));
        String second = twoDigitDecimalFormat.format(calendar.get(Calendar.SECOND));

        StringBuilder stringBuilder = new StringBuilder(prefix);

        stringBuilder.append(year).append(month).append(day).append(hour).append(minute).append(second);

        stringBuilder.append(suffix);

        return stringBuilder.toString();
    }

    private static File getOrCreateDir(File parent, String name) {
        File file = new File(parent, name);
        if (!file.exists()) {
            file.mkdir();
        }
        return file;
    }

    public static File getFile(String shortFilename, Location location) {
        return new File(getDirectory(location), shortFilename);
    }
}
