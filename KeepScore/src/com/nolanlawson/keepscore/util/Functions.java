package com.nolanlawson.keepscore.util;

import java.util.Date;
import java.util.GregorianCalendar;

import com.nolanlawson.keepscore.db.PlayerScore;
import com.nolanlawson.keepscore.util.CollectionUtil.Function;

public class Functions {

	public static final Function<PlayerScore, Integer> PLAYER_SCORE_TO_HISTORY_SIZE = 	
			new Function<PlayerScore, Integer>(){

				@Override
				public Integer apply(PlayerScore obj) {
					return obj.getHistory().size();
				}
	};
	
	public static final Function<Integer, Integer> INTEGER_TO_LENGTH_WITH_SIGN = 
			new Function<Integer, Integer>(){

		@Override
		public Integer apply(Integer obj) {
			return IntegerUtil.toStringWithSign(obj).length();
		}
	};

	public static final Function<Date, Date> TODAY_START = new Function<Date, Date>() {

		@Override
		public Date apply(Date currentDate) {
			return getClosestMidnightBefore(currentDate, 0);
		}
	};
	
	public static final Function<Date, Date> YESTERDAY_START = new Function<Date, Date>() {
		
		@Override
		public Date apply(Date currentDate) {
			return getClosestMidnightBefore(currentDate, 1000L * 60 * 60 * 24);
		}
	};
	
	public static final Function<Date, Date> DAY_BEFORE_YESTERDAY_START = new Function<Date, Date>() {
		
		@Override
		public Date apply(Date currentDate) {
			return getClosestMidnightBefore(currentDate, 1000L * 60 * 60 * 24 * 2);
		}
	};
	
	public static final Function<Date, Date> ONE_WEEK_AGO_START = new Function<Date, Date>() {
		
		@Override
		public Date apply(Date currentDate) {
			return getClosestMidnightBefore(currentDate, 1000L * 60 * 60 * 24 * 7);
		}
	};
	
	public static final Function<Date, Date> ONE_MONTH_AGO_START = new Function<Date, Date>() {
		
		@Override
		public Date apply(Date currentDate) {
			return getClosestMidnightBefore(currentDate, 1000L * 60 * 60 * 24 * 30);
		}
	};
	
	public static final Function<Date, Date> ONE_YEAR_AGO_START = new Function<Date, Date>() {
		
		@Override
		public Date apply(Date currentDate) {
			return getClosestMidnightBefore(currentDate, 1000L * 60 * 60 * 24 * 365);
		}
	};
	
	public static final Function<Date, Date> NOW = new Function<Date, Date>() {
		
		@Override
		public Date apply(Date currentDate) {
			Date date = new Date();
			date.setTime(currentDate.getTime());
			return date;
		}
	};
	
	public static final Function<Date, Date> THE_EPOCH = new Function<Date, Date>() {
		
		@Override
		public Date apply(Date currentDate) {
			return new Date(0L);
		}
	};
	
	private static Date getClosestMidnightBefore(Date date, long duration) {
		
		GregorianCalendar newDate = new GregorianCalendar();
		Date dateMinusDuration = new Date(date.getTime() - duration);
		newDate.setTime(dateMinusDuration);
		
		newDate.set(GregorianCalendar.HOUR_OF_DAY, 0);
		newDate.set(GregorianCalendar.MINUTE, 0);
		newDate.set(GregorianCalendar.SECOND, 0);
		newDate.set(GregorianCalendar.MILLISECOND, 0);		
		
		return newDate.getTime();
	}
}
