/* **********************************************************************
 * WebBee Copyright 2009 Dmitriy Rogatkin. All rights reserved.
 * Web application building blocks library. 
 *************************************************************************/
package com.beegman.webbee.model;

import java.util.HashSet;
import java.util.Set;

import org.aldan3.data.SimpleDataObject;
import org.aldan3.model.ProcessException;
import org.aldan3.model.Log;

/** This class keeps user credentials and provides authentication against
 * used user repository/directory, policy server, smart card and so on. 
 * 
 * @author Dmitriy
 *
 */
public abstract class Auth extends SimpleDataObject {
	private AppModel appModel;

	protected HashSet<Role> roles;

	protected String principle;

	protected Auth() {

	}

	public Auth(AppModel appModel) {
		this.appModel = appModel;
	}

	public void authenticate() {
		principle = null;
		try {
			if (appModel.getDOService().getObjectLike(this) != null)
				principle = (String) get(getPrincipalFieldName());
			//else
				//System.err.println("No auth found for "+this);
		} catch (ProcessException pe) {
			Log.l.error("", pe);
		}
	}

	public String getPrincipal() {
		return principle;
	}

	public Set<Role> getRoles() {
		return roles;
	}

	public abstract String getPrincipalFieldName();

	public abstract String getUserKeyName();
	
	public abstract String getFullUserNameFieldName();
}
