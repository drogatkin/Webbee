/* **********************************************************************
 * WebBee Copyright 2009 Dmitriy Rogatkin. All rights reserved.
 * Web application building blocks library. 
 *************************************************************************/
package com.beegman.webbee.util;

import java.util.ArrayList;
import java.util.Collection;

import org.aldan3.annot.OptionMap;
import org.aldan3.data.DODelegator;
import org.aldan3.data.util.FieldFiller;
import org.aldan3.model.Coordinator;
import org.aldan3.model.DataObject;

@OptionMap(valueMap="id")
public abstract class GenericNumOptions implements FieldFiller<Collection<DataObject>, Coordinator> {
	@Override
	public Collection<DataObject> fill(Coordinator coord, String filter) {
		ArrayList<DataObject> list = new ArrayList<DataObject>();
		for (int i = getStart(), n = getEnd(),s = getStep(); i != n; i+=s) {
			list.add(new DODelegator(new Option<Integer,String>(i, formatNum(i))));
		}
		return list;
	}

	abstract public int getStart();

	abstract public int getEnd();

	protected int getStep() {
		return 1;
	}

	protected String formatNum(int n) {
		return "" + n;
	}
}
