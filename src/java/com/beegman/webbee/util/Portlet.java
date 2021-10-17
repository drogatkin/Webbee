/* **********************************************************************
 * WebBee Copyright 2009 Dmitriy Rogatkin. All rights reserved.
 * Web application building blocks library. 
 *************************************************************************/
package com.beegman.webbee.util;

import org.aldan3.model.ServiceProvider;

import com.beegman.webbee.base.BaseBlock;

public class Portlet<T> implements ServiceProvider {
public static final String PORTLET = "portlet";
public static final String PORTLET_ID = "portletid";
public static final String PORTLET_TEMPLATE = "portlet_template";
	public T getModel() {
		return null;
	}
	
	public T loadModel(T model) {
		return model;
	}
	
	public String getTitle() {
		return "";
	}
	
	public String getView() {
		return "portlet/"+getPreferredServiceName()+BaseBlock.VIEW_EXT;
	}
	
	public PageInfo getPagenation() {
		return null;
	}

	protected long firstRow() {
		PageInfo pageInfo = getPagenation();
		if (pageInfo != null)
			return pageInfo.page*pageInfo.size;
		return 0;
	}
	
	protected int pageSize() {
		PageInfo pageInfo = getPagenation();
		if (pageInfo != null)
			return pageInfo.pageSize;
		return -1;
	}
	
	@Override
	public String getPreferredServiceName() {
		if (name == null)
			synchronized (this) {
				if (name == null) {
					name = getClass().getName();
					int p = name.lastIndexOf('.');
					if (p > 0)
						name =  name.substring(p + 1);
					name = name.toLowerCase();
				}
			}
		return name;
	}

	@Override
	public Object getServiceProvider() {
		return this;
	}
	
	private String name;
}
