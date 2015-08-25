/* **********************************************************************
 * WebBee Copyright 2009 Dmitriy Rogatkin. All rights reserved.
 * Web application building blocks library. 
 *************************************************************************/
package com.beegman.webbee.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.Set;

public class Tab extends PageRef {
	PageRef[] menu;

	public Tab(String service, String help) {
		anchor = service;
		title = anchor;
		this.description = help;
	}

	public Tab(String service, String help, String title) {
		anchor = service;
		this.description = help;
		this.title = title;
	}

	/** Creates tabs component array from localized resource
	 * 
	 * @param resource
	 * @return
	 */
	public static Tab[] createTabsFronResource(ResourceBundle resource) {
		Enumeration<String> ke = resource.getKeys();
		ArrayList<String> keys = new ArrayList<String>(10);
		while(ke.hasMoreElements())
			keys.add(ke.nextElement());
		Collections.sort(keys);
		String[] tabEntries = new String[keys.size()];
		keys.toArray(tabEntries);

		Tab[] result = new Tab[tabEntries.length];
		for (int i = 0; i < tabEntries.length; i++) {
			String tabEntry = resource.getString(tabEntries[i]);
			String tabComps[] = tabEntry.split(",");
			if (tabComps.length == 0)
				result[i] = new Tab(tabEntry, tabEntry);
			else if (tabComps.length == 1)
				result[i] = new Tab(tabComps[0], tabComps[0]);
			else if (tabComps.length == 2)
				result[i] = new Tab(tabComps[0], tabComps[1]);
			else if (tabComps.length == 3)
				result[i] = new Tab(tabComps[0], tabComps[1], tabComps[1]);
		}
		return result;
	}
}
