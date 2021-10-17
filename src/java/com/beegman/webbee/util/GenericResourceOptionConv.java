package com.beegman.webbee.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.TimeZone;

import org.aldan3.annot.FormField;
import org.aldan3.data.util.FieldConverter;
import org.aldan3.model.Log;
import org.aldan3.util.ResourceException;
import org.aldan3.util.ResourceManager;

import com.beegman.webbee.model.AppModel;

public class GenericResourceOptionConv implements FieldConverter<Object> {

	protected ArrayList<Option<String, String>> options;

	public GenericResourceOptionConv() {
		options = new ArrayList<Option<String, String>>();
	}

	public GenericResourceOptionConv(AppModel appModel, Field field) {
		PropertyResourceBundle rb;
		try {
			rb = (PropertyResourceBundle) appModel.getResourceManager(ResourceManager.RESOURCE_RES).getResource(
					getResourcePath(), null);
			FormField ff = field.getAnnotation(FormField.class);
			String key = field.getName();
			if (ff != null) {
				if (ff.queryResultMap().length > 0)
					key = ff.queryResultMap()[0];
			}

			// if (rb.containsKey(key)) for 1.6 and up
			// TODO get actual form field name not field name
			try {
				String[] options = rb.getString(key).split(",");
				this.options = new ArrayList<Option<String, String>>(options.length / 2);
				for (int i = 0; i < options.length;) {
					this.options
							.add(new Option<String, String>(options[i++].trim(), options[i++].replace("comma", ",")));
				}
			} catch (MissingResourceException mre) {
				Log.l.error("Can't find list : " + key, mre);
			}
		} catch (ResourceException e) {
			Log.l.error("Can't get resource : " + getResourcePath(), e);
		}
	}

	@Override
	public Object convert(String val, TimeZone tz, Locale lc) {
		for (Option<String, String> o : options) {
			// System.err.printf("CHecking %s vs %s is %b%n", o.label, val,
			// o.label.equals(val));
			if (o.label.equals(val))
				return (String) o.id;
		}
		// System.err.printf("coverting %s%n", val);
		return val;
	}

	@Override
	public String deConvert(Object value, TimeZone tz, Locale lc) {
		if (value != null)
			value = value.toString();
		for (Option<String, String> o : options) {
			// System.err.printf("Converting %s vs %s is %b%n", o.id, value,
			// o.id.equals(value));
			if (o.id.equals(value))
				return (String) o.label;
		}
		return null;
	}

	protected String getResourcePath() {
		return "data/listslabels";
	}
}
