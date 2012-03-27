package com.nolanlawson.keepscore.data;

import java.util.Date;

import com.nolanlawson.keepscore.R;
import com.nolanlawson.keepscore.util.CollectionUtil.Function;
import com.nolanlawson.keepscore.util.Functions;

/**
 * Basic measures of time to use for organizing games in the LoadGameActivity, 
 * e.g. "Today," "Yesterday," "Last," etc.
 * @author nolan
 *
 */
public enum TimePeriod {

	Today (R.string.title_today, Functions.TODAY_START, Functions.NOW),
	Yesterday (R.string.title_yesterday, Functions.YESTERDAY_START, Functions.TODAY_START),
	DayBeforeYesterday (R.string.title_two_days_ago, Functions.DAY_BEFORE_YESTERDAY_START, Functions.YESTERDAY_START),
	LastWeek (R.string.title_last_week, Functions.ONE_WEEK_AGO_START, Functions.DAY_BEFORE_YESTERDAY_START),
	LastMonth (R.string.title_last_month, Functions.ONE_MONTH_AGO_START, Functions.ONE_WEEK_AGO_START),
	LastYear (R.string.title_last_year, Functions.ONE_YEAR_AGO_START, Functions.ONE_MONTH_AGO_START),
	Older (R.string.title_older, Functions.THE_EPOCH, Functions.ONE_YEAR_AGO_START),
	;
	
	private int titleResId;
	private Function<Date, Date> startDateFunction;
	private Function<Date, Date> endDateFunction;
	
	private TimePeriod(int titleResId, Function<Date, Date> startDateFunction,
			Function<Date, Date> endDateFunction) {
		this.titleResId = titleResId;
		this.startDateFunction = startDateFunction;
		this.endDateFunction = endDateFunction;
	}

	public int getTitleResId() {
		return titleResId;
	}

	public Function<Date, Date> getStartDateFunction() {
		return startDateFunction;
	}

	public Function<Date, Date> getEndDateFunction() {
		return endDateFunction;
	}
	
	
}
