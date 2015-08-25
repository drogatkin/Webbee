/* **********************************************************************
 * WebBee Copyright 2009 Dmitriy Rogatkin. All rights reserved.
 * Web application building blocks library. 
 *************************************************************************/
package com.beegman.webbee.base;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.aldan3.servlet.Main;

import com.beegman.webbee.model.AppModel;

public class WebApp extends Main {
	public static final String COMMONDATA_IDENT = "commonlabels";
	
	static BaseBehavior commonBehavior; // keeps app specific common behavior

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		ServletContext sc = getServletContext();
		sc.setAttribute(getServletName() + AppModel.CONFIG_ATTR_SUFFX, getProperties());
		sc.setAttribute(getServletName() + AppModel.FRONTCTRL_ATTR_SUFFX, this);
		AppModel appModel = (AppModel) sc.getAttribute(AppModel.WEBAPP_MODEL);
		if (appModel != null) {
			commonBehavior = appModel.getCommonBehavior(); 
					appModel.notifyStart();
		} else 
			applyDefaultCommonBehavior();
		//System.err.println("ctx attr "+getTemplateEngineAttributeName() );
	}

	@Override
	public String getServletInfo() {
		return "webbee";
	}

	@Override
	public String getSharedDataIdent() {
		return COMMONDATA_IDENT;
	}
	
	protected void applyDefaultCommonBehavior() throws ServletException {
		String className = getProperty("CommonBehaviorClass", null);
		if (className != null) {
			try {
				commonBehavior = (BaseBehavior) Class.forName(className).newInstance();
				return;
			} catch (Exception e) {
				throw new ServletException("problem of instantiate common behavior class", e);
			} 
		}
		commonBehavior = new BaseBehavior(); 
	}
	
}