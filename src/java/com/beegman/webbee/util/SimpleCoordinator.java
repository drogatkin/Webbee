/* **********************************************************************
 * WebBee Copyright 2009 Dmitriy Rogatkin. All rights reserved.
 * Web application building blocks library. 
 *************************************************************************/
package com.beegman.webbee.util;

import com.beegman.webbee.model.AppModel;
import org.aldan3.model.Coordinator;

public class SimpleCoordinator<T extends AppModel> implements Coordinator {

	private T appModel;
	
	public SimpleCoordinator(T model) {
		appModel = model;
	}
	
	@Override
	public Object getModel(String name) {
		return appModel;
	}

	@Override
	public Object getService(String name) {
		if (DOSERVICE.equals(name))
			return appModel.getDOService();
		return appModel.getService(name);
	}

}
