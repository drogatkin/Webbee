/* **********************************************************************
 * WebBee Copyright 2009 Dmitriy Rogatkin. All rights reserved.
 * Web application building blocks library. 
 *************************************************************************/
package com.beegman.webbee.base;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.aldan3.model.DataObject;
import org.aldan3.data.util.Filler;
import org.aldan3.model.Field;
import org.aldan3.servlet.BasePageService;
import org.aldan3.servlet.Constant;
import org.aldan3.util.DataConv;
import org.aldan3.util.ResourceException;
import org.aldan3.util.ResourceManager;
import org.aldan3.web.ui.widget;

import com.beegman.webbee.model.AppModel;
import com.beegman.webbee.model.Appearance;
import com.beegman.webbee.model.Config;
import com.beegman.webbee.model.UIEvent;
import com.beegman.webbee.util.AsyncUpdater;
import com.beegman.webbee.util.History;
import com.beegman.webbee.util.PageRef;
import com.beegman.webbee.util.PopupInfo;
import com.beegman.webbee.util.SignonAgent;
import com.beegman.webbee.util.Tab;
import com.beegman.webbee.util.ajax;

public abstract class BaseBlock<T extends AppModel> extends BasePageService {

	// TODO move constants in an interface 
	public static final String VIEW_EXT = ".htmt";

	public static final String MODEL = "model";
	
	public static final String HEAD = "head";

	public static final String MESSAGE = "message";

	public static final String TABS = "tabs";

	public static final String TOPLINKS = "toplinks";

	public static final String UI = "ui";

	public static final String SIGNED = "$$signed";

	public static final String HISTORY = "history";

	public static final String ACTION = "action";

	public static final String APPEARANCE = "appearance";
	
	public static final String POPUP_INFO = "popup_info";

	public static final String TARGET_PAGE = "referer";

	public static final String PAGINATION = "pageinfo";

	public static final String ID = "id";

	public static final String PAGE = "page";

	public static final String AJAX = "ajax";

	public static final String BROWSER_CODE = "ua_code";

	public static final String MOBILE_BRWSR = "mobile";

	public static final String MOBILE_PLATFORM = "mobile_platform";

	public static final String PLT_ANDROID = "Android";
	
	public static final String PLT_BLACKBERRY = "Blackberry";

	public static final String PLT_IOS = "iOS";

	public static final String APP_NAME = "app_name";

	public static final String SERVICE_NAME = "pageservicename";

	public static final String CONFIG_CANVAS = "canvas";

	public static final String CONFIG_PUBLIC_CANVAS = "public_canvas";

	public static final String CONFIG_VIEWEXT = "viewext";

	public static final String CONFIG_PUBLIC_HOME = "public_home";

	public static final String CONFIG_ATTACH_HOME = "attach_home";
	
	public static final String CONFIG_MOBILLE_SUPPORT = "mobile_support";

	public static final String CONFIG_PERSPECTIVE_SEPARATOR = "perspective_separator";

	public static final String SESS_USER_ID = "wb#userid";
    
        public static final String SESS_USER_NAME = "wb#username";

	protected Config configCache;

	protected Appearance appearance; // TODO change to Appearance[] appearances;

	protected String navigation;
	
	protected Map modelMerge; // used for merging with final model
	
	protected String userAgent; // for caching value

	public T getAppModel() {
		return (T) frontController.getAttribute(AppModel.WEBAPP_MODEL);
	}

	protected SignonAgent getSignonAgent() {
		return (SignonAgent) frontController.getAttribute(Config.SIGNON_AGENT);
	}

	// // Note ////
	// History support works correctly in assumption that only one browser
	// window
	// is opened. History links will be messed if a user use more than one
	// window in
	// the same session scope

	/**
	 * returns last visited link
	 * 
	 * @return last visited link reference or null of no history was created or
	 *         no links in history
	 */
	protected PageRef popHistory() {
		return popHistory(1);
	}

