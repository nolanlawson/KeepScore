package com.nolanlawson.keepscore.util;

public class LongUtil {
    
    /**
     * Borrowed from Google Guava!
     * 
     * Compares the two specified {@code long} values. The sign of the value
     * returned is the same as that of {@code ((Long) a).compareTo(b)}.
     *
     * @param a the first {@code long} to compare
     * @param b the second {@code long} to compare
     * @return a negative value if {@code a} is less than {@code b}; a positive
     *     value if {@code a} is greater than {@code b}; or zero if they are equal
     */
    public static int compare(long a, long b) {
      return (a < b) ? -1 : ((a > b) ? 1 : 0);
    }
    
}
