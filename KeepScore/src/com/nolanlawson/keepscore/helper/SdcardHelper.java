package com.nolanlawson.keepscore.helper;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import android.os.Environment;

import com.nolanlawson.keepscore.util.UtilLogger;

public class SdcardHelper {

	private static final int BUFFER = 0x1000; // 4K
	
	private static final String ROOT_DIR = "keepscore";
	private static final String BACKUPS_DIR = "backups";
	
	private static UtilLogger log = new UtilLogger(SdcardHelper.class);
	
	public static boolean isAvailable() {
		return Environment.getExternalStorageDirectory() != null 
				&& Environment.getExternalStorageDirectory().exists();
	}
	
	public static File getBackupDir() {
		File rootDir = getOrCreateDir(Environment.getExternalStorageDirectory(), ROOT_DIR);
		return getOrCreateDir(rootDir, BACKUPS_DIR);
	}
	
	public static boolean backupExists(String filename) {
		File file = new File(getBackupDir(), filename);
		return file.exists();
	}
	
	public static List<String> list() {
		return Arrays.asList(getBackupDir().list());
	}
	
	public static String open(String filename) {
		File logFile = new File(getBackupDir(), filename);	
		
		StringBuilder stringBuilder = new StringBuilder();
		BufferedReader bufferedReader = null;
		
		try {
			
			bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(logFile)), BUFFER);
			
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
	
	public static boolean save(String filename, String xmlData) {
		File newFile = new File(getBackupDir(), filename);
		try {
			if (!newFile.exists()) {
				newFile.createNewFile();
			}
		} catch (IOException ex) {
			log.e(ex, "couldn't create new file");
			return false;
		}
		
		PrintStream out = null;
		try {
			// specifying BUFFER gets rid of an annoying warning message
			out = new PrintStream(new BufferedOutputStream(new FileOutputStream(newFile, true), BUFFER));
			
			out.print(xmlData);
		} catch (FileNotFoundException ex) {
			log.e(ex,"unexpected exception");
			return false;
		} finally {
			if (out != null) {
				out.close();
			}
		}
		return true;
	}
	
	public static String createBackupFilename() {
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
		
		stringBuilder.append(year).append("-").append(month).append("-")
				.append(day).append("-").append(hour).append("-")
				.append(minute).append("-").append(second);
		
		stringBuilder.append(".xml");
		
		return stringBuilder.toString();
	}
	
	

	private static File getOrCreateDir(File parent, String name) {
		File file = new File(parent, name);
		if (!file.exists()) {
			file.mkdir();
		}
		return file;
	}

	public static File getBackupFile(String backup) {
		return new File(getBackupDir(), backup);
	}
	
}