	/**
	 * returns entire navigation history cached in session
	 * 
	 * @return History object
	 */
	protected History getHistory() {
		HttpSession session = req.getSession(false);
		if (session != null)
			return (History) session.getAttribute(History.NAME);
		return null;
	}

	/**
	 * Gives access to navigation history cached in session
	 * 
	 * @param back
	 *            how many steps back in history 0 - current, 1 - previous and
	 *            so o n
	 * @return PageRef of page or null if doesn't exist
	 */
	protected PageRef popHistory(int back) {
		HttpSession session = req.getSession(false);
		if (session == null)
			return null; // do not maintain of history for non signed
		History history = (History) session.getAttribute(History.NAME);
		if (history == null)
			return null;
		return history.pop(back);
	}

	/**
	 * Gets current perspective of the page
	 * 
	 * @return String name of perspective
	 */
	protected String getPerspective() {
		// TODO use appearances
		if (appearance == null || appearance == Appearance.full)
			return null;
		return appearance.name() + getConfigValue(CONFIG_PERSPECTIVE_SEPARATOR, "");
	}

	protected boolean isAppearance(Appearance a) {
		return a.equals(appearance);
	}
	
	/**
	 * Puts record in navigation history cached in session
	 * 
	 */
	protected void visit() {
		if (WebApp.commonBehavior.useBreadCrumbs == false)
			return;
		HttpSession session = req.getSession(false);
		if (session == null)
			return; // do not maintain of history for non signed
		if (forwarded == false && isAppearance(Appearance.popup) == false) {
			History history = (History) session.getAttribute(History.NAME);
			if (history == null) {
				synchronized (session) {
					history = (History) session.getAttribute(History.NAME);
					if (history == null) {
						history = new History(); // define deepness
						session.setAttribute(History.NAME, history);
					}
				}
			}
			
			history.push(PageRef.create(req,
					useLabels() ? getResourceString(Variable.PAGE_TITLE, getPreferredServiceName())
							: getPreferredServiceName(), getPreferredServiceName()));
		}
	}

	@Override
	protected void start() {
		super.start();
		// currently hack, until BasePageService obtains a better method to reset
		// service state
		navigation = null;
		userAgent = req.getHeader(HTTP.USER_AGENT);
		// Mozilla/5.0 (iPod; CPU iPhone OS 5_0 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.0.2 Mobile/9A5248d Safari/6533.18.5
		// Mozilla/5.0 (Linux; U; Android 2.3.4; en-us; Nexus One Build/GRJ22) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1
		//
		// TODO force mobile mode from session
		if ("yes".equals(getConfigValue(CONFIG_MOBILLE_SUPPORT, null)) && userAgent != null
				&& (userAgent.indexOf("Mobile") > 0 || userAgent.indexOf("RIM Tablet OS") > 0 || userAgent.indexOf("Silk") > 0) && userAgent.indexOf("(KHTML, like Gecko)") > 0 || forceMobile()) {
			appearance = Appearance.mobile; // TODO check for Appearance.tablet
		} else
			appearance = null;
		//appearance = Appearance.mobile;
		modelMerge = null;
	}

	@Override
	protected boolean isAllowed(boolean override) throws ServletException {
		if (super.isAllowed(override))
			return true;
		// log("IsAllowed("+override+") = false, so sa called", null);
		SignonAgent sa = getSignonAgent();
		return sa != null && sa.signon(req, getAppModel());
	}

	@Override
	protected String getUnauthorizedPage() {
		String page = getConfigValue(CONFIG_PUBLIC_HOME, WebApp.commonBehavior.unauthorizedView);
		if (page == null || page.length() == 0) {
			try {
				page = new URL(req.getScheme(), req.getServerName(), req.getServerPort(), req.getContextPath()
						+ "/").toString();
			} catch (MalformedURLException e) {
				log("", e);
			}
		}
		try {
			return page
					+ "?"
					+ TARGET_PAGE
					+ "="
					+ URLEncoder.encode(req.getRequestURL().append('?').append(req.getQueryString()==null?"":req.getQueryString()).toString(),
							CharSet.UTF8);
		} catch (UnsupportedEncodingException e) {
		}
		return page;
	}

