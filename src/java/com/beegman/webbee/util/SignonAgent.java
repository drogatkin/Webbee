/* **********************************************************************
 * WebBee Copyright 2009 Dmitriy Rogatkin. All rights reserved.
 * Web application building blocks library. 
 *************************************************************************/
package com.beegman.webbee.util;

import javax.servlet.http.HttpServletRequest;

import com.beegman.webbee.model.AppModel;

/** can authenticate a requester by analyzing request parameters
 * 
 * @author dmitriy
 *
 * @param <T>
 */
public interface SignonAgent<T extends AppModel> {
	/** returns true if a request can be signed in
	 * It takes responsibility to populate all related to a signed entity information
	 * 
	 * @param req
	 * @param appModel
	 * @return
	 */
	boolean signon(HttpServletRequest req, T appModel);
}
