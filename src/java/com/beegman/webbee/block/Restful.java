/* **********************************************************************
 * WebBee Copyright 2012 Dmitriy Rogatkin. All rights reserved.
 * Web application building blocks library. 
 *************************************************************************/
package com.beegman.webbee.block;

import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

import javax.json.Json;
import javax.json.JsonObject;

import org.aldan3.annot.FormField;
import org.aldan3.annot.RequiresOverride;
import org.aldan3.util.DataConv;

import com.beegman.webbee.base.BaseBlock;
import com.beegman.webbee.model.AppModel;
import com.beegman.webbee.util.JSONDateUtil;

public class Restful<I, O, A extends AppModel> extends BaseBlock<A> {

	public enum restful_op {
		Create, Read, Update, Delete
	};

	transient protected restful_op op;

	transient protected String key;
	transient protected String source;
	transient protected String object;
	transient protected int returnCode;
	transient JSONDateUtil dateUtil;

	@Override
	protected void start() {
		super.start();
		String m = req.getMethod().toUpperCase();
		// TODO override with possible operation specified as parameter
		if ("GET".equals(m))
			op = restful_op.Read;
		else if ("POST".equals(m))
			op = restful_op.Update;
		else if ("PUT".equals(m))
			op = restful_op.Create;
		else if ("DELETE".equals(m))
			op = restful_op.Delete;
		String restReq = req.getPathInfo();
		if (restReq != null && restReq.length() > 0) {
			String[] reqParams = restReq.split("/");
		}
	}

	@Override
	protected boolean useLabels() {
		return false;
	}

	@Override
	protected String getCanvasView() {
		return null;
	}

	@Override
	protected Object doControl() {
		throw new UnsupportedOperationException("Here is no controller for RESTFul services");
	}

	@Override
	protected Object getModel() {
		Object result = null;
		switch (op) {
		case Update:
			result = storeModel(null);
		case Read:
			result = loadModel(null);
		case Create:
			result = storeModel(readModel());
		case Delete:
			result = deleteModel(null);
		default:
			break;
		}
		if (noTemplate()) {
			return result;
		}
		HashMap<String, Object> pageModel = new HashMap<String, Object>(10);
		pageModel.put(MODEL, result);
		return pageModel;
	}

	protected I newModel() {
		return null;
	}
	
	@RequiresOverride
	protected O loadModel(I in) {
		// TODO Auto-generated method stub
		return null;
	}

	@RequiresOverride
	protected O storeModel(I in) {

		return null;
	}

	@RequiresOverride
	protected I readModel() {
		I im =  null;
		try {
			im = newModel();
			fillPojo(im, Json.createReader(new InputStreamReader(req.getInputStream(), DataConv.ifNull(getEncoding(), "utf-8"))).readObject());
		} catch (Exception e) {
			log("", e);
		}

		return im;
	}

	@Override
	protected String getSubmitPage() {
		return navigation;
	}

	@RequiresOverride
	protected O deleteModel(I in) {
		return null;
	}

	protected String getKeyParameterName() {
		return "id";
	}
	
	protected void fillPojo(I pojo, JsonObject json) {
		for (Field f : pojo.getClass().getFields()) {
		FormField ff = f.getAnnotation(FormField.class);
		if (ff == null)
			continue;
		String n = f.getName();
		
		n = ff.formFieldName().isEmpty() ? n : ff.formFieldName();
		if (!json.containsKey(n))
			continue;
		Class<?> type = f.getType();
		try {
			if (type.isArray() || type.isAssignableFrom(Collection.class)) {
				//fillArray(json.getJSONArray(n), type.getComponentType(), false);
					log("Collections are not supported for %s", null, n);
				continue;
			}
			if (type.isPrimitive()) {
				if (type == char.class || type == int.class || type == short.class)
					f.setInt(pojo, json.getInt(n));
				else if (type == boolean.class)
					f.setBoolean(pojo, json.getBoolean(n));
				else if (type == long.class)
					f.setLong(pojo, json.getJsonNumber(n).longValue());
				else if (type == float.class)
					f.setFloat(pojo, (float) json.getJsonNumber(n).doubleValue());
				else if (type == double.class)
					f.setDouble(pojo, json.getJsonNumber(n).doubleValue());
				else log( "Unsupported type of preference value: %s for %s", null,  type,  n);
			} else {
				if (type == String.class)
					f.set(pojo, json.getString(n));
				else if (type == Date.class) {
					if (dateUtil == null)
						dateUtil = new JSONDateUtil();
					String v = json.getString(n);
					if (DataConv.hasValue(v))
						f.set(pojo, null);
					else
						f.set(pojo, dateUtil.parse(v));
				} else
					f.set(pojo, json.get(n));
			}
		} catch (Exception e) {
			log("Couldn't populate value to %%s", e, n);
		}
		}
	}
}
