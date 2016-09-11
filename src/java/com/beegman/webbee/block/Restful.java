/* **********************************************************************
 * WebBee Copyright 2012 Dmitriy Rogatkin. All rights reserved.
 * Web application building blocks library. 
 *************************************************************************/
package com.beegman.webbee.block;

import java.util.HashMap;

import org.aldan3.annot.RequiresOverride;

import com.beegman.webbee.base.BaseBlock;
import com.beegman.webbee.model.AppModel;

public class Restful<I, O, A extends AppModel> extends BaseBlock<A> {

	public enum restful_op {
		Create, Read, Update, Delete
	};

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
		else if ("DELETE".equals(m))
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
		throw new UnsupportedOperationException("Here is no controller for RESTFul services");
	}

	@Override
	protected Object getModel() {
		Object result = null;
		switch (op) {
		case Update:
			result = storeModel(null);
		case Read:
			result = loadModel(null);
		case Create:
			result = storeModel(readModel());
		case Delete:
			result = deleteModel(null);
		default:
			break;
		}
		if (noTemplate()) {
			return result;
		}
		HashMap<String, Object> pageModel = new HashMap<String, Object>(10);
		pageModel.put(MODEL, result);
		return pageModel;
	}

	protected I newModel() {
		return null;
	}
	
	@RequiresOverride
	protected O loadModel(I in) {
		// TODO Auto-generated method stub
		return null;
	}

	@RequiresOverride
	protected O storeModel(I in) {

		return null;
	}

	@RequiresOverride
	protected I readModel() {

		return null;
	}

	@Override
	protected String getSubmitPage() {
		return navigation;
	}

	@RequiresOverride
	protected O deleteModel(I in) {
		return null;
	}

	protected String getKeyParameterName() {
		return "id";
	}
}
