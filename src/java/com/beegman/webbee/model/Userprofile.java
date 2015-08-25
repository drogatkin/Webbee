/* **********************************************************************
 * WebBee Copyright 2009 Dmitriy Rogatkin. All rights reserved.
 * Web application building blocks library. 
 *************************************************************************/
package com.beegman.webbee.model;

import org.aldan3.annot.DBField;
import org.aldan3.annot.FormField;

import com.beegman.webbee.util.SimpleCoordinator;

public class Userprofile extends SimpleCoordinator<AppModel> {
	public Userprofile(AppModel model) {
		super(model);
	}

	@FormField
	@DBField
	public String login;

	@FormField
	@DBField
	public String givenName;

	@FormField
	@DBField
	public String surName;

	@FormField
	@DBField
	public String email;

}