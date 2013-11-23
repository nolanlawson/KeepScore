package com.nolanlawson.keepscore.util;

public class TimeUtil {

    /**
     * Format seconds into a string like this: 01:01, 02:02, etc. (i.e. h:mm:ss)
     * @param time
     * @return
     */
    public static String formatSeconds(int time) {
        
        long hours = Math.round(Math.floor(time * 1.0/ 3600));
        
        String minutesStr = String.valueOf(Math.round(Math.floor(((time * 1.0/ 60) % 60))));
        String secondsStr = String.valueOf(time % 60);
        
        StringBuilder result = new StringBuilder();
        
        if (hours > 0) {
            result.append(hours).append(":");
            // pad minutes to 2 digits
            result.append(minutesStr.length() == 1 ? "0" : "");
        }
        
        return result
                .append(minutesStr)
                .append(':')
                // pad seconds to 2 digits
                .append(secondsStr.length() == 1 ? "0" : "")
                .append(secondsStr)
                .toString();
    }
    
}
