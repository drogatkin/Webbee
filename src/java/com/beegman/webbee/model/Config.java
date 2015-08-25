/* **********************************************************************
 * WebBee Copyright 2009 Dmitriy Rogatkin. All rights reserved.
 * Web application building blocks library. 
 *************************************************************************/
package com.beegman.webbee.model;

import org.aldan3.data.SimpleDataObject;

/** This class gives an alternative configuration abilities instead
 * of using inheritance
 * @author Dmitriy
 *
 */
public class Config extends SimpleDataObject {

	public static final String SIGNON_AGENT = "SIGNON_AGENT";

	public static final String PROHIBITED_ACCESS = "PROHIBITED_ACCESS";
}
