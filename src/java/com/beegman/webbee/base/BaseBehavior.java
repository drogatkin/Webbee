/* **********************************************************************
 * WebBee Copyright 2012 Dmitriy Rogatkin. All rights reserved.
 * Web application building blocks library. 
 *************************************************************************/
package com.beegman.webbee.base;

import org.aldan3.servlet.Constant.CharSet;
import org.aldan3.servlet.Constant.Variable;

import com.beegman.webbee.model.AppModel;
/** This class supposes to define common behavior of  application without using inheritance
 *  It mimics all based methods of <code>baseblock</code>
 *  It implements application Delegate design pattern
 * @author Dmitriy
 *
 */
public class BaseBehavior<T extends AppModel> {
	public BaseBehavior() {
		multiThread = true;
		isPublic = false;
		useLabels = true;
		charset = CharSet.UTF8;
		canCache = false;
		useBreadCrumbs = true;
		ignoreSession = false;
	}

	protected boolean multiThread;
	protected boolean isPublic;
	protected boolean useLabels;
	protected String charset;
	protected boolean canCache;
	protected boolean ignoreSession;
	// prevent error returns to be wrapped to a map when are not maps
	protected boolean noErrorMapWrap;
	
	protected boolean useBreadCrumbs;
	
	protected String unauthorizedView;
	
	public String getTitle(BaseBlock<T> baseBlock) {
		AppModel app = baseBlock.getAppModel();
		if (app != null)
			return (baseBlock.useLabels() ? baseBlock.getResourceString(Variable.PAGE_TITLE, "") : baseBlock.getPreferredServiceName()) +" - "+app.getAppName();
		return AppModel.NAME;
	}
}
