package com.beegman.webbee.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;

import org.aldan3.annot.DBField;
import org.aldan3.annot.FormField;
import org.aldan3.data.DODelegator;
import org.aldan3.model.Log;

public class DODelegatorEx<T> extends DODelegator<T> {

	public DODelegatorEx(T model, String exclude) {
		super(model);
		fieldsMap.clear();
		for (java.lang.reflect.Field f : principal.getClass().getFields()) {
			FormField ff = f.getAnnotation(FormField.class);
			if (f.getAnnotation(DBField.class) != null || ff != null) {

				String dbFiledName = ff != null && ff.dbFieldName().length() > 0 ? ff.dbFieldName() : f.getName();
				if (fieldsMap.containsKey(dbFiledName))
					throw new IllegalArgumentException("Ambiguous db fields " + f + " and "
							+ fieldsMap.get(dbFiledName));
				fieldsMap.put(normilizeFieldName(dbFiledName), f);
			}
		}
		if (exclude != null) {
			StringTokenizer st = new StringTokenizer(exclude, ",");
			while (st.hasMoreTokens())
				fieldsMap.remove(normilizeFieldName(st.nextToken()));
		}
	}
	
	/** creates a data object based on specified object with 
	 * specified collected fields, and keys fields
	 * @param fields
	 * @param keys
	 * @param model
	 * @param table
	 */
	public DODelegatorEx(String fields, String keys, T model, String table) {
		super(model);
		if (fields == null || fields.isEmpty())
			throw new IllegalArgumentException("At least one data containing field has to be specified");
		
		HashMap<String, java.lang.reflect.Field> only = new HashMap<String, java.lang.reflect.Field>();
		StringTokenizer st = new StringTokenizer(fields, ",");
		while (st.hasMoreTokens()) {
			String field = normilizeFieldName( st.nextToken() );
			if (fieldsMap.containsKey(field)) {
				only.put(field, fieldsMap.get(field));
			} else
				Log.l.debug("The field %s was claimed as data wasn't in a fields list and ignored", field);
		}
		if (only.isEmpty())
			throw new IllegalArgumentException("No one of specified fileds belongs to  data object");
		if (keys != null) {
			selectedData = new HashSet<String>();
			st = new StringTokenizer(keys, ",");
			while (st.hasMoreTokens()) {
				String field = normilizeFieldName(st.nextToken());
				if (fieldsMap.containsKey(field))
					selectedData.add(field);
				else
					Log.l.debug("The field %s was claimed as key wasn't in fields list and ignored", field);
			}
		}
		fieldsMap = only;
	}

	public DODelegatorEx(T model) {
		this(model, null);
	}

}
