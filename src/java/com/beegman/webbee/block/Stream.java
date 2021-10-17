/* **********************************************************************
 * WebBee Copyright 2009 Dmitriy Rogatkin. All rights reserved.
 * Web application building blocks library. 
 *************************************************************************/
package com.beegman.webbee.block;

import java.io.IOException;
import java.io.OutputStream;

import com.beegman.webbee.base.BaseBlock;
import com.beegman.webbee.model.AppModel;

public class Stream<A extends AppModel> extends BaseBlock<A> {

	@Override
	protected Object doControl() {
		throw new UnsupportedOperationException("The Stream doesn't provide control ability");
	}

	@Override
	protected Object getModel() {
		setHeaders();
		try {
			fillStream(resp.getOutputStream());
		} catch (IOException e) {
			log("IO exception:", e);
		} catch(Throwable t) {
			if (t instanceof ThreadDeath)
				throw (ThreadDeath)t;
			log("Unhandled exception:", t);
		}
		return null;
	}

	@Override
	protected final String getSubmitPage() {
		throw new UnsupportedOperationException("The Stream doesn't provide control ability");
	}
	
	
	@Override
	protected boolean useLabels() {
		return false;
	}

	/** Override the method for filling a stream with data
	 * 
	 * @param os
	 */
	protected void fillStream(OutputStream os) throws IOException {
		
	}
	
	/** Override the method to setup specific headers
	 * 
	 */
	protected void setHeaders() {
		
	}
}
