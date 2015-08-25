/* **********************************************************************
 * WebBee Copyright 2010 Dmitriy Rogatkin. All rights reserved.
 * Web application building blocks library. 
 *************************************************************************/
package com.beegman.webbee.block;

import java.io.IOException;

import com.beegman.webbee.base.BaseBlock;
import com.beegman.webbee.model.AppModel;

public class Redirect<A extends AppModel> extends BaseBlock<A> {

	@Override
	protected Object doControl() {
		throw new IllegalStateException("The Redirect doesn't provide control ability");
	}

	@Override
	protected Object getModel() {
		try {
			String path = getRedirect();
			if (path == null)
				path = getSubmitPage();
			redirect(req, resp, path);
		} catch (IOException e) {
			log("", e);
		}
		return null;
	}

	@Override
	protected String getSubmitPage() {
		return null;
	}
	
	@Override
	protected void visit() {
	// avoid to log in history
	}
	
	@Override
	protected boolean useLabels() {
		return false;
	}

	@Override
	protected String getCanvasView() {
		return null;
	}

	protected String getRedirect() {
		return null;
	}
}
