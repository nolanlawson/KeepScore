package com.nolanlawson.keepscore.helper;

import java.io.File;

import android.os.Environment;

/**
 * Utils for saving to /sdcard/keepscore.
 * 
 * Why /sdcard/keepscore?  Because the stuff introduced in Froyo for a dedicated sdcard app directory
 * gets deleted when 
 * apps are erased, which is totally useless.
 * @author nolan
 *
 */
public class ExternalStorageHelper {

	private static final String ROOT_DIR = "keepscore";
	private static final String SETTING_SET_DIR = "settings";
	
	public static boolean isSdcardAccessible() {
		return Environment.getExternalStorageDirectory() != null;
	}
	
	public static File getSettingSetDir() {
		getOrCreateDir(ROOT_DIR);
		return getOrCreateDir(ROOT_DIR + '/' + SETTING_SET_DIR);
	}
	
	private static File getOrCreateDir(String dirname) {
		File keepscoreDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), dirname);
		if (!keepscoreDir.exists()) {
			keepscoreDir.mkdir();
		}
		return keepscoreDir;
	}
	
}
