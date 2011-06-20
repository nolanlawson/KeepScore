package com.nolanlawson.keepscore.helper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.nolanlawson.keepscore.util.UtilLogger;

import android.app.Activity;

public class CompatibilityHelper {

	private static UtilLogger log = new UtilLogger(CompatibilityHelper.class);
	
	private static Method overridePendingTransitionMethod;
	private static boolean alreadyChecked;
	
	/**
	 * Calls overridePendingTransition() if this device is running API level 5 or above.  Does nothing
	 * otherwise.
	 * @param activity
	 * @param enterAnim
	 * @param exitAnim
	 */
	public static void overridePendingTransition(Activity activity, int enterAnim, int exitAnim) {
		
		try {
				
			if (!alreadyChecked && overridePendingTransitionMethod == null) {
					// cache the method for small performance gains
					overridePendingTransitionMethod = Activity.class.getMethod(
							"overridePendingTransition", int.class, int.class);
			}
			
			if (overridePendingTransitionMethod != null) {
					overridePendingTransitionMethod.invoke(activity, enterAnim, exitAnim);
			}
				
		} catch (IllegalArgumentException e) {
			log.d(e, "");
		} catch (IllegalAccessException e) {
			log.d(e, "");
		} catch (InvocationTargetException e) {
			log.d(e, "");
		} catch (NoSuchMethodException e) {
			log.d(e, "");
		}
		
		alreadyChecked = true;
	}
}
