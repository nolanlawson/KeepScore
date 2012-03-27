package com.nolanlawson.keepscore.test;

import java.util.Date;
import java.util.TimeZone;

import android.test.ActivityInstrumentationTestCase2;

import com.nolanlawson.keepscore.MainActivity;
import com.nolanlawson.keepscore.util.CollectionUtil.Function;
import com.nolanlawson.keepscore.util.Functions;

/**
 * Test to make sure that the Date Functions work as expected and return the correct times.
 * @author nolan
 *
 */
public class DateTest extends ActivityInstrumentationTestCase2<MainActivity> {

	public DateTest() {
		super("com.nolanlawson.keepscore", MainActivity.class);
	}
	
	@Override
    public void setUp() throws Exception {
		TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
    }
	
	public void testDates() {
		
		//ref date is Saturday, March 24, 2012 around 7:30 pm GMT
		
		testDate("24 Mar 2012 19:27:21 GMT", Functions.NOW);
		testDate("24 Mar 2012 00:00:00 GMT", Functions.TODAY_START);
		testDate("23 Mar 2012 00:00:00 GMT", Functions.YESTERDAY_START);
		testDate("22 Mar 2012 00:00:00 GMT", Functions.DAY_BEFORE_YESTERDAY_START);
		testDate("17 Mar 2012 00:00:00 GMT", Functions.ONE_WEEK_AGO_START);
		testDate("23 Feb 2012 00:00:00 GMT", Functions.ONE_MONTH_AGO_START);
		testDate("25 Mar 2011 00:00:00 GMT", Functions.ONE_YEAR_AGO_START);
		testDate(new Date(0).toGMTString(), Functions.THE_EPOCH);
		
	}
	
	private void testDate(String expected, Function<Date, Date> function) {
		Date date = function.apply(getReferenceDate());
		System.out.println(date.toGMTString());
		assertEquals(expected, date.toGMTString());		
	}
	
	private Date getReferenceDate() {
		
		// Saturday, March 24, 2012 around 7:30 pm GMT
		Date referenceDate = new Date(1332617241000L);
		System.out.println(referenceDate.toGMTString());
		assertEquals("24 Mar 2012 19:27:21 GMT", referenceDate.toGMTString());
		
		return referenceDate;
	}
}
