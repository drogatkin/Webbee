package com.beegman.webbee.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.aldan3.app.Env;

public class JSONDateUtil {
	SimpleDateFormat JSONISO_8601_FMT = Env.getJavaVersion() > 7?new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.ENGLISH):
		new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZ", Locale.ENGLISH);

	public Date parse(String jds) throws ParseException {
		if (jds == null || jds.isEmpty())
			return null;
		return JSONISO_8601_FMT.parse(jds);
	}

	public String toJSON(Date date) {
		if(date == null)
			return "";
		return JSONISO_8601_FMT.format(date);
	}
}
