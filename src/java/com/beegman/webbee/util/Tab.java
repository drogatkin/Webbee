/* **********************************************************************
 * WebBee Copyright 2009 Dmitriy Rogatkin. All rights reserved.
 * Web application building blocks library. 
 *************************************************************************/
package com.beegman.webbee.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.ResourceBundle;

public class Tab extends PageRef {
	PageRef[] menu;
	String [] extra;

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
	
	public String[] getExtra() {
		return extra;
	}

	/** Creates tabs component array from localized resource
	 * 
	 * @param resource
	 * @return
	 */
	public static Tab[] createTabsFromResource(ResourceBundle resource, String ... roles) {
		Enumeration<String> ke = resource.getKeys();
		ArrayList<String> keys = new ArrayList<String>(10);
		while(ke.hasMoreElements())
			keys.add(ke.nextElement());
		Collections.sort(keys);
		ArrayList <Tab> tabs = new ArrayList<Tab> (keys.size());
		/*Pattern p = null;
		if (DataConv.hasValue(filterExp))
			p =
		Pattern.compile(filterExp); */
		HashSet<String> rolesSet = null;
		if (roles != null && roles.length > 0) 
			rolesSet = new HashSet<String>(Arrays.asList(roles));
		
		for(String key:keys) {
			String tabComps[] = resource.getString(key).split(",");
			if (tabComps.length > 3) {
				String[] extra = Arrays.copyOfRange(tabComps, 3, tabComps.length);
				boolean isRole = false;
				
				if (rolesSet != null) {
					for(String ro:extra) {
						if (rolesSet.contains(ro)) {
							isRole = true;
							break;
						}
					}
				} else
					isRole = true;
				//System.err.printf("Looking %s in %s%n", Arrays.toString(extra), rolesSet, isRole);
				if (isRole) {
					Tab t =  new Tab(tabComps[0], tabComps[2], tabComps[1]);
					t.extra = extra;
					tabs.add(t);
				}
			} else if (tabComps.length == 0)
				tabs.add( new Tab(key, key) );
			else if (tabComps.length == 1)
				tabs.add( new Tab(tabComps[0], tabComps[0]));
			else if (tabComps.length == 2)
				tabs.add( new Tab(tabComps[0], tabComps[1]));
			else if (tabComps.length == 3)
				tabs.add( new Tab(tabComps[0], tabComps[2], tabComps[1]));
		}
		return tabs.toArray(new Tab[tabs.size()]);
	}
}
