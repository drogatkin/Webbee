/* **********************************************************************
 * WebBee Copyright 2009 Dmitriy Rogatkin. All rights reserved.
 * Web application building blocks library. 
 *************************************************************************/
package com.beegman.webbee.util;

public class pagenation {
	public static final String NBSP = "&nbsp;";

	public static int LEFT_GRIP = 3;

	public static int RIGHT_GRIP = 7;

	public static final StringBuffer pagenation(PageInfo pi) {

		StringBuffer result = new StringBuffer(256);
		if (pi != null) {

			makeJSCallRef(result, "gotoPage(0,'" + pi.id + "')", "&laquo;").append(NBSP);
			//result.append(NBSP);
			if (pi.page > 0)
				makeJSCallRef(result, "gotoPage("+(pi.page-1)+",'" + pi.id + "')", "&lsaquo;").append(NBSP);
			if (pi.page >= 0) {
				int s = pi.page - LEFT_GRIP;
				if (s < 0)
					s = 0;
				for (int i = s; i < pi.page; i++)
					makeJSCallRef(result, "gotoPage("+(i)+",'" + pi.id + "')",Integer.toString(i + 1)).append(NBSP);
				result.append("<strong>").append(pi.page + 1).append("</strong>").append(NBSP);
				if (pi.pages > 0) {
					for (int i = pi.page + 1; i < s + RIGHT_GRIP; i++)
						makeJSCallRef(result, "gotoPage("+(i)+",'" + pi.id + "')",Integer.toString(i + 1)).append(NBSP);
				}
				result.append("....");
				makeJSCallRef(result, "gotoPage("+(pi.page+1)+",'" + pi.id + "')","&rsaquo;").append(NBSP);
				if (pi.pages > 0) {
					makeJSCallRef(result, "gotoPage("+pi.pages+",'" + pi.id + "')","&raquo;").append(NBSP);
				}
			}
		}
		return result;
	}

	public static StringBuffer makeJSCallRef(StringBuffer buf, String procCall, String Label) {
		return buf.append("<a href=\"javascript:void()\" onclick=\"").append(procCall).append("\">").append(Label)
				.append("</a>");
	}
}
