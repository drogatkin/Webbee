/* **********************************************************************
 * WebBee Copyright 2010 Dmitriy Rogatkin. All rights reserved.
 * Web application building blocks library. 
 *************************************************************************/
package com.beegman.webbee.util;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionEvent;

/** This class is provided for debug purpose only
 * 
 * @author Dmitriy
 *
 */
public class SessionLogger implements HttpSessionAttributeListener, HttpSessionListener {

	@Override
	public void attributeAdded(HttpSessionBindingEvent sessionBindingEvent) {
		//  Get the session
		HttpSession session = sessionBindingEvent.getSession();
		// Log some information
		System.out.println("[SessionAttr] "+new java.util.Date()+
		" Attribute added, session "+session+": "
		+sessionBindingEvent.getName()+"="+
		sessionBindingEvent.getValue());
	}

	@Override
	public void attributeRemoved(HttpSessionBindingEvent sessionBindingEvent) {
		// Get the session
		HttpSession session = sessionBindingEvent.getSession();
		System.out.println(new java.util.Date()+" Attribute removed, session "+session+": "+sessionBindingEvent.getName());
	}

	@Override
	public void attributeReplaced(HttpSessionBindingEvent sessionBindingEvent) {
		// Get the session
		HttpSession session = sessionBindingEvent.getSession();
		// Log some information
		System.out.println(new java.util.Date()+" Attribute	replaced, session "+session+": "+sessionBindingEvent
		.getName()+"="+sessionBindingEvent.getValue());
	}
	
	@Override
	public void 	sessionCreated(HttpSessionEvent se) {
		new Exception("Session "+se.getSession().getId()+" has  been created.").printStackTrace(System.out);
	}
	
	@Override
	public void 	sessionDestroyed(HttpSessionEvent se) {
		new Exception("Session "+se.getSession().getId()+" has  been destroyed.").printStackTrace(System.out);
	}

}
