/* **********************************************************************
 * WebBee Copyright 2009 Dmitriy Rogatkin. All rights reserved.
 * Web application building blocks library. 
 *************************************************************************/
package com.beegman.webbee.util;

import org.aldan3.annot.DBField;

public class Option <V,L>{
	public Option(V code, L name) {
		id = code;
		label = name;
	}

	public Option(V code, L name, String help) {
		this(code, name);
		description = help;
	}

	@DBField
	public V id;

	@DBField
	public L label;
	
	@DBField
	public String description;
}
