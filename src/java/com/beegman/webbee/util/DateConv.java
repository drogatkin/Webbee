/* **********************************************************************
 * WebBee Copyright 2009 Dmitriy Rogatkin. All rights reserved.
 * Web application building blocks library. 
 *************************************************************************/
package com.beegman.webbee.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.aldan3.data.util.FieldConverter;

public class DateConv implements FieldConverter<Date> {

	public static final String DATE_FMT = "MM/dd/yyyy";

	public static final String TIME_FMT = "hh:mm a";

	@Override
	public Date convert(String _date, TimeZone timeZone, Locale locale) {
		SimpleDateFormat sdf = locale != null ? new SimpleDateFormat(DATE_FMT + " " + TIME_FMT, locale)
				: new SimpleDateFormat(DATE_FMT + " " + TIME_FMT);
		if (timeZone != null)
			sdf.setTimeZone(timeZone);
		try {
			return sdf.parse(_date);
		} catch (Exception e) {
			// trying short
			sdf = new SimpleDateFormat(DATE_FMT);
			if (timeZone != null)
				sdf.setTimeZone(timeZone);			
			try {
				return sdf.parse(_date);
			} catch (ParseException e1) {
			}
		}
		return null;
	}

	@Override
	public String deConvert(Date _date, TimeZone timeZone, Locale locale) {
		if (_date == null)
			return "";
		SimpleDateFormat sdf = locale != null ? new SimpleDateFormat(DATE_FMT, locale)
				: new SimpleDateFormat(DATE_FMT);
		if (timeZone != null)
			sdf.setTimeZone(timeZone);
		return sdf.format(_date);
	}

}
