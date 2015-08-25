/* **********************************************************************
 * WebBee Copyright 2009 Dmitriy Rogatkin. All rights reserved.
 * Web application building blocks library. 
 *************************************************************************/
package com.beegman.webbee.model;

public class Role {
	enum Access {
		RAED, MODIFY, CREATE, DELETE, PRINT, EXECUTE
	};

	private String name;

	public Role(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}
