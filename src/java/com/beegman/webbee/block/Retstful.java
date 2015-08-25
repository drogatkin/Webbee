/* **********************************************************************
 * WebBee Copyright 2012 Dmitriy Rogatkin. All rights reserved.
 * Web application building blocks library. 
 *************************************************************************/
package com.beegman.webbee.block;

import java.util.HashMap;

import com.beegman.webbee.base.BaseBlock;
import com.beegman.webbee.model.AppModel;

public class Retstful<M, A extends AppModel> extends BaseBlock <A>{
	
	public enum restful_op  {Create, Read, Update, Delete};

	transient protected restful_op op;
	
	transient protected String key;
	transient protected String source;
	transient protected String object;
	transient int returnCode;
	
	@Override
	protected void start() {
		super.start();
		String m = req.getMethod().toUpperCase();
		// TODO override with possible operation specified as parameter
		if ("GET".equals(m))
			op = restful_op.Read;
		else if ("POST".equals(m))
			op = restful_op.Update;
		else if ("PUT".equals(m))
			op = restful_op.Create;
		else if ("DELET".equals(m))
			op = restful_op.Delete;
		String restReq = req.getPathInfo();
		if (restReq != null && restReq.length() > 0) {
			String[] reqParams = restReq.split("/");
			
		}
	}

	@Override
	protected boolean useLabels() {
		return false;
	}
	
	@Override
	protected String getCanvasView() {
		return null;
	}

	@Override
	protected Object doControl() {
		throw new UnsupportedOperationException("Here is no controller for RESTFul services") ;
	}

	@Override
	protected Object getModel() {
		HashMap pageModel = new HashMap(10);
		
		return pageModel;
	}

	@Override
	protected String getSubmitPage() {
		return navigation;
	}

	protected void delete() {
		
	}
	
	protected int getControlType() {
		return 0;
	}
}
