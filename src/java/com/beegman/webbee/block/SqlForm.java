/* **********************************************************************
 * WebBee Copyright 2009 Dmitriy Rogatkin. All rights reserved.
 * Web application building blocks library. 
 *************************************************************************/

package com.beegman.webbee.block;

import org.aldan3.annot.DataRelation;
import org.aldan3.data.DODelegator;
import org.aldan3.data.util.DataObjectWrapper;
import org.aldan3.data.util.Filter;
import org.aldan3.model.Coordinator;
import org.aldan3.model.DataObject;
import org.aldan3.model.ProcessException;

import com.beegman.webbee.model.AppModel;
import com.beegman.webbee.model.Appearance;
import com.beegman.webbee.util.PageRef;

// TODO rename to Sqlform
public class SqlForm<T, A extends AppModel> extends Form<T, A> implements Coordinator {

	@Override
	protected void loadModel(T model) {
		DataRelation dr = getClass().getAnnotation(DataRelation.class);
		if (dr == null)
			throw new RuntimeException("No DataRelation is defined, the operation isn't possible");
		DataObject dataObject = model instanceof DataObject ? (DataObject) model : new DODelegator(model);
		applyFilters(dr.filters(), dataObject);
		String keyString = getDefinedKeys(model, dr.keys(), true);
		if (keyString.length() > 0)
			try {
				if (model instanceof DataObject == false)
					dataObject = new DODelegator(model, getTableName(dr.table(), model), "", keyString);
				getAppModel().getDOService().getObjectLike(dataObject);
			} catch (ProcessException e) {
				modelInsert(Variable.ERROR, ""+e); // localized standard error messaging
				log("", e);
			}
	}

	protected String[] applyFilters(Class[] filterClasses, DataObject dataObject) {
		String ret[] = new String[filterClasses.length];
		for (int l = 0; l < filterClasses.length; l++) {
			try {
				Class<Filter> filterClass = filterClasses[l];
				Filter filter = filterClass.newInstance();
				dataObject.modifyField(ret[l] = filter.getName(), filter.getValue(this));
			} catch (Exception e) {
				log("", e);
			}
		}
		return ret;
	}

	protected boolean isValueDefined(Object fieldValue) {
		if (fieldValue == null)
			return false;
		if (fieldValue instanceof Number)
			return ((Number) fieldValue).longValue() > 0;
		return true;
	}

	@Override
	protected Object storeModel(T model) {
		final DataRelation dr = getClass().getAnnotation(DataRelation.class);
		if (dr == null)
			throw new RuntimeException("No DataRelation is defined, the operation isn't possible");
		massageModel(model);
		try {
			DataObject dataObject = null;
			//DataObject patternDataObject = null;
			String keys = getDefinedKeys(model, dr.keys(), false);
			if (model instanceof DataObject) {
				dataObject = (DataObject) model; // TODO use a wrapper with exclusion
			} else {
				dataObject = new DODelegator(model, getTableName(dr.table(), model), "", null);
			}
			dataObject = massageDO(dataObject);
			// TODO create a pattern with inclusion  ...
			// populate filters
			final String filterNames[] = applyFilters(dr.filters(), dataObject);

			if (keys.length() == 0)
				getAppModel().getDOService().addObject(dataObject);
			else
				getAppModel().getDOService().updateObjectsLike(new DataObjectWrapper(dataObject) {

					@Override
					public boolean containData(String name) {
						log("Checking data:" + name, null);
						for (String key : dr.keys())
							if (key.equals(name)) // TODO if has data
								return true;
						for (String key : filterNames)
							if (key.equals(name))
								return true;
						log("Not found:" + name, null);
						return false;
					}
				}, dataObject);
		} catch (ProcessException e) {
			log("", e);
			return "" + e;
		}
		navigation = onSubmit();
		return "";
	}

	protected String getDefinedKeys(T model, String[] keys, boolean allReq) {
		String keyString = "";
		for (String key : keys) {
			if (isValueDefined(getFieldValue(key, model))) {
				if (keyString.length() > 0)
					keyString += ',';
				keyString += key;
			} else if (allReq)
				return "";
		}
		return keyString;
	}

	protected void massageModel(T model) {

	}

	protected DataObject massageDO(DataObject dataObject) {
		return dataObject;
	}

	protected String onSubmit() {
		PageRef pr = Appearance.popup.equals(appearance)?null:popHistory(2);
		return pr != null?pr.toString():null;
	}

	protected Object getFieldValue(String name, T model) {
		if (model == null)
			return null;
		if (model instanceof DataObject)
			return ((DataObject) model).get(name);
		try {
			return model.getClass().getField(name).get(model);
		} catch (Exception e) {
			log("", e);
		}
		return null;
	}

	protected String getTableName(String tableName, T model) {
		if (tableName.length() > 0)
			return tableName;
		Class modelClass = model.getClass();
		DataRelation dr = (DataRelation) modelClass.getAnnotation(DataRelation.class);
		if (dr != null && dr.table().length() > 0)
			return dr.table();
		try {
			return modelClass.getField("NAME").get(null).toString();
		} catch (NoSuchFieldException e) {
		} catch (Exception e) {
			log("", e);
		}
		String result = modelClass.getName();
		int ld = result.lastIndexOf('.');
		if (ld > 0)
			return result.substring(ld + 1);
		return result;
	}

	@Override
	public Object getModel(String name) {
		return null;
	}

	@Override
	public Object getService(String name) {
		return this;
	}

}
