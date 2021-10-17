/* **********************************************************************
 * WebBee Copyright 2009 Dmitriy Rogatkin. All rights reserved.
 * Web application building blocks library. 
 *************************************************************************/
package com.beegman.webbee.util;

import java.io.Serializable;

public class PageInfo implements Serializable {
	
	/** Initializes page info object
	 * 
	 * @param ps page size
	 * @param s total size of array
	 * @param i id of page navigation
	 */
	public PageInfo(int ps, long s, String i) {
		id = i;
		pageSize = ps;
		size = s;
		pages = size/pageSize +(size%pageSize>0?1:0);
	}
	
	public int page;

	public long pages;

	public long size;
	
	public int pageSize;
	
	public String id;

}