	@Override
	protected Object applySideEffects(Object modelData) {
		if (userAgent != null) {
			if (modelData != null && modelData instanceof Map) {
				// user-agent code
				Map mapModel = (Map) modelData;
				if (userAgent.indexOf("Firefox") > 0)
					mapModel.put(BROWSER_CODE, "_ff");
				else if (userAgent.indexOf("Chrome") > 0)
					mapModel.put(BROWSER_CODE, "_ch");
				else if (userAgent.indexOf("Safari") > 0)
					mapModel.put(BROWSER_CODE, "_sf");
				if (appearance == Appearance.mobile) { // Appearance.mobile.equals(appearance)
					mapModel.put(MOBILE_BRWSR, MOBILE_BRWSR);
					if (userAgent.indexOf("iPod") > 0 || userAgent.indexOf("iPhone") > 0)
						mapModel.put(MOBILE_PLATFORM, PLT_IOS);
					else if (userAgent.indexOf("Android") > 0 || userAgent.indexOf("Silk") > 0)
						mapModel.put(MOBILE_PLATFORM, PLT_ANDROID);
					else if (userAgent.indexOf("RIM") > 0)
						mapModel.put(MOBILE_PLATFORM, PLT_BLACKBERRY);
				}
			}
		}
		return super.applySideEffects(modelData);
	}

	@Override
	protected void addEnv(Object model, boolean ajaxView) {
		if (!ajaxView)
			visit();
		super.addEnv(model, ajaxView);
		if (model != null && model instanceof Map) { //
			Map mapModel = (Map) model;
			try {
				mapModel.put(WebApp.COMMONDATA_IDENT,
						getResourceManager(ResourceManager.RESOURCE_RES).getResource(WebApp.COMMONDATA_IDENT, this));
			} catch (ResourceException e) {
				log("Can't locate shared resource:" + WebApp.COMMONDATA_IDENT, e);
			}
			HttpSession session = req.getSession(false);
			// note session is available in model too, so perhaps no reason to
			// have extra
			if (WebApp.commonBehavior.ignoreSession == false && session != null) {
				mapModel.put(HISTORY, session.getAttribute(History.NAME));
				if (isSigned()) {
					mapModel.put(SIGNED, true);
					req.setAttribute(SIGNED, true);
				}
			} else
				mapModel.remove(Constant.Variable.SESSION);

			// adding widget lib, TODO think just add to properties
			widget w = new widget();
			mapModel.put(UI, w);
			req.setAttribute(UI, w);
			mapModel.put(AJAX, new ajax());
			// if (mapModel.containsKey(SERVICE_NAME) == false) {
			mapModel.put(SERVICE_NAME, getPreferredServiceName());
			req.setAttribute(SERVICE_NAME, getPreferredServiceName());
			// }
			mapModel.put(Variable.PAGE_TITLE, getTitle());
			Locale locale = getLocale();
			if (locale == null)
				locale = Locale.getDefault(); // TODO override from config
			mapModel.put(Variable.LOCALE, locale);
			T app = getAppModel();
			if (app != null) {
				// TODO decide if put app model as well
				mapModel.put(APP_NAME, app.getAppName());
				// TODO where to decide to localize tabs
				Tab[] tabs = app.getTabs(this);
				if (tabs != null)
					mapModel.put(TABS, tabs);
				PageRef[] links = app.getTopBarLinks(this);
				if (tabs != null)
					mapModel.put(TOPLINKS, links);
			} else
				mapModel.put(APP_NAME, getConfigValue(APP_NAME, "Unnamed"));
			mapModel.put(ACTION, getPreferredServiceName());
			if (appearance != null)
				mapModel.put(APPEARANCE, appearance);
			mapModel.put(POPUP_INFO, getPopupInfo());
			addExtraHeader(mapModel, ajaxView);
			// TODO add search and pagination data
			if (modelMerge != null) {
				mapModel.putAll(modelMerge);
				modelMerge = null;
			}
		}
	}

