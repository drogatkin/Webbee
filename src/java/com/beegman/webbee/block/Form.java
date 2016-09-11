/* **********************************************************************
 * WebBee Copyright 2009 Dmitriy Rogatkin. All rights reserved.
 * Web application building blocks library. 
 *************************************************************************/
package com.beegman.webbee.block;

import java.util.HashMap;
import java.util.Map;

import org.aldan3.annot.RequiresOverride;

import com.beegman.webbee.base.BaseBlock;
import com.beegman.webbee.model.AppModel;
import com.beegman.webbee.util.AjaxHandlers;

public class Form<T, A extends AppModel> extends BaseBlock<A> {

	private boolean disableValidCascading;

	protected String errorMsg;

	@Override
	protected Object doControl() {
		disableValidCascading = false;
		errorMsg = "";
		T model = getFormModel();
		fillModel(model);
		if (errorMsg.length() == 0) {
			Object errorResult = postValidate(model);
			if (errorResult instanceof String) {
				errorMsg = (String) errorResult;
				if (errorMsg.length() == 0) {
					errorResult = storeModel(model);
					if (errorResult instanceof String) {
						errorMsg = (String) errorResult;
						if (errorMsg.length() == 0)
							return null;
					} else { // TODO reconsider check to Map
						if (errorResult == null || errorResult instanceof Map)
							return errorResult;
						errorMsg = errorResult.toString();
					}
				}
			} else { // TODO reconsider check to Map
				if (errorResult == null || errorResult instanceof Map)
					return errorResult;
				errorMsg = errorResult.toString();
			}
		}
		if(noTemplate())
			return errorMsg;
		HashMap pageModel = new HashMap(10);
		// TODO add method for model redisplay preparation, like cleaning password fields
		// sanitize(model);
		// TODO postValidate has to do above
		pageModel.put(MODEL, model);
		pageModel.put(Variable.ERROR, errorMsg);
		return pageModel;
	}

	@Override
	protected Object getModel() {
		HashMap<String, Object> pageModel = new HashMap<String, Object>(10);
		T model = getFormModel();
		// TODO message population code move to base block prefill standard variables
		String message = (String) req.getAttribute(MESSAGE);
		if (message != null)
			pageModel.put(Variable.ERROR, getResourceString(message, message));
		if (needFillBeforeLoad()) { // prefill model with possible parameters,like id
			disableValidCascading = true;
			fillModel(model);
		}
		pageModel.put(MODEL, loadModel(model));
		return pageModel;
	}

	@Override
	protected String getSubmitPage() {
		if (navigation == null) 
			return req.getContextPath() == null || req.getContextPath().length() == 0?"/":req.getContextPath();
			// TODO make it behavior configur
		return navigation;
	}

	/** this method has to be overridden in an inherited class
	 * 
	 * @return
	 */
	@RequiresOverride
	protected T getFormModel() {
		return null;
	}

	/** this method has to be overridden in inherited class
	 * 
	 * @param model
	 * @return
	 */
	protected Object storeModel(T model) {
		return null;
	}

	/** this method has to be overridden in inherited class
	 * 
	 * @param model
	 * @deprecated
	 */
	protected void loadFormModel(T model) {

	}

	/** this method has to be overridden in inherited class
	 * 
	 * @param model
	 * 
	 */
	protected T loadModel(T model) {
		loadFormModel(model);
		return model;
	}
	
	/** tells if model needs be pre-populated with web values before load
	 * 
	 * @return
	 */
	protected boolean needFillBeforeLoad() {
		return true;
	}
	
	/** this method has to be overridden in inherited class
	 * 
	 * @param model
	 * @return
	 */
	protected Object postValidate(T model) {
		return "";
	}
	
	public Object processAutosuggestCall() {
		return commonAjaxProcess("autosuggest", true);
	}

	public Object processCascadingCall() {
		return commonAjaxProcess("cascaded", false);
	}

	@Override
	protected boolean reportValidation(String name, String value, Exception problem) {
		if (disableValidCascading)
			return false;
		errorMsg += String.format(getResourceString("err_pat", "%s (%s: %s) "), problem.getMessage(), name, value);
		return false;
		//return super.reportValidation(name, value, problem);
	}

	protected T fillModelNoValidation() {
		T model = getFormModel();
		disableValidCascading = true;
		fillModel(model);
		return model;
	}

	protected HashMap commonAjaxProcess(String fieldName, boolean auto) {
		return AjaxHandlers.commonAjaxProcess(getParameterValue(fieldName, "", 0), auto,
				fillModelNoValidation(), req, getAppModel(), getTimeZone(), getLocale());
	}
}
