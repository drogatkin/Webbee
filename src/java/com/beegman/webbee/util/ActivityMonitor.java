/* **********************************************************************
 * WebBee Copyright 2009 Dmitriy Rogatkin. All rights reserved.
 * Web application building blocks library. 
 *************************************************************************/
package com.beegman.webbee.util;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionActivationListener;
import javax.servlet.http.HttpSessionEvent;

public class ActivityMonitor implements ServletRequestListener, ServletContextListener, HttpSessionActivationListener {
	static final public String STORED_ATTR_NAME = "activity monitor";

	@Override
	public void requestDestroyed(ServletRequestEvent sre) {
		HttpSession sess = ((HttpServletRequest) sre.getServletRequest()).getSession(false);
if (sess != null) {
	
}
	}

	@Override
	public void requestInitialized(ServletRequestEvent sre) {
	}

	@Override
	public void contextDestroyed(ServletContextEvent ce) {
		// TODO Auto-generated method stub

	}

	@Override
	public void contextInitialized(ServletContextEvent ce) {
		ce.getServletContext().setAttribute(STORED_ATTR_NAME, this);

	}

	@Override
	public void sessionDidActivate(HttpSessionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sessionWillPassivate(HttpSessionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}
