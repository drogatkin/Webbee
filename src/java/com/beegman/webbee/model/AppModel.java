/* **********************************************************************
 * WebBee Copyright 2009 Dmitriy Rogatkin. All rights reserved.
 * Web application building blocks library. 
 *************************************************************************/
package com.beegman.webbee.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.PropertyResourceBundle;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.sql.DataSource;

import org.aldan3.app.Registry;
import org.aldan3.data.DOService;
import org.aldan3.data.util.SimpleField;
import org.aldan3.model.Log;
import org.aldan3.model.TemplateProcessor;
import org.aldan3.servlet.Constant;
import org.aldan3.servlet.FrontController;
import org.aldan3.servlet.Main;
import org.aldan3.util.ResourceException;
import org.aldan3.util.ResourceManager;
import org.aldan3.util.ResourceManager.ResourceType;
import org.aldan3.util.Stream;

import com.beegman.webbee.base.BaseBehavior;
import com.beegman.webbee.base.BaseBlock;
import com.beegman.webbee.util.AsyncUpdater;
import com.beegman.webbee.util.PageRef;
import com.beegman.webbee.util.Tab;

public class AppModel extends Registry implements Serializable,
		ServletContextListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1770689296382588288L;

	public static final String WEBAPP_MODEL = "WEBAPP_MODEL";

	public static final String NAME = "WebBee";

	public static final String CONFIG_ATTR_SUFFX = "_base_config";

	public static final String FRONTCTRL_ATTR_SUFFX = "_front_controller";

	protected ServletContext servletContext;

	protected String datasourceName;

	protected DataSource datasource;

	protected DOService doService;
	
	protected Properties config;

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		deactivateServices();
	}

	@Override
	public void contextInitialized(ServletContextEvent ce) {
		servletContext = ce.getServletContext();
		synchronized (Log.class) {
			Log.l = new Log() {

				@Override
				public void log(String sev, String whe, String msg,
						Throwable ex, Object... params) {
					if (ex == null)
						servletContext.log(String.format("%s[%s]", sev, whe)
								+ String.format(msg, params));
					else
						servletContext.log(String.format("%s[%s]", sev, whe)
								+ String.format(msg, params), ex);
				}
			};
		}
		config = FrontController.loadProperties(servletContext.getInitParameter(Constant.IP_PROPERTIES), servletContext, getClass().getClassLoader());
		datasourceName = servletContext.getInitParameter("model_datasource");
		if (datasourceName != null && datasourceName.length() > 0)
			try {
				Class<?> icclass = Class.forName("javax.naming.InitialContext");

				if (datasourceName.startsWith("java:comp/env/") == false)
					datasourceName = "java:comp/env/" + datasourceName;
				datasource = (DataSource) icclass.getMethod("lookup",
						String.class).invoke(icclass.newInstance(),
						datasourceName);
				doService = createDataService(datasource);
			} catch (Exception e) {
				Log.l.error("Error in locating data source", e);
			}
		else
			Log.l.info(String.format("Application %s doesn't use datasource",
					getAppName()));
		servletContext.setAttribute(WEBAPP_MODEL, this);
		initServices();
	}

	/** used to customize data service to DB vendor specific
	 * 
	 * @param datasource
	 * @return
	 */
	protected DOService createDataService(DataSource datasource) {
		return new DOService(datasource);
	}
	
	/** Used for notification of model that front controller and its configuration
	 * are available
	 */
	public void notifyStart() {
		
	}
	
	/**
	 * put initialization of all app specific services there
	 * 
	 */
	protected void initServices() {
		register(new AsyncUpdater());
	}

	/**
	 * put deactivation services there
	 * 
	 */
	protected void deactivateServices() {
		((AsyncUpdater) getService(AsyncUpdater.NAME)).stop();
	}

	public String getDataSourceName() {
		return datasourceName;
	}

	public Connection getConnection() throws SQLException {
		return datasource.getConnection();
	}

	public Auth getAuth() {
		Auth auth = new Auth(this) {
			public String getName() {
				return "user";
			}

			@Override
			public String getPrincipalFieldName() {
				return "login";
			}

			@Override
			public String getUserKeyName() {
				return "id";
			}

			@Override
			public String getFullUserNameFieldName() {
				return getPrincipalFieldName();
			}

		};
		auth.defineField(new SimpleField("id"));
		auth.defineField(new SimpleField("login"));
		auth.defineField(new SimpleField("password"));
		return auth;
	}

	public DOService getDOService() {
		return doService;
	}

	/**
	 * returns name of application, the method must be overridden
	 * 
	 * @return application name
	 */
	public String getAppName() {
		return NAME;
	}

	/** gets init parameter either for app deployment descriptor or config file
	 * 
	 * @param name
	 * @return
	 */
	public String getInitParameter(String name) {		
		String result = servletContext==null?null:servletContext.getInitParameter(name);
		if (result == null)
			result = getBaseConfig().getProperty(name);
		return result;
	}

	/** gets config properties either directly, or from context attribute
	 * 
	 * @return
	 */
	public Properties getBaseConfig() {
		if (config != null)
			return config;
		return (Properties) servletContext.getAttribute(getServletName()
				+ CONFIG_ATTR_SUFFX);
	}

	public InputStream getResourceAsStream(String path) {
		return servletContext.getResourceAsStream(path);
	}

	public ResourceManager getResourceManager(ResourceType type) {
		return (ResourceManager) servletContext.getAttribute(Main
				.getResourceId(getServletName(), type));
	}

	public PropertyResourceBundle getTextResource(String resPath, Object requester) {
		try {
			return (PropertyResourceBundle)getResourceManager(ResourceManager.RESOURCE_RES).getResource(resPath, requester);
		} catch(ResourceException re) {
			Log.l.error("Error in locating resource "+resPath+" by "+requester, re);
			return null;
		}
	}	
	public TemplateProcessor getTemplateProcessor() {
		//System.err.println("Retrieving:"+getServletName()
			//	+ '_' + Constant.ATTR_DEF_TEMPLATE_PROC);
		return (TemplateProcessor) servletContext.getAttribute(getServletName()
				+ '_' + Constant.ATTR_DEF_TEMPLATE_PROC);
	}

	public File getAttachmentHome() {
		return new File(getBaseConfig().getProperty(
				BaseBlock.CONFIG_ATTACH_HOME, System.getProperty("user.home", ".")+"/."+getAppName()+"/attach"));
	}

	public Tab[] getTabs(BaseBlock<? extends AppModel> baseBlock) {
		try {
			return Tab.createTabsFromResource(baseBlock
					.getNamedResource("navigationtabs"));
		} catch (ResourceException e) {
			return new Tab[0];
		}
	}

	public PageRef[] getTopBarLinks(BaseBlock<? extends AppModel> baseBlock) {
		try {
			return PageRef.createPageRefsFromResource(baseBlock
					.getNamedResource("fastaccessbuttons"));
		} catch (ResourceException e) {
			return new PageRef[0];
		}
	}

	/**
	 * defines connection between model and particular aldan3 framework base
	 * servlet
	 * 
	 * @return servlet name has to be matched to name used in deployment
	 *         descriptor web.xml
	 */
	protected String getServletName() {
		return "webbee";
	}

	public BaseBehavior getCommonBehavior() {
		return new BaseBehavior ();
	}
	
	public <T> T inject(T obj) {
		return obj;
	}
	
	public Properties fillConfigProperties(String configName) {
		Properties result = new Properties();

		InputStream is = null;
		try {
			try {
				is = getResourceAsStream(getInitParameter(configName));
			} catch (Exception e) {
				Log.l.error("Can't read config resource: " + getInitParameter(configName), e);
			}
			if (is == null)
				is = new FileInputStream(getInitParameter(configName));
			result.load(is);
		} catch (Exception e) {
			Log.l.error("Can't read config file: " + getInitParameter(configName), e);
		} finally {
			Stream.close(is);
		}
		return result;
	}

}