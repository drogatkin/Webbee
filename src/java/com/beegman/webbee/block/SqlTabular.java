/* **********************************************************************
 * WebBee Copyright 2010 Dmitriy Rogatkin. All rights reserved.
 * Web application building blocks library. 
 *************************************************************************/
package com.beegman.webbee.block;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.aldan3.annot.DataRelation;
import org.aldan3.data.util.Filter;
import org.aldan3.model.Coordinator;
import org.aldan3.model.DOFactory;
import org.aldan3.model.DataObject;
import org.aldan3.model.ProcessException;
import org.aldan3.util.Sql;

import com.beegman.webbee.model.AppModel;
// TODO rename to Sqltabular
public class SqlTabular<D extends DataObject, A extends AppModel> extends Tabular<Collection<D>, A> implements
		Coordinator {

	@Override
	protected Collection<D> getTabularData(long pos, int size) {
		DataRelation dr = getClass().getAnnotation(DataRelation.class);
		if (dr == null)
			throw new RuntimeException("No DataRelation is defined, the operation isn't possible");
		String query = dr.query();
		// TODO it can be resource name and look for actual query in resource for db portability
		TreeMap<String, String> paramsMap = new TreeMap<String, String> (new Comparator<String>() {

			@Override
			public int compare(String s1, String s2) {
				int result = s2.length()-s1.length();
				if (result == 0)
					return s1.compareTo(s2);
				return result;
			}}); 
		
		for (String key : dr.keys())//{
			paramsMap.put(key, getFieldSQLData(key)); //log("putting %s:%s", null, key, getFieldSQLData(key));}
		Class filterClasses[] = dr.filters();
		for (Class<Filter> filterClass : filterClasses) {
			try {
				Filter filter = filterClass.newInstance();
				paramsMap.put(filter.getName(), Sql.toSqlValue(filter.getValue(this), getAppModel()
						.getDOService().getInlineDatePattern()));
			} catch (Exception e) {
				log("", e);
				throw new RuntimeException("Problem in applying filters", e);
			}
		}
		//log("Param map:%s", null, paramsMap);
		for (Iterator<Map.Entry<String,String>> i = paramsMap.entrySet().iterator(); i.hasNext();) {
			Map.Entry<String,String> e = i.next();
			query = query.replaceAll(":" + e.getKey(), e.getValue());
			//log("Applying '%s'/%s", null, e.getKey(), query);
		}
		try {
			return (Collection<D>) getAppModel().getDOService().getObjectsByQuery(query, pos, size, getDOFactory());
		} catch (ProcessException e) {
			log("", e);
		}
		return new ArrayList<D>();
	}

	protected DOFactory getDOFactory() {
		return null;
	}

	private String getFieldSQLData(String key) {
		Object pv = getObjectParameterValue(key, "", -1, false);
		if (pv != null && pv.getClass().isArray()) {
			if (((Object[])pv).length == 1)
					pv = ((Object[])pv)[0];
		}
		if (doPreserveKeys()) 
			modelInsert(key, pv);
		return Sql.toSqlValue(pv, getAppModel().getDOService()
				.getInlineDatePattern());
	}

	@Override
	public Object getModel(String name) {
		return null;
	}

	@Override
	public Object getService(String name) {
		return SqlTabular.this;
	}

	protected boolean doPreserveKeys() {
		return false;
	}
}
