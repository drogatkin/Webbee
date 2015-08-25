/* **********************************************************************
 * WebBee Copyright 2009 Dmitriy Rogatkin. All rights reserved.
 * Web application building blocks library. 
 *************************************************************************/
package com.beegman.webbee.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;

import org.aldan3.annot.OptionMap;
import org.aldan3.data.DODelegator;
import org.aldan3.data.util.FieldFiller;
import org.aldan3.model.Coordinator;
import org.aldan3.model.DataObject;
import org.aldan3.model.Log;
import org.aldan3.util.ResourceException;
import org.aldan3.util.ResourceManager;

import com.beegman.webbee.model.AppModel;
import com.beegman.webbee.util.Option;

@OptionMap(valueMap = "id")
public class GenericResourceOptions implements FieldFiller<Collection<DataObject>, Coordinator> {

	@Override
	public Collection<DataObject> fill(Coordinator coord, String filter) {
		// TODO provide user locale from threadlocal
		try {
			PropertyResourceBundle rb = (PropertyResourceBundle) ((AppModel) coord.getModel(null)).getResourceManager(
					ResourceManager.RESOURCE_RES).getResource(getResourcePath(), null); // TODO get Locale from threadlocal
			String key = getResourceKey(coord, filter);
			// if (rb.containsKey(key)) for 1.6 and up only
			try {
				String[] options = rb.getString(key).split(",");
				ArrayList<DataObject> list = new ArrayList<DataObject>();
				boolean stringValue = useStringValue();
				try {
					if (stringValue == false) {
						Class<?> t = coord.getClass().getField(filter).getType();
						stringValue = t.isAssignableFrom(Number.class) == false;
					}
				} catch (NoSuchFieldException nsf) {

				}
				if (stringValue)
					for (int i = 0; i < options.length;) { 
						list.add(new DODelegator(new Option<String, String>(options[i++].trim(), options[i++].replace(
								"comma", ","))));
					}
				else
					for (int i = 0; i < options.length;) {
						list.add(new DODelegator(new Option<Integer, String>(new Integer(options[i++].trim()),
								options[i++].replace("comma", ","))));
					}
				return list;
			} catch (MissingResourceException mre) {
				Log.l.error("Can't find list : " + key, mre);
			}
		} catch (ResourceException re) {
			Log.l.error("Can't find resource:" + getResourcePath(), re);
		}
		return null;
	}

	protected boolean useStringValue() {
		return false;
	}

	public String getResourceKey(Coordinator coord, String filter) {
		return filter;
	}

	protected String getResourcePath() {
		return "data/listslabels";
	}
}
