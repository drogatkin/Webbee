/* **********************************************************************
 * WebBee Copyright 2009 Dmitriy Rogatkin. All rights reserved.
 * Web application building blocks library. 
 *************************************************************************/
package com.beegman.webbee.util;

import java.util.Locale;
import java.util.TimeZone;

import org.aldan3.data.util.FieldConverter;

public class TimezoneConv implements FieldConverter<TimeZone> {

	@Override
	public TimeZone convert(String id, TimeZone timeZone, Locale locale) {
		try {
			return TimeZone.getTimeZone(id);
		} catch (Exception e) {
		}
		return TimeZone.getDefault();
	}

	@Override
	public String deConvert(TimeZone tz, TimeZone timeZone, Locale locale) {
		if (tz == null)
			return TimeZone.getDefault().getID();
		return tz.getID();
	}

}
