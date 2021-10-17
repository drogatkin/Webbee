/* **********************************************************************
 * WebBee Copyright 2012 Dmitriy Rogatkin. All rights reserved.
 * Web application building blocks library. 
 *************************************************************************/
package com.beegman.webbee.base;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.aldan3.data.util.SimpleField;
import org.aldan3.servlet.Constant.CharSet;
import org.aldan3.servlet.Constant.Request;
import org.aldan3.servlet.Constant.Variable;

import com.beegman.webbee.model.AppModel;
import com.beegman.webbee.model.UserInfo;

/**
 * This class supposes to define common behavior of application without using
 * inheritance It mimics all based methods of <code>baseblock</code> It
 * implements application Delegate design pattern
 * 
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
		isGetAllowed = true;
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
	
	protected boolean isGetAllowed;

	protected String unauthorizedView;

	public String getTitle(BaseBlock<T> baseBlock) {
		AppModel app = baseBlock.getAppModel();
		if (app != null)
			return (baseBlock.useLabels() ? baseBlock.getResourceString(Variable.PAGE_TITLE, "")
					: baseBlock.getPreferredServiceName()) + " - " + app.getAppName();
		return AppModel.NAME;
	}

	public UserInfo getUserInfo(HttpServletRequest req) {
		HttpSession s = req.getSession(false);
		UserInfo ui = null;
		if (s != null) {
			ui = (UserInfo) s.getAttribute(BaseBlock.SESS_USER_INFO);
			if (ui == null) {
				Object uid = s.getAttribute(BaseBlock.SESS_USER_ID);
				if (uid != null) {
					ui = createUserInfo();
					ui.modifyField(BaseBlock.SESS_USER_ID, uid);
					ui.modifyField(UserInfo.ROLE, s.getAttribute("role"));
					s.setAttribute(BaseBlock.SESS_USER_INFO, ui);
				}
			}
			if (ui != null)
				return ui;
		}
		ui = (UserInfo) req.getAttribute(Request.AUTHETICATE_REQ);
		if (ui != null)
			return ui;
		Principal p = req.getUserPrincipal();

		if (p != null && p.getName() != null && !p.getName().isEmpty()) {
			ui = createUserInfo();
			ui.modifyField(BaseBlock.SESS_USER_NAME, p.getName());
		}
		return ui;
	}

	public boolean isSigned(HttpServletRequest req) {
		return getUserInfo(req) != null;
	}

	protected UserInfo createUserInfo() {
		UserInfo result = new UserInfo();
		result.defineField(new SimpleField(BaseBlock.SESS_USER_ID));
		result.defineField(new SimpleField(BaseBlock.SESS_USER_NAME));
		result.defineField(new SimpleField(UserInfo.ROLE));
		return result;
	}
	
	public boolean isMobileApp(HttpServletRequest req) {
		return false;
	}
	
}
