package com.beegman.webbee.util;

import java.util.HashMap;
import java.util.StringTokenizer;

import org.aldan3.annot.DBField;
import org.aldan3.annot.FormField;
import org.aldan3.data.DODelegator;

public class DODelegatorEx<T> extends DODelegator<T> {

	public DODelegatorEx(T model, String exclude) {
		super(model);
		fieldsMap = new HashMap<String, java.lang.reflect.Field>();
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

	public DODelegatorEx(T model) {
		this(model, null);
	}

}
