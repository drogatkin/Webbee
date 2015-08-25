/* **********************************************************************
 * WebBee Copyright 2009 Dmitriy Rogatkin. All rights reserved.
 * Web application building blocks library. 
 *************************************************************************/
package com.beegman.webbee.block;

import java.util.HashMap;

import com.beegman.webbee.base.BaseBlock;
import com.beegman.webbee.model.AppModel;
import com.beegman.webbee.util.PageInfo;

// TODO add head type
public class Tabular<T, A extends AppModel> extends BaseBlock<A> {
	
	
	@Override
	protected Object doControl() {
		return null;
	}

	@Override
	protected Object getModel() {
		HashMap pageModel = new HashMap(10);
		PageInfo pageInfo = getPagination ();
		int ps = -1;
		int pos = 0;
		if (pageInfo != null) { //Pagination 
			pageInfo.page = getParameterValue(pageInfo.id, 0, 0);
			ps = pageInfo.pageSize;
			pageModel.put(PAGINATION, pageInfo);
			pos = pageInfo.page*ps;
		}
		pageModel.put(MODEL, getTabularData(pos, ps));
		pageModel.put(HEAD, getHead());
		return pageModel;
	}

	protected Object getHead() {
		return null;
	}

	@Override
	protected String getSubmitPage() {
		return null;
	}
	
	/** provides actual tabular data for rendering
	 * Override this method
	 * @return
	 */
	protected T getTabularData(long pos, int size) {
		return null;
	}

	/** provides pagination tracking structure
	 * 
	 * @return
	 */
	protected PageInfo getPagination () {
		return null;
	}

}
