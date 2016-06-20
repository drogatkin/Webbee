/* **********************************************************************
 * WebBee Copyright 2009 Dmitriy Rogatkin. All rights reserved.
 * Web application building blocks library. 
 *************************************************************************/
package com.beegman.webbee.block;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;

import org.aldan3.annot.FormField;
import org.aldan3.model.Log;
import org.aldan3.servlet.Constant;
import org.aldan3.util.inet.Base64Codecs;

import com.beegman.webbee.base.BaseBlock;
import com.beegman.webbee.model.AppModel;
import com.beegman.webbee.model.Auth;
import com.beegman.webbee.util.PageRef;

public class Signon<A extends AppModel> extends BaseBlock<A> {
	public static final String COOKIE_NAME = "user_name";

	public static final String COOKIE_LOGIN_TOKEN = "token";

	public static final String VAR_LOGIN = "login_v";

	boolean useSSO;

	boolean jumpLastHistory;

	@Override
	protected String getContentType(String name) {
		if (callAsAjaxOnly())
			return "application/json; charset=utf-8";
		return super.getContentType(name);
	}
	
	public Map processsignonDisplayCall() {
		//log("referer:"+req.getHeader("REFERER"), null);
		HashMap model = new HashMap();
		SignonFlags flags = new SignonFlags();
		String v = getFirstCookieValue(COOKIE_NAME);

		if (v != null) {
			flags.remember = true;
			model.put(VAR_LOGIN, Base64Codecs.base64Decode(v, Base64Codecs.UTF_8));
		} else {
			v = getParameterValue(VAR_LOGIN, null, 0);
			if (v != null)
				model.put(VAR_LOGIN, v);
		}

		v = getFirstCookieValue(COOKIE_LOGIN_TOKEN);
		flags.keep = v != null;
		model.put(MODEL, flags);
		model.put(TARGET_PAGE, getParameterValue(TARGET_PAGE, "", 0));
		String message = (String) req.getAttribute(MESSAGE);
		if (message != null)
			model.put(Variable.ERROR, getResourceString(message, message));
		return model;
	}

	@Override
	protected String getViewName() {
		return getsignonDisplayViewName();
	}

	public String getsignonDisplayViewName() {
		return super.getViewName();
	}

	@Override
	protected String getCanvasView() {
		if (callAsAjaxOnly())
			return null;
		return super.getCanvasView();
	}
	
	@Override
	protected void visit() {
	}

	@Override
	protected String getSubmitPage() {
		return getParameterValue(TARGET_PAGE, "Portal", 0);
	}

	@Override
	protected Object doControl() {
		if (isSigned()) {
			if (callAsAjaxOnly())
				return processsignonDisplayCall();
			return null;
		}
		AppModel appModel = getAppModel();
		Auth auth = appModel.getAuth();
		fill(auth);
		if (eligable()) {
			auth.authenticate();
			if (auth.getPrincipal() != null) {
				this.setAllowed(true); // assure session
				if (initSession(auth)) {
					manageFlags();
					if (callAsAjaxOnly())
						return processsignonDisplayCall();
					return null;
				}
			}
			logFail(auth);
		}
		Map model = processsignonDisplayCall();
		model.put(VAR_LOGIN, auth.get(auth.getPrincipalFieldName())); // TODO use resource
		model.put(Variable.ERROR, getResourceString("failed_login", String.format(getResourceString("err_signon",
				"%s can't sign you in. Possibly login and password provided not matching"), appModel
				.getAppName())));
		return model;
	}

	protected void manageFlags() {
		SignonFlags flags = new SignonFlags();
		fillModel(flags);
		Cookie c = getFirstCookie(COOKIE_NAME);
		String ui = getSessionAttribute(SESS_USER_ID, (Object) null).toString();
		if (flags.remember) {
			if (c == null || ui.equals(Base64Codecs.base64Decode(c.getValue(), Base64Codecs.UTF_8)) == false) {
				c = new Cookie(COOKIE_NAME, Base64Codecs.base64Encode(ui, Base64Codecs.UTF_8));
				c.setMaxAge(rememberLoginDuration());
			} else
				c = null;
		} else if (c != null) {
			c.setValue("");
			c.setMaxAge(0);
		}
		if (c != null)
			resp.addCookie(c);
		c = getFirstCookie(COOKIE_LOGIN_TOKEN);
		if (flags.keep) {
			if (c == null) {
				SavedUser su = new SavedUser();
				su.login = ui;
				su.expired = System.currentTimeMillis() + keepSignedDuration() * 1000;
				c = new Cookie(COOKIE_LOGIN_TOKEN, Base64Codecs.base64Encode(makeToken(su.serialize()),
						Base64Codecs.UTF_8));
				c.setMaxAge(keepSignedDuration());
			}
		} else if (c != null) {
			c.setValue("");
			c.setMaxAge(0);
		}
		if (c != null)
			resp.addCookie(c);

	}

