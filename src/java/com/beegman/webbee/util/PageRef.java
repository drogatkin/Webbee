/* **********************************************************************
 * WebBee Copyright 2009 Dmitriy Rogatkin. All rights reserved.
 * Web application building blocks library. 
 *************************************************************************/
package com.beegman.webbee.util;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.aldan3.servlet.Constant;

public class PageRef implements Serializable {
	protected URL url, referer;

	protected String title;

	protected String description;

	protected String query;

	protected String anchor;

	public static PageRef create(HttpServletRequest req, String title, String anchor) {
		PageRef result = new PageRef();
		String referer = req.getHeader(Constant.Variable.REFERER);
		if (referer != null)
			try {
				result.setReferer(new URL(referer));
			} catch (MalformedURLException e) {
			}
		result.setTitle(title);
		String qs;
		result.setQuery(qs = req.getQueryString());
		try {
			if (qs == null)
				result.setUrl(new URL(req.getRequestURL().toString()));
			else {
				result.setUrl(new URL(req.getRequestURL().append('?').append(qs).toString()));
			}
		} catch (MalformedURLException e) {
		}
		
		result.setAnchor(anchor);
		//result.setDescription(description);
		return result;
	}
	
	public static PageRef create(String anchor, String title, String description) {
		PageRef result = new PageRef();
		result.setAnchor(anchor);
		result.setTitle(title);
		result.setDescription(description);
		return result;
	}
	
	public final URL getUrl() {
		return url;
	}

	public final void setUrl(URL url) {
		this.url = url;
	}

	public final URL getReferer() {
		return referer;
	}

	public final void setReferer(URL referer) {
		this.referer = referer;
	}

	public final String getTitle() {
		return title;
	}

	public final void setTitle(String title) {
		this.title = title;
	}

	public final String getDescription() {
		return description;
	}

	public final void setDescription(String description) {
		this.description = description;
	}

	public final String getQuery() {
		return query;
	}

	public final void setQuery(String query) {
		this.query = query;
	}

	public final String getAnchor() {
		return anchor;
	}

	public final void setAnchor(String anchor) {
		this.anchor = anchor;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof PageRef == false)
			return false;
		PageRef pr = (PageRef)obj;
		if (anchor != null)
			return (anchor+query).equals(pr.getAnchor()+pr.getQuery());
		URL url = getUrl();
		if (url != null)
			return url.equals(pr.getUrl()) ;
		return false;
	}

	@Override
	public int hashCode() {
		URL url = getUrl();
		if (anchor == null)
			return url==null?"".hashCode():getUrl().hashCode();
		return anchor.hashCode()^(""+query).hashCode();
	}

	@Override
	public String toString() {
		if (anchor == null)
			return getUrl().toString();
		
		return anchor+(query==null?"":"?"+query);
	}
	
	/** Creates navigation buttons component array from localized resource
	 * 
	 * @param resource
	 * @return
	 */
	public static PageRef[] createPageRefsFromResource(ResourceBundle resource) {
		// TODO try to find generic code for tabs and buttons
		Enumeration<String> keys =resource.getKeys();
		ArrayList<String> keysarray = new ArrayList<String>();
		while(keys.hasMoreElements())
			keysarray.add(keys.nextElement());

		String[] tabEntries = new String[keysarray.size()];
		keysarray.toArray(tabEntries);
		Arrays.sort(tabEntries);
		PageRef[] result = new PageRef[tabEntries.length];
		for (int i = 0; i < tabEntries.length; i++) {
			String tabEntry = resource.getString(tabEntries[i]);
			String tabComps[] = tabEntry.split(",");
			if (tabComps.length == 0)
				result[i] = create(tabEntry, tabEntry, tabEntry);
			else if (tabComps.length == 1)
				result[i] = create(tabComps[0], tabComps[0], tabComps[0]);
			else if (tabComps.length == 2)
				result[i] = create(tabComps[0], tabComps[1], tabComps[1]);
			else if (tabComps.length == 3)
				result[i] = create(tabComps[0], tabComps[1], tabComps[2]);
		}
		return result;
	}
	
	public static String appendParamSeparator(String path) {
		int qi = path.indexOf('?');
		if (qi >= 0) {
			if (qi < path.length() - 1) {
				if (!path.endsWith("&"))
					path += '&';
			}
		} else
			path += '?';
		return path;
	}
}
