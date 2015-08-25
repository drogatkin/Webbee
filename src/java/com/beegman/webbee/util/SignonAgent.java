/* **********************************************************************
 * WebBee Copyright 2009 Dmitriy Rogatkin. All rights reserved.
 * Web application building blocks library. 
 *************************************************************************/
package com.beegman.webbee.util;

import javax.servlet.http.HttpServletRequest;

import com.beegman.webbee.model.AppModel;

public interface SignonAgent<T extends AppModel> {
	boolean signon(HttpServletRequest req, T appModel);
}
