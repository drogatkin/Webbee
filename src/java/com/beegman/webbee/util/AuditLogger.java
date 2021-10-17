/* **********************************************************************
 * WebBee Copyright 2011 Dmitriy Rogatkin. All rights reserved.
 * Web application building blocks library. 
 *************************************************************************/
package com.beegman.webbee.util;


import org.aldan3.data.DODelegator;
import org.aldan3.model.ProcessException;
import org.aldan3.model.ServiceProvider;

import com.beegman.webbee.model.AppModel;
import com.beegman.webbee.model.Audit;

public class AuditLogger<A extends AppModel, T extends Audit<A>> implements ServiceProvider<AuditLogger<A, T>> {
	public static final String NAME = "##AuditLogger"; 

	private AppModel appModel;
	
	public AuditLogger(A am) {
		appModel = am;
	}
	
	public void log(long obj, int type, char op, long by) throws ProcessException {
		T audit = (T) new Audit(appModel); // TODO make it factory
		audit.object = obj;
		audit.operation = op;
		audit.by_requester = by;
		audit.type = type;
		appModel.getDOService().addObject(new DODelegator<T>(audit, null, "id,stamp", null));
	}
	
	@Override
	public String getPreferredServiceName() {
		return NAME;
	}

	@Override
	public AuditLogger<A, T> getServiceProvider() {
		return this;
	}

}