	/** Implement own way to encrypt token
	 * 
	 * @param token
	 * @return encrypted token
	 */
	protected String makeToken(String token) {
		return token;
	}

	/** Decrypt token
	 * 
	 * @param token
	 * @return decrypted token
	 */
	protected String restoreToken(String token) {
		return token;
	}

	protected int rememberLoginDuration() {
		// TODO config
		return 60 * 60 * 24 * 30;
	}

	protected int keepSignedDuration() {
		return rememberLoginDuration();
	}
	
	protected boolean callAsAjaxOnly() {
		return false;
	}

	/** This methods allows to check brutal login discover attempts and bring CAPCHA
	 *  or completely block access
	 * @return
	 */
	protected boolean eligable() {
		return true;
	}

	protected void logFail(Auth auth) {
		// get failed map, identify attempt put count

	}

	protected boolean initSession(Auth auth) {
		HttpSession session = req.getSession(false);
		if (session == null) {
			Log.l.error("Session isn't created yet", new IllegalStateException("Session isn't created yet"));
			return false;
		}
		//throw new IllegalStateException("Session isn't created yet");
		session.setAttribute(SESS_USER_NAME, auth.get(auth.getUserKeyName()));
		session.setAttribute(SESS_USER_ID, auth.getPrincipal());
		session.setAttribute(Variable.PAGE_TITLE, auth.get(auth.getFullUserNameFieldName()));
		return true;
	}

	@Override
	protected Object getModel() {
		if (isSigned()) {
			PageRef pr = popHistory();
			try {
				redirect(req, resp, pr != null ? pr.getUrl().toString() : getSubmitPage());
				return null;
			} catch (IOException e) {
			}
		}

		return processsignonDisplayCall();
	}

	@Override
	public boolean isPublic() {
		return true;
	}

	@Override
	protected boolean isSigned() {
		boolean result = super.isSigned();
		log("Signed:" + result, null);
		if (result)
			return result;
		persistentSignon();
		return super.isSigned();
	}

	protected void persistentSignon() {
		Cookie c = getFirstCookie(COOKIE_LOGIN_TOKEN);
		if (c != null) {
			final SavedUser su = SavedUser.deserialize(restoreToken(Base64Codecs.base64Decode(c.getValue(),
					Base64Codecs.UTF_8)));
			log("user " + su, null);
			if (su != null && su.expired < System.currentTimeMillis()) {
				Auth auth = getAppModel().getAuth();
				auth.modifyField(auth.getUserKeyName(), su.login);
				setAllowed(true);
				if (initSession(auth)) {
					c = null;
				}
			}
		}

		if (c != null) {
			c.setValue("");
			c.setMaxAge(0);
			resp.addCookie(c);
		}
	}

	@Override
	protected String getPerspective() {
		return "insert/";
	}

	public static class SignonFlags {
		@FormField
		public boolean keep;

		@FormField
		public boolean remember;
	}

	public static class SavedUser {
		public static SavedUser deserialize(String savedUser) {
			if (savedUser == null)
				return null;
			int lcp = savedUser.lastIndexOf(',');
			if (lcp < 0 || lcp == savedUser.length() - 1)
				return null;
			SavedUser result = new SavedUser();
			result.login = savedUser.substring(0, lcp);
			result.expired = Long.parseLong(savedUser.substring(lcp + 1));
			return result;
		}

		String login;

		long expired;

		public String serialize() {
			return login + ',' + expired;
		}

		public String toString() {
			return "" + login + ", expired " + new Date(expired);
		}
	}
}