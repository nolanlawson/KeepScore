package com.nolanlawson.keepscore.helper;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
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
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Environment;

import com.nolanlawson.keepscore.util.UtilLogger;

public class SdcardHelper {

    private static final int BUFFER = 0x1000; // 4K

    private static final String ROOT_DIR = "keepscore";
    
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
     * @author nolan
     *
     */
    public static enum Location {
        Backups("backups"), 
        Shares("shares"),
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

    public static File getSavedBackupDir(Location location) {
        File rootDir = getOrCreateDir(Environment.getExternalStorageDirectory(), ROOT_DIR);
        return getOrCreateDir(rootDir, location.getDirectoryName());
    }
    
    public static boolean backupExists(String filename, Location location) {
        File file = new File(getSavedBackupDir(location), filename);
        return file.exists();
    }

    public static List<String> list(Location location) {
        return Arrays.asList(getSavedBackupDir(location).list());
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
     * Save the xml in zipped format.
     * 
     * @param filename
     * @param xmlData
     * @return
     */
    public static boolean save(String filename, Format format, Location location, String xmlData) {
        File newFile = new File(getSavedBackupDir(location), filename);
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

    public static String createBackupFilename(Format format) {
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

        StringBuilder stringBuilder = new StringBuilder("games-");

        stringBuilder.append(year).append(month).append(day).append(hour).append(minute).append(second);

        stringBuilder.append(".xml");
        if (format == Format.GZIP) {
            stringBuilder.append(".gz");
        }

        return stringBuilder.toString();
    }

    private static File getOrCreateDir(File parent, String name) {
        File file = new File(parent, name);
        if (!file.exists()) {
            file.mkdir();
        }
        return file;
    }

    public static File getBackupFile(String backup, Location location) {
        return new File(getSavedBackupDir(location), backup);
    }
}
