/* **********************************************************************
 * WebBee Copyright 2012 Dmitriy Rogatkin. All rights reserved.
 * Web application building blocks library. 
 *************************************************************************/
package com.beegman.webbee.block;

import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpServletResponse;

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
		parseOperationAttributes();
		returnCode = HttpServletResponse.SC_OK;
	}

	protected void parseOperationAttributes() {
		object = null;
		key = null;
		source = null;
		String restReq = req.getPathInfo();
		if (restReq != null && restReq.length() > 0) {
			String[] reqParams = restReq.split("/");
			switch (reqParams.length) {
			case 1:
				key = reqParams[0];
				break;
			case 2:
				object = reqParams[0];
				key = reqParams[1];
				break;
			default:
				log("Warning: request parameters are more (%d) than parsed", null, reqParams.length);
			case 3:
				source = reqParams[0];
				object = reqParams[2];
				key = reqParams[1];
				break;
			}
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
			break;
		case Read:
			result = loadModel(null);
			break;
		case Create:
			result = storeModel(readModel());
			break;
		case Delete:
			result = deleteModel(null);
			break;
		default:
			break;
		}
		resp.setStatus(returnCode);
		if (noTemplate()) {
			log("return %s", null, result);
			return result;
		}
		HashMap<String, Object> pageModel = new HashMap<String, Object>(10);
		pageModel.put(MODEL, result);
		return pageModel;
	}

	@RequiresOverride
	protected I newModel() {
		return null;
	}

	@RequiresOverride
	protected O loadModel(I in) {

		return null;
	}

	@RequiresOverride
	protected O storeModel(I in) {

		return null;
	}

	@RequiresOverride
	protected I readModel() {
		I im = null;
		try {
			im = newModel();
			fillPojo(im,
					Json.createReader(
							new InputStreamReader(req.getInputStream(), DataConv.ifNull(getEncoding(), "utf-8")))
							.readObject());
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

	protected void fillPojo(Object pojo, JsonObject json) {
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
					if (type.isArray()) {
						f.set(pojo, fillArray(json.getJsonArray(n), type.getComponentType(), false));
					} else
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
					else
						log("Unsupported type of preference value: %s for %s", null, type, n);
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
				log("Couldn't populate value to %s", e, n);
			}
		}
	}

	protected Object fillArray(JsonArray jsonArray, Class<?> componentType, boolean cs) {
		Object[] res = (Object[]) Array.newInstance(componentType, jsonArray.size());
		for (int k = 0; k < res.length; k++)
			if (componentType.isPrimitive()) {

			} else if (componentType == String.class)
				res[k] = jsonArray.getString(k);
			else
				fillPojo(res[k], jsonArray.getJsonObject(k));
		return res;
	}

	// TODO migrate the methods in JSON operating util class
	protected JsonArrayBuilder toJsonArr(Object objs) {
		JsonArrayBuilder jab = Json.createArrayBuilder();
		if (objs instanceof String[]) {
			String[] sa = (String[]) objs;
			for (String s : sa)
				jab.add(s);
		}
		return jab;
	}

	protected String toJson(Object pojo, boolean reverse, String... fieldNames) {
		JSONDateUtil du = null;
		JsonObjectBuilder ob = Json.createObjectBuilder();

		HashSet<String> ks = new HashSet<String>();
		for (String s : fieldNames)
			ks.add(s);
		for (Field f : pojo.getClass().getFields()) {
			FormField a = f.getAnnotation(FormField.class);
			String name = f.getName();
			if (a != null) {
				if (ks.contains(name) ^ reverse)
					continue;
				if (!a.formFieldName().isEmpty())
					name = a.formFieldName();
			} else
				continue;
			try {
				Class<?> type = f.getType();
				if (type == String.class) {
					ob.add(name, DataConv.objectToString(f.get(pojo)));
				} else if (type == int.class) {
					ob.add(name, f.getInt(pojo));
				} else if (type == boolean.class) {
					ob.add(name, f.getBoolean(pojo));
				} else if (type == long.class) {
					ob.add(name, f.getLong(pojo));
				} else if (type == float.class) {
					ob.add(name, f.getFloat(pojo));
				} else if (type == double.class) {
					ob.add(name, f.getDouble(pojo));
				} else if (type == Date.class) {
					if (du == null)
						du = new JSONDateUtil();
					if (f.get(pojo) != null)
						ob.add(name, du.toJSON((Date) f.get(pojo)));
				} else if (type.isArray()) {
					if (type.getComponentType() == String.class)
						ob.add(name, toJsonArr(f.get(pojo)));
					else
						log("Unsupported type for %s for %s", null, type.getComponentType(), name);
				} else {
					log("Unsupported type for %s for %s", null, type, name);
				}
			} catch (Exception e) {
				if (e instanceof IllegalArgumentException)
					throw (IllegalArgumentException) e;
				log("A problem in filling JSON object", e);
			}
		}
		return ob.build().toString();

	}
}
