/* **********************************************************************
 * WebBee Copyright 2010 Dmitriy Rogatkin. All rights reserved.
 * Web application building blocks library. 
 *************************************************************************/
package com.beegman.webbee.util;

import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;

import org.aldan3.util.ResourceException;
import org.aldan3.util.ResourceManager;

import com.beegman.webbee.model.AppModel;

public class ufa {
	public final static StringBuffer add(String name, String provider, Object model) {
		StringBuffer result = new StringBuffer(4096);
		return result;
	}

	public final static StringBuffer label(Integer value, String key, AppModel model) {		
		return label("" + value, key, model);	
	}

	public final static StringBuffer label(String value, String key, AppModel model) {
		StringBuffer result = new StringBuffer(4096);
		PropertyResourceBundle rb;
		try {
			rb = (PropertyResourceBundle) model.getResourceManager(ResourceManager.RESOURCE_RES).getResource(
					"data/listslabels", null); // TODO get Locale from threadlocal
			
			// if (rb.containsKey(key)) not  work for 1.5
			try {
				String[] options = rb.getString(key).split(",");
				for (int i = 0; i < options.length;) {
					if (options[i].equals(value)) {
						result.append(options[i + 1]);
						break;
					}
					i += 2;
				}
			} catch (MissingResourceException mre) {
				throw new ResourceException("Key:"+key+" not found");
			}
		} catch (ResourceException e) {
			result.append(e);
		}
		return result;
	}
}