	/**
	 * used for adding extra includes
	 * 
	 * @param mapModel
	 */
	protected void addExtraHeader(Map mapModel, boolean ajaxView) {
		if (ajaxView == false)
			mapModel.put("headextra", "insert/headextra.htmt");
	}

	public void modelInsert(String key, Object value) {
		// currently lazy mechanism without concurrency
		if (modelMerge == null)
			modelMerge = new HashMap<String, Object>();
		modelMerge.put(key,  value);
	}
	
	protected String getTitle() {
		return WebApp.commonBehavior.getTitle(this);
	}

	protected Map reportErrror(Map pageModel, String errorRes, String errorText, Object... messParams) {
		Map result = pageModel == null ? new HashMap(2) : pageModel;
		result.put(Variable.ERROR, String.format(getResourceString(errorRes, errorText), messParams));

		return result;
	}

	protected boolean forceMobile() {
		return false;		
	}
	
	@Override
	protected boolean noTemplate() {
		return WebApp.commonBehavior.noErrorMapWrap;
	}
	
	@Override
	protected String getViewName() {
		return DataConv.ifNull(getPerspective(),"") + getResourceName() + getConfigValue(CONFIG_VIEWEXT, VIEW_EXT);
	}

	@Override
	protected String getCharSet() {
		return WebApp.commonBehavior.charset;
	}

	@Override
	public boolean isThreadFriendly() {
		return WebApp.commonBehavior.multiThread == false;
	}

	@Override
	public boolean isThreadSafe() {
		return WebApp.commonBehavior.multiThread;
	}

	@Override
	protected boolean canCache() {
		return WebApp.commonBehavior.canCache;
	}
	
	@Override
	protected
	boolean useLabels() {
		return WebApp.commonBehavior.useLabels;
	}
	
	@Override
	public String getPreferredServiceName() {
		return getResourceName();
	}
	
	@Override
	public
	boolean isPublic() {
		return WebApp.commonBehavior.isPublic;
	}

	@Override
	public Object getServiceProvider() {
		return this;
	}

	@Override
	protected String getCanvasView() {
		// TODO select canvas by appearance
		String pref = getPerspective();
		if (pref == null)
			return getConfigValue(isPublic() ? CONFIG_PUBLIC_CANVAS : CONFIG_CANVAS, "canvas")
					+ getConfigValue(CONFIG_VIEWEXT, VIEW_EXT);
		return pref + getConfigValue(isPublic() ? CONFIG_PUBLIC_CANVAS : CONFIG_CANVAS, "canvas")
				+ getConfigValue(CONFIG_VIEWEXT, VIEW_EXT);
	}

	/**
	 * Returns config value
	 * 
	 * @param name
	 * @param def
	 * @return
	 */
	protected String getConfigValue(String name, String def) {
		return frontController.getProperty(name, def);
	}

	protected DataObject fill(final DataObject sdo) {
		fillDataObject(sdo, new Filler() {
			@Override
			public void fill(Field f) {
				// log( "Getting:"+f, null);
				String type = f.getType();
				String name = f.getWebId();
				if (name == null || name.isEmpty())
				   name = f.getName();
				sdo.modifyField(f,
						BaseBlock.this.getObjectParameterValue(name, null, 0,
								type == null || type.indexOf("char") >= 0 ? false : true));
			}
		});
		return sdo;
	}

	protected boolean isSigned() {
		try {
			if (isAllowed(false)) {
				HttpSession s = req.getSession(false);
				if (s != null && s.getAttribute(SESS_USER_ID) != null)
					return true;
			}
		} catch (ServletException e) {
		}
		return false;
	}

