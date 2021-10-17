/* **********************************************************************
 * WebBee Copyright 2009 Dmitriy Rogatkin. All rights reserved.
 * Web application building blocks library. 
 *************************************************************************/
package com.beegman.webbee.block;

import java.util.HashMap;

import com.beegman.webbee.base.BaseBlock;
import com.beegman.webbee.model.AppModel;

public class Cleaner<T, A extends AppModel> extends BaseBlock<A> {

	@Override
	protected Object getModel() {
		HashMap pageMap = new HashMap();
		T cleanerModel = getCleanerModel();
		if (cleanerModel != null)
			pageMap.put(MODEL, cleanerModel);
		return pageMap;
	}

	/** Override this method if some extra data has to be in view for success cleaning
	 * 
	 * @return
	 */
	protected T getCleanerModel() {
		return null;
	}

	@Override
	protected String getCanvasView() {
		return null;
	}

	@Override
	protected boolean useLabels() {
		return false;
	}

	@Override
	protected final Object doControl() {
		throw new IllegalStateException("The Cleaner doesn't provide control ability");
	}

	@Override
	protected final String getSubmitPage() {
		throw new IllegalStateException("The Cleaner doesn't provide control ability");
	}
	
	@Override
	protected void visit() {
	// avoid to log in history
	}

}
