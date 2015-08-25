/* **********************************************************************
 * WebBee Copyright 2009 Dmitriy Rogatkin. All rights reserved.
 * Web application building blocks library. 
 *************************************************************************/
package com.beegman.webbee.block;

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;

import com.beegman.webbee.base.BaseBlock;

public class Signoff extends BaseBlock {

	@Override
	protected Object doControl() {
		throw new IllegalStateException("No control for sign off");
	}

	@Override
	protected Object getModel() {
		setAllowed(false);
		Cookie c = getFirstCookie(Signon.COOKIE_LOGIN_TOKEN);
		if (c != null) {  // forget signon
			c.setValue("");
			c.setMaxAge(0);
			resp.addCookie(c);
		}
		String ar = autoRedirect();
		if (ar != null) {
			try {
				redirect(req, resp, ar + TARGET_PAGE + "=" + URLEncoder.encode(getParameterValue(TARGET_PAGE, "", 0), CharSet.UTF8));
			} catch (IOException e) {
			}
			return null;
		}
		return req.getParameterMap();
	}

	@Override
	protected String getSubmitPage() {
		return null;
	}

	@Override
	protected void setAllowed(boolean allowed) {
		super.setAllowed(allowed);
		HttpSession session = req.getSession(false);
		if (session != null)
			session.invalidate();
	}

	@Override
	public boolean isPublic() {
		return true;
	}

	protected String autoRedirect() {
		return null;
	}
}
