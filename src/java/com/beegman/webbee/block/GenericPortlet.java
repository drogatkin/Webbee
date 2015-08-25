/* **********************************************************************
 * WebBee Copyright 2009 Dmitriy Rogatkin. All rights reserved.
 * Web application building blocks library. 
 *************************************************************************/
package com.beegman.webbee.block;

import com.beegman.webbee.util.PageInfo;
import com.beegman.webbee.util.Portlet;

public class GenericPortlet<T> extends Portlet<T> {
	protected String title;
	protected T model;
	private PageInfo pageIndo;
	
	public GenericPortlet(T m, String t) {
		title = t;
		model = m;
		pageIndo = new PageInfo(20, -1, "pageinfo");
	}
	
	@Override
	public T getModel() {
		return model;
	}
	
	@Override
	public String getTitle() {
		return title;
		
	}

	@Override
	public PageInfo getPagenation() {
		return pageIndo;
	}
}
