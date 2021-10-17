/* **********************************************************************
 * WebBee Copyright 2009 Dmitriy Rogatkin. All rights reserved.
 * Web application building blocks library. 
 *************************************************************************/
package com.beegman.webbee.block;

import java.util.HashMap;

import com.beegman.webbee.base.BaseBlock;
import com.beegman.webbee.model.AppModel;

public class Info<T,A extends AppModel> extends BaseBlock<A> {
	@Override
	final protected Object doControl() {
		throw new IllegalAccessError();
	}

	@Override
	protected Object getModel() {
		HashMap pageModel = new HashMap(10);
		T model;
		pageModel.put(MODEL, model = getInfoModel());
		fillModel(model);
		populateInfo(model);
		return pageModel;
	}

	@Override
	final protected String getSubmitPage() {
		return null;
	}

	@Override
	protected boolean reportValidation(String name, String value, Exception problem) {
		return false;
	}

	/** Override the method for gaining functionality
	 * 
	 * @return
	 */
	protected T getInfoModel() {
		return null;
	}

	/** Fill info from database or other storage
	 * 
	 * @param model
	 */
	protected void populateInfo(T model) {
	}
}
