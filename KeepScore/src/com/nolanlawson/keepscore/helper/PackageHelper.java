package com.nolanlawson.keepscore.helper;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;

import com.nolanlawson.keepscore.util.UtilLogger;

public class PackageHelper {

	private static UtilLogger log = new UtilLogger(PackageHelper.class);
	
	public static String getVersionName(Context context) {
		try {
			return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			// should never happen
			log.d(e, "unexpected exception");
			return "";
		}
	}
}
