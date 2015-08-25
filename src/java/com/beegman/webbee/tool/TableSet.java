/* **********************************************************************
 * WebBee Copyright 2011 Dmitriy Rogatkin. All rights reserved.
 * Web application building blocks library. 
 *************************************************************************/
package com.beegman.webbee.tool;

import java.lang.reflect.Field;
import java.util.HashSet;

import org.aldan3.annot.DBField;
import org.aldan3.annot.DataRelation;
import org.aldan3.data.DOService;
import org.aldan3.data.SimpleDataObject;
import org.aldan3.data.util.AnnotField;
import org.aldan3.model.ProcessException;

public class TableSet {
	String storeName;
	HashSet<org.aldan3.model.Field> fields;
	
	public TableSet(Class<?> tableClass) {
		DataRelation relation = tableClass.getAnnotation(DataRelation.class);
		if (relation == null)
			throw new IllegalArgumentException("The class "+tableClass.getName()+" wasn't annotated as DataRelation");
		if (relation.table().length() == 0) {
			storeName = tableClass.getName();
			int dp = storeName.lastIndexOf('.');
			if (dp > 0)
				storeName = storeName.substring(dp+1);
		} else
			storeName = relation.table();
		processField(tableClass);
	}
	
	protected void processField(Class<?> tableClass) {
		Field[] members = tableClass.getFields();
		fields = new HashSet<org.aldan3.model.Field>(members.length);
		for (Field member:members) {
			DBField df = member.getAnnotation(DBField.class);
			if (df == null)
				continue;
			fields.add(new AnnotField(member));
		}
	}

	public void createTable(DOService dos, boolean update) throws ProcessException {
		if (update)
			dos.modifyStorageFor(storeName, fields);
		else
			dos.createStorageFor(storeName, fields);
	}
	
	public void dropTable(DOService dos) throws ProcessException {
		dos.deleteStorageFor(new SimpleDataObject() {

			@Override
			public String getName() {
				return storeName;
			}			
		});
	}

	@Override
	public String toString() {
		return "TableSet [storeName=" + storeName + ", fields=" + fields + "]";
	}
	
	// TODO add a method for altering table accordingly new  field set
}
