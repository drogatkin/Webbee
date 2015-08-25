/* **********************************************************************
 * WebBee Copyright 2009 Dmitriy Rogatkin. All rights reserved.
 * Web application building blocks library. 
 *************************************************************************/
package com.beegman.webbee.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateTimeConv extends DateConv {
	@Override
	public String deConvert(Date _date, TimeZone timeZone, Locale locale) {
		if (_date == null)
			return "";
		SimpleDateFormat sdf = locale != null ? new SimpleDateFormat(DATE_FMT + " " + TIME_FMT, locale)
				: new SimpleDateFormat(DATE_FMT + " " + TIME_FMT);
		if (timeZone != null)
			sdf.setTimeZone(timeZone);
		return sdf.format(_date);
	}
}
