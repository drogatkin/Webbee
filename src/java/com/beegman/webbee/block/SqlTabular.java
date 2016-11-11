/* **********************************************************************
 * WebBee Copyright 2010 Dmitriy Rogatkin. All rights reserved.
 * Web application building blocks library. 
 *************************************************************************/
package com.beegman.webbee.block;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.aldan3.annot.DataRelation;
import org.aldan3.data.util.Filter;
import org.aldan3.model.DOFactory;
import org.aldan3.model.DataObject;
import org.aldan3.model.ProcessException;
import org.aldan3.util.Sql;

import com.beegman.webbee.model.AppModel;
// TODO rename to Sqltabular

/** this class generates a collection of JDO objects based on used query in DataRelation
 * 
 * @author dmitriy
 *
 * @param <D>
 * @param <A>
 * 
 * <p>
 * Important: keys get directly used for getting request parameters and populating in a query.
 * It can generates a SQL injection problem. To address that either request a data conversion in a query itself.
 * Note that all request parameters get sanitized to present valid SQL inside of ''.
 * A key name can be defined as name:type. In this case type can be check and sanitized. The following types are allowed:
 * <ul>
 *   <li>date
 *   <li>number
 * </ul>  
 * 
 * 
 */
public class SqlTabular<D extends DataObject, A extends AppModel> extends Tabular<Collection<D>, A> {

	@Override
	protected Collection<D> getTabularData(long pos, int size) {
		DataRelation dr = getClass().getAnnotation(DataRelation.class);
		if (dr == null)
			throw new RuntimeException("No DataRelation is defined, the operation isn't possible");
		String query = getQuery(dr.query());
		// TODO it can be resource name and look for actual query in resource for db portability
		TreeMap<String, String> paramsMap = new TreeMap<String, String> (new Comparator<String>() {

			@Override
			public int compare(String s1, String s2) {
				int result = s2.length()-s1.length();
				if (result == 0)
					return s1.compareTo(s2);
				return result;
			}}); 
		
		for (String key : dr.keys()) {
			KeyAndType kat = KeyAndType.parse(key);
			paramsMap.put(kat.key, getFieldSQLData(kat.key, kat.type));
		}			 
		
		for (Class<? extends Filter> filterClass : dr.filters()) {
			try {
				Filter filter = getAppModel().inject(filterClass.newInstance());
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

	/** this method allows customize query
	 * 
	 * @param query
	 * @return
	 */
	protected String getQuery(String query) {
		return query;
	}

	protected DOFactory getDOFactory() {
		return null;
	}

	private String getFieldSQLData(String key, String type) {
		Object pv = getObjectParameterValue(key, "", -1, false);
		if (pv != null && pv.getClass().isArray()) {
			if (((Object[])pv).length == 1)
					pv = ((Object[])pv)[0];
		}
		if (doPreserveKeys()) 
			modelInsert(key, pv);
		if("number".equals(type)) {
			if (! (pv instanceof Number)) {
			    if (!pv.toString().matches("[+-]?([0-9]*\\.?[0-9]+|[0-9]+\\.?[0-9]*)([eE][+-]?[0-9]+)?"))
			    	throw new IllegalArgumentException("Expected '"+pv+"' as a number");
			}
		} else if( "date".equals(type)) {
			if (!(pv instanceof Date)) {
				
			}
		}
		return Sql.toSqlValue(pv, getAppModel().getDOService()
				.getInlineDatePattern());
	}

	protected boolean doPreserveKeys() {
		return false;
	}
	
	static class KeyAndType {
		String key;
		
		String type;

		public static KeyAndType parse(String s) {
			KeyAndType res = new KeyAndType();
			int sp = s.indexOf(':');
			if (sp > 0) {
				res.key = s.substring(0, sp);
				res.type = s.substring(sp+1);
			} else {
				res.key = s;
				res.type = "";
			}
			return res;
		}
	}
}