	public ResourceBundle getNamedResource(String name) throws ResourceException {
		return (ResourceBundle) getResourceManager(ResourceManager.RESOURCE_RES).getResource(name, this);
	}

	public String getFirstCookieValue(String name) {
		Cookie cookie = getFirstCookie(name);
		if (cookie != null)
			return cookie.getValue();
		return null;
	}

	public Cookie getFirstCookie(String name) {
		Cookie[] cookies = req.getCookies();
		if (cookies != null) // can be null?
			for (Cookie c : cookies)
				if (name.equals(c.getName()))
					return c;
		return null;
	}

	public Object processAutosuggestCall() {
		return null;
	}

	public String getAutosuggestViewName() {
		return "autosuggest_json" + getConfigValue(CONFIG_VIEWEXT, VIEW_EXT);
	}

	public Object processCascadingCall() {
		return null;
	}

	public String getCascadingViewName() {
		return "cascading_json" + getConfigValue(CONFIG_VIEWEXT, VIEW_EXT);
	}

	/**
	 * used for pushing events from UI update queue
	 * 
	 * @return null, since actual response will coming asynchronously
	 */
	public Object processAsyncupdateCall() {
		// if nothing in queue, then go async (however check first if dispatched
		// from async),
		// otherwise push updates
		Collection<UIEvent> events = popUIEvents();
		if (events != null && events.isEmpty() == false) {
			HashMap result = new HashMap(2);
			result.put("events", events);
			return result;
		}

		forLate();
		return null;
	}
	
	public String processCompletependingCall() {
		String uiid = getUIID();
		if (uiid != null) {
			AsyncUpdater service = (AsyncUpdater) getAppModel().getService(AsyncUpdater.NAME);
			if (service != null)
				service.dropRequester(uiid);
		}
		return "Ok";
	}

	public String getAsyncupdateViewName() {
		return "asyncupdate_json" + getConfigValue(CONFIG_VIEWEXT, VIEW_EXT);
	}

	protected void pushUIEvent(UIEvent uie) {
		pushUIEvent(uie, getUIID());
	}

	protected void pushUIEvent(UIEvent uie, String uiid) {
		if (uiid == null)
			throw new IllegalStateException();
		AsyncUpdater service = (AsyncUpdater) getAppModel().getService(AsyncUpdater.NAME);
		if (service != null)
			service.addEvent(uiid, uie);
	}

	protected Collection<UIEvent> popUIEvents() {
		return (Collection<UIEvent>) req.getAttribute(AsyncUpdater.ATTR_UI_EVENTS);
	}

	protected void forLate() {
		String uiid = getUIID();
		if (uiid == null) {
			try {
				resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			} catch (IOException e) {
				
			}
			return;
		}
		AsyncContext ac = req.startAsync();
		ac.setTimeout(getAsyncTimeout() * 1000); 
		// ac.addListener(this);
		AsyncUpdater service = (AsyncUpdater) getAppModel().getService(AsyncUpdater.NAME);
		if (service != null)
			service.addRequester(uiid, ac);
	}
	
	/** returns async timeout in seconds
	 * 
	 * @return
	 */
	protected int getAsyncTimeout() { 
		return 10*60;
	}

	/**
	 * retrieves id used for identification of current UI app model can be
	 * involved to find out as well
	 * 
	 * @return UI id or null if can't be identified
	 */
	protected String getUIID() {
		HttpSession session = req.getSession(false);
		if (session != null)
			return session.getId();
		return null;
	}

	/**
	 * override if the service needs popup
	 * 
	 * @return
	 */
	protected PopupInfo getPopupInfo() {
		return null;
	}

	protected void banAccess(int duration) {
		// TODO add IP to banned (blacklist)
	}

	protected boolean isBanned() {
		return false;
	}
}