package com.konstantinisaev.telegram.chart;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtils {

	private static SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM", Locale.getDefault());
	private static Calendar calendar = Calendar.getInstance();

	static {
		calendar.setTimeZone(TimeZone.getDefault());
	}

	public static String formatDate(long date){
		calendar.setTimeInMillis(date);
		Date convertedDate = calendar.getTime();
		return dateFormat.format(convertedDate);
	}
}
